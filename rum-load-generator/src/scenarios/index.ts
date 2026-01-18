import type { Page } from 'playwright';
import { BaseScenario } from './base-scenario.js';
import { DashboardScenario } from './dashboard.js';
import { DockingScenario } from './docking.js';
import { LifeSupportScenario } from './life-support.js';
import { PowerScenario } from './power.js';
import { CrewScenario } from './crew.js';
import { InventoryScenario } from './inventory.js';
import { loadConfig } from '../config.js';

type ScenarioConstructor = new (page: Page, browserId: number) => BaseScenario;

const scenarioMap: Record<string, ScenarioConstructor> = {
  dashboard: DashboardScenario,
  docking: DockingScenario,
  lifeSupport: LifeSupportScenario,
  power: PowerScenario,
  crew: CrewScenario,
  inventory: InventoryScenario,
};

export function selectWeightedScenario(page: Page, browserId: number): BaseScenario {
  const config = loadConfig();
  const weights = config.scenarioWeights;

  // Use cumulative weight selection for efficiency
  const entries = Object.entries(weights);
  const totalWeight = entries.reduce((sum, [, weight]) => sum + weight, 0);
  let random = Math.random() * totalWeight;

  for (const [scenario, weight] of entries) {
    random -= weight;
    if (random <= 0) {
      const ScenarioClass = scenarioMap[scenario];
      if (!ScenarioClass) {
        throw new Error(`Unknown scenario: ${scenario}`);
      }
      return new ScenarioClass(page, browserId);
    }
  }

  // Fallback to first scenario (should never reach here)
  const [firstScenario] = entries[0];
  return new scenarioMap[firstScenario]!(page, browserId);
}

export { BaseScenario };
