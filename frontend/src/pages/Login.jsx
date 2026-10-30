import { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { useAuthStore } from '../store/authStore'
import api from '../api/axiosConfig'
import { ArrowLeft, Mail, Lock, User, LogIn } from 'lucide-react'
import Input from '../components/ui/Input'
import Modal from '../components/ui/Modal'

export default function Login() {
  const [username, setUsername] = useState('admin')
  const [password, setPassword] = useState('admin123')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const [showForgotModal, setShowForgotModal] = useState(false)
  const [forgotEmail, setForgotEmail] = useState('')
  const [forgotMsg, setForgotMsg] = useState('')
  const [forgotLoading, setForgotLoading] = useState(false)
  const navigate = useNavigate()
  
  const login = useAuthStore(state => state.login)

  const handleSubmit = async (e) => {
    e.preventDefault()
    setLoading(true)
    setError('')
    try {
      const formData = new URLSearchParams()
      formData.append('username', username)
      formData.append('password', password)

      const res = await api.post('/token', formData, {
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      })

      const token = res?.data?.access_token
      if (!token) {
        throw new Error('No se recibió token')
      }

      login(token)

      const role = useAuthStore.getState().role
      if (role === 'Cliente') {
        navigate('/portal', { replace: true })
      } else {
        navigate('/dashboard', { replace: true })
      }
    } catch (err) {
      console.error('Login error', err)
      setError('Credenciales incorrectas o acceso denegado.')
    } finally {
      setLoading(false)
    }
  }

  const handleForgotSubmit = async (e) => {
    e.preventDefault()
    setForgotLoading(true)
    setForgotMsg('')
    try {
      const res = await api.post('/api/recuperar-password', { email: forgotEmail })
      const data = res.data
      if (data?.reset_url) {
        window.location.href = data.reset_url
      } else {
        setForgotMsg(data.detail || 'Si el correo está registrado, recibirás un enlace.')
      }
    } catch (err) {
      setForgotMsg('Error al procesar la solicitud.')
    } finally {
      setForgotLoading(false)
    }
  }

  return (
    <div className="login-page min-h-screen relative overflow-hidden bg-white font-sans selection:bg-[#047857] selection:text-white">
      
      {/* Background Image */}
      <img
        src="/bannerlogin.png"
        alt=""
        aria-hidden="true"
        className="absolute inset-0 w-full h-full object-cover object-left"
      />

      {/* Content */}
      <div className="relative z-10 min-h-screen flex flex-col">
        
        {/* Header */}
        <header className="flex items-center justify-between px-6 sm:px-10 py-5 max-w-[560px]">
          <img src="/logo-dark.png" alt="SIRBULL PET" className="h-16 sm:h-20 w-auto object-contain" />
          <Link to="/" className="flex items-center text-slate-400 hover:text-slate-900 transition-colors text-sm font-bold">
            <ArrowLeft className="w-4 h-4 mr-2" /> Volver
          </Link>
        </header>

        {/* Form Area (left side of banner) */}
        <div className="flex-1 flex items-center justify-center lg:justify-start px-6 sm:px-10 pb-12">
          <div className="w-full max-w-md animate-fade-in-up">
            
            <div className="mb-8">
              <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight text-center">Iniciar Sesión</h1>
            </div>

            <form onSubmit={handleSubmit} className="space-y-5">
              <Input
                label="Usuario o correo electrónico"
                type="text"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                required
                icon={User}
                labelClassName="text-sm font-medium text-slate-600 normal-case tracking-normal"
                placeholder="usuario o tucorreo@ejemplo.com"
              />
              
              <Input
                label="Contraseña"
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
                icon={Lock}
                labelClassName="text-sm font-medium text-slate-600 normal-case tracking-normal"
                placeholder="Ingresa tu contraseña"
              />

              {error && (
                <div className="p-3 rounded-lg bg-red-50 text-red-600 text-sm font-medium border border-red-200 flex items-center">
                  <div className="w-2 h-2 rounded-full bg-red-500 mr-3"></div>
                  {error}
                </div>
              )}

              <div className="flex justify-end">
                <button 
                  type="button"
                  onClick={() => setShowForgotModal(true)}
                  className="text-xs font-bold text-[#047857] hover:text-[#065F46] transition-colors outline-none"
                >
                  ¿Olvidaste tu contraseña?
                </button>
              </div>

              <button
                type="submit"
                disabled={loading}
                className="w-full py-3.5 rounded-xl bg-[#047857] text-white font-bold uppercase tracking-wide text-sm hover:bg-[#065F46] transition-colors flex items-center justify-center gap-2 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {loading ? (
                  <>
                    <span className="animate-spin rounded-full h-5 w-5 border-2 border-white border-b-transparent"></span>
                    Ingresando...
                  </>
                ) : (
                  <>
                    <LogIn className="w-5 h-5" /> Iniciar Sesión
                  </>
                )}
              </button>
            </form>

            {/* Footer info */}
            <div className="text-center mt-6 flex items-center justify-center gap-1.5 text-xs text-slate-400">
              <Lock className="w-3.5 h-3.5" /> Conexión segura de extremo a extremo
            </div>
          </div>
        </div>
      </div>

      {/* Forgot Password Modal */}
      <Modal isOpen={showForgotModal} onClose={() => setShowForgotModal(false)} title="Recuperar Contraseña">
        <form onSubmit={handleForgotSubmit} className="space-y-4">
          <Input 
            label="Ingresa tu Email"
            type="email" 
            required
            value={forgotEmail}
            onChange={e => setForgotEmail(e.target.value)}
            icon={Mail}
            placeholder="tucorreo@ejemplo.com"
          />
          {forgotMsg && (
            <div className="p-3 rounded-lg bg-emerald-50 text-emerald-700 text-sm font-medium border border-emerald-200">
              {forgotMsg}
            </div>
          )}
          <p className="text-xs text-slate-400 leading-relaxed">
            Modo demostración: si el correo existe, te redirigiremos automáticamente a la página para cambiar tu contraseña.
          </p>
          <div className="flex justify-end space-x-3 pt-4">
            <button
              type="button"
              onClick={() => setShowForgotModal(false)}
              className="px-4 py-2.5 rounded-xl bg-white hover:bg-slate-50 text-slate-600 border border-slate-200 hover:border-slate-300 text-sm font-bold transition-all"
            >
              Cancelar
            </button>
            <button
              type="submit"
              disabled={forgotLoading}
              className="px-4 py-2.5 rounded-xl bg-[#047857] text-white text-sm font-bold hover:bg-[#065F46] transition-colors flex items-center gap-2 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {forgotLoading ? (
                <>
                  <span className="animate-spin rounded-full h-4 w-4 border-2 border-white border-b-transparent"></span>
                  Enviando...
                </>
              ) : (
                'Recuperar'
              )}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  )
}
