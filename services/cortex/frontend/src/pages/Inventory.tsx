import { useState, useEffect } from 'react';
import { api, extractErrorInfo } from '../api/client';
import type { Supply, CargoManifest, ResupplyRequest } from '../types';
import { Card } from '../components/ui/Card';
import type { ErrorInfo } from '../components/ui/ErrorAlert';
import { useErrorToast } from '../context/ErrorToastContext';
import {
  Package,
  AlertTriangle,
  ShoppingCart,
  MinusCircle,
  PlusCircle,
  Filter,
  RefreshCw,
  Box
} from 'lucide-react';
import { motion, AnimatePresence } from 'framer-motion';

function Inventory() {
  const [supplies, setSupplies] = useState<Supply[]>([]);
  const [manifests, setManifests] = useState<CargoManifest[]>([]);
  const [resupplyRequests, setResupplyRequests] = useState<ResupplyRequest[]>([]);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState<ErrorInfo | null>(null);
  const [selectedCategory, setSelectedCategory] = useState<string | null>(null);
  const { showError } = useErrorToast();
  
  // Consume modal state
  const [consumingSupply, setConsumingSupply] = useState<Supply | null>(null);
  const [consumeQuantity, setConsumeQuantity] = useState(1);
  const [consumeLoading, setConsumeLoading] = useState(false);
  
  // Resupply modal state
  const [resupplyingSupply, setResupplyingSupply] = useState<Supply | null>(null);
  const [resupplyQuantity, setResupplyQuantity] = useState(100);
  const [resupplyLoading, setResupplyLoading] = useState(false);
  
  // Unload state
  const [unloadingManifests, setUnloadingManifests] = useState<Record<number, boolean>>({});

  useEffect(() => {
    loadData();
    const interval = setInterval(() => loadData(false), 15000);
    return () => clearInterval(interval);
  }, []);

  async function loadData(init = true) {
    try {
      if (init) setLoading(true);
      const [suppliesData, manifestsData, requestsData] = await Promise.all([
        api.inventory.getSupplies(),
        api.inventory.getManifests(),
        api.inventory.getResupplyRequests(),
      ]);
      setSupplies(suppliesData);
      setManifests(manifestsData);
      setResupplyRequests(requestsData);
      // Only clear error on successful load if it was a manual refresh
      if (init) setLoadError(null);
    } catch (err) {
      const errorInfo = extractErrorInfo(err, 'Failed to load inventory data');
      setLoadError(errorInfo);
      showError(errorInfo);
    } finally {
      if (init) setLoading(false);
    }
  }

  async function handleUnload(manifestId: number) {
    try {
      setUnloadingManifests(prev => ({ ...prev, [manifestId]: true }));
      await api.inventory.unloadManifest(manifestId);
      await loadData(false);
    } catch (err) {
      showError(extractErrorInfo(err, 'Failed to unload manifest'));
    } finally {
      setUnloadingManifests(prev => ({ ...prev, [manifestId]: false }));
    }
  }

  async function handleConsume() {
    if (!consumingSupply) return;

    try {
      setConsumeLoading(true);
      await api.inventory.consume(consumingSupply.id, consumeQuantity);
      setConsumingSupply(null);
      await loadData(false);
    } catch (err) {
      showError(extractErrorInfo(err, 'Failed to consume supply'));
    } finally {
      setConsumeLoading(false);
    }
  }

  async function handleResupply() {
    if (!resupplyingSupply) return;

    try {
      setResupplyLoading(true);
      await api.inventory.requestResupply(resupplyingSupply.id, resupplyQuantity);
      setResupplyingSupply(null);
      await loadData(false);
    } catch (err) {
      showError(extractErrorInfo(err, 'Failed to request resupply'));
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

  const getCategoryColor = (category: string) => {
    const colors: Record<string, string> = {
      FOOD: 'text-emerald-400 bg-emerald-400/10 border-emerald-400/20',
      MEDICAL: 'text-red-400 bg-red-400/10 border-red-400/20',
      MECHANICAL: 'text-zinc-400 bg-zinc-400/10 border-zinc-400/20',
      ELECTRONIC: 'text-blue-400 bg-blue-400/10 border-blue-400/20',
      FUEL: 'text-orange-400 bg-orange-400/10 border-orange-400/20',
      WATER: 'text-cyan-400 bg-cyan-400/10 border-cyan-400/20',
      OXYGEN: 'text-sky-400 bg-sky-400/10 border-sky-400/20',
      GENERAL: 'text-purple-400 bg-purple-400/10 border-purple-400/20',
    };
    return colors[category] || colors.GENERAL;
  };

  if (loading) {
     return (
        <div className="flex flex-col items-center justify-center h-[60vh] text-cyan-500/50 space-y-4">
           <Package className="w-12 h-12 animate-pulse" />
           <div className="font-mono text-sm tracking-widest animate-pulse">CHECKING INVENTORY LEVELS...</div>
        </div>
     );
  }

  if (loadError && supplies.length === 0) {
    return (
      <Card className="border-red-500/50 bg-red-950/20">
        <div className="flex flex-col items-center p-8 text-center">
          <Package className="w-12 h-12 text-red-500 mb-4" />
          <h3 className="text-xl text-red-400 font-bold mb-2 uppercase tracking-wide">Inventory System Offline</h3>
          <p className="text-red-400/70 text-sm mb-6">Unable to connect to the inventory system</p>
          <button
            onClick={() => loadData()}
            className="px-6 py-2 bg-red-500/20 hover:bg-red-500/30 text-red-400 border border-red-500/50 rounded transition-all font-mono text-sm uppercase tracking-wider flex items-center gap-2"
          >
            <RefreshCw className="w-4 h-4" />
            Retry Connection
          </button>
        </div>
      </Card>
    );
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col md:flex-row md:items-end justify-between gap-4">
         <div>
            <h2 className="text-3xl font-light text-white uppercase tracking-wider">
               Inventory <span className="text-cyan-400 font-bold">Logistics</span>
            </h2>
            <div className="flex items-center gap-2 mt-1 text-cyan-500/60 font-mono text-xs">
               <span className="w-2 h-2 rounded-full bg-cyan-500/50 animate-pulse" />
               TOTAL SKUS: {supplies.length}
            </div>
         </div>
         <div className="flex items-center gap-4">
            {lowStockCount > 0 && (
               <div className="px-3 py-1 bg-red-500/10 border border-red-500/30 rounded text-xs font-mono text-red-400 flex items-center gap-2 animate-pulse">
                  <AlertTriangle className="w-3 h-3" />
                  CRITICAL: {lowStockCount} ITEMS LOW
               </div>
            )}
            <button
               onClick={() => loadData()}
               className="p-2 text-cyan-500/50 hover:text-cyan-400 hover:bg-cyan-500/10 rounded-full transition-all"
               title="Refresh Data"
            >
               <RefreshCw className="w-5 h-5" />
            </button>
         </div>
      </div>



      {/* Category Filter Bar */}
      <div className="flex overflow-x-auto pb-2 gap-2 scrollbar-thin scrollbar-thumb-space-700 scrollbar-track-transparent">
         <button
            onClick={() => setSelectedCategory(null)}
            className={`flex items-center gap-2 px-4 py-2 rounded border transition-all whitespace-nowrap ${
               selectedCategory === null
                  ? 'bg-cyan-500/20 border-cyan-500/50 text-cyan-300 shadow-[0_0_10px_rgba(34,211,238,0.2)]'
                  : 'bg-space-900/50 border-space-700 text-cyan-500/50 hover:border-cyan-500/30 hover:text-cyan-400'
            }`}
         >
            <Filter className="w-3 h-3" />
            <span className="font-mono uppercase text-xs">All Categories</span>
         </button>
         {categories.map((cat) => (
            <button
               key={cat}
               onClick={() => setSelectedCategory(cat)}
               className={`px-4 py-2 rounded border transition-all whitespace-nowrap font-mono uppercase text-xs ${
                  selectedCategory === cat
                     ? 'bg-cyan-500/20 border-cyan-500/50 text-cyan-300 shadow-[0_0_10px_rgba(34,211,238,0.2)]'
                     : 'bg-space-900/50 border-space-700 text-cyan-500/50 hover:border-cyan-500/30 hover:text-cyan-400'
               }`}
            >
               {cat}
            </button>
         ))}
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
         {/* Main Supply List */}
         <div className="lg:col-span-2 space-y-4">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
               {filteredSupplies.map((supply) => (
                  <Card key={supply.id} className={`transition-all hover:bg-space-800/30 ${supply.isLowStock ? 'border-l-2 border-l-red-500' : ''}`} noPadding>
                     <div className="p-4">
                        <div className="flex justify-between items-start mb-3">
                           <div>
                              <div className="font-bold text-cyan-100 tracking-wide">{supply.name}</div>
                              <span className={`inline-block mt-1 px-1.5 py-0.5 rounded text-[10px] font-mono uppercase tracking-wider border ${getCategoryColor(supply.category)}`}>
                                 {supply.category}
                              </span>
                           </div>
                           <div className="text-right">
                              <div className={`text-xl font-mono font-bold ${supply.isLowStock ? 'text-red-400' : 'text-emerald-400'}`}>
                                 {supply.quantity}
                              </div>
                              <div className="text-[10px] text-cyan-500/50 uppercase">{supply.unit}</div>
                           </div>
                        </div>
                        
                        {/* Progress Bar for Stock Level (Simulated max based on minThreshold * 3) */}
                        <div className="w-full h-1 bg-space-950 rounded-full overflow-hidden mb-4">
                           <div 
                              className={`h-full ${supply.isLowStock ? 'bg-red-500' : 'bg-emerald-500'}`}
                              style={{ width: `${Math.min(100, (supply.quantity / (supply.minThreshold * 3)) * 100)}%` }}
                           />
                        </div>

                        <div className="flex gap-2">
                           <button
                              onClick={() => openConsumeModal(supply)}
                              disabled={supply.quantity === 0}
                              className="flex-1 py-1.5 flex items-center justify-center gap-2 rounded border border-orange-500/30 bg-orange-500/10 text-orange-400 hover:bg-orange-500/20 text-xs font-mono uppercase tracking-wider transition-all disabled:opacity-50 disabled:cursor-not-allowed"
                           >
                              <MinusCircle className="w-3 h-3" />
                              Use
                           </button>
                           <button
                              onClick={() => openResupplyModal(supply)}
                              className="flex-1 py-1.5 flex items-center justify-center gap-2 rounded border border-cyan-500/30 bg-cyan-500/10 text-cyan-400 hover:bg-cyan-500/20 text-xs font-mono uppercase tracking-wider transition-all"
                           >
                              <PlusCircle className="w-3 h-3" />
                              Order
                           </button>
                        </div>
                     </div>
                  </Card>
               ))}
            </div>
         </div>

         {/* Sidebar: Cargo & History */}
         <div className="space-y-6">
            {/* Pending Cargo */}
            <Card title="Cargo Manifests" subtitle="Pending Unloading" className="h-fit">
               {manifests.filter((m) => m.status === 'PENDING').length === 0 ? (
                  <div className="text-center py-8 text-cyan-500/30">
                     <Box className="w-8 h-8 mx-auto mb-2 opacity-50" />
                     <div className="text-xs font-mono uppercase tracking-widest">No pending cargo</div>
                  </div>
               ) : (
                  <div className="space-y-4">
                     {manifests
                        .filter((m) => m.status === 'PENDING')
                        .map((manifest) => (
                           <div key={manifest.id} className="p-3 bg-space-950/30 rounded border border-space-800">
                              <div className="flex justify-between items-start mb-2">
                                 <div>
                                    <div className="font-bold text-cyan-100 text-sm">{manifest.shipName}</div>
                                    <div className="text-[10px] text-cyan-500/50 uppercase tracking-wider">{new Date(manifest.createdAt).toLocaleDateString()}</div>
                                 </div>
                                 <button
                                    onClick={() => handleUnload(manifest.id)}
                                    disabled={!!unloadingManifests[manifest.id]}
                                    className="p-1.5 bg-emerald-500/20 text-emerald-400 hover:bg-emerald-500/30 rounded transition-all disabled:opacity-50"
                                    title="Unload Cargo"
                                 >
                                    {unloadingManifests[manifest.id] ? (
                                       <div className="w-4 h-4 border-2 border-emerald-400/30 border-t-emerald-400 rounded-full animate-spin" />
                                    ) : (
                                       <Box className="w-4 h-4" />
                                    )}
                                 </button>
                              </div>
                              <div className="space-y-1">
                                 {manifest.items.map((item) => (
                                    <div key={item.id} className="flex justify-between text-xs text-cyan-500/70 font-mono">
                                       <span>{item.supplyName}</span>
                                       <span>x{item.quantity}</span>
                                    </div>
                                 ))}
                              </div>
                           </div>
                        ))}
                  </div>
               )}
            </Card>

            {/* Recent Orders */}
            <Card title="Resupply Log" subtitle="Recent Requests" className="h-fit">
               <div className="space-y-3 max-h-[400px] overflow-y-auto pr-2 scrollbar-thin scrollbar-thumb-space-700">
                  {resupplyRequests.map((req) => (
                     <div key={req.id} className="flex items-center justify-between p-2 border-b border-space-800/50 last:border-0">
                        <div>
                           <div className="text-sm text-cyan-100">{req.supplyName}</div>
                           <div className="text-[10px] text-cyan-500/50 uppercase tracking-wider">
                              QTY: {req.quantity} • {new Date(req.requestedAt).toLocaleDateString()}
                           </div>
                        </div>
                        <div className={`px-2 py-0.5 rounded text-[10px] font-mono uppercase tracking-wider border ${
                           req.status === 'DELIVERED' ? 'text-emerald-400 border-emerald-500/30 bg-emerald-500/10' :
                           req.status === 'IN_TRANSIT' ? 'text-blue-400 border-blue-500/30 bg-blue-500/10' :
                           'text-yellow-400 border-yellow-500/30 bg-yellow-500/10'
                        }`}>
                           {req.status}
                        </div>
                     </div>
                  ))}
                  {resupplyRequests.length === 0 && (
                     <div className="text-center py-4 text-cyan-500/30 text-xs font-mono uppercase">No recent requests</div>
                  )}
               </div>
            </Card>
         </div>
      </div>

      {/* Action Modals */}
      <AnimatePresence>
         {/* Consume Modal */}
         {consumingSupply && (
            <div className="fixed inset-0 bg-space-950/80 backdrop-blur-sm flex items-center justify-center z-50 p-4">
               <motion.div 
                  initial={{ opacity: 0, scale: 0.9 }}
                  animate={{ opacity: 1, scale: 1 }}
                  exit={{ opacity: 0, scale: 0.9 }}
                  className="bg-space-900 border border-space-700 rounded-lg max-w-sm w-full shadow-[0_0_50px_rgba(0,0,0,0.5)] overflow-hidden"
               >
                  <div className="p-4 border-b border-space-700 bg-space-950/50">
                     <h3 className="text-lg font-bold text-white flex items-center gap-2">
                        <MinusCircle className="w-5 h-5 text-orange-400" />
                        Dispense Item
                     </h3>
                  </div>
                  
                  <div className="p-6 space-y-6">
                     <div className="text-center">
                        <div className="text-2xl font-bold text-cyan-100">{consumingSupply.name}</div>
                        <div className="text-sm text-cyan-500/50 font-mono uppercase mt-1">
                           Available: {consumingSupply.quantity} {consumingSupply.unit}
                        </div>
                     </div>

                     <div className="space-y-2">
                        <div className="flex justify-between text-xs text-cyan-500/70 font-mono uppercase">
                           <span>Amount</span>
                           <span>{consumeQuantity} {consumingSupply.unit}</span>
                        </div>
                        <input
                           type="range"
                           min="1"
                           max={consumingSupply.quantity}
                           value={consumeQuantity}
                           onChange={(e) => setConsumeQuantity(parseInt(e.target.value))}
                           className="w-full accent-orange-500 h-1 bg-space-800 rounded-lg appearance-none cursor-pointer"
                        />
                     </div>

                     {consumingSupply.quantity - consumeQuantity < consumingSupply.minThreshold && (
                        <div className="p-3 bg-yellow-500/10 border border-yellow-500/30 rounded text-yellow-400 text-xs flex items-center gap-2">
                           <AlertTriangle className="w-4 h-4 shrink-0" />
                           Warning: Stock will fall below minimum threshold.
                        </div>
                     )}
                  </div>

                  <div className="p-4 border-t border-space-700 bg-space-950/50 flex gap-3">
                     <button
                        onClick={() => setConsumingSupply(null)}
                        className="flex-1 py-2 text-cyan-500/70 hover:text-cyan-400 font-mono text-sm uppercase tracking-wider"
                     >
                        Cancel
                     </button>
                     <button
                        onClick={handleConsume}
                        disabled={consumeLoading}
                        className="flex-1 py-2 bg-orange-500/20 hover:bg-orange-500/30 text-orange-400 border border-orange-500/50 rounded transition-all font-mono text-sm uppercase tracking-wider flex items-center justify-center gap-2 disabled:opacity-50"
                     >
                        {consumeLoading ? 'Dispensing...' : 'Confirm'}
                     </button>
                  </div>
               </motion.div>
            </div>
         )}

         {/* Resupply Modal */}
         {resupplyingSupply && (
            <div className="fixed inset-0 bg-space-950/80 backdrop-blur-sm flex items-center justify-center z-50 p-4">
               <motion.div 
                  initial={{ opacity: 0, scale: 0.9 }}
                  animate={{ opacity: 1, scale: 1 }}
                  exit={{ opacity: 0, scale: 0.9 }}
                  className="bg-space-900 border border-space-700 rounded-lg max-w-sm w-full shadow-[0_0_50px_rgba(0,0,0,0.5)] overflow-hidden"
               >
                  <div className="p-4 border-b border-space-700 bg-space-950/50">
                     <h3 className="text-lg font-bold text-white flex items-center gap-2">
                        <ShoppingCart className="w-5 h-5 text-cyan-400" />
                        Requisition Request
                     </h3>
                  </div>
                  
                  <div className="p-6 space-y-6">
                     <div className="text-center">
                        <div className="text-2xl font-bold text-cyan-100">{resupplyingSupply.name}</div>
                        <div className="text-sm text-cyan-500/50 font-mono uppercase mt-1">
                           Current Stock: {resupplyingSupply.quantity} {resupplyingSupply.unit}
                        </div>
                     </div>

                     <div className="space-y-2">
                        <div className="flex justify-between text-xs text-cyan-500/70 font-mono uppercase">
                           <span>Order Quantity</span>
                           <span>{resupplyQuantity} {resupplyingSupply.unit}</span>
                        </div>
                        <input
                           type="range"
                           min={resupplyingSupply.minThreshold}
                           max={resupplyingSupply.minThreshold * 5}
                           step={10}
                           value={resupplyQuantity}
                           onChange={(e) => setResupplyQuantity(parseInt(e.target.value))}
                           className="w-full accent-cyan-500 h-1 bg-space-800 rounded-lg appearance-none cursor-pointer"
                        />
                        <div className="flex justify-between text-[10px] text-cyan-500/30 font-mono uppercase">
                           <span>Min ({resupplyingSupply.minThreshold})</span>
                           <span>Max ({resupplyingSupply.minThreshold * 5})</span>
                        </div>
                     </div>
                  </div>

                  <div className="p-4 border-t border-space-700 bg-space-950/50 flex gap-3">
                     <button
                        onClick={() => setResupplyingSupply(null)}
                        className="flex-1 py-2 text-cyan-500/70 hover:text-cyan-400 font-mono text-sm uppercase tracking-wider"
                     >
                        Cancel
                     </button>
                     <button
                        onClick={handleResupply}
                        disabled={resupplyLoading}
                        className="flex-1 py-2 bg-cyan-500/20 hover:bg-cyan-500/30 text-cyan-400 border border-cyan-500/50 rounded transition-all font-mono text-sm uppercase tracking-wider flex items-center justify-center gap-2 disabled:opacity-50"
                     >
                        {resupplyLoading ? 'Transmitting...' : 'Submit Order'}
                     </button>
                  </div>
               </motion.div>
            </div>
         )}
      </AnimatePresence>
    </div>
  );
}

export default Inventory;
