export function log(
  component: string,
  browserId: number,
  message: string,
  data?: Record<string, unknown>
): void {
  const timestamp = new Date().toISOString();
  const prefix = browserId > 0 ? `[${component}:${browserId}]` : `[${component}]`;

  const logEntry = {
    timestamp,
    component,
    browserId,
    message,
    ...data,
  };

  if (process.env.LOG_FORMAT === 'json') {
    console.log(JSON.stringify(logEntry));
  } else {
    const dataStr = data ? ` ${JSON.stringify(data)}` : '';
    console.log(`${timestamp} ${prefix} ${message}${dataStr}`);
  }
}
