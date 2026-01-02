import { NavLink, Link } from 'react-router-dom';
import { 
  LayoutDashboard, 
  Anchor, 
  Users, 
  ThermometerSun, 
  Zap, 
  Package, 
  Menu,
  X,
  Activity,
  Cpu,
  Settings
} from 'lucide-react';
import { useState, useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';

interface LayoutProps {
  children: React.ReactNode;
}

const navItems = [
  { path: '/', label: 'Command', icon: LayoutDashboard },
  { path: '/docking', label: 'Docking', icon: Anchor },
  { path: '/crew', label: 'Manifest', icon: Users },
  { path: '/life-support', label: 'Life Support', icon: ThermometerSun },
  { path: '/power', label: 'Power Grid', icon: Zap },
  { path: '/inventory', label: 'Cargo', icon: Package },
  { path: '/admin', label: 'Admin', icon: Settings },
];

function Layout({ children }: LayoutProps) {
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
  const [currentTime, setCurrentTime] = useState(new Date());

  useEffect(() => {
    const timer = setInterval(() => setCurrentTime(new Date()), 1000);
    return () => clearInterval(timer);
  }, []);

  return (
    <div className="h-screen flex flex-col relative overflow-hidden">
      {/* Cinematic CRT Effect Overlay */}
      <div className="scanline fixed inset-0 pointer-events-none z-[100]" />
      
      {/* Background Ambience */}
      <div className="fixed inset-0 bg-gradient-radial from-transparent to-space-950/80 pointer-events-none z-0" />

      {/* Top HUD Bar */}
      <header className="relative z-20 bg-space-900/80 backdrop-blur-md border-b border-space-700 h-16 flex-none">
        <div className="h-full max-w-[1920px] mx-auto px-4 md:px-6 flex items-center justify-between">
          
          {/* Brand / Logo Area */}
          <Link to="/" className="flex items-center space-x-4 group cursor-pointer">
            <div className="relative w-10 h-10 flex items-center justify-center">
              <div className="absolute inset-0 border-2 border-cyan-500/30 rounded-full animate-spin-slow" />
              <div className="absolute inset-1 border border-cyan-400/50 rounded-full" />
              <div className="absolute inset-0 bg-cyan-400/0 group-hover:bg-cyan-400/10 rounded-full transition-colors duration-300" />
              <Cpu className="w-5 h-5 text-cyan-400 drop-shadow-[0_0_5px_rgba(34,211,238,0.8)] group-hover:drop-shadow-[0_0_8px_rgba(34,211,238,1)] transition-all" />
            </div>
            <div>
              <h1 className="text-2xl font-bold tracking-widest text-transparent bg-clip-text bg-gradient-to-r from-cyan-300 to-cyan-600 uppercase font-sans group-hover:brightness-110 transition-all">
                Nexus<span className="font-light text-cyan-400/70">OS</span>
              </h1>
              <div className="flex items-center space-x-2 text-[10px] text-cyan-500/60 font-mono tracking-widest uppercase">
                <span>V.2.4.0</span>
                <span>::</span>
                <span>Sys.Online</span>
              </div>
            </div>
          </Link>

          {/* Center Status Ticker (Hidden on Mobile) */}
          <div className="hidden md:flex items-center space-x-8 text-xs font-mono text-cyan-500/60">
             <div className="flex items-center space-x-2">
                <Activity className="w-3 h-3 animate-pulse" />
                <span>CORE_TEMP: 42°C</span>
             </div>
             <div className="h-4 w-px bg-space-700" />
             <div className="flex items-center space-x-2">
                <span className="w-2 h-2 bg-emerald-500 rounded-full shadow-[0_0_8px_#10b981]" />
                <span className="text-emerald-500">OPTIMAL</span>
             </div>
          </div>

          {/* Right Side Clock & Mobile Menu */}
          <div className="flex items-center space-x-6">
            <div className="hidden md:block text-right font-mono text-cyan-400">
              <div className="text-lg leading-none tracking-widest text-shadow-glow">
                {currentTime.toLocaleTimeString('en-US', { hour12: false, timeZone: 'UTC' })}
              </div>
              <div className="text-[10px] text-cyan-600 uppercase tracking-[0.2em]">
                UTC // {currentTime.toLocaleDateString('en-US', { timeZone: 'UTC' })}
              </div>
            </div>

            <button 
              className="md:hidden text-cyan-400 hover:text-cyan-200 transition-colors"
              onClick={() => setIsMobileMenuOpen(!isMobileMenuOpen)}
            >
              {isMobileMenuOpen ? <X /> : <Menu />}
            </button>
          </div>
        </div>
      </header>

      {/* Main Layout Container */}
      <div className="flex flex-1 overflow-hidden relative z-10">
        
        {/* Sidebar Navigation (Desktop) */}
        <aside className="hidden md:flex flex-col w-64 bg-space-900/50 border-r border-space-700 backdrop-blur-sm">
          <nav className="flex-1 py-6 px-3 space-y-1">
            {navItems.map((item) => (
              <NavLink
                key={item.path}
                to={item.path}
                className={({ isActive }) =>
                  `group relative flex items-center px-4 py-3 text-sm font-medium tracking-wide transition-all duration-300 overflow-hidden rounded-sm ${
                    isActive
                      ? 'text-space-950 bg-cyan-400 shadow-[0_0_15px_rgba(34,211,238,0.4)]'
                      : 'text-cyan-500/70 hover:text-cyan-300 hover:bg-space-800/50'
                  }`
                }
              >
                {({ isActive }) => (
                  <>
                    {/* Decorative border/corner marks for inactive state */}
                    <div className="absolute left-0 top-0 w-0.5 h-full bg-cyan-500/30 opacity-0 group-hover:opacity-100 transition-opacity" />
                    
                    <item.icon className="w-5 h-5 mr-3 transition-transform group-hover:scale-110" />
                    <span className="font-mono uppercase">{item.label}</span>
                    
                    {/* Active glow effect */}
                    {isActive && (
                      <div className="absolute right-2 w-1.5 h-1.5 bg-space-950 rounded-full animate-pulse" />
                    )}
                  </>
                )}
              </NavLink>
            ))}
          </nav>
          
          {/* Bottom Sidebar Info */}
          <div className="p-4 border-t border-space-700/50">
            <div className="bg-space-950/50 rounded p-3 border border-space-800">
              <div className="flex justify-between items-center mb-2">
                <span className="text-[10px] uppercase text-cyan-600 font-mono">Sys.Load</span>
                <span className="text-[10px] font-mono text-emerald-400">24%</span>
              </div>
              <div className="w-full bg-space-800 h-1 rounded-full overflow-hidden">
                <div className="bg-gradient-to-r from-cyan-600 to-emerald-400 h-full w-1/4 animate-pulse" />
              </div>
            </div>
          </div>
        </aside>

        {/* Mobile Navigation Menu */}
        <AnimatePresence>
          {isMobileMenuOpen && (
            <motion.div
              initial={{ x: '-100%' }}
              animate={{ x: 0 }}
              exit={{ x: '-100%' }}
              transition={{ type: 'spring', damping: 20 }}
              className="absolute inset-y-0 left-0 w-64 bg-space-900 border-r border-space-700 z-50 md:hidden"
            >
              <nav className="flex-col py-6 px-3 space-y-1">
                {navItems.map((item) => (
                   <NavLink
                    key={item.path}
                    to={item.path}
                    onClick={() => setIsMobileMenuOpen(false)}
                    className={({ isActive }) =>
                      `flex items-center px-4 py-4 text-sm font-medium border-l-2 transition-all ${
                        isActive
                          ? 'border-cyan-400 text-cyan-100 bg-cyan-500/10'
                          : 'border-transparent text-cyan-500/70 hover:text-cyan-300 hover:bg-space-800/50'
                      }`
                    }
                  >
                    <item.icon className="w-5 h-5 mr-3" />
                    <span className="font-mono uppercase">{item.label}</span>
                  </NavLink>
                ))}
              </nav>
            </motion.div>
          )}
        </AnimatePresence>

        {/* Main Content Area */}
        <main className="flex-1 overflow-auto bg-space-950/30 p-4 md:p-8 scroll-smooth">
          <div className="max-w-[1600px] mx-auto animate-in fade-in slide-in-from-bottom-4 duration-500">
             {children}
          </div>
        </main>
      </div>

      {/* Decorative Corner Borders for Screen */}
      <div className="fixed top-0 left-0 w-8 h-8 border-t-2 border-l-2 border-cyan-500/30 rounded-tl-lg pointer-events-none z-50" />
      <div className="fixed top-0 right-0 w-8 h-8 border-t-2 border-r-2 border-cyan-500/30 rounded-tr-lg pointer-events-none z-50" />
      <div className="fixed bottom-0 left-0 w-8 h-8 border-b-2 border-l-2 border-cyan-500/30 rounded-bl-lg pointer-events-none z-50" />
      <div className="fixed bottom-0 right-0 w-8 h-8 border-b-2 border-r-2 border-cyan-500/30 rounded-br-lg pointer-events-none z-50" />
    </div>
  );
}

export default Layout;
