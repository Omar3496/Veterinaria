import { useEffect, useState } from 'react'
import { BrowserRouter, Routes, Route, Link, useLocation, Navigate } from 'react-router-dom'
import api from './api/axiosConfig'
import { useAuthStore } from './store/authStore'
import { Activity, Calendar, Users, Package, LayoutDashboard, LogOut, ShoppingCart, BarChart2, ClipboardList, Menu, X, FlaskConical } from 'lucide-react'
import Login from './pages/Login'
import Clientes from './pages/Clientes'
import Citas from './pages/Citas'
import Inventario from './pages/Inventario'
import Historial from './pages/Historial'
import Ventas from './pages/Ventas'
import Reportes from './pages/Reportes'
import Auditoria from './pages/Auditoria'
import Usuarios from './pages/Usuarios'
import ExamenesAuxiliares from './pages/ExamenesAuxiliares'
import Landing from './pages/Landing'
import PortalCliente from './pages/PortalCliente'
import ResetPassword from './pages/ResetPassword'
import BlogList from './pages/BlogList'
import BlogPost from './pages/BlogPost'
import StoreList from './pages/StoreList'
import Dashboard from './pages/Dashboard'
import { RoleGate } from './components/RoleGate'
import { ToastProvider } from './components/ui/ToastProvider'
import PageTransition from './components/ui/PageTransition'

// Layout Component with Responsive Sidebar
function Layout({ children }) {
  const location = useLocation();
  const [isSidebarOpen, setIsSidebarOpen] = useState(false);
  
  const isActive = (path) => location.pathname === path ? "bg-indigo-50 text-indigo-700" : "hover:bg-slate-100 text-slate-600";

  // Auto-close sidebar on mobile when route changes
  useEffect(() => {
    setIsSidebarOpen(false);
  }, [location.pathname]);

  return (
    <div className="min-h-screen flex relative bg-slate-50 selection:bg-indigo-500 selection:text-white">
      
      {/* Mobile Top Bar */}
      <div className="lg:hidden fixed top-0 left-0 right-0 h-16 bg-white/90 backdrop-blur-md border-b border-slate-200 flex items-center justify-between px-4 z-40">
        <img src="/logo-dark.png" alt="Sirbull Pet" className="h-10 w-auto object-contain" />
        <button onClick={() => setIsSidebarOpen(!isSidebarOpen)} className="text-slate-600 p-2">
          {isSidebarOpen ? <X className="w-6 h-6" /> : <Menu className="w-6 h-6" />}
        </button>
      </div>

      {/* Sidebar Overlay for Mobile */}
      {isSidebarOpen && (
        <div 
          className="fixed inset-0 bg-slate-900/40 backdrop-blur-sm z-40 lg:hidden"
          onClick={() => setIsSidebarOpen(false)}
        />
      )}

      {/* Sidebar */}
      <aside className={`fixed lg:static top-0 left-0 bottom-0 w-64 bg-white lg:bg-white/80 backdrop-blur-2xl border-r border-slate-200 shadow-sm text-slate-700 p-6 flex flex-col z-50 transition-transform duration-300 ${isSidebarOpen ? 'translate-x-0' : '-translate-x-full lg:translate-x-0'}`}>
        <div className="flex justify-between items-center mb-8 hidden lg:flex">
          <img src="/logo-dark.png" alt="Sirbull Pet" className="h-12 w-auto object-contain" />
        </div>
        <nav className="flex-1 space-y-2 overflow-y-auto pr-2">
          <Link to="/dashboard" className={`flex items-center p-3 rounded-xl font-bold transition-all ${isActive('/dashboard')}`}>
            <LayoutDashboard className="w-5 h-5 mr-3" /> Dashboard
          </Link>
          <Link to="/clientes" className={`flex items-center p-3 rounded-xl font-bold transition-all ${isActive('/clientes')}`}>
            <Users className="w-5 h-5 mr-3" /> Clientes
          </Link>
          <Link to="/citas" className={`flex items-center p-3 rounded-xl font-bold transition-all ${isActive('/citas')}`}>
            <Calendar className="w-5 h-5 mr-3" /> Citas
          </Link>
          <Link to="/historial" className={`flex items-center p-3 rounded-xl font-bold transition-all ${isActive('/historial')}`}>
            <Activity className="w-5 h-5 mr-3" /> Historial Clínico
          </Link>
          <Link to="/examenes-auxiliares" className={`flex items-center p-3 rounded-xl font-bold transition-all ${isActive('/examenes-auxiliares')}`}>
            <FlaskConical className="w-5 h-5 mr-3" /> Exámenes Auxiliares
          </Link>
          <Link to="/inventario" className={`flex items-center p-3 rounded-xl font-bold transition-all ${isActive('/inventario')}`}>
            <Package className="w-5 h-5 mr-3" /> Inventario
          </Link>
          <Link to="/ventas" className={`flex items-center p-3 rounded-xl font-bold transition-all ${isActive('/ventas')}`}>
            <ShoppingCart className="w-5 h-5 mr-3" /> Ventas / POS
          </Link>
          <RoleGate allowedRoles={['Administrador', 'Recepcionista']}>
            <Link to="/reportes" className={`flex items-center p-3 rounded-xl font-bold transition-all ${isActive('/reportes')}`}>
              <BarChart2 className="w-5 h-5 mr-3" /> Reportes
            </Link>
          </RoleGate>
          <RoleGate allowedRoles={['Administrador']}>
            <Link to="/auditoria" className={`flex items-center p-3 rounded-xl font-bold transition-all ${isActive('/auditoria')}`}>
              <ClipboardList className="w-5 h-5 mr-3" /> Auditoría
            </Link>
          </RoleGate>
          <RoleGate allowedRoles={['Administrador']}>
            <Link to="/usuarios" className={`flex items-center p-3 rounded-xl font-bold transition-all ${isActive('/usuarios')}`}>
              <Users className="w-5 h-5 mr-3" /> Usuarios
            </Link>
          </RoleGate>
        </nav>
        <div className="mt-4 pt-6 border-t border-slate-100 flex items-center justify-between">
          <p className="text-sm font-bold text-slate-500 uppercase tracking-widest">Panel Staff</p>
          <button 
            onClick={() => {
              useAuthStore.getState().logout()
              window.location.href = '/'
            }}
            className="p-2 text-slate-400 hover:text-rose-600 hover:bg-rose-50 rounded-lg transition-colors"
            title="Cerrar sesión"
          >
            <LogOut className="w-5 h-5" />
          </button>
        </div>
      </aside>

      {/* Main Content Area */}
      <main className="flex-1 lg:p-8 pt-20 p-4 lg:pt-8 h-screen overflow-y-auto relative">
        {/* Background Ambient Effect for the entire dashboard */}
        <div className="absolute top-0 right-0 w-[500px] h-[500px] bg-indigo-500/5 rounded-full blur-[120px] pointer-events-none -z-10"></div>
        {children}
      </main>
    </div>
  )
}

