import { useEffect, useState } from 'react'
import { useLocation, NavLink } from 'react-router-dom'
import { Activity, Calendar, Users, Package, LayoutDashboard, LogOut, ShoppingCart, BarChart2, ClipboardList, Menu, X } from 'lucide-react'
import { RoleGate } from './RoleGate'
import { useAuthStore } from '../store/authStore'

export default function Layout({ children }) {
  const location = useLocation();
  const [isSidebarOpen, setIsSidebarOpen] = useState(false);
  
  const isActive = (path) => location.pathname === path ? "bg-white/10 text-cyan-400" : "hover:bg-white/5 text-slate-300";

  // Auto-close sidebar on mobile when route changes
  useEffect(() => {
    setIsSidebarOpen(false);
  }, [location.pathname]);

  return (
    <div className="min-h-screen flex relative bg-[#06030F] selection:bg-cyan-500 selection:text-white">
      
      {/* Mobile Top Bar */}
      <div className="lg:hidden fixed top-0 left-0 right-0 h-16 bg-[#0f0a1c]/90 backdrop-blur-md border-b border-white/5 flex items-center justify-between px-4 z-40">
        <h1 className="text-xl font-bold text-cyan-400 tracking-wider">SIRBULL PET</h1>
        <button onClick={() => setIsSidebarOpen(!isSidebarOpen)} className="text-slate-300 p-2">
          {isSidebarOpen ? <X className="w-6 h-6" /> : <Menu className="w-6 h-6" />}
        </button>
      </div>

      {/* Sidebar Overlay for Mobile */}
      {isSidebarOpen && (
        <div 
          className="fixed inset-0 bg-black/60 backdrop-blur-sm z-40 lg:hidden"
          onClick={() => setIsSidebarOpen(false)}
        />
      )}

      {/* Sidebar */}
      <aside className={`fixed lg:static top-0 left-0 bottom-0 w-64 bg-[#0f0a1c] lg:bg-[#0f0a1c]/80 backdrop-blur-2xl border-r border-white/5 shadow-2xl text-white p-6 flex flex-col z-50 transition-transform duration-300 ${isSidebarOpen ? 'translate-x-0' : '-translate-x-full lg:translate-x-0'}`}>
        <div className="flex justify-between items-center mb-8 hidden lg:flex">
          <h1 className="text-2xl font-bold text-cyan-400 tracking-wider uppercase">Sirbull Pet</h1>
        </div>
        <nav className="flex-1 space-y-2 overflow-y-auto pr-2">
          <NavLink to="/dashboard" className={`flex items-center p-3 rounded-xl font-bold transition-all ${isActive('/dashboard')}`}>
            <LayoutDashboard className="w-5 h-5 mr-3" /> Dashboard
          </NavLink>
          <NavLink to="/clientes" className={`flex items-center p-3 rounded-xl font-bold transition-all ${isActive('/clientes')}`}>
            <Users className="w-5 h-5 mr-3" /> Clientes
          </NavLink>
          <NavLink to="/citas" className={`flex items-center p-3 rounded-xl font-bold transition-all ${isActive('/citas')}`}>
            <Calendar className="w-5 h-5 mr-3" /> Citas
          </NavLink>
          <NavLink to="/historial" className={`flex items-center p-3 rounded-xl font-bold transition-all ${isActive('/historial')}`}>
            <Activity className="w-5 h-5 mr-3" /> Historial Clínico
          </NavLink>
          <NavLink to="/inventario" className={`flex items-center p-3 rounded-xl font-bold transition-all ${isActive('/inventario')}`}>
            <Package className="w-5 h-5 mr-3" /> Inventario
          </NavLink>
          <NavLink to="/ventas" className={`flex items-center p-3 rounded-xl font-bold transition-all ${isActive('/ventas')}`}>
            <ShoppingCart className="w-5 h-5 mr-3" /> Ventas / POS
          </NavLink>
          <RoleGate allowedRoles={['Administrador', 'Recepcionista']}>
            <NavLink to="/reportes" className={`flex items-center p-3 rounded-xl font-bold transition-all ${isActive('/reportes')}`}>
              <BarChart2 className="w-5 h-5 mr-3" /> Reportes
            </NavLink>
          </RoleGate>
          <RoleGate allowedRoles={['Administrador']}>
            <NavLink to="/auditoria" className={`flex items-center p-3 rounded-xl font-bold transition-all ${isActive('/auditoria')}`}>
              <ClipboardList className="w-5 h-5 mr-3" /> Auditoría
            </NavLink>
          </RoleGate>
        </nav>
        <div className="mt-4 pt-6 border-t border-white/10 flex items-center justify-between">
          <p className="text-sm font-bold text-slate-500 uppercase tracking-widest">Panel Staff</p>
          <button 
            onClick={() => {
              useAuthStore.getState().logout()
              window.location.href = '/'
            }}
            className="p-2 text-slate-400 hover:text-red-400 hover:bg-red-500/10 rounded-lg transition-colors"
            title="Cerrar sesión"
          >
            <LogOut className="w-5 h-5" />
          </button>
        </div>
      </aside>

      {/* Main Content Area */}
      <main className="flex-1 lg:p-8 pt-20 p-4 lg:pt-8 h-screen overflow-y-auto relative">
        {/* Background Ambient Effect for the entire dashboard */}
        <div className="absolute top-0 right-0 w-[500px] h-[500px] bg-cyan-500/5 rounded-full blur-[120px] pointer-events-none -z-10"></div>
        {children}
      </main>
    </div>
  )
}