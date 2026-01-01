import { useState, useEffect } from 'react';
import { api } from '../api/client';
import type { DockingBay, Ship } from '../types';
import { Card } from '../components/ui/Card';
import { 
  Anchor, 
  Rocket, 
  LogOut, 
  LogIn, 
  AlertTriangle,
  RefreshCw,
  Box,
  Radio
} from 'lucide-react';
import { motion, AnimatePresence } from 'framer-motion';

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

  if (loading) {
    return (
      <div className="flex flex-col items-center justify-center h-[60vh] text-cyan-500/50 space-y-4">
        <Anchor className="w-12 h-12 animate-pulse" />
        <div className="font-mono text-sm tracking-widest animate-pulse">CONNECTING TO DOCKING CONTROL...</div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-col md:flex-row md:items-end justify-between gap-4">
         <div>
            <h2 className="text-3xl font-light text-white uppercase tracking-wider">
               Docking <span className="text-cyan-400 font-bold">Control</span>
            </h2>
            <div className="flex items-center gap-2 mt-1 text-cyan-500/60 font-mono text-xs">
               <span className="w-2 h-2 rounded-full bg-cyan-500/50 animate-pulse" />
               ACTIVE BAYS: {bays.length}
            </div>
         </div>
         <div className="flex items-center gap-4">
            <div className="px-3 py-1 bg-space-950/50 border border-space-700 rounded text-xs font-mono text-cyan-500/70">
              TRAFFIC: {ships.filter(s => s.status === 'INCOMING').length} INBOUND
            </div>
            <button
               onClick={loadData}
               className="p-2 text-cyan-500/50 hover:text-cyan-400 hover:bg-cyan-500/10 rounded-full transition-all"
               title="Refresh Data"
            >
               <RefreshCw className="w-5 h-5" />
            </button>
         </div>
      </div>

      {error && (
        <div className="bg-red-500/10 border border-red-500/30 text-red-400 p-4 rounded flex items-center gap-3">
          <AlertTriangle className="w-5 h-5" />
          <span>{error}</span>
        </div>
      )}

      {/* Docking Bays Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
        {bays.map((bay) => {
          const isAvailable = bay.status === 'AVAILABLE';
          const isOccupied = bay.status === 'OCCUPIED';
          const isReserved = bay.status === 'RESERVED';
          
          return (
            <Card 
              key={bay.id} 
              className={`border-t-4 transition-all duration-300 ${
                isAvailable ? 'border-t-emerald-500' :
                isOccupied ? 'border-t-red-500' :
                isReserved ? 'border-t-blue-500' : 'border-t-yellow-500'
              }`}
            >
              <div className="flex justify-between items-start mb-4">
                <div>
                  <h4 className="font-bold text-2xl font-mono text-cyan-100 tracking-wider">{bay.bayNumber}</h4>
                  <div className={`text-[10px] uppercase font-mono tracking-widest px-2 py-0.5 rounded w-fit mt-1 ${
                    isAvailable ? 'bg-emerald-500/10 text-emerald-400' :
                    isOccupied ? 'bg-red-500/10 text-red-400' :
                    'bg-blue-500/10 text-blue-400'
                  }`}>
                    {bay.status}
                  </div>
                </div>
                <div className="text-right">
                  <div className="text-[10px] text-cyan-500/50 uppercase">Capacity</div>
                  <div className="font-mono text-cyan-100 flex items-center justify-end gap-1">
                    <Box className="w-3 h-3" />
                    {bay.capacity}
                  </div>
                </div>
              </div>

              <div className="min-h-[60px] flex items-center justify-center p-3 bg-space-950/30 rounded border border-space-800/50 relative overflow-hidden">
                {/* Visual Bay Representation */}
                <div className="absolute inset-0 opacity-10 flex items-center justify-center">
                  <div className={`w-16 h-16 border-2 border-dashed rounded-full ${
                    isAvailable ? 'border-emerald-500 animate-pulse-slow' : 'border-red-500'
                  }`} />
                </div>
                
                {bay.currentShipName ? (
                  <div className="text-center z-10">
                    <div className="text-[10px] text-cyan-500/50 uppercase mb-1">Docked Vessel</div>
                    <div className="font-bold text-white tracking-wide">{bay.currentShipName}</div>
                  </div>
                ) : (
                  <div className="text-xs text-cyan-500/30 font-mono uppercase tracking-widest z-10">
                    Empty Bay
                  </div>
                )}
              </div>
            </Card>
          );
        })}
      </div>

      {/* Ships Manifest */}
      <Card title="Incoming & Outgoing Traffic" subtitle="Vessel Manifest" className="mt-8" noPadding>
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="border-b border-space-700 bg-space-950/20 text-xs font-mono text-cyan-500/70 uppercase tracking-wider">
                <th className="px-6 py-4 font-normal">Vessel Name</th>
                <th className="px-6 py-4 font-normal">Class / Type</th>
                <th className="px-6 py-4 font-normal">Crew Size</th>
                <th className="px-6 py-4 font-normal">Status</th>
                <th className="px-6 py-4 font-normal text-right">Docking Procedures</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-space-800/50">
              <AnimatePresence>
                {ships.map((ship) => (
                  <motion.tr 
                    key={ship.id}
                    initial={{ opacity: 0 }}
                    animate={{ opacity: 1 }}
                    exit={{ opacity: 0 }}
                    className="hover:bg-space-800/20 transition-colors group"
                  >
                    <td className="px-6 py-4">
                      <div className="font-bold text-cyan-100 tracking-wide flex items-center gap-3">
                        <Rocket className="w-4 h-4 text-cyan-500/50 group-hover:text-cyan-400 transition-colors" />
                        {ship.name}
                      </div>
                    </td>
                    <td className="px-6 py-4 font-mono text-sm text-cyan-100/80 uppercase">{ship.type}</td>
                    <td className="px-6 py-4 font-mono text-sm text-cyan-100/80">{ship.crewCount}</td>
                    <td className="px-6 py-4">
                      <div className={`inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-mono border ${
                        ship.status === 'INCOMING' ? 'bg-blue-500/10 text-blue-400 border-blue-500/20' :
                        ship.status === 'DOCKED' ? 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20' :
                        ship.status === 'DEPARTING' ? 'bg-yellow-500/10 text-yellow-400 border-yellow-500/20' :
                        'bg-space-800/50 text-gray-400 border-space-700'
                      }`}>
                        {ship.status === 'INCOMING' && <Radio className="w-3 h-3 animate-pulse" />}
                        {ship.status === 'DOCKED' && <Anchor className="w-3 h-3" />}
                        {ship.status === 'DEPARTING' && <LogOut className="w-3 h-3" />}
                        {ship.status}
                      </div>
                    </td>
                    <td className="px-6 py-4 text-right">
                      {ship.status === 'INCOMING' && (
                        <button
                          onClick={() => handleDock(ship.id)}
                          disabled={actionLoading === ship.id}
                          className="inline-flex items-center gap-2 px-4 py-1.5 bg-emerald-500/20 hover:bg-emerald-500/30 text-emerald-400 border border-emerald-500/50 rounded text-xs font-mono uppercase tracking-wider transition-all disabled:opacity-50 disabled:cursor-not-allowed"
                        >
                          {actionLoading === ship.id ? (
                             <div className="w-3 h-3 border-2 border-emerald-400/30 border-t-emerald-400 rounded-full animate-spin" />
                          ) : (
                             <LogIn className="w-3 h-3" />
                          )}
                          Initiate Docking
                        </button>
                      )}
                      {ship.status === 'DOCKED' && (
                        <button
                          onClick={() => handleUndock(ship.id)}
                          disabled={actionLoading === ship.id}
                          className="inline-flex items-center gap-2 px-4 py-1.5 bg-yellow-500/20 hover:bg-yellow-500/30 text-yellow-400 border border-yellow-500/50 rounded text-xs font-mono uppercase tracking-wider transition-all disabled:opacity-50 disabled:cursor-not-allowed"
                        >
                          {actionLoading === ship.id ? (
                             <div className="w-3 h-3 border-2 border-yellow-400/30 border-t-yellow-400 rounded-full animate-spin" />
                          ) : (
                             <LogOut className="w-3 h-3" />
                          )}
                          Undock Vessel
                        </button>
                      )}
                    </td>
                  </motion.tr>
                ))}
              </AnimatePresence>
            </tbody>
          </table>
          {ships.length === 0 && (
            <div className="p-8 text-center text-cyan-500/30 font-mono uppercase tracking-widest">
              No active vessel traffic
            </div>
          )}
        </div>
      </Card>
    </div>
  );
}

export default Docking;
