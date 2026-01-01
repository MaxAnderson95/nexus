import { NavLink } from 'react-router-dom';

interface LayoutProps {
  children: React.ReactNode;
}

const navItems = [
  { path: '/', label: 'Dashboard', icon: '◉' },
  { path: '/docking', label: 'Docking', icon: '⚓' },
  { path: '/crew', label: 'Crew', icon: '👥' },
  { path: '/life-support', label: 'Life Support', icon: '🌡' },
  { path: '/power', label: 'Power', icon: '⚡' },
  { path: '/inventory', label: 'Inventory', icon: '📦' },
];

function Layout({ children }: LayoutProps) {
  return (
    <div className="min-h-screen flex flex-col">
      {/* Header */}
      <header className="bg-gray-800 border-b border-gray-700">
        <div className="max-w-7xl mx-auto px-4 py-3">
          <div className="flex items-center justify-between">
            <div className="flex items-center space-x-3">
              <div className="w-10 h-10 bg-blue-600 rounded-full flex items-center justify-center">
                <span className="text-xl">◎</span>
              </div>
              <div>
                <h1 className="text-xl font-bold text-white">NEXUS STATION</h1>
                <p className="text-xs text-gray-400">Operations Control Center</p>
              </div>
            </div>
            <div className="text-sm text-gray-400">
              <span className="inline-block w-2 h-2 bg-green-500 rounded-full mr-2"></span>
              Systems Online
            </div>
          </div>
        </div>
      </header>

      {/* Navigation */}
      <nav className="bg-gray-800 border-b border-gray-700">
        <div className="max-w-7xl mx-auto px-4">
          <div className="flex space-x-1">
            {navItems.map((item) => (
              <NavLink
                key={item.path}
                to={item.path}
                className={({ isActive }) =>
                  `px-4 py-3 text-sm font-medium transition-colors ${
                    isActive
                      ? 'text-blue-400 border-b-2 border-blue-400'
                      : 'text-gray-400 hover:text-gray-200 hover:bg-gray-700'
                  }`
                }
              >
                <span className="mr-2">{item.icon}</span>
                {item.label}
              </NavLink>
            ))}
          </div>
        </div>
      </nav>

      {/* Main Content */}
      <main className="flex-1 bg-gray-900">
        <div className="max-w-7xl mx-auto px-4 py-6">{children}</div>
      </main>

      {/* Footer */}
      <footer className="bg-gray-800 border-t border-gray-700 py-3">
        <div className="max-w-7xl mx-auto px-4 text-center text-xs text-gray-500">
          NEXUS Station Management System v0.1.0 | OpenTelemetry Demo
        </div>
      </footer>
    </div>
  );
}

export default Layout;
