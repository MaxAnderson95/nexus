#!/bin/sh
set -e

# Runtime injection of frontend environment variables
# Replaces placeholder values in built JS files with actual environment variables

STATIC_DIR="/app/static"

# Only process if static files exist and Dash0 env vars are set
if [ -d "$STATIC_DIR" ] && [ -n "$VITE_DASH0_ENABLED" ]; then
  echo "Injecting frontend environment variables..."
  
  # Find all JS files and replace placeholders
  find "$STATIC_DIR" -name "*.js" -type f | while read -r file; do
    # Replace each placeholder with its corresponding env var value
    # Use | as sed delimiter to avoid issues with URLs containing /
    sed -i \
      -e "s|__VITE_DASH0_ENABLED__|${VITE_DASH0_ENABLED:-false}|g" \
      -e "s|__VITE_DASH0_ENDPOINT_URL__|${VITE_DASH0_ENDPOINT_URL:-}|g" \
      -e "s|__VITE_DASH0_AUTH_TOKEN__|${VITE_DASH0_AUTH_TOKEN:-}|g" \
      -e "s|__VITE_DASH0_ENVIRONMENT__|${VITE_DASH0_ENVIRONMENT:-production}|g" \
      "$file"
  done
  
  echo "Frontend environment variables injected."
fi

# Execute the main application
exec java -jar app.jar "$@"
