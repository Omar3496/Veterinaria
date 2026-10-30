import React, { useRef, useState, useEffect } from 'react'
import { motion } from 'framer-motion'

export default function ThreeHero() {
  const containerRef = useRef(null)
  const [mousePosition, setMousePosition] = useState({ x: 0, y: 0 })

  useEffect(() => {
    const handleMouseMove = (e) => {
      if (!containerRef.current) return
      const { innerWidth, innerHeight } = window
      // Normalize mouse position to range [-1, 1]
      const x = (e.clientX / innerWidth) * 2 - 1
      const y = (e.clientY / innerHeight) * 2 - 1
      setMousePosition({ x, y })
    }

    window.addEventListener('mousemove', handleMouseMove)
    return () => window.removeEventListener('mousemove', handleMouseMove)
  }, [])

  return (
    <div ref={containerRef} className="w-full h-full absolute inset-0 -z-10 overflow-hidden flex items-center justify-end pr-[10%] pt-20">
      
      {/* Background ambient lighting */}
      <div className="absolute top-[20%] right-[10%] w-[500px] h-[500px] bg-cyan-500/20 rounded-full blur-[120px] pointer-events-none"></div>
      <div className="absolute bottom-[20%] right-[30%] w-[400px] h-[400px] bg-fuchsia-500/10 rounded-full blur-[100px] pointer-events-none"></div>

      {/* Floating 3D Caduceus (Wand) Image */}
      <motion.div
        animate={{
          y: [0, -30, 0],
          rotateX: mousePosition.y * -15, // tilt effect based on mouse
          rotateY: mousePosition.x * 15,
        }}
        transition={{
          y: {
            duration: 6,
            repeat: Infinity,
            ease: "easeInOut"
          },
          rotateX: { type: "spring", stiffness: 50, damping: 20 },
          rotateY: { type: "spring", stiffness: 50, damping: 20 }
        }}
        className="relative w-[400px] h-[600px] lg:w-[600px] lg:h-[800px] perspective-1000"
        style={{ transformStyle: 'preserve-3d' }}
      >
        <img 
          src="/wand.png" 
          alt="Veterinary Caduceus" 
          className="w-full h-full object-contain mix-blend-screen opacity-90 drop-shadow-[0_0_50px_rgba(34,211,238,0.5)]"
          style={{ 
            filter: 'contrast(1.5) brightness(1.2)',
            maskImage: 'radial-gradient(ellipse at center, black 50%, transparent 72%)',
            WebkitMaskImage: 'radial-gradient(ellipse at center, black 50%, transparent 72%)'
          }}
        />
        
        {/* Glowing orb behind the wand */}
        <motion.div 
          animate={{ scale: [1, 1.2, 1], opacity: [0.5, 0.8, 0.5] }}
          transition={{ duration: 4, repeat: Infinity, ease: "easeInOut" }}
          className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-64 h-64 bg-cyan-400/30 rounded-full blur-[80px] -z-10"
        ></motion.div>
      </motion.div>

    </div>
  )
}
