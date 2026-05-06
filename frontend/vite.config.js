import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  define: {
    global: 'window',
  },
  test: {
    // jsdom simulates browser environment (localStorage, DOM, etc.)
    environment: 'jsdom',

    // Automatically set up @testing-library/jest-dom matchers (toBeInTheDocument, etc.)
    setupFiles: ['./src/__tests__/setup.js'],

    // Coverage configuration
    coverage: {
      provider: 'v8',
      reporter: ['text', 'html', 'lcov'],
      include: ['src/**/*.{js,jsx}'],
      exclude: [
        'src/__tests__/**',
        'src/main.jsx',
        'src/App.jsx',
        'src/assets/**',
      ],
      // Minimum coverage thresholds — build fails if below these
      thresholds: {
        lines: 60,
        functions: 60,
        branches: 50,
      },
    },

    // Verbose output shows individual test results
    reporter: ['verbose'],

    globals: true, // allows describe/it/expect without imports
  },
});
