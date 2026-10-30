import { useState, useEffect } from 'react'
import { useToast } from '../components/ui/ToastProvider'
import { TableSkeleton } from '../components/ui/Skeleton'
import api from '../api/axiosConfig'
import { ShoppingCart, Search, Plus, Minus, Trash2, Printer, User, Package, DollarSign, AlertTriangle, RotateCcw, Receipt } from 'lucide-react'
import { RoleGate } from '../components/RoleGate'
import PageHeader from '../components/ui/PageHeader'
import DataTable from '../components/ui/DataTable'
import Button from '../components/ui/Button'
import Modal from '../components/ui/Modal'
import Input from '../components/ui/Input'
import jsPDF from 'jspdf'
import 'jspdf-autotable'

export default function Ventas() {
  const toast = useToast();
  const [ventas, setVentas] = useState([])
  const [clientes, setClientes] = useState([])
  const [productos, setProductos] = useState([])
  
  const [showPOS, setShowPOS] = useState(false)
  const [selectedCliente, setSelectedCliente] = useState('')
  const [metodoPago, setMetodoPago] = useState('Efectivo')
  const [tipoComprobante, setTipoComprobante] = useState('Boleta')
  const [tipoDocumento, setTipoDocumento] = useState('DNI')
  const [documentoFiscal, setDocumentoFiscal] = useState('')
  const [carrito, setCarrito] = useState([])
  const [searchProd, setSearchProd] = useState('')
  const [filtroCliente, setFiltroCliente] = useState('')
  
  const [loading, setLoading] = useState(true)
  
  const [showDevolverModal, setShowDevolverModal] = useState(false)
  const [devolverVenta, setDevolverVenta] = useState(null)
  const [devolverTipo, setDevolverTipo] = useState('EXTORNO')
  const [devolverMotivo, setDevolverMotivo] = useState('')
  const [showNotasCredito, setShowNotasCredito] = useState(false)
  const [notasCredito, setNotasCredito] = useState([])
  const [loadingNotas, setLoadingNotas] = useState(false)
    
  const fetchData = async () => {
    try {
      setLoading(true)
      const [ventasRes, cliRes, prodRes] = await Promise.all([
        api.get('/api/ventas'),
        api.get('/api/clientes'),
        api.get('/api/productos')
      ])
      setVentas(ventasRes.data)
      setClientes(cliRes.data)
      setProductos(prodRes.data)
    } catch (err) {
      toast.error("Error al cargar datos del sistema.")
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchData()
  }, [])

  const hoyStr = new Date().toISOString().slice(0, 10)
  const en30Str = new Date(Date.now() + 30 * 86400000).toISOString().slice(0, 10)

  const esProductoVencido = (p) => p.fecha_vencimiento && p.fecha_vencimiento < hoyStr
  const esNoDisponible = (p) => {
    if (p.stock_por_almacen && p.stock_por_almacen.length) {
      const disp = p.stock_por_almacen.find(s => s.almacen === 'Disponible')
      return !disp || disp.cantidad <= 0
    }
    return p.estado_almacen && p.estado_almacen !== 'Disponible'
  }

  const maxDigitos = { DNI: 8, RUC: 11, CE: 12 }
  const tipoDocLabel = (doc, comprobante) => {
    const d = (doc || '').trim()
    if (comprobante === 'Factura') return 'RUC'
    if (d.length === 12) return 'CE'
    return 'DNI'
  }

  const filteredProductos = productos.filter(p => 
    (p.nombre.toLowerCase().includes(searchProd.toLowerCase()) || 
     p.sku.toLowerCase().includes(searchProd.toLowerCase())) && p.stock > 0 && !esNoDisponible(p)
  )

  const addToCart = (prod) => {
    if (esProductoVencido(prod)) {
      toast.error(`${prod.nombre} está vencido y no puede venderse.`)
      return
    }
    const exists = carrito.find(item => item.producto_id === prod.id)
    if (exists) {
      if (exists.cantidad >= prod.stock) {
        toast.error(`Stock máximo de ${prod.nombre} alcanzado.`)
        setTimeout(() => toast.error(null), 3000)
        return
      }
      setCarrito(carrito.map(item => item.producto_id === prod.id ? { ...item, cantidad: item.cantidad + 1 } : item))
    } else {
      setCarrito([...carrito, { producto_id: prod.id, nombre: prod.nombre, precio_unitario: prod.precio, cantidad: 1, max_stock: prod.stock }])
    }
  }

  const updateCartQty = (id, delta) => {
    setCarrito(carrito.map(item => {
      if (item.producto_id === id) {
        const newQty = item.cantidad + delta
        if (newQty > 0 && newQty <= item.max_stock) return { ...item, cantidad: newQty }
      }
      return item
    }))
  }

  const removeFromCart = (id) => {
    setCarrito(carrito.filter(item => item.producto_id !== id))
  }

  const total = carrito.reduce((acc, item) => acc + (item.precio_unitario * item.cantidad), 0)

  const ventasFiltradas = filtroCliente
    ? ventas.filter(v => v.cliente_id === parseInt(filtroCliente))
    : ventas
  const totalVentasFiltrado = ventasFiltradas.reduce((acc, v) => acc + v.total, 0)

  const generatePDF = (ventaData, clienteNombre) => {
    const doc = new jsPDF()
    doc.setFontSize(18)
    doc.text("VETERINARIA SIRBULL", 14, 20)
    
    doc.setFontSize(9)
    doc.text("Av. Los Gladiolos 123, San Juan de Lurigancho", 14, 26)
    doc.text("RUC: 20513245442", 14, 31)

    doc.setFontSize(12)
    doc.text("COMPROBANTE DE PAGO", 14, 39)
    doc.setFontSize(10)
    doc.text(`Fecha: ${new Date().toLocaleString()}`, 14, 47)
    doc.text(`Cliente: ${clienteNombre}`, 14, 52)
    doc.text(`Método de Pago: ${ventaData.metodo_pago}`, 14, 57)
    doc.text(`Comprobante: ${ventaData.tipo_comprobante}${ventaData.documento_fiscal ? ` (${tipoDocLabel(ventaData.documento_fiscal, ventaData.tipo_comprobante)}: ${ventaData.documento_fiscal})` : ''}`, 14, 62)
    
    const tableColumn = ["Producto", "Cant.", "Precio Unit.", "Subtotal"]
    const tableRows = []

    carrito.forEach(item => {
      const rowData = [
        item.nombre,
        item.cantidad.toString(),
        `$${item.precio_unitario.toFixed(2)}`,
        `$${(item.precio_unitario * item.cantidad).toFixed(2)}`
      ]
      tableRows.push(rowData)
    })

    doc.autoTable({
      head: [tableColumn],
      body: tableRows,
      startY: 67,
      theme: 'grid',
      headStyles: { fillColor: [34, 211, 238] }
    })

    const finalY = doc.lastAutoTable.finalY || 67
    doc.setFontSize(12)
    doc.setFont(undefined, 'bold')
    doc.text(`TOTAL: $${ventaData.total.toFixed(2)}`, 14, finalY + 10)

    doc.save(`Comprobante_Sirbull_${Date.now()}.pdf`)
  }

  const handleCheckout = async () => {
    if (!selectedCliente) {
      toast.error("Por favor seleccione un cliente.")
      return
    }
    if (carrito.length === 0) {
      toast.error("El carrito está vacío.")
      return
    }

    const doc = documentoFiscal.trim()
    if (tipoDocumento === 'RUC' && !/^\d{11}$/.test(doc)) {
      toast.error("Ingresa un RUC válido de 11 dígitos para emitir Factura.")
      return
    }
    if (tipoDocumento === 'DNI' && !/^\d{8}$/.test(doc)) {
      toast.error("Ingresa un DNI válido de 8 dígitos.")
      return
    }
    if (tipoDocumento === 'CE' && !/^20\d{10}$/.test(doc)) {
      toast.error("Ingresa un Carnet de Extranjería válido (12 dígitos, inicia con 20).")
      return
    }

    try {
      const payload = {
        cliente_id: parseInt(selectedCliente),
        metodo_pago: metodoPago,
        tipo_comprobante: tipoComprobante,
        documento_fiscal: doc,
        detalles: carrito.map(c => ({
          producto_id: c.producto_id,
          cantidad: c.cantidad,
          precio_unitario: c.precio_unitario
        }))
      }
      
      const res = await api.post('/api/ventas', payload)
      
      const clienteInfo = clientes.find(c => c.id === parseInt(selectedCliente))
      generatePDF(res.data, clienteInfo ? clienteInfo.nombre : 'Consumidor Final')
      
      toast.success("¡Venta registrada exitosamente!")
      setCarrito([])
      setSelectedCliente('')
      setShowPOS(false)
      
      fetchData() // refresh all (ventas and stock)
      
      setTimeout(() => toast.success(null), 5000)
    } catch (err) {
      toast.error(err.response?.data?.detail || "Error al procesar la venta.")
      setTimeout(() => toast.error(null), 5000)
    }
  }

  const handleChangeEstado = async (ventaId, estado) => {
    try {
      await api.put(`/api/ventas/${ventaId}/estado`, null, { params: { estado } })
      toast.success(`Venta ${estado.toLowerCase()} correctamente.`)
      fetchData()
      setTimeout(() => toast.success(null), 4000)
    } catch (err) {
      toast.error(err.response?.data?.detail || "Error al actualizar el estado de la venta.")
      setTimeout(() => toast.error(null), 4000)
    }
  }

  const ventaBadgeClass = (estado) => {
    const styles = {
      'Pendiente': 'bg-amber-50 text-amber-700 border-amber-200',
      'Confirmada': 'bg-emerald-50 text-emerald-700 border-emerald-200',
      'Entregada': 'bg-emerald-50 text-emerald-700 border-emerald-200',
      'Cancelada': 'bg-slate-100 text-slate-600 border-slate-200',
      'Anulación solicitada': 'bg-orange-50 text-orange-700 border-orange-200',
    }
    return `px-2 py-0.5 rounded-full text-xs font-bold border ${styles[estado] || styles['Pendiente']}`
  }

  const openDevolver = (venta) => {
    setDevolverVenta(venta)
    setDevolverTipo('EXTORNO')
    setDevolverMotivo('')
    setShowDevolverModal(true)
  }

  const handleDevolver = async () => {
    if (!devolverTipo) {
      toast.error("Seleccione el tipo de devolución.")
      return
    }
    try {
      await api.post(`/api/ventas/${devolverVenta.id}/devolver`, {
        tipo: devolverTipo,
        motivo: devolverMotivo.trim() || null
      })
      toast.success("Devolución registrada correctamente.")
      setShowDevolverModal(false)
      setDevolverVenta(null)
      fetchData()
      setTimeout(() => toast.success(null), 4000)
    } catch (err) {
      toast.error(err.response?.data?.detail || "Error al registrar la devolución.")
      setTimeout(() => toast.error(null), 4000)
    }
  }

  const openNotasCredito = async () => {
    setShowNotasCredito(true)
    setLoadingNotas(true)
    try {
      const res = await api.get('/api/notas-credito')
      setNotasCredito(res.data)
    } catch (err) {
      toast.error("Error al cargar notas de crédito.")
    } finally {
      setLoadingNotas(false)
    }
  }

  const columns = [
    {
      header: 'ID / Fecha',
      accessor: 'fecha',
      render: (item) => {
        const date = new Date(item.fecha)
        return (
          <div>
            <p className="font-bold text-slate-900 flex items-center">
              <ShoppingCart className="w-4 h-4 mr-2 text-indigo-600" />
              VNT-{String(item.id).padStart(4, '0')}
            </p>
            <p className="text-xs text-slate-500 mt-1">{date.toLocaleString()}</p>
          </div>
        )
      }
    },
    {
      header: 'Cliente',
      accessor: 'cliente_id',
      render: (item) => {
        const clienteInfo = clientes.find(c => c.id === item.cliente_id)
        return <span className="font-medium text-slate-600">{clienteInfo ? clienteInfo.nombre : `ID: ${item.cliente_id}`}</span>
      }
    },
    {
      header: 'Comprobante',
      accessor: 'tipo_comprobante',
      render: (item) => (
        <div>
          <p className="text-slate-600">{item.tipo_comprobante}</p>
          {item.documento_fiscal && (
            <p className="text-xs text-slate-400">{item.tipo_comprobante === 'Factura' ? 'RUC' : 'DNI'}: {item.documento_fiscal}</p>
          )}
        </div>
      )
    },
    {
      header: 'Código de Recojo',
      accessor: 'codigo_recojo',
      render: (item) => (
        <div>
          <p className="font-mono font-bold text-indigo-600 text-sm">{item.codigo_recojo || '—'}</p>
          {item.comprobante && <p className="font-mono text-xs text-slate-400">{item.comprobante}</p>}
        </div>
      )
    },
    {
      header: 'Total',
      accessor: 'total',
      render: (item) => <span className="font-bold text-indigo-600">${item.total.toFixed(2)}</span>
    },
    {
      header: 'Pago',
      accessor: 'metodo_pago',
      render: (item) => <span className="text-slate-600 text-sm">{item.metodo_pago}</span>
    },
    {
      header: 'Estado',
      accessor: 'estado',
      render: (item) => <span className={`inline-block ${ventaBadgeClass(item.estado)}`}>{item.estado}</span>
    },
    {
      header: 'Acciones',
      accessor: 'acciones',
      render: (item) => (
        <div className="flex items-center space-x-2">
          {item.estado === 'Pendiente' && (
            <Button variant="primary" size="sm" onClick={() => handleChangeEstado(item.id, 'Confirmada')}>
              Confirmar
            </Button>
          )}
          {item.estado === 'Confirmada' && (
            <Button variant="secondary" size="sm" onClick={() => handleChangeEstado(item.id, 'Entregada')}>
              Entregar
            </Button>
          )}
          {(item.estado === 'Pendiente' || item.estado === 'Confirmada') && (
            <Button variant="danger" size="sm" onClick={() => handleChangeEstado(item.id, 'Cancelada')}>
              Cancelar
            </Button>
          )}
          {item.estado === 'Anulación solicitada' && (
            <>
              <Button variant="danger" size="sm" onClick={() => handleChangeEstado(item.id, 'Cancelada')}>
                Aprobar Anulación
              </Button>
              <Button variant="secondary" size="sm" onClick={() => handleChangeEstado(item.id, 'Confirmada')}>
                Rechazar
              </Button>
            </>
          )}
          {item.estado === 'Entregada' && (
            <Button variant="secondary" size="sm" onClick={() => openDevolver(item)}>
              <RotateCcw className="w-3.5 h-3.5 mr-1" /> Devolver
            </Button>
          )}
          {item.estado === 'Cancelada' ? (
            <span className="text-slate-400 text-xs">—</span>
          ) : null}
        </div>
      )
    }
  ]

  return (
    <div className="space-y-6 animate-fade-in-up">
      <PageHeader 
        title="Historial de Ventas" 
        description="Listado de transacciones y facturación"
        actions={
          <RoleGate allowedRoles={['Administrador', 'Recepcionista']}>
            <div className="flex items-center space-x-2">
              <Button onClick={openNotasCredito} variant="secondary">
                <Receipt className="w-4 h-4 mr-2" /> Notas de Crédito
              </Button>
              <Button onClick={() => setShowPOS(true)} variant="primary">
                <Plus className="w-4 h-4 mr-2" /> Nueva Venta
              </Button>
            </div>
          </RoleGate>
        }
      />

            
      {loading ? (
        <TableSkeleton />
      ) : (
        <>
          <div className="flex flex-col md:flex-row md:items-end gap-3 bg-white rounded-2xl border border-slate-100 shadow-sm p-4">
            <div className="w-full md:w-72">
              <label className="block text-xs font-bold text-slate-400 uppercase tracking-widest mb-2">Filtrar por Cliente</label>
              <select className="w-full bg-white border border-slate-200 rounded-xl px-4 py-3 text-slate-900 focus:ring-1 focus:ring-indigo-500 outline-none transition-all" value={filtroCliente} onChange={e => setFiltroCliente(e.target.value)}>
                <option value="" className="bg-white">Todos los clientes</option>
                {clientes.map(c => <option key={c.id} value={c.id} className="bg-white">{c.nombre}</option>)}
              </select>
            </div>
            <div className="text-sm text-slate-500 pb-3">
              <span className="font-bold text-slate-700">{ventasFiltradas.length}</span> venta(s)
              {filtroCliente && <span> · Total: <span className="font-bold text-emerald-600">${totalVentasFiltrado.toFixed(2)}</span></span>}
            </div>
          </div>
          <DataTable
            columns={columns}
            data={ventasFiltradas}
            emptyMessage="No hay ventas registradas."
          />
        </>
      )}

      {/* POS Modal */}
      <Modal isOpen={showPOS} onClose={() => setShowPOS(false)} title="Punto de Venta" maxWidth="max-w-6xl">
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          
          {/* Catálogo de Productos */}
          <div className="lg:col-span-2 flex flex-col h-[60vh]">
            <div className="relative mb-4">
              <Search className="absolute left-3 top-2.5 h-5 w-5 text-slate-400" />
              <input 
                type="text"
                placeholder="Buscar producto por nombre o código..."
                value={searchProd}
                onChange={(e) => setSearchProd(e.target.value)}
                className="w-full pl-10 pr-4 py-2 bg-white border border-slate-200 rounded-xl text-slate-900 focus:ring-1 focus:ring-indigo-500 outline-none"
              />
            </div>
            
            <div className="flex-1 overflow-y-auto grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4 pr-2">
              {filteredProductos.map(prod => {
                const vencido = esProductoVencido(prod)
                const porVencer = !vencido && prod.fecha_vencimiento && prod.fecha_vencimiento <= en30Str
                return (
                  <div key={prod.id} onClick={() => !vencido && addToCart(prod)} className={`bg-white border rounded-xl flex flex-col justify-between p-4 transition-colors ${vencido ? 'opacity-60 cursor-not-allowed border-rose-200' : 'cursor-pointer group hover:border-indigo-300'}`}>
                    <div>
                      <div className="flex items-center justify-between mb-1">
                        <div className="text-xs text-indigo-600 font-mono">{prod.sku}</div>
                        {vencido ? (
                          <span className="inline-flex items-center px-2 py-0.5 rounded-md text-[10px] font-bold bg-rose-50 text-rose-600">
                            <AlertTriangle className="w-3 h-3 mr-1" /> VENCIDO
                          </span>
                        ) : porVencer ? (
                          <span className="inline-flex items-center px-2 py-0.5 rounded-md text-[10px] font-bold bg-amber-50 text-amber-600">
                            VENCE {prod.fecha_vencimiento}
                          </span>
                        ) : null}
                      </div>
                      <h4 className="font-bold text-slate-900 leading-tight group-hover:text-indigo-600 transition-colors">{prod.nombre}</h4>
                    </div>
                    <div className="mt-4 flex justify-between items-end">
                      <span className="text-xs bg-indigo-50 text-indigo-600 px-2 py-1 rounded-md font-bold">Stock: {prod.stock}</span>
                      <span className="text-lg font-black text-slate-900">${prod.precio.toFixed(2)}</span>
                    </div>
                  </div>
                )
              })}
              {filteredProductos.length === 0 && (
                <div className="col-span-full flex flex-col items-center justify-center text-slate-500 py-12">
                  <Package className="w-12 h-12 mb-2 opacity-50" />
                  <p>No se encontraron productos disponibles para la venta.</p>
                </div>
              )}
            </div>
          </div>

          {/* Panel lateral: Carrito y Checkout */}
          <div className="lg:col-span-1 flex flex-col h-[70vh] bg-white p-4 rounded-xl border border-slate-100">
            <div className="flex items-center justify-between border-b border-slate-200 pb-3 mb-3">
              <h3 className="font-bold text-lg flex items-center text-slate-900">
                <ShoppingCart className="w-5 h-5 mr-2 text-indigo-600" />
                Carrito
              </h3>
              <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-bold bg-indigo-50 text-indigo-600">{carrito.length} item(s)</span>
            </div>

            <div className="flex-1 min-h-0 overflow-y-auto space-y-3 mb-3 pr-1">
              {carrito.length === 0 ? (
                <p className="text-slate-500 text-sm text-center mt-8 italic">Agrega productos para comenzar</p>
              ) : (
                carrito.map(item => (
                  <div key={item.producto_id} className="flex justify-between items-center bg-slate-50 p-3 rounded-xl border border-slate-100">
                    <div className="flex-1 min-w-0">
                      <p className="font-bold text-sm text-slate-900 line-clamp-1">{item.nombre}</p>
                      <p className="text-xs text-indigo-600 font-bold">${item.precio_unitario.toFixed(2)}</p>
                    </div>
                    <div className="flex items-center space-x-1 ml-2">
                      <button onClick={() => updateCartQty(item.producto_id, -1)} className="p-1 bg-slate-100 rounded-lg text-slate-900"><Minus className="w-3 h-3" /></button>
                      <span className="text-sm font-bold w-5 text-center text-slate-900">{item.cantidad}</span>
                      <button onClick={() => updateCartQty(item.producto_id, 1)} className="p-1 bg-slate-100 rounded-lg text-slate-900"><Plus className="w-3 h-3" /></button>
                      <button onClick={() => removeFromCart(item.producto_id)} className="p-1 text-rose-600 hover:bg-rose-100 rounded-lg ml-1"><Trash2 className="w-3 h-3" /></button>
                    </div>
                  </div>
                ))
              )}
            </div>

            <div className="border-t border-slate-200 pt-3">
              <h3 className="font-bold text-slate-900 mb-3 flex items-center text-sm uppercase tracking-widest text-slate-400">
                <User className="w-4 h-4 mr-2 text-indigo-600" />
                Datos de Venta
              </h3>

              <div className="space-y-3 mb-3">
                <select
                  value={selectedCliente}
                  onChange={e => setSelectedCliente(e.target.value)}
                  className="w-full bg-white border border-slate-200 rounded-xl px-4 py-2 text-slate-900 focus:ring-1 focus:ring-indigo-500 outline-none"
                >
                  <option value="" className="bg-white">-- Seleccionar Cliente --</option>
                  {clientes.map(c => <option key={c.id} value={c.id} className="bg-white">{c.nombre} ({c.dni})</option>)}
                </select>

                <div className="grid grid-cols-2 gap-2">
                  <select
                    value={tipoComprobante}
                    onChange={e => {
                      setTipoComprobante(e.target.value)
                      setTipoDocumento(e.target.value === 'Factura' ? 'RUC' : 'DNI')
                      setDocumentoFiscal('')
                    }}
                    className="w-full bg-white border border-slate-200 rounded-xl px-3 py-2 text-slate-900 focus:ring-1 focus:ring-indigo-500 outline-none"
                  >
                    <option value="Boleta" className="bg-white">Boleta</option>
                    <option value="Factura" className="bg-white">Factura</option>
                  </select>
                  <select
                    value={tipoDocumento}
                    onChange={e => setTipoDocumento(e.target.value)}
                    className="w-full bg-white border border-slate-200 rounded-xl px-3 py-2 text-slate-900 focus:ring-1 focus:ring-indigo-500 outline-none"
                  >
                    {tipoComprobante === 'Factura'
                      ? <option value="RUC" className="bg-white">RUC (11 dígitos)</option>
                      : <>
                          <option value="DNI" className="bg-white">DNI (8 dígitos)</option>
                          <option value="CE" className="bg-white">CE (12 dígitos)</option>
                        </>}
                  </select>
                </div>

                <div>
                  <label className="text-xs font-bold text-slate-400 uppercase tracking-widest mb-1 block">
                    Documento Fiscal <span className="normal-case font-normal">({tipoDocumento} · {maxDigitos[tipoDocumento]} dígitos)</span>
                  </label>
                  <input
                    type="text"
                    inputMode="numeric"
                    maxLength={maxDigitos[tipoDocumento]}
                    value={documentoFiscal}
                    onChange={e => setDocumentoFiscal(e.target.value.replace(/\D/g, ''))}
                    placeholder={`${tipoDocumento} (${maxDigitos[tipoDocumento]} dígitos)`}
                    className="w-full bg-white border border-slate-200 rounded-xl px-4 py-2 text-slate-900 focus:ring-1 focus:ring-indigo-500 outline-none"
                  />
                </div>

                <div>
                  <label className="text-xs font-bold text-slate-400 uppercase tracking-widest mb-1 block">
                    Método de Pago
                  </label>
                  <select
                    value={metodoPago}
                    onChange={e => setMetodoPago(e.target.value)}
                    className="w-full bg-white border border-slate-200 rounded-xl px-3 py-2 text-slate-900 focus:ring-1 focus:ring-indigo-500 outline-none"
                  >
                    <option value="Efectivo" className="bg-white">Efectivo</option>
                    <option value="Tarjeta" className="bg-white">Tarjeta</option>
                    <option value="Transferencia" className="bg-white">Transferencia</option>
                  </select>
                </div>
              </div>

              <div className="border-t border-slate-200 pt-3">
                <div className="flex justify-between items-center mb-3">
                  <span className="font-bold text-slate-400">Total:</span>
                  <span className="text-3xl font-black text-indigo-600">${total.toFixed(2)}</span>
                </div>
                <Button
                  onClick={handleCheckout}
                  disabled={carrito.length === 0}
                  variant="primary"
                  className="w-full flex justify-center py-4"
                >
                  <DollarSign className="w-5 h-5 mr-2" />
                  Cobrar e Imprimir
                </Button>
              </div>
            </div>
          </div>
        </div>
      </Modal>

      {/* Modal Devolución */}
      <Modal isOpen={showDevolverModal} onClose={() => setShowDevolverModal(false)} title="Registrar Devolución" maxWidth="max-w-md">
        {devolverVenta && (
          <div className="space-y-5">
            <div className="p-4 rounded-xl bg-slate-50 border border-slate-200">
              <p className="text-sm text-slate-600">
                Venta <span className="font-bold text-slate-900">VNT-{String(devolverVenta.id).padStart(4, '0')}</span> ·{' '}
                <span className="font-bold text-indigo-600">${devolverVenta.total.toFixed(2)}</span>
              </p>
              <p className="text-xs text-slate-400 mt-1">
                Comprobante: {devolverVenta.comprobante || `${devolverVenta.tipo_comprobante} sin serie`}
              </p>
            </div>

            <div>
              <label className="block text-xs font-bold text-slate-400 uppercase tracking-widest mb-2">Tipo de Devolución</label>
              <select
                value={devolverTipo}
                onChange={(e) => setDevolverTipo(e.target.value)}
                className="w-full bg-white border border-slate-200 rounded-xl px-4 py-3 text-slate-900 focus:ring-1 focus:ring-indigo-500 outline-none"
              >
                <option value="EXTORNO">Extorno (devolución del dinero)</option>
                <option value="NOTA_CREDITO">Nota de Crédito</option>
              </select>
            </div>

            <div>
              <label className="block text-xs font-bold text-slate-400 uppercase tracking-widest mb-2">Motivo</label>
              <textarea
                value={devolverMotivo}
                onChange={(e) => setDevolverMotivo(e.target.value)}
                rows={3}
                placeholder="Ej: Producto en mal estado, cliente no conforme..."
                className="w-full bg-white border border-slate-200 rounded-xl px-4 py-3 text-slate-900 focus:ring-1 focus:ring-indigo-500 outline-none"
              />
            </div>

            <div className="flex items-center justify-end space-x-2 pt-2">
              <Button variant="secondary" onClick={() => setShowDevolverModal(false)}>Cancelar</Button>
              <Button variant="primary" onClick={handleDevolver}>
                <RotateCcw className="w-4 h-4 mr-2" /> Registrar Devolución
              </Button>
            </div>
          </div>
        )}
      </Modal>

      {/* Modal Notas de Crédito */}
      <Modal isOpen={showNotasCredito} onClose={() => setShowNotasCredito(false)} title="Notas de Crédito" maxWidth="max-w-3xl">
        {loadingNotas ? (
          <TableSkeleton rows={3} columns={4} />
        ) : notasCredito.length === 0 ? (
          <p className="text-slate-500 text-sm text-center py-8">No hay notas de crédito emitidas.</p>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="text-left text-xs uppercase tracking-widest text-slate-400 border-b border-slate-200">
                  <th className="py-2 pr-3 font-bold">Código</th>
                  <th className="py-2 pr-3 font-bold">Venta</th>
                  <th className="py-2 pr-3 font-bold">Cliente</th>
                  <th className="py-2 pr-3 font-bold">Monto</th>
                  <th className="py-2 pr-3 font-bold">Saldo</th>
                  <th className="py-2 pr-3 font-bold">Estado</th>
                  <th className="py-2 font-bold">Vence</th>
                </tr>
              </thead>
              <tbody>
                {notasCredito.map((nc) => (
                  <tr key={nc.id} className="border-b border-slate-100">
                    <td className="py-2.5 pr-3 font-mono font-bold text-indigo-600">{nc.codigo}</td>
                    <td className="py-2.5 pr-3 text-slate-600">VNT-{String(nc.venta_id || '?').padStart(4, '0')}</td>
                    <td className="py-2.5 pr-3 text-slate-600">{nc.cliente_nombre || `ID: ${nc.cliente_id}`}</td>
                    <td className="py-2.5 pr-3 font-bold text-slate-900">${nc.monto.toFixed(2)}</td>
                    <td className="py-2.5 pr-3 text-slate-600">${nc.saldo.toFixed(2)}</td>
                    <td className="py-2.5 pr-3">
                      <span className={`inline-block px-2 py-0.5 rounded-full text-xs font-bold border ${
                        nc.estado === 'ACTIVA' ? 'bg-emerald-50 text-emerald-700 border-emerald-200'
                        : nc.estado === 'CANJEADA' ? 'bg-slate-100 text-slate-500 border-slate-200'
                        : 'bg-slate-100 text-slate-500 border-slate-200'
                      }`}>{nc.estado}</span>
                    </td>
                    <td className="py-2.5 text-slate-600">{nc.fecha_vencimiento ? new Date(nc.fecha_vencimiento).toLocaleDateString() : '—'}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </Modal>
    </div>
  )
}