function ProtectedRoute({ children }) {
  const isAuthenticated = useAuthStore(state => state.isAuthenticated);
  return isAuthenticated ? children : <Navigate to="/login" />;
}

// Scroll to top on every route change (except scroll-to-section state)
function ScrollToTop() {
  const location = useLocation();
  useEffect(() => {
    if (location.state?.scrollTo === 'nosotros') return;
    window.scrollTo(0, 0);
  }, [location.pathname]);
  return null;
}

// Router Component
function AnimatedRoutes() {
  const location = useLocation();

  return (
    <>
      <ScrollToTop />
      <Routes location={location}>
      <Route path="/login" element={<PageTransition><Login /></PageTransition>} />
      <Route path="/reset-password" element={<PageTransition><ResetPassword /></PageTransition>} />
      <Route path="/" element={<PageTransition><Landing /></PageTransition>} />
      <Route path="/blog" element={<PageTransition><BlogList /></PageTransition>} />
      <Route path="/blog/:slug" element={<PageTransition><BlogPost /></PageTransition>} />
      <Route path="/tienda" element={<PageTransition><StoreList /></PageTransition>} />

      <Route path="/portal" element={
        <ProtectedRoute>
          <RoleGate allowedRoles={['Cliente']}>
            <PageTransition><PortalCliente /></PageTransition>
          </RoleGate>
        </ProtectedRoute>
      } />

      <Route path="/dashboard" element={
        <ProtectedRoute>
          <RoleGate allowedRoles={['Administrador', 'Veterinario', 'Recepcionista']}>
            <Layout><PageTransition><Dashboard /></PageTransition></Layout>
          </RoleGate>
        </ProtectedRoute>
      } />
      <Route path="/clientes" element={
        <ProtectedRoute>
          <Layout><PageTransition><Clientes /></PageTransition></Layout>
        </ProtectedRoute>
      } />
      <Route path="/citas" element={
        <ProtectedRoute>
          <Layout><PageTransition><Citas /></PageTransition></Layout>
        </ProtectedRoute>
      } />
      <Route path="/historial" element={
        <ProtectedRoute>
          <Layout><PageTransition><Historial /></PageTransition></Layout>
        </ProtectedRoute>
      } />
      <Route path="/examenes-auxiliares" element={
        <ProtectedRoute>
          <RoleGate allowedRoles={['Administrador', 'Veterinario', 'Recepcionista']}>
            <Layout><PageTransition><ExamenesAuxiliares /></PageTransition></Layout>
          </RoleGate>
        </ProtectedRoute>
      } />
      <Route path="/inventario" element={
        <ProtectedRoute>
          <Layout><PageTransition><Inventario /></PageTransition></Layout>
        </ProtectedRoute>
      } />
      <Route path="/ventas" element={
        <ProtectedRoute>
          <Layout><PageTransition><Ventas /></PageTransition></Layout>
        </ProtectedRoute>
      } />
      <Route path="/reportes" element={
        <ProtectedRoute>
          <RoleGate allowedRoles={['Administrador', 'Recepcionista']}>
            <Layout><PageTransition><Reportes /></PageTransition></Layout>
          </RoleGate>
        </ProtectedRoute>
      } />
      <Route path="/auditoria" element={
        <ProtectedRoute>
          <Layout><PageTransition><Auditoria /></PageTransition></Layout>
        </ProtectedRoute>
      } />
      <Route path="/usuarios" element={
        <ProtectedRoute>
          <RoleGate allowedRoles={['Administrador']}>
            <Layout><PageTransition><Usuarios /></PageTransition></Layout>
          </RoleGate>
        </ProtectedRoute>
      } />
    </Routes>
    </>
  );
}

function App() {
  return (
    <ToastProvider>
      <BrowserRouter>
        <AnimatedRoutes />
      </BrowserRouter>
    </ToastProvider>
  )
}

export default App
