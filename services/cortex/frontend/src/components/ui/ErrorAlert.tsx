import { AlertTriangle, Copy, Check } from 'lucide-react';
import { useState } from 'react';

export interface ErrorInfo {
  message: string;
  traceId: string | null;
}

interface ErrorAlertProps {
  error: ErrorInfo;
  className?: string;
}

export function ErrorAlert({ error, className = '' }: ErrorAlertProps) {
  const [copied, setCopied] = useState(false);

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

  return (
    <div className={`bg-red-500/10 border border-red-500/30 text-red-400 p-4 rounded ${className}`}>
      <div className="flex items-start gap-3">
        <AlertTriangle className="w-5 h-5 shrink-0 mt-0.5" />
        <div className="flex-1 min-w-0">
          <span className="block">{error.message}</span>
          {error.traceId && (
            <div className="mt-2 flex items-center gap-2 text-xs">
              <span className="text-red-400/70">Trace ID:</span>
              <code className="font-mono bg-red-500/10 px-2 py-0.5 rounded text-red-300 select-all">
                {error.traceId}
              </code>
              <button
                onClick={handleCopyTraceId}
                className="p-1 hover:bg-red-500/20 rounded transition-colors"
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
      </div>
    </div>
  );
}
