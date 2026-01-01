import { useState, useEffect } from 'react';
import { api } from '../api/client';
import type { CrewMember, Section } from '../types';

function Crew() {
  const [crew, setCrew] = useState<CrewMember[]>([]);
  const [sections, setSections] = useState<Section[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [selectedSection, setSelectedSection] = useState<number | null>(null);

  useEffect(() => {
    loadData();
  }, []);

  async function loadData() {
    try {
      setLoading(true);
      setError(null);
      const [crewData, sectionsData] = await Promise.all([
        api.crew.getRoster(),
        api.crew.getSections(),
      ]);
      setCrew(crewData);
      setSections(sectionsData);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load crew data');
    } finally {
      setLoading(false);
    }
  }

  const statusColors = {
    ACTIVE: 'text-green-400',
    ON_LEAVE: 'text-blue-400',
    OFF_DUTY: 'text-yellow-400',
    IN_TRANSIT: 'text-purple-400',
  };

  const filteredCrew = selectedSection
    ? crew.filter((c) => c.sectionId === selectedSection)
    : crew;

  if (loading) {
    return <div className="text-gray-400 text-center py-8">Loading crew data...</div>;
  }

  return (
    <div className="space-y-6">
      <h2 className="text-2xl font-bold text-white">Crew Manifest</h2>

      {error && (
        <div className="bg-red-900/50 border border-red-500 rounded-lg p-4">
          <p className="text-red-300 text-sm">{error}</p>
        </div>
      )}

      {/* Sections Overview */}
      <div>
        <h3 className="text-lg font-semibold text-white mb-4">Sections</h3>
        <div className="grid grid-cols-2 md:grid-cols-4 lg:grid-cols-8 gap-2">
          <button
            onClick={() => setSelectedSection(null)}
            className={`p-3 rounded-lg text-sm ${
              selectedSection === null
                ? 'bg-blue-600 text-white'
                : 'bg-gray-700 text-gray-300 hover:bg-gray-600'
            }`}
          >
            All ({crew.length})
          </button>
          {sections.map((section) => (
            <button
              key={section.id}
              onClick={() => setSelectedSection(section.id)}
              className={`p-3 rounded-lg text-sm ${
                selectedSection === section.id
                  ? 'bg-blue-600 text-white'
                  : 'bg-gray-700 text-gray-300 hover:bg-gray-600'
              }`}
            >
              <div className="font-medium truncate">{section.name}</div>
              <div className="text-xs opacity-70">
                {section.currentOccupancy}/{section.maxCapacity}
              </div>
            </button>
          ))}
        </div>
      </div>

      {/* Crew Table */}
      <div className="bg-gray-800 rounded-lg overflow-hidden">
        <table className="w-full">
          <thead className="bg-gray-700">
            <tr>
              <th className="px-4 py-3 text-left text-sm font-medium text-gray-300">Name</th>
              <th className="px-4 py-3 text-left text-sm font-medium text-gray-300">Rank</th>
              <th className="px-4 py-3 text-left text-sm font-medium text-gray-300">Role</th>
              <th className="px-4 py-3 text-left text-sm font-medium text-gray-300">Section</th>
              <th className="px-4 py-3 text-left text-sm font-medium text-gray-300">Status</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-700">
            {filteredCrew.map((member) => (
              <tr key={member.id} className="hover:bg-gray-700/50">
                <td className="px-4 py-3 text-white font-medium">{member.name}</td>
                <td className="px-4 py-3 text-gray-400">{member.rank}</td>
                <td className="px-4 py-3 text-gray-400">{member.role}</td>
                <td className="px-4 py-3 text-gray-400">{member.sectionName}</td>
                <td className={`px-4 py-3 ${statusColors[member.status]}`}>{member.status}</td>
              </tr>
            ))}
          </tbody>
        </table>
        {filteredCrew.length === 0 && (
          <div className="text-center py-8 text-gray-500">No crew members found</div>
        )}
      </div>

      {/* Stats */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
        <div className="bg-gray-800 rounded-lg p-4">
          <div className="text-2xl font-bold text-green-400">
            {crew.filter((c) => c.status === 'ACTIVE').length}
          </div>
          <div className="text-sm text-gray-400">Active</div>
        </div>
        <div className="bg-gray-800 rounded-lg p-4">
          <div className="text-2xl font-bold text-yellow-400">
            {crew.filter((c) => c.status === 'OFF_DUTY').length}
          </div>
          <div className="text-sm text-gray-400">Off Duty</div>
        </div>
        <div className="bg-gray-800 rounded-lg p-4">
          <div className="text-2xl font-bold text-blue-400">
            {crew.filter((c) => c.status === 'ON_LEAVE').length}
          </div>
          <div className="text-sm text-gray-400">On Leave</div>
        </div>
        <div className="bg-gray-800 rounded-lg p-4">
          <div className="text-2xl font-bold text-purple-400">
            {crew.filter((c) => c.status === 'IN_TRANSIT').length}
          </div>
          <div className="text-sm text-gray-400">In Transit</div>
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

export default Crew;
