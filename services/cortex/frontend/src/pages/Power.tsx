import { useState, useEffect } from 'react';
import { api, extractErrorInfo } from '../api/client';
import type { PowerGridStatus, PowerAllocation } from '../types';
import { Card } from '../components/ui/Card';
import type { ErrorInfo } from '../components/ui/ErrorAlert';
import { useErrorToast } from '../context/ErrorToastContext';
import {
  Zap,
  Battery,
  Cpu,
  Activity,
  AlertTriangle,
  Plus,
  Power as PowerIcon,
  RefreshCw,
  Trash2,
  Plug
} from 'lucide-react';
import { motion, AnimatePresence } from 'framer-motion';

function Power() {
  const [grid, setGrid] = useState<PowerGridStatus | null>(null);
  const [allocations, setAllocations] = useState<PowerAllocation[]>([]);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState<ErrorInfo | null>(null);
  const { showError } = useErrorToast();
  
  // Allocation modal state
  const [showAllocateModal, setShowAllocateModal] = useState(false);
  const [allocSystem, setAllocSystem] = useState('');
  const [allocAmount, setAllocAmount] = useState(50);
  const [allocPriority, setAllocPriority] = useState(5);
  const [allocLoading, setAllocLoading] = useState(false);
  
  // Deallocation state
  const [deallocatingSystems, setDeallocatingSystems] = useState<Record<string, boolean>>({});

  useEffect(() => {
    loadData();
    const interval = setInterval(() => loadData(false), 15000);
    return () => clearInterval(interval);
  }, []);

  async function loadData(init = true) {
    try {
      if (init) setLoading(true);
      const [gridData, allocData] = await Promise.all([
        api.power.getGrid(),
        api.power.getAllocations(),
      ]);
      setGrid(gridData);
      setAllocations(allocData);
      // Only clear error on successful load if it was a manual refresh
      if (init) setLoadError(null);
    } catch (err) {
      const errorInfo = extractErrorInfo(err, 'Failed to load power data');
      setLoadError(errorInfo);
      showError(errorInfo);
    } finally {
      if (init) setLoading(false);
    }
  }

  async function handleAllocate() {
    if (!allocSystem.trim()) {
      showError({ message: 'System name is required', traceId: null });
      return;
    }

    try {
      setAllocLoading(true);
      await api.power.allocate(allocSystem.trim(), allocAmount, allocPriority);
      setShowAllocateModal(false);
      setAllocSystem('');
      setAllocAmount(50);
      setAllocPriority(5);
      await loadData(false);
    } catch (err) {
      showError(extractErrorInfo(err, 'Failed to allocate power'));
    } finally {
      setAllocLoading(false);
    }
  }

  async function handleDeallocate(systemName: string) {
    try {
      setDeallocatingSystems(prev => ({ ...prev, [systemName]: true }));
      await api.power.deallocate(systemName);
      await loadData(false);
    } catch (err) {
      showError(extractErrorInfo(err, 'Failed to deallocate power'));
    } finally {
      setDeallocatingSystems(prev => ({ ...prev, [systemName]: false }));
    }
  }

  const systemPresets = [
    { name: 'research_lab', label: 'Research Lab' },
    { name: 'medical_bay', label: 'Medical Bay' },
    { name: 'communications', label: 'Communications' },
    { name: 'sensors', label: 'Sensors' },
    { name: 'defense_systems', label: 'Defense Systems' },
    { name: 'cargo_handling', label: 'Cargo Handling' },
  ];

  if (loading) {
     return (
        <div className="flex flex-col items-center justify-center h-[60vh] text-cyan-500/50 space-y-4">
           <Zap className="w-12 h-12 animate-pulse" />
           <div className="font-mono text-sm tracking-widest animate-pulse">ANALYZING POWER GRID...</div>
        </div>
     );
  }

  if (loadError && !grid) {
    return (
      <Card className="border-red-500/50 bg-red-950/20">
        <div className="flex flex-col items-center p-8 text-center">
          <AlertTriangle className="w-12 h-12 text-red-500 mb-4" />
          <h3 className="text-xl text-red-400 font-bold mb-2 uppercase tracking-wide">Power Grid Offline</h3>
          <p className="text-red-400/70 text-sm mb-6">Unable to connect to the power grid system</p>
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

  if (!grid) return null;

  return (
    <div className="space-y-6">
       {/* Header */}
       <div className="flex flex-col md:flex-row md:items-end justify-between gap-4">
         <div>
            <h2 className="text-3xl font-light text-white uppercase tracking-wider">
               Power <span className="text-cyan-400 font-bold">Grid</span>
            </h2>
            <div className="flex items-center gap-2 mt-1 text-cyan-500/60 font-mono text-xs">
               <span className="w-2 h-2 rounded-full bg-cyan-500/50 animate-pulse" />
               OUTPUT: {Math.round(grid.totalOutputKw)} KW • LOAD: {Math.round(grid.utilizationPercent)}%
            </div>
         </div>
         <div className="flex items-center gap-4">
            <button
               onClick={() => setShowAllocateModal(true)}
               className="px-4 py-2 bg-cyan-500/20 hover:bg-cyan-500/30 text-cyan-400 border border-cyan-500/50 rounded flex items-center gap-2 transition-all font-mono text-xs uppercase tracking-wider"
            >
               <Plus className="w-4 h-4" />
               New Allocation
            </button>
            <button
               onClick={() => loadData()}
               className="p-2 text-cyan-500/50 hover:text-cyan-400 hover:bg-cyan-500/10 rounded-full transition-all"
               title="Refresh Data"
            >
               <RefreshCw className="w-5 h-5" />
            </button>
         </div>
      </div>



      {/* Main Grid Visualization */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
         {/* Status & Load */}
          <Card className="lg:col-span-2 relative overflow-hidden" title="Grid Load Distribution">
             <div className="absolute bottom-0 right-0 p-4 opacity-5 pointer-events-none">
                <Zap className="w-24 h-24" />
             </div>
            
            <div className="flex flex-col gap-6 relative z-10">
               <div>
                   <div className="flex justify-between items-end mb-2">
                      <span className="text-sm text-cyan-500/70 font-mono uppercase tracking-widest">Total Utilization</span>
                      <span className={`text-2xl font-mono font-bold ${
                         grid.utilizationPercent > 90 ? 'text-amber-400' : 
                         grid.utilizationPercent > 75 ? 'text-yellow-400' : 'text-cyan-400'
                      }`}>
                         {grid.utilizationPercent.toFixed(1)}%
                      </span>
                   </div>
                   <div className="w-full h-4 bg-space-950 rounded-full overflow-hidden border border-space-800 shadow-[inset_0_2px_4px_rgba(0,0,0,0.5)]">
                      <motion.div 
                         initial={{ width: 0 }}
                         animate={{ width: `${Math.min(grid.utilizationPercent, 100)}%` }}
                         transition={{ duration: 1.5, ease: "easeOut" }}
                         className={`h-full relative overflow-hidden ${
                            grid.utilizationPercent > 90 ? 'bg-gradient-to-r from-amber-600 to-amber-400' :
                            grid.utilizationPercent > 75 ? 'bg-gradient-to-r from-yellow-600 to-yellow-400' :
                            'bg-gradient-to-r from-cyan-600 to-cyan-400'
                         }`}
                      >
                        <div className="absolute inset-0 bg-[linear-gradient(45deg,rgba(255,255,255,0.1)_25%,transparent_25%,transparent_50%,rgba(255,255,255,0.1)_50%,rgba(255,255,255,0.1)_75%,transparent_75%,transparent)] bg-[length:1rem_1rem] animate-[progress-bar-stripes_1s_linear_infinite]" />
                     </motion.div>
                  </div>
               </div>

               <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                  <div className="p-3 bg-space-950/30 rounded border border-space-800 text-center">
                     <div className="text-2xl font-bold text-white font-mono">{Math.round(grid.totalCapacityKw)}</div>
                     <div className="text-[10px] text-cyan-500/50 uppercase tracking-wider mt-1">Capacity (kW)</div>
                  </div>
                  <div className="p-3 bg-space-950/30 rounded border border-space-800 text-center">
                     <div className="text-2xl font-bold text-blue-400 font-mono">{Math.round(grid.totalOutputKw)}</div>
                     <div className="text-[10px] text-cyan-500/50 uppercase tracking-wider mt-1">Output (kW)</div>
                  </div>
                  <div className="p-3 bg-space-950/30 rounded border border-space-800 text-center">
                     <div className="text-2xl font-bold text-yellow-400 font-mono">{Math.round(grid.totalAllocatedKw)}</div>
                     <div className="text-[10px] text-cyan-500/50 uppercase tracking-wider mt-1">Allocated (kW)</div>
                  </div>
                  <div className="p-3 bg-space-950/30 rounded border border-space-800 text-center">
                     <div className="text-2xl font-bold text-emerald-400 font-mono">{Math.round(grid.availableKw)}</div>
                     <div className="text-[10px] text-cyan-500/50 uppercase tracking-wider mt-1">Available (kW)</div>
                  </div>
               </div>
            </div>
         </Card>

         {/* Sources */}
         <Card className="h-full" title="Power Sources" subtitle={`${grid.onlineSources}/${grid.totalSources} Online`}>
            <div className="space-y-4 max-h-[300px] overflow-y-auto pr-2 scrollbar-thin scrollbar-thumb-space-700">
               {grid.sources.map((source) => (
                  <div key={source.id} className="p-3 bg-space-950/30 rounded border border-space-800">
                     <div className="flex justify-between items-center mb-2">
                        <div className="font-bold text-white text-sm flex items-center gap-2">
                           {source.type === 'SOLAR_ARRAY' && <Zap className="w-3 h-3 text-yellow-400" />}
                           {source.type === 'FUSION_REACTOR' && <Activity className="w-3 h-3 text-blue-400" />}
                           {source.type === 'BATTERY_BANK' && <Battery className="w-3 h-3 text-emerald-400" />}
                           {source.type === 'FUEL_CELL' && <Cpu className="w-3 h-3 text-orange-400" />}
                           {source.name}
                        </div>
                         <div className={`w-2 h-2 rounded-full ${source.status === 'ONLINE' ? 'bg-emerald-500 shadow-[0_0_8px_#10b981]' : source.status === 'STANDBY' ? 'bg-blue-500 shadow-[0_0_8px_#3b82f6]' : 'bg-slate-500'}`} />
                     </div>
                     <div className="space-y-1">
                        <div className="flex justify-between text-[10px] text-cyan-500/50 uppercase">
                           <span>Output</span>
                           <span>{Math.round(source.currentOutputKw)} / {Math.round(source.maxOutputKw)} kW</span>
                        </div>
                        <div className="w-full h-1 bg-space-900 rounded-full overflow-hidden">
                           <div 
                              className="h-full bg-cyan-500" 
                              style={{ width: `${source.utilizationPercent}%` }} 
                           />
                        </div>
                     </div>
                  </div>
               ))}
            </div>
         </Card>
      </div>

      {/* Allocation Matrix */}
      <Card title="Power Allocation Matrix" subtitle="Active Distribution Systems">
         <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
            <AnimatePresence>
               {allocations.map((alloc) => (
                  <motion.div
                     key={alloc.id}
                     initial={{ opacity: 0, scale: 0.95 }}
                     animate={{ opacity: 1, scale: 1 }}
                     exit={{ opacity: 0, scale: 0.95 }}
                     className="bg-space-950/30 border border-space-800 rounded p-4 relative group hover:border-cyan-500/30 transition-colors"
                  >
                     <div className="flex justify-between items-start mb-3">
                        <div className="font-bold text-white tracking-wide">{alloc.systemName}</div>
                         <span className={`text-[10px] px-1.5 py-0.5 rounded font-mono border ${
                            alloc.priority <= 2 ? 'text-purple-400 border-purple-500/30 bg-purple-500/10' :
                            alloc.priority <= 4 ? 'text-amber-400 border-amber-500/30 bg-amber-500/10' :
                            'text-cyan-400 border-cyan-500/30 bg-cyan-500/10'
                         }`}>
                            PRIORITY {alloc.priority}
                         </span>
                     </div>
                     
                     <div className="flex items-center gap-2 mb-4">
                        <Plug className="w-4 h-4 text-cyan-500/50" />
                        <span className="text-2xl font-mono text-cyan-100">{Math.round(alloc.allocatedKw)}</span>
                        <span className="text-xs text-cyan-500/50 mt-2">kW</span>
                     </div>

                      <button
                         onClick={() => handleDeallocate(alloc.systemName)}
                         disabled={!!deallocatingSystems[alloc.systemName]}
                         className="w-full py-2 bg-slate-500/10 hover:bg-slate-500/20 text-slate-400 border border-slate-500/20 rounded text-xs font-mono uppercase tracking-wider flex items-center justify-center gap-2 transition-all"
                      >
                         {deallocatingSystems[alloc.systemName] ? (
                            <div className="w-3 h-3 border-2 border-slate-400/30 border-t-slate-400 rounded-full animate-spin" />
                         ) : (
                            <Trash2 className="w-3 h-3" />
                         )}
                         Deallocate
                      </button>
                     
                     {/* Always visible on mobile, hidden on desktop until hover */}
                     <div className="md:hidden mt-2">
                        {/* Hidden duplicate removed as main button is now always visible */}
                     </div>
                  </motion.div>
               ))}
            </AnimatePresence>
            
            {allocations.length === 0 && (
               <div className="col-span-full py-8 text-center text-cyan-500/30 border-2 border-dashed border-space-800 rounded">
                  <PowerIcon className="w-8 h-8 mx-auto mb-2 opacity-50" />
                  <div className="font-mono uppercase tracking-widest">No Active Allocations</div>
               </div>
            )}
         </div>
      </Card>

      {/* Allocation Modal */}
      <AnimatePresence>
         {showAllocateModal && (
            <div className="fixed inset-0 bg-space-950/80 backdrop-blur-sm flex items-center justify-center z-50 p-4">
               <motion.div 
                  initial={{ opacity: 0, scale: 0.9 }}
                  animate={{ opacity: 1, scale: 1 }}
                  exit={{ opacity: 0, scale: 0.9 }}
                  className="bg-space-900 border border-space-700 rounded-lg max-w-md w-full shadow-[0_0_50px_rgba(0,0,0,0.5)] overflow-hidden"
               >
                  <div className="p-4 border-b border-space-700 bg-space-950/50">
                     <h3 className="text-lg font-bold text-white flex items-center gap-2">
                        <Plus className="w-5 h-5 text-cyan-400" />
                        New Power Allocation
                     </h3>
                  </div>
                  
                  <div className="p-6 space-y-6">
                     <div className="space-y-2">
                        <label className="text-xs text-cyan-500/70 font-mono uppercase">System Identifier</label>
                        <input
                           type="text"
                           value={allocSystem}
                           onChange={(e) => setAllocSystem(e.target.value)}
                           placeholder="ENTER SYSTEM ID..."
                           className="w-full bg-space-950 border border-space-700 rounded p-2 text-cyan-100 font-mono text-sm placeholder:text-space-700 focus:border-cyan-500/50 focus:outline-none transition-colors"
                        />
                        <div className="flex flex-wrap gap-2 mt-2">
                           {systemPresets.map((preset) => (
                              <button
                                 key={preset.name}
                                 onClick={() => setAllocSystem(preset.name)}
                                 className={`px-2 py-1 rounded border text-[10px] font-mono uppercase transition-all ${
                                    allocSystem === preset.name
                                       ? 'bg-cyan-500/20 border-cyan-500/50 text-cyan-300'
                                       : 'bg-space-800/50 border-space-700 text-cyan-500/50 hover:text-cyan-400'
                                 }`}
                              >
                                 {preset.label}
                              </button>
                           ))}
                        </div>
                     </div>

                     <div className="space-y-2">
                        <div className="flex justify-between text-xs text-cyan-500/70 font-mono uppercase">
                           <span>Allocation Amount</span>
                           <span className="text-cyan-100">{allocAmount} kW</span>
                        </div>
                        <input
                           type="range"
                           min="10"
                           max="500"
                           step="10"
                           value={allocAmount}
                           onChange={(e) => setAllocAmount(parseInt(e.target.value))}
                           className="w-full accent-cyan-500 h-1 bg-space-800 rounded-lg appearance-none cursor-pointer"
                        />
                        <div className="flex justify-between text-[10px] text-cyan-500/30 font-mono uppercase">
                           <span>10 kW</span>
                           <span>500 kW</span>
                        </div>
                     </div>

                      <div className="space-y-2">
                         <div className="flex justify-between text-xs text-cyan-500/70 font-mono uppercase">
                            <span>Priority Level</span>
                            <span className={`font-bold ${
                               allocPriority <= 2 ? 'text-purple-400' :
                               allocPriority <= 4 ? 'text-amber-400' : 'text-cyan-400'
                            }`}>LEVEL {allocPriority}</span>
                         </div>
                         <div className="flex gap-1">
                            {[1, 2, 3, 4, 5, 6, 7, 8, 9, 10].map((p) => (
                               <button
                                  key={p}
                                  onClick={() => setAllocPriority(p)}
                                  className={`flex-1 h-8 rounded text-xs font-mono transition-all ${
                                     allocPriority === p
                                        ? p <= 2 ? 'bg-purple-500 text-white shadow-[0_0_10px_rgba(168,85,247,0.5)]' :
                                          p <= 4 ? 'bg-amber-500 text-white shadow-[0_0_10px_rgba(245,158,11,0.5)]' : 
                                          'bg-cyan-500 text-white shadow-[0_0_10px_rgba(6,182,212,0.5)]'
                                        : 'bg-space-800 text-cyan-500/30 hover:bg-space-700'
                                  }`}
                               >
                                  {p}
                               </button>
                            ))}
                         </div>
                         <div className="text-[10px] text-cyan-500/30 font-mono uppercase text-center mt-1">
                            1 = Critical • 10 = Low Priority
                         </div>
                      </div>

                     {grid && allocAmount > grid.availableKw && (
                        <div className="p-3 bg-yellow-500/10 border border-yellow-500/30 rounded text-yellow-400 text-xs flex items-center gap-2">
                           <AlertTriangle className="w-4 h-4 shrink-0" />
                           Warning: Exceeds currently available grid capacity.
                        </div>
                     )}
                  </div>

                  <div className="p-4 border-t border-space-700 bg-space-950/50 flex gap-3">
                     <button
                        onClick={() => setShowAllocateModal(false)}
                        className="flex-1 py-2 text-cyan-500/70 hover:text-cyan-400 font-mono text-sm uppercase tracking-wider"
                     >
                        Cancel
                     </button>
                     <button
                        onClick={handleAllocate}
                        disabled={allocLoading || !allocSystem.trim()}
                        className="flex-1 py-2 bg-cyan-500/20 hover:bg-cyan-500/30 text-cyan-400 border border-cyan-500/50 rounded transition-all font-mono text-sm uppercase tracking-wider flex items-center justify-center gap-2 disabled:opacity-50"
                     >
                        {allocLoading ? 'Processing...' : 'Confirm'}
                     </button>
                  </div>
               </motion.div>
            </div>
         )}
      </AnimatePresence>
    </div>
  );
}

export default Power;
