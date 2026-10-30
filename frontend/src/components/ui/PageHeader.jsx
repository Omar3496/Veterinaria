import React from 'react'

export default function PageHeader({ title, description, icon, actions }) {
  return (
    <div className="mb-8">
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 mb-4">
        <div className="flex items-center space-x-4">
          {icon && <div className="w-10 h-10 bg-emerald-50 border border-emerald-100 rounded-xl flex items-center justify-center text-emerald-700">{icon}</div>}
          <div>
            <h1 className="mb-1 text-3xl font-bold text-slate-900 tracking-tight">
              {title}
            </h1>
            {description && <p className="text-slate-500 max-w-2xl">{description}</p>}
          </div>
        </div>
        {actions && (
          <div className="flex items-center gap-2 shrink-0">
            {actions}
          </div>
        )}
      </div>
      <div className="h-px bg-gradient-to-r from-transparent via-slate-200 to-transparent"></div>
    </div>
  )
}
