import { useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import api from '../api/axiosConfig'
import { ArrowLeft, Lock, KeyRound, CheckCircle2 } from 'lucide-react'
import Input from '../components/ui/Input'

export default function ResetPassword() {
  const [searchParams] = useSearchParams()
  const token = searchParams.get('token') || ''
  const [newPassword, setNewPassword] = useState('')
  const [confirm, setConfirm] = useState('')
  const [error, setError] = useState('')
  const [msg, setMsg] = useState('')
  const [loading, setLoading] = useState(false)

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setMsg('')

    if (!token) {
      setError('El enlace de recuperación es inválido o está incompleto.')
      return
    }
    if (newPassword.length < 6) {
      setError('La contraseña debe tener al menos 6 caracteres.')
      return
    }
    if (newPassword !== confirm) {
      setError('Las contraseñas no coinciden.')
      return
    }

    setLoading(true)
    try {
      const res = await api.post('/api/reset-password', { token, new_password: newPassword })
      setMsg(res.data.detail || 'Contraseña actualizada correctamente.')
    } catch (err) {
      setError(err.response?.data?.detail || 'Error al restablecer la contraseña.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen relative overflow-hidden bg-white font-sans selection:bg-[#047857] selection:text-white">
      <img
        src="/bannerlogin.png"
        alt=""
        aria-hidden="true"
        className="absolute inset-0 w-full h-full object-cover object-left"
      />

      <div className="relative z-10 min-h-screen flex flex-col">
        <header className="flex items-center justify-between px-6 sm:px-10 py-5 max-w-[560px]">
          <img src="/logo-dark.png" alt="SIRBULL PET" className="h-16 sm:h-20 w-auto object-contain" />
          <Link to="/" className="flex items-center text-slate-400 hover:text-slate-900 transition-colors text-sm font-bold">
            <ArrowLeft className="w-4 h-4 mr-2" /> Volver
          </Link>
        </header>

        <div className="flex-1 flex items-center justify-center lg:justify-start px-6 sm:px-10 pb-12">
          <div className="w-full max-w-md animate-fade-in-up">
            <div className="mb-8 flex items-center justify-center gap-3">
              <div className="w-11 h-11 rounded-2xl bg-[#047857] text-white flex items-center justify-center">
                <KeyRound className="w-6 h-6" />
              </div>
              <div>
                <h1 className="text-2xl font-extrabold text-slate-900 tracking-tight">Nueva Contraseña</h1>
                <p className="text-sm text-slate-500">Establece una contraseña nueva para tu cuenta</p>
              </div>
            </div>

            <form onSubmit={handleSubmit} className="space-y-5">
              <Input
                label="Nueva contraseña"
                type="password"
                value={newPassword}
                onChange={(e) => setNewPassword(e.target.value)}
                required
                icon={Lock}
                labelClassName="text-sm font-medium text-slate-600 normal-case tracking-normal"
                placeholder="Mínimo 6 caracteres"
              />

              <Input
                label="Confirmar contraseña"
                type="password"
                value={confirm}
                onChange={(e) => setConfirm(e.target.value)}
                required
                icon={Lock}
                labelClassName="text-sm font-medium text-slate-600 normal-case tracking-normal"
                placeholder="Repite la contraseña"
              />

              {error && (
                <div className="p-3 rounded-lg bg-red-50 text-red-600 text-sm font-medium border border-red-200 flex items-center">
                  <div className="w-2 h-2 rounded-full bg-red-500 mr-3"></div>
                  {error}
                </div>
              )}

              {msg && (
                <div className="p-3 rounded-lg bg-emerald-50 text-emerald-700 text-sm font-medium border border-emerald-200">
                  <div className="flex items-center mb-2">
                    <CheckCircle2 className="w-5 h-5 mr-2" /> {msg}
                  </div>
                  <Link to="/login" className="inline-block w-full text-center mt-1 py-2.5 rounded-xl bg-[#047857] text-white text-sm font-bold hover:bg-[#065F46] transition-colors">
                    Iniciar Sesión
                  </Link>
                </div>
              )}

              {!msg && (
                <button
                  type="submit"
                  disabled={loading}
                  className="w-full py-3.5 rounded-xl bg-[#047857] text-white font-bold uppercase tracking-wide text-sm hover:bg-[#065F46] transition-colors flex items-center justify-center gap-2 disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  {loading ? (
                    <>
                      <span className="animate-spin rounded-full h-5 w-5 border-2 border-white border-b-transparent"></span>
                      Guardando...
                    </>
                  ) : (
                    'Restablecer Contraseña'
                  )}
                </button>
              )}
            </form>

            {!msg && (
              <div className="text-center mt-6">
                <Link to="/login" className="text-xs font-bold text-[#047857] hover:text-[#065F46] transition-colors">
                  ¿Ya recordaste tu contraseña? Volver al inicio de sesión
                </Link>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  )
}
