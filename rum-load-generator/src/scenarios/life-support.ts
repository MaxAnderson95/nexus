import { BaseScenario } from './base-scenario.js';
import { humanDelay } from '../utils/timing.js';

export class LifeSupportScenario extends BaseScenario {
  name = 'life-support';

  async execute(): Promise<void> {
    await this.navigate('/life-support');
    await this.page.waitForLoadState('networkidle');
    await humanDelay(this.page);

    // Scroll to view sections
    await this.page.evaluate(() => window.scrollBy(0, 300));
    await humanDelay(this.page);

    // Randomly perform an action
    const action = this.randomChoice(['selfTest', 'alerts', 'view']);

    switch (action) {
      case 'selfTest':
        await this.runSelfTest();
        break;
      case 'alerts':
        await this.manageAlerts();
        break;
      case 'view':
        await this.page.evaluate(() => window.scrollBy(0, 200));
        await humanDelay(this.page);
        break;
    }
  }

  private async runSelfTest(): Promise<void> {
    // Look for self-test button
    const selfTestButton = this.page.locator('button:has-text("Self-Test"), button:has-text("Run Test")').first();

    if (await selfTestButton.isVisible({ timeout: 2000 }).catch(() => false)) {
      await selfTestButton.click();
      this.log('Running self-test');

      // Wait for test to complete (with timeout)
      await this.page.waitForTimeout(3000);
      await humanDelay(this.page);
    } else {
      this.log('No self-test button found');
    }
  }

  private async manageAlerts(): Promise<void> {
    // Look for alerts tab or section
    const alertsElement = this.page.locator('text="Alerts", button:has-text("Alerts")').first();

    if (await alertsElement.isVisible({ timeout: 2000 }).catch(() => false)) {
      await alertsElement.click();
      this.log('Viewing alerts');
      await humanDelay(this.page);

      // Try to acknowledge an alert if present
      const acknowledgeButton = this.page.locator('button:has-text("Acknowledge"), button:has-text("Dismiss")').first();
      if (await acknowledgeButton.isVisible({ timeout: 2000 }).catch(() => false)) {
        await acknowledgeButton.click();
        this.log('Acknowledged alert');
        await humanDelay(this.page);
      }
    }
  }
}
