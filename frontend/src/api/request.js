import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../stores/auth'

export const useMock = import.meta.env.VITE_USE_MOCK !== 'false'

const service = axios.create({
  baseURL: '/api',
  timeout: 15000
})

service.interceptors.request.use((config) => {
  const auth = useAuthStore()
  if (auth.token) {
    config.headers.Authorization = `Bearer ${auth.token}`
  }
  return config
})

service.interceptors.response.use(
  (response) => {
    if (response.config.responseType === 'blob') {
      return response
    }
    const body = response.data
    if (body?.code && body.code !== 200) {
      ElMessage.error(body.message || '请求失败')
      return Promise.reject(new Error(body.message || '请求失败'))
    }
    return body
  },
  (error) => {
    const message = error.response?.data?.message || error.message || '网络请求失败'
    ElMessage.error(message)
    return Promise.reject(error)
  }
)

export function mockResolve(payload, delay = 180) {
  return new Promise((resolve) => {
    window.setTimeout(() => resolve(payload), delay)
  })
}

export default service
