import { useState, useEffect } from 'react';
import { api } from '../api/client';
import type { DashboardStatus } from '../types';
import { Card, StatsCard } from '../components/ui/Card';
import { 
  Users, 
  Anchor, 
  ThermometerSun, 
  Zap, 
  Package, 
  AlertTriangle, 
  Activity,
  RefreshCw
} from 'lucide-react';
import { motion } from 'framer-motion';

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
      <div className="flex flex-col items-center justify-center h-[60vh] text-cyan-500/50 space-y-4">
        <Activity className="w-12 h-12 animate-pulse" />
        <div className="font-mono text-sm tracking-widest animate-pulse">ESTABLISHING UPLINK...</div>
      </div>
    );
  }

  if (error) {
    return (
      <Card className="border-red-500/50 bg-red-950/20">
        <div className="flex flex-col items-center p-8 text-center">
          <AlertTriangle className="w-12 h-12 text-red-500 mb-4" />
          <h3 className="text-xl text-red-400 font-bold mb-2 uppercase tracking-wide">Signal Lost</h3>
          <p className="text-red-300/70 font-mono mb-6">{error}</p>
          <button 
            onClick={loadDashboard}
            className="px-6 py-2 bg-red-500/20 hover:bg-red-500/30 text-red-400 border border-red-500/50 rounded transition-all font-mono text-sm uppercase tracking-wider flex items-center gap-2"
          >
            <RefreshCw className="w-4 h-4" />
            Retry Connection
          </button>
        </div>
      </Card>
    );
  }

  if (!status) return null;

  return (
    <div className="space-y-6">
      {/* Header Section */}
      <div className="flex flex-col md:flex-row md:items-end justify-between gap-4">
        <div>
          <h2 className="text-3xl font-light text-white uppercase tracking-wider">
            Station <span className="text-cyan-400 font-bold">Status</span>
          </h2>
          <div className="flex items-center gap-2 mt-1 text-cyan-500/60 font-mono text-xs">
            <span className="w-2 h-2 rounded-full bg-cyan-500/50 animate-pulse" />
            LAST SYNC: {new Date(status.timestamp).toLocaleTimeString()}
          </div>
        </div>
        
        <div className="flex items-center gap-4">
          <div className="text-right">
             <div className="text-xs font-mono text-cyan-500/50 uppercase tracking-widest">System Integrity</div>
             <div className={`text-xl font-bold tracking-widest ${
                status.overallStatus === 'NOMINAL' ? 'text-emerald-400' :
                status.overallStatus === 'WARNING' ? 'text-yellow-400' : 'text-red-400'
             }`}>
                {status.overallStatus}
             </div>
          </div>
          <button
             onClick={loadDashboard}
             className="p-2 text-cyan-500/50 hover:text-cyan-400 hover:bg-cyan-500/10 rounded-full transition-all"
             title="Refresh Data"
          >
            <RefreshCw className="w-5 h-5" />
          </button>
        </div>
      </div>

      {/* Primary Metrics Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        <StatsCard
          label="Docking Bays"
          value={`${status.docking.availableBays}/${status.docking.totalBays}`}
          trend={`${status.docking.incomingShips} INBOUND`}
          trendUp={true}
          icon={Anchor}
        />
        <StatsCard
          label="Crew Complement"
          value={status.crew.totalCrew}
          trend={`${status.crew.activeCrew} ON DUTY`}
          trendUp={true}
          icon={Users}
        />
        <StatsCard
          label="Life Support"
          value={`${status.lifeSupport.sectionsNominal}/${status.lifeSupport.totalSections}`}
          trend={`${status.lifeSupport.activeAlerts} ALERTS`}
          trendUp={status.lifeSupport.activeAlerts === 0}
          icon={ThermometerSun}
        />
        <StatsCard
          label="Power Grid"
          value={`${Math.round(status.power.utilizationPercent)}%`}
          trend={`${Math.round(status.power.availableKw)} KW AVAIL`}
          trendUp={status.power.utilizationPercent < 90}
          icon={Zap}
        />
      </div>

      {/* Detailed Diagnostics Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        
        {/* Docking Diagnostics */}
        <Card title="Docking Bay Diagnostics" subtitle="Traffic Control & Bay Status" className="h-full">
          <div className="grid grid-cols-2 gap-4">
            <div className="space-y-4">
               {/* Visual representation of bays could go here later */}
               <div className="p-4 rounded bg-space-950/50 border border-space-800">
                  <div className="text-xs text-cyan-500/50 uppercase mb-1">Available Bays</div>
                  <div className="text-2xl text-emerald-400 font-mono">{status.docking.availableBays}</div>
               </div>
               <div className="p-4 rounded bg-space-950/50 border border-space-800">
                  <div className="text-xs text-cyan-500/50 uppercase mb-1">Occupied Bays</div>
                  <div className="text-2xl text-yellow-400 font-mono">{status.docking.occupiedBays}</div>
               </div>
            </div>
            <div className="space-y-4">
               <div className="p-4 rounded bg-space-950/50 border border-space-800">
                  <div className="text-xs text-cyan-500/50 uppercase mb-1">Reserved</div>
                  <div className="text-2xl text-cyan-400 font-mono">{status.docking.reservedBays}</div>
               </div>
               <div className="p-4 rounded bg-space-950/50 border border-space-800">
                  <div className="text-xs text-cyan-500/50 uppercase mb-1">Docked Ships</div>
                  <div className="text-2xl text-white font-mono">{status.docking.dockedShips}</div>
               </div>
            </div>
          </div>
        </Card>

        {/* Power Grid Analysis */}
        <Card title="Power Grid Analysis" subtitle="Reactor Output & Distribution" className="h-full">
           <div className="space-y-6">
              <div>
                 <div className="flex justify-between text-xs font-mono mb-2 uppercase tracking-widest text-cyan-500/70">
                    <span>Grid Load</span>
                    <span className={status.power.utilizationPercent > 90 ? 'text-red-400' : 'text-cyan-400'}>
                       {Math.round(status.power.utilizationPercent)}%
                    </span>
                 </div>
                 <div className="h-2 bg-space-950 rounded-full overflow-hidden border border-space-800">
                    <motion.div 
                       initial={{ width: 0 }}
                       animate={{ width: `${status.power.utilizationPercent}%` }}
                       transition={{ duration: 1, ease: "easeOut" }}
                       className={`h-full rounded-full ${
                          status.power.utilizationPercent > 90 ? 'bg-red-500 shadow-[0_0_10px_#ef4444]' :
                          status.power.utilizationPercent > 75 ? 'bg-yellow-500 shadow-[0_0_10px_#eab308]' :
                          'bg-cyan-500 shadow-[0_0_10px_#06b6d4]'
                       }`} 
                    />
                 </div>
              </div>

              <div className="grid grid-cols-2 gap-4">
                 <div className="flex flex-col justify-between p-3 rounded border border-space-800 bg-space-900/30">
                    <span className="text-xs text-cyan-500/50 font-mono uppercase">Total Output</span>
                    <span className="text-xl text-white font-mono mt-1">{Math.round(status.power.totalOutputKw)} <span className="text-xs text-cyan-500/50">kW</span></span>
                 </div>
                 <div className="flex flex-col justify-between p-3 rounded border border-space-800 bg-space-900/30">
                    <span className="text-xs text-cyan-500/50 font-mono uppercase">Allocated</span>
                    <span className="text-xl text-white font-mono mt-1">{Math.round(status.power.totalAllocatedKw)} <span className="text-xs text-cyan-500/50">kW</span></span>
                 </div>
              </div>
           </div>
        </Card>

        {/* Life Support Matrices */}
        <Card title="Life Support Matrix" subtitle="Environmental Controls" className="h-full">
           <div className="grid grid-cols-3 gap-2 mb-6">
              <div className="p-3 bg-emerald-500/10 border border-emerald-500/30 rounded text-center">
                 <div className="text-2xl font-bold text-emerald-400 font-mono">{status.lifeSupport.sectionsNominal}</div>
                 <div className="text-[10px] text-emerald-500/70 uppercase tracking-wider mt-1">Nominal</div>
              </div>
              <div className="p-3 bg-yellow-500/10 border border-yellow-500/30 rounded text-center">
                 <div className="text-2xl font-bold text-yellow-400 font-mono">{status.lifeSupport.sectionsWarning}</div>
                 <div className="text-[10px] text-yellow-500/70 uppercase tracking-wider mt-1">Warning</div>
              </div>
              <div className="p-3 bg-red-500/10 border border-red-500/30 rounded text-center">
                 <div className="text-2xl font-bold text-red-400 font-mono">{status.lifeSupport.sectionsCritical}</div>
                 <div className="text-[10px] text-red-500/70 uppercase tracking-wider mt-1">Critical</div>
              </div>
           </div>
           
           <div className="grid grid-cols-2 gap-4">
              <div className="flex items-center justify-between p-2 border-b border-space-800">
                 <span className="text-sm text-cyan-500/70">Avg Oxygen Levels</span>
                 <span className="font-mono text-cyan-100">{status.lifeSupport.averageO2Level.toFixed(1)}%</span>
              </div>
              <div className="flex items-center justify-between p-2 border-b border-space-800">
                 <span className="text-sm text-cyan-500/70">Avg Temperature</span>
                 <span className="font-mono text-cyan-100">{status.lifeSupport.averageTemperature.toFixed(1)}°C</span>
              </div>
           </div>
        </Card>

        {/* Inventory Logistics */}
        <Card title="Logistics & Supply" subtitle="Inventory Management" className="h-full">
           <div className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                 <div className="p-4 bg-space-950/30 rounded border border-space-800 flex flex-col items-center justify-center">
                    <Package className="w-6 h-6 text-cyan-500/50 mb-2" />
                    <div className="text-2xl font-bold text-white font-mono">{status.inventory.totalItems}</div>
                    <div className="text-[10px] text-cyan-500/50 uppercase tracking-widest mt-1">Total Items</div>
                 </div>
                 
                 <div className={`p-4 rounded border flex flex-col items-center justify-center ${
                    status.inventory.lowStockItems > 0 
                       ? 'bg-red-500/10 border-red-500/30' 
                       : 'bg-space-950/30 border-space-800'
                 }`}>
                    <AlertTriangle className={`w-6 h-6 mb-2 ${status.inventory.lowStockItems > 0 ? 'text-red-400' : 'text-cyan-500/50'}`} />
                    <div className={`text-2xl font-bold font-mono ${status.inventory.lowStockItems > 0 ? 'text-red-400' : 'text-white'}`}>
                       {status.inventory.lowStockItems}
                    </div>
                    <div className={`text-[10px] uppercase tracking-widest mt-1 ${status.inventory.lowStockItems > 0 ? 'text-red-400/70' : 'text-cyan-500/50'}`}>
                       Low Stock
                    </div>
                 </div>
              </div>

              <div className="grid grid-cols-2 gap-4 mt-2">
                 <div className="flex justify-between items-center p-2 bg-space-900/20 rounded">
                    <span className="text-xs text-cyan-500/70 uppercase">Pending Resupply</span>
                    <span className="font-mono font-bold text-cyan-100">{status.inventory.pendingResupply}</span>
                 </div>
                 <div className="flex justify-between items-center p-2 bg-space-900/20 rounded">
                    <span className="text-xs text-cyan-500/70 uppercase">Incoming Manifests</span>
                    <span className="font-mono font-bold text-cyan-100">{status.inventory.pendingManifests}</span>
                 </div>
              </div>
           </div>
        </Card>

      </div>
    </div>
  );
}

export default Dashboard;
