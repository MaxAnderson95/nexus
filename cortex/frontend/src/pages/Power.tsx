import { useState, useEffect } from 'react';
import { api } from '../api/client';
import type { PowerGridStatus, PowerAllocation } from '../types';

function Power() {
  const [grid, setGrid] = useState<PowerGridStatus | null>(null);
  const [allocations, setAllocations] = useState<PowerAllocation[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    loadData();
  }, []);

  async function loadData() {
    try {
      setLoading(true);
      setError(null);
      const [gridData, allocData] = await Promise.all([
        api.power.getGrid(),
        api.power.getAllocations(),
      ]);
      setGrid(gridData);
      setAllocations(allocData);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load power data');
    } finally {
      setLoading(false);
    }
  }

  const statusColors = {
    ONLINE: 'text-green-400',
    OFFLINE: 'text-red-400',
    MAINTENANCE: 'text-yellow-400',
    DEGRADED: 'text-orange-400',
  };

  const typeIcons = {
    SOLAR_ARRAY: '☀️',
    FUSION_REACTOR: '⚛️',
    BATTERY_BANK: '🔋',
    FUEL_CELL: '⚡',
  };

  if (loading) {
    return <div className="text-gray-400 text-center py-8">Loading power data...</div>;
  }

  if (!grid) return null;

  return (
    <div className="space-y-6">
      <h2 className="text-2xl font-bold text-white">Power Grid</h2>

      {error && (
        <div className="bg-red-900/50 border border-red-500 rounded-lg p-4">
          <p className="text-red-300 text-sm">{error}</p>
        </div>
      )}

      {/* Grid Overview */}
      <div className="bg-gray-800 rounded-lg p-6">
        <h3 className="text-lg font-semibold text-white mb-4">Grid Status</h3>
        
        {/* Utilization Bar */}
        <div className="mb-6">
          <div className="flex justify-between text-sm mb-2">
            <span className="text-gray-400">Power Utilization</span>
            <span className={
              grid.utilizationPercent > 90 ? 'text-red-400' :
              grid.utilizationPercent > 70 ? 'text-yellow-400' : 'text-green-400'
            }>
              {grid.utilizationPercent.toFixed(1)}%
            </span>
          </div>
          <div className="w-full bg-gray-700 rounded-full h-4">
            <div 
              className={`h-4 rounded-full transition-all ${
                grid.utilizationPercent > 90 ? 'bg-red-500' :
                grid.utilizationPercent > 70 ? 'bg-yellow-500' : 'bg-green-500'
              }`}
              style={{ width: `${Math.min(grid.utilizationPercent, 100)}%` }}
            />
          </div>
        </div>

        {/* Stats */}
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
          <div className="bg-gray-700 rounded-lg p-4">
            <div className="text-2xl font-bold text-white">{Math.round(grid.totalCapacityKw)}</div>
            <div className="text-sm text-gray-400">Total Capacity (kW)</div>
          </div>
          <div className="bg-gray-700 rounded-lg p-4">
            <div className="text-2xl font-bold text-blue-400">{Math.round(grid.totalOutputKw)}</div>
            <div className="text-sm text-gray-400">Current Output (kW)</div>
          </div>
          <div className="bg-gray-700 rounded-lg p-4">
            <div className="text-2xl font-bold text-yellow-400">{Math.round(grid.totalAllocatedKw)}</div>
            <div className="text-sm text-gray-400">Allocated (kW)</div>
          </div>
          <div className="bg-gray-700 rounded-lg p-4">
            <div className="text-2xl font-bold text-green-400">{Math.round(grid.availableKw)}</div>
            <div className="text-sm text-gray-400">Available (kW)</div>
          </div>
        </div>
      </div>

      {/* Power Sources */}
      <div>
        <h3 className="text-lg font-semibold text-white mb-4">
          Power Sources ({grid.onlineSources}/{grid.totalSources} Online)
        </h3>
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {grid.sources.map((source) => (
            <div key={source.id} className="bg-gray-800 rounded-lg p-4">
              <div className="flex justify-between items-start mb-3">
                <div>
                  <span className="text-xl mr-2">{typeIcons[source.type]}</span>
                  <span className="font-bold text-white">{source.name}</span>
                </div>
                <span className={`text-sm ${statusColors[source.status]}`}>
                  {source.status}
                </span>
              </div>
              
              <div className="space-y-2">
                <div>
                  <div className="flex justify-between text-sm mb-1">
                    <span className="text-gray-400">Output</span>
                    <span className="text-white">
                      {Math.round(source.currentOutputKw)} / {Math.round(source.maxOutputKw)} kW
                    </span>
                  </div>
                  <div className="w-full bg-gray-700 rounded-full h-2">
                    <div 
                      className="h-2 rounded-full bg-blue-500"
                      style={{ width: `${source.utilizationPercent}%` }}
                    />
                  </div>
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Allocations */}
      <div>
        <h3 className="text-lg font-semibold text-white mb-4">Power Allocations</h3>
        <div className="bg-gray-800 rounded-lg overflow-hidden">
          <table className="w-full">
            <thead className="bg-gray-700">
              <tr>
                <th className="px-4 py-3 text-left text-sm font-medium text-gray-300">System</th>
                <th className="px-4 py-3 text-left text-sm font-medium text-gray-300">Priority</th>
                <th className="px-4 py-3 text-right text-sm font-medium text-gray-300">Allocated (kW)</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-700">
              {allocations.map((alloc) => (
                <tr key={alloc.id} className="hover:bg-gray-700/50">
                  <td className="px-4 py-3 text-white">{alloc.systemName}</td>
                  <td className="px-4 py-3">
                    <span className={`px-2 py-1 rounded text-xs ${
                      alloc.priority <= 2 ? 'bg-red-900 text-red-300' :
                      alloc.priority <= 4 ? 'bg-yellow-900 text-yellow-300' : 'bg-gray-700 text-gray-300'
                    }`}>
                      P{alloc.priority}
                    </span>
                  </td>
                  <td className="px-4 py-3 text-right text-yellow-400 font-medium">
                    {Math.round(alloc.allocatedKw)}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      <button
        onClick={loadData}
        className="px-4 py-2 bg-blue-600 hover:bg-blue-700 rounded text-sm"
      >
        Refresh
      </button>
    </div>
  );
}

export default Power;
