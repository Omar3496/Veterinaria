import React, { useEffect, useState, useCallback, useRef } from 'react';
import { Link } from 'react-router-dom';
import { useToast } from '../components/ui/ToastProvider';
import { TableSkeleton } from '../components/ui/Skeleton';
import { useAuthStore } from '../store/authStore';
import api from '../api/axiosConfig';
import {
  PawPrint,
  Syringe,
  Calendar,
  Camera,
  Bell,
  Phone,
  Clock,
  Stethoscope,
  FileText,
  CheckCircle2,
  AlertTriangle,
  CalendarPlus,
  ChevronLeft,
  ChevronRight,
  ShoppingBag,
  Pill,
  FlaskConical,
  Send
} from 'lucide-react';
import Card from '../components/ui/Card';
import StatusBadge from '../components/ui/StatusBadge';
import SiteHeader from '../components/SiteHeader';
import Button from '../components/ui/Button';
import Modal from '../components/ui/Modal';
import Input from '../components/ui/Input';
import DataTable from '../components/ui/DataTable';

const formatDate = (value) => {
  if (!value) return '—';
  const d = new Date(value);
  if (isNaN(d.getTime())) return '—';
  return d.toLocaleDateString('es-ES', { year: 'numeric', month: 'short', day: 'numeric' });
};

const formatDateTime = (value) => {
  if (!value) return '—';
  const d = new Date(value);
  if (isNaN(d.getTime())) return '—';
  return d.toLocaleString('es-ES', { day: 'numeric', month: 'short', hour: '2-digit', minute: '2-digit' });
};

const citaBadgeStatus = (estado) => {
  switch (estado) {
    case 'Programada': return 'pendiente';
    case 'Completada': return 'completado';
    case 'Cancelada': return 'cancelado';
    default: return 'pendiente';
  }
};

const ventaEstadoInfo = {
  'Pendiente': { label: 'Pendiente de confirmación', cls: 'bg-amber-50 text-amber-700 border-amber-200' },
  'Confirmada': { label: 'Listo para recoger', cls: 'bg-emerald-50 text-emerald-700 border-emerald-200' },
  'Entregada': { label: 'Recogida', cls: 'bg-slate-100 text-slate-600 border-slate-200' },
  'Cancelada': { label: 'Cancelada', cls: 'bg-rose-50 text-rose-600 border-rose-200' },
  'Anulación solicitada': { label: 'Anulación solicitada', cls: 'bg-orange-50 text-orange-700 border-orange-200' },
};

const VentaBadge = ({ estado }) => {
  const info = ventaEstadoInfo[estado] || ventaEstadoInfo['Pendiente'];
  return (
    <span className={`inline-block px-2 py-0.5 rounded-full text-[0.75rem] font-bold border ${info.cls}`}>
      {info.label}
    </span>
  );
};

const calcEdad = (fechaNacimiento) => {
  if (!fechaNacimiento) return null;
  const nac = new Date(fechaNacimiento);
  const hoy = new Date();
  if (isNaN(nac.getTime())) return null;
  let años = hoy.getFullYear() - nac.getFullYear();
  const m = hoy.getMonth() - nac.getMonth();
  if (m < 0 || (m === 0 && hoy.getDate() < nac.getDate())) años--;
  if (años < 1) {
    const meses = Math.max(0, hoy.getMonth() - nac.getMonth() + 12 * (hoy.getFullYear() - nac.getFullYear()));
    return `${meses} mes${meses !== 1 ? 'es' : ''}`;
  }
  return `${años} año${años !== 1 ? 's' : ''}`;
};

const diasRestantes = (fecha) => {
  if (!fecha) return null;
  const hoy = new Date();
  hoy.setHours(0, 0, 0, 0);
  const target = new Date(fecha);
  target.setHours(0, 0, 0, 0);
  return Math.round((target - hoy) / 86400000);
};

const PetCard = ({ mascota, proximaDosis, onSchedule, onViewHistorial, onUploadPhoto }) => {
  const edad = calcEdad(mascota.fecha_nacimiento);
  const days = diasRestantes(proximaDosis);
  const doseTone = days == null ? 'emerald' : days < 0 ? 'rose' : days <= 3 ? 'orange' : days <= 7 ? 'amber' : 'emerald';
  const doseTones = {
    emerald: 'bg-emerald-50 text-emerald-700 border-emerald-100',
    amber: 'bg-amber-50 text-amber-700 border-amber-100',
    orange: 'bg-orange-50 text-orange-700 border-orange-100',
    rose: 'bg-rose-50 text-rose-700 border-rose-100',
  };
  return (
    <Card
      variant="default"
      className="flex flex-col p-5 bg-white border-slate-100 hover:border-emerald-200 hover:shadow-[0_16px_40px_-16px_rgba(4,120,87,0.25)] transition-all duration-300 hover:-translate-y-1"
    >
      <div className="flex items-start justify-between">
        <div className="flex items-center">
          <div className="relative">
            {mascota.foto_url ? (
              <img
                src={mascota.foto_url}
                alt={mascota.nombre}
                className="w-20 h-20 rounded-2xl object-cover border border-emerald-200 shadow-sm"
              />
            ) : (
              <div className="w-20 h-20 rounded-2xl bg-gradient-to-br from-emerald-50 to-emerald-100 border border-emerald-200 flex items-center justify-center">
                <PawPrint className="w-9 h-9 text-emerald-600" />
              </div>
            )}
            <label
              className="absolute -bottom-2 -right-2 bg-emerald-700 border-2 border-white rounded-full p-1.5 cursor-pointer hover:bg-emerald-600 transition-colors shadow-lg"
              title="Cambiar foto"
              onClick={(e) => {
                e.stopPropagation();
                const input = document.createElement('input');
                input.type = 'file';
                input.accept = 'image/*';
                input.onchange = (ev) => onUploadPhoto(mascota.id, ev.target.files[0]);
                input.click();
              }}
            >
              <Camera className="w-3 h-3 text-white" />
            </label>
          </div>
          <div className="ml-4">
            <h4 className="text-lg font-black text-slate-900 leading-tight">{mascota.nombre}</h4>
            <div className="flex items-center gap-1.5 mt-1.5 flex-wrap">
              <span className="text-[11px] font-bold text-emerald-700 bg-emerald-50 border border-emerald-100 px-2 py-0.5 rounded-full capitalize">{mascota.especie}</span>
              {mascota.raza && <span className="text-[11px] font-bold text-slate-500 bg-slate-50 border border-slate-200 px-2 py-0.5 rounded-full capitalize">{mascota.raza}</span>}
            </div>
            {edad && (
              <p className="text-xs text-slate-400 mt-1.5 flex items-center font-medium">
                <Clock className="w-3.5 h-3.5 mr-1" /> {edad}
              </p>
            )}
          </div>
        </div>
      </div>

      {days != null ? (
        <div className={`mt-4 flex items-center justify-between rounded-xl border px-3 py-2 ${doseTones[doseTone]}`}>
          <span className="text-xs font-bold flex items-center">
            <Syringe className="w-3.5 h-3.5 mr-1.5" /> Próxima dosis
          </span>
          <span className="text-xs font-black tabular-nums">
            {formatDate(proximaDosis)} · {days < 0 ? `vencida hace ${-days}d` : days === 0 ? 'hoy' : `en ${days}d`}
          </span>
        </div>
      ) : (
        <div className="mt-4 flex items-center justify-between rounded-xl border px-3 py-2 bg-slate-50 text-slate-400 border-slate-200">
          <span className="text-xs font-bold flex items-center">
            <CheckCircle2 className="w-3.5 h-3.5 mr-1.5" /> Vacunación al día
          </span>
          <span className="text-xs font-medium">Sin dosis próxima</span>
        </div>
      )}

      <div className="mt-auto pt-4 border-t border-slate-100 flex items-center gap-2.5">
        <Button
          variant="secondary"
          size="sm"
          onClick={() => onViewHistorial(mascota)}
          className="flex-1 text-xs font-bold"
        >
          <FileText className="w-4 h-4 mr-1.5" /> Historial Clínico
        </Button>
        <button
          type="button"
          title={`Agendar cita para ${mascota.nombre}`}
          aria-label={`Agendar cita para ${mascota.nombre}`}
          onClick={() => onSchedule(mascota.id)}
          className="inline-flex items-center justify-center w-10 h-10 shrink-0 rounded-xl bg-gradient-to-b from-emerald-600 to-emerald-700 text-white shadow-[inset_0_1px_0_rgba(255,255,255,0.22),0_3px_10px_-2px_rgba(4,120,87,0.5)] hover:from-emerald-500 hover:to-emerald-600 hover:-translate-y-0.5 hover:shadow-[inset_0_1px_0_rgba(255,255,255,0.28),0_10px_24px_-8px_rgba(4,120,87,0.6)] active:translate-y-0 transition-all duration-200"
        >
          <CalendarPlus className="w-5 h-5" />
        </button>
      </div>
    </Card>
  );
};

