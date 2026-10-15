import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'
import { fromDate } from './src/lib/persianDate.ts'

// https://vite.dev/config/
export default defineConfig({
  // Absolute asset paths: pages live at nested URLs such as /calendar/1405/mehr.
  base: '/',
  plugins: [react()],
  define: {
    // Which calendar years get static pages; fixed per build so the browser never disagrees with
    // the pre-rendered HTML around Nowruz.
    __BUILD_JALALI_YEAR__: JSON.stringify(fromDate(new Date()).year),
  },
})
