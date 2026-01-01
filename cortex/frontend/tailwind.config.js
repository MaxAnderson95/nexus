/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      fontFamily: {
        sans: ['Rajdhani', 'sans-serif'],
        mono: ['"Share Tech Mono"', 'monospace'],
      },
      colors: {
        space: {
          950: '#030712', // Deepest void
          900: '#0b1121', // Deep background
          800: '#151e32', // Panel background
          700: '#2a3a5a', // Panel border
        },
        cyan: {
          400: '#22d3ee', // Main accent
          500: '#06b6d4',
          glow: 'rgba(34, 211, 238, 0.5)',
        },
        alert: {
          red: '#ef4444',
          orange: '#f97316',
          success: '#10b981',
        }
      },
      backgroundImage: {
        'grid-pattern': "linear-gradient(to right, #1f2937 1px, transparent 1px), linear-gradient(to bottom, #1f2937 1px, transparent 1px)",
        'radial-gradient': 'radial-gradient(circle at center, var(--tw-gradient-stops))',
      },
      boxShadow: {
        'glow-cyan': '0 0 10px rgba(34, 211, 238, 0.3), 0 0 20px rgba(34, 211, 238, 0.1)',
        'glow-red': '0 0 10px rgba(239, 68, 68, 0.3), 0 0 20px rgba(239, 68, 68, 0.1)',
        'panel': '0 4px 6px -1px rgba(0, 0, 0, 0.5), 0 2px 4px -1px rgba(0, 0, 0, 0.3)',
      },
      animation: {
        'pulse-slow': 'pulse 4s cubic-bezier(0.4, 0, 0.6, 1) infinite',
        'spin-slow': 'spin 12s linear infinite',
      }
    },
  },
  plugins: [],
}