const ReminderItem = ({ recordatorio }) => {
  const days = diasRestantes(recordatorio.proxima_dosis);
  const isOverdue = days != null && days < 0;
  const tone = isOverdue ? 'rose' : days <= 3 ? 'orange' : days <= 7 ? 'amber' : 'emerald';
  const styles = {
    rose: { box: 'bg-rose-50 border-rose-100', icon: 'text-rose-500', bar: 'bg-rose-500', pill: 'bg-rose-50 text-rose-700 border-rose-100' },
    orange: { box: 'bg-orange-50 border-orange-100', icon: 'text-orange-500', bar: 'bg-orange-500', pill: 'bg-orange-50 text-orange-700 border-orange-100' },
    amber: { box: 'bg-amber-50 border-amber-100', icon: 'text-amber-600', bar: 'bg-amber-500', pill: 'bg-amber-50 text-amber-700 border-amber-100' },
    emerald: { box: 'bg-emerald-50 border-emerald-100', icon: 'text-emerald-600', bar: 'bg-emerald-500', pill: 'bg-emerald-50 text-emerald-700 border-emerald-100' },
  };
  const s = styles[tone];
  const label = isOverdue
    ? `Vencida hace ${-days} ${days === -1 ? 'día' : 'días'}`
    : days === 0 ? 'Hoy' : `En ${days} ${days === 1 ? 'día' : 'días'}`;

  return (
    <Card
      variant="secondary"
      className="p-4 bg-white border-slate-100 hover:border-slate-200 hover:shadow-md transition-all duration-200"
    >
      <div className="flex items-start space-x-3">
        <div className={`inline-flex p-2.5 rounded-xl border ${s.box}`}>
          <Syringe className={`w-4 h-4 ${s.icon}`} />
        </div>
        <div className="flex-1 min-w-0">
          <div className="flex items-center justify-between gap-2">
            <h4 className="font-bold text-sm text-slate-900 truncate">{recordatorio.tipo}</h4>
            <span className={`shrink-0 text-[11px] font-black px-2 py-0.5 rounded-full border ${s.pill}`}>{label}</span>
          </div>
          <p className="text-xs text-emerald-700 font-semibold mt-0.5">{recordatorio.mascota_nombre}</p>
          <div className="mt-2 h-1.5 rounded-full bg-slate-100 overflow-hidden">
            <div className={`h-full rounded-full ${s.bar}`} style={{ width: isOverdue ? '100%' : `${Math.max(12, 100 - days * 8)}%` }} />
          </div>
          <p className="text-[11px] text-slate-400 mt-1.5 font-medium">
            {isOverdue ? 'Próxima dosis' : 'Aplicar el'}: {formatDate(recordatorio.proxima_dosis)}
          </p>
        </div>
      </div>
    </Card>
  );
};

const WEEKDAYS = ['Lu', 'Ma', 'Mi', 'Ju', 'Vi', 'Sá', 'Do'];
const MONTHS = ['Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio', 'Julio', 'Agosto', 'Septiembre', 'Octubre', 'Noviembre', 'Diciembre'];

