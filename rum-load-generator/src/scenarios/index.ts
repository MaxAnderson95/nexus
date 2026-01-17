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

  // Build weighted list
  const weightedList: string[] = [];
  for (const [scenario, weight] of Object.entries(weights)) {
    for (let i = 0; i < weight; i++) {
      weightedList.push(scenario);
    }
  }

  // Select random scenario
  const selected = weightedList[Math.floor(Math.random() * weightedList.length)];
  const ScenarioClass = scenarioMap[selected];

  if (!ScenarioClass) {
    throw new Error(`Unknown scenario: ${selected}`);
  }

  return new ScenarioClass(page, browserId);
}

export { BaseScenario };
