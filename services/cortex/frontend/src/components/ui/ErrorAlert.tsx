import { AlertTriangle, AlertCircle, Copy, Check, X } from 'lucide-react';
import { useState } from 'react';

export interface ErrorInfo {
  message: string;
  traceId: string | null;
  status?: number;
}

interface ErrorAlertProps {
  error: ErrorInfo;
  className?: string;
  onDismiss?: () => void;
}

export function ErrorAlert({ error, className = '', onDismiss }: ErrorAlertProps) {
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
      }
    : {
        bg: 'bg-red-500/10',
        border: 'border-red-500/30',
        text: 'text-red-400',
        textMuted: 'text-red-400/70',
        hover: 'hover:bg-red-500/20',
        codeBg: 'bg-red-500/10',
        codeText: 'text-red-300',
      };

  const Icon = isUserError ? AlertCircle : AlertTriangle;

  return (
    <div className={`${colors.bg} border ${colors.border} ${colors.text} p-4 rounded ${className}`}>
      <div className="flex items-start gap-3">
        <Icon className="w-5 h-5 shrink-0 mt-0.5" />
        <div className="flex-1 min-w-0">
          <span className="block">{error.message}</span>
          {/* Only show trace ID for system errors (5xx) - user errors don't need debugging */}
          {isSystemError && error.traceId && (
            <div className="mt-2 flex items-center gap-2 text-xs">
              <span className={colors.textMuted}>Trace ID:</span>
              <code className={`font-mono ${colors.codeBg} px-2 py-0.5 rounded ${colors.codeText} select-all`}>
                {error.traceId}
              </code>
              <button
                onClick={handleCopyTraceId}
                className={`p-1 ${colors.hover} rounded transition-colors`}
                title="Copy Trace ID"
              >
                {copied ? (
                  <Check className="w-3 h-3 text-emerald-400" />
                ) : (
                  <Copy className="w-3 h-3" />
                )}
              </button>
            </div>
          )}
        </div>
        {onDismiss && (
          <button
            onClick={onDismiss}
            className={`p-1 ${colors.hover} rounded transition-colors shrink-0`}
            title="Dismiss"
          >
            <X className="w-4 h-4" />
          </button>
        )}
      </div>
    </div>
  );
}
