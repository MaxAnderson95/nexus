/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_DASH0_ENABLED?: string
  readonly VITE_DASH0_ENDPOINT_URL?: string
  readonly VITE_DASH0_AUTH_TOKEN?: string
  readonly VITE_DASH0_DATASET?: string
  readonly VITE_DASH0_ENVIRONMENT?: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}
