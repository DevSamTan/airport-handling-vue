import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: { port: 5173 },
  optimizeDeps: {
    // ExcelJS browser bundle is CommonJS/UMD — pre-bundle it to ESM so Vite can import it
    include: ['exceljs'],
  },
})
