import { defineStore } from 'pinia'

const saved = JSON.parse(localStorage.getItem('commerceInsightAuth') || '{}')

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: saved.token || '',
    userId: saved.userId || null,
    username: saved.username || '',
    role: saved.role || '',
    status: saved.status || ''
  }),
  actions: {
    setSession(data) {
      this.token = data.token
      this.userId = data.userId
      this.username = data.username
      this.role = data.role
      this.status = data.status
      localStorage.setItem('commerceInsightAuth', JSON.stringify(data))
    },
    logout() {
      this.token = ''
      this.userId = null
      this.username = ''
      this.role = ''
      this.status = ''
      localStorage.removeItem('commerceInsightAuth')
    }
  }
})
