import React, { forwardRef } from 'react'

const Input = forwardRef(({ 
  label, 
  error, 
  variant = 'default',
  className = '', 
  containerClassName = '',
  labelClassName = '',
  icon: Icon,
  ...props 
}, ref) => {
  const isGlass = variant === 'glass'

  const baseInput = isGlass
    ? 'bg-white/15 border-white/30 text-white placeholder:text-white/50 backdrop-blur-sm'
    : 'bg-white border-slate-200 text-slate-900 placeholder:text-slate-400'

  const baseFocus = isGlass
    ? 'focus:border-amber-300/70 focus:ring-amber-300/20'
    : 'focus:border-[#047857] focus:ring-[#047857]/20'

  const errorBorder = isGlass
    ? 'border-rose-400/60 focus:border-rose-400 focus:ring-rose-400/20'
    : 'border-rose-300 focus:border-rose-500 focus:ring-rose-500/20'

  return (
    <div className={`${containerClassName}`}>
      {label && (
        <label className={`block text-xs font-bold text-slate-500 uppercase tracking-widest mb-2 ${labelClassName}`}>
          {label}
        </label>
      )}
      <div className="relative">
        {Icon && (
          <div className={`absolute left-3 top-1/2 -translate-y-1/2 ${isGlass ? 'text-white/50' : 'text-slate-400'}`}>
            <Icon className="w-5 h-5" />
          </div>
        )}
        <input
          ref={ref}
          className={`
            w-full border rounded-xl px-4 py-3 outline-none transition-all duration-300
            ${Icon ? 'pl-10' : ''}
            ${error ? errorBorder : baseFocus}
            ${baseInput}
            ${className}
          `}
          {...props}
        />
      </div>
      {error && (
        <p className="mt-2 text-xs font-medium text-rose-600 flex items-center">
          <span className="w-1.5 h-1.5 rounded-full bg-rose-500 mr-2"></span>
          {error}
        </p>
      )}
    </div>
  )
})

Input.displayName = 'Input'

export default Input
