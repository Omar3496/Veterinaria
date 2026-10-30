import { useState, useEffect } from 'react'
import { useToast } from '../components/ui/ToastProvider'
import { TableSkeleton } from '../components/ui/Skeleton'
import api from '../api/axiosConfig'
import { Plus, Users, User, PawPrint, AlertCircle, Trash2, UserPlus, Search } from 'lucide-react'
import { RoleGate } from '../components/RoleGate'
import PageHeader from '../components/ui/PageHeader'
import DataTable from '../components/ui/DataTable'
import Button from '../components/ui/Button'
import Modal from '../components/ui/Modal'
import Input from '../components/ui/Input'

export default function Clientes() {
  const toast = useToast();
  const [clientes, setClientes] = useState([])
  const [loading, setLoading] = useState(true)
    
  const [searchTerm, setSearchTerm] = useState('')

  const [showForm, setShowForm] = useState(false)
  const [newCliente, setNewCliente] = useState({ dni: '', nombre: '', telefono: '', email: '', username: '', password: '' })
  
  const [showMascotaForm, setShowMascotaForm] = useState(null) 
  const [newMascota, setNewMascota] = useState({ nombre: '', especie: '', raza: '' })

  const fetchClientes = async () => {
    try {
      setLoading(true)
      const res = await api.get('/api/clientes')
      setClientes(res.data)
    } catch (err) {
      toast.error('Error al cargar la lista de clientes.')
      console.error(err)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchClientes()
  }, [])

  const handleCreateCliente = async (e) => {
    e.preventDefault()
    try {
      await api.post('/api/clientes', newCliente)
      setNewCliente({ dni: '', nombre: '', telefono: '', email: '', username: '', password: '' })
      setShowForm(false)
      fetchClientes()
    } catch (err) {
      toast.error(err.response?.data?.detail || "Error al crear cliente")
      setTimeout(() => toast.error(null), 3000)
    }
  }

  const handleCreateMascota = async (e, cliente_id) => {
    e.preventDefault()
    try {
      await api.post(`/api/clientes/${cliente_id}/mascotas`, newMascota)
      setNewMascota({ nombre: '', especie: '', raza: '' })
      setShowMascotaForm(null)
      fetchClientes()
    } catch (err) {
      toast.error("Error al registrar mascota")
      setTimeout(() => toast.error(null), 3000)
    }
  }

  const handleDeleteCliente = async (id) => {
    if(!window.confirm("¿Estás seguro de eliminar este cliente? Se borrarán sus mascotas y citas asociadas.")) return;
    try {
      await api.delete(`/api/clientes/${id}`)
      fetchClientes()
    } catch(err) {
      toast.error("Error al eliminar cliente.")
      setTimeout(() => toast.error(null), 3000)
    }
  }

  const filteredClientes = clientes.filter(c => 
    c.nombre.toLowerCase().includes(searchTerm.toLowerCase()) || 
    c.dni.includes(searchTerm)
  )

  const columns = [
    { 
      header: 'Cliente', 
      accessor: 'nombre',
      render: (item) => (
        <div>
          <p className="font-bold text-slate-900 flex items-center">
            <User className="w-4 h-4 mr-2 text-indigo-600" />
            {item.nombre}
          </p>
          <p className="text-xs text-slate-500 mt-1">DNI: {item.dni}</p>
        </div>
      )
    },
    { 
      header: 'Contacto', 
      accessor: 'contacto',
      render: (item) => (
        <div>
          <p className="font-medium text-slate-600">{item.telefono}</p>
          {item.email && <p className="text-xs text-slate-500">{item.email}</p>}
        </div>
      )
    },
    {
      header: 'Mascotas',
      accessor: 'mascotas',
      render: (item) => (
        <div className="flex flex-wrap gap-2">
          {item.mascotas && item.mascotas.length > 0 ? (
            item.mascotas.map(m => (
              <span key={m.id} className="inline-flex items-center px-2 py-1 rounded-md text-xs font-medium bg-indigo-50 text-indigo-600 border border-indigo-100">
                <PawPrint className="w-3 h-3 mr-1" />
                {m.nombre} ({m.especie})
              </span>
            ))
          ) : (
            <span className="text-xs text-slate-500 italic">Sin mascotas</span>
          )}
        </div>
      )
    },
    {
      header: 'Acciones',
      accessor: 'acciones',
      render: (item) => (
        <div className="flex space-x-2">
          <Button variant="ghost" onClick={() => setShowMascotaForm(item.id)}>
            <Plus className="w-4 h-4 mr-1" /> Mascota
          </Button>
          <RoleGate allowedRoles={['Administrador']}>
            <button onClick={() => handleDeleteCliente(item.id)} className="text-rose-600 hover:text-rose-600 bg-rose-50 hover:bg-rose-100 p-2 rounded-xl transition-colors" title="Eliminar Cliente">
              <Trash2 className="w-4 h-4" />
            </button>
          </RoleGate>
        </div>
      )
    }
  ]

  return (
    <div className="space-y-6 animate-fade-in-up">
      <PageHeader 
        title="Directorio de Clientes" 
        description="Gestión de propietarios y sus mascotas asociadas"
        actions={
          <Button onClick={() => setShowForm(true)} variant="primary">
            <UserPlus className="w-4 h-4 mr-2" /> Nuevo Cliente
          </Button>
        }
      />

      
      {/* Buscador */}
      <div className="bg-white backdrop-blur-xl border border-slate-100 rounded-2xl p-4 flex items-center shadow-sm">
        <Search className="w-5 h-5 text-slate-400 mr-3" />
        <input 
          type="text" 
          placeholder="Buscar cliente por nombre o DNI..." 
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          className="bg-transparent border-none text-slate-900 w-full focus:outline-none placeholder:text-slate-500"
        />
      </div>

      <Modal isOpen={showForm} onClose={() => setShowForm(false)} title="Registrar Nuevo Cliente" maxWidth="max-w-2xl">
        <form onSubmit={handleCreateCliente} className="space-y-4">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <Input label="Nombre Completo" required type="text" value={newCliente.nombre} onChange={e => setNewCliente({...newCliente, nombre: e.target.value})} />
            <Input label="DNI / Documento" required type="text" value={newCliente.dni} onChange={e => setNewCliente({...newCliente, dni: e.target.value})} />
            <Input label="Teléfono" required type="text" value={newCliente.telefono} onChange={e => setNewCliente({...newCliente, telefono: e.target.value})} />
            <Input label="Correo Electrónico (Opcional)" type="email" value={newCliente.email} onChange={e => setNewCliente({...newCliente, email: e.target.value})} />
          </div>
          <div className="border-t border-slate-100 pt-4">
            <p className="text-xs font-bold text-slate-400 uppercase tracking-widest mb-3">Cuenta de acceso del cliente (Opcional)</p>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <Input label="Usuario (para el Portal y la Tienda)" type="text" value={newCliente.username} onChange={e => setNewCliente({...newCliente, username: e.target.value})} />
              <Input label="Contraseña" type="password" value={newCliente.password} onChange={e => setNewCliente({...newCliente, password: e.target.value})} />
            </div>
            <p className="text-xs text-slate-500 mt-2">Si completas ambos, el cliente podrá iniciar sesión en su portal y en la tienda. Déjalos vacíos si solo deseas registrarlo en el directorio.</p>
          </div>
          <div className="flex justify-end space-x-3 pt-4">
            <Button type="button" variant="ghost" onClick={() => setShowForm(false)}>Cancelar</Button>
            <Button type="submit" variant="primary">Registrar Cliente</Button>
          </div>
        </form>
      </Modal>

      <Modal isOpen={showMascotaForm !== null} onClose={() => setShowMascotaForm(null)} title="Registrar Mascota" maxWidth="max-w-md">
        <form onSubmit={(e) => handleCreateMascota(e, showMascotaForm)} className="space-y-4">
          <Input label="Nombre" required type="text" value={newMascota.nombre} onChange={e => setNewMascota({...newMascota, nombre: e.target.value})} />
          <Input label="Especie" required type="text" value={newMascota.especie} onChange={e => setNewMascota({...newMascota, especie: e.target.value})} />
          <Input label="Raza (Opcional)" type="text" value={newMascota.raza} onChange={e => setNewMascota({...newMascota, raza: e.target.value})} />
          <div className="flex justify-end space-x-3 pt-4">
            <Button type="button" variant="ghost" onClick={() => setShowMascotaForm(null)}>Cancelar</Button>
            <Button type="submit" variant="primary">Registrar Mascota</Button>
          </div>
        </form>
      </Modal>

      {loading ? (
        <TableSkeleton />
      ) : (
        <DataTable 
          columns={columns}
          data={filteredClientes}
          emptyMessage="No se encontraron clientes."
        />
      )}
    </div>
  )
}
