import axios, { type AxiosResponse, isAxiosError } from 'axios'
import { useAuthStore } from '@/store/authStore'
import type { ApiResult } from './types'

/** 单体后端统一入口，例如 http://localhost:8080 */
const baseURL = import.meta.env.VITE_API_BASE ?? ''

export const api = axios.create({
  baseURL,
  timeout: 30000,
})

api.interceptors.request.use((config) => {
  const t = useAuthStore.getState().token
  if (t) {
    config.headers.Authorization = `Bearer ${t}`
  }
  return config
})

api.interceptors.response.use(
  (r) => r,
  (err: unknown) => {
    if (isAxiosError(err) && err.response?.status === 401) {
      useAuthStore.getState().logout()
      if (window.location.pathname !== '/login') {
        window.location.replace('/login')
      }
      return Promise.reject(new Error('登录状态已失效，请重新登录'))
    }
    if (isAxiosError(err) && err.response?.status === 403) {
      if (window.location.pathname !== '/forbidden') {
        window.location.replace('/forbidden')
      }
      return Promise.reject(new Error('无访问权限'))
    }
    if (isAxiosError(err) && err.response?.data && typeof err.response.data === 'object') {
      const d = err.response.data as { message?: string; code?: string }
      if (d.message) {
        return Promise.reject(new Error(d.message))
      }
    }
    return Promise.reject(err instanceof Error ? err : new Error('请求失败'))
  },
)

export function unwrap<T>(res: AxiosResponse<ApiResult<T>>): T {
  const body = res.data
  if (!body.success) {
    throw new Error(body.message || `业务错误 ${body.code}`)
  }
  return body.data
}