const startOfDay = (d) => new Date(d.getFullYear(), d.getMonth(), d.getDate());
const toDateStr = (d) =>
  `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
const startOfWeek = (d) => {
  const date = startOfDay(d);
  const offset = (date.getDay() + 6) % 7;
  date.setDate(date.getDate() - offset);
  return date;
};
const addDays = (d, n) => new Date(d.getFullYear(), d.getMonth(), d.getDate() + n);
const getWeekDays = (weekStart) => Array.from({ length: 7 }, (_, i) => addDays(weekStart, i));
const weekLabel = (ws) => {
  const we = addDays(ws, 6);
  const dayFmt = (d) => d.getDate();
  const monthFmt = (d) => MONTHS[d.getMonth()].slice(0, 3);
  const sameMonth = ws.getMonth() === we.getMonth() && ws.getFullYear() === we.getFullYear();
  if (sameMonth) return `${dayFmt(ws)} – ${dayFmt(we)} ${monthFmt(ws)} ${ws.getFullYear()}`;
  if (ws.getFullYear() === we.getFullYear())
    return `${dayFmt(ws)} ${monthFmt(ws)} – ${dayFmt(we)} ${monthFmt(we)} ${ws.getFullYear()}`;
  return `${dayFmt(ws)} ${monthFmt(ws)} ${ws.getFullYear()} – ${dayFmt(we)} ${monthFmt(we)} ${we.getFullYear()}`;
};

const AgendarCitaModal = ({ isOpen, onClose, mascotas, onConfirm, citaReprogramar = null }) => {
  const toast = useToast();
  const [mascotaId, setMascotaId] = useState('');
  const [weekStart, setWeekStart] = useState(() => startOfWeek(new Date()));
  const [selectedDate, setSelectedDate] = useState(null);
  const [selectedTime, setSelectedTime] = useState('');
  const [disponibilidadPorDia, setDisponibilidadPorDia] = useState({});
  const [loadingDias, setLoadingDias] = useState(false);
  const [motivo, setMotivo] = useState('');
  const [tipo, setTipo] = useState('Consulta médica');
  const [examenes, setExamenes] = useState([]);
  const [examenId, setExamenId] = useState('');
  const [loading, setLoading] = useState(false);
  const fetchCounter = useRef(0);

  const TIPOS_CITA = ['Consulta médica', 'Control / Seguimiento', 'Vacunación', 'Baño / Aseo', 'Examen auxiliar', 'Cirugía'];

  const originalHora = citaReprogramar ? (() => {
    const d = new Date(citaReprogramar.fecha_hora);
    const pad = (n) => String(n).padStart(2, '0');
    return `${pad(d.getHours())}:${pad(d.getMinutes())}`;
  })() : null;

  useEffect(() => {
    if (isOpen) {
      if (citaReprogramar) {
        const d = new Date(citaReprogramar.fecha_hora);
        setMascotaId(citaReprogramar.mascota_id);
        setWeekStart(startOfWeek(d));
        setSelectedDate(d);
        setSelectedTime(originalHora);
        setMotivo('');
      } else {
        setMascotaId(mascotas.length === 1 ? mascotas[0].id : '');
        setWeekStart(startOfWeek(new Date()));
        setSelectedDate(null);
        setSelectedTime('');
        setMotivo('');
        setTipo('Consulta médica');
        setExamenId('');
        api
          .get('/api/examenes-auxiliares')
          .then((r) => setExamenes(Array.isArray(r.data) ? r.data.filter((e) => e.activo !== false) : []))
          .catch(() => setExamenes([]));
      }
    }
  }, [isOpen, mascotas, citaReprogramar, originalHora]);

  useEffect(() => {
    const counter = ++fetchCounter.current;
    const days = getWeekDays(weekStart);
    setLoadingDias(true);
    Promise.all(
      days.map((d) =>
        api
          .get('/api/citas/disponibilidad', { params: { fecha: toDateStr(d) } })
          .then((r) => [toDateStr(d), r.data])
          .catch(() => [toDateStr(d), null])
      )
    ).then((results) => {
      if (counter !== fetchCounter.current) return;
      setDisponibilidadPorDia((prev) => {
        const next = { ...prev };
        results.forEach(([k, v]) => { next[k] = v; });
        return next;
      });
      setLoadingDias(false);
    });
  }, [weekStart]);

  const today = startOfDay(new Date());
  const weekDays = getWeekDays(weekStart);

  const handleDayClick = (d) => {
    if (startOfDay(d) < today) return;
    if (!selectedDate || startOfDay(d).getTime() !== startOfDay(selectedDate).getTime()) {
      setSelectedTime('');
    }
    setSelectedDate(d);
  };

  const dayStatus = (d) => {
    const key = toDateStr(d);
    if (startOfDay(d) < today) return { type: 'past', label: 'Pasado' };
    const disp = disponibilidadPorDia[key];
    if (!disp) return { type: 'loading', label: '…' };
    if (!disp.disponible) return { type: 'closed', label: 'Cerrado' };
    const libres = disp.horarios.filter((s) => s.disponible).length;
    if (libres === 0) return { type: 'full', label: 'Completo' };
    return { type: 'free', label: 'Libre' };
  };

  const selectedDisponibilidad = selectedDate ? disponibilidadPorDia[toDateStr(selectedDate)] : null;
  const selectedLibres = selectedDisponibilidad?.disponible
    ? selectedDisponibilidad.horarios.filter((s) => s.disponible).length
    : 0;

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!mascotaId) { toast.error('Selecciona una mascota'); return; }
    if (!selectedDate) { toast.error('Selecciona un día en el calendario'); return; }
    if (!selectedTime) { toast.error('Selecciona una hora'); return; }
    if (!motivo.trim()) { toast.error('Indica el motivo de la cita'); return; }
    if (tipo === 'Examen auxiliar' && !examenId) { toast.error('Selecciona el examen auxiliar que deseas realizarte'); return; }
    const [hh, mm] = selectedTime.split(':').map(Number);
    const fechaHora = new Date(selectedDate.getFullYear(), selectedDate.getMonth(), selectedDate.getDate(), hh, mm);
    setLoading(true);
    await onConfirm(mascotaId, fechaHora.toISOString(), motivo, tipo, examenId);
    setLoading(false);
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title={citaReprogramar ? 'Reprogramar Cita' : 'Agendar Nueva Cita'} maxWidth="max-w-2xl lg:max-w-3xl">
      <form onSubmit={handleSubmit} className="space-y-5">
        {citaReprogramar && (
          <div className="p-3 rounded-xl bg-emerald-50 border border-emerald-200 text-sm">
            <p className="text-slate-500">
              Reprogramando: <span className="font-bold text-slate-900">{citaReprogramar.mascota_nombre}</span>
            </p>
            <p className="text-xs text-slate-400 mt-0.5">
              Actual: {formatDateTime(citaReprogramar.fecha_hora)} · Motivo: {citaReprogramar.motivo}
            </p>
          </div>
        )}

        {!citaReprogramar && (
          <div className="space-y-2">
            <label className="block text-xs font-bold text-slate-400 uppercase tracking-widest mb-2">Mascota</label>
            <select
              value={mascotaId}
              onChange={(e) => setMascotaId(Number(e.target.value))}
              className="w-full px-4 py-3 bg-white border border-slate-200 rounded-xl text-slate-900 focus:outline-none focus:border-emerald-500 focus:ring-1 focus:ring-emerald-500"
            >
              <option value="">Selecciona una mascota...</option>
              {mascotas.map((m) => (
                <option key={m.id} value={m.id}>
                  {m.nombre} ({m.especie} {m.raza || ''})
                </option>
              ))}
            </select>
          </div>
        )}

        {!citaReprogramar && (
          <div className="space-y-2">
            <label className="block text-xs font-bold text-slate-400 uppercase tracking-widest mb-2">
              Tipo de atención <span className="text-rose-600">*</span>
            </label>
            <select
              value={tipo}
              onChange={(e) => { setTipo(e.target.value); setExamenId(''); }}
              className="w-full px-4 py-3 bg-white border border-slate-200 rounded-xl text-slate-900 focus:outline-none focus:border-emerald-500 focus:ring-1 focus:ring-emerald-500"
            >
              {TIPOS_CITA.map((t) => (
                <option key={t} value={t}>{t}</option>
              ))}
            </select>
          </div>
        )}

        {!citaReprogramar && tipo === 'Examen auxiliar' && (
          <div className="space-y-2">
            <label className="block text-xs font-bold text-slate-400 uppercase tracking-widest mb-2">
              Examen auxiliar <span className="text-rose-600">*</span>
            </label>
            <select
              value={examenId}
              onChange={(e) => setExamenId(Number(e.target.value))}
              className="w-full px-4 py-3 bg-white border border-slate-200 rounded-xl text-slate-900 focus:outline-none focus:border-emerald-500 focus:ring-1 focus:ring-emerald-500"
            >
              <option value="">Selecciona un examen...</option>
              {examenes.map((ex) => (
                <option key={ex.id} value={ex.id}>
                  {ex.nombre}{ex.precio != null && ex.precio > 0 ? ` · $${Number(ex.precio).toFixed(2)}` : ''}
                </option>
              ))}
            </select>
            {examenes.length === 0 && (
              <p className="text-xs text-slate-400">No hay exámenes disponibles por el momento.</p>
            )}
          </div>
        )}

        <div className="space-y-2">
          <div className="flex items-center justify-between">
            <label className="block text-xs font-bold text-slate-400 uppercase tracking-widest mb-2">Semana</label>
            <button
              type="button"
              onClick={() => setWeekStart(startOfWeek(new Date()))}
              className="text-[11px] font-bold text-emerald-700 hover:text-emerald-600 transition-colors"
            >
              Hoy
            </button>
          </div>
          <div className="bg-slate-50 border border-slate-200 rounded-2xl p-3">
            <div className="flex items-center justify-between mb-3">
              <button
                type="button"
                onClick={() => setWeekStart(addDays(weekStart, -7))}
                className="p-1.5 rounded-lg bg-slate-50 hover:bg-slate-100 text-slate-600 transition-colors"
                title="Semana anterior"
              >
                <ChevronLeft className="w-4 h-4" />
              </button>
              <span className="text-sm font-bold text-slate-900 uppercase tracking-widest">{weekLabel(weekStart)}</span>
              <button
                type="button"
                onClick={() => setWeekStart(addDays(weekStart, 7))}
                className="p-1.5 rounded-lg bg-slate-50 hover:bg-slate-100 text-slate-600 transition-colors"
                title="Semana siguiente"
              >
                <ChevronRight className="w-4 h-4" />
              </button>
            </div>

            <div className="grid grid-cols-7 gap-1.5">
              {weekDays.map((d) => {
                const key = toDateStr(d);
                const status = dayStatus(d);
                const isSelected = selectedDate && startOfDay(d).getTime() === startOfDay(selectedDate).getTime();
                const statusColor = {
                  free: 'bg-emerald-50 text-emerald-700',
                  full: 'bg-rose-50 text-rose-700',
                  closed: 'bg-orange-50 text-orange-700',
                  past: 'bg-slate-50 text-slate-500',
                  loading: 'bg-slate-50 text-slate-500',
                }[status.type];

  return (
                  <button
                    type="button"
                    key={key}
                    disabled={status.type === 'past' || status.type === 'loading'}
                    onClick={() => handleDayClick(d)}
                    className={`flex flex-col items-center rounded-xl border py-2 px-1 min-w-0 transition-all
                      ${status.type === 'past' || status.type === 'loading'
                        ? 'opacity-50 cursor-not-allowed'
                        : 'cursor-pointer hover:border-emerald-300'}
                      ${isSelected
                        ? 'border-emerald-500 bg-emerald-100 shadow-md'
                        : 'border-slate-200 bg-white'}`}
                  >
                    <span className="text-[10px] uppercase text-slate-500">{WEEKDAYS[(d.getDay() + 6) % 7]}</span>
                    <span className={`text-base font-black ${isSelected ? 'text-emerald-600' : 'text-slate-900'}`}>{d.getDate()}</span>
                    <span className={`text-[9px] font-bold mt-1 px-1.5 py-0.5 rounded-full whitespace-nowrap ${statusColor}`}>
                      {status.type === 'loading' ? '…' : status.label}
                    </span>
                  </button>
                );
              })}
            </div>
            <div className="flex items-center gap-3 mt-3 pt-3 border-t border-slate-200 text-[10px] text-slate-500 flex-wrap">
              <span className="flex items-center gap-1"><span className="w-2 h-2 rounded-full bg-emerald-500 inline-block" /> con horas libres</span>
              <span className="flex items-center gap-1"><span className="w-2 h-2 rounded-full bg-rose-500 inline-block" /> completo</span>
              <span className="flex items-center gap-1"><span className="w-2 h-2 rounded-full bg-orange-500 inline-block" /> cerrado</span>
              <span className="flex items-center gap-1"><span className="w-2 h-2 rounded-full bg-slate-100 inline-block" /> pasado</span>
            </div>
          </div>
        </div>

        <div className="space-y-2">
          <label className="block text-xs font-bold text-slate-400 uppercase tracking-widest mb-2">Hora</label>
          {!selectedDate ? (
            <div className="py-5 text-center text-sm text-slate-400">Selecciona un día para ver los horarios.</div>
          ) : loadingDias ? (
            <div className="py-5 text-center text-sm text-slate-400">Cargando horarios...</div>
          ) : !selectedDisponibilidad ? (
            <div className="py-5 text-center text-sm text-slate-400">No se pudo consultar este día.</div>
          ) : !selectedDisponibilidad.disponible ? (
            <div className="p-3 rounded-xl bg-orange-500/10 border border-orange-500/20 text-sm text-orange-300">
              Este día no hay atención. Elige otro día.
            </div>
          ) : selectedLibres === 0 && !citaReprogramar ? (
            <div className="p-3 rounded-xl bg-rose-50 border border-rose-200 text-sm text-red-300">
              Este día está completo. Elige otro día o navega a la siguiente semana.
            </div>
          ) : (
            <>
              <div className="grid grid-cols-4 sm:grid-cols-6 gap-2">
                {selectedDisponibilidad.horarios.map((s) => (
                  <button
                    key={s.hora}
                    type="button"
                    disabled={!s.disponible && !(citaReprogramar && s.hora === originalHora)}
                    onClick={() => setSelectedTime(s.hora)}
                    className={`px-2 py-2 rounded-xl text-xs font-bold border transition-colors
                      ${selectedTime === s.hora
                        ? 'bg-emerald-700 border-emerald-600 text-white'
                        : s.disponible || (citaReprogramar && s.hora === originalHora)
                          ? 'bg-slate-50 border-slate-200 text-slate-900 hover:bg-slate-100'
                          : 'bg-slate-50 border-slate-200 text-slate-400 cursor-not-allowed line-through'}`}
                  >
                    {s.hora}
                  </button>
                ))}
              </div>
              <p className="text-[11px] text-slate-500">Selecciona una hora para este día.</p>
            </>
          )}
        </div>

        {selectedDate && (
          <div className="flex items-center justify-between gap-2 p-3 rounded-xl bg-emerald-50 border border-emerald-200">
            <span className="text-sm text-slate-600 capitalize flex items-center">
              <Calendar className="w-4 h-4 mr-2 text-emerald-700 shrink-0" />
              {selectedDate.toLocaleDateString('es-ES', { weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' })}
            </span>
            <span className="text-sm font-black text-emerald-700">{selectedTime || '—'}</span>
          </div>
        )}

        {!citaReprogramar && (
          <div className="space-y-2">
            <label className="block text-xs font-bold text-slate-400 uppercase tracking-widest mb-2">
              Motivo de la cita <span className="text-rose-600">*</span>
            </label>
            <input
              type="text"
              required
              value={motivo}
              onChange={(e) => setMotivo(e.target.value)}
              placeholder="Ej: Vacunación anual, control de peso, revisión de oídos..."
              className="w-full px-4 py-3 bg-white border border-slate-200 rounded-xl text-slate-900 focus:outline-none focus:border-emerald-500 focus:ring-1 focus:ring-emerald-500"
            />
          </div>
        )}

        <Button type="submit" variant="primary" className="w-full" isLoading={loading}>
          {citaReprogramar ? 'Confirmar Reprogramación' : 'Agendar Cita'}
        </Button>
      </form>
    </Modal>
  );
};

const CancelarCitaModal = ({ isOpen, onClose, cita, onConfirm }) => {
  const toast = useToast();
  const [motivo, setMotivo] = useState('');
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (isOpen) setMotivo('');
  }, [isOpen]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!motivo.trim()) { toast.error('Indica el motivo de la cancelación'); return; }
    setLoading(true);
    await onConfirm(cita.id, motivo);
    setLoading(false);
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Cancelar Cita" maxWidth="max-w-md">
      <div className="mb-4 flex items-start space-x-3 p-4 rounded-2xl bg-rose-50 border border-rose-200">
        <AlertTriangle className="w-5 h-5 text-rose-600 mt-0.5" />
        <div>
          <p className="text-slate-900 font-semibold">{cita ? `${cita.mascota_nombre} — ${formatDateTime(cita.fecha_hora)}` : ''}</p>
          <p className="text-xs text-slate-400">La cancelación quedará registrada en el sistema.</p>
        </div>
      </div>
      <form onSubmit={handleSubmit} className="space-y-5">
        <Input
          label="Motivo de cancelación"
          type="text"
          value={motivo}
          onChange={(e) => setMotivo(e.target.value)}
          placeholder="Ej: Impago, cambio de planes, urgencia..."
        />
        <Button type="submit" variant="danger" className="w-full" isLoading={loading}>
          Confirmar Cancelación
        </Button>
      </form>
    </Modal>
  );
};

const HistorialModal = ({ isOpen, onClose, mascota, historial, loading }) => {
  const vacunas = historial?.vacunas || [];
  const consultas = historial?.consultas || [];
  const recetas = historial?.recetas || [];
  const examenes = historial?.examenes || [];
  const referencias = historial?.referencias || [];

  return (
    <Modal isOpen={isOpen} onClose={onClose} title={`Historial de ${mascota?.nombre || ''}`} maxWidth="max-w-2xl">
      {loading ? (
        <TableSkeleton rows={3} columns={3} />
      ) : (
        <div className="space-y-6">
          <div>
            <h3 className="text-sm font-bold text-slate-900 flex items-center mb-3">
              <Syringe className="w-4 h-4 mr-2 text-emerald-600" /> Vacunas
            </h3>
            {vacunas.length === 0 ? (
              <p className="text-sm text-slate-400">Aún no tiene vacunas registradas.</p>
            ) : (
              <div className="space-y-2">
                {vacunas.map((v) => (
                  <div key={v.id} className="flex items-center justify-between p-3 rounded-xl bg-slate-50 border border-slate-200">
                    <div>
                      <p className="text-slate-900 font-semibold text-sm">{v.tipo}</p>
                      <p className="text-xs text-slate-400">Aplicada: {formatDate(v.fecha_aplicacion)}</p>
                    </div>
                    {v.proxima_dosis && (
                      <div className="text-right">
                        <p className="text-xs text-slate-400">Próxima dosis</p>
                        <p className="text-xs text-emerald-700 font-bold">{formatDate(v.proxima_dosis)}</p>
                      </div>
                    )}
                  </div>
                ))}
              </div>
            )}
          </div>

          <div>
            <h3 className="text-sm font-bold text-slate-900 flex items-center mb-3">
              <Stethoscope className="w-4 h-4 mr-2 text-emerald-600" /> Consultas
            </h3>
            {consultas.length === 0 ? (
              <p className="text-sm text-slate-400">Aún no tiene consultas registradas.</p>
            ) : (
              <div className="space-y-2">
                {consultas.map((c) => (
                  <div key={c.id} className="p-3 rounded-xl bg-slate-50 border border-slate-200">
                    <div className="flex items-center justify-between">
                      <p className="text-xs text-slate-400">{formatDateTime(c.fecha)}</p>
                      {c.peso != null && <span className="text-xs text-slate-400 font-mono">{c.peso} kg</span>}
                    </div>
                    <p className="text-slate-900 font-semibold text-sm mt-1">Diagnóstico: {c.diagnostico}</p>
                    {c.tratamiento && <p className="text-xs text-slate-400 mt-0.5">Tratamiento: {c.tratamiento}</p>}
                  </div>
                ))}
              </div>
            )}
          </div>

          <div>
            <h3 className="text-sm font-bold text-slate-900 flex items-center mb-3">
              <Pill className="w-4 h-4 mr-2 text-emerald-600" /> Recetas
            </h3>
            {recetas.length === 0 ? (
              <p className="text-sm text-slate-400">Aún no tiene recetas registradas.</p>
            ) : (
              <div className="space-y-2">
                {recetas.map((r) => (
                  <div key={r.id} className="p-3 rounded-xl bg-slate-50 border border-slate-200">
                    <div className="flex items-center justify-between">
                      <p className="text-slate-900 font-semibold text-sm">{r.nombre_medicamento}</p>
                      <p className="text-xs text-slate-400">{formatDate(r.fecha)}</p>
                    </div>
                    {(r.dosis || r.cantidad) && (
                      <p className="text-xs text-slate-500 mt-0.5">
                        {r.dosis && <span className="font-bold text-emerald-700">{r.dosis}</span>}
                        {r.cantidad && <span className="ml-2">{r.cantidad}</span>}
                      </p>
                    )}
                    {r.indicaciones && <p className="text-xs text-slate-400 mt-1">Indicaciones: {r.indicaciones}</p>}
                  </div>
                ))}
              </div>
            )}
          </div>

          <div>
            <h3 className="text-sm font-bold text-slate-900 flex items-center mb-3">
              <FlaskConical className="w-4 h-4 mr-2 text-emerald-600" /> Exámenes Auxiliares
            </h3>
            {examenes.length === 0 ? (
              <p className="text-sm text-slate-400">Aún no tiene exámenes registrados.</p>
            ) : (
              <div className="space-y-2">
                {examenes.map((e) => (
                  <div key={e.id} className="p-3 rounded-xl bg-slate-50 border border-slate-200">
                    <div className="flex items-center justify-between">
                      <p className="text-slate-900 font-semibold text-sm">{e.examen_nombre || `Examen #${e.examen_auxiliar_id}`}</p>
                      <span className={`text-[11px] font-bold px-2 py-0.5 rounded-full border ${
                        e.estado === 'REALIZADO' ? 'bg-emerald-50 text-emerald-700 border-emerald-200' : 'bg-amber-50 text-amber-700 border-amber-200'
                      }`}>{e.estado === 'REALIZADO' ? 'Realizado' : 'Pendiente'}</span>
                    </div>
                    {e.resultado && <p className="text-xs text-slate-500 mt-1"><span className="font-bold text-slate-600">Resultado:</span> {e.resultado}</p>}
                  </div>
                ))}
              </div>
            )}
          </div>

          <div>
            <h3 className="text-sm font-bold text-slate-900 flex items-center mb-3">
              <Send className="w-4 h-4 mr-2 text-emerald-600" /> Referencias
            </h3>
            {referencias.length === 0 ? (
              <p className="text-sm text-slate-400">Aún no tiene referencias.</p>
            ) : (
              <div className="space-y-2">
                {referencias.map((r) => (
                  <div key={r.id} className="p-3 rounded-xl bg-slate-50 border border-slate-200">
                    <p className="text-slate-900 font-semibold text-sm">{r.centro_destino}</p>
                    {r.especialidad && <p className="text-xs text-emerald-700 font-bold mt-0.5">{r.especialidad}</p>}
                    <p className="text-xs text-slate-400 mt-0.5">Motivo: {r.motivo}</p>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      )}
    </Modal>
  );
};

  const comprasColumns = [
    {
      header: 'Fecha',
      accessor: 'fecha',
      render: (item) => <span className="text-slate-900 font-semibold whitespace-nowrap">{formatDateTime(item.fecha)}</span>,
    },
    {
      header: 'Código de Recojo',
      accessor: 'codigo_recojo',
      render: (item) => <span className="font-mono font-bold text-emerald-700 text-sm">{item.codigo_recojo || '—'}</span>,
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
      ),
    },
    {
      header: 'Método',
      accessor: 'metodo_pago',
      render: (item) => <span className="text-slate-600">{item.metodo_pago}</span>,
    },
    {
      header: 'Total',
      accessor: 'total',
      render: (item) => <span className="text-slate-900 font-bold">${item.total.toFixed(2)}</span>,
    },
    {
      header: 'Estado',
      accessor: 'estado',
      render: (item) => <VentaBadge estado={item.estado} />,
    },
    {
      header: 'Acciones',
      accessor: 'acciones',
      render: (item) => (
        <div className="flex items-center space-x-2">
          <Button variant="secondary" size="sm" onClick={() => setVentaDetalle(item)}>
            Ver
          </Button>
          {(item.estado === 'Pendiente' || item.estado === 'Confirmada') && (
            <Button variant="danger" size="sm" onClick={() => setVentaAnulacion(item)}>
              Solicitar Anulación
            </Button>
          )}
          {item.estado === 'Anulación solicitada' && (
            <span className="text-[11px] font-bold text-orange-600">En revisión</span>
          )}
        </div>
      ),
    },
  ];
