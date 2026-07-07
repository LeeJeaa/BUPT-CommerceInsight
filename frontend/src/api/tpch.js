import request, { mockResolve, useMock } from './request'
import { tpchMocks } from '../mock/tpch.mock'

export function runQ1(params) {
  return useMock ? mockResolve(tpchMocks.q1) : request.get('/tpch/q1', { params })
}

export function runQ5(params) {
  return useMock ? mockResolve(tpchMocks.q5) : request.get('/tpch/q5', { params })
}

export function runQ12(params) {
  return useMock ? mockResolve(tpchMocks.q12) : request.get('/tpch/q12', { params })
}

export function runQ14(params) {
  return useMock ? mockResolve(tpchMocks.q14) : request.get('/tpch/q14', { params })
}
