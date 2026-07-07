import request, { mockResolve, useMock } from './request'
import { newOrderMock, paymentMock } from '../mock/tpcc.mock'

export function createNewOrder(data) {
  return useMock ? mockResolve(newOrderMock) : request.post('/tpcc/new-order', data)
}

export function createPayment(data) {
  return useMock ? mockResolve(paymentMock) : request.post('/tpcc/payment', data)
}
