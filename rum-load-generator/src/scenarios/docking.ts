import { BaseScenario } from './base-scenario.js';
import { humanDelay } from '../utils/timing.js';

export class DockingScenario extends BaseScenario {
  name = 'docking';

  async execute(): Promise<void> {
    // Navigate to docking page
    await this.navigate('/docking');

    // Wait for page to load
    await this.page.waitForLoadState('networkidle');
    await humanDelay(this.page);

    // Scroll to view bays
    await this.page.evaluate(() => window.scrollBy(0, 300));
    await humanDelay(this.page);

    // Randomly perform an action
    const action = this.randomChoice(['dock', 'undock', 'view']);

    switch (action) {
      case 'dock':
        await this.attemptDock();
        break;
      case 'undock':
        await this.attemptUndock();
        break;
      case 'view':
        // Just viewing - scroll around
        await this.page.evaluate(() => window.scrollBy(0, 300));
        await humanDelay(this.page);
        break;
    }
  }

  private async attemptDock(): Promise<void> {
    // Look for "Initiate Docking" button (for incoming ships)
    const dockButton = this.page.locator('button:has-text("Initiate Docking")').first();

    if (await dockButton.isVisible({ timeout: 2000 }).catch(() => false)) {
      await dockButton.click();
      this.log('Clicked dock button');
      await this.page.waitForLoadState('networkidle');
      await humanDelay(this.page);
    } else {
      this.log('No ships available for docking');
    }
  }

  private async attemptUndock(): Promise<void> {
    // Look for "Undock Vessel" button (for docked ships)
    const undockButton = this.page.locator('button:has-text("Undock Vessel")').first();

    if (await undockButton.isVisible({ timeout: 2000 }).catch(() => false)) {
      await undockButton.click();
      this.log('Clicked undock button');
      await this.page.waitForLoadState('networkidle');
      await humanDelay(this.page);
    } else {
      this.log('No ships to undock');
    }
  }
}
