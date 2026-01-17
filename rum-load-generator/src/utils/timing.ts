import { loadConfig } from '../config.js';

export function randomBetween(min: number, max: number): number {
  return Math.floor(Math.random() * (max - min + 1)) + min;
}

export function thinkTime(): number {
  const config = loadConfig();
  return randomBetween(config.minThinkTime, config.maxThinkTime);
}

export function scenarioWait(): number {
  const config = loadConfig();
  return randomBetween(config.minWaitBetweenScenarios, config.maxWaitBetweenScenarios);
}

export async function humanDelay(page: { waitForTimeout: (ms: number) => Promise<void> }): Promise<void> {
  await page.waitForTimeout(thinkTime());
}
