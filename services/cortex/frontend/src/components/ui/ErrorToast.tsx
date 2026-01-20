import { useState } from 'react';
import { AlertTriangle, AlertCircle, Copy, Check, X } from 'lucide-react';
import { motion } from 'framer-motion';
import type { ErrorInfo } from './ErrorAlert';

interface ErrorToastProps {
  error: ErrorInfo;
  onDismiss: () => void;
}

export function ErrorToast({ error, onDismiss }: ErrorToastProps) {
  const [copied, setCopied] = useState(false);

  // Determine if this is a user/conflict error (4xx) vs system error (5xx)
  const isUserError = error.status && error.status >= 400 && error.status < 500;
  const isSystemError = !isUserError;

  const handleCopyTraceId = async () => {
    if (error.traceId) {
      try {
        await navigator.clipboard.writeText(error.traceId);
        setCopied(true);
        setTimeout(() => setCopied(false), 2000);
      } catch {
        // Clipboard access can fail in unsecured contexts or when permission is denied
        // Silently fail - the trace ID is still selectable via select-all on the code element
      }
    }
  };

  // User errors (4xx): amber/yellow styling, no trace ID needed
  // System errors (5xx): red styling, show trace ID for debugging
  const colors = isUserError
    ? {
        bg: 'bg-amber-500/10',
        border: 'border-amber-500/30',
        text: 'text-amber-400',
        textMuted: 'text-amber-400/70',
        hover: 'hover:bg-amber-500/20',
        codeBg: 'bg-amber-500/10',
        codeText: 'text-amber-300',
        progressBg: 'bg-amber-500/30',
        progress: 'bg-amber-500',
      }
    : {
        bg: 'bg-red-500/10',
        border: 'border-red-500/30',
        text: 'text-red-400',
        textMuted: 'text-red-400/70',
        hover: 'hover:bg-red-500/20',
        codeBg: 'bg-red-500/10',
        codeText: 'text-red-300',
        progressBg: 'bg-red-500/30',
        progress: 'bg-red-500',
      };

  const Icon = isUserError ? AlertCircle : AlertTriangle;

  return (
    <motion.div
      initial={{ opacity: 0, x: 100, scale: 0.95 }}
      animate={{ opacity: 1, x: 0, scale: 1 }}
      exit={{ opacity: 0, x: 100, scale: 0.95 }}
      transition={{ type: 'spring', damping: 25, stiffness: 300 }}
      layout
      className={`${colors.bg} border ${colors.border} ${colors.text} rounded-lg shadow-[0_0_30px_rgba(0,0,0,0.5)] backdrop-blur-sm overflow-hidden w-96 max-w-[calc(100vw-2rem)]`}
    >
      <div className="p-4">
        <div className="flex items-start gap-3">
          <Icon className="w-5 h-5 shrink-0 mt-0.5" />
          <div className="flex-1 min-w-0">
            <span className="block text-sm">{error.message}</span>
            {/* Only show trace ID for system errors (5xx) - user errors don't need debugging */}
            {isSystemError && error.traceId && (
              <div className="mt-2 text-xs">
                <div className="flex items-center gap-2 mb-1">
                  <span className={colors.textMuted}>Trace ID:</span>
                  <button
                    onClick={handleCopyTraceId}
                    className={`p-1 ${colors.hover} rounded transition-colors shrink-0`}
                    title="Copy Trace ID"
                  >
                    {copied ? (
                      <Check className="w-3 h-3 text-emerald-400" />
                    ) : (
                      <Copy className="w-3 h-3" />
                    )}
                  </button>
                </div>
                <code className={`font-mono ${colors.codeBg} px-2 py-1 rounded ${colors.codeText} select-all block break-all`}>
                  {error.traceId}
                </code>
              </div>
            )}
          </div>
          <button
            onClick={onDismiss}
            className={`p-1 ${colors.hover} rounded transition-colors shrink-0`}
            title="Dismiss"
          >
            <X className="w-4 h-4" />
          </button>
        </div>
      </div>
      {/* Progress bar showing countdown */}
      <div className={`h-1 ${colors.progressBg}`}>
        <motion.div
          className={`h-full ${colors.progress}`}
          initial={{ width: '100%' }}
          animate={{ width: '0%' }}
          transition={{ duration: 10, ease: 'linear' }}
        />
      </div>
    </motion.div>
  );
}
