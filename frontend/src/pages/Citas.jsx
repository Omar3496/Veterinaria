import { useState, useEffect } from 'react'
import { useToast } from '../components/ui/ToastProvider'
import { TableSkeleton } from '../components/ui/Skeleton'
import api from '../api/axiosConfig'
import { Calendar as CalendarIcon, Clock, CheckCircle, XCircle, AlertCircle, Trash2, User, PawPrint, Plus, Save, Settings2, Stethoscope, CalendarClock, Pill, FlaskConical, Send, DollarSign } from 'lucide-react'
import { RoleGate } from '../components/RoleGate'
import PageHeader from '../components/ui/PageHeader'
import DataTable from '../components/ui/DataTable'
import Button from '../components/ui/Button'
import Modal from '../components/ui/Modal'
import Input from '../components/ui/Input'
import Card from '../components/ui/Card'
import StatusBadge from '../components/ui/StatusBadge'

const DIAS_SEMANA = [
  { key: 1, label: 'L' },
  { key: 2, label: 'M' },
  { key: 3, label: 'M' },
  { key: 4, label: 'J' },
  { key: 5, label: 'V' },
  { key: 6, label: 'S' },
  { key: 7, label: 'D' },
]

const TIPOS_CITA = ['Consulta médica', 'Control / Seguimiento', 'Vacunación', 'Baño / Aseo', 'Examen auxiliar', 'Cirugía']

