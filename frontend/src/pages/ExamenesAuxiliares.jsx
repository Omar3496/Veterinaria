import { useState, useEffect } from 'react'
import { useToast } from '../components/ui/ToastProvider'
import api from '../api/axiosConfig'
import { FlaskConical, Plus, Pencil, Trash2, RefreshCw, Clock } from 'lucide-react'
import { RoleGate } from '../components/RoleGate'
import PageHeader from '../components/ui/PageHeader'
import DataTable from '../components/ui/DataTable'
import Button from '../components/ui/Button'
import Input from '../components/ui/Input'
import Modal from '../components/ui/Modal'

const estadoBadge = (activo) =>
  activo
    ? <span className="inline-block px-2 py-0.5 rounded-full text-xs font-bold border bg-emerald-50 text-emerald-700 border-emerald-200">Activo</span>
    : <span className="inline-block px-2 py-0.5 rounded-full text-xs font-bold border bg-slate-100 text-slate-500 border-slate-200">Inactivo</span>

export default function ExamenesAuxiliares() {
  const toast = useToast()
  const [examenes, setExamenes] = useState([])
  const [loading, setLoading] = useState(true)
  const [showForm, setShowForm] = useState(false)
  const [editing, setEditing] = useState(null)
  const [saving, setSaving] = useState(false)
  const [form, setForm] = useState({ nombre: '', descripcion: '', precio: '', tiempo_estimado: '' })

  useEffect(() => {
    fetchExamenes()
  }, [])

  const fetchExamenes = async () => {
    setLoading(true)
    try {
      const res = await api.get('/api/examenes-auxiliares')
      setExamenes(res.data)
    } catch (err) {
      toast.error(err.response?.data?.detail || "No se pudieron cargar los exámenes.")
    } finally {
      setLoading(false)
    }
  }

  const resetForm = () => {
    setForm({ nombre: '', descripcion: '', precio: '', tiempo_estimado: '' })
    setEditing(null)
  }

  const openCreate = () => {
    resetForm()
    setShowForm(true)
  }

  const openEdit = (item) => {
    setEditing(item)
    setForm({
      nombre: item.nombre,
      descripcion: item.descripcion || '',
      precio: item.precio != null ? item.precio : '',
      tiempo_estimado: item.tiempo_estimado || '',
    })
    setShowForm(true)
  }

  const handleSave = async () => {
    if (!form.nombre.trim()) {
      toast.error("El nombre del examen es obligatorio.")
      return
    }
    setSaving(true)
    const payload = {
      nombre: form.nombre.trim(),
      descripcion: form.descripcion.trim() || null,
      precio: form.precio !== '' && form.precio != null ? Number(form.precio) : 0,
      tiempo_estimado: form.tiempo_estimado.trim() || null,
    }
    try {
      if (editing) {
        await api.put(`/api/examenes-auxiliares/${editing.id}`, payload)
        toast.success("Examen actualizado correctamente.")
      } else {
        await api.post('/api/examenes-auxiliares', payload)
        toast.success("Examen registrado correctamente.")
      }
      setShowForm(false)
      resetForm()
      fetchExamenes()
      setTimeout(() => toast.success(null), 4000)
    } catch (err) {
      toast.error(err.response?.data?.detail || "Error al guardar el examen.")
      setTimeout(() => toast.error(null), 4000)
    } finally {
      setSaving(false)
    }
  }

  const handleDelete = async (item) => {
    if (!window.confirm(`¿Eliminar el examen "${item.nombre}"?`)) return
    try {
      await api.delete(`/api/examenes-auxiliares/${item.id}`)
      toast.success("Examen eliminado.")
      fetchExamenes()
      setTimeout(() => toast.success(null), 4000)
    } catch (err) {
      toast.error(err.response?.data?.detail || "No se pudo eliminar el examen.")
      setTimeout(() => toast.error(null), 4000)
    }
  }

  const columns = [
    { header: 'ID', accessor: 'id', render: (item) => <span className="text-indigo-600/70 font-mono text-xs">#{item.id}</span> },
    {
      header: 'Nombre',
      accessor: 'nombre',
      render: (item) => (
        <span className="font-bold text-slate-900 flex items-center gap-2">
          <span className="p-1.5 rounded-lg bg-indigo-50 border border-indigo-100 text-indigo-600">
            <FlaskConical className="w-4 h-4" />
          </span>
          {item.nombre}
        </span>
      ),
    },
    { header: 'Descripción', accessor: 'descripcion', render: (item) => <span className="text-slate-500">{item.descripcion || '—'}</span> },
    {
      header: 'Precio',
      accessor: 'precio',
      render: (item) => <span className="font-bold text-slate-900">${(item.precio || 0).toFixed(2)}</span>,
    },
    {
      header: 'Tiempo',
      accessor: 'tiempo_estimado',
      render: (item) => item.tiempo_estimado
        ? <span className="text-slate-600 flex items-center gap-1"><Clock className="w-3.5 h-3.5 text-slate-400" />{item.tiempo_estimado}</span>
        : <span className="text-slate-400">—</span>,
    },
    { header: 'Estado', accessor: 'activo', render: (item) => estadoBadge(item.activo) },
    {
      header: 'Acciones',
      accessor: 'acciones',
      render: (item) => (
        <RoleGate allowedRoles={['Administrador']}>
          <div className="flex items-center space-x-2">
            <button onClick={() => openEdit(item)} className="text-indigo-600 hover:text-indigo-500 bg-indigo-50 hover:bg-indigo-100 p-2 rounded-xl transition-colors" title="Editar Examen">
              <Pencil className="w-4 h-4" />
            </button>
            <button onClick={() => handleDelete(item)} className="text-rose-600 hover:text-rose-600 bg-rose-50 hover:bg-rose-100 p-2 rounded-xl transition-colors" title="Eliminar Examen">
              <Trash2 className="w-4 h-4" />
            </button>
          </div>
        </RoleGate>
      ),
    },
  ]

  return (
    <div className="space-y-6 animate-fade-in-up">
      <PageHeader
        title="Exámenes Auxiliares"
        description="Catálogo de exámenes disponibles para asignar en las atenciones"
        actions={
          <div className="flex items-center gap-2">
            <Button onClick={fetchExamenes} variant="secondary" isLoading={loading}>
              <RefreshCw className={`w-4 h-4 mr-2 ${loading ? 'animate-spin' : ''}`} />
              Refrescar
            </Button>
            <RoleGate allowedRoles={['Administrador']}>
              <Button onClick={openCreate} variant="primary">
                <Plus className="w-4 h-4 mr-2" /> Nuevo Examen
              </Button>
            </RoleGate>
          </div>
        }
      />

      <DataTable
        columns={columns}
        data={examenes}
        emptyMessage="No hay exámenes auxiliares registrados."
      />

      <Modal isOpen={showForm} onClose={() => setShowForm(false)} title={editing ? 'Editar Examen' : 'Nuevo Examen'}>
        <div className="space-y-4">
          <Input
            label="Nombre del examen *"
            required
            value={form.nombre}
            onChange={(e) => setForm({ ...form, nombre: e.target.value })}
            placeholder="Ej: Hemograma completo"
          />
          <div>
            <label className="block text-xs font-bold text-slate-500 uppercase tracking-widest mb-2">Descripción</label>
            <textarea
              value={form.descripcion}
              onChange={(e) => setForm({ ...form, descripcion: e.target.value })}
              placeholder="Breve descripción del examen"
              rows={3}
              className="w-full px-4 py-3 bg-white border border-slate-200 rounded-xl text-slate-900 focus:outline-none focus:border-[#047857] focus:ring-1 focus:ring-[#047857]/20 resize-none"
            />
          </div>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <Input
              label="Precio (S/.)"
              type="number"
              step="0.01"
              min="0"
              value={form.precio}
              onChange={(e) => setForm({ ...form, precio: e.target.value })}
              placeholder="0.00"
            />
            <Input
              label="Tiempo estimado"
              value={form.tiempo_estimado}
              onChange={(e) => setForm({ ...form, tiempo_estimado: e.target.value })}
              placeholder="Ej: 30 min"
            />
          </div>
          <div className="flex items-center justify-end gap-2 pt-2">
            <Button variant="ghost" onClick={() => setShowForm(false)}>Cancelar</Button>
            <Button variant="primary" onClick={handleSave} isLoading={saving}>
              {editing ? 'Guardar Cambios' : 'Crear Examen'}
            </Button>
          </div>
        </div>
      </Modal>
    </div>
  )
}
