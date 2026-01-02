import { useState, useEffect } from 'react';
import { api, ApiError } from '../api/client';
import type { CrewMember, Section } from '../types';
import { Card } from '../components/ui/Card';
import { ErrorAlert, type ErrorInfo } from '../components/ui/ErrorAlert';
import {
  Users,
  Search,
  MapPin,
  Shield,
  Briefcase,
  UserCircle,
  ArrowRightLeft,
  Filter,
  RefreshCw,
  AlertTriangle
} from 'lucide-react';
import { motion, AnimatePresence } from 'framer-motion';

function Crew() {
  const [crew, setCrew] = useState<CrewMember[]>([]);
  const [sections, setSections] = useState<Section[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<ErrorInfo | null>(null);
  const [selectedSection, setSelectedSection] = useState<number | null>(null);
  const [searchTerm, setSearchTerm] = useState('');

  // Relocate modal state
  const [relocatingMember, setRelocatingMember] = useState<CrewMember | null>(null);
  const [relocateTargetSection, setRelocateTargetSection] = useState<number | null>(null);
  const [relocateLoading, setRelocateLoading] = useState(false);
  const [relocateError, setRelocateError] = useState<ErrorInfo | null>(null);

  useEffect(() => {
    loadData();
    const interval = setInterval(() => loadData(false), 15000);
    return () => clearInterval(interval);
  }, []);

  async function loadData(init = true) {
    try {
      if (init) setLoading(true);
      setError(null);
      const [crewData, sectionsData] = await Promise.all([
        api.crew.getRoster(),
        api.crew.getSections(),
      ]);
      setCrew(crewData);
      setSections(sectionsData);
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Failed to load crew data';
      const traceId = err instanceof ApiError ? err.traceId : null;
      setError({ message, traceId });
    } finally {
      if (init) setLoading(false);
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
      await loadData(false);
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Failed to relocate crew member';
      const traceId = err instanceof ApiError ? err.traceId : null;
      setRelocateError({ message, traceId });
    } finally {
      setRelocateLoading(false);
    }
  }

  // Only show crew currently aboard the station (exclude those in transit)
  const aboardCrew = crew.filter((c) => c.status !== 'IN_TRANSIT');

  const filteredCrew = aboardCrew.filter((c) => {
    const matchesSection = selectedSection ? c.sectionId === selectedSection : true;
    const matchesSearch = c.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
                          c.role.toLowerCase().includes(searchTerm.toLowerCase()) ||
                          c.rank.toLowerCase().includes(searchTerm.toLowerCase());
    return matchesSection && matchesSearch;
  });

  const availableSectionsForRelocate = relocatingMember
    ? sections.filter(
        (s) => s.id !== relocatingMember.sectionId && s.currentOccupancy < s.maxCapacity
      )
    : [];

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'ACTIVE': return 'text-emerald-400 bg-emerald-400/10 border-emerald-400/20';
      case 'ON_LEAVE': return 'text-cyan-400 bg-cyan-400/10 border-cyan-400/20';
      case 'OFF_DUTY': return 'text-yellow-400 bg-yellow-400/10 border-yellow-400/20';
      case 'IN_TRANSIT': return 'text-purple-400 bg-purple-400/10 border-purple-400/20';
      default: return 'text-gray-400 bg-gray-400/10 border-gray-400/20';
    }
  };

  if (loading) {
     return (
        <div className="flex flex-col items-center justify-center h-[60vh] text-cyan-500/50 space-y-4">
           <Users className="w-12 h-12 animate-pulse" />
           <div className="font-mono text-sm tracking-widest animate-pulse">LOADING PERSONNEL DATA...</div>
        </div>
     );
  }

  return (
    <div className="space-y-6">
       {/* Header */}
       <div className="flex flex-col md:flex-row md:items-end justify-between gap-4">
         <div>
            <h2 className="text-3xl font-light text-white uppercase tracking-wider">
               Crew <span className="text-cyan-400 font-bold">Manifest</span>
            </h2>
            <div className="flex items-center gap-2 mt-1 text-cyan-500/60 font-mono text-xs">
               <span className="w-2 h-2 rounded-full bg-cyan-500/50 animate-pulse" />
               PERSONNEL ABOARD: {aboardCrew.length}
            </div>
         </div>
         <div className="flex gap-2">
            <div className="relative group">
               <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-cyan-500/50 group-focus-within:text-cyan-400 transition-colors" />
               <input 
                  type="text" 
                  placeholder="SEARCH PERSONNEL..." 
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  className="bg-space-950/50 border border-space-700 rounded-full pl-9 pr-4 py-2 text-sm text-cyan-100 placeholder:text-cyan-900/50 focus:outline-none focus:border-cyan-500/50 focus:ring-1 focus:ring-cyan-500/50 transition-all w-64 font-mono"
               />
            </div>
            <button
               onClick={() => loadData()}
               className="p-2 text-cyan-500/50 hover:text-cyan-400 hover:bg-cyan-500/10 rounded-full transition-all"
               title="Refresh Data"
            >
               <RefreshCw className="w-5 h-5" />
            </button>
         </div>
      </div>

      {error && <ErrorAlert error={error} />}

      {/* Section Filter Bar */}
      <div className="flex overflow-x-auto pb-2 gap-2 scrollbar-thin scrollbar-thumb-space-700 scrollbar-track-transparent">
        <button
          onClick={() => setSelectedSection(null)}
          className={`flex items-center gap-2 px-4 py-2 rounded border transition-all whitespace-nowrap ${
            selectedSection === null
              ? 'bg-cyan-500/20 border-cyan-500/50 text-cyan-300 shadow-[0_0_10px_rgba(34,211,238,0.2)]'
              : 'bg-space-900/50 border-space-700 text-cyan-500/50 hover:border-cyan-500/30 hover:text-cyan-400'
          }`}
        >
          <Filter className="w-4 h-4" />
          <span className="font-mono uppercase text-sm">All Sections</span>
          <span className="text-xs bg-space-950 px-1.5 py-0.5 rounded text-cyan-500/70">{aboardCrew.length}</span>
        </button>
        
        {sections.map((section) => (
          <button
            key={section.id}
            onClick={() => setSelectedSection(section.id)}
            className={`flex items-center gap-2 px-4 py-2 rounded border transition-all whitespace-nowrap ${
              selectedSection === section.id
                ? 'bg-cyan-500/20 border-cyan-500/50 text-cyan-300 shadow-[0_0_10px_rgba(34,211,238,0.2)]'
                : 'bg-space-900/50 border-space-700 text-cyan-500/50 hover:border-cyan-500/30 hover:text-cyan-400'
            }`}
          >
            <span className="font-mono uppercase text-sm">{section.name}</span>
            <div className="flex flex-col items-end text-[10px] leading-none opacity-70">
              <span>{section.currentOccupancy}/{section.maxCapacity}</span>
              <div className="w-full h-0.5 bg-space-950 mt-0.5 rounded-full overflow-hidden">
                <div 
                  className={`h-full ${
                    section.currentOccupancy / section.maxCapacity > 0.9 ? 'bg-red-500' : 'bg-cyan-500'
                  }`}
                  style={{ width: `${(section.currentOccupancy / section.maxCapacity) * 100}%` }} 
                />
              </div>
            </div>
          </button>
        ))}
      </div>

      {/* Crew Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
        <AnimatePresence>
          {filteredCrew.map((member) => (
            <motion.div
              key={member.id}
              initial={{ opacity: 0, scale: 0.95 }}
              animate={{ opacity: 1, scale: 1 }}
              exit={{ opacity: 0, scale: 0.95 }}
              transition={{ duration: 0.2 }}
            >
              <Card className="h-full hover:bg-space-800/30 transition-colors group">
                <div className="flex justify-between items-start mb-4">
                  <div className="flex items-center gap-3">
                    <div className="w-10 h-10 rounded bg-space-950 flex items-center justify-center border border-space-700 group-hover:border-cyan-500/30 transition-colors">
                      <UserCircle className="w-6 h-6 text-cyan-500/50" />
                    </div>
                    <div>
                      <h3 className="font-bold text-cyan-100 tracking-wide">{member.name}</h3>
                      <div className="flex items-center gap-2 text-xs text-cyan-500/60 font-mono uppercase">
                        <Shield className="w-3 h-3" />
                        {member.rank}
                      </div>
                    </div>
                  </div>
                  <span className={`text-[10px] px-2 py-1 rounded border font-mono uppercase tracking-wider ${getStatusColor(member.status)}`}>
                    {member.status.replace('_', ' ')}
                  </span>
                </div>

                <div className="space-y-2 mb-4">
                  <div className="flex items-center justify-between text-sm p-2 bg-space-950/30 rounded border border-space-800/50">
                    <span className="text-cyan-500/50 flex items-center gap-2">
                      <Briefcase className="w-3 h-3" />
                      Role
                    </span>
                    <span className="text-cyan-100 font-mono text-xs uppercase">{member.role}</span>
                  </div>
                  <div className="flex items-center justify-between text-sm p-2 bg-space-950/30 rounded border border-space-800/50">
                    <span className="text-cyan-500/50 flex items-center gap-2">
                      <MapPin className="w-3 h-3" />
                      Section
                    </span>
                    <span className="text-cyan-100 font-mono text-xs uppercase">{member.sectionName}</span>
                  </div>
                </div>

                <button
                  onClick={() => setRelocatingMember(member)}
                  disabled={member.status === 'IN_TRANSIT'}
                  className="w-full py-2 flex items-center justify-center gap-2 rounded border border-cyan-500/20 bg-cyan-500/5 text-cyan-400 text-xs font-mono uppercase tracking-wider hover:bg-cyan-500/10 hover:border-cyan-500/40 transition-all disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  <ArrowRightLeft className="w-3 h-3" />
                  Relocate
                </button>
              </Card>
            </motion.div>
          ))}
        </AnimatePresence>
      </div>

      {filteredCrew.length === 0 && (
        <div className="flex flex-col items-center justify-center h-48 text-cyan-500/30 border border-dashed border-space-700 rounded-lg">
          <Users className="w-12 h-12 mb-2" />
          <p className="font-mono uppercase tracking-widest">No personnel found</p>
        </div>
      )}

      {/* Relocate Modal */}
      <AnimatePresence>
        {relocatingMember && (
          <div className="fixed inset-0 bg-space-950/80 backdrop-blur-sm flex items-center justify-center z-50 p-4">
            <motion.div 
              initial={{ opacity: 0, scale: 0.9 }}
              animate={{ opacity: 1, scale: 1 }}
              exit={{ opacity: 0, scale: 0.9 }}
              className="bg-space-900 border border-space-700 rounded-lg max-w-md w-full shadow-[0_0_50px_rgba(0,0,0,0.5)] overflow-hidden"
            >
              <div className="p-4 border-b border-space-700 flex justify-between items-center bg-space-950/50">
                <h3 className="text-lg font-bold text-white flex items-center gap-2">
                  <ArrowRightLeft className="w-5 h-5 text-purple-400" />
                  Relocate Personnel
                </h3>
                <button onClick={() => setRelocatingMember(null)} className="text-cyan-500/50 hover:text-cyan-400">
                  <span className="sr-only">Close</span>
                  ✕
                </button>
              </div>

              <div className="p-6 space-y-6">
                <div className="flex items-center gap-4 p-4 bg-space-950/50 border border-space-800 rounded">
                  <div className="w-12 h-12 rounded bg-space-900 flex items-center justify-center">
                    <UserCircle className="w-8 h-8 text-cyan-500/50" />
                  </div>
                  <div>
                    <div className="font-bold text-white text-lg">{relocatingMember.name}</div>
                    <div className="flex gap-4 text-xs font-mono text-cyan-500/70">
                      <span>CURRENT: {relocatingMember.sectionName}</span>
                      <span>RANK: {relocatingMember.rank}</span>
                    </div>
                  </div>
                </div>

                {relocateError && <ErrorAlert error={relocateError} />}

                <div>
                  <label className="block text-xs font-mono text-cyan-500/70 uppercase mb-2">Select Destination Section</label>
                  <div className="grid grid-cols-1 gap-2 max-h-48 overflow-y-auto pr-2 scrollbar-thin scrollbar-thumb-space-700">
                    {availableSectionsForRelocate.map((section) => (
                      <button
                        key={section.id}
                        onClick={() => setRelocateTargetSection(section.id)}
                        className={`p-3 rounded border text-left transition-all ${
                          relocateTargetSection === section.id
                            ? 'bg-purple-500/20 border-purple-500/50 shadow-[0_0_10px_rgba(168,85,247,0.2)]'
                            : 'bg-space-800/30 border-space-700 hover:border-cyan-500/30'
                        }`}
                      >
                        <div className="flex justify-between items-center mb-1">
                          <span className={`font-mono font-bold ${relocateTargetSection === section.id ? 'text-purple-300' : 'text-cyan-100'}`}>
                            {section.name}
                          </span>
                          <span className="text-xs text-cyan-500/50">{section.currentOccupancy}/{section.maxCapacity}</span>
                        </div>
                        <div className="w-full h-1 bg-space-950 rounded-full overflow-hidden">
                          <div 
                            className={`h-full ${relocateTargetSection === section.id ? 'bg-purple-500' : 'bg-cyan-500/50'}`} 
                            style={{ width: `${(section.currentOccupancy / section.maxCapacity) * 100}%` }}
                          />
                        </div>
                      </button>
                    ))}
                    {availableSectionsForRelocate.length === 0 && (
                      <div className="p-4 text-center text-yellow-400 bg-yellow-400/10 border border-yellow-400/20 rounded">
                        No other sections available with capacity.
                      </div>
                    )}
                  </div>
                </div>
              </div>

              <div className="p-4 border-t border-space-700 bg-space-950/50 flex justify-end gap-3">
                <button
                  onClick={() => setRelocatingMember(null)}
                  className="px-4 py-2 text-cyan-500/70 hover:text-cyan-400 font-mono text-sm uppercase tracking-wider"
                >
                  Cancel
                </button>
                <button
                  onClick={handleRelocate}
                  disabled={!relocateTargetSection || relocateLoading}
                  className="px-6 py-2 bg-purple-500/20 hover:bg-purple-500/30 text-purple-300 border border-purple-500/50 rounded transition-all font-mono text-sm uppercase tracking-wider flex items-center gap-2 disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  {relocateLoading ? (
                    <>
                      <div className="w-3 h-3 border-2 border-purple-300/30 border-t-purple-300 rounded-full animate-spin" />
                      Processing...
                    </>
                  ) : (
                    <>
                      <ArrowRightLeft className="w-4 h-4" />
                      Confirm Transfer
                    </>
                  )}
                </button>
              </div>
            </motion.div>
          </div>
        )}
      </AnimatePresence>
    </div>
  );
}

export default Crew;
