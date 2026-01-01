import { useState, useEffect } from 'react';
import { api } from '../api/client';
import type { EnvironmentStatus, Alert, SelfTestResult } from '../types';

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
      await loadData();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to adjust environment');
    } finally {
      setAdjustLoading(false);
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

      {/* Self-Test Result */}
      {testResult && (
        <div className={`rounded-lg border-2 p-4 ${testResult.passed ? 'border-green-500 bg-green-900/20' : 'border-red-500 bg-red-900/20'}`}>
          <div className="flex justify-between items-start mb-3">
            <h3 className="font-bold text-white">
              Self-Test Result: {testResult.sectionName}
            </h3>
            <div className="flex items-center gap-2">
              <span className={`text-xs px-2 py-1 rounded ${testResult.passed ? 'bg-green-600' : 'bg-red-600'}`}>
                {testResult.overallStatus}
              </span>
              <span className="text-xs text-gray-400">{testResult.durationMs}ms</span>
              <button
                onClick={() => setTestResult(null)}
                className="text-gray-400 hover:text-white"
              >
                x
              </button>
            </div>
          </div>
          <div className="grid grid-cols-2 md:grid-cols-5 gap-2">
            {testResult.subsystems.map((sub, idx) => (
              <div key={idx} className={`p-2 rounded text-sm ${sub.passed ? 'bg-green-900/50' : 'bg-red-900/50'}`}>
                <div className="font-medium">{sub.name}</div>
                <div className={`text-xs ${sub.passed ? 'text-green-400' : 'text-red-400'}`}>
                  {sub.message}
                </div>
              </div>
            ))}
          </div>
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
              
              {/* Action Buttons */}
              <div className="mt-3 pt-3 border-t border-gray-600 flex gap-2">
                <button
                  onClick={() => handleSelfTest(section.sectionId)}
                  disabled={testingSection !== null}
                  className={`flex-1 px-2 py-1 rounded text-xs ${
                    testingSection === section.sectionId
                      ? 'bg-yellow-600 text-white'
                      : testingSection !== null
                      ? 'bg-gray-600 text-gray-400 cursor-not-allowed'
                      : 'bg-cyan-600 hover:bg-cyan-700 text-white'
                  }`}
                >
                  {testingSection === section.sectionId ? 'Testing...' : 'Self-Test'}
                </button>
                <button
                  onClick={() => openAdjustModal(section)}
                  className="flex-1 px-2 py-1 bg-purple-600 hover:bg-purple-700 rounded text-xs text-white"
                >
                  Adjust
                </button>
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

      {/* Adjustment Modal */}
      {adjustingSection && (
        <div className="fixed inset-0 bg-black/70 flex items-center justify-center z-50">
          <div className="bg-gray-800 rounded-lg p-6 max-w-md w-full mx-4">
            <h3 className="text-lg font-semibold text-white mb-4">
              Adjust Environment: {adjustingSection.sectionName}
            </h3>

            <div className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-300 mb-2">
                  Target Temperature: {adjustTemp}°C
                </label>
                <input
                  type="range"
                  min="16"
                  max="28"
                  step="0.5"
                  value={adjustTemp}
                  onChange={(e) => setAdjustTemp(parseFloat(e.target.value))}
                  className="w-full"
                />
                <div className="flex justify-between text-xs text-gray-500">
                  <span>16°C</span>
                  <span>22°C</span>
                  <span>28°C</span>
                </div>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-300 mb-2">
                  Target O2 Level: {adjustO2}%
                </label>
                <input
                  type="range"
                  min="19"
                  max="23"
                  step="0.1"
                  value={adjustO2}
                  onChange={(e) => setAdjustO2(parseFloat(e.target.value))}
                  className="w-full"
                />
                <div className="flex justify-between text-xs text-gray-500">
                  <span>19%</span>
                  <span>21%</span>
                  <span>23%</span>
                </div>
              </div>
            </div>

            <div className="flex gap-3 mt-6">
              <button
                onClick={handleAdjust}
                disabled={adjustLoading}
                className={`flex-1 py-2 rounded text-sm ${
                  adjustLoading
                    ? 'bg-gray-600 text-gray-400 cursor-not-allowed'
                    : 'bg-purple-600 hover:bg-purple-700 text-white'
                }`}
              >
                {adjustLoading ? 'Applying...' : 'Apply Changes'}
              </button>
              <button
                onClick={() => setAdjustingSection(null)}
                disabled={adjustLoading}
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

export default LifeSupport;
