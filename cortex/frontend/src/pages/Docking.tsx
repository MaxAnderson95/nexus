import { useState, useEffect } from 'react';
import { api } from '../api/client';
import type { DockingBay, Ship } from '../types';

function Docking() {
  const [bays, setBays] = useState<DockingBay[]>([]);
  const [ships, setShips] = useState<Ship[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [actionLoading, setActionLoading] = useState<number | null>(null);

  useEffect(() => {
    loadData();
  }, []);

  async function loadData() {
    try {
      setLoading(true);
      setError(null);
      const [baysData, shipsData] = await Promise.all([
        api.docking.getBays(),
        api.docking.getShips(),
      ]);
      setBays(baysData);
      setShips(shipsData);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load docking data');
    } finally {
      setLoading(false);
    }
  }

  async function handleDock(shipId: number) {
    try {
      setActionLoading(shipId);
      await api.docking.dockShip(shipId);
      await loadData();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to dock ship');
    } finally {
      setActionLoading(null);
    }
  }

  async function handleUndock(shipId: number) {
    try {
      setActionLoading(shipId);
      await api.docking.undockShip(shipId);
      await loadData();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to undock ship');
    } finally {
      setActionLoading(null);
    }
  }

  const statusColors = {
    AVAILABLE: 'bg-green-900/30 border-green-500 text-green-400',
    OCCUPIED: 'bg-yellow-900/30 border-yellow-500 text-yellow-400',
    RESERVED: 'bg-blue-900/30 border-blue-500 text-blue-400',
    MAINTENANCE: 'bg-red-900/30 border-red-500 text-red-400',
  };

  const shipStatusColors = {
    INCOMING: 'text-blue-400',
    DOCKED: 'text-green-400',
    DEPARTING: 'text-yellow-400',
    IN_TRANSIT: 'text-gray-400',
  };

  if (loading) {
    return <div className="text-gray-400 text-center py-8">Loading docking data...</div>;
  }

  return (
    <div className="space-y-6">
      <h2 className="text-2xl font-bold text-white">Docking Control</h2>

      {error && (
        <div className="bg-red-900/50 border border-red-500 rounded-lg p-4">
          <p className="text-red-300 text-sm">{error}</p>
        </div>
      )}

      {/* Docking Bays */}
      <div>
        <h3 className="text-lg font-semibold text-white mb-4">Docking Bays</h3>
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {bays.map((bay) => (
            <div
              key={bay.id}
              className={`rounded-lg border p-4 ${statusColors[bay.status]}`}
            >
              <div className="flex justify-between items-start">
                <div>
                  <h4 className="font-bold text-lg">{bay.bayNumber}</h4>
                  <p className="text-sm opacity-80">{bay.status}</p>
                </div>
                <div className="text-sm opacity-60">
                  Capacity: {bay.capacity}
                </div>
              </div>
              {bay.currentShipName && (
                <div className="mt-3 pt-3 border-t border-current/20">
                  <p className="text-sm">Current Ship:</p>
                  <p className="font-medium">{bay.currentShipName}</p>
                </div>
              )}
            </div>
          ))}
        </div>
      </div>

      {/* Ships */}
      <div>
        <h3 className="text-lg font-semibold text-white mb-4">Ships</h3>
        <div className="bg-gray-800 rounded-lg overflow-hidden">
          <table className="w-full">
            <thead className="bg-gray-700">
              <tr>
                <th className="px-4 py-3 text-left text-sm font-medium text-gray-300">Ship</th>
                <th className="px-4 py-3 text-left text-sm font-medium text-gray-300">Type</th>
                <th className="px-4 py-3 text-left text-sm font-medium text-gray-300">Crew</th>
                <th className="px-4 py-3 text-left text-sm font-medium text-gray-300">Status</th>
                <th className="px-4 py-3 text-left text-sm font-medium text-gray-300">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-700">
              {ships.map((ship) => (
                <tr key={ship.id} className="hover:bg-gray-700/50">
                  <td className="px-4 py-3 text-white">{ship.name}</td>
                  <td className="px-4 py-3 text-gray-400">{ship.type}</td>
                  <td className="px-4 py-3 text-gray-400">{ship.crewCount}</td>
                  <td className={`px-4 py-3 ${shipStatusColors[ship.status]}`}>{ship.status}</td>
                  <td className="px-4 py-3">
                    {ship.status === 'INCOMING' && (
                      <button
                        onClick={() => handleDock(ship.id)}
                        disabled={actionLoading === ship.id}
                        className="px-3 py-1 bg-green-600 hover:bg-green-700 disabled:opacity-50 rounded text-sm"
                      >
                        {actionLoading === ship.id ? 'Docking...' : 'Dock'}
                      </button>
                    )}
                    {ship.status === 'DOCKED' && (
                      <button
                        onClick={() => handleUndock(ship.id)}
                        disabled={actionLoading === ship.id}
                        className="px-3 py-1 bg-yellow-600 hover:bg-yellow-700 disabled:opacity-50 rounded text-sm"
                      >
                        {actionLoading === ship.id ? 'Undocking...' : 'Undock'}
                      </button>
                    )}
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

export default Docking;
