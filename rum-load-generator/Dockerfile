FROM mcr.microsoft.com/playwright:v1.40.0-jammy

WORKDIR /app

# Copy package files
COPY package.json package-lock.json* ./

# Install dependencies
RUN npm ci

# Copy source code
COPY tsconfig.json ./
COPY src/ ./src/

# Default configuration
ENV TARGET_URL=http://cortex:8080
ENV BROWSER_COUNT=2
ENV ITERATIONS=0
ENV HEADLESS=true
ENV MIN_THINK_TIME=500
ENV MAX_THINK_TIME=2000
ENV MIN_SCENARIO_WAIT=3000
ENV MAX_SCENARIO_WAIT=8000
ENV RUM_FLUSH_TIMEOUT=5000
ENV LOG_FORMAT=text

# Health check - verify Playwright can launch
HEALTHCHECK --interval=30s --timeout=10s --start-period=10s --retries=3 \
  CMD node -e "require('playwright').chromium.launch({headless:true}).then(b=>b.close())" || exit 1

CMD ["npm", "start"]
