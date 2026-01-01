import { useState, useEffect } from 'react';
import { api } from '../api/client';
import type { EnvironmentStatus, Alert } from '../types';

function LifeSupport() {
  const [environment, setEnvironment] = useState<EnvironmentStatus[]>([]);
  const [alerts, setAlerts] = useState<Alert[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    loadData();
  }, []);

  async function loadData() {
    try {
      setLoading(true);
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
      setLoading(false);
    }
  }

  async function handleAcknowledge(alertId: number) {
    try {
      await api.lifeSupport.acknowledgeAlert(alertId);
      await loadData();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to acknowledge alert');
    }
  }

  const statusColors = {
    NOMINAL: 'border-green-500 bg-green-900/20',
    WARNING: 'border-yellow-500 bg-yellow-900/20',
    CRITICAL: 'border-red-500 bg-red-900/20',
  };

  const severityColors = {
    INFO: 'bg-blue-900/50 border-blue-500',
    WARNING: 'bg-yellow-900/50 border-yellow-500',
    CRITICAL: 'bg-red-900/50 border-red-500',
    EMERGENCY: 'bg-red-900/80 border-red-400',
  };

  if (loading) {
    return <div className="text-gray-400 text-center py-8">Loading life support data...</div>;
  }

  return (
    <div className="space-y-6">
      <h2 className="text-2xl font-bold text-white">Life Support Systems</h2>

      {error && (
        <div className="bg-red-900/50 border border-red-500 rounded-lg p-4">
          <p className="text-red-300 text-sm">{error}</p>
        </div>
      )}

      {/* Alerts */}
      {alerts.length > 0 && (
        <div>
          <h3 className="text-lg font-semibold text-white mb-4">Active Alerts ({alerts.length})</h3>
          <div className="space-y-2">
            {alerts.map((alert) => (
              <div
                key={alert.id}
                className={`rounded-lg border p-3 flex items-center justify-between ${severityColors[alert.severity]}`}
              >
                <div>
                  <span className="text-sm font-medium">[{alert.severity}]</span>
                  <span className="ml-2 text-sm">{alert.sectionName}:</span>
                  <span className="ml-2">{alert.message}</span>
                </div>
                <button
                  onClick={() => handleAcknowledge(alert.id)}
                  className="px-3 py-1 bg-gray-600 hover:bg-gray-500 rounded text-sm"
                >
                  Acknowledge
                </button>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Environment Status */}
      <div>
        <h3 className="text-lg font-semibold text-white mb-4">Section Environment</h3>
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
          {environment.map((section) => (
            <div
              key={section.sectionId}
              className={`rounded-lg border-2 p-4 ${statusColors[section.status]}`}
            >
              <div className="flex justify-between items-start mb-3">
                <h4 className="font-bold text-white">{section.sectionName}</h4>
                <span className={`text-xs px-2 py-1 rounded ${
                  section.status === 'NOMINAL' ? 'bg-green-600' :
                  section.status === 'WARNING' ? 'bg-yellow-600' : 'bg-red-600'
                }`}>
                  {section.status}
                </span>
              </div>
              
              <div className="space-y-2 text-sm">
                <div className="flex justify-between">
                  <span className="text-gray-400">O2 Level</span>
                  <span className={section.o2Level < 20 ? 'text-red-400' : 'text-white'}>
                    {section.o2Level.toFixed(1)}%
                  </span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-400">Temperature</span>
                  <span className="text-white">{section.temperature.toFixed(1)}°C</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-400">Pressure</span>
                  <span className="text-white">{section.pressure.toFixed(1)} kPa</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-400">Humidity</span>
                  <span className="text-white">{section.humidity.toFixed(1)}%</span>
                </div>
                <div className="flex justify-between border-t border-gray-600 pt-2 mt-2">
                  <span className="text-gray-400">Occupancy</span>
                  <span className="text-white">{section.currentOccupancy}/{section.maxOccupancy}</span>
                </div>
              </div>
            </div>
          ))}
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

export default LifeSupport;