export default function Citas() {
  const toast = useToast();
  const [citas, setCitas] = useState([])
  const [clientes, setClientes] = useState([])
  const [loading, setLoading] = useState(true)
  const [horarios, setHorarios] = useState(null)
  const [showHorarios, setShowHorarios] = useState(false)
  const [savingHorarios, setSavingHorarios] = useState(false)
  const [savedHorarios, setSavedHorarios] = useState(false)
    
  const [showForm, setShowForm] = useState(false)
  const [newCita, setNewCita] = useState({ mascota_id: '', fecha_hora: '', motivo: '', tipo: 'Consulta médica', examen_auxiliar_id: '' })
  const [examenesCatalogo, setExamenesCatalogo] = useState([])
  const [pagoConfig, setPagoConfig] = useState(null)

  const [showAtenderForm, setShowAtenderForm] = useState(false)
  const [atenderCita, setAtenderCita] = useState(null)
  const [atenderData, setAtenderData] = useState({
    diagnostico: '',
    tratamiento: '',
    peso: '',
    recetas: [],
    examenes: [],
    referencia: { centro_destino: '', especialidad: '', motivo: '' },
  })

  const [showReprogramarForm, setShowReprogramarForm] = useState(false)
  const [reprogramarCita, setReprogramarCita] = useState(null)
  const [reprogramarFecha, setReprogramarFecha] = useState('')

  const [showCancelarForm, setShowCancelarForm] = useState(false)
  const [cancelarCita, setCancelarCita] = useState(null)
  const [motivoCancelacion, setMotivoCancelacion] = useState('')

  const [showPaymentModal, setShowPaymentModal] = useState(false)
  const [pendingCita, setPendingCita] = useState(null)
  const [currentPago, setCurrentPago] = useState(null)
  const [selectedMetodo, setSelectedMetodo] = useState(null)
  const [processingPago, setProcessingPago] = useState(false)

  // Payment Config Modal
  const [showPagoConfig, setShowPagoConfig] = useState(false)
  const [pagoConfigForm, setPagoConfigForm] = useState({
    pago_requerido: true,
    costo_cita: 30.0,
    metodos: ["YAPE", "PLIN", "TARJETA", "EFECTIVO"],
    tipos: {
      "Consulta médica": 30.0,
      "Control / Seguimiento": 25.0,
      "Vacunación": 35.0,
      "Baño / Aseo": 20.0,
      "Examen auxiliar": 0.0,
      "Cirugía": 100.0
    }
  })
  const [savingPagoConfig, setSavingPagoConfig] = useState(false)
  const [savedPagoConfig, setSavedPagoConfig] = useState(false)

  const toLocalInput = (iso) => {
    const d = new Date(iso)
    const pad = (n) => String(n).padStart(2, '0')
    return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`
  }

  const openReprogramar = (cita) => {
    setReprogramarCita(cita)
    setReprogramarFecha(toLocalInput(cita.fecha_hora))
    setShowReprogramarForm(true)
  }

  const handleReprogramar = async (e) => {
    e.preventDefault()
    if (!reprogramarFecha) {
      toast.error("Selecciona una nueva fecha y hora.")
      setTimeout(() => toast.error(null), 3000)
      return
    }
    try {
      await api.put(`/api/citas/${reprogramarCita.id}/reprogramar`, { fecha_hora: new Date(reprogramarFecha).toISOString() })
      toast.success("Cita reprogramada correctamente.")
      setShowReprogramarForm(false)
      setReprogramarCita(null)
      fetchCitas()
      setTimeout(() => toast.success(null), 3000)
    } catch (err) {
      toast.error(err.response?.data?.detail || "Error al reprogramar la cita.")
      setTimeout(() => toast.error(null), 3000)
    }
  }

  const fetchCitas = async () => {
    try {
      const res = await api.get('/api/citas')
      setCitas(res.data)
    } catch (err) {
      console.error(err)
    }
  }

  const fetchClientes = async () => {
    try {
      const res = await api.get('/api/clientes')
      setClientes(res.data)
    } catch (err) {
      console.error(err)
    }
  }

  const fetchHorarios = async () => {
    try {
      const res = await api.get('/api/configuracion/horarios')
      setHorarios(res.data)
    } catch (err) {
      console.error(err)
    }
  }

  const fetchExamenesCatalogo = async () => {
    try {
      const res = await api.get('/api/examenes-auxiliares')
      setExamenesCatalogo(Array.isArray(res.data) ? res.data.filter((e) => e.activo !== false) : [])
    } catch (err) {
      console.error(err)
    }
  }

  const fetchPagoConfig = async () => {
    try {
      const res = await api.get('/api/configuracion/pago')
      setPagoConfig(res.data)
      // Also populate form for modal
      setPagoConfigForm((prev) => ({ ...prev, ...res.data }))
    } catch (err) {
      console.error(err)
    }
  }

  const savePagoConfig = async () => {
    setSavingPagoConfig(true)
    try {
      const res = await api.put('/api/configuracion/pago', pagoConfigForm)
      setPagoConfig(res.data)
      setPagoConfigForm((prev) => ({ ...prev, ...res.data }))
      setSavedPagoConfig(true)
      toast.success('Configuración de pagos guardada correctamente')
    } catch (err) {
      setSavedPagoConfig(false)
      toast.error(err.response?.data?.detail || 'Error al guardar la configuración de pagos')
    } finally {
      setSavingPagoConfig(false)
    }
  }

  const loadData = async () => {
    setLoading(true)
    await Promise.all([fetchCitas(), fetchClientes(), fetchHorarios(), fetchExamenesCatalogo(), fetchPagoConfig()])
    setLoading(false)
  }

  useEffect(() => {
    loadData()
  }, [])

  const toggleDia = (key) => {
    setSavedHorarios(false)
    setHorarios((prev) => {
      const dias = prev.dias.includes(key) ? prev.dias.filter((d) => d !== key) : [...prev.dias, key]
      return { ...prev, dias: dias.sort((a, b) => a - b) }
    })
  }

  const saveHorarios = async () => {
    setSavingHorarios(true)
    try {
      const res = await api.put('/api/configuracion/horarios', horarios)
      setHorarios(res.data)
      setSavedHorarios(true)
      toast.success('Horarios de atención guardados correctamente')
    } catch (err) {
      setSavedHorarios(false)
      toast.error(err.response?.data?.detail || 'Error al guardar los horarios')
    } finally {
      setSavingHorarios(false)
    }
  }

  const formatResumenHorarios = () => {
    if (!horarios) return ''
    const nombreDias = { 1: 'Lunes', 2: 'Martes', 3: 'Miércoles', 4: 'Jueves', 5: 'Viernes', 6: 'Sábado', 7: 'Domingo' }
    const dias = horarios.dias.length === 0
      ? 'Sin días'
      : horarios.dias.map((d) => nombreDias[d]).join(', ')
    return `${dias} · ${horarios.hora_inicio} - ${horarios.hora_fin} · Turnos de ${horarios.duracion_min} min`
  }

  const handleCreateCita = async (e) => {
    e.preventDefault()
    try {
      const isoDate = new Date(newCita.fecha_hora).toISOString()
      const payload = { mascota_id: newCita.mascota_id, fecha_hora: isoDate, motivo: newCita.motivo, tipo: newCita.tipo }
      if (newCita.tipo === 'Examen auxiliar' && newCita.examen_auxiliar_id) {
        payload.examen_auxiliar_id = Number(newCita.examen_auxiliar_id)
      }
      const res = await api.post('/api/citas', payload)
      const citaCreada = res.data
      // Si el pago es requerido, iniciar creación de pago y mostrar modal
      if (pagoConfig && pagoConfig.pago_requerido) {
        try {
          const pagoRes = await api.post(`/api/citas/${citaCreada.id}/pagos`, { metodo: (pagoConfig.metodos && pagoConfig.metodos[0]) || 'YAPE' })
          setCurrentPago(pagoRes.data)
          setPendingCita(citaCreada)
          setSelectedMetodo((pagoConfig.metodos && pagoConfig.metodos[0]) || 'YAPE')
          setShowPaymentModal(true)
        } catch (err) {
          toast.error('Cita creada, pero no se pudo iniciar el pago: ' + (err.response?.data?.detail || err.message))
        }
      } else {
        toast.success('Cita creada correctamente')
        fetchCitas()
      }
      setNewCita({ mascota_id: '', fecha_hora: '', motivo: '', tipo: 'Consulta médica', examen_auxiliar_id: '' })
      setShowForm(false)
    } catch (err) {
      toast.error(err.response?.data?.detail || "Error al programar la cita. Verifica los datos.")
      setTimeout(() => toast.error(null), 3000)
    }
  }

  const updateEstado = async (id, estado) => {
    try {
      await api.put(`/api/citas/${id}/estado?estado=${estado}`)
      fetchCitas()
    } catch (err) {
      toast.error("Error al actualizar estado.")
      setTimeout(() => toast.error(null), 3000)
    }
  }

  const openCancelar = (cita) => {
    setCancelarCita(cita)
    setMotivoCancelacion('')
    setShowCancelarForm(true)
  }

  const handleCancelar = async (e) => {
    e.preventDefault()
    if (!motivoCancelacion.trim()) {
      toast.error("Debes indicar el motivo de cancelación.")
      setTimeout(() => toast.error(null), 3000)
      return
    }
    try {
      await api.put(`/api/citas/${cancelarCita.id}/estado`, null, { params: { estado: 'Cancelada', motivo_cancelacion: motivoCancelacion.trim() } })
      toast.success("Cita cancelada correctamente.")
      setShowCancelarForm(false)
      setCancelarCita(null)
      fetchCitas()
      setTimeout(() => toast.success(null), 3000)
    } catch (err) {
      toast.error(err.response?.data?.detail || "Error al cancelar la cita.")
      setTimeout(() => toast.error(null), 3000)
    }
  }

  const openAtender = (cita) => {
    setAtenderCita(cita)
    setAtenderData({
      diagnostico: '',
      tratamiento: '',
      peso: '',
      recetas: [],
      examenes: [],
      referencia: { centro_destino: '', especialidad: '', motivo: '' },
    })
    setShowAtenderForm(true)
  }

  const updateReceta = (idx, campo, valor) => {
    setAtenderData((prev) => {
      const recetas = [...prev.recetas]
      recetas[idx] = { ...recetas[idx], [campo]: valor }
      return { ...prev, recetas }
    })
  }

  const addReceta = () => {
    setAtenderData((prev) => ({ ...prev, recetas: [...prev.recetas, { nombre_medicamento: '', dosis: '', cantidad: '', indicaciones: '' }] }))
  }

  const removeReceta = (idx) => {
    setAtenderData((prev) => ({ ...prev, recetas: prev.recetas.filter((_, i) => i !== idx) }))
  }

  const toggleExamen = (examen) => {
    setAtenderData((prev) => {
      const yaExiste = prev.examenes.some((e) => e.examen_auxiliar_id === examen.id)
      return {
        ...prev,
        examenes: yaExiste
          ? prev.examenes.filter((e) => e.examen_auxiliar_id !== examen.id)
          : [...prev.examenes, { examen_auxiliar_id: examen.id }],
      }
    })
  }

  const handleAtenderCita = async (e) => {
    e.preventDefault()
    try {
      const payload = {
        diagnostico: atenderData.diagnostico,
        tratamiento: atenderData.tratamiento,
        peso: atenderData.peso ? Number(atenderData.peso) : null,
        recetas: atenderData.recetas.filter((r) => r.nombre_medicamento && r.nombre_medicamento.trim()),
        examenes: atenderData.examenes,
        referencia: (atenderData.referencia.centro_destino && atenderData.referencia.centro_destino.trim())
          ? {
              centro_destino: atenderData.referencia.centro_destino.trim(),
              especialidad: atenderData.referencia.especialidad.trim() || null,
              motivo: atenderData.referencia.motivo.trim(),
            }
          : null,
      }
      await api.post(`/api/citas/${atenderCita.id}/atender`, payload)
      toast.success("Cita atendida y consulta registrada.")
      setShowAtenderForm(false)
      setAtenderCita(null)
      setAtenderData({
        diagnostico: '',
        tratamiento: '',
        peso: '',
        recetas: [],
        examenes: [],
        referencia: { centro_destino: '', especialidad: '', motivo: '' },
      })
      fetchCitas()
      setTimeout(() => toast.success(null), 3000)
    } catch (err) {
      toast.error(err.response?.data?.detail || "Error al atender la cita.")
      setTimeout(() => toast.error(null), 3000)
    }
  }

  const handleDeleteCita = async (id) => {
    if(!window.confirm("¿Estás seguro de eliminar esta cita?")) return;
    try {
      await api.delete(`/api/citas/${id}`)
      fetchCitas()
    } catch(err) {
      toast.error("Error al eliminar la cita.")
      setTimeout(() => toast.error(null), 3000)
    }
  }

  const allMascotas = clientes.flatMap(c => 
    c.mascotas.map(m => ({ ...m, propietario_nombre: c.nombre }))
  )

  const getMascotaInfo = (mascota_id) => {
    const m = allMascotas.find(masc => masc.id === mascota_id)
    return m ? m : { nombre: 'Desconocido', especie: '', propietario_nombre: 'Desconocido' }
  }

  const handleProcessPago = async (pagoId, success = true) => {
    setProcessingPago(true)
    try {
      const res = await api.post(`/api/pagos/${pagoId}/procesar`, success ? { success: true } : { success: false })
      toast.success('Pago procesado: ' + res.data.estado)
      setShowPaymentModal(false)
      setCurrentPago(null)
      setPendingCita(null)
      fetchCitas()
      setTimeout(() => toast.success(null), 3000)
    } catch (err) {
      toast.error(err.response?.data?.detail || 'Error procesando pago')
      setTimeout(() => toast.error(null), 3000)
    } finally {
      setProcessingPago(false)
    }
  }

  const columns = [
    { 
      header: 'Fecha y Hora', 
      accessor: 'fecha_hora',
      render: (item) => {
        const dateObj = new Date(item.fecha_hora)
        return (
          <div className="font-bold flex items-center">
            <Clock className="w-4 h-4 mr-2 text-indigo-600" />
            {dateObj.toLocaleDateString()} {dateObj.toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'})}
          </div>
        )
      }
    },
    { 
      header: 'Paciente', 
      accessor: 'mascota_id',
      render: (item) => {
        const mascotaInfo = getMascotaInfo(item.mascota_id)
        return (
          <div>
            <p className="font-bold text-slate-900 flex items-center">
              <PawPrint className="w-3 h-3 mr-1 text-indigo-600" /> {mascotaInfo.nombre} 
              <span className="text-xs text-slate-500 font-normal ml-1">({mascotaInfo.especie})</span>
            </p>
            <p className="text-xs text-slate-400 flex items-center mt-0.5">
              <User className="w-3 h-3 mr-1" /> Dueño: {mascotaInfo.propietario_nombre}
            </p>
          </div>
        )
      }
    },
    { header: 'Motivo', accessor: 'motivo', render: (item) => (
        <div>
          <p className="text-slate-700">{item.motivo}</p>
          {item.tipo && <span className="inline-block mt-1 px-2 py-0.5 rounded-full text-[11px] font-bold border bg-indigo-50 text-indigo-700 border-indigo-100">{item.tipo}</span>}
          {item.examen_auxiliar_nombre && <span className="block text-[11px] text-slate-400 mt-0.5">Examen: {item.examen_auxiliar_nombre}</span>}
        </div>
      ) },
    { 
      header: 'Estado', 
      accessor: 'estado',
      render: (item) => <StatusBadge status={item.estado} />
    },
    { 
      header: 'Acciones', 
      accessor: 'acciones',
      render: (item) => {
        const isCompleted = item.estado === 'Completada' || item.estado === 'Completado'
        const isCancelled = item.estado === 'Cancelada'
        
        return (
          <div className="flex items-center space-x-2">
            {!isCompleted && !isCancelled && (
              <>
                <RoleGate allowedRoles={['Administrador', 'Veterinario']}>
                  <button onClick={() => openAtender(item)} className="text-emerald-600 hover:text-emerald-600 bg-emerald-50 hover:bg-emerald-100 p-2 rounded-xl transition-colors" title="Atender cita y registrar consulta">
                    <Stethoscope className="w-4 h-4" />
                  </button>
                </RoleGate>
                <RoleGate allowedRoles={['Recepcionista']}>
                  <button onClick={() => updateEstado(item.id, 'Completada')} className="text-emerald-600 hover:text-emerald-600 bg-emerald-50 hover:bg-emerald-100 p-2 rounded-xl transition-colors" title="Marcar como Completada">
                    <CheckCircle className="w-4 h-4" />
                  </button>
                </RoleGate>
                <button onClick={() => openReprogramar(item)} className="text-indigo-600 hover:text-indigo-700 bg-indigo-50 hover:bg-indigo-100 p-2 rounded-xl transition-colors" title="Reprogramar Cita">
                  <CalendarClock className="w-4 h-4" />
                </button>
                <button onClick={() => openCancelar(item)} className="text-orange-500 hover:text-orange-400 bg-orange-500/10 hover:bg-orange-500/20 p-2 rounded-xl transition-colors" title="Cancelar Cita">
                  <XCircle className="w-4 h-4" />
                </button>
              </>
            )}
            <RoleGate allowedRoles={['Administrador', 'Veterinario']}>
              <button onClick={() => handleDeleteCita(item.id)} className="text-rose-600 hover:text-rose-600 bg-rose-50 hover:bg-rose-100 p-2 rounded-xl transition-colors" title="Eliminar Cita">
                <Trash2 className="w-4 h-4" />
              </button>
            </RoleGate>
          </div>
        )
      }
    }
  ]

  return (
    <div className="space-y-6 animate-fade-in-up">
      <PageHeader 
        title="Agenda de Citas" 
        description="Programación y seguimiento de pacientes"
        actions={
          <Button onClick={() => setShowForm(true)} variant="primary">
            <Plus className="w-4 h-4 mr-2" /> Nueva Cita
          </Button>
        }
      />

      
      <Modal isOpen={showForm} onClose={() => setShowForm(false)} title="Programar Nueva Cita" maxWidth="max-w-2xl">
        <form onSubmit={handleCreateCita} className="space-y-4">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-bold text-slate-400 uppercase tracking-widest mb-2">Paciente</label>
              <select 
                required
                value={newCita.mascota_id}
                onChange={e => setNewCita({...newCita, mascota_id: e.target.value})}
                className="w-full bg-white border border-slate-200 rounded-xl px-4 py-3 text-slate-900 focus:ring-1 focus:ring-indigo-500 outline-none transition-all"
              >
                <option value="" className="bg-white">Selecciona un paciente...</option>
                {allMascotas.map(m => (
                  <option key={m.id} value={m.id} className="bg-white">{m.nombre} (Dueño: {m.propietario_nombre})</option>
                ))}
              </select>
            </div>
            
            <Input 
              label="Fecha y Hora"
              type="datetime-local" 
              required
              value={newCita.fecha_hora}
              onChange={e => setNewCita({...newCita, fecha_hora: e.target.value})}
            />
            
            <div>
              <label className="block text-xs font-bold text-slate-400 uppercase tracking-widest mb-2">Tipo de atención</label>
              <select
                value={newCita.tipo}
                onChange={e => setNewCita({...newCita, tipo: e.target.value, examen_auxiliar_id: ''})}
                className="w-full bg-white border border-slate-200 rounded-xl px-4 py-3 text-slate-900 focus:ring-1 focus:ring-indigo-500 outline-none transition-all"
              >
                {TIPOS_CITA.map(t => (
                  <option key={t} value={t} className="bg-white">{t}</option>
                ))}
              </select>
            </div>
            
            {newCita.tipo === 'Examen auxiliar' && (
              <div>
                <label className="block text-xs font-bold text-slate-400 uppercase tracking-widest mb-2">Examen auxiliar *</label>
                <select
                  required
                  value={newCita.examen_auxiliar_id}
                  onChange={e => setNewCita({...newCita, examen_auxiliar_id: e.target.value})}
                  className="w-full bg-white border border-slate-200 rounded-xl px-4 py-3 text-slate-900 focus:ring-1 focus:ring-indigo-500 outline-none transition-all"
                >
                  <option value="" className="bg-white">Selecciona un examen...</option>
                  {examenesCatalogo.map(ex => (
                    <option key={ex.id} value={ex.id} className="bg-white">{ex.nombre}{ex.precio != null && ex.precio > 0 ? ` · $${Number(ex.precio).toFixed(2)}` : ''}</option>
                  ))}
                </select>
                {examenesCatalogo.length === 0 && (
                  <p className="text-xs text-slate-400 mt-1">No hay exámenes disponibles. Regístralos en Exámenes Auxiliares.</p>
                )}
              </div>
            )}
            
            <div className="md:col-span-2">
              <Input 
                label="Motivo (Ej. Vacunación, Consulta)"
                type="text" 
                required
                value={newCita.motivo}
                onChange={e => setNewCita({...newCita, motivo: e.target.value})}
              />
            </div>
          </div>
          
          <div className="flex justify-end space-x-3 pt-4">
            <Button type="button" variant="ghost" onClick={() => setShowForm(false)}>Cancelar</Button>
            <Button type="submit" variant="primary">Agendar Cita</Button>
          </div>
        </form>
      </Modal>

      <Modal isOpen={showPaymentModal} onClose={() => setShowPaymentModal(false)} title="Pago de cita" maxWidth="max-w-md">
        <div className="space-y-4">
          {pendingCita && (
            <div>
              <p className="font-bold">Cita para: {getMascotaInfo(pendingCita.mascota_id).nombre}</p>
              <p className="text-sm text-slate-500">Fecha: {new Date(pendingCita.fecha_hora).toLocaleString()}</p>
            </div>
          )}
          {currentPago && (
            <div>
              <p>Monto: <strong>{currentPago.monto}</strong></p>
              <p>Referencia: <strong>{currentPago.referencia}</strong></p>
            </div>
          )}
          <div className="flex gap-2">
            {(pagoConfig?.metodos || ['YAPE','PLIN','TARJETA']).map((m) => (
              <button key={m} onClick={() => setSelectedMetodo(m)} className={`px-3 py-2 rounded ${selectedMetodo===m? 'bg-indigo-600 text-white':'bg-gray-100'}`}>
                {m}
              </button>
            ))}
          </div>
          <div className="flex gap-2">
            <button onClick={() => handleProcessPago(currentPago.id, true)} className="px-4 py-2 bg-emerald-600 text-white rounded" disabled={processingPago}>Simular pago exitoso</button>
            <button onClick={() => handleProcessPago(currentPago.id, false)} className="px-4 py-2 bg-rose-600 text-white rounded" disabled={processingPago}>Simular pago fallido</button>
          </div>
        </div>
      </Modal>

      {/* Modal Atender Cita */}
      <Modal isOpen={showAtenderForm} onClose={() => setShowAtenderForm(false)} title="Atender Cita" maxWidth="max-w-2xl">
        {atenderCita && (
          <div className="mb-4 p-4 rounded-xl bg-slate-50 border border-slate-200">
            <div className="flex items-center mb-2">
              <PawPrint className="w-4 h-4 mr-2 text-indigo-600" />
              <span className="font-bold text-slate-900">{getMascotaInfo(atenderCita.mascota_id).nombre}</span>
              <span className="text-xs text-slate-500 ml-2">({getMascotaInfo(atenderCita.mascota_id).especie})</span>
            </div>
            <p className="text-xs text-slate-400 flex items-center">
              <User className="w-3 h-3 mr-1" /> Dueño: {getMascotaInfo(atenderCita.mascota_id).propietario_nombre}
            </p>
            <p className="text-xs text-slate-400 mt-1">
              Motivo: {atenderCita.motivo} · {new Date(atenderCita.fecha_hora).toLocaleString()}
            </p>
          </div>
        )}
        <form onSubmit={handleAtenderCita} className="space-y-5">
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div className="md:col-span-2">
              <Input label="Diagnóstico" required type="text" value={atenderData.diagnostico} onChange={e => setAtenderData({...atenderData, diagnostico: e.target.value})} />
            </div>
            <Input label="Peso (kg)" type="number" step="0.1" min="0" value={atenderData.peso} onChange={e => setAtenderData({...atenderData, peso: e.target.value})} />
          </div>
          <Input label="Tratamiento" type="text" value={atenderData.tratamiento} onChange={e => setAtenderData({...atenderData, tratamiento: e.target.value})} />

          {/* Recetas */}
          <div className="rounded-xl border border-slate-200 p-4 space-y-3">
            <div className="flex items-center justify-between">
              <h4 className="text-sm font-bold text-slate-900 flex items-center">
                <Pill className="w-4 h-4 mr-2 text-emerald-600" /> Recetas
                <span className="ml-2 text-[11px] font-semibold text-slate-400">Se recetan sin afectar el stock</span>
              </h4>
              <Button type="button" variant="secondary" size="sm" onClick={addReceta}>
                <Plus className="w-4 h-4 mr-1" /> Agregar
              </Button>
            </div>
            {atenderData.recetas.length === 0 && (
              <p className="text-xs text-slate-400">Aún no agregas medicamentos a la receta.</p>
            )}
            {atenderData.recetas.map((rec, idx) => (
              <div key={idx} className="space-y-2 p-3 rounded-xl bg-slate-50 border border-slate-200">
                <div className="grid grid-cols-1 md:grid-cols-3 gap-2">
                  <div className="md:col-span-1">
                    <label className="block text-[11px] font-bold text-slate-400 uppercase tracking-widest mb-1">Medicamento *</label>
                    <input type="text" required value={rec.nombre_medicamento} onChange={(e) => updateReceta(idx, 'nombre_medicamento', e.target.value)} placeholder="Ej: Amoxicilina"
                      className="w-full px-3 py-2 bg-white border border-slate-200 rounded-lg text-sm focus:outline-none focus:border-indigo-500" />
                  </div>
                  <div>
                    <label className="block text-[11px] font-bold text-slate-400 uppercase tracking-widest mb-1">Dosis</label>
                    <input type="text" value={rec.dosis} onChange={(e) => updateReceta(idx, 'dosis', e.target.value)} placeholder="Ej: 250 mg c/12h"
                      className="w-full px-3 py-2 bg-white border border-slate-200 rounded-lg text-sm focus:outline-none focus:border-indigo-500" />
                  </div>
                  <div>
                    <label className="block text-[11px] font-bold text-slate-400 uppercase tracking-widest mb-1">Cantidad</label>
                    <input type="text" value={rec.cantidad} onChange={(e) => updateReceta(idx, 'cantidad', e.target.value)} placeholder="Ej: 1 caja"
                      className="w-full px-3 py-2 bg-white border border-slate-200 rounded-lg text-sm focus:outline-none focus:border-indigo-500" />
                  </div>
                </div>
                <div className="flex items-end gap-2">
                  <div className="flex-1">
                    <label className="block text-[11px] font-bold text-slate-400 uppercase tracking-widest mb-1">Indicaciones</label>
                    <input type="text" value={rec.indicaciones} onChange={(e) => updateReceta(idx, 'indicaciones', e.target.value)} placeholder="Ej: Por 7 días"
                      className="w-full px-3 py-2 bg-white border border-slate-200 rounded-lg text-sm focus:outline-none focus:border-indigo-500" />
                  </div>
                  <button type="button" onClick={() => removeReceta(idx)} className="text-rose-600 hover:text-rose-500 bg-rose-50 hover:bg-rose-100 p-2 rounded-lg transition-colors" title="Quitar receta">
                    <Trash2 className="w-4 h-4" />
                  </button>
                </div>
              </div>
            ))}
          </div>

          {/* Exámenes auxiliares */}
          <div className="rounded-xl border border-slate-200 p-4 space-y-3">
            <h4 className="text-sm font-bold text-slate-900 flex items-center">
              <FlaskConical className="w-4 h-4 mr-2 text-indigo-600" /> Exámenes Auxiliares
            </h4>
            {examenesCatalogo.length === 0 ? (
              <p className="text-xs text-slate-400">No hay exámenes en el catálogo. Regístralos en Exámenes Auxiliares.</p>
            ) : (
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-2">
                {examenesCatalogo.map((ex) => {
                  const checked = atenderData.examenes.some((e) => e.examen_auxiliar_id === ex.id)
                  return (
                    <label key={ex.id} className={`flex items-center justify-between gap-2 p-3 rounded-xl border cursor-pointer transition-colors ${checked ? 'bg-indigo-50 border-indigo-300' : 'bg-white border-slate-200 hover:border-indigo-200'}`}>
                      <span className="flex items-center gap-2 min-w-0">
                        <input type="checkbox" checked={checked} onChange={() => toggleExamen(ex)} className="w-4 h-4 text-indigo-600 rounded" />
                        <span className="text-sm font-semibold text-slate-800 truncate">{ex.nombre}</span>
                      </span>
                      {ex.precio != null && ex.precio > 0 && <span className="text-xs font-bold text-slate-400 shrink-0">${Number(ex.precio).toFixed(2)}</span>}
                    </label>
                  )
                })}
              </div>
            )}
          </div>

          {/* Referencia médica */}
          <div className="rounded-xl border border-slate-200 p-4 space-y-3">
            <h4 className="text-sm font-bold text-slate-900 flex items-center">
              <Send className="w-4 h-4 mr-2 text-teal-600" /> Referencia Médica
              <span className="ml-2 text-[11px] font-semibold text-slate-400">opcional</span>
            </h4>
            <div className="grid grid-cols-1 md:grid-cols-3 gap-2">
              <div className="md:col-span-1">
                <label className="block text-[11px] font-bold text-slate-400 uppercase tracking-widest mb-1">Centro destino</label>
                <input type="text" value={atenderData.referencia.centro_destino} onChange={(e) => setAtenderData({ ...atenderData, referencia: { ...atenderData.referencia, centro_destino: e.target.value } })} placeholder="Ej: Clínica Vet Norte"
                  className="w-full px-3 py-2 bg-white border border-slate-200 rounded-lg text-sm focus:outline-none focus:border-teal-500" />
              </div>
              <div>
                <label className="block text-[11px] font-bold text-slate-400 uppercase tracking-widest mb-1">Especialidad</label>
                <input type="text" value={atenderData.referencia.especialidad} onChange={(e) => setAtenderData({ ...atenderData, referencia: { ...atenderData.referencia, especialidad: e.target.value } })} placeholder="Ej: Dermatología"
                  className="w-full px-3 py-2 bg-white border border-slate-200 rounded-lg text-sm focus:outline-none focus:border-teal-500" />
              </div>
              <div>
                <label className="block text-[11px] font-bold text-slate-400 uppercase tracking-widest mb-1">Motivo</label>
                <input type="text" value={atenderData.referencia.motivo} onChange={(e) => setAtenderData({ ...atenderData, referencia: { ...atenderData.referencia, motivo: e.target.value } })} placeholder="Ej: Requiere evaluación especializada"
                  className="w-full px-3 py-2 bg-white border border-slate-200 rounded-lg text-sm focus:outline-none focus:border-teal-500" />
              </div>
            </div>
          </div>

          <p className="text-xs text-slate-500">Al guardar, la cita quedará como Completada y la consulta se registrará en el historial clínico con su peso actual.</p>
          <div className="flex justify-end space-x-3 pt-4">
            <Button type="button" variant="ghost" onClick={() => setShowAtenderForm(false)}>Cancelar</Button>
            <Button type="submit" variant="primary">Guardar y Atender</Button>
          </div>
        </form>
      </Modal>

      {/* Modal Reprogramar Cita */}
      <Modal isOpen={showReprogramarForm} onClose={() => setShowReprogramarForm(false)} title="Reprogramar Cita" maxWidth="max-w-md">
        {reprogramarCita && (
          <div className="mb-4 p-4 rounded-xl bg-slate-50 border border-slate-200">
            <div className="flex items-center mb-2">
              <PawPrint className="w-4 h-4 mr-2 text-indigo-600" />
              <span className="font-bold text-slate-900">{getMascotaInfo(reprogramarCita.mascota_id).nombre}</span>
              <span className="text-xs text-slate-500 ml-2">({getMascotaInfo(reprogramarCita.mascota_id).especie})</span>
            </div>
            <p className="text-xs text-slate-400 flex items-center">
              <User className="w-3 h-3 mr-1" /> Dueño: {getMascotaInfo(reprogramarCita.mascota_id).propietario_nombre}
            </p>
            <p className="text-xs text-slate-400 mt-1">Motivo: {reprogramarCita.motivo}</p>
            <p className="text-xs text-slate-400 mt-0.5">Actual: {new Date(reprogramarCita.fecha_hora).toLocaleString()}</p>
          </div>
        )}
        <form onSubmit={handleReprogramar} className="space-y-4">
          <Input
            label="Nueva fecha y hora"
            type="datetime-local"
            required
            value={reprogramarFecha}
            onChange={(e) => setReprogramarFecha(e.target.value)}
          />
          <p className="text-xs text-slate-500">El sistema validará que el nuevo horario no esté ocupado por otra cita.</p>
          <div className="flex justify-end space-x-3 pt-4">
            <Button type="button" variant="ghost" onClick={() => setShowReprogramarForm(false)}>Cancelar</Button>
            <Button type="submit" variant="primary">Confirmar Reprogramación</Button>
          </div>
        </form>
      </Modal>

      <Modal isOpen={showCancelarForm} onClose={() => setShowCancelarForm(false)} title="Cancelar Cita">
        {cancelarCita && (
          <div className="mb-4 p-4 rounded-xl bg-slate-50 border border-slate-200">
            <div className="flex items-center mb-2">
              <PawPrint className="w-4 h-4 mr-2 text-orange-500" />
              <span className="font-bold text-slate-900">{getMascotaInfo(cancelarCita.mascota_id).nombre}</span>
              <span className="text-xs text-slate-500 ml-2">({getMascotaInfo(cancelarCita.mascota_id).especie})</span>
            </div>
            <p className="text-xs text-slate-400 flex items-center">
              <User className="w-3 h-3 mr-1" /> Dueño: {getMascotaInfo(cancelarCita.mascota_id).propietario_nombre}
            </p>
            <p className="text-xs text-slate-400 mt-1">Motivo: {cancelarCita.motivo}</p>
            <p className="text-xs text-slate-400 mt-0.5">Fecha: {new Date(cancelarCita.fecha_hora).toLocaleString()}</p>
          </div>
        )}
        <form onSubmit={handleCancelar} className="space-y-4">
          <Input
            label="Motivo de cancelación *"
            required
            value={motivoCancelacion}
            onChange={(e) => setMotivoCancelacion(e.target.value)}
            placeholder="Ej: El dueño no podrá asistir, emergencia, etc."
          />
          <div className="flex justify-end space-x-3 pt-4">
            <Button type="button" variant="ghost" onClick={() => setShowCancelarForm(false)}>Volver</Button>
            <Button type="submit" variant="danger">Confirmar Cancelación</Button>
          </div>
        </form>
      </Modal>

      {loading ? (
        <TableSkeleton />
      ) : (
        <>
        <Card className="p-5 bg-slate-50 backdrop-blur-lg border border-slate-200">
          <div className="flex flex-wrap items-center justify-between gap-3">
            <div className="flex items-center gap-3">
              <div className="p-2 rounded-xl bg-indigo-50 border border-indigo-100">
                <Clock className="w-5 h-5 text-indigo-600" />
              </div>
              <div>
                <h3 className="font-bold text-slate-900 flex items-center">
                  <Settings2 className="w-4 h-4 mr-2 text-indigo-600" /> Horarios de Atención
                </h3>
                <p className="text-xs text-slate-400 mt-0.5">{formatResumenHorarios()}</p>
              </div>
            </div>
            <RoleGate allowedRoles={['Administrador', 'Veterinario']}>
              <Button type="button" size="sm" variant="secondary" onClick={() => setShowHorarios(!showHorarios)}>
                {showHorarios ? 'Ocultar' : 'Configurar'}
              </Button>
            </RoleGate>
          </div>

          {showHorarios && horarios && (
            <div className="mt-5 pt-5 border-t border-slate-200 grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-5">
              <div>
                <label className="block text-xs font-bold text-slate-400 uppercase tracking-widest mb-2">Días de atención</label>
                <div className="flex gap-1.5 flex-wrap">
                  {DIAS_SEMANA.map((d) => (
                    <button
                      key={d.key}
                      type="button"
                      onClick={() => toggleDia(d.key)}
                      className={`w-9 h-9 rounded-xl text-sm font-bold border transition-colors
                        ${horarios.dias.includes(d.key)
                          ? 'bg-indigo-600 border-indigo-500 text-white'
                          : 'bg-slate-50 border-slate-200 text-slate-400 hover:bg-slate-100'}`}
                    >
                      {d.label}
                    </button>
                  ))}
                </div>
              </div>

              <Input
                label="Hora de apertura"
                type="time"
                value={horarios.hora_inicio}
                onChange={(e) => { setSavedHorarios(false); setHorarios({ ...horarios, hora_inicio: e.target.value }) }}
              />

              <Input
                label="Hora de cierre"
                type="time"
                value={horarios.hora_fin}
                onChange={(e) => { setSavedHorarios(false); setHorarios({ ...horarios, hora_fin: e.target.value }) }}
              />

              <div>
                <label className="block text-xs font-bold text-slate-400 uppercase tracking-widest mb-2">Duración del turno</label>
                <select
                  value={horarios.duracion_min}
                  onChange={(e) => { setSavedHorarios(false); setHorarios({ ...horarios, duracion_min: Number(e.target.value) }) }}
                  className="w-full bg-white border border-slate-200 rounded-xl px-4 py-3 text-slate-900 focus:ring-1 focus:ring-indigo-500 outline-none transition-all"
                >
                  <option value={15} className="bg-white">15 minutos</option>
                  <option value={30} className="bg-white">30 minutos</option>
                  <option value={60} className="bg-white">1 hora</option>
                </select>
              </div>

              <div className="md:col-span-2 lg:col-span-4 flex justify-end">
                <Button type="button" variant={savedHorarios ? 'success' : 'primary'} onClick={saveHorarios} isLoading={savingHorarios}>
                  {savedHorarios ? (
                    <>
                      <CheckCircle className="w-4 h-4 mr-2" /> Guardado
                    </>
                  ) : (
                    <>
                      <Save className="w-4 h-4 mr-2" /> Guardar Horarios
                    </>
                  )}
                </Button>
              </div>
            </div>
          )}
        </Card>

        {/* Payment Config Card */}
        <Card className="p-5 bg-slate-50 backdrop-blur-lg border border-slate-200 mt-4">
          <div className="flex flex-wrap items-center justify-between gap-3">
            <div className="flex items-center gap-3">
              <div className="p-2 rounded-xl bg-emerald-50 border border-emerald-100">
                <DollarSign className="w-5 h-5 text-emerald-600" />
              </div>
              <div>
                <h3 className="font-bold text-slate-900 flex items-center">
                  <Settings2 className="w-4 h-4 mr-2 text-emerald-600" /> Configuración de Pagos de Citas
                </h3>
                <p className="text-xs text-slate-400 mt-0.5">
                  {pagoConfig?.pago_requerido ? 'Pago requerido' : 'Pago opcional'} · {pagoConfig?.metodos?.length || 0} métodos · {Object.keys(pagoConfig?.tipos || {}).length} tipos de cita con precio
                </p>
              </div>
            </div>
            <RoleGate allowedRoles={['Administrador']}>
              <Button type="button" size="sm" variant="secondary" onClick={() => setShowPagoConfig(!showPagoConfig)}>
                {showPagoConfig ? 'Ocultar' : 'Configurar'}
              </Button>
            </RoleGate>
          </div>

          {showPagoConfig && pagoConfig && (
            <div className="mt-5 pt-5 border-t border-slate-200 space-y-5">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-bold text-slate-400 uppercase tracking-widest mb-2">Pago requerido</label>
                  <select
                    value={pagoConfigForm.pago_requerido ? 'true' : 'false'}
                    onChange={(e) => setPagoConfigForm({ ...pagoConfigForm, pago_requerido: e.target.value === 'true' })}
                    className="w-full bg-white border border-slate-200 rounded-xl px-4 py-3 text-slate-900 focus:ring-1 focus:ring-emerald-500 outline-none transition-all"
                  >
                    <option value="true" className="bg-white">Sí, requerido</option>
                    <option value="false" className="bg-white">No, opcional</option>
                  </select>
                </div>
                <Input
                  label="Costo base de cita (fallback)"
                  type="number"
                  step="0.1"
                  min="0"
                  value={pagoConfigForm.costo_cita}
                  onChange={(e) => setPagoConfigForm({ ...pagoConfigForm, costo_cita: parseFloat(e.target.value) || 0 })}
                />
              </div>

              <div>
                <label className="block text-xs font-bold text-slate-400 uppercase tracking-widest mb-2">Métodos de pago permitidos</label>
                <div className="flex flex-wrap gap-2">
                  {["YAPE", "PLIN", "TARJETA", "EFECTIVO"].map((m) => (
                    <label key={m} className="inline-flex items-center gap-2 px-3 py-2 rounded-xl border cursor-pointer transition-colors">
                      <input
                        type="checkbox"
                        checked={pagoConfigForm.metodos?.includes(m)}
                        onChange={(e) => {
                          const metodos = [...(pagoConfigForm.metodos || [])]
                          if (e.target.checked) metodos.push(m)
                          else metodos.splice(metodos.indexOf(m), 1)
                          setPagoConfigForm({ ...pagoConfigForm, metodos })
                        }}
                        className="w-4 h-4 text-emerald-600 rounded"
                      />
                      <span className="text-sm font-medium text-slate-700">{m}</span>
                    </label>
                  ))}
                </div>
              </div>

              <div>
                <label className="block text-xs font-bold text-slate-400 uppercase tracking-widest mb-2">Precios por tipo de cita</label>
                <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-3">
                  {TIPOS_CITA.map((tipo) => (
                    <div key={tipo} className="bg-white border border-slate-200 rounded-xl p-3">
                      <label className="block text-xs font-bold text-slate-400 uppercase tracking-widest mb-1">{tipo}</label>
                      <input
                        type="number"
                        step="0.1"
                        min="0"
                        value={pagoConfigForm.tipos?.[tipo] || 0}
                        onChange={(e) => setPagoConfigForm({
                          ...pagoConfigForm,
                          tipos: { ...pagoConfigForm.tipos, [tipo]: parseFloat(e.target.value) || 0 }
                        })}
                        className="w-full px-3 py-2 bg-white border border-slate-200 rounded-lg text-sm focus:outline-none focus:border-emerald-500"
                      />
                    </div>
                  ))}
                </div>
              </div>

              <div className="flex justify-end pt-4 border-t border-slate-200">
                <Button type="button" variant={savedPagoConfig ? 'success' : 'primary'} onClick={savePagoConfig} isLoading={savingPagoConfig}>
                  {savedPagoConfig ? (
                    <>
                      <CheckCircle className="w-4 h-4 mr-2" /> Guardado
                    </>
                  ) : (
                    <>
                      <Save className="w-4 h-4 mr-2" /> Guardar Configuración
                    </>
                  )}
                </Button>
              </div>
            </div>
          )}
        </Card>

        <DataTable 
          columns={columns}
          data={citas}
          emptyMessage="No hay citas programadas"
        />
        </>
      )}
    </div>
  )
}
