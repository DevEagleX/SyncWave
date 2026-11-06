import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  // sockjs-client expects Node's "global" object, which doesn't exist in the browser.
  // This tells Vite to replace every reference to "global" with "window" instead.
  define: {
    global: 'window',
  },
})
