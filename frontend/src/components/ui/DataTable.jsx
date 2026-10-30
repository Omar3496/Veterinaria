import React from 'react'
import { Inbox } from 'lucide-react'

export default function DataTable({ 
  columns, 
  data, 
  keyExtractor = (item) => item.id,
  onRowClick,
  emptyMessage = "No hay datos disponibles",
  emptyAction
}) {
  return (
    <div className="w-full overflow-hidden rounded-2xl border border-slate-100 bg-white shadow-sm">
      <div className="overflow-x-auto">
        <table className="w-full text-left border-collapse">
          <thead>
            <tr className="border-b border-slate-100 bg-slate-50">
              {columns.map((col, i) => (
                <th key={i} className="px-6 py-4 text-xs font-bold text-slate-500 uppercase tracking-widest whitespace-nowrap">
                  {col.header}
                </th>
              ))}
            </tr>
          </thead>
          <tbody>
            {data.length === 0 ? (
              <tr>
                <td colSpan={columns.length} className="px-6 py-12 text-center text-slate-400">
                  <div className="flex flex-col items-center justify-center">
                    <Inbox className="w-10 h-10 mb-3 text-slate-200" />
                    <p>{emptyMessage}</p>
                    {emptyAction && <div className="mt-4">{emptyAction}</div>}
                  </div>
                </td>
              </tr>
            ) : (
              data.map((item, rowIndex) => (
                <tr
                  key={keyExtractor(item)}
                  onClick={() => onRowClick && onRowClick(item)}
                  className={`
                    border-b border-slate-100 last:border-0 
                    ${onRowClick ? 'cursor-pointer hover:bg-emerald-50/40 transition-colors' : ''}
                  `}
                >
                  {columns.map((col, colIndex) => (
                    <td key={colIndex} className="px-6 py-4 text-sm text-slate-600">
                      {col.render ? col.render(item) : item[col.accessor]}
                    </td>
                  ))}
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  )
}
