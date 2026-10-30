import { useState, useEffect, useRef } from 'react'
import { useToast } from '../components/ui/ToastProvider'
import { TableSkeleton } from '../components/ui/Skeleton'
import api from '../api/axiosConfig'
import { useAuthStore } from '../store/authStore'
import { Package, Plus, AlertTriangle, Trash2, Pencil, Truck, FileInput, PlusCircle, MinusCircle, Eye, History, ScanLine, Lock, Tags, PencilLine, Warehouse, CheckCircle2, Sparkles } from 'lucide-react'
import { Html5Qrcode } from 'html5-qrcode'
import { RoleGate } from '../components/RoleGate'
import PageHeader from '../components/ui/PageHeader'
import DataTable from '../components/ui/DataTable'
import Button from '../components/ui/Button'
import Modal from '../components/ui/Modal'
import Input from '../components/ui/Input'

const TAB_PRODUCTOS = 'productos'
const TAB_PROVEEDORES = 'proveedores'
const TAB_INGRESOS = 'ingresos'
const TAB_MOVIMIENTOS = 'movimientos'
const TAB_ALMACENES = 'almacenes'
const TAB_CATALOGO = 'catalogos'

const ESTADOS_ALMACEN = ['Disponible', 'No disponible', 'Dañado', 'Vencido']

const TIPO_MOVIMIENTOS = ['INGRESO', 'VENTA', 'AJUSTE', 'CAMBIO_ALMACEN', 'ANULACION_INGRESO']

const MONEDAS = ['SOL', 'USD']
const TIPOS_COMPROBANTE = ['Factura', 'Boleta']

const AUTO_CATEGORIAS = [
  { nombre: 'Alimento para Perros', keywords: ['dogchow', 'dog', 'croqueta', 'pedigree', 'raza', 'labrador', 'comida seca', 'humeda', 'sobres', 'lata', 'premium'] },
  { nombre: 'Alimento para Gatos', keywords: ['gato', 'whiskas', 'cat'] },
  { nombre: 'Golosinas y Snacks', keywords: ['snack', 'premio', 'bocadito', 'galleta'] },
  { nombre: 'Higiene y Aseo', keywords: ['champu', 'jabon', 'shampoo', 'cepillo', 'peine'] },
  { nombre: 'Farmacia y Vacunas', keywords: ['vacuna', 'antibiotico', 'analgesico', 'jeringa', 'antipulgas', 'garrapatas', 'desparasitante', 'amoxicilina'] },
  { nombre: 'Juguetes', keywords: ['pelota', 'hueso', 'cuerda', 'mordedor', 'juguete'] },
  { nombre: 'Accesorios', keywords: ['correa', 'collar', 'arnes', 'cama', 'manta', 'colchon'] },
  { nombre: 'Otros', keywords: [] },
]
const AUTO_MARCAS = ['DogChow', 'Purina', 'Pro Plan', 'Royal Canin', "Hill's", 'Pedigree', 'Whiskas', 'Optimum', 'NexGard', 'Bravecto', 'Advantage', 'Kong']

const SECCIONES_CATALOGO = [
  { tipo: 'categorias', titulo: 'Categorías', color: 'indigo' },
  { tipo: 'familias', titulo: 'Familias', color: 'emerald' },
  { tipo: 'marcas', titulo: 'Marcas', color: 'amber' },
]

const selectCls = "w-full bg-white border border-slate-200 rounded-xl px-4 py-3 text-slate-900 focus:ring-1 focus:ring-indigo-500 outline-none transition-all"

