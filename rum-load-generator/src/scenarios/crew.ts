import { BaseScenario } from './base-scenario.js';
import { humanDelay } from '../utils/timing.js';

export class CrewScenario extends BaseScenario {
  name = 'crew';

  async execute(): Promise<void> {
    await this.navigate('/crew');
    await this.page.waitForLoadState('networkidle');
    await humanDelay(this.page);

    // View crew roster or sections
    const viewType = this.randomChoice(['roster', 'sections']);

    if (viewType === 'roster') {
      await this.viewRoster();
    } else {
      await this.viewSections();
    }

    // Maybe relocate a crew member
    if (Math.random() < 0.3) {
      await this.relocateCrewMember();
    }
  }

  private async viewRoster(): Promise<void> {
    // Scroll through roster
    await this.page.evaluate(() => window.scrollBy(0, 400));
    this.log('Viewing crew roster');
    await humanDelay(this.page);

    // Click on a crew member row if visible
    const crewRow = this.page.locator('tr').nth(this.randomInt(1, 5));
    if (await crewRow.isVisible({ timeout: 2000 }).catch(() => false)) {
      await crewRow.click();
      this.log('Selected crew member');
      await humanDelay(this.page);
    }
  }

  private async viewSections(): Promise<void> {
    // Look for sections tab or link
    const sectionsTab = this.page.locator('text="Sections", button:has-text("Sections")').first();

    if (await sectionsTab.isVisible({ timeout: 2000 }).catch(() => false)) {
      await sectionsTab.click();
      this.log('Viewing sections');
      await humanDelay(this.page);
    }

    // Scroll to view all sections
    await this.page.evaluate(() => window.scrollBy(0, 300));
    await humanDelay(this.page);
  }

  private async relocateCrewMember(): Promise<void> {
    // Look for relocate button
    const relocateButton = this.page.locator('button:has-text("Relocate"), button:has-text("Move")').first();

    if (await relocateButton.isVisible({ timeout: 2000 }).catch(() => false)) {
      await relocateButton.click();
      this.log('Opening relocation dialog');
      await humanDelay(this.page);

      // Confirm if dialog appears
      const confirmButton = this.page.locator('button:has-text("Confirm"), button:has-text("Submit")').first();
      if (await confirmButton.isVisible({ timeout: 2000 }).catch(() => false)) {
        await confirmButton.click();
        this.log('Confirmed relocation');
        await humanDelay(this.page);
      }
    }
  }
}
