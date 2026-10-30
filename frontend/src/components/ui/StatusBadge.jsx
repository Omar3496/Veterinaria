import React from 'react'

export default function StatusBadge({ status, size = 'default' }) {
  const variants = {
    activo: 'bg-emerald-50 text-emerald-700 border border-emerald-200',
    pendiente: 'bg-amber-50 text-amber-700 border border-amber-200',
    vencido: 'bg-rose-50 text-rose-700 border border-rose-200',
    cancelado: 'bg-slate-100 text-slate-600 border border-slate-200',
    completado: 'bg-emerald-50 text-emerald-700 border border-emerald-200',
  }

  const sizeClasses = {
    default: 'px-3 py-1 rounded-full text-xs font-bold',
    sm: 'px-2 py-0.5 rounded-full text-[0.75rem] font-bold',
  }

  return (
    <span className={`inline-block ${sizeClasses[size]} ${variants[status] || variants.pendiente}`}>
      {status.charAt(0).toUpperCase() + status.slice(1)}
    </span>
  )
}