export default function Inventario() {
  const toast = useToast();
  const role = useAuthStore(state => state.role)
  const isVet = role === 'Veterinario'
  const [tab, setTab] = useState(TAB_PRODUCTOS)

  const [productos, setProductos] = useState([])
  const [proveedores, setProveedores] = useState([])
  const [ingresos, setIngresos] = useState([])
  const [movimientos, setMovimientos] = useState([])
  const [categorias, setCategorias] = useState([])
  const [familias, setFamilias] = useState([])
  const [marcas, setMarcas] = useState([])
  const [loading, setLoading] = useState(true)

  const [showProductoForm, setShowProductoForm] = useState(false)
  const [editingProducto, setEditingProducto] = useState(null)
  const [productoForm, setProductoForm] = useState({ sku: '', codigo_barra: '', nombre: '', descripcion: '', precio: 0, stock: 0, stock_minimo: 5, proveedor_id: '', imagen: '', fecha_vencimiento: '', es_perecible: false, precio_compra: 0, estado_almacen: 'Disponible', categoria_id: '', familia_id: '', marca_id: '' })

  const [showScanner, setShowScanner] = useState(false)
  const [scanError, setScanError] = useState('')
  const scanTargetRef = useRef('codigo_barra')

  const [showCatalogoForm, setShowCatalogoForm] = useState(false)
  const [catalogoTipo, setCatalogoTipo] = useState('categorias')
  const [catalogoForm, setCatalogoForm] = useState({ nombre: '' })
  const [editandoCatalogo, setEditandoCatalogo] = useState(null)

  const [showProveedorForm, setShowProveedorForm] = useState(false)
  const [editingProveedor, setEditingProveedor] = useState(null)
  const [proveedorForm, setProveedorForm] = useState({ ruc: '', razon_social: '', contacto: '', telefono: '', direccion: '', email: '', moneda: 'SOL' })

  const [showIngresoForm, setShowIngresoForm] = useState(false)
  const [showIngresoResumen, setShowIngresoResumen] = useState(false)
  const [ingresoForm, setIngresoForm] = useState({ proveedor_id: '', tipo_pago: 'Contado', tipo_comprobante: 'Factura', serie: '', numero: '' })
  const [ingresoItems, setIngresoItems] = useState([{ producto_id: '', cantidad: 1, precio_unitario: 0, porcentaje: 25, precio_venta: 0, fecha_vencimiento: '' }])
  const [ingresoDetail, setIngresoDetail] = useState(null)
  const [ingresoFiltroProveedor, setIngresoFiltroProveedor] = useState('')
  const [movForm, setMovForm] = useState({ producto_id: '', cantidad: 1, almacen_origen: 'Disponible', almacen_destino: 'Dañado', observacion: '' })
  const [movFiltro, setMovFiltro] = useState({ producto_id: '', tipo: '' })
  const [almacenFiltro, setAlmacenFiltro] = useState('Todos')
  const [clasifDraft, setClasifDraft] = useState({})
  const catCache = {}
  const famCache = {}
  const marCache = {}

  const fetchProductos = async () => {
    const res = await api.get('/api/productos')
    setProductos(res.data)
  }

  const fetchProveedores = async () => {
    const res = await api.get('/api/proveedores')
    setProveedores(res.data)
  }

  const fetchIngresos = async () => {
    const res = await api.get('/api/ingresos')
    setIngresos(res.data)
  }

  const fetchMovimientos = async () => {
    const res = await api.get('/api/movimientos-stock')
    setMovimientos(res.data)
  }

  const fetchCategorias = async () => {
    const res = await api.get('/api/categorias')
    setCategorias(res.data)
  }

  const fetchFamilias = async () => {
    const res = await api.get('/api/familias')
    setFamilias(res.data)
  }

  const fetchMarcas = async () => {
    const res = await api.get('/api/marcas')
    setMarcas(res.data)
  }

  const fetchAll = async () => {
    try {
      setLoading(true)
      if (isVet) {
        await fetchProductos()
      } else {
        await Promise.all([fetchProductos(), fetchProveedores(), fetchIngresos(), fetchMovimientos(), fetchCategorias(), fetchFamilias(), fetchMarcas()])
      }
    } catch (err) {
      toast.error("Error al cargar datos del inventario")
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchAll()
  }, [])

  const resetProductoForm = () => {
    setProductoForm({ sku: '', codigo_barra: '', nombre: '', descripcion: '', precio: 0, stock: 0, stock_minimo: 5, proveedor_id: '', imagen: '', fecha_vencimiento: '', es_perecible: false, precio_compra: 0, estado_almacen: 'Disponible', categoria_id: '', familia_id: '', marca_id: '' })
    setEditingProducto(null)
  }

  const openNewProducto = async () => {
    resetProductoForm()
    setShowProductoForm(true)
    try {
      const res = await api.get('/api/productos/next-sku')
      setProductoForm(f => ({ ...f, sku: res.data.sku }))
    } catch (err) {
      toast.error(err.response?.data?.detail || "No se pudo generar el SKU")
    }
  }

  const openEditProducto = (item) => {
    setEditingProducto(item)
    setProductoForm({
      sku: item.sku || '',
      codigo_barra: item.codigo_barra || '',
      nombre: item.nombre,
      descripcion: item.descripcion || '',
      precio: item.precio,
      stock: item.stock,
      stock_minimo: item.stock_minimo,
      proveedor_id: item.proveedor_id || '',
      imagen: item.imagen || '',
      fecha_vencimiento: item.fecha_vencimiento || '',
      es_perecible: !!item.es_perecible,
      precio_compra: item.precio_compra || 0,
      estado_almacen: item.estado_almacen || 'Disponible',
      categoria_id: item.categoria_id || '',
      familia_id: item.familia_id || '',
      marca_id: item.marca_id || ''
    })
    setShowProductoForm(true)
  }

  const handleSaveProducto = async (e) => {
    e.preventDefault()
    const payload = {
      ...productoForm,
      precio: parseFloat(productoForm.precio),
      stock: parseInt(productoForm.stock),
      stock_minimo: parseInt(productoForm.stock_minimo),
      proveedor_id: productoForm.proveedor_id ? parseInt(productoForm.proveedor_id) : null,
      categoria_id: productoForm.categoria_id ? parseInt(productoForm.categoria_id) : null,
      familia_id: productoForm.familia_id ? parseInt(productoForm.familia_id) : null,
      marca_id: productoForm.marca_id ? parseInt(productoForm.marca_id) : null,
      fecha_vencimiento: productoForm.fecha_vencimiento || null,
      es_perecible: !!productoForm.es_perecible,
      precio_compra: parseFloat(productoForm.precio_compra) || 0
    }
    try {
      if (editingProducto) {
        await api.put(`/api/productos/${editingProducto.id}`, payload)
      } else {
        await api.post('/api/productos', payload)
      }
      setShowProductoForm(false)
      resetProductoForm()
      fetchProductos()
      toast.success(editingProducto ? "Producto actualizado" : "Producto creado")
    } catch (err) {
      toast.error(editingProducto ? "Error al actualizar producto" : "Error al crear producto")
    }
  }

  const handleDeleteProducto = async (id) => {
    if (!window.confirm("¿Estás seguro de eliminar este insumo?")) return;
    try {
      await api.delete(`/api/productos/${id}`)
      fetchProductos()
      toast.success("Producto eliminado")
    } catch (err) {
      toast.error("Error al eliminar producto.")
    }
  }

  const movimientosFiltrados = movimientos.filter(m =>
    (!movFiltro.producto_id || m.producto_id === parseInt(movFiltro.producto_id)) &&
    (!movFiltro.tipo || m.tipo_movimiento === movFiltro.tipo)
  )

  const handleRegistrarMovimiento = async () => {
    const producto = productos.find(p => p.id === parseInt(movForm.producto_id))
    if (!producto) { toast.error("Selecciona un producto"); return; }
    const cantidad = parseInt(movForm.cantidad) || 0
    if (cantidad <= 0) { toast.error("Indica cuántas unidades quieres mover (mayor a 0)"); return; }
    if (movForm.almacen_origen === movForm.almacen_destino) { toast.error("El almacén de origen y destino deben ser distintos"); return; }
    const origen = movForm.almacen_origen
    const destino = movForm.almacen_destino
    try {
      await api.post('/api/almacenes/mover', {
        producto_id: producto.id,
        cantidad,
        almacen_origen: origen,
        almacen_destino: destino,
        observacion: movForm.observacion.trim() || undefined,
      })
      setMovForm({ producto_id: '', cantidad: 1, almacen_origen: 'Disponible', almacen_destino: 'Dañado', observacion: '' })
      await Promise.all([fetchProductos(), fetchMovimientos()])
      toast.success(`Se movieron ${cantidad} unidad(es) de "${origen}" a "${destino}"`)
    } catch (err) {
      toast.error(err.response?.data?.detail || "Error al registrar el movimiento")
    }
  }

  const playBeep = () => {
    try {
      const AudioCtx = window.AudioContext || window.webkitAudioContext
      const ctx = new AudioCtx()
      const osc = ctx.createOscillator()
      const gain = ctx.createGain()
      osc.connect(gain)
      gain.connect(ctx.destination)
      osc.frequency.value = 1250
      osc.type = 'sine'
      gain.gain.setValueAtTime(0.35, ctx.currentTime)
      gain.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + 0.22)
      osc.start()
      osc.stop(ctx.currentTime + 0.22)
    } catch (_e) {
      /* sin audio */
    }
  }

  const abrirScanner = (target) => {
    scanTargetRef.current = target
    setScanError('')
    setShowScanner(true)
  }

  useEffect(() => {
    if (!showScanner) return
    const el = document.getElementById('barcode-reader')
    if (!el) return
    let scanner = null
    try {
      scanner = new Html5Qrcode('barcode-reader')
      scanner.start(
        { facingMode: 'environment' },
        { fps: 10, qrbox: { width: 260, height: 150 } },
        (decodedText) => {
          playBeep()
          setProductoForm(f => ({ ...f, [scanTargetRef.current]: String(decodedText).trim() }))
          if (scanner) { scanner.stop().then(() => setShowScanner(false)).catch(() => setShowScanner(false)) }
        },
        () => {}
      ).catch(() => setScanError(`No se pudo abrir la cámara. Verifica los permisos.`))
    } catch (err) {
      setScanError(`No se pudo iniciar el lector: ${err.message || err}`)
    }
    return () => {
      if (scanner) { scanner.stop().catch(() => {}) }
    }
  }, [showScanner])

  const refreshCatalogo = () => {
    Promise.all([fetchCategorias(), fetchFamilias(), fetchMarcas()]).catch(() => {})
  }

  const openCatalogoNew = (tipo) => {
    setCatalogoTipo(tipo)
    setCatalogoForm({ nombre: '' })
    setEditandoCatalogo(null)
    setShowCatalogoForm(true)
  }

  const openCatalogoEdit = (tipo, item) => {
    setCatalogoTipo(tipo)
    setCatalogoForm({ nombre: item.nombre })
    setEditandoCatalogo(item)
    setShowCatalogoForm(true)
  }

  const handleSaveCatalogo = async (e) => {
    e.preventDefault()
    const url = `/api/${catalogoTipo}`
    try {
      if (editandoCatalogo) {
        await api.put(`${url}/${editandoCatalogo.id}`, catalogoForm)
        toast.success("Elemento actualizado")
      } else {
        await api.post(url, catalogoForm)
        toast.success("Elemento registrado")
      }
      setShowCatalogoForm(false)
      refreshCatalogo()
    } catch (err) {
      toast.error(err.response?.data?.detail || "Error al guardar el elemento")
    }
  }

  const handleDeleteCatalogo = async (tipo, item) => {
    if (!window.confirm(`¿Eliminar "${item.nombre}"? Los productos asociados quedarán sin esta clasificación.`)) return
    try {
      await api.delete(`/api/${tipo}/${item.id}`)
      refreshCatalogo()
      toast.success("Elemento eliminado")
    } catch (err) {
      toast.error(err.response?.data?.detail || "Error al eliminar el elemento")
    }
  }

  const hoyStr = new Date().toISOString().slice(0, 10)
  const en30Str = new Date(Date.now() + 30 * 86400000).toISOString().slice(0, 10)
  const productosVencidos = productos.filter(p => p.fecha_vencimiento && p.fecha_vencimiento < hoyStr)
  const unidadesEnAlmacen = (p, est) => {
    const fila = (p.stock_por_almacen || []).find(s => s.almacen === est)
    return fila ? fila.cantidad : 0
  }
  const productoSelMov = productos.find(p => p.id === parseInt(movForm.producto_id))

  const productosPorVencer = productos.filter(p => p.fecha_vencimiento && p.fecha_vencimiento >= hoyStr && p.fecha_vencimiento <= en30Str)
  const productosNoDisponibles = productos.filter(p => unidadesEnAlmacen(p, 'Disponible') <= 0)

  const ingresosFiltrados = ingresoFiltroProveedor
    ? ingresos.filter(i => i.proveedor_id === parseInt(ingresoFiltroProveedor))
    : ingresos
  const totalIngresosFiltrado = ingresosFiltrados.reduce((acc, i) => acc + i.total, 0)

  const almacenesFiltrados = productos.filter(p =>
    almacenFiltro === 'Todos' || unidadesEnAlmacen(p, almacenFiltro) > 0
  )

  const productosSinCategoria = productos.filter(p => !p.categoria_id)
  const productosSinFamilia = productos.filter(p => !p.familia_id)
  const productosSinMarca = productos.filter(p => !p.marca_id)
  const productosSinClasificar = productos.filter(p => !p.categoria_id || !p.familia_id || !p.marca_id)

  const monedaLabel = (m) => (m === 'USD' ? 'Dólares (USD)' : 'Soles (SOL)')
  const proveedorMoneda = ingresoForm.proveedor_id
    ? (proveedores.find(p => p.id === parseInt(ingresoForm.proveedor_id))?.moneda || 'SOL')
    : 'SOL'

  const clasifSel = (id) => clasifDraft[id] || { categoria_id: '', familia_id: '', marca_id: '' }
  const setClasif = (id, key, value) => setClasifDraft(d => ({ ...d, [id]: { ...(d[id] || {}), [key]: value } }))

  const asegurarEntidad = async (tipo, nombre) => {
    const cache = { categorias: catCache, familias: famCache, marcas: marCache }[tipo]
    if (cache[nombre]) return cache[nombre]
    const listas = { categorias, familias, marcas }
    const found = listas[tipo].find(x => x.nombre.toLowerCase() === nombre.toLowerCase())
    if (found) { cache[nombre] = found.id; return found.id }
    try {
      const res = await api.post(`/api/${tipo}`, { nombre })
      cache[nombre] = res.data.id
      refreshCatalogo()
      return res.data.id
    } catch (err) {
      const res = await api.get(`/api/${tipo}`)
      const item = res.data.find(x => x.nombre.toLowerCase() === nombre.toLowerCase())
      if (item) { cache[nombre] = item.id; return item.id }
      throw err
    }
  }

  const payloadClasificacion = (p, extra) => ({
    sku: p.sku,
    nombre: p.nombre,
    precio: p.precio,
    stock: p.stock,
    stock_minimo: p.stock_minimo,
    proveedor_id: p.proveedor_id,
    fecha_vencimiento: p.fecha_vencimiento || null,
    es_perecible: !!p.es_perecible,
    precio_compra: p.precio_compra || 0,
    estado_almacen: p.estado_almacen || 'Disponible',
    codigo_barra: p.codigo_barra,
    imagen: p.imagen,
    descripcion: p.descripcion,
    ...extra
  })

  const handleClasificarProducto = async (item) => {
    const draft = clasifSel(item.id)
    try {
      await api.put(`/api/productos/${item.id}`, payloadClasificacion(item, {
        categoria_id: draft.categoria_id ? parseInt(draft.categoria_id) : item.categoria_id,
        familia_id: draft.familia_id ? parseInt(draft.familia_id) : item.familia_id,
        marca_id: draft.marca_id ? parseInt(draft.marca_id) : item.marca_id,
      }))
      setClasifDraft(d => { const next = { ...d }; delete next[item.id]; return next })
      await fetchProductos()
      toast.success(`${item.nombre} clasificado`)
    } catch (err) {
      toast.error(err.response?.data?.detail || "Error al clasificar el producto")
    }
  }

  const autoClasificar = async () => {
    if (!window.confirm('¿Clasificar automáticamente todos los productos según su nombre?')) return
    let ok = 0
    for (const p of productos) {
      const n = (p.nombre || '').toLowerCase()
      const cat = AUTO_CATEGORIAS.find(c => c.keywords.some(k => n.includes(k)))
      if (!cat) continue
      try {
        const categoria_id = await asegurarEntidad('categorias', cat.nombre)
        const familia_id = await asegurarEntidad('familias', cat.nombre)
        const marcaNombre = AUTO_MARCAS.find(m => n.includes(m.toLowerCase()))
        const marca_id = marcaNombre ? await asegurarEntidad('marcas', marcaNombre) : p.marca_id
        await api.put(`/api/productos/${p.id}`, payloadClasificacion(p, { categoria_id, familia_id, marca_id: marca_id || null }))
        ok++
      } catch (_e) { /* se omite y se sigue */ }
    }
    await Promise.all([fetchProductos(), fetchCategorias(), fetchFamilias(), fetchMarcas()])
    toast.success(ok > 0 ? `${ok} productos clasificados` : "No se encontraron productos por clasificar")
  }

  const almacenBadge = (estado) => {
    const map = {
      'Disponible': 'bg-emerald-50 text-emerald-700 border-emerald-200',
      'No disponible': 'bg-slate-100 text-slate-500 border-slate-200',
      'Dañado': 'bg-rose-50 text-rose-700 border-rose-200',
      'Vencido': 'bg-amber-50 text-amber-700 border-amber-200',
    }
    return map[estado] || 'bg-slate-50 text-slate-500 border-slate-200'
  }

  const vencimientoBadge = (fecha) => {
    if (!fecha) return <span className="text-slate-400 text-xs">Sin fecha</span>
    const badgeCls = fecha < hoyStr
      ? 'bg-rose-50 text-rose-700 border-rose-200'
      : fecha <= en30Str
        ? 'bg-amber-50 text-amber-700 border-amber-200'
        : 'bg-emerald-50 text-emerald-700 border-emerald-200'
    const label = fecha < hoyStr ? `VENCIDO ${fecha}` : fecha <= en30Str ? `VENCE ${fecha}` : fecha
    return <span className={`inline-flex items-center px-2 py-0.5 rounded-full border text-[11px] font-bold ${badgeCls}`}>{label}</span>
  }

  const tipoMovimientoBadge = (tipo) => {
    const map = {
      'INGRESO': 'bg-emerald-50 text-emerald-700 border-emerald-200',
      'VENTA': 'bg-indigo-50 text-indigo-700 border-indigo-200',
      'AJUSTE': 'bg-amber-50 text-amber-700 border-amber-200',
      'ANULACION_INGRESO': 'bg-rose-50 text-rose-700 border-rose-200',
      'CAMBIO_ALMACEN': 'bg-sky-50 text-sky-700 border-sky-200',
    }
    const nombres = {
      'INGRESO': 'Ingreso',
      'VENTA': 'Venta',
      'AJUSTE': 'Ajuste',
      'ANULACION_INGRESO': 'Anulación de ingreso',
      'CAMBIO_ALMACEN': 'Cambio de almacén',
    }
    return <span className={`inline-flex items-center px-2 py-0.5 rounded-full border text-[11px] font-bold ${map[tipo] || 'bg-slate-50 text-slate-500 border-slate-200'}`}>{nombres[tipo] || tipo}</span>
  }

  const handleImagenFile = (e) => {
    const file = e.target.files?.[0]
    if (!file) return
    if (!file.type.startsWith('image/')) { toast.error("El archivo debe ser una imagen"); return; }
    if (file.size > 1.5 * 1024 * 1024) { toast.error("La imagen no debe superar 1.5 MB"); return; }
    const reader = new FileReader()
    reader.onload = () => setProductoForm(f => ({ ...f, imagen: reader.result }))
    reader.onerror = () => toast.error("No se pudo leer la imagen")
    reader.readAsDataURL(file)
  }

  const resetProveedorForm = () => {
    setProveedorForm({ ruc: '', razon_social: '', contacto: '', telefono: '', direccion: '', email: '', moneda: 'SOL' })
    setEditingProveedor(null)
  }

  const openNewProveedor = () => {
    resetProveedorForm()
    setShowProveedorForm(true)
  }

  const openEditProveedor = (item) => {
    setEditingProveedor(item)
    setProveedorForm({
      ruc: item.ruc,
      razon_social: item.razon_social || item.nombre || '',
      contacto: item.contacto || '',
      telefono: item.telefono || '',
      direccion: item.direccion || '',
      email: item.email || '',
      moneda: item.moneda || 'SOL'
    })
    setShowProveedorForm(true)
  }

  const handleSaveProveedor = async (e) => {
    e.preventDefault()
    const ruc = (proveedorForm.ruc || '').trim()
    if (!/^\d{11}$/.test(ruc)) { toast.error("El RUC debe tener 11 dígitos"); return; }
    try {
      if (editingProveedor) {
        await api.put(`/api/proveedores/${editingProveedor.id}`, proveedorForm)
      } else {
        await api.post('/api/proveedores', proveedorForm)
      }
      setShowProveedorForm(false)
      resetProveedorForm()
      fetchProveedores()
      toast.success(editingProveedor ? "Proveedor actualizado" : "Proveedor creado")
    } catch (err) {
      toast.error(editingProveedor ? "Error al actualizar proveedor" : "Error al crear proveedor")
    }
  }

  const handleDeleteProveedor = async (id) => {
    if (!window.confirm("¿Estás seguro de eliminar este proveedor?")) return;
    try {
      await api.delete(`/api/proveedores/${id}`)
      fetchProveedores()
      toast.success("Proveedor eliminado")
    } catch (err) {
      toast.error("Error al eliminar proveedor.")
    }
  }

  const handleDeleteIngreso = async (item) => {
    if (!window.confirm(`¿Eliminar el ingreso de ${item.proveedor_nombre} por $${item.total.toFixed(2)}? El stock de los productos se revertirá.`)) return;
    try {
      await api.delete(`/api/ingresos/${item.id}`)
      setIngresoDetail(null)
      await Promise.all([fetchIngresos(), fetchProductos()])
      toast.success("Ingreso eliminado y stock revertido")
    } catch (err) {
      toast.error(err.response?.data?.detail || "Error al eliminar ingreso.")
    }
  }

  const ingresoTotal = ingresoItems.reduce((acc, it) => acc + ((parseFloat(it.precio_unitario) || 0) * (parseInt(it.cantidad) || 0)), 0)

  const updateIngresoItem = (index, key, value) => {
    setIngresoItems(items => items.map((it, i) => {
      if (i !== index) return it
      const next = { ...it, [key]: value }
      if (key === 'producto_id') {
        const prod = productos.find(p => p.id === parseInt(value))
        if (prod) {
          next.precio_unitario = prod.precio_compra || 0
          next.precio_venta = prod.precio || 0
        }
      }
      return next
    }))
  }

  const calcularPrecioVenta = (index) => {
    setIngresoItems(items => items.map((it, i) => {
      if (i !== index) return it
      const costo = parseFloat(it.precio_unitario) || 0
      const pct = parseFloat(it.porcentaje) || 0
      if (costo <= 0) return it
      return { ...it, precio_venta: Math.round(costo * (1 + pct / 100) * 1.18 * 100) / 100 }
    }))
  }

  const addIngresoItem = () => setIngresoItems(items => [...items, { producto_id: '', cantidad: 1, precio_unitario: 0, porcentaje: 25, precio_venta: 0, fecha_vencimiento: '' }])
  const removeIngresoItem = (index) => setIngresoItems(items => items.filter((_, i) => i !== index))

  const detallesIngreso = () => ingresoItems
    .filter(it => it.producto_id && parseInt(it.cantidad) > 0)
    .map(it => ({
      producto_id: parseInt(it.producto_id),
      cantidad: parseInt(it.cantidad),
      precio_unitario: parseFloat(it.precio_unitario) || 0,
      precio_venta: parseFloat(it.precio_venta) || null,
      fecha_vencimiento: it.fecha_vencimiento || null
    }))

  const handleIngresoResumen = (e) => {
    e.preventDefault()
    if (!ingresoForm.proveedor_id) { toast.error("Selecciona un proveedor"); return; }
    if (!ingresoForm.serie.trim() || !ingresoForm.numero.trim()) { toast.error("Completa la serie y el número del comprobante"); return; }
    const detalles = detallesIngreso()
    if (detalles.length === 0) { toast.error("Agrega al menos un producto con cantidad"); return; }
    for (const det of detalles) {
      const prod = productos.find(p => p.id === det.producto_id)
      if (prod && prod.es_perecible && !det.fecha_vencimiento) {
        toast.error(`${prod.nombre} es perecible: indica su fecha de vencimiento`)
        return
      }
    }
    setShowIngresoResumen(true)
  }

  const handleConfirmarIngreso = async () => {
    try {
      await api.post('/api/ingresos', {
        proveedor_id: parseInt(ingresoForm.proveedor_id),
        tipo_pago: ingresoForm.tipo_pago,
        tipo_comprobante: ingresoForm.tipo_comprobante,
        serie: ingresoForm.serie.trim(),
        numero: ingresoForm.numero.trim(),
        detalles: detallesIngreso()
      })
      setShowIngresoResumen(false)
      setShowIngresoForm(false)
      setIngresoForm({ proveedor_id: '', tipo_pago: 'Contado', tipo_comprobante: 'Factura', serie: '', numero: '' })
      setIngresoItems([{ producto_id: '', cantidad: 1, precio_unitario: 0, porcentaje: 25, precio_venta: 0, fecha_vencimiento: '' }])
      await Promise.all([fetchIngresos(), fetchProductos()])
      toast.success("Ingreso de mercadería registrado")
    } catch (err) {
      toast.error(err.response?.data?.detail || "Error al registrar ingreso")
    }
  }

  const irATab = (tabKey) => {
    setShowIngresoForm(false)
    setTab(tabKey)
  }

  const stockBadge = (item) => {
    const isLowStock = item.stock <= item.stock_minimo
    return (
      <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-bold border ${isLowStock ? 'bg-rose-50 text-rose-600 border-rose-200' : 'bg-emerald-50 text-emerald-600 border-emerald-200'}`}>
        <span className={`w-1.5 h-1.5 rounded-full mr-1.5 ${isLowStock ? 'bg-rose-500' : 'bg-emerald-500'}`}></span>
        {item.stock} {isLowStock && '(¡Bajo!)'}
      </span>
    )
  }

  const productoColumns = [
    { header: 'Foto', accessor: 'imagen', render: (item) => (
        item.imagen
          ? <img src={item.imagen} alt={item.nombre} className="w-10 h-10 rounded-lg object-contain border border-slate-100 bg-white" />
          : <div className="w-10 h-10 rounded-lg bg-slate-50 border border-slate-100 flex items-center justify-center text-slate-300"><Package className="w-5 h-5" /></div>
      )
    },
    { header: 'SKU', accessor: 'sku', render: (item) => <span className="font-mono text-indigo-600/70">{item.sku}</span> },
    { header: 'Cód. Barras / EAN', accessor: 'codigo_barra', render: (item) => <span className="font-mono text-slate-500">{item.codigo_barra || '—'}</span> },
    { header: 'Producto', accessor: 'nombre', render: (item) => (
        <div>
          <p className="font-bold text-slate-900">{item.nombre}</p>
          {item.descripcion && <p className="text-xs text-slate-500">{item.descripcion}</p>}
        </div>
      )
    },
    { header: 'Categoría', accessor: 'categoria_nombre', render: (item) => <span className="text-slate-400">{item.categoria_nombre || '—'}</span> },
    { header: 'Familia', accessor: 'familia_nombre', render: (item) => <span className="text-slate-400">{item.familia_nombre || '—'}</span> },
    { header: 'Marca', accessor: 'marca_nombre', render: (item) => <span className="text-slate-400">{item.marca_nombre || '—'}</span> },
    { header: 'Proveedor', accessor: 'proveedor_nombre', render: (item) => <span className="text-slate-400">{item.proveedor_nombre || '—'}</span> },
    { header: 'Precio', accessor: 'precio', render: (item) => <span className="font-medium text-slate-600">${item.precio.toFixed(2)}</span> },
    { header: 'Costo', accessor: 'precio_compra', render: (item) => <span className="text-slate-400">${(item.precio_compra || 0).toFixed(2)}</span> },
    { header: 'Stock Mín.', accessor: 'stock_minimo', render: (item) => <span className="text-slate-500">{item.stock_minimo}</span> },
    { header: 'Stock Actual', accessor: 'stock', render: stockBadge },
    {
      header: 'Almacén', accessor: 'estado_almacen', render: (item) => {
        const detalle = (item.stock_por_almacen && item.stock_por_almacen.length)
          ? item.stock_por_almacen
          : [{ almacen: item.estado_almacen || 'Disponible', cantidad: item.stock || 0 }]
        return (
          <div className="flex flex-wrap gap-1 max-w-[220px]">
            {detalle.map(d => (
              <span key={d.almacen} className={`inline-flex items-center gap-1 px-2 py-0.5 rounded-full border text-[11px] font-bold ${almacenBadge(d.almacen)}`}>
                {d.almacen}
                <span className="opacity-70">×{d.cantidad}</span>
              </span>
            ))}
          </div>
        )
      }
    },
    { header: 'Vencimiento', accessor: 'fecha_vencimiento', render: (item) => vencimientoBadge(item.fecha_vencimiento) },
    { header: 'Acciones', accessor: 'acciones', render: (item) => (
        <RoleGate allowedRoles={['Administrador']}>
          <div className="flex items-center space-x-2">
            <button onClick={() => openEditProducto(item)} className="text-indigo-600 hover:text-indigo-500 bg-indigo-50 hover:bg-indigo-100 p-2 rounded-xl transition-colors" title="Editar Producto">
              <Pencil className="w-4 h-4" />
            </button>
            <button onClick={() => handleDeleteProducto(item.id)} className="text-rose-600 hover:text-rose-600 bg-rose-50 hover:bg-rose-100 p-2 rounded-xl transition-colors" title="Eliminar Producto">
              <Trash2 className="w-4 h-4" />
            </button>
          </div>
        </RoleGate>
      )
    }
  ]

  const proveedorColumns = [
    { header: 'RUC', accessor: 'ruc', render: (item) => <span className="font-mono text-indigo-600/70">{item.ruc}</span> },
    { header: 'Razón Social', accessor: 'razon_social', render: (item) => <span className="font-bold text-slate-900">{item.razon_social || item.nombre}</span> },
    { header: 'Contacto', accessor: 'contacto', render: (item) => <span className="text-slate-500">{item.contacto || '—'}</span> },
    { header: 'Teléfono', accessor: 'telefono', render: (item) => <span className="text-slate-400">{item.telefono || '—'}</span> },
    { header: 'Email', accessor: 'email', render: (item) => <span className="text-slate-400">{item.email || '—'}</span> },
    { header: 'Dirección', accessor: 'direccion', render: (item) => <span className="text-slate-400">{item.direccion || '—'}</span> },
    { header: 'Moneda', accessor: 'moneda', render: (item) => <span className="inline-flex items-center px-2 py-0.5 rounded-full border border-slate-200 bg-slate-50 text-[11px] font-bold text-slate-600">{item.moneda === 'USD' ? '$' : 'S/'} {monedaLabel(item.moneda)}</span> },
    { header: 'Acciones', accessor: 'acciones', render: (item) => (
        <RoleGate allowedRoles={['Administrador']}>
          <div className="flex items-center space-x-2">
            <button onClick={() => openEditProveedor(item)} className="text-indigo-600 hover:text-indigo-500 bg-indigo-50 hover:bg-indigo-100 p-2 rounded-xl transition-colors" title="Editar Proveedor">
              <Pencil className="w-4 h-4" />
            </button>
            <button onClick={() => handleDeleteProveedor(item.id)} className="text-rose-600 hover:text-rose-600 bg-rose-50 hover:bg-rose-100 p-2 rounded-xl transition-colors" title="Eliminar Proveedor">
              <Trash2 className="w-4 h-4" />
            </button>
          </div>
        </RoleGate>
      )
    }
  ]

  const ingresoColumns = [
    { header: 'Fecha', accessor: 'fecha', render: (item) => <span className="text-slate-600">{new Date(item.fecha).toLocaleString('es-ES')}</span> },
    { header: 'Proveedor', accessor: 'proveedor_nombre', render: (item) => <span className="font-bold text-indigo-600">{item.proveedor_nombre}</span> },
    { header: 'Tipo de Pago', accessor: 'tipo_pago', render: (item) => <span className="text-slate-400">{item.tipo_pago}</span> },
    { header: 'Comprobante', accessor: 'numero_factura', render: (item) => (
        <div>
          <p className="text-[11px] font-bold text-slate-500 uppercase tracking-wider">{item.tipo_comprobante || 'Factura'}</p>
          <p className="font-mono text-indigo-600/70">{item.numero_factura || '—'}</p>
        </div>
      )
    },
    { header: 'Items', accessor: 'detalles', render: (item) => <span className="text-slate-400">{item.detalles?.length ?? 0}</span> },
    { header: 'Total', accessor: 'total', render: (item) => <span className="font-bold text-slate-900">${item.total.toFixed(2)}</span> },
    { header: 'Acciones', accessor: 'acciones', render: (item) => (
        <div className="flex items-center space-x-2">
          <button onClick={() => setIngresoDetail(item)} className="text-indigo-600 hover:text-indigo-500 bg-indigo-50 hover:bg-indigo-100 p-2 rounded-xl transition-colors" title="Ver Detalle">
            <Eye className="w-4 h-4" />
          </button>
          <RoleGate allowedRoles={['Administrador']}>
            <button onClick={() => handleDeleteIngreso(item)} className="text-rose-600 hover:text-rose-600 bg-rose-50 hover:bg-rose-100 p-2 rounded-xl transition-colors" title="Eliminar Ingreso">
              <Trash2 className="w-4 h-4" />
            </button>
          </RoleGate>
        </div>
      )
    },
  ]

  const movimientoColumns = [
    { header: 'Fecha', accessor: 'fecha', render: (item) => <span className="text-slate-600">{new Date(item.fecha).toLocaleString('es-ES')}</span> },
    { header: 'Producto', accessor: 'producto_nombre', render: (item) => <span className="font-bold text-slate-900">{item.producto_nombre}</span> },
    { header: 'Tipo de Movimiento', accessor: 'tipo_movimiento', render: (item) => tipoMovimientoBadge(item.tipo_movimiento) },
    { header: 'Cantidad', accessor: 'cantidad', render: (item) => <span className={`font-bold ${item.cantidad > 0 ? 'text-emerald-600' : item.cantidad < 0 ? 'text-rose-600' : 'text-slate-400'}`}>{(item.cantidad > 0 ? '+' : '') + item.cantidad}</span> },
    { header: 'Estado', accessor: 'estado', render: (item) => <span className="text-slate-500">{item.estado_anterior ? `${item.estado_anterior} → ${item.estado_nuevo}` : '—'}</span> },
    { header: 'Observación', accessor: 'observacion', render: (item) => <span className="text-xs text-slate-500">{item.observacion || '—'}</span> },
    { header: 'Referencia', accessor: 'referencia', render: (item) => <span className="font-mono text-xs text-indigo-600/70">{item.referencia || '—'}</span> },
    { header: 'Usuario', accessor: 'usuario_nombre', render: (item) => <span className="text-xs text-slate-400">{item.usuario_nombre || '—'}</span> },
  ]

  const tabs = [
    { key: TAB_PRODUCTOS, label: 'Productos', icon: Package },
    { key: TAB_ALMACENES, label: 'Almacenes', icon: Warehouse },
    { key: TAB_CATALOGO, label: 'Catálogo', icon: Tags },
    { key: TAB_PROVEEDORES, label: 'Proveedores', icon: Truck },
    { key: TAB_INGRESOS, label: 'Ingresos de Mercadería', icon: FileInput },
    { key: TAB_MOVIMIENTOS, label: 'Movimientos de Inventario', icon: History },
  ].filter(t => !(isVet && (t.key !== TAB_PRODUCTOS)))

  return (
    <div className="space-y-6 animate-fade-in-up">
      <PageHeader
        title="Control de Inventario"
        description="Gestión de productos, proveedores e ingresos de mercadería"
        actions={
          <>
            <RoleGate allowedRoles={['Administrador']}>
              {tab === TAB_PRODUCTOS && (
                <Button onClick={openNewProducto} variant="primary">
                  <Plus className="w-4 h-4 mr-2" /> Nuevo Insumo
                </Button>
              )}
              {tab === TAB_PROVEEDORES && (
                <Button onClick={openNewProveedor} variant="primary">
                  <Plus className="w-4 h-4 mr-2" /> Nuevo Proveedor
                </Button>
              )}
            </RoleGate>
            <RoleGate allowedRoles={['Administrador', 'Recepcionista']}>
              {tab === TAB_INGRESOS && (
                <Button onClick={() => setShowIngresoForm(true)} variant="primary">
                  <Plus className="w-4 h-4 mr-2" /> Nuevo Ingreso
                </Button>
              )}
            </RoleGate>
          </>
        }
      />

      <div className="flex gap-2 border-b border-slate-100 pb-px">
        {tabs.map(t => {
          const Icon = t.icon
          const active = tab === t.key
          return (
            <button
              key={t.key}
              onClick={() => setTab(t.key)}
              className={`inline-flex items-center px-4 py-2.5 rounded-t-xl text-sm font-bold transition-colors border-b-2 ${active ? 'bg-slate-50 text-indigo-600 border-indigo-500' : 'text-slate-400 border-transparent hover:text-slate-900'}`}
            >
              <Icon className="w-4 h-4 mr-2" /> {t.label}
            </button>
          )
        })}
      </div>

      {loading ? (
        <TableSkeleton />
      ) : (
        <>
          {tab === TAB_PRODUCTOS && (
            <>
              {(productosVencidos.length > 0 || productosPorVencer.length > 0 || productosNoDisponibles.length > 0) && (
                <div className="mb-4 space-y-2">
                  {productosVencidos.length > 0 && (
                    <div className="flex items-start gap-3 p-4 rounded-xl border border-rose-200 bg-rose-50">
                      <AlertTriangle className="w-5 h-5 text-rose-600 flex-shrink-0 mt-0.5" />
                      <div className="text-sm text-rose-700">
                        <p className="font-bold">{productosVencidos.length} producto(s) vencido(s)</p>
                        <p className="text-rose-600/80">Muévelos a "Vencido" en la pestaña Movimientos de Inventario para retirarlos de la venta.</p>
                      </div>
                    </div>
                  )}
                  {productosPorVencer.length > 0 && (
                    <div className="flex items-start gap-3 p-4 rounded-xl border border-amber-200 bg-amber-50">
                      <AlertTriangle className="w-5 h-5 text-amber-600 flex-shrink-0 mt-0.5" />
                      <div className="text-sm text-amber-700">
                        <p className="font-bold">{productosPorVencer.length} producto(s) por vencer en los próximos 30 días</p>
                        <p className="text-amber-600/80">Revisa las fechas en la pestaña Almacenes.</p>
                      </div>
                    </div>
                  )}
                  {productosNoDisponibles.length > 0 && (
                    <div className="flex items-start gap-3 p-4 rounded-xl border border-slate-200 bg-slate-50">
                      <AlertTriangle className="w-5 h-5 text-slate-500 flex-shrink-0 mt-0.5" />
                      <div className="text-sm text-slate-600">
                        <p className="font-bold">{productosNoDisponibles.length} producto(s) en almacén no disponible</p>
                        <p className="text-slate-500">No se ofrecen a la venta en el punto de venta.</p>
                      </div>
                    </div>
                  )}
                </div>
              )}
              <DataTable columns={productoColumns} data={productos} emptyMessage="Inventario vacío. Registra tu primer insumo." />
            </>
          )}
          {tab === TAB_ALMACENES && (
            <div className="space-y-5">
              <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
                {ESTADOS_ALMACEN.map(est => {
                  const count = productos.filter(p => unidadesEnAlmacen(p, est) > 0).length
                  const unidades = productos.reduce((acc, p) => acc + unidadesEnAlmacen(p, est), 0)
                  const active = almacenFiltro === est
                  const colorMap = {
                    'Disponible': active ? 'border-emerald-500 bg-emerald-50' : 'border-emerald-100 bg-white hover:border-emerald-200',
                    'No disponible': active ? 'border-slate-500 bg-slate-50' : 'border-slate-100 bg-white hover:border-slate-200',
                    'Dañado': active ? 'border-rose-500 bg-rose-50' : 'border-rose-100 bg-white hover:border-rose-200',
                    'Vencido': active ? 'border-amber-500 bg-amber-50' : 'border-amber-100 bg-white hover:border-amber-200',
                  }
                  return (
                    <button
                      key={est}
                      onClick={() => setAlmacenFiltro(active ? 'Todos' : est)}
                      className={`p-4 rounded-2xl border-2 text-left transition-colors ${colorMap[est] || 'border-slate-100 bg-white'}`}
                    >
                      <p className="text-xs font-bold uppercase tracking-widest text-slate-400">{est}</p>
                      <p className="text-3xl font-black text-slate-800 mt-1">{unidades}</p>
                      <p className="text-xs text-slate-400 mt-1">unidad(es) · {count} producto(s)</p>
                    </button>
                  )
                })}
              </div>
              <div className="bg-white rounded-2xl border border-slate-100 shadow-sm p-4">
                <div className="flex flex-col md:flex-row md:items-center justify-between gap-3 mb-4">
                  <h3 className="text-sm font-bold text-slate-700 uppercase tracking-widest">
                    {almacenFiltro === 'Todos' ? 'Todos los almacenes' : `Almacén "${almacenFiltro}"`} · {almacenesFiltrados.length} producto(s)
                  </h3>
                  {almacenFiltro !== 'Todos' && (
                    <Button type="button" variant="secondary" size="sm" onClick={() => setAlmacenFiltro('Todos')}>
                      Ver todos
                    </Button>
                  )}
                </div>
                <DataTable columns={productoColumns} data={almacenesFiltrados} emptyMessage="No hay productos en este almacén." />
              </div>
            </div>
          )}
          {tab === TAB_CATALOGO && (
            <RoleGate allowedRoles={['Administrador', 'Recepcionista']}>
              <div className="bg-white rounded-2xl border border-slate-100 shadow-sm p-5 mb-6">
                <div className="flex flex-col md:flex-row md:items-start md:justify-between gap-4">
                  <div>
                    <h3 className="font-bold text-slate-800 text-lg flex items-center gap-2">
                      <Tags className="w-5 h-5 text-indigo-600" /> Clasificación de Productos
                    </h3>
                    <p className="text-sm text-slate-500 mt-1">
                      Asigna a cada producto su categoría, familia y marca. El market ya se muestra sectorizado por nombre;
                      esta clasificación deja el inventario organizado.
                    </p>
                    <div className="flex flex-wrap gap-2 mt-3">
                      <span className="inline-flex items-center px-3 py-1.5 rounded-full text-xs font-bold bg-indigo-50 text-indigo-600 border border-indigo-100">{productosSinCategoria.length} sin categoría</span>
                      <span className="inline-flex items-center px-3 py-1.5 rounded-full text-xs font-bold bg-emerald-50 text-emerald-600 border border-emerald-100">{productosSinFamilia.length} sin familia</span>
                      <span className="inline-flex items-center px-3 py-1.5 rounded-full text-xs font-bold bg-amber-50 text-amber-600 border border-amber-100">{productosSinMarca.length} sin marca</span>
                    </div>
                  </div>
                  <RoleGate allowedRoles={['Administrador']}>
                    <Button type="button" variant="secondary" onClick={autoClasificar}>
                      <Sparkles className="w-4 h-4 mr-2" /> Auto-clasificar por nombre
                    </Button>
                  </RoleGate>
                </div>

                {productosSinClasificar.length > 0 && (
                  <div className="mt-5 border-t border-slate-100 pt-5">
                    <p className="text-xs font-bold text-slate-400 uppercase tracking-widest mb-3">
                      Pendientes por clasificar ({productosSinClasificar.length})
                    </p>
                    <div className="space-y-2 max-h-[420px] overflow-y-auto pr-1">
                      {productosSinClasificar.map(p => {
                        const sel = clasifSel(p.id)
                        const done = sel.categoria_id || sel.familia_id || sel.marca_id
                        return (
                          <div key={p.id} className="flex flex-col lg:flex-row lg:items-center gap-2 p-3 rounded-xl border border-slate-100 bg-slate-50/60">
                            <div className="lg:w-1/4">
                              <p className="text-sm font-bold text-slate-800 truncate" title={p.nombre}>{p.nombre}</p>
                              <p className="text-xs text-slate-400">SKU {p.sku} · Stock {p.stock}</p>
                            </div>
                            <select className="flex-1" value={sel.categoria_id} onChange={e => setClasif(p.id, 'categoria_id', e.target.value)}>
                              <option value="" className="bg-white">{p.categoria_nombre ? `Categoría: ${p.categoria_nombre}` : '-- Categoría --'}</option>
                              {categorias.map(c => <option key={c.id} value={c.id} className="bg-white">{c.nombre}</option>)}
                            </select>
                            <select className="flex-1" value={sel.familia_id} onChange={e => setClasif(p.id, 'familia_id', e.target.value)}>
                              <option value="" className="bg-white">{p.familia_nombre ? `Familia: ${p.familia_nombre}` : '-- Familia --'}</option>
                              {familias.map(f => <option key={f.id} value={f.id} className="bg-white">{f.nombre}</option>)}
                            </select>
                            <select className="flex-1" value={sel.marca_id} onChange={e => setClasif(p.id, 'marca_id', e.target.value)}>
                              <option value="" className="bg-white">{p.marca_nombre ? `Marca: ${p.marca_nombre}` : '-- Marca --'}</option>
                              {marcas.map(m => <option key={m.id} value={m.id} className="bg-white">{m.nombre}</option>)}
                            </select>
                            <Button type="button" variant="primary" size="sm" disabled={!done} onClick={() => handleClasificarProducto(p)}>
                              <CheckCircle2 className="w-4 h-4 mr-1" /> Guardar
                            </Button>
                          </div>
                        )
                      })}
                    </div>
                  </div>
                )}
              </div>

              <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
                {SECCIONES_CATALOGO.map(sec => {
                  const lista = sec.tipo === 'categorias' ? categorias : sec.tipo === 'familias' ? familias : marcas
                  return (
                    <div key={sec.tipo} className="bg-white rounded-2xl border border-slate-100 shadow-sm p-5">
                      <div className="flex items-center justify-between mb-4">
                        <h3 className="font-bold text-slate-800 text-lg">{sec.titulo}</h3>
                        <RoleGate allowedRoles={['Administrador']}>
                          <Button onClick={() => openCatalogoNew(sec.tipo)} variant="primary" size="sm">
                            <Plus className="w-4 h-4 mr-1" /> Nuevo
                          </Button>
                        </RoleGate>
                      </div>
                      <ul className="divide-y divide-slate-50">
                        {lista.length === 0 && (
                          <li className="py-6 text-center text-sm text-slate-400">Sin registros</li>
                        )}
                        {lista.map(item => (
                          <li key={item.id} className="py-2.5 flex items-center justify-between gap-2">
                            <span className="text-sm font-medium text-slate-700 truncate">{item.nombre}</span>
                            <RoleGate allowedRoles={['Administrador']}>
                              <div className="flex items-center space-x-1 flex-shrink-0">
                                <button onClick={() => openCatalogoEdit(sec.tipo, item)} className="text-indigo-600 hover:text-indigo-500 bg-indigo-50 hover:bg-indigo-100 p-1.5 rounded-lg transition-colors" title="Editar">
                                  <PencilLine className="w-3.5 h-3.5" />
                                </button>
                                <button onClick={() => handleDeleteCatalogo(sec.tipo, item)} className="text-rose-600 hover:text-rose-600 bg-rose-50 hover:bg-rose-100 p-1.5 rounded-lg transition-colors" title="Eliminar">
                                  <Trash2 className="w-3.5 h-3.5" />
                                </button>
                              </div>
                            </RoleGate>
                          </li>
                        ))}
                      </ul>
                    </div>
                  )
                })}
              </div>
            </RoleGate>
          )}
          {tab === TAB_PROVEEDORES && (
            <DataTable columns={proveedorColumns} data={proveedores} emptyMessage="No hay proveedores registrados." />
          )}
          {tab === TAB_INGRESOS && (
            <>
              <div className="flex flex-col md:flex-row md:items-end gap-3 bg-white rounded-2xl border border-slate-100 shadow-sm p-4">
                <div className="w-full md:w-72">
                  <label className="block text-xs font-bold text-slate-400 uppercase tracking-widest mb-2">Filtrar por Proveedor</label>
                  <select className={selectCls} value={ingresoFiltroProveedor} onChange={e => setIngresoFiltroProveedor(e.target.value)}>
                    <option value="" className="bg-white">Todos los proveedores</option>
                    {proveedores.map(p => <option key={p.id} value={p.id} className="bg-white">{p.razon_social || p.nombre}</option>)}
                  </select>
                </div>
                <div className="text-sm text-slate-500 pb-3">
                  <span className="font-bold text-slate-700">{ingresosFiltrados.length}</span> compra(s)
                  {ingresoFiltroProveedor && <span> · Total invertido: <span className="font-bold text-emerald-600">${totalIngresosFiltrado.toFixed(2)}</span></span>}
                </div>
              </div>
              <DataTable
                columns={ingresoColumns}
                data={ingresosFiltrados}
                emptyMessage="No hay ingresos de mercadería registrados."
                emptyAction={
                  <RoleGate allowedRoles={['Administrador', 'Recepcionista']}>
                    <Button onClick={() => setShowIngresoForm(true)} variant="primary" size="sm">
                      <Plus className="w-4 h-4 mr-1" /> Registrar el primer ingreso
                    </Button>
                  </RoleGate>
                }
              />
            </>
          )}
          {tab === TAB_MOVIMIENTOS && (
            <div className="space-y-5">
              <div className="bg-white rounded-2xl border border-slate-200 p-5">
                <h3 className="text-sm font-bold text-slate-700 uppercase tracking-widest mb-4">Registrar Movimiento</h3>
                <div className="grid grid-cols-1 md:grid-cols-5 gap-3 items-end">
                  <div>
                    <label className="block text-xs font-bold text-slate-400 uppercase tracking-widest mb-2">Producto</label>
                    <select className={selectCls} value={movForm.producto_id} onChange={e => {
                      const id = e.target.value
                      const p = productos.find(x => x.id === parseInt(id))
                      const conStock = (p?.stock_por_almacen || []).find(s => s.cantidad > 0)
                      setMovForm({ ...movForm, producto_id: id, almacen_origen: conStock ? conStock.almacen : 'Disponible' })
                    }}>
                      <option value="" className="bg-white">-- Seleccionar producto --</option>
                      {productos.map(p => <option key={p.id} value={p.id} className="bg-white">{p.nombre} (stock: {p.stock})</option>)}
                    </select>
                  </div>
                  <div>
                    <label className="block text-xs font-bold text-slate-400 uppercase tracking-widest mb-2">Cantidad a Mover</label>
                    <input type="number" min="1" className={selectCls} value={movForm.cantidad} onChange={e => setMovForm({ ...movForm, cantidad: e.target.value })} />
                  </div>
                  <div>
                    <label className="block text-xs font-bold text-slate-400 uppercase tracking-widest mb-2">Almacén Origen</label>
                    <select className={selectCls} value={movForm.almacen_origen} onChange={e => setMovForm({ ...movForm, almacen_origen: e.target.value })}>
                      {ESTADOS_ALMACEN.map(e => <option key={e} value={e} className="bg-white">{e}</option>)}
                    </select>
                    {productoSelMov && (
                      <p className="text-[11px] text-slate-400 mt-1">Disponible en origen: <span className="font-bold text-slate-600">{unidadesEnAlmacen(productoSelMov, movForm.almacen_origen)}</span></p>
                    )}
                  </div>
                  <div>
                    <label className="block text-xs font-bold text-slate-400 uppercase tracking-widest mb-2">Almacén Destino</label>
                    <select className={selectCls} value={movForm.almacen_destino} onChange={e => setMovForm({ ...movForm, almacen_destino: e.target.value })}>
                      {ESTADOS_ALMACEN.map(e => <option key={e} value={e} className="bg-white">{e}</option>)}
                    </select>
                  </div>
                  <div className="flex gap-2">
                    <Button type="button" variant="primary" onClick={handleRegistrarMovimiento}>
                      <PlusCircle className="w-4 h-4 mr-2" /> Registrar
                    </Button>
                    <Button type="button" variant="ghost" onClick={() => setMovForm({ producto_id: '', cantidad: 1, almacen_origen: 'Disponible', almacen_destino: 'Dañado', observacion: '' })}>Limpiar</Button>
                  </div>
                </div>
                <div className="mt-3">
                  <label className="block text-xs font-bold text-slate-400 uppercase tracking-widest mb-2">Observación (opcional)</label>
                  <input type="text" className={selectCls} value={movForm.observacion} onChange={e => setMovForm({ ...movForm, observacion: e.target.value })} placeholder="Motivo del movimiento, ej: caja dañada, producto vencido" />
                </div>
              </div>

              <div className="bg-white rounded-2xl border border-slate-200 p-5">
                <h3 className="text-sm font-bold text-slate-700 uppercase tracking-widest mb-4">Historial de Movimientos</h3>
                <div className="grid grid-cols-1 md:grid-cols-3 gap-3 items-end mb-4">
                  <div>
                    <label className="block text-xs font-bold text-slate-400 uppercase tracking-widest mb-2">Producto</label>
                    <select className={selectCls} value={movFiltro.producto_id} onChange={e => setMovFiltro({ ...movFiltro, producto_id: e.target.value })}>
                      <option value="" className="bg-white">Todos los productos</option>
                      {productos.map(p => <option key={p.id} value={p.id} className="bg-white">{p.nombre}</option>)}
                    </select>
                  </div>
                  <div>
                    <label className="block text-xs font-bold text-slate-400 uppercase tracking-widest mb-2">Tipo de Movimiento</label>
                    <select className={selectCls} value={movFiltro.tipo} onChange={e => setMovFiltro({ ...movFiltro, tipo: e.target.value })}>
                      <option value="" className="bg-white">Todos</option>
                      {TIPO_MOVIMIENTOS.map(t => <option key={t} value={t} className="bg-white">{t}</option>)}
                    </select>
                  </div>
                  <Button type="button" variant="secondary" onClick={() => setMovFiltro({ producto_id: '', tipo: '' })}>Limpiar Filtros</Button>
                </div>
                <DataTable columns={movimientoColumns} data={movimientosFiltrados} emptyMessage="No hay movimientos de inventario registrados." />
              </div>
            </div>
          )}
        </>
      )}

      <Modal isOpen={showProductoForm} onClose={() => { setShowProductoForm(false); resetProductoForm(); }} title={editingProducto ? "Editar Insumo" : "Registrar Nuevo Insumo"} maxWidth="max-w-2xl">
        <form onSubmit={handleSaveProducto} className="space-y-4">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-bold text-slate-400 uppercase tracking-widest mb-2">SKU</label>
              <div className="relative">
                <input
                  type="text"
                  value={productoForm.sku}
                  readOnly
                  onFocus={e => e.target.select()}
                  className={`${selectCls} pr-10 bg-slate-50 text-slate-500 cursor-not-allowed select-none`}
                />
                <Lock className="w-4 h-4 absolute right-4 top-1/2 -translate-y-1/2 text-slate-300" />
              </div>
              <p className="mt-2 text-xs font-medium text-indigo-600 flex items-center">
                <Lock className="w-3.5 h-3.5 mr-1.5 flex-shrink-0" />
                Generado automáticamente en secuencia creciente
              </p>
            </div>
            <div>
              <label className="block text-xs font-bold text-slate-400 uppercase tracking-widest mb-2">Código de Barras / EAN</label>
              <div className="flex items-center gap-2">
                <input
                  type="text"
                  placeholder="Ej. 7501234567890"
                  value={productoForm.codigo_barra}
                  onChange={e => setProductoForm({...productoForm, codigo_barra: e.target.value})}
                  onKeyDown={e => { if (e.key === 'Enter') { e.preventDefault(); playBeep(); } }}
                  className={selectCls}
                />
                <button
                  type="button"
                  onClick={() => abrirScanner('codigo_barra')}
                  className="flex-shrink-0 inline-flex items-center gap-1.5 px-4 py-3 rounded-xl bg-indigo-50 text-indigo-600 font-bold text-sm hover:bg-indigo-100 transition-colors"
                  title="Escanear código de barras con la cámara"
                >
                  <ScanLine className="w-4 h-4" /> Scanear
                </button>
              </div>
              <p className="mt-2 text-xs text-slate-400">En el celular usa "Scanear" para abrir la cámara. En la PC usa la pistola de barras o escribe manualmente.</p>
            </div>
            <Input label="Nombre del Producto" required type="text" value={productoForm.nombre} onChange={e => setProductoForm({...productoForm, nombre: e.target.value})} />
            <div className="md:col-span-2">
              <Input label="Descripción (Opcional)" type="text" value={productoForm.descripcion} onChange={e => setProductoForm({...productoForm, descripcion: e.target.value})} />
            </div>
            <div className="md:col-span-2">
              <label className="block text-xs font-bold text-slate-400 uppercase tracking-widest mb-2">Foto del Producto</label>
              <div className="flex items-center gap-4">
                {productoForm.imagen ? (
                  <div className="relative w-20 h-20 rounded-xl border border-slate-200 overflow-hidden bg-white">
                    <img src={productoForm.imagen} alt="Vista previa" className="w-full h-full object-contain" />
                    <button
                      type="button"
                      onClick={() => setProductoForm(f => ({ ...f, imagen: '' }))}
                      className="absolute -top-1 -right-1 w-5 h-5 bg-rose-500 text-white rounded-full text-xs leading-none flex items-center justify-center hover:bg-rose-600"
                      title="Quitar foto"
                    >✕</button>
                  </div>
                ) : (
                  <div className="w-20 h-20 rounded-xl border border-dashed border-slate-300 bg-slate-50 flex items-center justify-center text-slate-400">
                    <Package className="w-7 h-7" />
                  </div>
                )}
                <div className="flex-1">
                  <input
                    type="file"
                    accept="image/*"
                    onChange={handleImagenFile}
                    className="w-full text-sm text-slate-500 file:mr-3 file:px-3 file:py-1.5 file:rounded-xl file:border-0 file:bg-indigo-50 file:text-indigo-600 file:font-bold file:cursor-pointer hover:file:bg-indigo-100"
                  />
                  <p className="mt-2 text-xs text-slate-400">JPG o PNG, máximo 1.5 MB. Esta foto se muestra en la tienda.</p>
                </div>
              </div>
            </div>
            <div>
              <label className="block text-xs font-bold text-slate-400 uppercase tracking-widest mb-2">Proveedor</label>
              <select className={selectCls} value={productoForm.proveedor_id} onChange={e => setProductoForm({...productoForm, proveedor_id: e.target.value})}>
                <option value="" className="bg-white">-- Sin proveedor --</option>
                {proveedores.map(p => <option key={p.id} value={p.id} className="bg-white">{p.razon_social || p.nombre}</option>)}
              </select>
            </div>
            <div>
              <label className="block text-xs font-bold text-slate-400 uppercase tracking-widest mb-2">Categoría</label>
              <div className="flex items-center gap-2">
                <select className={selectCls} value={productoForm.categoria_id} onChange={e => setProductoForm({...productoForm, categoria_id: e.target.value})}>
                  <option value="" className="bg-white">-- Sin categoría --</option>
                  {categorias.map(c => <option key={c.id} value={c.id} className="bg-white">{c.nombre}</option>)}
                </select>
                <button
                  type="button"
                  onClick={() => openCatalogoNew('categorias')}
                  className="flex-shrink-0 w-11 h-11 rounded-xl bg-indigo-50 text-indigo-600 hover:bg-indigo-100 flex items-center justify-center transition-colors"
                  title="Nueva categoría"
                >
                  <Plus className="w-4 h-4" />
                </button>
              </div>
            </div>
            <div>
              <label className="block text-xs font-bold text-slate-400 uppercase tracking-widest mb-2">Familia</label>
              <div className="flex items-center gap-2">
                <select className={selectCls} value={productoForm.familia_id} onChange={e => setProductoForm({...productoForm, familia_id: e.target.value})}>
                  <option value="" className="bg-white">-- Sin familia --</option>
                  {familias.map(f => <option key={f.id} value={f.id} className="bg-white">{f.nombre}</option>)}
                </select>
                <button
                  type="button"
                  onClick={() => openCatalogoNew('familias')}
                  className="flex-shrink-0 w-11 h-11 rounded-xl bg-indigo-50 text-indigo-600 hover:bg-indigo-100 flex items-center justify-center transition-colors"
                  title="Nueva familia"
                >
                  <Plus className="w-4 h-4" />
                </button>
              </div>
            </div>
            <div>
              <label className="block text-xs font-bold text-slate-400 uppercase tracking-widest mb-2">Marca</label>
              <div className="flex items-center gap-2">
                <select className={selectCls} value={productoForm.marca_id} onChange={e => setProductoForm({...productoForm, marca_id: e.target.value})}>
                  <option value="" className="bg-white">-- Sin marca --</option>
                  {marcas.map(m => <option key={m.id} value={m.id} className="bg-white">{m.nombre}</option>)}
                </select>
                <button
                  type="button"
                  onClick={() => openCatalogoNew('marcas')}
                  className="flex-shrink-0 w-11 h-11 rounded-xl bg-indigo-50 text-indigo-600 hover:bg-indigo-100 flex items-center justify-center transition-colors"
                  title="Nueva marca"
                >
                  <Plus className="w-4 h-4" />
                </button>
              </div>
            </div>
            <Input label="Precio Unitario ($)" required type="number" step="0.01" value={productoForm.precio} onChange={e => setProductoForm({...productoForm, precio: e.target.value})} />
            <div>
              <label className="block text-xs font-bold text-slate-400 uppercase tracking-widest mb-2">Stock Actual</label>
              <input disabled value={productoForm.stock} className="w-full bg-white border border-slate-100 rounded-xl px-4 py-3 text-slate-400 cursor-not-allowed" />
              <p className="mt-2 text-xs font-medium text-indigo-600 flex items-center">
                <FileInput className="w-3.5 h-3.5 mr-1.5 flex-shrink-0" />
                El stock se registra en la pestaña Ingresos de Mercadería (proveedor y pago)
              </p>
            </div>
            <Input label="Stock Mínimo (Alerta)" required type="number" value={productoForm.stock_minimo} onChange={e => setProductoForm({...productoForm, stock_minimo: e.target.value})} />
            <div>
              <label className="block text-xs font-bold text-slate-400 uppercase tracking-widest mb-2">Costo de Compra</label>
              <input disabled value={productoForm.precio_compra ? `$${Number(productoForm.precio_compra).toFixed(2)}` : 'Se registra en Ingresos'} className="w-full bg-white border border-slate-100 rounded-xl px-4 py-3 text-slate-400 cursor-not-allowed" />
            </div>
            <div className="md:col-span-2">
              <label className="flex items-start gap-3 p-3 rounded-xl border border-slate-200 bg-slate-50 cursor-pointer">
                <input
                  type="checkbox"
                  checked={!!productoForm.es_perecible}
                  onChange={e => setProductoForm({...productoForm, es_perecible: e.target.checked})}
                  className="w-5 h-5 accent-indigo-600 mt-0.5"
                />
                <span>
                  <span className="block text-sm font-bold text-slate-700">Producto perecible</span>
                  <span className="block text-xs text-slate-400">Si está activado, cada ingreso de mercadería pedirá la fecha de vencimiento del lote.</span>
                </span>
              </label>
            </div>
            <div className="md:col-span-2">
              <Input label="Fecha de Vencimiento (perecible)" type="date" value={productoForm.fecha_vencimiento} onChange={e => setProductoForm({...productoForm, fecha_vencimiento: e.target.value})} />
            </div>
          </div>
          <div className="flex justify-end space-x-3 pt-4">
            <Button type="button" variant="ghost" onClick={() => { setShowProductoForm(false); resetProductoForm(); }}>Cancelar</Button>
            <Button type="submit" variant="primary">{editingProducto ? "Guardar Cambios" : "Guardar Producto"}</Button>
          </div>
        </form>
      </Modal>

      <Modal isOpen={showProveedorForm} onClose={() => { setShowProveedorForm(false); resetProveedorForm(); }} title={editingProveedor ? "Editar Proveedor" : "Registrar Nuevo Proveedor"} maxWidth="max-w-2xl">
        <form onSubmit={handleSaveProveedor} className="space-y-4">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <Input label="RUC" required type="text" maxLength={11} value={proveedorForm.ruc} onChange={e => setProveedorForm({...proveedorForm, ruc: e.target.value.replace(/\D/g, '')})} />
            <Input label="Razón Social" required type="text" value={proveedorForm.razon_social} onChange={e => setProveedorForm({...proveedorForm, razon_social: e.target.value})} />
            <Input label="Persona de Contacto" type="text" placeholder="Ej. Carlos Vargas" value={proveedorForm.contacto} onChange={e => setProveedorForm({...proveedorForm, contacto: e.target.value})} />
            <div>
              <label className="block text-xs font-bold text-slate-400 uppercase tracking-widest mb-2">Moneda de Compra</label>
              <select className={selectCls} value={proveedorForm.moneda} onChange={e => setProveedorForm({...proveedorForm, moneda: e.target.value})}>
                {MONEDAS.map(m => <option key={m} value={m} className="bg-white">{m === 'USD' ? 'Dólares ($)' : 'Soles (S/)'}</option>)}
              </select>
            </div>
            <Input label="Teléfono" type="text" value={proveedorForm.telefono} onChange={e => setProveedorForm({...proveedorForm, telefono: e.target.value})} />
            <Input label="Email" type="email" value={proveedorForm.email} onChange={e => setProveedorForm({...proveedorForm, email: e.target.value})} />
            <div className="md:col-span-2">
              <Input label="Dirección" type="text" value={proveedorForm.direccion} onChange={e => setProveedorForm({...proveedorForm, direccion: e.target.value})} />
            </div>
          </div>
          <div className="flex justify-end space-x-3 pt-4">
            <Button type="button" variant="ghost" onClick={() => { setShowProveedorForm(false); resetProveedorForm(); }}>Cancelar</Button>
            <Button type="submit" variant="primary">{editingProveedor ? "Guardar Cambios" : "Guardar Proveedor"}</Button>
          </div>
        </form>
      </Modal>

      <Modal isOpen={showIngresoForm} onClose={() => { setShowIngresoForm(false); setShowIngresoResumen(false); }} title="Registrar Ingreso de Mercadería" maxWidth="max-w-4xl">
        <form onSubmit={handleIngresoResumen} className="space-y-5">
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
            <div>
              <label className="block text-xs font-bold text-slate-400 uppercase tracking-widest mb-2">Proveedor</label>
              <select required className={selectCls} value={ingresoForm.proveedor_id} onChange={e => setIngresoForm({...ingresoForm, proveedor_id: e.target.value})}>
                <option value="" className="bg-white">-- Seleccionar proveedor --</option>
                {proveedores.map(p => <option key={p.id} value={p.id} className="bg-white">{p.razon_social || p.nombre} ({p.ruc})</option>)}
              </select>
              {proveedores.length === 0 && (
                <div className="mt-3 p-3 rounded-xl border border-amber-200 bg-amber-50 text-sm">
                  <p className="font-bold text-amber-700">No hay proveedores registrados.</p>
                  <p className="text-amber-600 text-xs mt-1">Regístralo primero en la pestaña Proveedores.</p>
                  <Button type="button" variant="secondary" size="sm" className="mt-2" onClick={() => irATab(TAB_PROVEEDORES)}>
                    <Truck className="w-4 h-4 mr-2" /> Ir a Proveedores
                  </Button>
                </div>
              )}
            </div>
            <div>
              <label className="block text-xs font-bold text-slate-400 uppercase tracking-widest mb-2">Tipo de Pago</label>
              <select className={selectCls} value={ingresoForm.tipo_pago} onChange={e => setIngresoForm({...ingresoForm, tipo_pago: e.target.value})}>
                <option className="bg-white">Contado</option>
                <option className="bg-white">Crédito</option>
                <option className="bg-white">Transferencia</option>
              </select>
            </div>
            <div>
              <label className="block text-xs font-bold text-slate-400 uppercase tracking-widest mb-2">Tipo de Comprobante</label>
              <select className={selectCls} value={ingresoForm.tipo_comprobante} onChange={e => setIngresoForm({...ingresoForm, tipo_comprobante: e.target.value})}>
                {TIPOS_COMPROBANTE.map(t => <option key={t} className="bg-white">{t}</option>)}
              </select>
            </div>
            <div>
              <label className="block text-xs font-bold text-slate-400 uppercase tracking-widest mb-2">Serie y Número</label>
              <div className="flex gap-2">
                <input type="text" placeholder="Serie" maxLength={4} className={selectCls} value={ingresoForm.serie} onChange={e => setIngresoForm({...ingresoForm, serie: e.target.value.toUpperCase()})} />
                <input type="text" placeholder="Número" maxLength={8} className={selectCls} value={ingresoForm.numero} onChange={e => setIngresoForm({...ingresoForm, numero: e.target.value.replace(/\D/g, '')})} />
              </div>
            </div>
          </div>
          <div className="flex flex-wrap items-center gap-x-4 gap-y-1 text-xs">
            <p className="text-slate-400">Comprobante del proveedor. Ej: <span className="font-mono text-indigo-600">{ingresoForm.tipo_comprobante} {ingresoForm.serie}-{ingresoForm.numero || '000000'}</span></p>
            {ingresoForm.proveedor_id && (
              <p className="font-medium text-indigo-600">Moneda de compra: {monedaLabel(proveedorMoneda)}</p>
            )}
          </div>

          <div className="space-y-3">
            <div className="flex items-center justify-between">
              <p className="text-sm font-bold text-slate-600 uppercase tracking-widest">Detalle de Productos</p>
              <Button type="button" variant="secondary" size="sm" onClick={addIngresoItem}>
                <PlusCircle className="w-4 h-4 mr-2" /> Agregar Producto
              </Button>
            </div>
            <div className="overflow-x-auto rounded-xl border border-slate-200">
              <table className="w-full text-left border-collapse min-w-[880px]">
                <thead>
                  <tr className="border-b border-slate-200 bg-slate-50">
                    <th className="px-4 py-3 text-xs font-bold text-slate-400 uppercase tracking-widest">Producto</th>
                    <th className="px-4 py-3 text-xs font-bold text-slate-400 uppercase tracking-widest w-24">Cant.</th>
                    <th className="px-4 py-3 text-xs font-bold text-slate-400 uppercase tracking-widest w-32">Costo Compra ($)</th>
                    <th className="px-4 py-3 text-xs font-bold text-slate-400 uppercase tracking-widest w-20">% Gan.</th>
                    <th className="px-4 py-3 text-xs font-bold text-slate-400 uppercase tracking-widest w-44">Precio Venta ($)</th>
                    <th className="px-4 py-3 text-xs font-bold text-slate-400 uppercase tracking-widest w-40">Vencimiento</th>
                    <th className="px-4 py-3 text-xs font-bold text-slate-400 uppercase tracking-widest w-32">Subtotal</th>
                    <th className="w-12"></th>
                  </tr>
                </thead>
                <tbody>
                  {ingresoItems.map((item, i) => {
                    const sub = (parseFloat(item.precio_unitario) || 0) * (parseInt(item.cantidad) || 0)
                    const prodSel = productos.find(p => p.id === parseInt(item.producto_id))
                    return (
                      <tr key={i} className="border-b border-slate-100 last:border-0">
                        <td className="px-4 py-2">
                          <select className={selectCls} value={item.producto_id} onChange={e => updateIngresoItem(i, 'producto_id', e.target.value)}>
                            <option value="" className="bg-white">-- Seleccionar producto --</option>
                            {productos.map(p => <option key={p.id} value={p.id} className="bg-white">{p.nombre} (stock: {p.stock}){p.es_perecible ? ' · PERECIBLE' : ''}</option>)}
                          </select>
                        </td>
                        <td className="px-4 py-2">
                          <input type="number" min="1" className={selectCls} value={item.cantidad} onChange={e => updateIngresoItem(i, 'cantidad', e.target.value)} />
                        </td>
                        <td className="px-4 py-2">
                          <input type="number" min="0" step="0.01" className={selectCls} value={item.precio_unitario} onChange={e => updateIngresoItem(i, 'precio_unitario', e.target.value)} />
                        </td>
                        <td className="px-4 py-2">
                          <input type="number" min="0" step="1" className={selectCls} value={item.porcentaje} onChange={e => updateIngresoItem(i, 'porcentaje', e.target.value)} />
                        </td>
                        <td className="px-4 py-2">
                          <div className="flex gap-1 items-center">
                            <input type="number" min="0" step="0.01" className={selectCls} value={item.precio_venta} onChange={e => updateIngresoItem(i, 'precio_venta', e.target.value)} />
                            <button type="button" onClick={() => calcularPrecioVenta(i)} className="shrink-0 px-2 py-1.5 text-[11px] font-bold rounded-lg bg-indigo-600 text-white hover:bg-indigo-700" title="Calcular precio de venta = costo + % ganancia + IGV">Calcular</button>
                          </div>
                        </td>
                        <td className="px-4 py-2">
                          {prodSel && prodSel.es_perecible ? (
                            <input type="date" className={selectCls} value={item.fecha_vencimiento} onChange={e => updateIngresoItem(i, 'fecha_vencimiento', e.target.value)} />
                          ) : (
                            <span className="text-xs text-slate-300">No aplica</span>
                          )}
                        </td>
                        <td className="px-4 py-2 text-sm font-bold text-slate-900">${sub.toFixed(2)}</td>
                        <td className="px-4 py-2">
                          <button type="button" onClick={() => removeIngresoItem(i)} className="text-rose-600 hover:text-rose-600 p-1" title="Quitar">
                            <MinusCircle className="w-4 h-4" />
                          </button>
                        </td>
                      </tr>
                    )
                  })}
                </tbody>
              </table>
            </div>
            {productos.length === 0 && (
              <div className="p-3 rounded-xl border border-amber-200 bg-amber-50 text-sm">
                <p className="font-bold text-amber-700">No hay productos registrados.</p>
                <p className="text-amber-600 text-xs mt-1">Regístralo primero en la pestaña Productos. El stock inicial se suma con este ingreso.</p>
                <Button type="button" variant="secondary" size="sm" className="mt-2" onClick={() => irATab(TAB_PRODUCTOS)}>
                  <Package className="w-4 h-4 mr-2" /> Ir a Productos
                </Button>
              </div>
            )}
          </div>

          <div className="flex items-center justify-between pt-2">
            <span className="text-sm font-bold text-slate-400 uppercase tracking-widest">Total del Ingreso</span>
            <span className="text-2xl font-black text-indigo-600">${ingresoTotal.toFixed(2)}</span>
          </div>

          <div className="flex justify-end space-x-3 pt-2">
            <Button type="button" variant="ghost" onClick={() => { setShowIngresoForm(false); setShowIngresoResumen(false); }}>Cancelar</Button>
            <Button type="submit" variant="primary">
              <Eye className="w-4 h-4 mr-2" /> Ver Resumen
            </Button>
          </div>
        </form>
      </Modal>

      <Modal isOpen={showIngresoResumen} onClose={() => setShowIngresoResumen(false)} title="Resumen de la Compra" maxWidth="max-w-3xl">
        <div className="space-y-5">
          <div className="rounded-xl border border-indigo-100 bg-indigo-50/60 p-4">
            <div className="flex items-start justify-between gap-3">
              <div>
                <p className="text-xs font-bold text-slate-400 uppercase tracking-widest mb-1">Proveedor</p>
                <p className="font-bold text-indigo-700">{proveedores.find(p => p.id === parseInt(ingresoForm.proveedor_id))?.razon_social || proveedores.find(p => p.id === parseInt(ingresoForm.proveedor_id))?.nombre || '—'}</p>
                <p className="text-xs text-slate-500 mt-0.5">RUC {proveedores.find(p => p.id === parseInt(ingresoForm.proveedor_id))?.ruc || '—'} · Moneda: {monedaLabel(proveedorMoneda)}</p>
              </div>
              <div className="text-right">
                <p className="text-xs font-bold text-slate-400 uppercase tracking-widest mb-1">Comprobante</p>
                <p className="font-mono font-bold text-slate-800">{ingresoForm.tipo_comprobante} {ingresoForm.serie}-{ingresoForm.numero}</p>
                <p className="text-xs text-slate-500 mt-0.5">{new Date().toLocaleDateString('es-ES')} · {ingresoForm.tipo_pago}</p>
              </div>
            </div>
          </div>

          <div className="overflow-x-auto rounded-xl border border-slate-200">
            <table className="w-full text-left border-collapse min-w-[560px]">
              <thead>
                <tr className="border-b border-slate-200 bg-slate-50">
                  <th className="px-4 py-3 text-xs font-bold text-slate-400 uppercase tracking-widest">Producto</th>
                  <th className="px-4 py-3 text-xs font-bold text-slate-400 uppercase tracking-widest">Cant.</th>
                  <th className="px-4 py-3 text-xs font-bold text-slate-400 uppercase tracking-widest">Costo</th>
                  <th className="px-4 py-3 text-xs font-bold text-slate-400 uppercase tracking-widest">P. Venta</th>
                  <th className="px-4 py-3 text-xs font-bold text-slate-400 uppercase tracking-widest">Vencimiento</th>
                  <th className="px-4 py-3 text-xs font-bold text-slate-400 uppercase tracking-widest">Subtotal</th>
                </tr>
              </thead>
              <tbody>
                {detallesIngreso().map((det, i) => {
                  const prod = productos.find(p => p.id === det.producto_id)
                  return (
                    <tr key={i} className="border-b border-slate-100 last:border-0">
                      <td className="px-4 py-3 text-slate-900 font-medium">{prod?.nombre || `Producto #${det.producto_id}`}</td>
                      <td className="px-4 py-3 text-slate-600">{det.cantidad}</td>
                      <td className="px-4 py-3 text-slate-600">${det.precio_unitario.toFixed(2)}</td>
                      <td className="px-4 py-3 text-slate-600">{det.precio_venta ? `$${det.precio_venta.toFixed(2)}` : '—'}</td>
                      <td className="px-4 py-3 text-slate-600">{det.fecha_vencimiento || '—'}</td>
                      <td className="px-4 py-3 font-bold text-slate-900">${(det.precio_unitario * det.cantidad).toFixed(2)}</td>
                    </tr>
                  )
                })}
              </tbody>
              <tfoot>
                <tr className="border-t border-slate-200 bg-slate-50">
                  <td colSpan="5" className="px-4 py-3 text-sm font-bold text-slate-400 uppercase tracking-widest">Total de la Compra</td>
                  <td className="px-4 py-3 text-lg font-black text-indigo-600">${ingresoTotal.toFixed(2)}</td>
                </tr>
              </tfoot>
            </table>
          </div>

          <p className="text-xs text-slate-400">Revisa los datos antes de confirmar. Al registrar, el stock y el costo de compra de cada producto se actualizan automáticamente.</p>

          <div className="flex justify-end space-x-3">
            <Button type="button" variant="ghost" onClick={() => setShowIngresoResumen(false)}>Volver a Editar</Button>
            <Button type="button" variant="primary" onClick={handleConfirmarIngreso}>
              <CheckCircle2 className="w-4 h-4 mr-2" /> Confirmar y Registrar Compra
            </Button>
          </div>
        </div>
      </Modal>

      <Modal isOpen={!!ingresoDetail} onClose={() => setIngresoDetail(null)} title="Detalle del Ingreso de Mercadería" maxWidth="max-w-2xl">
        {ingresoDetail && (
          <div className="space-y-4">
            <div className="grid grid-cols-2 md:grid-cols-4 gap-4 text-sm">
              <div>
                <p className="text-xs font-bold text-slate-500 uppercase tracking-widest mb-1">Proveedor</p>
                <p className="font-bold text-indigo-600">{ingresoDetail.proveedor_nombre}</p>
              </div>
              <div>
                <p className="text-xs font-bold text-slate-500 uppercase tracking-widest mb-1">Fecha</p>
                <p className="text-slate-900">{new Date(ingresoDetail.fecha).toLocaleString('es-ES')}</p>
              </div>
              <div>
                <p className="text-xs font-bold text-slate-500 uppercase tracking-widest mb-1">Tipo de Pago</p>
                <p className="text-slate-900">{ingresoDetail.tipo_pago}</p>
              </div>
              <div>
                <p className="text-xs font-bold text-slate-500 uppercase tracking-widest mb-1">Comprobante</p>
                <p className="text-slate-900 font-bold">{ingresoDetail.tipo_comprobante || 'Factura'}</p>
                <p className="font-mono text-slate-900">{ingresoDetail.numero_factura || '—'}</p>
              </div>
            </div>

            <div className="overflow-x-auto rounded-xl border border-slate-200">
              <table className="w-full text-left border-collapse min-w-[420px]">
                <thead>
                  <tr className="border-b border-slate-200 bg-slate-50">
                    <th className="px-4 py-3 text-xs font-bold text-slate-400 uppercase tracking-widest">Producto</th>
                    <th className="px-4 py-3 text-xs font-bold text-slate-400 uppercase tracking-widest">Cant.</th>
                    <th className="px-4 py-3 text-xs font-bold text-slate-400 uppercase tracking-widest">P. Unitario</th>
                    <th className="px-4 py-3 text-xs font-bold text-slate-400 uppercase tracking-widest">Vencimiento</th>
                    <th className="px-4 py-3 text-xs font-bold text-slate-400 uppercase tracking-widest">Subtotal</th>
                  </tr>
                </thead>
                <tbody>
                  {(ingresoDetail.detalles || []).map(d => (
                    <tr key={d.id} className="border-b border-slate-100 last:border-0">
                      <td className="px-4 py-3 text-slate-900 font-medium">{d.producto_nombre || `Producto #${d.producto_id}`}</td>
                      <td className="px-4 py-3 text-slate-600">{d.cantidad}</td>
                      <td className="px-4 py-3 text-slate-600">${d.precio_unitario.toFixed(2)}</td>
                      <td className="px-4 py-3 text-slate-600">{d.fecha_vencimiento ? vencimientoBadge(d.fecha_vencimiento) : '—'}</td>
                      <td className="px-4 py-3 text-slate-600">${d.subtotal.toFixed(2)}</td>
                    </tr>
                  ))}
                </tbody>
                <tfoot>
                  <tr className="border-t border-slate-200 bg-slate-50">
                    <td colSpan="4" className="px-4 py-3 text-sm font-bold text-slate-400 uppercase tracking-widest">Total</td>
                    <td className="px-4 py-3 text-lg font-black text-indigo-600">${ingresoDetail.total.toFixed(2)}</td>
                  </tr>
                </tfoot>
              </table>
            </div>

            <div className="flex justify-end gap-2">
              <RoleGate allowedRoles={['Administrador']}>
                <Button type="button" variant="danger" onClick={() => handleDeleteIngreso(ingresoDetail)}>
                  <Trash2 className="w-4 h-4 mr-2" /> Eliminar y Revertir Stock
                </Button>
              </RoleGate>
              <Button type="button" variant="ghost" onClick={() => setIngresoDetail(null)}>Cerrar</Button>
            </div>
          </div>
        )}
      </Modal>

      <Modal isOpen={showScanner} onClose={() => setShowScanner(false)} title="Escanear Código de Barras" maxWidth="max-w-md">
        <div className="space-y-4">
          <div id="barcode-reader" className="w-full rounded-xl overflow-hidden bg-slate-900 min-h-[240px]"></div>
          {scanError && (
            <div className="p-3 rounded-xl border border-rose-200 bg-rose-50 text-sm text-rose-700">
              <p className="font-bold">No se pudo usar la cámara</p>
              <p className="text-rose-600 text-xs mt-1">{scanError}</p>
            </div>
          )}
          <p className="text-xs text-slate-400 leading-relaxed">
            Apunta la cámara al código de barras del producto. En cuanto se detecte, el campo se llenará automáticamente y escucharás un sonido de confirmación. También puedes escribir el código manualmente.
          </p>
          <div className="flex justify-end">
            <Button type="button" variant="ghost" onClick={() => setShowScanner(false)}>Cancelar</Button>
          </div>
        </div>
      </Modal>

      <Modal isOpen={showCatalogoForm} onClose={() => setShowCatalogoForm(false)} title={editandoCatalogo ? `Editar ${catalogoTipo.slice(0, -1)}` : `Nueva ${catalogoTipo.slice(0, -1)}`} maxWidth="max-w-sm">
        <form onSubmit={handleSaveCatalogo} className="space-y-4">
          <Input label="Nombre" required type="text" autoFocus value={catalogoForm.nombre} onChange={e => setCatalogoForm({...catalogoForm, nombre: e.target.value})} />
          <div className="flex justify-end space-x-3 pt-2">
            <Button type="button" variant="ghost" onClick={() => setShowCatalogoForm(false)}>Cancelar</Button>
            <Button type="submit" variant="primary">{editandoCatalogo ? "Guardar Cambios" : "Guardar"}</Button>
          </div>
        </form>
      </Modal>
    </div>
  )
}
