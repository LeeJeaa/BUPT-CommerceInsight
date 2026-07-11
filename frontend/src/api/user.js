import request, { mockResolve, useMock } from './request'
import { statusMock, usersMock } from '../mock/user.mock'

export function listUsers(params) {
  return useMock ? mockResolve(usersMock) : request.get('/users', { params })
}

export function approveUser(userId) {
  return useMock ? mockResolve(statusMock(userId, 'approved')) : request.put(`/users/${userId}/approve`)
}

export function disableUser(userId) {
  return useMock ? mockResolve(statusMock(userId, 'disabled')) : request.put(`/users/${userId}/disable`)
}
