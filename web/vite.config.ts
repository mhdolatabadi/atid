import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

// https://vite.dev/config/
export default defineConfig({
  // Absolute asset paths: pages live at nested URLs such as /app/texts/ziyarat_ashura.
  base: '/',
  plugins: [react()],
})
