import React from 'react'

export default function Card({ 
  children, 
  variant = 'default', 
  className = '', 
  ...props 
}) {
  const cardRef = React.useRef(null)
  const [mousePosition, setMousePosition] = React.useState({ x: 0, y: 0 })
  const [isHovering, setIsHovering] = React.useState(false)

  const handleMouseMove = (e) => {
    if (!cardRef.current) return
    const rect = cardRef.current.getBoundingClientRect()
    setMousePosition({
      x: e.clientX - rect.left,
      y: e.clientY - rect.top,
    })
  }

  const baseStyles = "rounded-3xl border relative overflow-hidden"
  
  const variants = {
    default: "bg-white border-slate-100 shadow-sm",
    bento: "bg-white border-slate-100 hover:border-emerald-200 transition-colors duration-500 shadow-sm",
    spotlight: "bg-white border-slate-100 shadow-sm",
  }

  return (
    <div
      ref={cardRef}
      onMouseMove={handleMouseMove}
      onMouseEnter={() => setIsHovering(true)}
      onMouseLeave={() => setIsHovering(false)}
      className={`${baseStyles} ${variants[variant]} ${className}`}
      {...props}
    >
      {variant === 'spotlight' && isHovering && (
        <div
          className="pointer-events-none absolute -inset-px rounded-3xl opacity-0 transition duration-300"
          style={{
            opacity: isHovering ? 1 : 0,
            background: `radial-gradient(600px circle at ${mousePosition.x}px ${mousePosition.y}px, rgba(4,120,87,0.06), transparent 40%)`,
          }}
        />
      )}
      <div className="relative z-10 h-full w-full">
        {children}
      </div>
    </div>
  )
}
