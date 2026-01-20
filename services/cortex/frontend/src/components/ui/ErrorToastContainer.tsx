import { AnimatePresence } from 'framer-motion';
import { useErrorToast } from '../../context/ErrorToastContext';
import { ErrorToast } from './ErrorToast';

export function ErrorToastContainer() {
  const { toasts, dismissToast } = useErrorToast();

  return (
    <div className="fixed top-4 right-4 z-50 flex flex-col gap-3 pointer-events-none">
      <AnimatePresence mode="popLayout">
        {toasts.map((toast) => (
          <div key={toast.id} className="pointer-events-auto">
            <ErrorToast
              error={toast.error}
              onDismiss={() => dismissToast(toast.id)}
            />
          </div>
        ))}
      </AnimatePresence>
    </div>
  );
}
