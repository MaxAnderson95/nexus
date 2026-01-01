import { useState, useEffect } from 'react';
import { api } from '../api/client';
import type { CrewMember, Section } from '../types';

function Crew() {
  const [crew, setCrew] = useState<CrewMember[]>([]);
  const [sections, setSections] = useState<Section[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [selectedSection, setSelectedSection] = useState<number | null>(null);
  
  // Relocate modal state
  const [relocatingMember, setRelocatingMember] = useState<CrewMember | null>(null);
  const [relocateTargetSection, setRelocateTargetSection] = useState<number | null>(null);
  const [relocateLoading, setRelocateLoading] = useState(false);
  const [relocateError, setRelocateError] = useState<string | null>(null);

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

  async function handleRelocate() {
    if (!relocatingMember || !relocateTargetSection) return;
    
    try {
      setRelocateLoading(true);
      setRelocateError(null);
      await api.crew.relocate(relocatingMember.id, relocateTargetSection);
      setRelocatingMember(null);
      setRelocateTargetSection(null);
      await loadData();
    } catch (err) {
      setRelocateError(err instanceof Error ? err.message : 'Failed to relocate crew member');
    } finally {
      setRelocateLoading(false);
    }
  }

  function openRelocateModal(member: CrewMember) {
    setRelocatingMember(member);
    setRelocateTargetSection(null);
    setRelocateError(null);
  }

  function closeRelocateModal() {
    setRelocatingMember(null);
    setRelocateTargetSection(null);
    setRelocateError(null);
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

  // Available sections for relocation (exclude current section and full sections)
  const availableSectionsForRelocate = relocatingMember
    ? sections.filter(
        (s) => s.id !== relocatingMember.sectionId && s.currentOccupancy < s.maxCapacity
      )
    : [];

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
              <th className="px-4 py-3 text-left text-sm font-medium text-gray-300">Actions</th>
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
                <td className="px-4 py-3">
                  <button
                    onClick={() => openRelocateModal(member)}
                    disabled={member.status === 'IN_TRANSIT'}
                    className={`px-3 py-1 rounded text-sm ${
                      member.status === 'IN_TRANSIT'
                        ? 'bg-gray-600 text-gray-400 cursor-not-allowed'
                        : 'bg-purple-600 hover:bg-purple-700 text-white'
                    }`}
                  >
                    Relocate
                  </button>
                </td>
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

      {/* Relocate Modal */}
      {relocatingMember && (
        <div className="fixed inset-0 bg-black/70 flex items-center justify-center z-50">
          <div className="bg-gray-800 rounded-lg p-6 max-w-md w-full mx-4">
            <h3 className="text-lg font-semibold text-white mb-4">
              Relocate {relocatingMember.name}
            </h3>
            
            <p className="text-gray-400 text-sm mb-4">
              Current section: <span className="text-white">{relocatingMember.sectionName}</span>
            </p>

            {relocateError && (
              <div className="bg-red-900/50 border border-red-500 rounded-lg p-3 mb-4">
                <p className="text-red-300 text-sm">{relocateError}</p>
              </div>
            )}

            <div className="mb-4">
              <label className="block text-sm font-medium text-gray-300 mb-2">
                Select destination section:
              </label>
              <div className="grid grid-cols-2 gap-2 max-h-48 overflow-y-auto">
                {availableSectionsForRelocate.map((section) => (
                  <button
                    key={section.id}
                    onClick={() => setRelocateTargetSection(section.id)}
                    className={`p-3 rounded-lg text-sm text-left ${
                      relocateTargetSection === section.id
                        ? 'bg-purple-600 text-white'
                        : 'bg-gray-700 text-gray-300 hover:bg-gray-600'
                    }`}
                  >
                    <div className="font-medium">{section.name}</div>
                    <div className="text-xs opacity-70">
                      {section.currentOccupancy}/{section.maxCapacity} occupied
                    </div>
                  </button>
                ))}
              </div>
              {availableSectionsForRelocate.length === 0 && (
                <p className="text-yellow-400 text-sm">No available sections for relocation</p>
              )}
            </div>

            <div className="flex gap-3">
              <button
                onClick={handleRelocate}
                disabled={!relocateTargetSection || relocateLoading}
                className={`flex-1 py-2 rounded text-sm ${
                  !relocateTargetSection || relocateLoading
                    ? 'bg-gray-600 text-gray-400 cursor-not-allowed'
                    : 'bg-purple-600 hover:bg-purple-700 text-white'
                }`}
              >
                {relocateLoading ? 'Relocating...' : 'Confirm Relocation'}
              </button>
              <button
                onClick={closeRelocateModal}
                disabled={relocateLoading}
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

export default Crew;
