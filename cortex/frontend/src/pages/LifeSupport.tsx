import { useState, useEffect } from 'react';
import { api } from '../api/client';
import type { EnvironmentStatus, Alert, SelfTestResult } from '../types';
import { Card } from '../components/ui/Card';
import { 
  ThermometerSun, 
  Wind, 
  Droplets, 
  Gauge, 
  AlertOctagon, 
  Activity, 
  CheckCircle2, 
  XCircle,
  Settings,
  RefreshCw,
  Bell
} from 'lucide-react';
import { motion, AnimatePresence } from 'framer-motion';

function LifeSupport() {
  const [environment, setEnvironment] = useState<EnvironmentStatus[]>([]);
  const [alerts, setAlerts] = useState<Alert[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  
  // Self-test state
  const [testingSection, setTestingSection] = useState<number | null>(null);
  const [testResult, setTestResult] = useState<SelfTestResult | null>(null);
  
  // Adjustment modal state
  const [adjustingSection, setAdjustingSection] = useState<EnvironmentStatus | null>(null);
  const [adjustTemp, setAdjustTemp] = useState<number>(22);
  const [adjustO2, setAdjustO2] = useState<number>(21);
  const [adjustLoading, setAdjustLoading] = useState(false);

  useEffect(() => {
    loadData();
  }, []);

  async function loadData(init = true) {
    try {
      if (init) setLoading(true);
      setError(null);
      const [envData, alertsData] = await Promise.all([
        api.lifeSupport.getEnvironment(),
        api.lifeSupport.getAlerts(),
      ]);
      setEnvironment(envData);
      setAlerts(alertsData);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load life support data');
    } finally {
      if (init) setLoading(false);
    }
  }

  async function handleAcknowledge(alertId: number) {
    try {
      await api.lifeSupport.acknowledgeAlert(alertId);
      await loadData(false);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to acknowledge alert');
    }
  }

  async function handleSelfTest(sectionId: number) {
    try {
      setTestingSection(sectionId);
      setTestResult(null);
      setError(null);
      const result = await api.lifeSupport.runSelfTest(sectionId);
      setTestResult(result);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Self-test failed');
    } finally {
      setTestingSection(null);
    }
  }

  function openAdjustModal(section: EnvironmentStatus) {
    setAdjustingSection(section);
    setAdjustTemp(section.targetTemperature);
    setAdjustO2(section.targetO2);
  }

  async function handleAdjust() {
    if (!adjustingSection) return;
    
    try {
      setAdjustLoading(true);
      setError(null);
      await api.lifeSupport.adjustSection(adjustingSection.sectionId, {
        targetTemperature: adjustTemp,
        targetO2: adjustO2,
      });
      setAdjustingSection(null);
      await loadData(false);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to adjust environment');
    } finally {
      setAdjustLoading(false);
    }
  }

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'NOMINAL': return 'text-emerald-400 border-emerald-500/50 shadow-[0_0_15px_rgba(16,185,129,0.2)]';
      case 'WARNING': return 'text-yellow-400 border-yellow-500/50 shadow-[0_0_15px_rgba(234,179,8,0.2)]';
      case 'CRITICAL': return 'text-red-400 border-red-500/50 shadow-[0_0_15px_rgba(239,68,68,0.2)]';
      default: return 'text-gray-400 border-gray-500/50';
    }
  };

  if (loading) {
     return (
        <div className="flex flex-col items-center justify-center h-[60vh] text-cyan-500/50 space-y-4">
           <ThermometerSun className="w-12 h-12 animate-pulse" />
           <div className="font-mono text-sm tracking-widest animate-pulse">CALIBRATING SENSORS...</div>
        </div>
     );
  }

  return (
    <div className="space-y-6">
       {/* Header */}
       <div className="flex flex-col md:flex-row md:items-end justify-between gap-4">
         <div>
            <h2 className="text-3xl font-light text-white uppercase tracking-wider">
               Life <span className="text-cyan-400 font-bold">Support</span>
            </h2>
            <div className="flex items-center gap-2 mt-1 text-cyan-500/60 font-mono text-xs">
               <span className="w-2 h-2 rounded-full bg-cyan-500/50 animate-pulse" />
               MONITORING {environment.length} SECTORS
            </div>
         </div>
         <div className="flex items-center gap-4">
            {alerts.length > 0 && (
               <div className="px-3 py-1 bg-red-500/10 border border-red-500/30 rounded text-xs font-mono text-red-400 flex items-center gap-2 animate-pulse">
                  <Bell className="w-3 h-3" />
                  {alerts.length} ACTIVE ALERTS
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

      {error && (
        <div className="bg-red-500/10 border border-red-500/30 text-red-400 p-4 rounded flex items-center gap-3">
          <AlertOctagon className="w-5 h-5" />
          <span>{error}</span>
        </div>
      )}

      {/* Test Result Display */}
      <AnimatePresence>
         {testResult && (
            <motion.div 
               initial={{ opacity: 0, y: -20 }}
               animate={{ opacity: 1, y: 0 }}
               exit={{ opacity: 0, y: -20 }}
               className={`rounded border p-4 ${testResult.passed ? 'bg-emerald-500/10 border-emerald-500/30' : 'bg-red-500/10 border-red-500/30'}`}
            >
               <div className="flex justify-between items-start mb-3">
                  <div>
                     <h3 className={`font-bold flex items-center gap-2 ${testResult.passed ? 'text-emerald-400' : 'text-red-400'}`}>
                        {testResult.passed ? <CheckCircle2 className="w-5 h-5" /> : <XCircle className="w-5 h-5" />}
                        Diagnostic Report: {testResult.sectionName}
                     </h3>
                     <div className="text-xs font-mono opacity-70 mt-1 uppercase">Duration: {testResult.durationMs}ms • Status: {testResult.overallStatus}</div>
                  </div>
                  <button onClick={() => setTestResult(null)} className="text-white/50 hover:text-white">✕</button>
               </div>
               
               <div className="grid grid-cols-2 md:grid-cols-5 gap-2">
                  {testResult.subsystems.map((sub, idx) => (
                     <div key={idx} className={`p-2 rounded text-xs font-mono border ${sub.passed ? 'bg-emerald-500/5 border-emerald-500/20 text-emerald-300' : 'bg-red-500/5 border-red-500/20 text-red-300'}`}>
                        <div className="font-bold mb-1">{sub.name}</div>
                        <div className="opacity-80">{sub.message}</div>
                     </div>
                  ))}
               </div>
            </motion.div>
         )}
      </AnimatePresence>

      {/* Active Alerts List */}
      <AnimatePresence>
         {alerts.map((alert) => (
            <motion.div
               key={alert.id}
               initial={{ opacity: 0, x: -20 }}
               animate={{ opacity: 1, x: 0 }}
               exit={{ opacity: 0, x: 20 }}
               className={`rounded border p-3 flex items-center justify-between shadow-lg backdrop-blur-sm ${
                  alert.severity === 'CRITICAL' || alert.severity === 'EMERGENCY' 
                     ? 'bg-red-500/10 border-red-500/50 text-red-400 shadow-red-900/20' 
                     : 'bg-yellow-500/10 border-yellow-500/50 text-yellow-400 shadow-yellow-900/20'
               }`}
            >
               <div className="flex items-center gap-3">
                  <AlertOctagon className={`w-5 h-5 ${['CRITICAL', 'EMERGENCY'].includes(alert.severity) ? 'animate-pulse' : ''}`} />
                  <div>
                     <span className="text-xs font-mono font-bold px-1.5 py-0.5 rounded border border-current mr-2">{alert.severity}</span>
                     <span className="font-mono text-sm tracking-wide">{alert.sectionName}: {alert.message}</span>
                  </div>
               </div>
               <button
                  onClick={() => handleAcknowledge(alert.id)}
                  className="px-3 py-1 bg-space-950/50 hover:bg-space-950 text-xs font-mono uppercase tracking-wider rounded border border-current/30 transition-all"
               >
                  Acknowledge
               </button>
            </motion.div>
         ))}
      </AnimatePresence>

      {/* Environmental Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 2xl:grid-cols-4 gap-6">
         {environment.map((section) => (
            <Card key={section.sectionId} className={`border-t-4 transition-all duration-300 ${getStatusColor(section.status).split(' ')[1]}`}>
               <div className="flex justify-between items-start mb-4 p-4 pb-0">
                  <div>
                     <h4 className="font-bold text-lg text-cyan-100 tracking-wide">{section.sectionName}</h4>
                     <div className={`text-[10px] font-mono uppercase tracking-widest px-2 py-0.5 rounded w-fit mt-1 border ${getStatusColor(section.status)}`}>
                        {section.status}
                     </div>
                  </div>
                  <div className="text-right text-[10px] font-mono text-cyan-500/50">
                     <div>OCCUPANCY</div>
                     <div className="text-base text-cyan-100">{section.currentOccupancy}/{section.maxOccupancy}</div>
                  </div>
               </div>

               <div className="grid grid-cols-2 gap-px bg-space-800/50 border-y border-space-800/50">
                  <div className="bg-space-900/50 p-3 flex flex-col items-center justify-center text-center">
                     <Wind className="w-4 h-4 text-cyan-500/50 mb-1" />
                     <div className={`text-xl font-mono ${section.o2Level < 20 ? 'text-red-400 animate-pulse' : 'text-cyan-100'}`}>
                        {section.o2Level.toFixed(1)}%
                     </div>
                     <div className="text-[10px] text-cyan-500/30 uppercase">Oxygen</div>
                  </div>
                  <div className="bg-space-900/50 p-3 flex flex-col items-center justify-center text-center">
                     <ThermometerSun className="w-4 h-4 text-cyan-500/50 mb-1" />
                     <div className="text-xl font-mono text-cyan-100">
                        {section.temperature.toFixed(1)}°C
                     </div>
                     <div className="text-[10px] text-cyan-500/30 uppercase">Temp</div>
                  </div>
                  <div className="bg-space-900/50 p-3 flex flex-col items-center justify-center text-center">
                     <Gauge className="w-4 h-4 text-cyan-500/50 mb-1" />
                     <div className="text-xl font-mono text-cyan-100">
                        {section.pressure.toFixed(1)} <span className="text-[10px]">kPa</span>
                     </div>
                     <div className="text-[10px] text-cyan-500/30 uppercase">Pressure</div>
                  </div>
                  <div className="bg-space-900/50 p-3 flex flex-col items-center justify-center text-center">
                     <Droplets className="w-4 h-4 text-cyan-500/50 mb-1" />
                     <div className="text-xl font-mono text-cyan-100">
                        {section.humidity.toFixed(1)}%
                     </div>
                     <div className="text-[10px] text-cyan-500/30 uppercase">Humidity</div>
                  </div>
               </div>

               <div className="p-4 flex gap-2">
                  <button
                     onClick={() => handleSelfTest(section.sectionId)}
                     disabled={testingSection !== null}
                     className="flex-1 py-1.5 flex items-center justify-center gap-2 rounded border border-cyan-500/30 bg-cyan-500/10 text-cyan-400 hover:bg-cyan-500/20 text-xs font-mono uppercase tracking-wider transition-all disabled:opacity-50 disabled:cursor-not-allowed"
                  >
                     {testingSection === section.sectionId ? (
                        <div className="w-3 h-3 border-2 border-cyan-400/30 border-t-cyan-400 rounded-full animate-spin" />
                     ) : (
                        <Activity className="w-3 h-3" />
                     )}
                     Test
                  </button>
                  <button
                     onClick={() => openAdjustModal(section)}
                     className="flex-1 py-1.5 flex items-center justify-center gap-2 rounded border border-purple-500/30 bg-purple-500/10 text-purple-400 hover:bg-purple-500/20 text-xs font-mono uppercase tracking-wider transition-all"
                  >
                     <Settings className="w-3 h-3" />
                     Adjust
                  </button>
               </div>
            </Card>
         ))}
      </div>

      {/* Adjustment Modal */}
      <AnimatePresence>
         {adjustingSection && (
            <div className="fixed inset-0 bg-space-950/80 backdrop-blur-sm flex items-center justify-center z-50 p-4">
               <motion.div 
                  initial={{ opacity: 0, scale: 0.9 }}
                  animate={{ opacity: 1, scale: 1 }}
                  exit={{ opacity: 0, scale: 0.9 }}
                  className="bg-space-900 border border-space-700 rounded-lg max-w-sm w-full shadow-[0_0_50px_rgba(0,0,0,0.5)] overflow-hidden"
               >
                  <div className="p-4 border-b border-space-700 bg-space-950/50">
                     <h3 className="text-lg font-bold text-white flex items-center gap-2">
                        <Settings className="w-5 h-5 text-purple-400" />
                        Adjust Parameters
                     </h3>
                     <div className="text-xs text-cyan-500/50 font-mono uppercase mt-1">
                        Sector: {adjustingSection.sectionName}
                     </div>
                  </div>
                  
                  <div className="p-6 space-y-6">
                     <div className="space-y-4">
                        <div className="space-y-2">
                           <div className="flex justify-between text-xs text-cyan-500/70 font-mono uppercase">
                              <span>Target Temperature</span>
                              <span className="text-cyan-100">{adjustTemp}°C</span>
                           </div>
                           <input
                              type="range"
                              min="16"
                              max="28"
                              step="0.5"
                              value={adjustTemp}
                              onChange={(e) => setAdjustTemp(parseFloat(e.target.value))}
                              className="w-full accent-purple-500 h-1 bg-space-800 rounded-lg appearance-none cursor-pointer"
                           />
                           <div className="flex justify-between text-[10px] text-cyan-500/30 font-mono uppercase">
                              <span>Min (16°C)</span>
                              <span>Max (28°C)</span>
                           </div>
                        </div>

                        <div className="space-y-2">
                           <div className="flex justify-between text-xs text-cyan-500/70 font-mono uppercase">
                              <span>Target O2 Level</span>
                              <span className="text-cyan-100">{adjustO2}%</span>
                           </div>
                           <input
                              type="range"
                              min="19"
                              max="23"
                              step="0.1"
                              value={adjustO2}
                              onChange={(e) => setAdjustO2(parseFloat(e.target.value))}
                              className="w-full accent-cyan-500 h-1 bg-space-800 rounded-lg appearance-none cursor-pointer"
                           />
                           <div className="flex justify-between text-[10px] text-cyan-500/30 font-mono uppercase">
                              <span>Min (19%)</span>
                              <span>Max (23%)</span>
                           </div>
                        </div>
                     </div>
                  </div>

                  <div className="p-4 border-t border-space-700 bg-space-950/50 flex gap-3">
                     <button
                        onClick={() => setAdjustingSection(null)}
                        className="flex-1 py-2 text-cyan-500/70 hover:text-cyan-400 font-mono text-sm uppercase tracking-wider"
                     >
                        Cancel
                     </button>
                     <button
                        onClick={handleAdjust}
                        disabled={adjustLoading}
                        className="flex-1 py-2 bg-purple-500/20 hover:bg-purple-500/30 text-purple-400 border border-purple-500/50 rounded transition-all font-mono text-sm uppercase tracking-wider flex items-center justify-center gap-2 disabled:opacity-50"
                     >
                        {adjustLoading ? 'Calibrating...' : 'Confirm'}
                     </button>
                  </div>
               </motion.div>
            </div>
         )}
      </AnimatePresence>
    </div>
  );
}

export default LifeSupport;
