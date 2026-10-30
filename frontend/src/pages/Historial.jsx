import { useState, useEffect } from 'react'
import { useToast } from '../components/ui/ToastProvider'
import { TableSkeleton } from '../components/ui/Skeleton'
import api from '../api/axiosConfig'
import { Search, PawPrint, Calendar, User, Printer, Plus, Activity, Stethoscope } from 'lucide-react'
import { RoleGate } from '../components/RoleGate'
import PageHeader from '../components/ui/PageHeader'
import DataTable from '../components/ui/DataTable'
import Button from '../components/ui/Button'
import Modal from '../components/ui/Modal'
import Input from '../components/ui/Input'
import StatusBadge from '../components/ui/StatusBadge'
import jsPDF from 'jspdf'
import 'jspdf-autotable'

export default function Historial() {
  const toast = useToast();
  const [clientes, setClientes] = useState([])
  const [citas, setCitas] = useState([])
  const [productos, setProductos] = useState([])
  const [loading, setLoading] = useState(true)
  
  const [searchTerm, setSearchTerm] = useState('')
  const [selectedMascotaId, setSelectedMascotaId] = useState(null)
  
  const [showVacunaForm, setShowVacunaForm] = useState(false)
  const [newVacuna, setNewVacuna] = useState({ producto_id: '', fecha_aplicacion: new Date().toISOString().split('T')[0], intervalo_dias: '365', custom_dias: '' })

    const loadData = async () => {
    try {
      setLoading(true)
      const [clientesRes, citasRes, productosRes] = await Promise.all([
        api.get('/api/clientes'),
        api.get('/api/citas'),
        api.get('/api/productos')
      ])
      setClientes(clientesRes.data)
      setCitas(citasRes.data)
      setProductos(productosRes.data)
    } catch (err) {
      console.error(err)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadData()
  }, [])

  const allMascotas = clientes.flatMap(c => 
    c.mascotas.map(m => ({ ...m, propietario_nombre: c.nombre, dni: c.dni, telefono: c.telefono }))
  )

  const activeMascotas = allMascotas.filter(m => m.activo !== false)

  const filteredMascotas = activeMascotas.filter(m => 
    m.nombre.toLowerCase().includes(searchTerm.toLowerCase()) || 
    (m.especie || '').toLowerCase().includes(searchTerm.toLowerCase()) ||
    m.propietario_nombre.toLowerCase().includes(searchTerm.toLowerCase()) ||
    m.dni.includes(searchTerm)
  )

  const selectedMascota = activeMascotas.find(m => m.id === selectedMascotaId)
  
  const historialCitas = citas
    .filter(c => c.mascota_id === selectedMascotaId)
    .sort((a, b) => new Date(b.fecha_hora) - new Date(a.fecha_hora))

  const productosConStock = productos.filter(p => p.stock > 0)
  const selectedProducto = productos.find(p => p.id === Number(newVacuna.producto_id))
  const intervaloEfectivo = newVacuna.intervalo_dias === 'custom' ? Number(newVacuna.custom_dias) : Number(newVacuna.intervalo_dias)
  const proximaDosisCalculada = (() => {
    if (!newVacuna.fecha_aplicacion || !intervaloEfectivo) return null
    const d = new Date(newVacuna.fecha_aplicacion + 'T12:00:00')
    d.setDate(d.getDate() + intervaloEfectivo)
    return d
  })()

  const handleCreateVacuna = async (e) => {
    e.preventDefault()
    try {
      const payload = {
        tipo: selectedProducto?.nombre || '',
        producto_id: newVacuna.producto_id ? Number(newVacuna.producto_id) : null,
        fecha_aplicacion: newVacuna.fecha_aplicacion,
        intervalo_dias: intervaloEfectivo > 0 ? intervaloEfectivo : null
      }
      await api.post(`/api/mascotas/${selectedMascotaId}/vacunas`, payload)
      toast.success("Vacuna registrada exitosamente.")
      setShowVacunaForm(false)
      setNewVacuna({ producto_id: '', fecha_aplicacion: new Date().toISOString().split('T')[0], intervalo_dias: '365', custom_dias: '' })
      loadData()
      setTimeout(() => toast.success(null), 3000)
    } catch (err) {
      toast.error(err.response?.data?.detail || "Error al registrar vacuna.")
      setTimeout(() => toast.error(null), 3000)
    }
  }

  const exportPDF = () => {
    if (!selectedMascota) return;

    const doc = new jsPDF()
    doc.setFontSize(18)
    doc.text("Clínica Veterinaria Sirbull", 14, 20)
    
    doc.setFontSize(14)
    doc.text("Expediente Clínico", 14, 30)
    
    doc.setFontSize(10)
    doc.text(`Paciente: ${selectedMascota.nombre} (${selectedMascota.especie} - ${selectedMascota.raza || 'Mestizo'})`, 14, 40)
    doc.text(`Propietario: ${selectedMascota.propietario_nombre}`, 14, 45)
    doc.text(`DNI: ${selectedMascota.dni} | Tel: ${selectedMascota.telefono}`, 14, 50)
    
    doc.text(`Fecha de emisión: ${new Date().toLocaleDateString()}`, 14, 60)

    const tableColumn = ["Fecha y Hora", "Motivo de Visita", "Estado"]
    const tableRows = []

    historialCitas.forEach(cita => {
      const d = new Date(cita.fecha_hora)
      const rowData = [
        `${d.toLocaleDateString()} ${d.toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'})}`,
        cita.motivo,
        cita.estado
      ]
      tableRows.push(rowData)
    })

    doc.autoTable({
      head: [tableColumn],
      body: tableRows,
      startY: 70,
      theme: 'grid',
      headStyles: { fillColor: [34, 211, 238] }
    })

    if (selectedMascota.vacunas && selectedMascota.vacunas.length > 0) {
      const finalY = doc.lastAutoTable.finalY || 70
      doc.setFontSize(12)
      doc.text("Registro de Vacunación", 14, finalY + 15)
      
      const vacColumn = ["Vacuna", "Fecha Aplicación", "Cada", "Próxima Dosis"]
      const vacRows = selectedMascota.vacunas.map(v => [
        v.tipo, 
        new Date(v.fecha_aplicacion).toLocaleDateString(), 
        v.intervalo_dias ? `cada ${v.intervalo_dias} días` : '—',
        v.proxima_dosis ? new Date(v.proxima_dosis).toLocaleDateString() : 'N/A'
      ])

      doc.autoTable({
        head: [vacColumn],
        body: vacRows,
        startY: finalY + 20,
        theme: 'grid',
        headStyles: { fillColor: [16, 185, 129] } // Emerald green for vaccines
      })
    }

    doc.save(`Expediente_${selectedMascota.nombre.replace(/\s+/g, '_')}.pdf`)
  }

  const columnsCitas = [
    {
      header: 'Fecha y Hora',
      accessor: 'fecha_hora',
      render: (item) => {
        const d = new Date(item.fecha_hora)
        return <span className="font-bold text-slate-900">{d.toLocaleDateString()} {d.toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'})}</span>
      }
    },
    { header: 'Motivo', accessor: 'motivo', render: (item) => <span className="text-slate-600">{item.motivo}</span> },
    { header: 'Estado', accessor: 'estado', render: (item) => <StatusBadge status={item.estado} /> }
  ]

  const columnsConsultas = [
    { header: 'Fecha', accessor: 'fecha', render: (item) => <span className="text-slate-600">{new Date(item.fecha).toLocaleDateString()} {new Date(item.fecha).toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'})}</span> },
    { header: 'Cita', accessor: 'cita_motivo', render: (item) => <span className="text-slate-400">{item.cita_motivo || '—'}</span> },
    { header: 'Diagnóstico', accessor: 'diagnostico', render: (item) => <span className="font-bold text-slate-900">{item.diagnostico}</span> },
    { header: 'Tratamiento', accessor: 'tratamiento', render: (item) => <span className="text-slate-600">{item.tratamiento}</span> },
    { header: 'Peso', accessor: 'peso', render: (item) => <span className="text-slate-600">{item.peso ? `${item.peso} kg` : '—'}</span> }
  ]

  const columnsVacunas = [
    { header: 'Vacuna', accessor: 'tipo', render: (item) => <span className="font-bold text-slate-900">{item.tipo}</span> },
    { header: 'Fecha Aplicación', accessor: 'fecha_aplicacion', render: (item) => <span className="text-slate-600">{new Date(item.fecha_aplicacion).toLocaleDateString()}</span> },
    { header: 'Cada', accessor: 'intervalo_dias', render: (item) => <span className="text-slate-600">{item.intervalo_dias ? `cada ${item.intervalo_dias} días` : '—'}</span> },
    { header: 'Próxima Dosis', accessor: 'proxima_dosis', render: (item) => <span className="text-indigo-600 font-bold">{item.proxima_dosis ? new Date(item.proxima_dosis).toLocaleDateString() : 'No requerida'}</span> }
  ]

  return (
    <div className="space-y-6 animate-fade-in-up">
      <PageHeader 
        title="Historial Clínico" 
        description="Expedientes de pacientes y registro de vacunación"
      />

            
      <div className="grid grid-cols-1 lg:grid-cols-4 gap-6">
        
        {/* Panel lateral: Buscador de Pacientes */}
        <div className="lg:col-span-1 flex flex-col h-[70vh]">
          <div className="relative mb-4">
            <Search className="absolute left-3 top-2.5 h-5 w-5 text-slate-400" />
            <input 
              type="text"
              placeholder="Buscar por nombre, especie o dueño..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="w-full pl-10 pr-4 py-2 bg-white border border-slate-200 rounded-xl text-slate-900 focus:ring-1 focus:ring-indigo-500 outline-none"
            />
          </div>
          
          <div className="flex-1 overflow-y-auto space-y-2 pr-2">
            {filteredMascotas.map(m => (
              <div 
                key={m.id} 
                onClick={() => setSelectedMascotaId(m.id)}
                className={`p-4 rounded-xl cursor-pointer border transition-all ${selectedMascotaId === m.id ? 'bg-indigo-50 border-indigo-300 shadow-md' : 'bg-white border-slate-100 hover:border-indigo-200'}`}
              >
                <div className="flex items-center">
                  <div className={`p-2 rounded-lg mr-3 ${selectedMascotaId === m.id ? 'bg-indigo-100 text-indigo-600' : 'bg-slate-50 text-slate-400'}`}>
                    <PawPrint className="w-5 h-5" />
                  </div>
                  <div>
                    <h4 className={`font-bold ${selectedMascotaId === m.id ? 'text-indigo-600' : 'text-slate-900'}`}>{m.nombre}</h4>
                    <p className="text-xs text-slate-500">{m.especie}</p>
                  </div>
                </div>
              </div>
            ))}
            {filteredMascotas.length === 0 && (
              <p className="text-slate-500 text-sm text-center mt-4">No se encontraron resultados.</p>
            )}
          </div>
        </div>

        {/* Panel principal: Expediente */}
        <div className="lg:col-span-3">
          {!selectedMascotaId ? (
            <div className="h-full min-h-[400px] flex flex-col items-center justify-center bg-white border border-slate-100 rounded-2xl">
              <PawPrint className="w-16 h-16 text-slate-600 mb-4 opacity-50" />
              <p className="text-slate-400 text-lg">Selecciona un paciente para ver su historial</p>
            </div>
          ) : (
            <div className="space-y-6">
              
              {/* Tarjeta de Información del Paciente */}
              <div className="bg-white border border-slate-100 rounded-2xl p-6 shadow-xl relative overflow-hidden">
                <div className="absolute top-0 right-0 w-64 h-64 bg-indigo-50 rounded-full blur-[80px] -translate-y-1/2 translate-x-1/4 pointer-events-none"></div>
                
                <div className="flex justify-between items-start relative z-10">
                  <div className="flex items-center">
                    <div className="w-20 h-20 rounded-2xl bg-indigo-50 border border-indigo-100 flex items-center justify-center mr-6 shadow-inner">
                      <PawPrint className="w-10 h-10 text-indigo-600" />
                    </div>
                    <div>
                      <h2 className="text-3xl font-extrabold text-slate-900 tracking-tight flex items-center">
                        {selectedMascota.nombre}
                      </h2>
                      <div className="flex space-x-4 mt-2 text-sm">
                        <p className="text-indigo-600 font-bold bg-indigo-50 px-2 py-1 rounded-md">{selectedMascota.especie} {selectedMascota.raza && `- ${selectedMascota.raza}`}</p>
                        <p className="text-slate-400 flex items-center"><User className="w-4 h-4 mr-1"/> Propietario: {selectedMascota.propietario_nombre}</p>
                      </div>
                    </div>
                  </div>
                  <Button onClick={exportPDF} variant="secondary">
                    <Printer className="w-4 h-4 mr-2" /> PDF
                  </Button>
                </div>
              </div>

              {/* Registro de Vacunas */}
              <div className="bg-white border border-slate-100 rounded-2xl p-6 shadow-xl">
                <div className="flex justify-between items-center mb-6">
                  <h3 className="text-lg font-bold text-slate-900 flex items-center">
                    <Activity className="w-5 h-5 mr-2 text-emerald-600" /> Carnet de Vacunación
                  </h3>
                  <RoleGate allowedRoles={['Administrador', 'Veterinario']}>
                    <Button onClick={() => setShowVacunaForm(true)} variant="primary" className="bg-emerald-600 hover:bg-emerald-500 text-white">
                      <Plus className="w-4 h-4 mr-1" /> Registrar Vacuna
                    </Button>
                  </RoleGate>
                </div>
                
                {(!selectedMascota.vacunas || selectedMascota.vacunas.length === 0) ? (
                  <p className="text-slate-500 italic text-sm">No hay vacunas registradas para este paciente.</p>
                ) : (
                  <DataTable columns={columnsVacunas} data={selectedMascota.vacunas} emptyMessage="" />
                )}
              </div>

              {/* Consultas Médicas */}
              <div className="bg-white border border-slate-100 rounded-2xl p-6 shadow-xl">
                <div className="flex justify-between items-center mb-6">
                  <h3 className="text-lg font-bold text-slate-900 flex items-center">
                    <Stethoscope className="w-5 h-5 mr-2 text-indigo-600" /> Consultas Médicas
                  </h3>
                  <p className="text-xs text-slate-400 font-medium">Se registran al atender la cita en Citas</p>
                </div>
                
                {(!selectedMascota.consultas || selectedMascota.consultas.length === 0) ? (
                  <p className="text-slate-500 italic text-sm">No hay consultas registradas para este paciente.</p>
                ) : (
                  <DataTable columns={columnsConsultas} data={selectedMascota.consultas} emptyMessage="" />
                )}
              </div>

              {/* Historial de Citas */}
              <div className="bg-white border border-slate-100 rounded-2xl p-6 shadow-xl">
                <h3 className="text-lg font-bold text-slate-900 flex items-center mb-6">
                  <Calendar className="w-5 h-5 mr-2 text-indigo-600" /> Historial de Visitas Clínicas
                </h3>
                
                <DataTable 
                  columns={columnsCitas} 
                  data={historialCitas} 
                  emptyMessage="No hay historial de visitas para este paciente." 
                />
              </div>

            </div>
          )}
        </div>
      </div>

      {/* Modal Nueva Vacuna */}
      <Modal isOpen={showVacunaForm} onClose={() => setShowVacunaForm(false)} title={`Registrar Vacuna para ${selectedMascota?.nombre}`} maxWidth="max-w-md">
        <form onSubmit={handleCreateVacuna} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-slate-600 mb-2">Vacuna / Producto del inventario</label>
            <select
              required
              value={newVacuna.producto_id}
              onChange={e => setNewVacuna({...newVacuna, producto_id: e.target.value})}
              className="w-full bg-white border border-slate-200 rounded-xl px-4 py-3 text-slate-900 focus:ring-1 focus:ring-indigo-500 outline-none transition-all"
            >
              <option value="" disabled>Selecciona un producto...</option>
              {productosConStock.map(p => (
                <option key={p.id} value={p.id}>
                  {p.nombre}{p.sku ? ` (${p.sku})` : ''} - Stock: {p.stock}
                </option>
              ))}
            </select>
            {productosConStock.length === 0 && (
              <p className="text-amber-400 text-xs mt-2">No hay productos con stock en el inventario. Agrega o repón stock primero.</p>
            )}
          </div>
          <Input label="Fecha de Aplicación" required type="date" value={newVacuna.fecha_aplicacion} onChange={e => setNewVacuna({...newVacuna, fecha_aplicacion: e.target.value})} />
          <div>
            <label className="block text-sm font-medium text-slate-600 mb-2">Cada cuánto tiempo se aplica</label>
            <select
              value={newVacuna.intervalo_dias}
              onChange={e => setNewVacuna({...newVacuna, intervalo_dias: e.target.value})}
              className="w-full bg-white border border-slate-200 rounded-xl px-4 py-3 text-slate-900 focus:ring-1 focus:ring-indigo-500 outline-none transition-all"
            >
              <option value="30">Cada 1 mes (30 días)</option>
              <option value="60">Cada 2 meses (60 días)</option>
              <option value="90">Cada 3 meses (90 días)</option>
              <option value="180">Cada 6 meses (180 días)</option>
              <option value="365">Cada 1 año (365 días)</option>
              <option value="custom">Personalizado (días)</option>
            </select>
            {newVacuna.intervalo_dias === 'custom' && (
              <Input
                label="Cantidad de días"
                required
                type="number"
                min="1"
                value={newVacuna.custom_dias}
                onChange={e => setNewVacuna({...newVacuna, custom_dias: e.target.value})}
              />
            )}
          </div>
          {proximaDosisCalculada && (
            <p className="text-sm text-indigo-600 font-bold">
              Próxima dosis calculada: {proximaDosisCalculada.toLocaleDateString()}
            </p>
          )}
          <div className="flex justify-end space-x-3 pt-4">
            <Button type="button" variant="ghost" onClick={() => setShowVacunaForm(false)}>Cancelar</Button>
            <Button type="submit" variant="primary">Guardar Registro</Button>
          </div>
        </form>
      </Modal>

    </div>
  )
}
