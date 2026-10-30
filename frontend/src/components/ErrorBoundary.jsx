import React from 'react'

export default class ErrorBoundary extends React.Component {
  constructor(props) {
    super(props)
    this.state = { hasError: false, message: '' }
  }

  static getDerivedStateFromError(error) {
    return { hasError: true, message: error.message || 'Error desconocido' }
  }

  componentDidCatch(error, info) {
    console.error('Error capturado por ErrorBoundary:', error, info)
  }

  handleReload = () => {
    window.location.reload()
  }

  render() {
    if (this.state.hasError) {
      return (
        <div className="min-h-screen bg-[#06030F] flex items-center justify-center p-6 text-center">
          <div className="max-w-md w-full">
            <h1 className="text-2xl font-bold text-red-400 mb-4">Ocurrió un error</h1>
            <p className="text-slate-400 text-sm mb-6 break-words">
              {this.state.message}
            </p>
            <button
              type="button"
              onClick={this.handleReload}
              className="px-6 py-3 rounded-xl bg-cyan-500 text-slate-900 font-bold hover:bg-cyan-400 transition-colors"
            >
              Recargar página
            </button>
          </div>
        </div>
      )
    }
    return this.props.children
  }
}
