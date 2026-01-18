export interface Config {
  baseUrl: string;
  browserCount: number;
  iterationsPerBrowser: number;
  minThinkTime: number;
  maxThinkTime: number;
  minWaitBetweenScenarios: number;
  maxWaitBetweenScenarios: number;
  rumFlushTimeout: number;
  headless: boolean;
  slowMo: number;
  scenarioWeights: Record<string, number>;
}

let cachedConfig: Config | null = null;

export function loadConfig(): Config {
  if (cachedConfig) {
    return cachedConfig;
  }

  cachedConfig = {
    baseUrl: process.env.TARGET_URL || 'http://cortex:8080',
    browserCount: parseInt(process.env.BROWSER_COUNT || '2', 10),
    iterationsPerBrowser: parseInt(process.env.ITERATIONS || '0', 10),
    minThinkTime: parseInt(process.env.MIN_THINK_TIME || '500', 10),
    maxThinkTime: parseInt(process.env.MAX_THINK_TIME || '2000', 10),
    minWaitBetweenScenarios: parseInt(process.env.MIN_SCENARIO_WAIT || '3000', 10),
    maxWaitBetweenScenarios: parseInt(process.env.MAX_SCENARIO_WAIT || '8000', 10),
    rumFlushTimeout: parseInt(process.env.RUM_FLUSH_TIMEOUT || '5000', 10),
    headless: process.env.HEADLESS !== 'false',
    slowMo: parseInt(process.env.SLOW_MO || '0', 10),
    scenarioWeights: {
      dashboard: parseInt(process.env.WEIGHT_DASHBOARD || '30', 10),
      docking: parseInt(process.env.WEIGHT_DOCKING || '20', 10),
      lifeSupport: parseInt(process.env.WEIGHT_LIFE_SUPPORT || '15', 10),
      power: parseInt(process.env.WEIGHT_POWER || '15', 10),
      crew: parseInt(process.env.WEIGHT_CREW || '10', 10),
      inventory: parseInt(process.env.WEIGHT_INVENTORY || '10', 10),
    },
  };

  return cachedConfig;
}
