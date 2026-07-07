import request, { mockResolve, useMock } from './request'
import { loginMock, registerMock } from '../mock/auth.mock'

export function login(data) {
  return useMock ? mockResolve(loginMock(data)) : request.post('/auth/login', data)
}

export function register(data) {
  return useMock ? mockResolve(registerMock(data)) : request.post('/auth/register', data)
}
