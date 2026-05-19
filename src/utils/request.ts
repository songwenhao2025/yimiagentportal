// =====================================================
// 壹米AI Agent门户 - API 请求封装
// =====================================================

import axios, { type AxiosInstance, type InternalAxiosRequestConfig, type AxiosResponse } from 'axios'
import { apiConfig } from '@/config'

/**
 * 创建 axios 实例
 */
const request: AxiosInstance = axios.create({
  baseURL: apiConfig.baseUrl,
  timeout: apiConfig.timeout,
  headers: {
    'Content-Type': 'application/json',
  },
})

/**
 * 请求拦截器
 */
request.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    // 添加请求头
    const token = uni.getStorageSync('token')
    if (token) {
      config.headers = config.headers || {}
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

/**
 * 响应拦截器
 */
request.interceptors.response.use(
  (response: AxiosResponse) => {
    const { data } = response
    
    // 统一处理响应格式
    if (data.code === 200 || data.success) {
      return data.data || data
    } else {
      // 业务错误处理
      uni.showToast({
        title: data.message || '请求失败',
        icon: 'error',
      })
      return Promise.reject(data)
    }
  },
  (error) => {
    // 网络错误或服务器错误处理
    if (error.response) {
      const status = error.response.status
      switch (status) {
        case 401:
          uni.showToast({ title: '登录已过期', icon: 'none' })
          uni.redirectTo({ url: '/pages/login/index' })
          break
        case 403:
          // 权限不足，静默处理，由页面决定是否提示
          break
        default:
          // 其他 HTTP 错误静默处理，由页面自行决定提示方式
          break
      }
    } else {
      // 仅网络异常等客户端错误才提示
      uni.showToast({ title: '网络异常', icon: 'none' })
    }
    return Promise.reject(error)
  }
)

/**
 * 请求方法封装
 */
export const http = {
  get<T = any>(url: string, params?: Record<string, any>): Promise<T> {
    return request.get(url, { params })
  },

  post<T = any>(url: string, data?: Record<string, any>, config?: Record<string, any>): Promise<T> {
    return request.post(url, data, config)
  },

  put<T = any>(url: string, data?: Record<string, any>): Promise<T> {
    return request.put(url, data)
  },

  delete<T = any>(url: string, params?: Record<string, any>): Promise<T> {
    return request.delete(url, { params })
  },

  patch<T = any>(url: string, data?: Record<string, any>): Promise<T> {
    return request.patch(url, data)
  },
}

export default request