// Dashboard
export interface DashboardStatus {
  docking: DockingSummary;
  crew: CrewSummary;
  lifeSupport: LifeSupportSummary;
  power: PowerSummary;
  inventory: InventorySummary;
  overallStatus: 'NOMINAL' | 'WARNING' | 'CRITICAL';
  timestamp: string;
}

export interface DockingSummary {
  totalBays: number;
  availableBays: number;
  occupiedBays: number;
  reservedBays: number;
  incomingShips: number;
  dockedShips: number;
}

export interface CrewSummary {
  totalCrew: number;
  activeCrew: number;
  onLeaveCrew: number;
  offDutyCrew: number;
  inTransitCrew: number;
}

export interface LifeSupportSummary {
  totalSections: number;
  sectionsNominal: number;
  sectionsWarning: number;
  sectionsCritical: number;
  activeAlerts: number;
  averageO2Level: number;
  averageTemperature: number;
}

export interface PowerSummary {
  totalCapacityKw: number;
  totalOutputKw: number;
  totalAllocatedKw: number;
  availableKw: number;
  utilizationPercent: number;
  onlineSources: number;
  totalSources: number;
}

export interface InventorySummary {
  totalItems: number;
  lowStockItems: number;
  pendingResupply: number;
  pendingManifests: number;
}

// Docking
export interface DockingBay {
  id: number;
  bayNumber: string;
  status: 'AVAILABLE' | 'OCCUPIED' | 'RESERVED' | 'MAINTENANCE';
  currentShipId: number | null;
  currentShipName: string | null;
  capacity: number;
}

export interface Ship {
  id: number;
  name: string;
  type: 'CARGO' | 'PASSENGER' | 'MILITARY' | 'RESEARCH' | 'SUPPLY';
  crewCount: number;
  cargoCapacity: number;
  status: 'INCOMING' | 'DOCKED' | 'DEPARTING' | 'IN_TRANSIT';
  arrivalTime: string | null;
}

export interface DockingLog {
  id: number;
  shipId: number;
  shipName: string;
  bayId: number;
  bayNumber: string;
  action: 'DOCK' | 'UNDOCK' | 'ARRIVAL_SCHEDULED' | 'DEPARTURE_SCHEDULED';
  timestamp: string;
}

// Crew
export interface CrewMember {
  id: number;
  name: string;
  rank: string;
  role: string;
  sectionId: number;
  sectionName: string;
  status: 'ACTIVE' | 'ON_LEAVE' | 'OFF_DUTY' | 'IN_TRANSIT';
  arrivedAt: string;
}

export interface Section {
  id: number;
  name: string;
  deck: number;
  maxCapacity: number;
  currentOccupancy: number;
  occupancyPercent: number;
}

// Life Support
export interface EnvironmentStatus {
  sectionId: number;
  sectionName: string;
  o2Level: number;
  co2Level: number;
  temperature: number;
  pressure: number;
  humidity: number;
  targetO2: number;
  targetTemperature: number;
  targetPressure: number;
  targetHumidity: number;
  currentOccupancy: number;
  maxOccupancy: number;
  status: 'NOMINAL' | 'WARNING' | 'CRITICAL';
  lastUpdated: string;
}

export interface Alert {
  id: number;
  sectionId: number;
  sectionName: string;
  type: string;
  severity: 'INFO' | 'WARNING' | 'CRITICAL' | 'EMERGENCY';
  message: string;
  acknowledged: boolean;
  acknowledgedAt: string | null;
  createdAt: string;
}

// Power
export interface PowerGridStatus {
  totalCapacityKw: number;
  totalOutputKw: number;
  totalAllocatedKw: number;
  availableKw: number;
  utilizationPercent: number;
  onlineSources: number;
  totalSources: number;
  sources: PowerSourceSummary[];
}

export interface PowerSourceSummary {
  id: number;
  name: string;
  type: 'SOLAR_ARRAY' | 'FUSION_REACTOR' | 'BATTERY_BANK' | 'FUEL_CELL';
  status: 'ONLINE' | 'OFFLINE' | 'MAINTENANCE' | 'DEGRADED';
  maxOutputKw: number;
  currentOutputKw: number;
  utilizationPercent: number;
}

export interface PowerAllocation {
  id: number;
  systemName: string;
  allocatedKw: number;
  priority: number;
  sectionId: number | null;
}

// Inventory
export interface Supply {
  id: number;
  name: string;
  category: 'FOOD' | 'MEDICAL' | 'MECHANICAL' | 'ELECTRONIC' | 'FUEL' | 'WATER' | 'OXYGEN' | 'GENERAL';
  quantity: number;
  unit: string;
  minThreshold: number;
  sectionId: number | null;
  isLowStock: boolean;
}

export interface CargoManifest {
  id: number;
  shipId: number;
  shipName: string;
  status: 'PENDING' | 'UNLOADING' | 'COMPLETED' | 'CANCELLED';
  items: CargoItem[];
  createdAt: string;
}

export interface CargoItem {
  id: number;
  supplyId: number;
  supplyName: string;
  quantity: number;
}

export interface ResupplyRequest {
  id: number;
  supplyId: number;
  supplyName: string;
  quantity: number;
  status: 'PENDING' | 'APPROVED' | 'IN_TRANSIT' | 'DELIVERED' | 'CANCELLED';
  requestedAt: string;
}
