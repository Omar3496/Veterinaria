import { useEffect, useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { useAuthStore } from '../store/authStore'
import { MapPin, Mail, Smartphone, Menu, X, LogOut, User, ShoppingCart } from 'lucide-react'

export default function SiteHeader({
  variant = 'overlay',
  showContactBar = false,
  showCart = false,
  cartItemsCount = 0,
  onCartOpen = () => {},
}) {
  const [scrolled, setScrolled] = useState(false)
  const [showMobileMenu, setShowMobileMenu] = useState(false)
  const [activeSection, setActiveSection] = useState('')
  const location = useLocation()
  const navigate = useNavigate()

  const isAuthenticated = useAuthStore((state) => state.isAuthenticated)
  const role = useAuthStore((state) => state.role)
  const dashboardLink = role === 'Cliente' ? '/portal' : '/dashboard'

  const solid = variant === 'solid' || scrolled
  const isOverlay = !solid

  useEffect(() => {
    const onScroll = () => setScrolled(window.scrollY > 40)
    onScroll()
    window.addEventListener('scroll', onScroll, { passive: true })
    return () => window.removeEventListener('scroll', onScroll)
  }, [])

  useEffect(() => {
    if (location.pathname !== '/') {
      setActiveSection('')
      return
    }
    const section = document.getElementById('nosotros')
    if (!section) return
    const onScroll = () => {
      const rect = section.getBoundingClientRect()
      setActiveSection(rect.top <= window.innerHeight * 0.55 && rect.bottom > 0 ? 'nosotros' : '')
    }
    onScroll()
    window.addEventListener('scroll', onScroll, { passive: true })
    return () => window.removeEventListener('scroll', onScroll)
  }, [location.pathname])

  useEffect(() => {
    setShowMobileMenu(false)
  }, [location.pathname])

  const handleLogout = () => {
    useAuthStore.getState().logout()
    window.location.href = '/'
  }

  const isActive = (path) => {
    if (path === '/') return location.pathname === '/'
    return location.pathname === path || location.pathname.startsWith(path + '/')
  }

  const goToNosotros = () => {
    setShowMobileMenu(false)
    if (location.pathname === '/') {
      document.getElementById('nosotros')?.scrollIntoView({ behavior: 'smooth' })
    } else {
      navigate('/', { state: { scrollTo: 'nosotros' } })
    }
  }

  const goToHome = () => {
    setShowMobileMenu(false)
    if (location.pathname === '/') {
      window.scrollTo({ top: 0, behavior: 'smooth' })
    }
  }

  const linkClass = (path) => `nav-link ${isActive(path) ? 'nav-active' : ''}`

  const nosotrosActive = location.pathname === '/' && activeSection === 'nosotros'
  const inicioActive = location.pathname === '/' && !nosotrosActive

  const isPanelActive = isAuthenticated && isActive(dashboardLink)

  const ctaButtonClass = (active) =>
    `inline-flex items-center gap-2 px-5 py-2.5 rounded-full font-bold text-sm border-2 transition-all ${
      active
        ? 'border-[#047857] bg-white text-[#047857] shadow-md hover:bg-[#047857]/5'
        : 'border-[#047857] bg-[#047857] text-white shadow-md hover:bg-[#065F46] hover:border-[#065F46] hover:shadow-lg hover:-translate-y-0.5'
    }`

  return (
    <>
      <header className={`fixed top-0 inset-x-0 z-50 transition-all duration-500 ${solid ? 'header-scrolled bg-white/95 backdrop-blur-md shadow-lg shadow-slate-900/5 border-b border-slate-200' : 'bg-transparent border-b border-transparent'}`}>

        {/* Contact bar (solo top, pantallas anchas) */}
        {showContactBar && (
          <div className={`hidden lg:block overflow-hidden transition-all duration-500 ${scrolled ? 'max-h-0 opacity-0' : 'max-h-14 opacity-100 bg-[#0b0a12]/95 border-b border-white/10'}`}>
            <div className="max-w-7xl mx-auto px-10 sm:px-16 lg:px-24 flex items-center justify-between py-2.5 text-sm">
              <div className="flex items-center gap-6">
                <span className="flex items-center gap-2 text-slate-300">
                  <MapPin className="w-4 h-4 text-[#047857]" /> Pet Street 123 - Nueva York
                </span>
                <a href="mailto:contacto@sirbullpet.com" className="flex items-center gap-2 text-slate-300 hover:text-white transition-colors">
                  <Mail className="w-4 h-4 text-[#047857]" /> correo electrónico@tudominio.com
                </a>
                <a href="tel:123456789" className="flex items-center gap-2 text-slate-300 hover:text-white transition-colors">
                  <Smartphone className="w-4 h-4 text-[#047857]" /> (123) 456-789
                </a>
              </div>
              <div className="flex items-center gap-4">
                <a href="#" aria-label="Facebook" className="text-slate-300 hover:text-white transition-colors">
                  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M18 2h-3a5 5 0 0 0-5 5v3H7v4h3v8h4v-8h3l1-4h-4V7a1 1 0 0 1 1-1h3z"></path></svg>
                </a>
                <a href="#" aria-label="Twitter" className="text-slate-300 hover:text-white transition-colors">
                  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M23 3a10.9 10.9 0 0 1-3.14 1.53 4.48 4.48 0 0 0-7.86 3v1A10.66 10.66 0 0 1 3 4s-4 9 5 13a11.64 11.64 0 0 1-7 2c9 5 20 0 20-11.5a4.5 4.5 0 0 0-.08-.83A7.72 7.72 0 0 0 23 3z"></path></svg>
                </a>
                <a href="#" aria-label="Instagram" className="text-slate-300 hover:text-white transition-colors">
                  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><rect x="2" y="2" width="20" height="20" rx="5" ry="5"></rect><path d="M16 11.37A4 4 0 1 1 12.63 8 4 4 0 0 1 16 11.37z"></path><line x1="17.5" y1="6.5" x2="17.51" y2="6.5"></line></svg>
                </a>
              </div>
            </div>
          </div>
        )}

        {/* Navbar */}
        <nav className={`transition-all duration-500 ${solid ? 'py-2' : 'pt-6 pb-4'}`}>
          <div className="max-w-7xl mx-auto px-5 sm:px-16 lg:px-24">
            <div className={`flex justify-between items-center ${solid ? 'h-16 sm:h-20' : 'h-20 sm:h-24'}`}>

              {/* Logo */}
              <Link to="/" onClick={goToHome} className="flex items-center animate-slide-in-left cursor-pointer">
                <img
                  src={isOverlay ? '/logo.png' : '/logo-dark.png'}
                  alt="Sirbull Pet"
                  className={`w-auto object-contain transition-all duration-500 ${solid ? 'h-12 sm:h-16' : 'h-20 sm:h-28'}`}
                />
              </Link>

              {/* Links & CTA */}
              <div className="hidden md:flex items-center gap-8 animate-fade-in-up">
                <Link to="/" onClick={goToHome} className={`nav-link ${inicioActive ? 'nav-active' : ''}`}>Inicio</Link>
                <Link to="/tienda" className={linkClass('/tienda')}>Tienda</Link>
                <Link to="/blog" className={linkClass('/blog')}>Blog</Link>
                <button onClick={goToNosotros} className={`nav-link cursor-pointer ${nosotrosActive ? 'nav-active' : ''}`}>Sobre Nosotros</button>

                {showCart && (
                  <button
                    onClick={onCartOpen}
                    aria-label="Carrito de compras"
                    className={`relative transition-colors ${isOverlay ? 'text-white hover:text-[#047857]' : 'text-slate-700 hover:text-[#047857]'}`}
                  >
                    <ShoppingCart className="w-6 h-6" />
                    {cartItemsCount > 0 && (
                      <span className="absolute -top-2 -right-2 bg-[#047857] text-white text-xs font-bold w-5 h-5 flex items-center justify-center rounded-full">
                        {cartItemsCount}
                      </span>
                    )}
                  </button>
                )}

                {isAuthenticated ? (
                  <div className="ml-2 flex items-center gap-2">
                    <Link to={dashboardLink} className={ctaButtonClass(isPanelActive)}>
                      <User className="w-4 h-4" /> Mi Cuenta
                    </Link>
                    <button
                      onClick={handleLogout}
                      title="Cerrar sesión"
                      className={`inline-flex items-center justify-center w-10 h-10 rounded-full border transition-all ${isOverlay ? 'bg-white/5 border-white/15 text-white hover:text-red-400 hover:border-red-400/40 hover:bg-red-500/10' : 'bg-slate-900/5 border-slate-200 text-slate-700 hover:text-red-500 hover:border-red-400/50 hover:bg-red-50'}`}
                    >
                      <LogOut className="w-4 h-4" />
                    </button>
                  </div>
                ) : (
                  <Link to="/login" className={`ml-2 ${ctaButtonClass(false)}`}>
                    <User className="w-4 h-4" /> Iniciar Sesión
                  </Link>
                )}
              </div>

              {/* Mobile: cart + menu */}
              <div className="md:hidden flex items-center space-x-3">
                {showCart && (
                  <button
                    onClick={onCartOpen}
                    className={`relative p-2 ${isOverlay ? 'text-white' : 'text-slate-900'}`}
                    aria-label="Carrito de compras"
                  >
                    <ShoppingCart className="w-6 h-6" />
                    {cartItemsCount > 0 && (
                      <span className="absolute -top-1 -right-1 bg-[#047857] text-white text-xs font-bold w-5 h-5 flex items-center justify-center rounded-full">
                        {cartItemsCount}
                      </span>
                    )}
                  </button>
                )}
                <button
                  onClick={() => setShowMobileMenu(true)}
                  className={`p-2 sm:p-2.5 rounded-lg border transition-colors ${isOverlay ? 'bg-white/5 border-white/10 text-white hover:bg-white/10' : 'bg-slate-900/5 border-slate-200 text-slate-900 hover:bg-slate-100'}`}
                  aria-label="Abrir menú"
                >
                  <Menu className="w-6 h-6 sm:w-7 sm:h-7" />
                </button>
              </div>
            </div>
          </div>
        </nav>
      </header>

      {/* Mobile Menu Overlay */}
      {showMobileMenu && (
        <div className={`fixed inset-0 z-[200] backdrop-blur-xl flex flex-col animate-fade-in ${isOverlay ? 'bg-[#06030F]/95' : 'bg-white/95'}`}>
          <div className={`flex justify-between items-center p-6 border-b ${isOverlay ? 'border-white/10' : 'border-slate-100'}`}>
            <img src={isOverlay ? '/logo.png' : '/logo-dark.png'} alt="Sirbull Pet" className="h-10 w-auto object-contain" />
            <button
              onClick={() => setShowMobileMenu(false)}
              className={`p-2 rounded-full border transition-colors ${isOverlay ? 'bg-white/5 border-white/10 text-slate-400 hover:text-white' : 'bg-slate-100 border-slate-200 text-slate-500 hover:text-slate-900'}`}
              aria-label="Cerrar menú"
            >
              <X className="w-6 h-6" />
            </button>
          </div>

          <div className="flex flex-col p-8 space-y-6 flex-grow overflow-y-auto">
            <Link to="/" onClick={goToHome} className={`text-xl font-bold border-b pb-4 ${inicioActive ? 'text-[#047857] border-[#047857]/20' : isOverlay ? 'text-slate-300 hover:text-white border-white/5' : 'text-slate-700 hover:text-[#047857] border-slate-100'}`}>
              Inicio
            </Link>
            <Link to="/tienda" onClick={() => setShowMobileMenu(false)} className={`text-xl font-bold border-b pb-4 ${isActive('/tienda') ? 'text-[#047857] border-[#047857]/20' : isOverlay ? 'text-slate-300 hover:text-white border-white/5' : 'text-slate-700 hover:text-[#047857] border-slate-100'}`}>
              Tienda
            </Link>
            <Link to="/blog" onClick={() => setShowMobileMenu(false)} className={`text-xl font-bold border-b pb-4 ${isActive('/blog') ? 'text-[#047857] border-[#047857]/20' : isOverlay ? 'text-slate-300 hover:text-white border-white/5' : 'text-slate-700 hover:text-[#047857] border-slate-100'}`}>
              Blog
            </Link>
            <button
              onClick={goToNosotros}
              className={`text-xl font-bold border-b pb-4 text-left cursor-pointer ${nosotrosActive ? 'text-[#047857] border-[#047857]/20' : isOverlay ? 'text-slate-300 hover:text-white border-white/5' : 'text-slate-700 hover:text-[#047857] border-slate-100'}`}
            >
              Sobre Nosotros
            </button>

            {showCart && (
              <button onClick={() => { setShowMobileMenu(false); onCartOpen(); }} className="w-full flex items-center justify-center gap-2 py-4 rounded-xl bg-[#047857] text-white font-bold hover:bg-[#065F46] transition-colors">
                <ShoppingCart className="w-5 h-5" /> Ver Carrito
              </button>
            )}

            <div className="pt-8 space-y-4">
              {isAuthenticated ? (
                <>
                  <Link to={dashboardLink} onClick={() => setShowMobileMenu(false)} className={`w-full flex items-center justify-center gap-2 py-4 rounded-xl border-2 font-bold transition-colors ${isPanelActive ? 'bg-white border-[#047857] text-[#047857] hover:bg-[#047857]/5' : 'bg-[#047857] border-[#047857] text-white hover:bg-[#065F46]'}`}>
                    <User className="w-5 h-5" /> Mi Cuenta
                  </Link>
                  <button onClick={handleLogout} className={`w-full flex items-center justify-center gap-2 py-4 rounded-xl border font-bold transition-colors ${isOverlay ? 'bg-slate-900 border-white/10 text-slate-300 hover:text-red-400 hover:border-red-400/30 hover:bg-red-500/10' : 'bg-white border-slate-200 text-slate-700 hover:text-red-500 hover:border-red-400/50 hover:bg-red-50'}`}>
                    <LogOut className="w-5 h-5" /> Cerrar Sesión
                  </button>
                </>
              ) : (
                <Link to="/login" className="w-full flex items-center justify-center gap-2 py-4 rounded-xl bg-[#047857] text-white font-bold hover:bg-[#065F46] transition-colors">
                  <User className="w-5 h-5" /> Iniciar Sesión
                </Link>
              )}
            </div>
          </div>
        </div>
      )}
    </>
  )
}
