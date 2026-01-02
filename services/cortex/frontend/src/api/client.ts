import type {
  DashboardStatus,
  DockingBay,
  Ship,
  DockingLog,
  CrewMember,
  Section,
  EnvironmentStatus,
  Alert,
  SelfTestResult,
  PowerGridStatus,
  PowerAllocation,
  Supply,
  CargoManifest,
  ResupplyRequest,
  ResetAllTablesResponse,
} from '../types';

const BASE_URL = '/api';
const TRACE_ID_HEADER = 'x-trace-id';

class ApiError extends Error {
  public traceId: string | null;

  constructor(public status: number, message: string, traceId: string | null = null) {
    super(message);
    this.name = 'ApiError';
    this.traceId = traceId;
  }
}

async function request<T>(path: string, options?: RequestInit): Promise<T> {
  const response = await fetch(`${BASE_URL}${path}`, {
    headers: {
      'Content-Type': 'application/json',
      ...options?.headers,
    },
    ...options,
  });

  // Extract trace ID from response header
  const traceId = response.headers.get(TRACE_ID_HEADER);

  if (!response.ok) {
    const error = await response.json().catch(() => ({ message: 'Unknown error' }));
    // Use trace ID from error body if available, otherwise fall back to header
    const errorTraceId = error.traceId || traceId;
    throw new ApiError(response.status, error.message || error.error || 'Request failed', errorTraceId);
  }

  return response.json();
}

export const api = {
  // Dashboard
  dashboard: {
    getStatus: () => request<DashboardStatus>('/dashboard/status'),
  },

  // Docking
  docking: {
    getBays: () => request<DockingBay[]>('/docking/bays'),
    getBay: (id: number) => request<DockingBay>(`/docking/bays/${id}`),
    getShips: () => request<Ship[]>('/docking/ships'),
    getIncomingShips: () => request<Ship[]>('/docking/ships/incoming'),
    dockShip: (shipId: number) =>
      request<{ success: boolean; message: string }>(`/docking/dock/${shipId}`, { method: 'POST' }),
    undockShip: (shipId: number) =>
      request<{ success: boolean; message: string }>(`/docking/undock/${shipId}`, { method: 'POST' }),
    getLogs: () => request<DockingLog[]>('/docking/logs'),
  },

  // Crew
  crew: {
    getRoster: () => request<CrewMember[]>('/crew'),
    getMember: (id: number) => request<CrewMember>(`/crew/${id}`),
    getSections: () => request<Section[]>('/crew/sections'),
    getSectionMembers: (sectionId: number) => request<CrewMember[]>(`/crew/section/${sectionId}`),
    relocate: (crewId: number, targetSectionId: number) =>
      request<{ message: string }>('/crew/relocate', {
        method: 'POST',
        body: JSON.stringify({ crewId, targetSectionId }),
      }),
  },

  // Life Support
  lifeSupport: {
    getEnvironment: () => request<EnvironmentStatus[]>('/life-support/environment'),
    getSectionEnvironment: (sectionId: number) =>
      request<EnvironmentStatus>(`/life-support/environment/section/${sectionId}`),
    adjustSection: (sectionId: number, settings: { targetTemperature?: number; targetO2?: number }) =>
      request<EnvironmentStatus>(`/life-support/environment/section/${sectionId}/adjust`, {
        method: 'POST',
        body: JSON.stringify(settings),
      }),
    getAlerts: () => request<Alert[]>('/life-support/alerts'),
    acknowledgeAlert: (alertId: number) =>
      request<Alert>(`/life-support/alerts/${alertId}/acknowledge`, { method: 'POST' }),
    runSelfTest: (sectionId: number) =>
      request<SelfTestResult>(`/life-support/environment/section/${sectionId}/self-test`, { method: 'POST' }),
  },

  // Power
  power: {
    getGrid: () => request<PowerGridStatus>('/power/grid'),
    getSources: () => request<PowerGridStatus>('/power/sources'),
    getAllocations: () => request<PowerAllocation[]>('/power/allocations'),
    allocate: (system: string, amountKw: number, priority?: number) =>
      request<PowerAllocation>('/power/allocate', {
        method: 'POST',
        body: JSON.stringify({ system, amountKw, priority: priority ?? 5 }),
      }),
    deallocate: (system: string) =>
      request<{ message: string }>('/power/deallocate', {
        method: 'POST',
        body: JSON.stringify({ system }),
      }),
  },

  // Inventory
  inventory: {
    getSupplies: () => request<Supply[]>('/inventory/supplies'),
    getSupply: (id: number) => request<Supply>(`/inventory/supplies/${id}`),
    getLowStock: () => request<Supply[]>('/inventory/supplies/low-stock'),
    consume: (supplyId: number, quantity: number) =>
      request<{ message: string }>('/inventory/consume', {
        method: 'POST',
        body: JSON.stringify({ supplyId, quantity }),
      }),
    requestResupply: (supplyId: number, quantity: number) =>
      request<{ message: string }>('/inventory/resupply', {
        method: 'POST',
        body: JSON.stringify({ supplyId, quantity }),
      }),
    getResupplyRequests: () => request<ResupplyRequest[]>('/inventory/resupply-requests'),
    getManifests: () => request<CargoManifest[]>('/inventory/cargo-manifests'),
    unloadManifest: (manifestId: number) =>
      request<{ message: string }>(`/inventory/cargo-manifests/${manifestId}/unload`, { method: 'POST' }),
  },

  // Admin
  admin: {
    resetAllTables: () =>
      request<ResetAllTablesResponse>('/admin/resetAllTables', { method: 'POST' }),
  },
};

export { ApiError };
