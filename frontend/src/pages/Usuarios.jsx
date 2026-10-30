import { useState, useEffect } from 'react'
import { useToast } from '../components/ui/ToastProvider'
import api from '../api/axiosConfig'
import { Users, Plus, Pencil, Trash2, UserCog, RefreshCw, Power } from 'lucide-react'
import PageHeader from '../components/ui/PageHeader'
import DataTable from '../components/ui/DataTable'
import Button from '../components/ui/Button'
import Input from '../components/ui/Input'
import Modal from '../components/ui/Modal'

const ROLES = ['Administrador', 'Veterinario', 'Recepcionista', 'Cliente']

const rolBadge = (rol) => {
  const styles = {
    'Administrador': 'bg-violet-50 text-violet-700 border-violet-200',
    'Veterinario': 'bg-sky-50 text-sky-700 border-sky-200',
    'Recepcionista': 'bg-amber-50 text-amber-700 border-amber-200',
    'Cliente': 'bg-emerald-50 text-emerald-700 border-emerald-200',
  }
  return (
    <span className={`inline-block px-2 py-0.5 rounded-full text-xs font-bold border ${styles[rol] || 'bg-slate-50 text-slate-600 border-slate-200'}`}>
      {rol}
    </span>
  )
}

export default function Usuarios() {
  const toast = useToast()
  const [usuarios, setUsuarios] = useState([])
  const [loading, setLoading] = useState(true)
  const [showForm, setShowForm] = useState(false)
  const [editing, setEditing] = useState(null)
  const [saving, setSaving] = useState(false)
  const [form, setForm] = useState({ username: '', rol: 'Recepcionista', password: '' })

  useEffect(() => {
    fetchUsuarios()
  }, [])

  const fetchUsuarios = async () => {
    setLoading(true)
    try {
      const res = await api.get('/api/usuarios')
      setUsuarios(res.data)
    } catch (err) {
      toast.error(err.response?.data?.detail || "No tienes permisos para ver los usuarios.")
    } finally {
      setLoading(false)
    }
  }

  const resetForm = () => {
    setForm({ username: '', rol: 'Recepcionista', password: '' })
    setEditing(null)
  }

  const openCreate = () => {
    resetForm()
    setShowForm(true)
  }

  const openEdit = (item) => {
    setEditing(item)
    setForm({ username: item.username, rol: item.rol, password: '' })
    setShowForm(true)
  }

  const handleSave = async () => {
    if (!form.username.trim()) {
      toast.error("El nombre de usuario es obligatorio.")
      return
    }
    if (!editing && !form.password) {
      toast.error("La contraseña es obligatoria.")
      return
    }
    setSaving(true)
    try {
      if (editing) {
        const payload = { username: form.username, rol: form.rol }
        if (form.password) payload.password = form.password
        await api.put(`/api/usuarios/${editing.id}`, payload)
        toast.success("Usuario actualizado correctamente.")
      } else {
        await api.post('/api/usuarios', {
          username: form.username,
          rol: form.rol,
          password: form.password,
        })
        toast.success("Usuario creado correctamente.")
      }
      setShowForm(false)
      resetForm()
      fetchUsuarios()
      setTimeout(() => toast.success(null), 4000)
    } catch (err) {
      toast.error(err.response?.data?.detail || "Error al guardar el usuario.")
      setTimeout(() => toast.error(null), 4000)
    } finally {
      setSaving(false)
    }
  }

  const handleToggleActive = async (item) => {
    try {
      await api.put(`/api/usuarios/${item.id}`, { is_active: !item.is_active })
      toast.success(item.is_active ? "Usuario desactivado." : "Usuario activado.")
      fetchUsuarios()
      setTimeout(() => toast.success(null), 4000)
    } catch (err) {
      toast.error(err.response?.data?.detail || "No se pudo cambiar el estado del usuario.")
      setTimeout(() => toast.error(null), 4000)
    }
  }

  const handleDelete = async (item) => {
    if (!window.confirm(`¿Eliminar el usuario "${item.username}"?`)) return
    try {
      await api.delete(`/api/usuarios/${item.id}`)
      toast.success("Usuario eliminado.")
      fetchUsuarios()
      setTimeout(() => toast.success(null), 4000)
    } catch (err) {
      toast.error(err.response?.data?.detail || "No se pudo eliminar el usuario.")
      setTimeout(() => toast.error(null), 4000)
    }
  }

  const columns = [
    { header: 'ID', accessor: 'id', render: (item) => <span className="text-indigo-600/70 font-mono text-xs">#{item.id}</span> },
    { header: 'Usuario', accessor: 'username', render: (item) => (
        <span className="font-bold text-slate-900 flex items-center gap-2">
          <UserCog className="w-4 h-4 text-slate-400" /> {item.username}
        </span>
      )
    },
    { header: 'Rol', accessor: 'rol', render: (item) => rolBadge(item.rol) },
    { header: 'Estado', accessor: 'is_active', render: (item) => (
        item.is_active
          ? <span className="inline-block px-2 py-0.5 rounded-full text-xs font-bold border bg-emerald-50 text-emerald-700 border-emerald-200">Activo</span>
          : <span className="inline-block px-2 py-0.5 rounded-full text-xs font-bold border bg-slate-100 text-slate-500 border-slate-200">Inactivo</span>
      )
    },
    { header: 'Acciones', accessor: 'acciones', render: (item) => (
        <div className="flex items-center space-x-2">
          <button onClick={() => openEdit(item)} className="text-indigo-600 hover:text-indigo-500 bg-indigo-50 hover:bg-indigo-100 p-2 rounded-xl transition-colors" title="Editar Usuario">
            <Pencil className="w-4 h-4" />
          </button>
          <button onClick={() => handleToggleActive(item)} className="text-amber-600 hover:text-amber-500 bg-amber-50 hover:bg-amber-100 p-2 rounded-xl transition-colors" title={item.is_active ? 'Desactivar' : 'Activar'}>
            <Power className="w-4 h-4" />
          </button>
          <button onClick={() => handleDelete(item)} className="text-rose-600 hover:text-rose-600 bg-rose-50 hover:bg-rose-100 p-2 rounded-xl transition-colors" title="Eliminar Usuario">
            <Trash2 className="w-4 h-4" />
          </button>
        </div>
      )
    },
  ]

  return (
    <div className="space-y-6 animate-fade-in-up">
      <PageHeader
        title="Gestión de Usuarios"
        description="Crear, modificar y desactivar cuentas de acceso al sistema"
        actions={
          <div className="flex items-center gap-2">
            <Button onClick={fetchUsuarios} variant="secondary" isLoading={loading}>
              <RefreshCw className={`w-4 h-4 mr-2 ${loading ? 'animate-spin' : ''}`} />
              Refrescar
            </Button>
            <Button onClick={openCreate} variant="primary">
              <Plus className="w-4 h-4 mr-2" /> Nuevo Usuario
            </Button>
          </div>
        }
      />

      <DataTable
        columns={columns}
        data={usuarios}
        emptyMessage="No hay usuarios registrados."
      />

      <Modal isOpen={showForm} onClose={() => setShowForm(false)} title={editing ? 'Editar Usuario' : 'Nuevo Usuario'}>
        <div className="space-y-4">
          <Input
            label="Nombre de usuario"
            required
            value={form.username}
            onChange={(e) => setForm({ ...form, username: e.target.value })}
            placeholder="ej: jperez"
          />
          <div>
            <label className="block text-xs font-bold text-slate-500 uppercase tracking-widest mb-2">Rol</label>
            <select
              value={form.rol}
              onChange={(e) => setForm({ ...form, rol: e.target.value })}
              className="w-full px-4 py-3 bg-white border border-slate-200 rounded-xl text-slate-900 focus:outline-none focus:border-[#047857] focus:ring-1 focus:ring-[#047857]/20"
            >
              {ROLES.map((r) => (
                <option key={r} value={r}>{r}</option>
              ))}
            </select>
          </div>
          <Input
            label={editing ? "Nueva contraseña (dejar vacío para no cambiar)" : "Contraseña"}
            type="password"
            required={!editing}
            value={form.password}
            onChange={(e) => setForm({ ...form, password: e.target.value })}
            placeholder="••••••••"
          />
          <div className="flex items-center justify-end gap-2 pt-2">
            <Button variant="ghost" onClick={() => setShowForm(false)}>Cancelar</Button>
            <Button variant="primary" onClick={handleSave} isLoading={saving}>
              {editing ? 'Guardar Cambios' : 'Crear Usuario'}
            </Button>
          </div>
        </div>
      </Modal>
    </div>
  )
}
