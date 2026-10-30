import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    tailwindcss(),
    react(),
  ],
  server: {
    host: true, // Esto abre el servidor a la red local
    port: 5173, // Opcional, puedes mantener tu puerto
    proxy: {
      '/api': {
        target: 'http://localhost:8000',
        changeOrigin: true
      },
      '/token': {
        target: 'http://localhost:8000',
        changeOrigin: true
      }
    }
  }
})
