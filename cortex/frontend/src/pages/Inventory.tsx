import { useState, useEffect } from 'react';
import { api } from '../api/client';
import type { Supply, CargoManifest, ResupplyRequest } from '../types';

function Inventory() {
  const [supplies, setSupplies] = useState<Supply[]>([]);
  const [manifests, setManifests] = useState<CargoManifest[]>([]);
  const [resupplyRequests, setResupplyRequests] = useState<ResupplyRequest[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [selectedCategory, setSelectedCategory] = useState<string | null>(null);

  useEffect(() => {
    loadData();
  }, []);

  async function loadData() {
    try {
      setLoading(true);
      setError(null);
      const [suppliesData, manifestsData, requestsData] = await Promise.all([
        api.inventory.getSupplies(),
        api.inventory.getManifests(),
        api.inventory.getResupplyRequests(),
      ]);
      setSupplies(suppliesData);
      setManifests(manifestsData);
      setResupplyRequests(requestsData);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load inventory data');
    } finally {
      setLoading(false);
    }
  }

  async function handleUnload(manifestId: number) {
    try {
      await api.inventory.unloadManifest(manifestId);
      await loadData();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to unload manifest');
    }
  }

  const categories = [...new Set(supplies.map((s) => s.category))];
  const filteredSupplies = selectedCategory
    ? supplies.filter((s) => s.category === selectedCategory)
    : supplies;

  const lowStockCount = supplies.filter((s) => s.isLowStock).length;

  const categoryColors: Record<string, string> = {
    FOOD: 'bg-green-900/50 text-green-300',
    MEDICAL: 'bg-red-900/50 text-red-300',
    MECHANICAL: 'bg-gray-700 text-gray-300',
    ELECTRONIC: 'bg-blue-900/50 text-blue-300',
    FUEL: 'bg-yellow-900/50 text-yellow-300',
    WATER: 'bg-cyan-900/50 text-cyan-300',
    OXYGEN: 'bg-sky-900/50 text-sky-300',
    GENERAL: 'bg-purple-900/50 text-purple-300',
  };

  if (loading) {
    return <div className="text-gray-400 text-center py-8">Loading inventory data...</div>;
  }

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <h2 className="text-2xl font-bold text-white">Inventory Management</h2>
        {lowStockCount > 0 && (
          <span className="px-3 py-1 bg-red-900 text-red-300 rounded-full text-sm">
            {lowStockCount} items low
          </span>
        )}
      </div>

      {error && (
        <div className="bg-red-900/50 border border-red-500 rounded-lg p-4">
          <p className="text-red-300 text-sm">{error}</p>
        </div>
      )}

      {/* Category Filter */}
      <div className="flex flex-wrap gap-2">
        <button
          onClick={() => setSelectedCategory(null)}
          className={`px-3 py-1 rounded text-sm ${
            selectedCategory === null ? 'bg-blue-600 text-white' : 'bg-gray-700 text-gray-300'
          }`}
        >
          All ({supplies.length})
        </button>
        {categories.map((cat) => (
          <button
            key={cat}
            onClick={() => setSelectedCategory(cat)}
            className={`px-3 py-1 rounded text-sm ${
              selectedCategory === cat ? 'bg-blue-600 text-white' : categoryColors[cat]
            }`}
          >
            {cat} ({supplies.filter((s) => s.category === cat).length})
          </button>
        ))}
      </div>

      {/* Supplies Table */}
      <div className="bg-gray-800 rounded-lg overflow-hidden">
        <table className="w-full">
          <thead className="bg-gray-700">
            <tr>
              <th className="px-4 py-3 text-left text-sm font-medium text-gray-300">Item</th>
              <th className="px-4 py-3 text-left text-sm font-medium text-gray-300">Category</th>
              <th className="px-4 py-3 text-right text-sm font-medium text-gray-300">Quantity</th>
              <th className="px-4 py-3 text-right text-sm font-medium text-gray-300">Min Threshold</th>
              <th className="px-4 py-3 text-left text-sm font-medium text-gray-300">Status</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-700">
            {filteredSupplies.map((supply) => (
              <tr key={supply.id} className={`hover:bg-gray-700/50 ${supply.isLowStock ? 'bg-red-900/20' : ''}`}>
                <td className="px-4 py-3 text-white">{supply.name}</td>
                <td className="px-4 py-3">
                  <span className={`px-2 py-1 rounded text-xs ${categoryColors[supply.category]}`}>
                    {supply.category}
                  </span>
                </td>
                <td className={`px-4 py-3 text-right ${supply.isLowStock ? 'text-red-400' : 'text-white'}`}>
                  {supply.quantity} {supply.unit}
                </td>
                <td className="px-4 py-3 text-right text-gray-400">
                  {supply.minThreshold} {supply.unit}
                </td>
                <td className="px-4 py-3">
                  {supply.isLowStock ? (
                    <span className="text-red-400 text-sm">LOW STOCK</span>
                  ) : (
                    <span className="text-green-400 text-sm">OK</span>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {/* Pending Cargo */}
      {manifests.filter((m) => m.status === 'PENDING').length > 0 && (
        <div>
          <h3 className="text-lg font-semibold text-white mb-4">Pending Cargo</h3>
          <div className="space-y-3">
            {manifests
              .filter((m) => m.status === 'PENDING')
              .map((manifest) => (
                <div key={manifest.id} className="bg-gray-800 rounded-lg p-4">
                  <div className="flex justify-between items-start">
                    <div>
                      <h4 className="font-medium text-white">{manifest.shipName}</h4>
                      <p className="text-sm text-gray-400">
                        {manifest.items.length} items |{' '}
                        {new Date(manifest.createdAt).toLocaleDateString()}
                      </p>
                    </div>
                    <button
                      onClick={() => handleUnload(manifest.id)}
                      className="px-3 py-1 bg-green-600 hover:bg-green-700 rounded text-sm"
                    >
                      Unload
                    </button>
                  </div>
                  <div className="mt-2 text-sm text-gray-400">
                    {manifest.items.map((item) => (
                      <span key={item.id} className="mr-3">
                        {item.supplyName}: {item.quantity}
                      </span>
                    ))}
                  </div>
                </div>
              ))}
          </div>
        </div>
      )}

      {/* Resupply Requests */}
      {resupplyRequests.length > 0 && (
        <div>
          <h3 className="text-lg font-semibold text-white mb-4">Resupply Requests</h3>
          <div className="bg-gray-800 rounded-lg overflow-hidden">
            <table className="w-full">
              <thead className="bg-gray-700">
                <tr>
                  <th className="px-4 py-3 text-left text-sm font-medium text-gray-300">Item</th>
                  <th className="px-4 py-3 text-right text-sm font-medium text-gray-300">Quantity</th>
                  <th className="px-4 py-3 text-left text-sm font-medium text-gray-300">Status</th>
                  <th className="px-4 py-3 text-left text-sm font-medium text-gray-300">Requested</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-700">
                {resupplyRequests.map((req) => (
                  <tr key={req.id}>
                    <td className="px-4 py-3 text-white">{req.supplyName}</td>
                    <td className="px-4 py-3 text-right text-gray-400">{req.quantity}</td>
                    <td className="px-4 py-3">
                      <span className={`px-2 py-1 rounded text-xs ${
                        req.status === 'DELIVERED' ? 'bg-green-900 text-green-300' :
                        req.status === 'IN_TRANSIT' ? 'bg-blue-900 text-blue-300' :
                        req.status === 'APPROVED' ? 'bg-yellow-900 text-yellow-300' :
                        'bg-gray-700 text-gray-300'
                      }`}>
                        {req.status}
                      </span>
                    </td>
                    <td className="px-4 py-3 text-gray-400 text-sm">
                      {new Date(req.requestedAt).toLocaleDateString()}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      <button
        onClick={loadData}
        className="px-4 py-2 bg-blue-600 hover:bg-blue-700 rounded text-sm"
      >
        Refresh
      </button>
    </div>
  );
}

export default Inventory;
