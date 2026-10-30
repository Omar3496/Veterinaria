import React, { useEffect } from 'react'
import { createPortal } from 'react-dom'
import { X } from 'lucide-react'

export default function Modal({ 
  isOpen, 
  onClose, 
  title, 
  children,
  maxWidth = "max-w-md" 
}) {
  useEffect(() => {
    if (isOpen) {
      document.body.style.overflow = 'hidden'
    } else {
      document.body.style.overflow = 'unset'
    }
    return () => { document.body.style.overflow = 'unset' }
  }, [isOpen])

  if (!isOpen) return null

  return createPortal(
    <div className="fixed inset-0 z-[100] flex items-center justify-center p-4">
      <div
        className="absolute inset-0 bg-slate-900/40 backdrop-blur-sm"
        onClick={onClose}
      />
      <div
        className={`relative w-full ${maxWidth} max-h-[calc(100dvh-2rem)] flex flex-col bg-white border border-slate-100 rounded-3xl shadow-2xl overflow-hidden`}
      >
        {title && (
          <div className="px-4 sm:px-6 py-5 border-b border-slate-100 flex justify-between items-center bg-slate-50 shrink-0">
            <h2 className="text-lg font-bold text-slate-900 tracking-tight">
              {title}
            </h2>
            <button 
              onClick={onClose} 
              className="text-slate-400 hover:text-slate-700 transition-colors w-8 h-8 flex items-center justify-center rounded-full hover:bg-slate-100"
            >
              <X className="w-5 h-5" />
            </button>
          </div>
        )}
        <div className="p-4 sm:p-6 overflow-y-auto min-h-0">
          {children}
        </div>
      </div>
    </div>,
    document.body
  )
}
