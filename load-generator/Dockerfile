FROM ghcr.io/astral-sh/uv:python3.12-alpine

WORKDIR /app

# Install dependencies
COPY requirements.txt .
RUN uv pip install --system --no-cache -r requirements.txt

# Copy source code
COPY . .

# Expose Locust web UI port
EXPOSE 8089

# Default environment variables for headless mode
ENV LOCUST_HEADLESS=true
ENV LOCUST_USERS=2
ENV LOCUST_SPAWN_RATE=1

# Run Locust with environment variable support for headless mode
# When LOCUST_HEADLESS=true, locust automatically reads LOCUST_USERS and LOCUST_SPAWN_RATE
# Redirect output to /dev/null to suppress periodic stats
CMD locust --host=${LOCUST_HOST:-http://localhost:8080} --web-host=0.0.0.0 >/dev/null 2>&1
