import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import {ElementPlusResolver} from "unplugin-vue-components/resolvers";
import ElementPlus from "unplugin-element-plus/vite";

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    // ElementPlus 插件配置
    ElementPlus({useSource:true}),
    // 自动导入 API (如 ref, reactive 等)
    AutoImport({resolvers:[ElementPlusResolver({importStyle:'sass'})]}),
    // 自动注册组件 (如 ElButton, ElTable 等)
    Components({resolvers: [ElementPlusResolver({importStyle: 'sass'})]}),
    vueDevTools(),
  ],
  resolve: {
    alias: {
      // 配置路径别名，'@' 代表 src 目录
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
  },
  server: {
    port: 5173, // 开发服务器端口
    // 代理配置，解决跨域问题
    proxy: {
      // Java 后端接口代理：将 /api 开头的请求转发到 http://localhost:9090
      '/api': {
        target: 'http://localhost:9090',
        changeOrigin: true, // 允许跨域
        rewrite: (path) => path.replace(/^\/api/, '') // 去掉路径中的 /api 前缀
      },
      // 文件服务代理：将 /files 开头的请求转发到 http://localhost:9090/files
      '/files': {
        target: 'http://localhost:9090',
        changeOrigin: true,
      },
      // Python 视频流服务代理：将 /py-api 开头的请求转发到 http://localhost:5000
      '/py-api': {
        target: 'http://localhost:5000',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/py-api/, '') // 去掉路径中的 /py-api 前缀
      }
    }
  },
  css:{
    preprocessorOptions: {
      scss: {
        // 全局引入 SCSS 变量文件
        additionalData:'@use "@/assets/index.scss" as *;',
      }
    }
  }
})
