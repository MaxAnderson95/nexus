import { createContext, useContext, useState, useCallback, type ReactNode } from 'react';
import type { ErrorInfo } from '../components/ui/ErrorAlert';

export interface ErrorToastItem {
  id: string;
  error: ErrorInfo;
  timestamp: number;
}

interface ErrorToastContextType {
  toasts: ErrorToastItem[];
  showError: (error: ErrorInfo) => void;
  dismissToast: (id: string) => void;
}

const ErrorToastContext = createContext<ErrorToastContextType | null>(null);

let toastIdCounter = 0;

export function ErrorToastProvider({ children }: { children: ReactNode }) {
  const [toasts, setToasts] = useState<ErrorToastItem[]>([]);

  const showError = useCallback((error: ErrorInfo) => {
    const id = `toast-${++toastIdCounter}-${Date.now()}`;
    const newToast: ErrorToastItem = {
      id,
      error,
      timestamp: Date.now(),
    };

    setToasts((prev) => [newToast, ...prev]);

    // Auto-dismiss after 10 seconds
    setTimeout(() => {
      setToasts((prev) => prev.filter((t) => t.id !== id));
    }, 10000);
  }, []);

  const dismissToast = useCallback((id: string) => {
    setToasts((prev) => prev.filter((t) => t.id !== id));
  }, []);

  return (
    <ErrorToastContext.Provider value={{ toasts, showError, dismissToast }}>
      {children}
    </ErrorToastContext.Provider>
  );
}

export function useErrorToast() {
  const context = useContext(ErrorToastContext);
  if (!context) {
    throw new Error('useErrorToast must be used within an ErrorToastProvider');
  }
  return context;
}
