import { useState, useEffect } from 'react'
import { useToast } from '../components/ui/ToastProvider'
import { TableSkeleton } from '../components/ui/Skeleton';
import api from '../api/axiosConfig';
import { ClipboardList, ShieldAlert, RefreshCw, Activity } from 'lucide-react';
import PageHeader from '../components/ui/PageHeader';
import DataTable from '../components/ui/DataTable';
import StatusBadge from '../components/ui/StatusBadge';
import Button from '../components/ui/Button';

export default function Auditoria() {
  const toast = useToast();
  const [logs, setLogs] = useState([]);
  const [loading, setLoading] = useState(true);
  ;

  useEffect(() => {
    fetchLogs();
  }, []);

  const fetchLogs = async () => {
    setLoading(true);
    try {
      const res = await api.get('/api/auditoria');
      setLogs(res.data);
    } catch (err) {
      toast.error("No tienes permisos o ocurrió un error al cargar la auditoría.");
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const getActionColor = (accion) => {
    switch (accion) {
      case 'POST': return 'bg-emerald-50 text-emerald-600 border border-emerald-200';
      case 'PUT': return 'bg-indigo-50 text-indigo-600 border border-indigo-200';
      case 'DELETE': return 'bg-rose-50 text-rose-600 border border-rose-200';
      case 'LOGIN': return 'bg-violet-50 text-violet-600 border border-violet-200';
      default: return 'bg-slate-50 text-slate-400 border border-slate-200';
    }
  };

  const columns = [
    {
      header: 'ID',
      accessor: 'id',
      render: (item) => <span className="text-indigo-600/70 font-mono text-xs">#{item.id}</span>
    },
    {
      header: 'Fecha / Hora',
      accessor: 'timestamp',
      render: (item) => <span className="text-slate-600">{new Date(item.timestamp).toLocaleString()}</span>
    },
    {
      header: 'Usuario ID',
      accessor: 'usuario_id',
      render: (item) => (
        <span className="font-bold text-slate-900">
          {item.usuario_id ? item.usuario_id : 'Sistema/Anónimo'}
        </span>
      )
    },
    {
      header: 'Acción',
      accessor: 'accion',
      render: (item) => (
        <span className={`px-2 py-1 rounded-full text-xs font-bold ${getActionColor(item.accion)}`}>
          {item.accion}
        </span>
      )
    },
    {
      header: 'Endpoint',
      accessor: 'endpoint',
      render: (item) => (
        <span className="font-mono text-xs text-indigo-600 max-w-[200px] truncate block" title={item.endpoint}>
          {item.endpoint}
        </span>
      )
    },
    {
      header: 'Detalle',
      accessor: 'detalle',
      render: (item) => <span className="text-slate-400 text-xs">{item.detalle || '-'}</span>
    }
  ];

  return (
    <div className="space-y-6 animate-fade-in-up">
      <PageHeader 
        title="Registro de Auditoría" 
        description="Supervisión de seguridad y cambios en el sistema"
        actions={
          <Button onClick={fetchLogs} variant="secondary" isLoading={loading}>
            <RefreshCw className={`w-4 h-4 mr-2 ${loading ? 'animate-spin' : ''}`} />
            Refrescar
          </Button>
        }
      />

      
      <DataTable 
        columns={columns}
        data={logs}
        emptyMessage="No hay registros de auditoría aún."
      />
    </div>
  );
}
