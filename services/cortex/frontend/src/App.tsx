import { Routes, Route } from 'react-router-dom'
import Layout from './components/Layout'
import Dashboard from './pages/Dashboard'
import Docking from './pages/Docking'
import Crew from './pages/Crew'
import LifeSupport from './pages/LifeSupport'
import Power from './pages/Power'
import Inventory from './pages/Inventory'
import Admin from './pages/Admin'

function App() {
  return (
    <Layout>
      <Routes>
        <Route path="/" element={<Dashboard />} />
        <Route path="/docking" element={<Docking />} />
        <Route path="/crew" element={<Crew />} />
        <Route path="/life-support" element={<LifeSupport />} />
        <Route path="/power" element={<Power />} />
        <Route path="/inventory" element={<Inventory />} />
        <Route path="/admin" element={<Admin />} />
      </Routes>
    </Layout>
  )
}

export default App
