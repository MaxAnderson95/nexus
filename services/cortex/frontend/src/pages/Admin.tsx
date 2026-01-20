import { useState } from 'react';
import { api, extractErrorInfo } from '../api/client';
import type { ResetAllTablesResponse } from '../types';
import { Card } from '../components/ui/Card';
import { useErrorToast } from '../context/ErrorToastContext';
import {
  RefreshCw,
  AlertTriangle,
  CheckCircle2,
  XCircle,
  Database,
  Loader2
} from 'lucide-react';
import { motion, AnimatePresence } from 'framer-motion';

function Admin() {
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState<ResetAllTablesResponse | null>(null);
  const [showConfirmModal, setShowConfirmModal] = useState(false);
  const { showError } = useErrorToast();

  async function handleResetDemoData() {
    try {
      setLoading(true);
      setResult(null);
      setShowConfirmModal(false);
      const response = await api.admin.resetAllTables();
      setResult(response);
    } catch (err) {
      showError(extractErrorInfo(err, 'Failed to reset demo data'));
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col md:flex-row md:items-end justify-between gap-4">
        <div>
          <h2 className="text-3xl font-light text-white uppercase tracking-wider">
            System <span className="text-cyan-400 font-bold">Administration</span>
          </h2>
          <div className="flex items-center gap-2 mt-1 text-cyan-500/60 font-mono text-xs">
            <span className="w-2 h-2 rounded-full bg-cyan-500/50 animate-pulse" />
            MAINTENANCE OPERATIONS
          </div>
        </div>
      </div>



      {/* Demo Data Reset Card */}
      <Card 
        title="Demo Data Management" 
        subtitle="Reset all service data to initial demo state"
        className="relative overflow-hidden"
      >
        <div className="absolute bottom-0 right-0 p-4 opacity-5 pointer-events-none">
          <Database className="w-24 h-24" />
        </div>
        
        <div className="relative z-10 space-y-6">
          <div className="p-4 bg-space-950/30 rounded border border-space-800">
            <div className="flex items-start gap-4">
              <div className="p-3 bg-amber-500/10 rounded-full">
                <RefreshCw className="w-6 h-6 text-amber-400" />
              </div>
              <div className="flex-1">
                <h4 className="text-lg font-bold text-white mb-2">Reset Demo Data</h4>
                <p className="text-cyan-500/70 text-sm mb-4">
                  This will clear all data from every service and reinitialize with fresh demo data. 
                  All changes made during your session will be lost.
                </p>
                <div className="text-xs text-cyan-500/50 font-mono uppercase tracking-wider mb-4">
                  Affected Services: Power, Life Support, Crew, Docking, Inventory
                </div>
                <button
                  onClick={() => setShowConfirmModal(true)}
                  disabled={loading}
                  className="px-6 py-3 bg-amber-500/20 hover:bg-amber-500/30 text-amber-400 border border-amber-500/50 rounded flex items-center gap-2 transition-all font-mono text-sm uppercase tracking-wider disabled:opacity-50"
                >
                  {loading ? (
                    <>
                      <Loader2 className="w-4 h-4 animate-spin" />
                      Resetting...
                    </>
                  ) : (
                    <>
                      <RefreshCw className="w-4 h-4" />
                      Reset Demo Data
                    </>
                  )}
                </button>
              </div>
            </div>
          </div>

          {/* Results Display */}
          <AnimatePresence>
            {result && (
              <motion.div
                initial={{ opacity: 0, y: -10 }}
                animate={{ opacity: 1, y: 0 }}
                exit={{ opacity: 0, y: -10 }}
                className="space-y-4"
              >
                <div className={`p-4 rounded border flex items-center gap-3 ${
                  result.status === 'success' 
                    ? 'bg-emerald-500/10 border-emerald-500/30 text-emerald-400'
                    : 'bg-amber-500/10 border-amber-500/30 text-amber-400'
                }`}>
                  {result.status === 'success' ? (
                    <CheckCircle2 className="w-5 h-5" />
                  ) : (
                    <AlertTriangle className="w-5 h-5" />
                  )}
                  <span>{result.message}</span>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-3">
                  {result.results.map((serviceResult) => (
                    <div
                      key={serviceResult.service}
                      className={`p-3 rounded border flex items-center gap-3 ${
                        serviceResult.status === 'success'
                          ? 'bg-space-950/30 border-emerald-500/20'
                          : 'bg-red-500/10 border-red-500/30'
                      }`}
                    >
                      {serviceResult.status === 'success' ? (
                        <CheckCircle2 className="w-4 h-4 text-emerald-400" />
                      ) : (
                        <XCircle className="w-4 h-4 text-red-400" />
                      )}
                      <div className="flex-1">
                        <div className="font-mono text-sm text-white uppercase">
                          {serviceResult.service}
                        </div>
                        <div className={`text-xs ${
                          serviceResult.status === 'success' 
                            ? 'text-emerald-400/70' 
                            : 'text-red-400/70'
                        }`}>
                          {serviceResult.status === 'success' ? 'Reset Complete' : serviceResult.message}
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              </motion.div>
            )}
          </AnimatePresence>
        </div>
      </Card>

      {/* System Information Card */}
      <Card title="System Information" subtitle="Current environment details">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div className="p-4 bg-space-950/30 rounded border border-space-800">
            <div className="text-xs text-cyan-500/50 font-mono uppercase tracking-wider mb-1">
              Environment
            </div>
            <div className="text-lg text-white font-mono">Development</div>
          </div>
          <div className="p-4 bg-space-950/30 rounded border border-space-800">
            <div className="text-xs text-cyan-500/50 font-mono uppercase tracking-wider mb-1">
              Database
            </div>
            <div className="text-lg text-white font-mono">PostgreSQL 16</div>
          </div>
          <div className="p-4 bg-space-950/30 rounded border border-space-800">
            <div className="text-xs text-cyan-500/50 font-mono uppercase tracking-wider mb-1">
              Cache
            </div>
            <div className="text-lg text-white font-mono">Redis 7</div>
          </div>
          <div className="p-4 bg-space-950/30 rounded border border-space-800">
            <div className="text-xs text-cyan-500/50 font-mono uppercase tracking-wider mb-1">
              Storage Mode
            </div>
            <div className="text-lg text-white font-mono">Persistent Volume</div>
          </div>
        </div>
      </Card>

      {/* Confirmation Modal */}
      <AnimatePresence>
        {showConfirmModal && (
          <div className="fixed inset-0 bg-space-950/80 backdrop-blur-sm flex items-center justify-center z-50 p-4">
            <motion.div 
              initial={{ opacity: 0, scale: 0.9 }}
              animate={{ opacity: 1, scale: 1 }}
              exit={{ opacity: 0, scale: 0.9 }}
              className="bg-space-900 border border-space-700 rounded-lg max-w-md w-full shadow-[0_0_50px_rgba(0,0,0,0.5)] overflow-hidden"
            >
              <div className="p-4 border-b border-space-700 bg-amber-500/5">
                <h3 className="text-lg font-bold text-amber-400 flex items-center gap-2">
                  <AlertTriangle className="w-5 h-5" />
                  Confirm Reset
                </h3>
              </div>
              
              <div className="p-6">
                <p className="text-cyan-100 mb-4">
                  Are you sure you want to reset all demo data? This action will:
                </p>
                <ul className="text-cyan-500/70 text-sm space-y-2 mb-6">
                  <li className="flex items-center gap-2">
                    <span className="w-1.5 h-1.5 bg-amber-400 rounded-full" />
                    Clear all tables in every microservice
                  </li>
                  <li className="flex items-center gap-2">
                    <span className="w-1.5 h-1.5 bg-amber-400 rounded-full" />
                    Restore initial demo data
                  </li>
                  <li className="flex items-center gap-2">
                    <span className="w-1.5 h-1.5 bg-amber-400 rounded-full" />
                    Remove all session changes
                  </li>
                </ul>
                <p className="text-amber-400/70 text-sm font-mono">
                  This cannot be undone.
                </p>
              </div>

              <div className="p-4 border-t border-space-700 bg-space-950/50 flex gap-3">
                <button
                  onClick={() => setShowConfirmModal(false)}
                  className="flex-1 py-2 text-cyan-500/70 hover:text-cyan-400 font-mono text-sm uppercase tracking-wider"
                >
                  Cancel
                </button>
                <button
                  onClick={handleResetDemoData}
                  className="flex-1 py-2 bg-amber-500/20 hover:bg-amber-500/30 text-amber-400 border border-amber-500/50 rounded transition-all font-mono text-sm uppercase tracking-wider flex items-center justify-center gap-2"
                >
                  <RefreshCw className="w-4 h-4" />
                  Confirm Reset
                </button>
              </div>
            </motion.div>
          </div>
        )}
      </AnimatePresence>
    </div>
  );
}

export default Admin;
