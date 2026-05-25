import { defineConfig } from "vite";
import uni from "@dcloudio/vite-plugin-uni";

export default defineConfig({
  plugins: [uni()],
  server: {
    proxy: {
      '/api/auth': {
        target: 'http://localhost:8081',
        changeOrigin: true,
      },
      '/api/agents': {
        target: 'http://localhost:8082',
        changeOrigin: true,
      },
      '/api/agent-call-logs': {
        target: 'http://localhost:8082',
        changeOrigin: true,
      },
      '/api/skills': {
        target: 'http://localhost:8083',
        changeOrigin: true,
      },
      '/api/workflows': {
        target: 'http://localhost:8084',
        changeOrigin: true,
      },
      '/api/workflow-executions': {
        target: 'http://localhost:8084',
        changeOrigin: true,
      },
      '/api/knowledge': {
        target: 'http://localhost:8085',
        changeOrigin: true,
      },
      '/api/admin': {
        target: 'http://localhost:8086',
        changeOrigin: true,
      },
      '/api/users': {
        target: 'http://localhost:8081',
        changeOrigin: true,
      },
      '/api/llm': {
        target: 'http://localhost:8087',
        changeOrigin: true,
      },
    },
  },
});
