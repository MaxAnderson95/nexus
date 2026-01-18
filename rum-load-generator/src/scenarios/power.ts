import { BaseScenario } from './base-scenario.js';
import { humanDelay } from '../utils/timing.js';

export class PowerScenario extends BaseScenario {
  name = 'power';

  async execute(): Promise<void> {
    await this.navigate('/power');
    await this.page.waitForLoadState('networkidle');
    await humanDelay(this.page);

    // Scroll to view power grid
    await this.page.evaluate(() => window.scrollBy(0, 300));
    await humanDelay(this.page);

    // Randomly perform an action
    const action = this.randomChoice(['allocate', 'deallocate', 'view']);

    switch (action) {
      case 'allocate':
        await this.allocatePower();
        break;
      case 'deallocate':
        await this.deallocatePower();
        break;
      case 'view':
        await this.page.evaluate(() => window.scrollBy(0, 300));
        await humanDelay(this.page);
        break;
    }
  }

  private async allocatePower(): Promise<void> {
    // Look for allocate button
    const allocateButton = this.page.locator('button:has-text("Allocate"), button:has-text("Add Allocation")').first();

    if (await allocateButton.isVisible({ timeout: 2000 }).catch(() => false)) {
      await allocateButton.click();
      this.log('Opening power allocation');
      await humanDelay(this.page);

      // If a modal appears, try to fill it
      const confirmButton = this.page.locator('button:has-text("Confirm"), button:has-text("Submit")').first();
      if (await confirmButton.isVisible({ timeout: 2000 }).catch(() => false)) {
        await confirmButton.click();
        this.log('Confirmed allocation');
        await humanDelay(this.page);
      }
    } else {
      this.log('No allocate button found');
    }
  }

  private async deallocatePower(): Promise<void> {
    // Look for deallocate or remove button
    const deallocateButton = this.page.locator('button:has-text("Deallocate"), button:has-text("Remove")').first();

    if (await deallocateButton.isVisible({ timeout: 2000 }).catch(() => false)) {
      await deallocateButton.click();
      this.log('Deallocating power');
      await this.page.waitForLoadState('networkidle');
      await humanDelay(this.page);
    } else {
      this.log('No deallocate button found');
    }
  }
}
