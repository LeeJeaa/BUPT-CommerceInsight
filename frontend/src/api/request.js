import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'
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
      const handled = handleAuthError(body.code, body.message)
      if (!handled) {
        ElMessage.error(body.message || '请求失败')
      }
      return Promise.reject(new Error(body.message || '请求失败'))
    }
    return body
  },
  (error) => {
    const status = error.response?.status
    const message = error.response?.data?.message || error.message || '网络请求失败'
    const handled = handleAuthError(status, message)
    const silent = error.config?.silentStatuses?.includes(Number(status))
    if (!handled && !silent) {
      ElMessage.error(message)
    }
    return Promise.reject(error)
  }
)

function handleAuthError(status, message) {
  if (Number(status) === 401) {
    const auth = useAuthStore()
    auth.logout()
    ElMessage.error(message || '登录已失效，请重新登录')
    if (router.currentRoute.value.path !== '/login') {
      router.push('/login')
    }
    return true
  }
  if (Number(status) === 403) {
    ElMessage.error(message || '无权限访问该功能')
    return true
  }
  return false
}

export function mockResolve(payload, delay = 180) {
  return new Promise((resolve) => {
    window.setTimeout(() => resolve(payload), delay)
  })
}

export default service
