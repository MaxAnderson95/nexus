import { BaseScenario } from './base-scenario.js';
import { humanDelay } from '../utils/timing.js';

export class InventoryScenario extends BaseScenario {
  name = 'inventory';

  async execute(): Promise<void> {
    await this.navigate('/inventory');
    await this.page.waitForLoadState('networkidle');
    await humanDelay(this.page);

    // Randomly select what to view/do
    const action = this.randomChoice(['supplies', 'cargo', 'consume', 'resupply']);

    switch (action) {
      case 'supplies':
        await this.viewSupplies();
        break;
      case 'cargo':
        await this.viewCargoManifests();
        break;
      case 'consume':
        await this.consumeSupply();
        break;
      case 'resupply':
        await this.resupply();
        break;
    }
  }

  private async viewSupplies(): Promise<void> {
    this.log('Viewing supplies');

    // Scroll through supplies list
    for (let i = 0; i < 3; i++) {
      await this.page.evaluate(() => window.scrollBy(0, 200));
      await humanDelay(this.page);
    }
  }

  private async viewCargoManifests(): Promise<void> {
    // Look for cargo tab
    const cargoTab = this.page.locator('text="Cargo", button:has-text("Cargo"), text="Manifests"').first();

    if (await cargoTab.isVisible({ timeout: 2000 }).catch(() => false)) {
      await cargoTab.click();
      this.log('Viewing cargo manifests');
      await humanDelay(this.page);

      // Maybe unload cargo
      if (Math.random() < 0.3) {
        const unloadButton = this.page.locator('button:has-text("Unload")').first();
        if (await unloadButton.isVisible({ timeout: 2000 }).catch(() => false)) {
          await unloadButton.click();
          this.log('Unloading cargo');
          await humanDelay(this.page);
        }
      }
    }
  }

  private async consumeSupply(): Promise<void> {
    // Look for consume button
    const consumeButton = this.page.locator('button:has-text("Consume")').first();

    if (await consumeButton.isVisible({ timeout: 2000 }).catch(() => false)) {
      await consumeButton.click();
      this.log('Consuming supply');
      await humanDelay(this.page);

      // Confirm if dialog appears
      const confirmButton = this.page.locator('button:has-text("Confirm"), button:has-text("Submit")').first();
      if (await confirmButton.isVisible({ timeout: 2000 }).catch(() => false)) {
        await confirmButton.click();
        await humanDelay(this.page);
      }
    }
  }

  private async resupply(): Promise<void> {
    // Look for resupply/request button
    const resupplyButton = this.page.locator('button:has-text("Resupply"), button:has-text("Request")').first();

    if (await resupplyButton.isVisible({ timeout: 2000 }).catch(() => false)) {
      await resupplyButton.click();
      this.log('Opening resupply dialog');
      await humanDelay(this.page);

      // Confirm if dialog appears
      const confirmButton = this.page.locator('button:has-text("Confirm"), button:has-text("Submit")').first();
      if (await confirmButton.isVisible({ timeout: 2000 }).catch(() => false)) {
        await confirmButton.click();
        this.log('Submitted resupply request');
        await humanDelay(this.page);
      }
    }
  }
}
