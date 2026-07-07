import request, { mockResolve, useMock } from './request'
import { filterCustomersMock, filterOrderRevenueMock, partSupplierMock } from '../mock/query.mock'

export function queryCustomers(params) {
  return useMock ? mockResolve(filterCustomersMock(params)) : request.get('/query/customers', { params })
}

export function queryOrderRevenue(params) {
  return useMock ? mockResolve(filterOrderRevenueMock(params)) : request.get('/query/order-revenue', { params })
}

export function queryPartSupplier(params) {
  return mockResolve(partSupplierMock)
}
