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
  
  // Consume modal state
  const [consumingSupply, setConsumingSupply] = useState<Supply | null>(null);
  const [consumeQuantity, setConsumeQuantity] = useState(1);
  const [consumeLoading, setConsumeLoading] = useState(false);
  
  // Resupply modal state
  const [resupplyingSupply, setResupplyingSupply] = useState<Supply | null>(null);
  const [resupplyQuantity, setResupplyQuantity] = useState(100);
  const [resupplyLoading, setResupplyLoading] = useState(false);
  
  // Unload state
  const [unloadingManifest, setUnloadingManifest] = useState<number | null>(null);

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
      setUnloadingManifest(manifestId);
      setError(null);
      await api.inventory.unloadManifest(manifestId);
      await loadData();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to unload manifest');
    } finally {
      setUnloadingManifest(null);
    }
  }

  async function handleConsume() {
    if (!consumingSupply) return;
    
    try {
      setConsumeLoading(true);
      setError(null);
      await api.inventory.consume(consumingSupply.id, consumeQuantity);
      setConsumingSupply(null);
      await loadData();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to consume supply');
    } finally {
      setConsumeLoading(false);
    }
  }

  async function handleResupply() {
    if (!resupplyingSupply) return;
    
    try {
      setResupplyLoading(true);
      setError(null);
      await api.inventory.requestResupply(resupplyingSupply.id, resupplyQuantity);
      setResupplyingSupply(null);
      await loadData();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to request resupply');
    } finally {
      setResupplyLoading(false);
    }
  }

  function openConsumeModal(supply: Supply) {
    setConsumingSupply(supply);
    setConsumeQuantity(Math.min(10, supply.quantity));
  }

  function openResupplyModal(supply: Supply) {
    setResupplyingSupply(supply);
    setResupplyQuantity(supply.minThreshold * 2);
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
              <th className="px-4 py-3 text-right text-sm font-medium text-gray-300">Actions</th>
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
                <td className="px-4 py-3 text-right">
                  <div className="flex gap-1 justify-end">
                    <button
                      onClick={() => openConsumeModal(supply)}
                      disabled={supply.quantity === 0}
                      className={`px-2 py-1 rounded text-xs ${
                        supply.quantity === 0
                          ? 'bg-gray-600 text-gray-400 cursor-not-allowed'
                          : 'bg-orange-600 hover:bg-orange-700 text-white'
                      }`}
                    >
                      Use
                    </button>
                    <button
                      onClick={() => openResupplyModal(supply)}
                      className="px-2 py-1 bg-green-600 hover:bg-green-700 rounded text-xs text-white"
                    >
                      Resupply
                    </button>
                  </div>
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
                      disabled={unloadingManifest === manifest.id}
                      className={`px-3 py-1 rounded text-sm ${
                        unloadingManifest === manifest.id
                          ? 'bg-gray-600 text-gray-400 cursor-not-allowed'
                          : 'bg-green-600 hover:bg-green-700 text-white'
                      }`}
                    >
                      {unloadingManifest === manifest.id ? 'Unloading...' : 'Unload'}
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

      {/* Consume Modal */}
      {consumingSupply && (
        <div className="fixed inset-0 bg-black/70 flex items-center justify-center z-50">
          <div className="bg-gray-800 rounded-lg p-6 max-w-md w-full mx-4">
            <h3 className="text-lg font-semibold text-white mb-4">
              Use Supply: {consumingSupply.name}
            </h3>
            
            <p className="text-gray-400 text-sm mb-4">
              Available: <span className="text-white">{consumingSupply.quantity} {consumingSupply.unit}</span>
            </p>

            <div className="mb-4">
              <label className="block text-sm font-medium text-gray-300 mb-2">
                Quantity to use: {consumeQuantity} {consumingSupply.unit}
              </label>
              <input
                type="range"
                min="1"
                max={consumingSupply.quantity}
                value={consumeQuantity}
                onChange={(e) => setConsumeQuantity(parseInt(e.target.value))}
                className="w-full"
              />
              <div className="flex justify-between text-xs text-gray-500">
                <span>1</span>
                <span>{Math.round(consumingSupply.quantity / 2)}</span>
                <span>{consumingSupply.quantity}</span>
              </div>
            </div>

            {consumingSupply.quantity - consumeQuantity < consumingSupply.minThreshold && (
              <div className="bg-yellow-900/50 border border-yellow-500 rounded p-2 mb-4">
                <p className="text-yellow-300 text-xs">
                  Warning: This will bring stock below minimum threshold
                </p>
              </div>
            )}

            <div className="flex gap-3">
              <button
                onClick={handleConsume}
                disabled={consumeLoading}
                className={`flex-1 py-2 rounded text-sm ${
                  consumeLoading
                    ? 'bg-gray-600 text-gray-400 cursor-not-allowed'
                    : 'bg-orange-600 hover:bg-orange-700 text-white'
                }`}
              >
                {consumeLoading ? 'Processing...' : 'Confirm Use'}
              </button>
              <button
                onClick={() => setConsumingSupply(null)}
                disabled={consumeLoading}
                className="px-4 py-2 bg-gray-600 hover:bg-gray-500 rounded text-sm"
              >
                Cancel
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Resupply Modal */}
      {resupplyingSupply && (
        <div className="fixed inset-0 bg-black/70 flex items-center justify-center z-50">
          <div className="bg-gray-800 rounded-lg p-6 max-w-md w-full mx-4">
            <h3 className="text-lg font-semibold text-white mb-4">
              Request Resupply: {resupplyingSupply.name}
            </h3>
            
            <p className="text-gray-400 text-sm mb-2">
              Current stock: <span className={resupplyingSupply.isLowStock ? 'text-red-400' : 'text-white'}>
                {resupplyingSupply.quantity} {resupplyingSupply.unit}
              </span>
            </p>
            <p className="text-gray-400 text-sm mb-4">
              Minimum threshold: <span className="text-white">{resupplyingSupply.minThreshold} {resupplyingSupply.unit}</span>
            </p>

            <div className="mb-4">
              <label className="block text-sm font-medium text-gray-300 mb-2">
                Quantity to request: {resupplyQuantity} {resupplyingSupply.unit}
              </label>
              <input
                type="range"
                min={resupplyingSupply.minThreshold}
                max={resupplyingSupply.minThreshold * 5}
                step={10}
                value={resupplyQuantity}
                onChange={(e) => setResupplyQuantity(parseInt(e.target.value))}
                className="w-full"
              />
              <div className="flex justify-between text-xs text-gray-500">
                <span>{resupplyingSupply.minThreshold}</span>
                <span>{resupplyingSupply.minThreshold * 3}</span>
                <span>{resupplyingSupply.minThreshold * 5}</span>
              </div>
            </div>

            <div className="flex gap-3">
              <button
                onClick={handleResupply}
                disabled={resupplyLoading}
                className={`flex-1 py-2 rounded text-sm ${
                  resupplyLoading
                    ? 'bg-gray-600 text-gray-400 cursor-not-allowed'
                    : 'bg-green-600 hover:bg-green-700 text-white'
                }`}
              >
                {resupplyLoading ? 'Submitting...' : 'Submit Request'}
              </button>
              <button
                onClick={() => setResupplyingSupply(null)}
                disabled={resupplyLoading}
                className="px-4 py-2 bg-gray-600 hover:bg-gray-500 rounded text-sm"
              >
                Cancel
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default Inventory;
