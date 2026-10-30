import React, { createContext, useContext, useReducer, useCallback } from 'react';
import { AlertCircle, CheckCircle2, Info, X } from 'lucide-react';

const ToastContext = createContext(null);

const toastReducer = (state, action) => {
  switch (action.type) {
    case 'ADD_TOAST':
      return [...state, action.payload];
    case 'REMOVE_TOAST':
      return state.filter((toast) => toast.id !== action.payload);
    default:
      return state;
  }
};

export const ToastProvider = ({ children }) => {
  const [toasts, dispatch] = useReducer(toastReducer, []);

  const addToast = useCallback((message, type = 'info', duration = 4000) => {
    const id = Date.now().toString();
    dispatch({ type: 'ADD_TOAST', payload: { id, message, type } });

    if (duration) {
      setTimeout(() => {
        dispatch({ type: 'REMOVE_TOAST', payload: id });
      }, duration);
    }
  }, []);

  const removeToast = useCallback((id) => {
    dispatch({ type: 'REMOVE_TOAST', payload: id });
  }, []);

  return (
    <ToastContext.Provider value={{ toast: { 
      success: (msg) => addToast(msg, 'success'),
      error: (msg) => addToast(msg, 'error'),
      info: (msg) => addToast(msg, 'info'),
      custom: addToast
    }}}>
      {children}
      
      {/* Toast Container */}
      <div className="fixed bottom-6 right-6 z-[100] flex flex-col gap-3 pointer-events-none">
        {toasts.map((t) => (
          <div key={t.id} className="pointer-events-auto">
            <ToastItem toast={t} onClose={() => removeToast(t.id)} />
          </div>
        ))}
      </div>
    </ToastContext.Provider>
  );
};

const ToastItem = ({ toast, onClose }) => {
  const config = {
    success: { icon: CheckCircle2, bg: 'bg-emerald-600', border: 'border-emerald-500' },
    error: { icon: AlertCircle, bg: 'bg-rose-600', border: 'border-rose-500' },
    info: { icon: Info, bg: 'bg-indigo-600', border: 'border-indigo-500' }
  };
  
  const { icon: Icon, bg, border } = config[toast.type] || config.info;

  return (
    <div className={`${bg} border ${border} text-white px-4 py-3 rounded-xl shadow-2xl backdrop-blur-md flex items-center min-w-[300px]`}>
      <Icon className="w-5 h-5 mr-3 flex-shrink-0 drop-shadow-sm" />
      <p className="font-medium text-sm flex-grow pr-4">{toast.message}</p>
      <button 
        onClick={onClose}
        className="text-white/60 hover:text-white p-1 rounded-md hover:bg-white/10 transition-colors"
      >
        <X className="w-4 h-4" />
      </button>
    </div>
  );
};

export const useToast = () => {
  const context = useContext(ToastContext);
  if (!context) {
    throw new Error('useToast debe ser usado dentro de un ToastProvider');
  }
  return context.toast;
};
