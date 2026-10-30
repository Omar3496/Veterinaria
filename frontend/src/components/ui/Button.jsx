import React from 'react'

export default function Button({ 
  children, 
  variant = 'primary', 
  size = 'md', 
  className = '', 
  isLoading = false,
  ...props 
}) {
  const baseStyles = "inline-flex items-center justify-center font-bold rounded-xl transition-all outline-none disabled:opacity-50 disabled:cursor-not-allowed"
  
  const variants = {
    primary: "bg-gradient-to-b from-emerald-600 to-emerald-700 text-white shadow-[inset_0_1px_0_rgba(255,255,255,0.22),0_3px_10px_-2px_rgba(4,120,87,0.5)] hover:from-emerald-500 hover:to-emerald-600 hover:-translate-y-0.5 hover:shadow-[inset_0_1px_0_rgba(255,255,255,0.28),0_10px_24px_-8px_rgba(4,120,87,0.6)] active:translate-y-0 active:shadow-inner transition-all duration-200",
    secondary: "bg-white text-slate-700 border border-slate-200 shadow-[0_1px_2px_rgba(15,23,42,0.06)] hover:border-emerald-300 hover:text-emerald-900 hover:-translate-y-0.5 hover:shadow-md hover:shadow-emerald-900/5 active:translate-y-0 transition-all duration-200",
    ghost: "bg-transparent hover:bg-slate-100 text-slate-500 hover:text-slate-900",
    danger: "bg-rose-50 hover:bg-rose-100 text-rose-600 border border-rose-200 hover:border-rose-300",
    success: "bg-emerald-50 hover:bg-emerald-100 text-emerald-600 border border-emerald-200 hover:border-emerald-300",
    "3d": "bg-gradient-to-b from-emerald-600 to-emerald-700 shadow-[0_4px_14px_0_rgb(5,150,105,0.35)] hover:shadow-[0_6px_20px_rgba(5,150,105,0.25)] hover:-translate-y-0.5 active:translate-y-1 active:shadow-inner text-white rounded-xl transition-all duration-200 font-bold px-4 py-2",
    "3d-danger": "bg-gradient-to-b from-rose-500 to-rose-600 shadow-[0_4px_14px_0_rgb(244,63,94,0.35)] hover:shadow-[0_6px_20px_rgba(244,63,94,0.25)] hover:-translate-y-0.5 active:translate-y-1 active:shadow-inner text-white rounded-xl transition-all duration-200 font-bold px-4 py-2",
    "3d-success": "bg-gradient-to-b from-emerald-500 to-emerald-600 shadow-[0_4px_14px_0_rgb(16,185,129,0.35)] hover:shadow-[0_6px_20px_rgba(16,185,129,0.25)] hover:-translate-y-0.5 active:translate-y-1 active:shadow-inner text-white rounded-xl transition-all duration-200 font-bold px-4 py-2",
    "3d-secondary": "bg-gradient-to-b from-white to-slate-50 border border-slate-200 shadow-[0_4px_14px_0_rgb(0,0,0,0.04)] hover:shadow-[0_6px_20px_rgba(0,0,0,0.06)] hover:-translate-y-0.5 active:translate-y-1 active:shadow-inner text-slate-700 rounded-xl transition-all duration-200 font-bold px-4 py-2"
  }

  const sizes = {
    sm: "px-3 py-1.5 text-xs",
    md: "px-4 py-2.5 text-sm",
    lg: "px-6 py-3 text-base"
  }

  return (
    <button
      type="button"
      className={`${baseStyles} ${variants[variant]} ${sizes[size]} ${className}`} 
      disabled={isLoading || props.disabled}
      {...props}
    >
      {isLoading ? (
        <div className="flex items-center">
          <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-current mr-2"></div>
          Cargando...
        </div>
      ) : (
        children
      )}
    </button>
  )
}
