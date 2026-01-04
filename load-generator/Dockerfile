FROM ghcr.io/astral-sh/uv:python3.12-alpine

WORKDIR /app

# Install dependencies
COPY requirements.txt .
RUN uv pip install --system --no-cache -r requirements.txt

# Copy source code
COPY . .

# Expose Locust web UI port
EXPOSE 8089

# Default environment variables
ENV LOCUST_USERS=2
ENV LOCUST_SPAWN_RATE=1

# Run Locust with autostart (keeps web UI available while auto-starting the test)
# Web UI available on port 8089 for monitoring
# Redirect output to /dev/null to suppress periodic stats in logs
CMD locust --web-host=0.0.0.0 --autostart >/dev/null 2>&1
