import { chromium, Browser, BrowserContext, Page } from 'playwright';
import { loadConfig, Config } from './config.js';
import { selectWeightedScenario } from './scenarios/index.js';
import { scenarioWait } from './utils/timing.js';
import { flushRumData } from './utils/rum-flush.js';
import { log } from './utils/logger.js';

interface BrowserInstance {
  id: number;
  browser: Browser;
  context: BrowserContext;
  page: Page;
  running: boolean;
}

export class BrowserPool {
  private config: Config;
  private instances: BrowserInstance[] = [];
  private shutdownRequested = false;

  constructor() {
    this.config = loadConfig();
  }

  async start(): Promise<void> {
    log('pool', 0, `Starting browser pool with ${this.config.browserCount} browsers`);

    // Launch browsers in parallel
    const launchPromises = Array.from({ length: this.config.browserCount }, (_, i) =>
      this.launchBrowser(i + 1)
    );

    this.instances = await Promise.all(launchPromises);

    // Start scenario loops for each browser
    const runPromises = this.instances.map((instance) => this.runBrowserLoop(instance));

    await Promise.all(runPromises);
  }

  async shutdown(): Promise<void> {
    log('pool', 0, 'Shutdown requested');
    this.shutdownRequested = true;

    // Flush RUM data and close all browsers
    for (const instance of this.instances) {
      try {
        instance.running = false;
        await flushRumData(instance.page);
        await instance.context.close();
        await instance.browser.close();
        log('pool', instance.id, 'Browser closed');
      } catch (error) {
        log('pool', instance.id, 'Error closing browser', { error: String(error) });
      }
    }
  }

  private async launchBrowser(id: number): Promise<BrowserInstance> {
    log('pool', id, 'Launching browser');

    const browser = await chromium.launch({
      headless: this.config.headless,
      slowMo: this.config.slowMo,
      args: [
        '--disable-dev-shm-usage',
        '--no-sandbox',
        '--disable-gpu',
      ],
    });

    const context = await browser.newContext({
      viewport: { width: 1920, height: 1080 },
      userAgent: `NexusRUMLoadGenerator/1.0 Browser/${id}`,
    });

    const page = await context.newPage();
    this.attachPageListeners(page, id);

    return { id, browser, context, page, running: true };
  }

  private attachPageListeners(page: Page, id: number): void {
    // Add console logging from browser
    page.on('console', (msg) => {
      if (msg.type() === 'error') {
        log('browser', id, `Console error: ${msg.text()}`);
      }
    });

    // Track page errors
    page.on('pageerror', (error) => {
      log('browser', id, `Page error: ${error.message}`);
    });
  }

  private async runBrowserLoop(instance: BrowserInstance): Promise<void> {
    let iterations = 0;
    const maxIterations = this.config.iterationsPerBrowser;

    // Initial navigation to base URL
    try {
      await instance.page.goto(this.config.baseUrl);
      await instance.page.waitForLoadState('networkidle');
    } catch (error) {
      log('pool', instance.id, 'Initial navigation failed', { error: String(error) });
    }

    while (instance.running && !this.shutdownRequested) {
      if (maxIterations > 0 && iterations >= maxIterations) {
        log('pool', instance.id, `Completed ${iterations} iterations, stopping`);
        break;
      }

      try {
        // Select and run a weighted random scenario
        const scenario = selectWeightedScenario(instance.page, instance.id);
        await scenario.run();
        iterations++;

        // Wait between scenarios (simulates user taking a break)
        const waitTime = scenarioWait();
        log('pool', instance.id, `Waiting ${waitTime}ms before next scenario`);
        await instance.page.waitForTimeout(waitTime);
      } catch (error) {
        log('pool', instance.id, 'Scenario error, recovering', { error: String(error) });

        // Try to recover by navigating to home
        try {
          await instance.page.goto(this.config.baseUrl);
          await instance.page.waitForTimeout(2000);
        } catch {
          // If recovery fails, recreate the page
          log('pool', instance.id, 'Recreating page after error');
          await instance.page.close().catch(() => {});
          instance.page = await instance.context.newPage();
          this.attachPageListeners(instance.page, instance.id);
        }
      }
    }
  }
}
