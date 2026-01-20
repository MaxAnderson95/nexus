import { useState, useEffect, useCallback } from 'react';
import { Link } from 'react-router-dom';
import { api, extractErrorInfo } from '../api/client';
import type { DockingSummary, CrewSummary, LifeSupportSummary, PowerSummary, InventorySummary } from '../types';
import { Card, StatsCard } from '../components/ui/Card';
import type { ErrorInfo } from '../components/ui/ErrorAlert';
import { useErrorToast } from '../context/ErrorToastContext';
import {
  Users,
  Anchor,
  ThermometerSun,
  Zap,
  Package,
  AlertTriangle,
  RefreshCw,
  Loader2
} from 'lucide-react';
import { motion } from 'framer-motion';

// Service state type for independent loading
interface ServiceState<T> {
  data: T | null;
  loading: boolean;
  error: ErrorInfo | null;
}

function Dashboard() {
  const [docking, setDocking] = useState<ServiceState<DockingSummary>>({ data: null, loading: true, error: null });
  const [crew, setCrew] = useState<ServiceState<CrewSummary>>({ data: null, loading: true, error: null });
  const [lifeSupport, setLifeSupport] = useState<ServiceState<LifeSupportSummary>>({ data: null, loading: true, error: null });
  const [power, setPower] = useState<ServiceState<PowerSummary>>({ data: null, loading: true, error: null });
  const [inventory, setInventory] = useState<ServiceState<InventorySummary>>({ data: null, loading: true, error: null });
  const [lastSync, setLastSync] = useState<Date | null>(null);
  const { showError } = useErrorToast();

  // Individual service loaders
  const loadDocking = useCallback(async (init = true) => {
    try {
      if (init) setDocking(prev => ({ ...prev, loading: true }));
      const data = await api.dashboard.getDockingSummary();
      setDocking({ data, loading: false, error: null });
    } catch (err) {
      const errorInfo = extractErrorInfo(err, 'Failed to load docking data');
      setDocking(prev => ({
        data: prev.data,
        loading: false,
        error: errorInfo
      }));
      if (init) showError(errorInfo);
    }
  }, [showError]);

  const loadCrew = useCallback(async (init = true) => {
    try {
      if (init) setCrew(prev => ({ ...prev, loading: true }));
      const data = await api.dashboard.getCrewSummary();
      setCrew({ data, loading: false, error: null });
    } catch (err) {
      const errorInfo = extractErrorInfo(err, 'Failed to load crew data');
      setCrew(prev => ({
        data: prev.data,
        loading: false,
        error: errorInfo
      }));
      if (init) showError(errorInfo);
    }
  }, [showError]);

  const loadLifeSupport = useCallback(async (init = true) => {
    try {
      if (init) setLifeSupport(prev => ({ ...prev, loading: true }));
      const data = await api.dashboard.getLifeSupportSummary();
      setLifeSupport({ data, loading: false, error: null });
    } catch (err) {
      const errorInfo = extractErrorInfo(err, 'Failed to load life support data');
      setLifeSupport(prev => ({
        data: prev.data,
        loading: false,
        error: errorInfo
      }));
      if (init) showError(errorInfo);
    }
  }, [showError]);

  const loadPower = useCallback(async (init = true) => {
    try {
      if (init) setPower(prev => ({ ...prev, loading: true }));
      const data = await api.dashboard.getPowerSummary();
      setPower({ data, loading: false, error: null });
    } catch (err) {
      const errorInfo = extractErrorInfo(err, 'Failed to load power data');
      setPower(prev => ({
        data: prev.data,
        loading: false,
        error: errorInfo
      }));
      if (init) showError(errorInfo);
    }
  }, [showError]);

  const loadInventory = useCallback(async (init = true) => {
    try {
      if (init) setInventory(prev => ({ ...prev, loading: true }));
      const data = await api.dashboard.getInventorySummary();
      setInventory({ data, loading: false, error: null });
    } catch (err) {
      const errorInfo = extractErrorInfo(err, 'Failed to load inventory data');
      setInventory(prev => ({
        data: prev.data,
        loading: false,
        error: errorInfo
      }));
      if (init) showError(errorInfo);
    }
  }, [showError]);

  const loadAll = useCallback((init = true) => {
    // Fire all requests in parallel - each will update independently
    loadDocking(init);
    loadCrew(init);
    loadLifeSupport(init);
    loadPower(init);
    loadInventory(init);
    setLastSync(new Date());
  }, [loadDocking, loadCrew, loadLifeSupport, loadPower, loadInventory]);

  // Hard refresh: clear all state first, then fetch fresh data
  // This ensures stale data is not shown during chaos/failures
  const refreshAll = useCallback(() => {
    // Wipe all existing data
    setDocking({ data: null, loading: true, error: null });
    setCrew({ data: null, loading: true, error: null });
    setLifeSupport({ data: null, loading: true, error: null });
    setPower({ data: null, loading: true, error: null });
    setInventory({ data: null, loading: true, error: null });
    // Fetch fresh data
    loadDocking(false);
    loadCrew(false);
    loadLifeSupport(false);
    loadPower(false);
    loadInventory(false);
    setLastSync(new Date());
  }, [loadDocking, loadCrew, loadLifeSupport, loadPower, loadInventory]);

  useEffect(() => {
    loadAll(true);
    const interval = setInterval(() => loadAll(false), 15000);
    return () => clearInterval(interval);
  }, [loadAll]);

  // Compute overall status based on available data
  const computeOverallStatus = (): 'NOMINAL' | 'WARNING' | 'CRITICAL' | 'UNKNOWN' => {
    const hasAnyError = docking.error || crew.error || lifeSupport.error || power.error || inventory.error;
    const allLoading = docking.loading && crew.loading && lifeSupport.loading && power.loading && inventory.loading;

    if (allLoading) return 'UNKNOWN';
    if (hasAnyError) return 'WARNING';

    // Check for critical conditions
    if (lifeSupport.data && lifeSupport.data.sectionsCritical > 0) return 'CRITICAL';
    if (power.data && power.data.utilizationPercent > 95) return 'CRITICAL';

    // Check for warning conditions
    if (lifeSupport.data && lifeSupport.data.sectionsWarning > 0) return 'WARNING';
    if (power.data && power.data.utilizationPercent > 85) return 'WARNING';
    if (inventory.data && inventory.data.lowStockItems > 5) return 'WARNING';

    return 'NOMINAL';
  };

  const overallStatus = computeOverallStatus();

  // Loading skeleton component
  const LoadingSkeleton = ({ className = '' }: { className?: string }) => (
    <div className={`animate-pulse flex items-center justify-center ${className}`}>
      <Loader2 className="w-6 h-6 text-cyan-500/50 animate-spin" />
    </div>
  );

  // Error card component for service sections
  const ServiceError = ({ onRetry, serviceName }: { error: ErrorInfo; onRetry: () => void; serviceName: string }) => (
    <div className="h-full flex flex-col items-center justify-center p-6 text-center">
      <AlertTriangle className="w-8 h-8 text-red-500/70 mb-3" />
      <div className="text-sm text-red-400 mb-4">{serviceName} Offline</div>
      <button
        onClick={onRetry}
        className="px-3 py-1 bg-red-500/20 hover:bg-red-500/30 text-red-400 border border-red-500/50 rounded transition-all font-mono text-xs uppercase tracking-wider flex items-center gap-2"
      >
        <RefreshCw className="w-3 h-3" />
        Retry
      </button>
    </div>
  );

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
            {lastSync ? `LAST SYNC: ${lastSync.toLocaleTimeString()}` : 'SYNCING...'}
          </div>
        </div>

        <div className="flex items-center gap-4">
          <div className="text-right">
             <div className="text-xs font-mono text-cyan-500/50 uppercase tracking-widest">System Integrity</div>
             <div className={`text-xl font-bold tracking-widest ${
                overallStatus === 'NOMINAL' ? 'text-emerald-400' :
                overallStatus === 'WARNING' ? 'text-yellow-400' :
                overallStatus === 'CRITICAL' ? 'text-red-400' : 'text-cyan-500/50'
             }`}>
                {overallStatus}
             </div>
          </div>
          <button
             onClick={refreshAll}
             className="p-2 text-cyan-500/50 hover:text-cyan-400 hover:bg-cyan-500/10 rounded-full transition-all"
             title="Refresh All Data"
          >
            <RefreshCw className="w-5 h-5" />
          </button>
        </div>
      </div>

      {/* Primary Metrics Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        <Link to="/docking">
          {docking.loading && !docking.data ? (
            <StatsCard label="Docking Bays" value="--" trend="LOADING" trendUp={true} icon={Anchor} />
          ) : docking.error && !docking.data ? (
            <StatsCard label="Docking Bays" value="ERR" trend="OFFLINE" trendUp={false} icon={Anchor} />
          ) : docking.data ? (
            <StatsCard
              label="Docking Bays"
              value={`${docking.data.availableBays}/${docking.data.totalBays}`}
              trend={`${docking.data.incomingShips} INBOUND`}
              trendUp={true}
              icon={Anchor}
            />
          ) : null}
        </Link>
        <Link to="/crew">
          {crew.loading && !crew.data ? (
            <StatsCard label="Crew Complement" value="--" trend="LOADING" trendUp={true} icon={Users} />
          ) : crew.error && !crew.data ? (
            <StatsCard label="Crew Complement" value="ERR" trend="OFFLINE" trendUp={false} icon={Users} />
          ) : crew.data ? (
            <StatsCard
              label="Crew Complement"
              value={crew.data.totalCrew}
              trend={`${crew.data.activeCrew} ON DUTY`}
              trendUp={true}
              icon={Users}
            />
          ) : null}
        </Link>
        <Link to="/life-support">
          {lifeSupport.loading && !lifeSupport.data ? (
            <StatsCard label="Life Support" value="--" trend="LOADING" trendUp={true} icon={ThermometerSun} />
          ) : lifeSupport.error && !lifeSupport.data ? (
            <StatsCard label="Life Support" value="ERR" trend="OFFLINE" trendUp={false} icon={ThermometerSun} />
          ) : lifeSupport.data ? (
            <StatsCard
              label="Life Support"
              value={`${lifeSupport.data.sectionsNominal}/${lifeSupport.data.totalSections}`}
              trend={`${lifeSupport.data.activeAlerts} ALERTS`}
              trendUp={lifeSupport.data.activeAlerts === 0}
              icon={ThermometerSun}
            />
          ) : null}
        </Link>
        <Link to="/power">
          {power.loading && !power.data ? (
            <StatsCard label="Power Grid" value="--" trend="LOADING" trendUp={true} icon={Zap} />
          ) : power.error && !power.data ? (
            <StatsCard label="Power Grid" value="ERR" trend="OFFLINE" trendUp={false} icon={Zap} />
          ) : power.data ? (
            <StatsCard
              label="Power Grid"
              value={`${Math.round(power.data.utilizationPercent)}%`}
              trend={`${Math.round(power.data.availableKw)} KW AVAIL`}
              trendUp={power.data.utilizationPercent < 90}
              icon={Zap}
            />
          ) : null}
        </Link>
      </div>

      {/* Detailed Diagnostics Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">

        {/* Docking Diagnostics */}
        <Link to="/docking" className="block h-full">
          <Card title="Docking Bay Diagnostics" subtitle="Traffic Control & Bay Status" className="h-full hover:border-cyan-500/50 hover:bg-space-900/60 transition-all min-h-[200px]">
            {docking.loading && !docking.data ? (
              <LoadingSkeleton className="h-32" />
            ) : docking.error && !docking.data ? (
              <ServiceError error={docking.error} onRetry={() => loadDocking(true)} serviceName="Docking" />
            ) : docking.data ? (
              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-4">
                   <div className="p-4 rounded bg-space-950/50 border border-space-800">
                      <div className="text-xs text-cyan-500/50 uppercase mb-1">Available Bays</div>
                      <div className="text-2xl text-emerald-400 font-mono">{docking.data.availableBays}</div>
                   </div>
                   <div className="p-4 rounded bg-space-950/50 border border-space-800">
                      <div className="text-xs text-cyan-500/50 uppercase mb-1">Occupied Bays</div>
                      <div className="text-2xl text-yellow-400 font-mono">{docking.data.occupiedBays}</div>
                   </div>
                </div>
                <div className="space-y-4">
                   <div className="p-4 rounded bg-space-950/50 border border-space-800">
                      <div className="text-xs text-cyan-500/50 uppercase mb-1">Reserved</div>
                      <div className="text-2xl text-cyan-400 font-mono">{docking.data.reservedBays}</div>
                   </div>
                   <div className="p-4 rounded bg-space-950/50 border border-space-800">
                      <div className="text-xs text-cyan-500/50 uppercase mb-1">Docked Ships</div>
                      <div className="text-2xl text-white font-mono">{docking.data.dockedShips}</div>
                   </div>
                </div>
              </div>
            ) : null}
          </Card>
        </Link>

        {/* Power Grid Analysis */}
        <Link to="/power" className="block h-full">
          <Card title="Power Grid Analysis" subtitle="Reactor Output & Distribution" className="h-full hover:border-cyan-500/50 hover:bg-space-900/60 transition-all min-h-[200px]">
            {power.loading && !power.data ? (
              <LoadingSkeleton className="h-32" />
            ) : power.error && !power.data ? (
              <ServiceError error={power.error} onRetry={() => loadPower(true)} serviceName="Power" />
            ) : power.data ? (
              <div className="space-y-6">
                <div>
                   <div className="flex justify-between text-xs font-mono mb-2 uppercase tracking-widest text-cyan-500/70">
                      <span>Grid Load</span>
                      <span className={power.data.utilizationPercent > 90 ? 'text-red-400' : 'text-cyan-400'}>
                         {Math.round(power.data.utilizationPercent)}%
                      </span>
                   </div>
                   <div className="h-2 bg-space-950 rounded-full overflow-hidden border border-space-800">
                      <motion.div
                         initial={{ width: 0 }}
                         animate={{ width: `${power.data.utilizationPercent}%` }}
                         transition={{ duration: 1, ease: "easeOut" }}
                         className={`h-full rounded-full ${
                            power.data.utilizationPercent > 90 ? 'bg-red-500 shadow-[0_0_10px_#ef4444]' :
                            power.data.utilizationPercent > 75 ? 'bg-yellow-500 shadow-[0_0_10px_#eab308]' :
                            'bg-cyan-500 shadow-[0_0_10px_#06b6d4]'
                         }`}
                      />
                   </div>
                </div>

                <div className="grid grid-cols-2 gap-4">
                   <div className="flex flex-col justify-between p-3 rounded border border-space-800 bg-space-900/30">
                      <span className="text-xs text-cyan-500/50 font-mono uppercase">Total Output</span>
                      <span className="text-xl text-white font-mono mt-1">{Math.round(power.data.totalOutputKw)} <span className="text-xs text-cyan-500/50">kW</span></span>
                   </div>
                   <div className="flex flex-col justify-between p-3 rounded border border-space-800 bg-space-900/30">
                      <span className="text-xs text-cyan-500/50 font-mono uppercase">Allocated</span>
                      <span className="text-xl text-white font-mono mt-1">{Math.round(power.data.totalAllocatedKw)} <span className="text-xs text-cyan-500/50">kW</span></span>
                   </div>
                </div>
              </div>
            ) : null}
          </Card>
        </Link>

        {/* Life Support Matrices */}
        <Link to="/life-support" className="block h-full">
          <Card title="Life Support Matrix" subtitle="Environmental Controls" className="h-full hover:border-cyan-500/50 hover:bg-space-900/60 transition-all min-h-[200px]">
            {lifeSupport.loading && !lifeSupport.data ? (
              <LoadingSkeleton className="h-32" />
            ) : lifeSupport.error && !lifeSupport.data ? (
              <ServiceError error={lifeSupport.error} onRetry={() => loadLifeSupport(true)} serviceName="Life Support" />
            ) : lifeSupport.data ? (
              <>
                <div className="grid grid-cols-3 gap-2 mb-6">
                  <div className="p-3 bg-emerald-500/10 border border-emerald-500/30 rounded text-center">
                     <div className="text-2xl font-bold text-emerald-400 font-mono">{lifeSupport.data.sectionsNominal}</div>
                     <div className="text-[10px] text-emerald-500/70 uppercase tracking-wider mt-1">Nominal</div>
                  </div>
                  <div className="p-3 bg-yellow-500/10 border border-yellow-500/30 rounded text-center">
                     <div className="text-2xl font-bold text-yellow-400 font-mono">{lifeSupport.data.sectionsWarning}</div>
                     <div className="text-[10px] text-yellow-500/70 uppercase tracking-wider mt-1">Warning</div>
                  </div>
                  <div className="p-3 bg-red-500/10 border border-red-500/30 rounded text-center">
                     <div className="text-2xl font-bold text-red-400 font-mono">{lifeSupport.data.sectionsCritical}</div>
                     <div className="text-[10px] text-red-500/70 uppercase tracking-wider mt-1">Critical</div>
                  </div>
                </div>

                <div className="grid grid-cols-2 gap-4">
                  <div className="flex items-center justify-between p-2 border-b border-space-800">
                     <span className="text-sm text-cyan-500/70">Avg Oxygen Levels</span>
                     <span className="font-mono text-cyan-100">{lifeSupport.data.averageO2Level.toFixed(1)}%</span>
                  </div>
                  <div className="flex items-center justify-between p-2 border-b border-space-800">
                     <span className="text-sm text-cyan-500/70">Avg Temperature</span>
                     <span className="font-mono text-cyan-100">{lifeSupport.data.averageTemperature.toFixed(1)}°C</span>
                  </div>
                </div>
              </>
            ) : null}
          </Card>
        </Link>

        {/* Inventory Logistics */}
        <Link to="/inventory" className="block h-full">
          <Card title="Logistics & Supply" subtitle="Inventory Management" className="h-full hover:border-cyan-500/50 hover:bg-space-900/60 transition-all min-h-[200px]">
            {inventory.loading && !inventory.data ? (
              <LoadingSkeleton className="h-32" />
            ) : inventory.error && !inventory.data ? (
              <ServiceError error={inventory.error} onRetry={() => loadInventory(true)} serviceName="Inventory" />
            ) : inventory.data ? (
              <div className="space-y-4">
                <div className="grid grid-cols-2 gap-4">
                   <div className="p-4 bg-space-950/30 rounded border border-space-800 flex flex-col items-center justify-center">
                      <Package className="w-6 h-6 text-cyan-500/50 mb-2" />
                      <div className="text-2xl font-bold text-white font-mono">{inventory.data.totalItems}</div>
                      <div className="text-[10px] text-cyan-500/50 uppercase tracking-widest mt-1">Total Items</div>
                   </div>

                   <div className={`p-4 rounded border flex flex-col items-center justify-center ${
                      inventory.data.lowStockItems > 0
                         ? 'bg-red-500/10 border-red-500/30'
                         : 'bg-space-950/30 border-space-800'
                   }`}>
                      <AlertTriangle className={`w-6 h-6 mb-2 ${inventory.data.lowStockItems > 0 ? 'text-red-400' : 'text-cyan-500/50'}`} />
                      <div className={`text-2xl font-bold font-mono ${inventory.data.lowStockItems > 0 ? 'text-red-400' : 'text-white'}`}>
                         {inventory.data.lowStockItems}
                      </div>
                      <div className={`text-[10px] uppercase tracking-widest mt-1 ${inventory.data.lowStockItems > 0 ? 'text-red-400/70' : 'text-cyan-500/50'}`}>
                         Low Stock
                      </div>
                   </div>
                </div>

                <div className="grid grid-cols-2 gap-4 mt-2">
                   <div className="flex justify-between items-center p-2 bg-space-900/20 rounded">
                      <span className="text-xs text-cyan-500/70 uppercase">Pending Resupply</span>
                      <span className="font-mono font-bold text-cyan-100">{inventory.data.pendingResupply}</span>
                   </div>
                   <div className="flex justify-between items-center p-2 bg-space-900/20 rounded">
                      <span className="text-xs text-cyan-500/70 uppercase">Incoming Manifests</span>
                      <span className="font-mono font-bold text-cyan-100">{inventory.data.pendingManifests}</span>
                   </div>
                </div>
              </div>
            ) : null}
          </Card>
        </Link>

      </div>
    </div>
  );
}

export default Dashboard;
