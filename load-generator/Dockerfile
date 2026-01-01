FROM python:3.12-slim

WORKDIR /app

# Install dependencies
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Copy source code
COPY . .

# Expose Locust web UI port
EXPOSE 8089

# Run Locust
CMD ["locust", "--host=${LOCUST_HOST:-http://localhost}", "--web-host=0.0.0.0"]
