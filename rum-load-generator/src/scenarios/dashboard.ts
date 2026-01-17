import { BaseScenario } from './base-scenario.js';
import { humanDelay } from '../utils/timing.js';

export class DashboardScenario extends BaseScenario {
  name = 'dashboard';

  async execute(): Promise<void> {
    // Navigate to dashboard (home page)
    await this.navigate('/');

    // Wait for dashboard to load
    await this.page.waitForLoadState('networkidle');
    await humanDelay(this.page);

    // Scroll through dashboard to trigger lazy loading and view metrics
    const scrollPositions = [300, 600, 900, 0];

    for (const position of scrollPositions) {
      await this.page.evaluate((pos) => window.scrollTo(0, pos), position);
      this.log(`Scrolled to position ${position}`);
      await humanDelay(this.page);
    }

    // Maybe click on a navigation link to go deeper
    if (Math.random() < 0.3) {
      const navLinks = ['Docking', 'Power', 'Crew', 'Life Support', 'Inventory'];
      const selectedNav = this.randomChoice(navLinks);

      const navLink = this.page.locator(`nav >> text="${selectedNav}"`).first();
      if (await navLink.isVisible({ timeout: 2000 }).catch(() => false)) {
        await navLink.click();
        this.log(`Clicked nav link: ${selectedNav}`);
        await this.page.waitForLoadState('networkidle');
        await humanDelay(this.page);
      }
    }

    // Simulate reading time
    await this.page.waitForTimeout(this.randomInt(2000, 5000));
  }
}
