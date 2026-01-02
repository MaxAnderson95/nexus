FROM ghcr.io/astral-sh/uv:python3.12-alpine

WORKDIR /app

# Install dependencies
COPY requirements.txt .
RUN uv pip install --system --no-cache -r requirements.txt

# Copy source code
COPY . .

# Expose Locust web UI port
EXPOSE 8089

# Run Locust (use shell form to expand environment variables)
CMD locust --host=${LOCUST_HOST:-http://localhost:8080} --web-host=0.0.0.0
