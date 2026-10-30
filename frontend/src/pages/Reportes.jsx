import { useState, useEffect } from 'react'
import { useToast } from '../components/ui/ToastProvider'
import { TableSkeleton } from '../components/ui/Skeleton'
import api from '../api/axiosConfig'
import { BarChart2, Calendar, Download, TrendingUp, Package, Users, Activity } from 'lucide-react'
import { 
  AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer,
  BarChart, Bar, Legend,
  LineChart, Line,
  PieChart, Pie, Cell
} from 'recharts'
import jsPDF from 'jspdf'
import 'jspdf-autotable'
import PageHeader from '../components/ui/PageHeader'
import StatCard from '../components/ui/StatCard'
import Button from '../components/ui/Button'
import Input from '../components/ui/Input'

export default function Reportes() {
  const toast = useToast();
  const [ventas, setVentas] = useState([])
  const [productos, setProductos] = useState([])
  const [citas, setCitas] = useState([])
  const [demografia, setDemografia] = useState([])
  
  const [loading, setLoading] = useState(true)
  
  const today = new Date()
  const thirtyDaysAgo = new Date()
  thirtyDaysAgo.setDate(today.getDate() - 30)
  
  const [fechaInicio, setFechaInicio] = useState(thirtyDaysAgo.toISOString().split('T')[0])
  const [fechaFin, setFechaFin] = useState(today.toISOString().split('T')[0])

  const fetchReportes = async () => {
    setLoading(true)
    try {
      const params = { fecha_inicio: fechaInicio, fecha_fin: fechaFin }
      const [vRes, pRes, cRes, dRes] = await Promise.all([
        api.get('/api/reportes/ventas', { params }),
        api.get('/api/reportes/productos', { params }),
        api.get('/api/reportes/citas', { params }),
        api.get('/api/reportes/demografia')
      ])
      
      setVentas(vRes.data)
      setProductos(pRes.data)
      setCitas(cRes.data)
      setDemografia(dRes.data)
    } catch (err) {
      console.error("Error cargando reportes:", err)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchReportes()
  }, [fechaInicio, fechaFin])

  const COLORS = ['#22d3ee', '#818cf8', '#c084fc', '#f472b6', '#34d399']

  const exportPDF = () => {
    const doc = new jsPDF()
    doc.setFontSize(18)
    doc.text("Clínica Veterinaria Sirbull", 14, 20)
    
    doc.setFontSize(14)
    doc.text("Reporte de Desempeño y Analíticas", 14, 30)
    
    doc.setFontSize(10)
    doc.text(`Período: ${fechaInicio} a ${fechaFin}`, 14, 38)
    doc.text(`Generado el: ${new Date().toLocaleString()}`, 14, 43)

    const totalVentas = ventas.reduce((acc, curr) => acc + curr.total, 0)
    doc.setFontSize(12)
    doc.text(`Resumen de Ingresos Totales: $${totalVentas.toFixed(2)}`, 14, 53)

    const ventasRows = ventas.map(v => [v.fecha, `$${v.total.toFixed(2)}`])
    doc.autoTable({
      head: [["Fecha", "Total Ingresos"]],
      body: ventasRows,
      startY: 60,
      theme: 'grid',
      headStyles: { fillColor: [34, 211, 238] }
    })

    const finalY = doc.lastAutoTable.finalY || 60
    doc.text("Top Productos Más Vendidos", 14, finalY + 15)
    
    const prodRows = productos.map(p => [p.nombre, p.cantidad])
    doc.autoTable({
      head: [["Producto", "Cantidad Vendida"]],
      body: prodRows,
      startY: finalY + 20,
      theme: 'grid',
      headStyles: { fillColor: [129, 140, 248] }
    })

    doc.save(`Reporte_Sirbull_${fechaInicio}_a_${fechaFin}.pdf`)
  }

  const totalVentas = ventas.reduce((acc, curr) => acc + curr.total, 0)
  const totalCitas = citas.reduce((acc, curr) => acc + curr.cantidad, 0)
  const citasCompletadas = citas.filter(c => c.estado === 'Completada').reduce((acc, curr) => acc + curr.cantidad, 0)
  const totalPacientes = demografia.reduce((acc, curr) => acc + curr.cantidad, 0)

  // Custom Tooltip for Recharts to match Dark Theme
  const CustomTooltip = ({ active, payload, label }) => {
    if (active && payload && payload.length) {
      return (
        <div className="bg-white border border-slate-200 p-3 rounded-lg shadow-xl backdrop-blur-md">
          <p className="text-slate-900 font-bold text-sm mb-1">{label}</p>
          {payload.map((entry, index) => (
            <p key={index} className="text-sm" style={{ color: entry.color }}>
              {entry.name}: {entry.name === 'Ingresos' ? '$' : ''}{entry.value}
            </p>
          ))}
        </div>
      )
    }
    return null
  }

  return (
    <div className="space-y-6 animate-fade-in-up">
      <PageHeader 
        title="Reportes y Analíticas" 
        description="Métricas de rendimiento e ingresos"
        actions={
          <Button onClick={exportPDF} variant="primary">
            <Download className="w-4 h-4 mr-2" /> Exportar PDF
          </Button>
        }
      />

      <div className="bg-white backdrop-blur-xl border border-slate-100 rounded-2xl p-4 shadow-sm flex flex-col md:flex-row gap-4 items-end z-20 relative">
        <div className="w-full md:w-auto flex-1">
          <Input 
            label="Fecha Inicio"
            type="date"
            value={fechaInicio}
            onChange={(e) => setFechaInicio(e.target.value)}
          />
        </div>
        <div className="w-full md:w-auto flex-1">
          <Input 
            label="Fecha Fin"
            type="date"
            value={fechaFin}
            onChange={(e) => setFechaFin(e.target.value)}
          />
        </div>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
        <StatCard title="Ingresos Totales" value={`$${totalVentas.toFixed(2)}`} icon={TrendingUp} trend="+12%" color="text-indigo-600" />
        <StatCard title="Citas Atendidas" value={citasCompletadas} icon={Activity} trend="+5%" color="text-emerald-600" />
        <StatCard title="Total Citas" value={totalCitas} icon={Calendar} trend="Estable" color="text-purple-400" />
        <StatCard title="Pacientes Activos" value={totalPacientes} icon={Users} trend="Creciendo" color="text-blue-400" />
      </div>

      {loading ? (
        <TableSkeleton />
      ) : (
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          
          <div className="bg-white border border-slate-100 p-6 rounded-2xl shadow-xl">
            <h3 className="text-lg font-bold text-slate-900 mb-6 flex items-center">
              <TrendingUp className="w-5 h-5 mr-2 text-indigo-600" /> Evolución de Ingresos
            </h3>
            <div className="h-72">
              <ResponsiveContainer width="100%" height="100%">
                <AreaChart data={ventas}>
                  <defs>
                    <linearGradient id="colorTotal" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="5%" stopColor="#22d3ee" stopOpacity={0.3}/>
                      <stop offset="95%" stopColor="#22d3ee" stopOpacity={0}/>
                    </linearGradient>
                  </defs>
                  <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.05)" vertical={false} />
                  <XAxis dataKey="fecha" stroke="rgba(255,255,255,0.3)" tick={{fill: '#94a3b8', fontSize: 12}} />
                  <YAxis stroke="rgba(255,255,255,0.3)" tick={{fill: '#94a3b8', fontSize: 12}} tickFormatter={(val) => `$${val}`} />
                  <Tooltip content={<CustomTooltip />} />
                  <Area type="monotone" dataKey="total" name="Ingresos" stroke="#22d3ee" strokeWidth={3} fillOpacity={1} fill="url(#colorTotal)" />
                </AreaChart>
              </ResponsiveContainer>
            </div>
          </div>

          <div className="bg-white border border-slate-100 p-6 rounded-2xl shadow-xl">
            <h3 className="text-lg font-bold text-slate-900 mb-6 flex items-center">
              <Package className="w-5 h-5 mr-2 text-purple-400" /> Top Productos Vendidos
            </h3>
            <div className="h-72">
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={productos} layout="vertical" margin={{ top: 5, right: 30, left: 20, bottom: 5 }}>
                  <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.05)" horizontal={false} />
                  <XAxis type="number" stroke="rgba(255,255,255,0.3)" tick={{fill: '#94a3b8', fontSize: 12}} />
                  <YAxis dataKey="nombre" type="category" stroke="rgba(255,255,255,0.3)" tick={{fill: '#94a3b8', fontSize: 12}} width={120} />
                  <Tooltip content={<CustomTooltip />} />
                  <Bar dataKey="cantidad" name="Vendidos" fill="#c084fc" radius={[0, 4, 4, 0]}>
                    {productos.map((entry, index) => (
                      <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                    ))}
                  </Bar>
                </BarChart>
              </ResponsiveContainer>
            </div>
          </div>

          <div className="bg-white border border-slate-100 p-6 rounded-2xl shadow-xl">
            <h3 className="text-lg font-bold text-slate-900 mb-6 flex items-center">
              <Activity className="w-5 h-5 mr-2 text-emerald-600" /> Estado de Citas Médicas
            </h3>
            <div className="h-72">
              <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                  <Pie
                    data={citas}
                    cx="50%"
                    cy="50%"
                    innerRadius={60}
                    outerRadius={100}
                    paddingAngle={5}
                    dataKey="cantidad"
                    nameKey="estado"
                    label={({ name, percent }) => `${name} ${(percent * 100).toFixed(0)}%`}
                    labelLine={false}
                  >
                    {citas.map((entry, index) => (
                      <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                    ))}
                  </Pie>
                  <Tooltip content={<CustomTooltip />} />
                  <Legend wrapperStyle={{ color: '#94a3b8' }} />
                </PieChart>
              </ResponsiveContainer>
            </div>
          </div>

          <div className="bg-white border border-slate-100 p-6 rounded-2xl shadow-xl">
            <h3 className="text-lg font-bold text-slate-900 mb-6 flex items-center">
              <Users className="w-5 h-5 mr-2 text-blue-400" /> Distribución Demográfica (Especies)
            </h3>
            <div className="h-72">
              <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                  <Pie
                    data={demografia}
                    cx="50%"
                    cy="50%"
                    outerRadius={100}
                    dataKey="cantidad"
                    nameKey="especie"
                    label={({ name, percent }) => `${name} ${(percent * 100).toFixed(0)}%`}
                    labelLine={false}
                  >
                    {demografia.map((entry, index) => (
                      <Cell key={`cell-${index}`} fill={COLORS[(index + 2) % COLORS.length]} />
                    ))}
                  </Pie>
                  <Tooltip content={<CustomTooltip />} />
                </PieChart>
              </ResponsiveContainer>
            </div>
          </div>

        </div>
      )}
    </div>
  )
}
