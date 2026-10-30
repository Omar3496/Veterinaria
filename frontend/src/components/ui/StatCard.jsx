import React from 'react'
import Card from './Card'

const tones = {
  emerald: { box: 'bg-emerald-50 border-emerald-100', icon: 'text-emerald-600' },
  amber: { box: 'bg-amber-50 border-amber-100', icon: 'text-amber-600' },
  orange: { box: 'bg-orange-50 border-orange-100', icon: 'text-orange-600' },
  rose: { box: 'bg-rose-50 border-rose-100', icon: 'text-rose-600' },
  sky: { box: 'bg-sky-50 border-sky-100', icon: 'text-sky-600' },
}

export default function StatCard({ title, value, icon: Icon, trend, tone = 'emerald', footer }) {
  const t = tones[tone] || tones.emerald
  return (
    <Card variant="spotlight" className="p-5">
      <div className="flex items-center justify-between mb-4">
        <span className="text-[11px] font-extrabold uppercase tracking-[0.14em] text-slate-500">{title}</span>
        {Icon && (
          <span className={`p-2.5 rounded-xl border ${t.box}`}>
            <Icon className={`w-5 h-5 ${t.icon}`} />
          </span>
        )}
      </div>
      <h3 className="text-[2rem] leading-none font-black text-slate-900 tracking-tight tabular-nums">{value}</h3>
      {footer && <p className="text-xs text-slate-400 mt-2">{footer}</p>}
      {trend && (
        <p className={`text-xs font-bold mt-2 inline-flex items-center gap-1 rounded-full px-2 py-0.5 ${trend > 0 ? 'bg-emerald-50 text-emerald-700' : 'bg-rose-50 text-rose-600'}`}>
          {trend > 0 ? '↑' : '↓'} {trend > 0 ? '+' : ''}{trend}%
        </p>
      )}
    </Card>
  )
}
