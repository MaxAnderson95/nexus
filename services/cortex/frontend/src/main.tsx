import { init } from '@dash0/sdk-web'
import React from 'react'
import ReactDOM from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import App from './App'
import './index.css'

// Initialize Dash0 Web SDK for website monitoring
// Must be called before React renders to capture initial page load
// Set VITE_DASH0_ENABLED=true to enable (disabled by default)
if (
  import.meta.env.VITE_DASH0_ENABLED === 'true' &&
  import.meta.env.VITE_DASH0_ENDPOINT_URL &&
  import.meta.env.VITE_DASH0_AUTH_TOKEN
) {
  init({
    serviceName: 'nexus-station-frontend',
    environment: import.meta.env.VITE_DASH0_ENVIRONMENT || 'development',
    endpoint: {
      url: import.meta.env.VITE_DASH0_ENDPOINT_URL,
      authToken: import.meta.env.VITE_DASH0_AUTH_TOKEN,
      ...(import.meta.env.VITE_DASH0_DATASET && { dataset: import.meta.env.VITE_DASH0_DATASET }),
    },
    // Enable trace context propagation to correlate frontend requests with backend traces
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
