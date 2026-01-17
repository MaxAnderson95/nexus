import type { Page } from 'playwright';
import { humanDelay } from '../utils/timing.js';
import { flushRumData } from '../utils/rum-flush.js';
import { log } from '../utils/logger.js';

export abstract class BaseScenario {
  protected page: Page;
  protected browserId: number;

  constructor(page: Page, browserId: number) {
    this.page = page;
    this.browserId = browserId;
  }

  abstract name: string;
  abstract execute(): Promise<void>;

  protected log(message: string, data?: Record<string, unknown>): void {
    log(this.name, this.browserId, message, data);
  }

  protected async navigate(path: string): Promise<void> {
    this.log(`Navigating to ${path}`);
    await this.page.goto(path);
    await this.page.waitForLoadState('networkidle');
    await humanDelay(this.page);
  }

  protected async click(selector: string, description: string): Promise<void> {
    this.log(`Clicking: ${description}`);
    await this.page.click(selector);
    await humanDelay(this.page);
  }

  protected async clickAndWait(selector: string, description: string): Promise<void> {
    await this.click(selector, description);
    await this.page.waitForLoadState('networkidle');
  }

  protected async waitForSelector(selector: string, timeout = 10000): Promise<void> {
    await this.page.waitForSelector(selector, { timeout });
  }

  protected randomChoice<T>(items: T[]): T {
    return items[Math.floor(Math.random() * items.length)];
  }

  protected randomInt(min: number, max: number): number {
    return Math.floor(Math.random() * (max - min + 1)) + min;
  }

  async run(): Promise<void> {
    try {
      this.log('Starting scenario');
      await this.execute();
      this.log('Scenario completed');
    } catch (error) {
      this.log('Scenario failed', { error: String(error) });
      throw error;
    } finally {
      await flushRumData(this.page);
    }
  }
}
