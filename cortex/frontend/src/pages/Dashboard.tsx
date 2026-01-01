import { useState, useEffect } from 'react';
import { api } from '../api/client';
import type { DashboardStatus } from '../types';

function StatusCard({ 
  title, 
  value, 
  subtitle, 
  status = 'normal' 
}: { 
  title: string; 
  value: string | number; 
  subtitle: string;
  status?: 'normal' | 'warning' | 'critical';
}) {
  const statusColors = {
    normal: 'border-green-500',
    warning: 'border-yellow-500',
    critical: 'border-red-500',
  };

  return (
    <div className={`bg-gray-800 rounded-lg p-4 border-l-4 ${statusColors[status]}`}>
      <h3 className="text-sm font-medium text-gray-400">{title}</h3>
      <p className="text-2xl font-bold text-white mt-1">{value}</p>
      <p className="text-xs text-gray-500 mt-1">{subtitle}</p>
    </div>
  );
}

function Dashboard() {
  const [status, setStatus] = useState<DashboardStatus | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    loadDashboard();
  }, []);

  async function loadDashboard() {
    try {
      setLoading(true);
      setError(null);
      const data = await api.dashboard.getStatus();
      setStatus(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load dashboard');
    } finally {
      setLoading(false);
    }
  }

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="text-gray-400">Loading station status...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="bg-red-900/50 border border-red-500 rounded-lg p-4">
        <h3 className="text-red-400 font-medium">Error Loading Dashboard</h3>
        <p className="text-red-300 text-sm mt-1">{error}</p>
        <button 
          onClick={loadDashboard}
          className="mt-3 px-4 py-2 bg-red-600 hover:bg-red-700 rounded text-sm"
        >
          Retry
        </button>
      </div>
    );
  }

  if (!status) return null;

  const overallStatusColors = {
    NOMINAL: 'text-green-400',
    WARNING: 'text-yellow-400',
    CRITICAL: 'text-red-400',
  };

  return (
    <div className="space-y-6">
      {/* Overall Status */}
      <div className="bg-gray-800 rounded-lg p-6">
        <div className="flex items-center justify-between">
          <div>
            <h2 className="text-xl font-bold text-white">Station Status</h2>
            <p className="text-sm text-gray-400 mt-1">
              Last updated: {new Date(status.timestamp).toLocaleString()}
            </p>
          </div>
          <div className={`text-2xl font-bold ${overallStatusColors[status.overallStatus]}`}>
            {status.overallStatus}
          </div>
        </div>
      </div>

      {/* Status Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        {/* Docking */}
        <StatusCard
          title="Docking Bays"
          value={`${status.docking.availableBays}/${status.docking.totalBays}`}
          subtitle={`${status.docking.incomingShips} incoming ships`}
          status={status.docking.availableBays === 0 ? 'warning' : 'normal'}
        />

        {/* Crew */}
        <StatusCard
          title="Crew"
          value={status.crew.totalCrew}
          subtitle={`${status.crew.activeCrew} active, ${status.crew.offDutyCrew} off-duty`}
        />

        {/* Life Support */}
        <StatusCard
          title="Life Support"
          value={`${status.lifeSupport.sectionsNominal}/${status.lifeSupport.totalSections}`}
          subtitle={`${status.lifeSupport.activeAlerts} active alerts`}
          status={
            status.lifeSupport.sectionsCritical > 0 ? 'critical' :
            status.lifeSupport.sectionsWarning > 0 ? 'warning' : 'normal'
          }
        />

        {/* Power */}
        <StatusCard
          title="Power"
          value={`${Math.round(status.power.utilizationPercent)}%`}
          subtitle={`${Math.round(status.power.availableKw)} kW available`}
          status={status.power.utilizationPercent > 90 ? 'warning' : 'normal'}
        />
      </div>

      {/* Detailed Sections */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Docking Details */}
        <div className="bg-gray-800 rounded-lg p-4">
          <h3 className="text-lg font-semibold text-white mb-4">Docking Status</h3>
          <div className="grid grid-cols-2 gap-3 text-sm">
            <div className="bg-gray-700 rounded p-3">
              <span className="text-gray-400">Available</span>
              <span className="float-right text-green-400 font-medium">{status.docking.availableBays}</span>
            </div>
            <div className="bg-gray-700 rounded p-3">
              <span className="text-gray-400">Occupied</span>
              <span className="float-right text-yellow-400 font-medium">{status.docking.occupiedBays}</span>
            </div>
            <div className="bg-gray-700 rounded p-3">
              <span className="text-gray-400">Reserved</span>
              <span className="float-right text-blue-400 font-medium">{status.docking.reservedBays}</span>
            </div>
            <div className="bg-gray-700 rounded p-3">
              <span className="text-gray-400">Docked Ships</span>
              <span className="float-right text-white font-medium">{status.docking.dockedShips}</span>
            </div>
          </div>
        </div>

        {/* Power Details */}
        <div className="bg-gray-800 rounded-lg p-4">
          <h3 className="text-lg font-semibold text-white mb-4">Power Grid</h3>
          <div className="space-y-3">
            <div>
              <div className="flex justify-between text-sm mb-1">
                <span className="text-gray-400">Utilization</span>
                <span className="text-white">{Math.round(status.power.utilizationPercent)}%</span>
              </div>
              <div className="w-full bg-gray-700 rounded-full h-2">
                <div 
                  className={`h-2 rounded-full ${
                    status.power.utilizationPercent > 90 ? 'bg-red-500' :
                    status.power.utilizationPercent > 70 ? 'bg-yellow-500' : 'bg-green-500'
                  }`}
                  style={{ width: `${status.power.utilizationPercent}%` }}
                />
              </div>
            </div>
            <div className="grid grid-cols-2 gap-3 text-sm">
              <div className="bg-gray-700 rounded p-3">
                <span className="text-gray-400">Output</span>
                <span className="float-right text-white font-medium">{Math.round(status.power.totalOutputKw)} kW</span>
              </div>
              <div className="bg-gray-700 rounded p-3">
                <span className="text-gray-400">Allocated</span>
                <span className="float-right text-white font-medium">{Math.round(status.power.totalAllocatedKw)} kW</span>
              </div>
            </div>
          </div>
        </div>

        {/* Life Support Details */}
        <div className="bg-gray-800 rounded-lg p-4">
          <h3 className="text-lg font-semibold text-white mb-4">Life Support</h3>
          <div className="grid grid-cols-3 gap-3 text-sm">
            <div className="bg-green-900/30 border border-green-700 rounded p-3 text-center">
              <div className="text-2xl font-bold text-green-400">{status.lifeSupport.sectionsNominal}</div>
              <div className="text-green-500 text-xs">Nominal</div>
            </div>
            <div className="bg-yellow-900/30 border border-yellow-700 rounded p-3 text-center">
              <div className="text-2xl font-bold text-yellow-400">{status.lifeSupport.sectionsWarning}</div>
              <div className="text-yellow-500 text-xs">Warning</div>
            </div>
            <div className="bg-red-900/30 border border-red-700 rounded p-3 text-center">
              <div className="text-2xl font-bold text-red-400">{status.lifeSupport.sectionsCritical}</div>
              <div className="text-red-500 text-xs">Critical</div>
            </div>
          </div>
          <div className="mt-3 text-sm text-gray-400">
            Avg O2: {status.lifeSupport.averageO2Level.toFixed(1)}% | 
            Avg Temp: {status.lifeSupport.averageTemperature.toFixed(1)}°C
          </div>
        </div>

        {/* Inventory Details */}
        <div className="bg-gray-800 rounded-lg p-4">
          <h3 className="text-lg font-semibold text-white mb-4">Inventory</h3>
          <div className="grid grid-cols-2 gap-3 text-sm">
            <div className="bg-gray-700 rounded p-3">
              <span className="text-gray-400">Total Items</span>
              <span className="float-right text-white font-medium">{status.inventory.totalItems}</span>
            </div>
            <div className={`rounded p-3 ${status.inventory.lowStockItems > 0 ? 'bg-red-900/30 border border-red-700' : 'bg-gray-700'}`}>
              <span className="text-gray-400">Low Stock</span>
              <span className={`float-right font-medium ${status.inventory.lowStockItems > 0 ? 'text-red-400' : 'text-white'}`}>
                {status.inventory.lowStockItems}
              </span>
            </div>
            <div className="bg-gray-700 rounded p-3">
              <span className="text-gray-400">Pending Resupply</span>
              <span className="float-right text-white font-medium">{status.inventory.pendingResupply}</span>
            </div>
            <div className="bg-gray-700 rounded p-3">
              <span className="text-gray-400">Pending Cargo</span>
              <span className="float-right text-white font-medium">{status.inventory.pendingManifests}</span>
            </div>
          </div>
        </div>
      </div>

      {/* Refresh Button */}
      <div className="text-center">
        <button
          onClick={loadDashboard}
          className="px-6 py-2 bg-blue-600 hover:bg-blue-700 rounded-lg text-sm font-medium transition-colors"
        >
          Refresh Status
        </button>
      </div>
    </div>
  );
}

export default Dashboard;