export default function PortalCliente() {
  const toast = useToast();
  const { username } = useAuthStore();
  const [cliente, setCliente] = useState(null);
  const [mascotas, setMascotas] = useState([]);
  const [recordatorios, setRecordatorios] = useState([]);
  const [citas, setCitas] = useState([]);
  const [compras, setCompras] = useState([]);
  const [ordenesMedicas, setOrdenesMedicas] = useState({ recetas: [], examenes: [], referencias: [] });
  const [ordenesTab, setOrdenesTab] = useState('recetas');
  const [loading, setLoading] = useState(true);

  const [showCitaModal, setShowCitaModal] = useState(false);
  const [citasFilter, setCitasFilter] = useState('todas');
  const [citaAReprogramar, setCitaAReprogramar] = useState(null);
  const [citaACancelar, setCitaACancelar] = useState(null);
  const [mascotaHistorial, setMascotaHistorial] = useState(null);
  const [historial, setHistorial] = useState({ vacunas: [], consultas: [] });
  const [loadingHistorial, setLoadingHistorial] = useState(false);
  const [ventaDetalle, setVentaDetalle] = useState(null);
  const [ventaAnulacion, setVentaAnulacion] = useState(null);
  const [loadingAnulacion, setLoadingAnulacion] = useState(false);

  // Payment flow state
  const [showPaymentModal, setShowPaymentModal] = useState(false);
  const [pendingCita, setPendingCita] = useState(null);
  const [currentPago, setCurrentPago] = useState(null);
  const [selectedMetodo, setSelectedMetodo] = useState(null);
  const [processingPago, setProcessingPago] = useState(false);

  const fetchAll = useCallback(async () => {
    try {
      const [meRes, mascotasRes, citasRes, recRes, comprasRes, ordenesRes] = await Promise.all([
        api.get('/api/clientes/me'),
        api.get('/api/mascotas'),
        api.get('/api/citas/mias'),
        api.get('/api/recordatorios'),
        api.get('/api/ventas/mias'),
        api.get('/api/ordenes-medicas/mias'),
      ]);
      setCliente(meRes.data);
      setMascotas(mascotasRes.data);
      setCitas(citasRes.data);
      setRecordatorios(recRes.data);
      setCompras(comprasRes.data);
      setOrdenesMedicas(ordenesRes.data || { recetas: [], examenes: [], referencias: [] });
    } catch (err) {
      console.error('Error fetching data', err);
      toast.error('Error al cargar los datos. Por favor intenta de nuevo.');
    } finally {
      setLoading(false);
    }
  }, [toast]);

  useEffect(() => {
    fetchAll();
  }, [fetchAll]);

  const handlePhotoUpload = async (mascotaId, file) => {
    if (!file) return;
    const reader = new FileReader();
    reader.onloadend = async () => {
      try {
        await api.put(`/api/mascotas/${mascotaId}/foto`, { foto_base64: reader.result });
        setMascotas((prev) => prev.map((m) => (m.id === mascotaId ? { ...m, foto_url: reader.result } : m)));
        toast.success('Foto actualizada correctamente');
      } catch (err) {
        console.error('Error al subir foto', err);
        toast.error('Error al subir la foto');
      }
    };
    reader.readAsDataURL(file);
  };

  const handleProfilePhotoUpload = async (file) => {
    if (!file) return;
    const reader = new FileReader();
    reader.onloadend = async () => {
      try {
        await api.put('/api/clientes/me/foto', { foto_base64: reader.result });
        setCliente((prev) => (prev ? { ...prev, foto_url: reader.result } : prev));
        toast.success('Foto de perfil actualizada correctamente');
      } catch (err) {
        console.error('Error al subir foto de perfil', err);
        toast.error('No se pudo actualizar la foto de perfil.');
      }
    };
    reader.readAsDataURL(file);
  };

  const handleConfirmCita = async (mascotaId, fechaHora, motivo, tipo, examenId) => {
    try {
      const payload = { mascota_id: mascotaId, fecha_hora: fechaHora, motivo };
      if (tipo) payload.tipo = tipo;
      if (examenId) payload.examen_auxiliar_id = examenId;
      
      const res = await api.post('/api/citas', payload);
      const citaCreada = res.data;
      
      // Check payment config
      const pagoConfigRes = await api.get('/api/configuracion/pago');
      const pagoConfig = pagoConfigRes.data;
      
      // Si el pago es requerido, crear pago y mostrar modal
      if (pagoConfig && pagoConfig.pago_requerido) {
        try {
          const metodo = (pagoConfig.metodos && pagoConfig.metodos[0]) || 'YAPE';
          const pagoRes = await api.post(`/api/citas/${citaCreada.id}/pagos`, { metodo });
          setCurrentPago(pagoRes.data);
          setPendingCita(citaCreada);
          setSelectedMetodo(metodo);
          setShowPaymentModal(true);
          setShowCitaModal(false);
        } catch (err) {
          console.error('Error al crear pago', err);
          toast.error('Cita creada, pero no se pudo iniciar el pago: ' + (err.response?.data?.detail || err.message));
          setShowCitaModal(false);
          fetchAll();
        }
      } else {
        // No requiere pago, la cita queda directamente en PROGRAMADA
        setShowCitaModal(false);
        toast.success('Cita solicitada correctamente. Te confirmaremos pronto.');
        fetchAll();
      }
    } catch (err) {
      console.error('Error al programar cita', err);
      toast.error(err.response?.data?.detail || 'No se pudo programar la cita.');
    }
  };

  const handleProcessPago = async (pagoId, success = true) => {
    setProcessingPago(true);
    try {
      const res = await api.post(`/api/pagos/${pagoId}/procesar`, success ? { success: true } : { success: false });
      toast.success('Pago procesado: ' + res.data.estado);
      setShowPaymentModal(false);
      setCurrentPago(null);
      setPendingCita(null);
      fetchAll();
      setTimeout(() => toast.success(null), 3000);
    } catch (err) {
      toast.error(err.response?.data?.detail || 'Error procesando pago');
      setTimeout(() => toast.error(null), 3000);
    } finally {
      setProcessingPago(false);
    }
  };

  const handleConfirmCancelar = async (citaId, motivo) => {
    try {
      await api.put(`/api/citas/${citaId}/cancelar`, { motivo_cancelacion: motivo });
      setCitaACancelar(null);
      toast.success('Cita cancelada correctamente.');
      fetchAll();
    } catch (err) {
      console.error('Error al cancelar cita', err);
      toast.error(err.response?.data?.detail || 'No se pudo cancelar la cita.');
    }
  };

  const handleConfirmReprogramar = async (citaId, fechaHora) => {
    try {
      await api.put(`/api/citas/${citaId}/reprogramar`, { fecha_hora: fechaHora });
      setCitaAReprogramar(null);
      toast.success('Cita reprogramada correctamente.');
      fetchAll();
    } catch (err) {
      console.error('Error al reprogramar cita', err);
      toast.error(err.response?.data?.detail || 'No se pudo reprogramar la cita.');
    }
  };

  const handleViewHistorial = async (mascota) => {
    setMascotaHistorial(mascota);
    setHistorial({ vacunas: [], consultas: [] });
    setLoadingHistorial(true);
    try {
      const res = await api.get(`/api/mascotas/${mascota.id}/historial`);
      setHistorial(res.data);
    } catch (err) {
      console.error('Error al cargar historial', err);
      toast.error('No se pudo cargar el historial.');
    } finally {
      setLoadingHistorial(false);
    }
  };

  const handleSolicitarAnulacion = async () => {
    if (!ventaAnulacion) return;
    setLoadingAnulacion(true);
    try {
      await api.post(`/api/ventas/${ventaAnulacion.id}/solicitar-anulacion`);
      setVentaAnulacion(null);
      toast.success('Solicitud de anulación enviada. Revisaremos tu pedido.');
      fetchAll();
    } catch (err) {
      console.error('Error al solicitar anulación', err);
      toast.error(err.response?.data?.detail || 'No se pudo solicitar la anulación.');
    } finally {
      setLoadingAnulacion(false);
    }
  };

  const citasPendientes = citas.filter((c) => c.estado === 'Programada');

  const citasCount = {
    todas: citas.length,
    Programada: citasPendientes.length,
    Completada: citas.filter((c) => c.estado === 'Completada').length,
    Cancelada: citas.filter((c) => c.estado === 'Cancelada').length,
  };
  const citasFiltradas = citasFilter === 'todas' ? citas : citas.filter((c) => c.estado === citasFilter);

  const dosisPorMascota = recordatorios.reduce((acc, r) => {
    if (!acc[r.mascota_id] || new Date(r.proxima_dosis) < new Date(acc[r.mascota_id])) {
      acc[r.mascota_id] = r.proxima_dosis;
    }
    return acc;
  }, {});

  const citaFilterOptions = [
    { key: 'todas', label: 'Todas', count: citasCount.todas, empty: 'Aún no tienes citas. Agenda tu primera atención.' },
    { key: 'Programada', label: 'Próximas', count: citasCount.Programada, empty: 'No tienes citas próximas programadas.' },
    { key: 'Completada', label: 'Completadas', count: citasCount.Completada, empty: 'Aún no hay citas completadas.' },
    { key: 'Cancelada', label: 'Canceladas', count: citasCount.Cancelada, empty: 'No tienes citas canceladas.' },
  ];
  const activeFilter = citaFilterOptions.find((f) => f.key === citasFilter) || citaFilterOptions[0];

  const citasColumns = [
    {
      header: 'Fecha y Hora',
      accessor: 'fecha_hora',
      render: (item) => <span className="text-slate-900 font-semibold whitespace-nowrap">{formatDateTime(item.fecha_hora)}</span>,
    },
    {
      header: 'Mascota',
      accessor: 'mascota_nombre',
      render: (item) => <span className="text-emerald-700 font-bold">{item.mascota_nombre}</span>,
    },
    { header: 'Motivo', accessor: 'motivo', render: (item) => (
        <div>
          <span className="text-slate-600">{item.motivo}</span>
          {item.tipo && <span className="block text-[11px] text-emerald-700 font-semibold mt-0.5">{item.tipo}</span>}
        </div>
      ) },
    {
      header: 'Estado',
      accessor: 'estado',
      render: (item) => <StatusBadge status={citaBadgeStatus(item.estado)} size="sm" />,
    },
    {
      header: 'Acciones',
      accessor: 'acciones',
      render: (item) =>
        item.estado === 'Programada' ? (
          <div className="flex items-center space-x-2">
            <Button variant="secondary" size="sm" onClick={() => setCitaAReprogramar(item)}>
              Reprogramar
            </Button>
            <Button variant="danger" size="sm" onClick={() => setCitaACancelar(item)}>
              Cancelar
            </Button>
          </div>
        ) : (
          <span className="text-slate-500 text-xs">—</span>
        ),
    },
  ];

  return (
    <div className="min-h-screen bg-slate-50 font-sans text-slate-600 selection:bg-[#047857] selection:text-white">
      
      {/* Header unificado (fijo claro) */}
      <SiteHeader variant="solid" showContactBar />

      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 pt-32 sm:pt-36 lg:pt-44 pb-8 space-y-8 animate-fade-in-up">
        {/* Hero: saludo profesional con foto de perfil */}
        <section className="relative overflow-hidden rounded-3xl bg-white border border-slate-200/70 shadow-[0_24px_60px_-28px_rgba(15,23,42,0.25)] p-6 sm:p-8 lg:p-10">
          <div className="absolute -top-28 -right-20 w-80 h-80 rounded-full bg-gradient-to-br from-emerald-100/70 to-teal-100/50 blur-3xl pointer-events-none" />
          <div className="absolute -bottom-32 -left-24 w-80 h-80 rounded-full bg-gradient-to-br from-teal-100/60 to-emerald-100/40 blur-3xl pointer-events-none" />
          <PawPrint className="absolute -right-10 top-1/2 -translate-y-1/2 w-44 h-44 text-emerald-600/[0.04] rotate-12 pointer-events-none" />

          <div className="relative z-10 flex flex-col md:flex-row md:items-center gap-6">
            <div className="relative shrink-0">
              <div className="w-20 h-20 sm:w-24 sm:h-24 rounded-full ring-4 ring-emerald-100 overflow-hidden bg-gradient-to-br from-emerald-50 to-teal-50 flex items-center justify-center shadow-[0_14px_34px_-12px_rgba(4,120,87,0.5)]">
                {cliente?.foto_url ? (
                  <img src={cliente.foto_url} alt="Foto de perfil" className="w-full h-full object-cover" />
                ) : (
                  <PawPrint className="w-10 h-10 text-emerald-600" />
                )}
              </div>
              <label
                className="absolute bottom-0 right-0 bg-emerald-700 text-white rounded-full p-2 cursor-pointer shadow-lg hover:bg-emerald-600 transition-colors"
                title="Cambiar foto de perfil"
                onClick={() => {
                  const input = document.createElement('input');
                  input.type = 'file';
                  input.accept = 'image/*';
                  input.onchange = (ev) => handleProfilePhotoUpload(ev.target.files[0]);
                  input.click();
                }}
              >
                <Camera className="w-4 h-4" />
              </label>
            </div>

            <div className="flex-1">
              <p className="text-emerald-600 text-xs font-bold uppercase tracking-[0.2em] mb-1">Panel del paciente</p>
              <h1 className="[font-family:Changa] text-3xl sm:text-4xl font-bold tracking-tight text-slate-900">
                Hola, {cliente?.nombre?.split(' ')[0] || username || 'visitante'}
              </h1>
              <p className="text-slate-500 mt-2 max-w-2xl text-sm sm:text-base">
                Bienvenido a tu espacio personal. Agenda, reprograma o cancela tus citas y consulta el historial clínico y la vacunación de tus mascotas.
              </p>
            </div>
          </div>

          <div className="relative z-10 mt-8 grid grid-cols-1 sm:grid-cols-3 gap-4 pt-6 border-t border-slate-100">
            <div className="flex items-center gap-4 rounded-2xl bg-white border border-emerald-100 shadow-[0_12px_28px_-16px_rgba(4,120,87,0.45)] p-4">
              <span className="p-3 rounded-xl bg-gradient-to-b from-emerald-500 to-emerald-700 text-white shadow-[inset_0_1px_0_rgba(255,255,255,0.25),0_8px_16px_-6px_rgba(4,120,87,0.6)] shrink-0">
                <PawPrint className="w-5 h-5" />
              </span>
              <div>
                <p className="text-[1.75rem] leading-none font-black text-slate-900 tabular-nums">{mascotas.length}</p>
                <p className="text-emerald-700 text-[11px] font-bold uppercase tracking-[0.14em] mt-1.5">Mascotas Registradas</p>
                <p className="text-slate-400 text-xs mt-0.5">con historial y citas al día</p>
              </div>
            </div>
            <div className="flex items-center gap-4 rounded-2xl bg-white border border-emerald-100 shadow-[0_12px_28px_-16px_rgba(4,120,87,0.45)] p-4">
              <span className="p-3 rounded-xl bg-gradient-to-b from-emerald-500 to-emerald-700 text-white shadow-[inset_0_1px_0_rgba(255,255,255,0.25),0_8px_16px_-6px_rgba(4,120,87,0.6)] shrink-0">
                <Calendar className="w-5 h-5" />
              </span>
              <div>
                <p className="text-[1.75rem] leading-none font-black text-slate-900 tabular-nums">{citasPendientes.length}</p>
                <p className="text-emerald-700 text-[11px] font-bold uppercase tracking-[0.14em] mt-1.5">Citas Pendientes</p>
                <p className="text-slate-400 text-xs mt-0.5">esperando confirmación</p>
              </div>
            </div>
            <div className="flex items-center gap-4 rounded-2xl bg-white border border-emerald-100 shadow-[0_12px_28px_-16px_rgba(4,120,87,0.45)] p-4">
              <span className="p-3 rounded-xl bg-gradient-to-b from-emerald-500 to-emerald-700 text-white shadow-[inset_0_1px_0_rgba(255,255,255,0.25),0_8px_16px_-6px_rgba(4,120,87,0.6)] shrink-0">
                <Syringe className="w-5 h-5" />
              </span>
              <div>
                <p className="text-[1.75rem] leading-none font-black text-slate-900 tabular-nums">{recordatorios.length}</p>
                <p className="text-emerald-700 text-[11px] font-bold uppercase tracking-[0.14em] mt-1.5">Vacunas por Vencer</p>
                <p className="text-slate-400 text-xs mt-0.5">en los próximos 15 días</p>
              </div>
            </div>
          </div>
        </section>

      {loading ? (
        <TableSkeleton rows={5} columns={4} />
      ) : (
        <>

          <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
            {/* Main content */}
            <div className="lg:col-span-2 space-y-8">
              {/* Mis Citas */}
              <section id="mis-citas" className="scroll-mt-32 space-y-4">
                <div className="flex items-center justify-between gap-3 flex-wrap">
                  <h3 className="text-2xl font-black text-slate-900 tracking-tight flex items-center gap-3">
                    <span className="p-2.5 rounded-xl bg-emerald-50 border border-emerald-100 text-emerald-700">
                      <Calendar className="w-5 h-5" />
                    </span>
                    Mis Citas
                  </h3>
                  <Button
                    variant="primary"
                    size="lg"
                    onClick={() => setShowCitaModal(true)}
                    className="px-8"
                  >
                    <CalendarPlus className="w-5 h-5 mr-2" /> Agendar Cita
                  </Button>
                </div>

                <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
                  {[
                    { key: 'Programada', label: 'Citas programadas', sub: 'próximas atenciones', icon: Calendar, count: citasCount.Programada, tone: 'emerald' },
                    { key: 'Completada', label: 'Atenciones realizadas', sub: 'consultas atendidas', icon: Stethoscope, count: citasCount.Completada, tone: 'indigo' },
                    { key: 'Cancelada', label: 'Canceladas', sub: 'no se llevaron a cabo', icon: AlertTriangle, count: citasCount.Cancelada, tone: 'rose' },
                  ].map((c) => {
                    const Icon = c.icon;
                    const tones = {
                      emerald: 'from-emerald-500 to-emerald-700 border-emerald-700',
                      indigo: 'from-indigo-500 to-indigo-700 border-indigo-700',
                      rose: 'from-rose-500 to-rose-700 border-rose-700',
                    };
                    return (
                      <button
                        key={c.key}
                        type="button"
                        onClick={() => setCitasFilter(c.key)}
                        className={`flex items-center gap-4 rounded-2xl bg-white border border-slate-100 p-4 text-left hover:-translate-y-0.5 hover:shadow-[0_12px_28px_-16px_rgba(4,120,87,0.35)] transition-all duration-200 ${
                          citasFilter === c.key ? 'ring-2 ring-emerald-500/50 border-emerald-300' : ''
                        }`}
                      >
                        <span className={`p-3 rounded-xl bg-gradient-to-b text-white shadow-[inset_0_1px_0_rgba(255,255,255,0.25),0_8px_16px_-6px_rgba(4,120,87,0.5)] shrink-0 ${tones[c.tone]}`}>
                          <Icon className="w-5 h-5" />
                        </span>
                        <div>
                          <p className="text-2xl leading-none font-black text-slate-900 tabular-nums">{c.count}</p>
                          <p className="text-slate-700 text-xs font-bold mt-1.5">{c.label}</p>
                          <p className="text-slate-400 text-[11px] mt-0.5">{c.sub}</p>
                        </div>
                      </button>
                    );
                  })}
                </div>

                <div className="flex items-center gap-2 flex-wrap">
                  {citaFilterOptions.map((f) => (
                    <button
                      key={f.key}
                      type="button"
                      onClick={() => setCitasFilter(f.key)}
                      className={`inline-flex items-center gap-1.5 text-xs font-bold rounded-full px-3.5 py-1.5 border transition-all duration-200 ${
                        citasFilter === f.key
                          ? 'bg-gradient-to-b from-emerald-600 to-emerald-700 text-white border-emerald-700 shadow-[inset_0_1px_0_rgba(255,255,255,0.2),0_2px_6px_-1px_rgba(4,120,87,0.4)]'
                          : 'bg-white text-slate-500 border-slate-200 hover:border-emerald-300 hover:text-emerald-800'
                      }`}
                    >
                      {f.label}
                      <span className={`px-1.5 py-0.5 rounded-full text-[10px] font-black ${citasFilter === f.key ? 'bg-white/20 text-white' : 'bg-slate-100 text-slate-500'}`}>
                        {f.count}
                      </span>
                    </button>
                  ))}
                </div>

                <DataTable
                  columns={citasColumns}
                  data={citasFiltradas}
                  emptyMessage={activeFilter.empty}
                />
              </section>

              {/* Mis Compras */}
              <section id="mis-compras" className="scroll-mt-32 space-y-4">
                <div className="flex items-center justify-between gap-3 flex-wrap">
                  <div>
                    <h3 className="text-2xl font-black text-slate-900 tracking-tight flex items-center gap-3">
                      <span className="p-2.5 rounded-xl bg-emerald-50 border border-emerald-100 text-emerald-700">
                        <ShoppingBag className="w-5 h-5" />
                      </span>
                      Mis Compras
                    </h3>
                    <p className="text-sm text-slate-400 mt-1 ml-12 font-medium">
                      Tus pedidos de la tienda y su código de recojo.
                    </p>
                  </div>
                </div>

                {compras.length === 0 ? (
                  <Card className="text-center py-10 bg-white border-slate-100">
                    <div className="mx-auto mb-3 w-14 h-14 rounded-2xl bg-gradient-to-br from-emerald-50 to-emerald-100 border border-emerald-100 flex items-center justify-center">
                      <ShoppingBag className="w-7 h-7 text-emerald-600" />
                    </div>
                    <p className="text-slate-500">
                      Aún no has realizado compras. Visita nuestra{' '}
                      <Link to="/tienda" className="text-emerald-700 font-bold hover:underline">tienda online</Link>.
                    </p>
                  </Card>
                ) : (
                  <DataTable columns={comprasColumns} data={compras} emptyMessage="Aún no tienes compras." />
                )}
              </section>

              {/* Mis Órdenes Médicas */}
              <section id="mis-ordenes" className="scroll-mt-32 space-y-4">
                <div className="flex items-end justify-between gap-3 flex-wrap">
                  <div>
                    <h3 className="text-2xl font-black text-slate-900 tracking-tight flex items-center gap-3">
                      <span className="p-2.5 rounded-xl bg-emerald-50 border border-emerald-100 text-emerald-700">
                        <FileText className="w-5 h-5" />
                      </span>
                      Mis Órdenes Médicas
                    </h3>
                    <p className="text-sm text-slate-400 mt-1 ml-12 font-medium">
                      Recetas, exámenes auxiliares y referencias de tus mascotas.
                    </p>
                  </div>
                </div>

                <div className="flex items-center gap-2 flex-wrap">
                  {[
                    { key: 'recetas', label: 'Recetas', icon: Pill, count: ordenesMedicas.recetas.length },
                    { key: 'examenes', label: 'Exámenes Auxiliares', icon: FlaskConical, count: ordenesMedicas.examenes.length },
                    { key: 'referencias', label: 'Referencias', icon: Send, count: ordenesMedicas.referencias.length },
                  ].map((t) => {
                    const Icon = t.icon;
                    return (
                      <button
                        key={t.key}
                        type="button"
                        onClick={() => setOrdenesTab(t.key)}
                        className={`inline-flex items-center gap-1.5 text-xs font-bold rounded-full px-3.5 py-1.5 border transition-all duration-200 ${
                          ordenesTab === t.key
                            ? 'bg-gradient-to-b from-emerald-600 to-emerald-700 text-white border-emerald-700 shadow-[inset_0_1px_0_rgba(255,255,255,0.2),0_2px_6px_-1px_rgba(4,120,87,0.4)]'
                            : 'bg-white text-slate-500 border-slate-200 hover:border-emerald-300 hover:text-emerald-800'
                        }`}
                      >
                        <Icon className="w-3.5 h-3.5" />
                        {t.label}
                        <span className={`px-1.5 py-0.5 rounded-full text-[10px] font-black ${ordenesTab === t.key ? 'bg-white/20 text-white' : 'bg-slate-100 text-slate-500'}`}>
                          {t.count}
                        </span>
                      </button>
                    );
                  })}
                </div>

                <div className="space-y-2.5">
                  {ordenesTab === 'recetas' && (
                    ordenesMedicas.recetas.length === 0 ? (
                      <Card className="text-center py-8 bg-white border-slate-100">
                        <div className="mx-auto mb-3 w-12 h-12 rounded-2xl bg-emerald-50 border border-emerald-100 flex items-center justify-center">
                          <Pill className="w-6 h-6 text-emerald-600" />
                        </div>
                        <p className="text-slate-500">Aún no tienes recetas registradas.</p>
                      </Card>
                    ) : (
                      ordenesMedicas.recetas.map((r) => (
                        <Card key={r.id} variant="secondary" className="p-4 bg-white border-slate-100">
                          <div className="flex flex-wrap items-start justify-between gap-2">
                            <div>
                              <p className="text-sm font-bold text-slate-900">{r.nombre_medicamento}</p>
                              <p className="text-xs text-slate-400 mt-0.5">
                                {r.mascota_nombre && <span className="text-emerald-700 font-semibold">{r.mascota_nombre}</span>}
                                {r.fecha ? ` · ${formatDate(r.fecha)}` : ''}
                              </p>
                            </div>
                            {r.dosis && <span className="text-xs font-bold text-emerald-700 bg-emerald-50 border border-emerald-100 px-2 py-1 rounded-lg">{r.dosis}</span>}
                          </div>
                          {(r.cantidad || r.indicaciones) && (
                            <div className="mt-3 pt-3 border-t border-slate-100 text-xs text-slate-500 space-y-1">
                              {r.cantidad && <p><span className="font-bold text-slate-600">Cantidad:</span> {r.cantidad}</p>}
                              {r.indicaciones && <p><span className="font-bold text-slate-600">Indicaciones:</span> {r.indicaciones}</p>}
                            </div>
                          )}
                        </Card>
                      ))
                    )
                  )}

                  {ordenesTab === 'examenes' && (
                    ordenesMedicas.examenes.length === 0 ? (
                      <Card className="text-center py-8 bg-white border-slate-100">
                        <div className="mx-auto mb-3 w-12 h-12 rounded-2xl bg-indigo-50 border border-indigo-100 flex items-center justify-center">
                          <FlaskConical className="w-6 h-6 text-indigo-600" />
                        </div>
                        <p className="text-slate-500">Aún no tienes exámenes auxiliares.</p>
                      </Card>
                    ) : (
                      ordenesMedicas.examenes.map((e) => {
                        const badge = {
                          PENDIENTE: { label: 'Pendiente', cls: 'bg-amber-50 text-amber-700 border-amber-200' },
                          REALIZADO: { label: 'Realizado', cls: 'bg-emerald-50 text-emerald-700 border-emerald-200' },
                          ANULADA: { label: 'Anulado', cls: 'bg-rose-50 text-rose-600 border-rose-200' },
                        }[e.estado] || { label: e.estado, cls: 'bg-slate-50 text-slate-600 border-slate-200' };
                        return (
                          <Card key={e.id} variant="secondary" className="p-4 bg-white border-slate-100">
                            <div className="flex flex-wrap items-start justify-between gap-2">
                              <div>
                                <p className="text-sm font-bold text-slate-900">{e.examen_nombre || `Examen #${e.examen_auxiliar_id}`}</p>
                                <p className="text-xs text-slate-400 mt-0.5">
                                  {e.mascota_nombre && <span className="text-emerald-700 font-semibold">{e.mascota_nombre}</span>}
                                  {e.fecha ? ` · ${formatDate(e.fecha)}` : ''}
                                </p>
                              </div>
                              <span className={`inline-block px-2 py-0.5 rounded-full text-[0.75rem] font-bold border ${badge.cls}`}>{badge.label}</span>
                            </div>
                            {e.resultado && (
                              <div className="mt-3 pt-3 border-t border-slate-100 text-xs text-slate-500">
                                <p><span className="font-bold text-slate-600">Resultado:</span> {e.resultado}</p>
                              </div>
                            )}
                            {e.observaciones && (
                              <p className="text-xs text-slate-400 mt-1"><span className="font-bold text-slate-500">Obs.:</span> {e.observaciones}</p>
                            )}
                          </Card>
                        );
                      })
                    )
                  )}

                  {ordenesTab === 'referencias' && (
                    ordenesMedicas.referencias.length === 0 ? (
                      <Card className="text-center py-8 bg-white border-slate-100">
                        <div className="mx-auto mb-3 w-12 h-12 rounded-2xl bg-teal-50 border border-teal-100 flex items-center justify-center">
                          <Send className="w-6 h-6 text-teal-600" />
                        </div>
                        <p className="text-slate-500">Aún no tienes referencias médicas.</p>
                      </Card>
                    ) : (
                      ordenesMedicas.referencias.map((ref) => (
                        <Card key={ref.id} variant="secondary" className="p-4 bg-white border-slate-100">
                          <div className="flex flex-wrap items-start justify-between gap-2">
                            <div>
                              <p className="text-sm font-bold text-slate-900">{ref.centro_destino}</p>
                              <p className="text-xs text-slate-400 mt-0.5">
                                {ref.mascota_nombre && <span className="text-emerald-700 font-semibold">{ref.mascota_nombre}</span>}
                                {ref.fecha ? ` · ${formatDate(ref.fecha)}` : ''}
                              </p>
                            </div>
                            {ref.especialidad && (
                              <span className="text-xs font-bold text-teal-700 bg-teal-50 border border-teal-100 px-2 py-1 rounded-lg">{ref.especialidad}</span>
                            )}
                          </div>
                          <p className="text-xs text-slate-500 mt-2"><span className="font-bold text-slate-600">Motivo:</span> {ref.motivo}</p>
                        </Card>
                      ))
                    )
                  )}
                </div>
              </section>

              {/* Mis Mascotas */}
              <section>
                <div className="flex items-end justify-between gap-3 flex-wrap mb-4">
                  <div>
                    <h3 className="text-lg font-bold text-slate-900 tracking-tight flex items-center gap-2.5">
                      <span className="p-2 rounded-xl bg-emerald-50 border border-emerald-100 text-emerald-700">
                        <PawPrint className="w-4 h-4" />
                      </span>
                      Mis Mascotas
                    </h3>
                    <p className="text-sm text-slate-400 mt-1 ml-12 font-medium">
                      Historial clínico, vacunas y citas de cada compañero.
                    </p>
                  </div>
                </div>
                {mascotas.length === 0 ? (
                  <Card className="text-center py-10 bg-white border-slate-100">
                    <div className="mx-auto mb-3 w-14 h-14 rounded-2xl bg-gradient-to-br from-emerald-50 to-emerald-100 border border-emerald-100 flex items-center justify-center">
                      <PawPrint className="w-7 h-7 text-emerald-600" />
                    </div>
                    <p className="text-slate-500">
                      Aún no tienes mascotas registradas. Acércate a recepción para registrar a tu compañero.
                    </p>
                  </Card>
                ) : (
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                    {mascotas.map((mascota) => (
                      <PetCard
                        key={mascota.id}
                        mascota={mascota}
                        proximaDosis={dosisPorMascota[mascota.id]}
                        onSchedule={() => setShowCitaModal(true)}
                        onViewHistorial={handleViewHistorial}
                        onUploadPhoto={handlePhotoUpload}
                      />
                    ))}
                  </div>
                )}
              </section>
            </div>

            {/* Sidebar: Alertas de vacunación */}
            <aside className="space-y-6">
              <div>
                <h3 className="text-lg font-bold text-slate-900 tracking-tight flex items-center gap-2.5 mb-4">
                  <span className="p-2 rounded-xl bg-orange-50 border border-orange-100 text-orange-600">
                    <Bell className="w-4 h-4" />
                  </span>
                  Alertas de Vacunación
                </h3>
                <p className="text-sm text-slate-400 font-medium -mt-2 mb-4 ml-12">
                  Próximas dosis a vencer en los próximos 15 días.
                </p>
              </div>
              {recordatorios.length === 0 ? (
                <Card className="text-center py-8 bg-white border-slate-100">
                  <div className="mx-auto mb-3 w-12 h-12 rounded-2xl bg-emerald-50 flex items-center justify-center">
                    <CheckCircle2 className="w-6 h-6 text-emerald-600" />
                  </div>
                  <p className="text-slate-500">No tienes vacunas próximas a vencer.</p>
                </Card>
              ) : (
                <div className="space-y-4">
                  {recordatorios.map((r) => (
                    <ReminderItem key={r.vacuna_id} recordatorio={r} />
                  ))}
                </div>
              )}

              <div className="p-5 rounded-2xl bg-emerald-50 border border-emerald-100">
                <div className="flex items-start space-x-3">
                  <span className="p-2 rounded-xl bg-white border border-emerald-200 shrink-0">
                    <Phone className="w-4 h-4 text-emerald-700" />
                  </span>
                  <div>
                    <p className="text-sm font-semibold text-slate-900">¿Necesitas ayuda?</p>
                    <p className="text-xs text-slate-500 mt-0.5">
                      Nuestro equipo está disponible para asistirte con el cuidado de tu mascota. Llámanos o acércate a recepción para agendar una cita.
                    </p>
                  </div>
                </div>
              </div>
            </aside>
          </div>
        </>
      )}

      <AgendarCitaModal
        isOpen={showCitaModal}
        onClose={() => setShowCitaModal(false)}
        mascotas={mascotas}
        onConfirm={handleConfirmCita}
      />

      <AgendarCitaModal
        isOpen={!!citaAReprogramar}
        onClose={() => setCitaAReprogramar(null)}
        mascotas={mascotas}
        citaReprogramar={citaAReprogramar}
        onConfirm={(mascotaId, fechaHora) => handleConfirmReprogramar(citaAReprogramar.id, fechaHora)}
      />

      <CancelarCitaModal
        isOpen={!!citaACancelar}
        onClose={() => setCitaACancelar(null)}
        cita={citaACancelar}
        onConfirm={handleConfirmCancelar}
      />

      <HistorialModal
        isOpen={!!mascotaHistorial}
        onClose={() => setMascotaHistorial(null)}
        mascota={mascotaHistorial}
        historial={historial}
        loading={loadingHistorial}
      />

      <Modal
        isOpen={!!ventaDetalle}
        onClose={() => setVentaDetalle(null)}
        title={`Detalle de compra ${ventaDetalle?.codigo_recojo || ''}`}
        maxWidth="max-w-xl"
      >
        {ventaDetalle && (
          <div className="space-y-5">
            <div className="grid grid-cols-2 gap-3 text-sm">
              <div>
                <p className="text-xs font-bold text-slate-400 uppercase tracking-widest mb-1">Fecha</p>
                <p className="font-semibold text-slate-900">{formatDateTime(ventaDetalle.fecha)}</p>
              </div>
              <div>
                <p className="text-xs font-bold text-slate-400 uppercase tracking-widest mb-1">Estado</p>
                <VentaBadge estado={ventaDetalle.estado} />
              </div>
              <div>
                <p className="text-xs font-bold text-slate-400 uppercase tracking-widest mb-1">Comprobante</p>
                <p className="font-semibold text-slate-900">{ventaDetalle.comprobante || ventaDetalle.tipo_comprobante}</p>
              </div>
              <div>
                <p className="text-xs font-bold text-slate-400 uppercase tracking-widest mb-1">Método de pago</p>
                <p className="font-semibold text-slate-900">{ventaDetalle.metodo_pago}</p>
              </div>
            </div>

            <div className="border-t border-slate-100 pt-4">
              <h4 className="text-sm font-bold text-slate-900 mb-3">Productos</h4>
              <div className="space-y-2">
                {(ventaDetalle.detalles || []).map((d) => (
                  <div key={d.id} className="flex justify-between items-center p-3 rounded-xl bg-slate-50 border border-slate-200">
                    <div>
                      <p className="font-semibold text-slate-900 text-sm">{d.producto_nombre || `Producto #${d.producto_id}`}</p>
                      <p className="text-xs text-slate-400">{d.cantidad} × ${d.precio_unitario.toFixed(2)}</p>
                    </div>
                    <span className="font-bold text-slate-900 text-sm">${d.subtotal.toFixed(2)}</span>
                  </div>
                ))}
              </div>
              <div className="flex justify-between items-center mt-4 pt-3 border-t border-slate-100">
                <span className="font-bold text-slate-900">Total</span>
                <span className="text-xl font-black text-emerald-700">${ventaDetalle.total.toFixed(2)}</span>
              </div>
            </div>
          </div>
        )}
      </Modal>

      <Modal
        isOpen={!!ventaAnulacion}
        onClose={() => setVentaAnulacion(null)}
        title="Solicitar Anulación"
        maxWidth="max-w-md"
      >
        {ventaAnulacion && (
          <div className="space-y-5">
            <div className="flex items-start space-x-3 p-4 rounded-2xl bg-orange-50 border border-orange-200">
              <AlertTriangle className="w-5 h-5 text-orange-600 mt-0.5 shrink-0" />
              <div>
                <p className="text-slate-900 font-semibold text-sm">
                  Compra {ventaAnulacion.codigo_recojo} · ${ventaAnulacion.total.toFixed(2)}
                </p>
                <p className="text-xs text-slate-500 mt-1">
                  Tu solicitud quedará en revisión. Si la aprobamos, se cancelará la venta y se devolverá el stock.
                </p>
              </div>
            </div>
            <div className="flex items-center justify-end space-x-2">
              <Button variant="secondary" onClick={() => setVentaAnulacion(null)}>Cerrar</Button>
              <Button variant="danger" onClick={handleSolicitarAnulacion} isLoading={loadingAnulacion}>
                Confirmar Solicitud
              </Button>
            </div>
          </div>
        )}
      </Modal>
      </div>
    </div>
  );
}
