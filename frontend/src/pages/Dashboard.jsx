import React, { useEffect, useState } from 'react'
import { Calendar } from 'lucide-react'
import api from '../api/axiosConfig'
import StatCard from '../components/ui/StatCard'
import DataTable from '../components/ui/DataTable'
import PageHeader from '../components/ui/PageHeader'
import Modal from '../components/ui/Modal'
import Input from '../components/ui/Input'
import Button from '../components/ui/Button'
import { useToast } from '../components/ui/ToastProvider'

export default function Dashboard() {
  const toast = useToast();
  const [stats, setStats] = useState({ citas_hoy: 0, vacunas_pendientes: 0, alertas_stock: 0 })
  const [citas, setCitas] = useState([])
  const [recordatorios, setRecordatorios] = useState([])
  const [loading, setLoading] = useState(true)
  const [showPagoConfig, setShowPagoConfig] = useState(false)
  const [pagoCfg, setPagoCfg] = useState(null)
  const [savingPagoCfg, setSavingPagoCfg] = useState(false)

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [statsRes, citasRes, recRes] = await Promise.all([
          api.get('/api/stats'),
          api.get('/api/citas/proximas'),
          api.get('/api/recordatorios')
        ])
        setStats(statsRes.data)
        setCitas(citasRes.data)
        setRecordatorios(recRes.data)
      } catch (error) {
        console.error("Error fetching data from backend", error)
      } finally {
        setLoading(false)
      }
    }
    fetchData()
  }, [])

  const openPagoConfig = async () => {
    try {
      const res = await api.get('/api/configuracion/pago')
      setPagoCfg(res.data)
      setShowPagoConfig(true)
    } catch (err) {
      toast.error('No se pudo cargar configuración de pago')
    }
  }

  const savePagoConfig = async () => {
    try {
      setSavingPagoCfg(true)
      // Normalize metodos as array
      const payload = { ...pagoCfg }
      if (typeof payload.metodos === 'string') payload.metodos = payload.metodos.split(',').map(s => s.trim()).filter(Boolean)
      await api.put('/api/configuracion/pago', payload)
      toast.success('Configuración de pago guardada')
      setShowPagoConfig(false)
    } catch (err) {
      toast.error(err.response?.data?.detail || 'Error guardando configuración')
    } finally {
      setSavingPagoCfg(false)
    }
  }

  if (loading) {
    return (
      <div className="flex items-center justify-center h-full">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-indigo-500"></div>
      </div>
    )
  }

  const citasColumns = [
    { header: 'Hora', accessor: 'hora' },
    { header: 'Paciente (Mascota)', accessor: 'mascota', render: (item) => <span className="text-indigo-600 font-bold">{item.mascota}</span> },
    { header: 'Propietario', accessor: 'propietario' },
    { header: 'Motivo', accessor: 'motivo' },
    { header: 'Estado', accessor: 'estado', render: (item) => (
      <span className="bg-amber-50 text-amber-600 px-3 py-1 rounded-full text-xs font-bold border border-amber-200">
        {item.estado}
      </span>
    )}
  ]

  const recColumns = [
    { header: 'Próxima Dosis', accessor: 'proxima_dosis', render: (item) => {
        const isOverdue = new Date(item.proxima_dosis) < new Date()
        return (
          <span className={`font-bold px-3 py-1 rounded-full text-xs border ${isOverdue ? 'bg-rose-50 text-rose-600 border-rose-200' : 'bg-orange-500/10 text-orange-400 border-orange-500/20'}`}>
            {new Date(item.proxima_dosis).toLocaleDateString()} {isOverdue && "(Vencida)"}
          </span>
        )
      }
    },
    { header: 'Vacuna', accessor: 'tipo', render: (item) => <span className="font-bold text-slate-900">{item.tipo}</span> },
    { header: 'Paciente', accessor: 'mascota_nombre', render: (item) => <span className="text-indigo-600 font-bold">{item.mascota_nombre}</span> },
    { header: 'Propietario / Teléfono', accessor: 'cliente_nombre', render: (item) => (
        <span>{item.cliente_nombre} - <span className="font-mono text-xs text-slate-400 ml-1">{item.cliente_telefono}</span></span>
    )}
  ]

  return (
    <div className="animate-fade-in-up">
      <PageHeader 
        title="Panel de Control" 
        description="Resumen de operaciones del día (Conectado al Backend)"
      />

      <div className="mb-4">
        <Button variant="secondary" onClick={openPagoConfig}>Configurar pagos</Button>
      </div>

      {/* Stats Grid */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
        <StatCard title="Citas de Hoy" value={stats.citas_hoy} trend={5} />
        <StatCard title="Vacunas Pendientes" value={stats.vacunas_pendientes} trend={-2} />
        <StatCard title="Alertas de Stock" value={stats.alertas_stock} trend={12} />
      </div>

      <div className="grid lg:grid-cols-2 gap-8 mb-8">
        {/* Recent Activity */}
        <div className="flex flex-col gap-4">
          <h3 className="text-lg font-bold text-slate-900 flex items-center">
            Próximas Citas
          </h3>
          <DataTable 
            columns={citasColumns}
            data={citas}
            emptyMessage="No hay citas próximas"
          />
        </div>

        {/* Recordatorios de Vacunación */}
        <div className="flex flex-col gap-4">
          <h3 className="text-lg font-bold text-slate-900 flex items-center">
            <Calendar className="w-5 h-5 mr-2 text-orange-400" />
            Alertas de Vacunación
          </h3>
          <DataTable 
            columns={recColumns}
            data={recordatorios}
            emptyMessage="No hay vacunas próximas a vencer"
            keyExtractor={(item) => item.vacuna_id}
          />
        </div>
      </div>
      <Modal isOpen={showPagoConfig} onClose={() => setShowPagoConfig(false)} title="Configuración de Pagos" maxWidth="max-w-2xl">
        {pagoCfg ? (
          <div className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <label className="flex items-center space-x-2"><input type="checkbox" checked={pagoCfg.pago_requerido} onChange={(e) => setPagoCfg({...pagoCfg, pago_requerido: e.target.checked})} /> <span>Pago requerido para citar</span></label>
              <Input label="Costo por defecto (costo_cita)" type="number" value={pagoCfg.costo_cita} onChange={(e) => setPagoCfg({...pagoCfg, costo_cita: Number(e.target.value)})} />
            </div>
            <div>
              <label className="block text-sm font-bold mb-1">Métodos (coma-separados)</label>
              <input className="w-full px-3 py-2 border rounded" value={Array.isArray(pagoCfg.metodos) ? pagoCfg.metodos.join(',') : (pagoCfg.metodos || '')} onChange={(e) => setPagoCfg({...pagoCfg, metodos: e.target.value})} />
            </div>
            <div>
              <label className="block text-sm font-bold mb-1">Precios por tipo (JSON)</label>
              <textarea className="w-full px-3 py-2 border rounded h-36 font-mono text-sm" value={JSON.stringify(pagoCfg.tipos || {}, null, 2)} onChange={(e) => {
                try { setPagoCfg({...pagoCfg, tipos: JSON.parse(e.target.value)}); } catch (err) { /* ignore parse until save */ }
              }} />
            </div>
            <div className="flex justify-end gap-2">
              <Button variant="ghost" onClick={() => setShowPagoConfig(false)}>Cerrar</Button>
              <Button variant="primary" onClick={savePagoConfig} disabled={savingPagoCfg}>{savingPagoCfg ? 'Guardando...' : 'Guardar'}</Button>
            </div>
          </div>
        ) : (
          <div>Cargando...</div>
        )}
      </Modal>
    </div>
  )
}
