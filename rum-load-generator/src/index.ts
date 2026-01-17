import { BrowserPool } from './browser-pool.js';
import { loadConfig } from './config.js';
import { log } from './utils/logger.js';

async function main(): Promise<void> {
  const config = loadConfig();

  log('main', 0, 'NEXUS RUM Load Generator starting', {
    targetUrl: config.baseUrl,
    browsers: config.browserCount,
    iterations: config.iterationsPerBrowser || 'infinite',
    headless: config.headless,
  });

  const pool = new BrowserPool();

  // Handle graceful shutdown
  const shutdown = async (signal: string) => {
    log('main', 0, `Received ${signal}, shutting down gracefully`);
    await pool.shutdown();
    process.exit(0);
  };

  process.on('SIGINT', () => shutdown('SIGINT'));
  process.on('SIGTERM', () => shutdown('SIGTERM'));

  try {
    await pool.start();
    log('main', 0, 'All browser loops completed');
  } catch (error) {
    log('main', 0, 'Fatal error', { error: String(error) });
    await pool.shutdown();
    process.exit(1);
  }
}

main();
