import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  server: {
    proxy: {
      '/ark': {
        target: 'http://localhost:8081',
        changeOrigin: true,
        // 路径重写（如果需要）
        // rewrite: (path) => path.replace(/^\/ark/, '')
      }
    }
  }
})