import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

// https://vite.dev/config/
export default defineConfig({
  // Absolute asset paths: pages live at nested URLs such as /prayer-times/tehran.
  base: '/',
  plugins: [react()],
})
