import type { Page } from 'playwright';
import { loadConfig } from '../config.js';

/**
 * Ensures RUM data is flushed before browser/page closes.
 *
 * The Dash0 Web SDK batches data and sends it periodically or on page unload.
 * We need to ensure data is sent before we close the browser programmatically.
 */
export async function flushRumData(page: Page): Promise<void> {
  const config = loadConfig();

  try {
    // Trigger visibility change and pagehide events which typically flush RUM data
    await page.evaluate(() => {
      document.dispatchEvent(new Event('visibilitychange'));
      window.dispatchEvent(new Event('pagehide'));
    });

    // Wait for any pending network requests to complete
    await page.waitForLoadState('networkidle', { timeout: config.rumFlushTimeout });
  } catch {
    // Log but don't fail - some data may still have been sent
  }

  // Additional wait to ensure async sends complete
  await page.waitForTimeout(500);
}

/**
 * Wrapper to execute a scenario and ensure RUM data is flushed afterward
 */
export async function withRumFlush<T>(
  page: Page,
  fn: () => Promise<T>
): Promise<T> {
  try {
    return await fn();
  } finally {
    await flushRumData(page);
  }
}
