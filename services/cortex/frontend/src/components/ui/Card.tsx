import React from 'react';
import { cn } from '../../lib/utils';
import { motion } from 'framer-motion';

interface CardProps extends Omit<React.ComponentPropsWithoutRef<typeof motion.div>, "children"> {
  title?: string;
  subtitle?: string;
  action?: React.ReactNode;
  noPadding?: boolean;
  children?: React.ReactNode;
}

export function Card({ 
  title, 
  subtitle, 
  action, 
  children, 
  className, 
  noPadding = false,
  ...props 
}: CardProps) {
  return (
    <motion.div 
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.3 }}
      className={cn(
        "relative bg-space-900/40 backdrop-blur-sm border border-space-700/50 overflow-hidden group",
        "before:absolute before:inset-0 before:bg-gradient-to-b before:from-cyan-500/5 before:to-transparent before:pointer-events-none",
        "hover:border-cyan-500/30 transition-colors duration-300",
        className
      )}
      {...props}
    >
      {/* Decorative corners */}
      <div className="absolute top-0 left-0 w-2 h-2 border-t border-l border-cyan-500/50" />
      <div className="absolute top-0 right-0 w-2 h-2 border-t border-r border-cyan-500/50" />
      <div className="absolute bottom-0 left-0 w-2 h-2 border-b border-l border-cyan-500/50" />
      <div className="absolute bottom-0 right-0 w-2 h-2 border-b border-r border-cyan-500/50" />

      {(title || subtitle || action) && (
        <div className="relative px-6 py-4 border-b border-space-700/50 flex items-center justify-between bg-space-900/20">
          <div>
            {title && (
              <h3 className="text-lg font-medium text-cyan-100 tracking-wide font-sans flex items-center gap-2">
                {title}
                <div className="h-1 w-1 rounded-full bg-cyan-400 animate-pulse" />
              </h3>
            )}
            {subtitle && (
              <p className="text-xs text-cyan-500/70 font-mono uppercase tracking-wider mt-0.5">{subtitle}</p>
            )}
          </div>
          {action && <div className="ml-4">{action}</div>}
        </div>
      )}
      
      <div className={cn("relative", !noPadding && "p-6")}>
        {children}
      </div>
    </motion.div>
  );
}

export function StatsCard({ 
  label, 
  value, 
  trend, 
  trendUp, 
  icon: Icon 
}: { 
  label: string; 
  value: string | number; 
  trend?: string; 
  trendUp?: boolean;
  icon?: React.ElementType;
}) {
  return (
    <Card className="hover:bg-space-800/20 transition-colors">
      <div className="flex items-start justify-between">
        <div>
          <p className="text-xs font-mono text-cyan-500/70 uppercase tracking-widest">{label}</p>
          <div className="mt-2 flex items-baseline gap-2">
            <span className="text-2xl font-bold text-white tracking-wide font-mono text-shadow-glow">{String(value)}</span>
            {trend && (
              <span className={cn(
                "text-xs font-mono px-1.5 py-0.5 rounded",
                trendUp ? "text-emerald-400 bg-emerald-400/10" : "text-red-400 bg-red-400/10"
              )}>
                {trend}
              </span>
            )}
          </div>
        </div>
        {Icon && (
          <div className="p-2 bg-cyan-500/10 rounded border border-cyan-500/20 text-cyan-400">
            <Icon className="w-5 h-5" />
          </div>
        )}
      </div>
    </Card>
  );
}
