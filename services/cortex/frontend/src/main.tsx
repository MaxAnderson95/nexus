import { init } from '@dash0/sdk-web'
import React from 'react'
import ReactDOM from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import App from './App'
import './index.css'

// Dash0 Web SDK configuration
// In production, these placeholders are replaced by docker-entrypoint.sh at container startup
// Using window assignment prevents the minifier from analyzing/removing this code
declare global {
  interface Window {
    __DASH0_CONFIG__?: {
      enabled: string
      endpoint: string
      authToken: string
      dataset: string
      environment: string
    }
  }
}

window.__DASH0_CONFIG__ = {
  enabled: '__VITE_DASH0_ENABLED__',
  endpoint: '__VITE_DASH0_ENDPOINT_URL__',
  authToken: '__VITE_DASH0_AUTH_TOKEN__',
  dataset: '__VITE_DASH0_DATASET__',
  environment: '__VITE_DASH0_ENVIRONMENT__',
}

// Initialize Dash0 if properly configured (placeholders replaced with real values)
const dash0 = window.__DASH0_CONFIG__
if (
  dash0.enabled === 'true' &&
  dash0.endpoint &&
  !dash0.endpoint.startsWith('__') &&
  dash0.authToken &&
  !dash0.authToken.startsWith('__')
) {
  init({
    serviceName: 'nexus-station-frontend',
    environment: dash0.environment && !dash0.environment.startsWith('__') ? dash0.environment : 'production',
    endpoint: {
      url: dash0.endpoint,
      authToken: dash0.authToken,
      ...(dash0.dataset && !dash0.dataset.startsWith('__') && { dataset: dash0.dataset }),
    },
    propagators: [
      { type: 'traceparent', match: [/.*\/api\/.*/] },
    ],
  })
}

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <BrowserRouter>
      <App />
    </BrowserRouter>
  </React.StrictMode>,
)
