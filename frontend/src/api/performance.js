import request, { mockResolve, useMock } from './request'
import { normalizePerformance, performanceMock } from '../mock/performance.mock'

export async function getPerformanceResults(params) {
  if (useMock) {
    return mockResolve(performanceMock)
  }
  const response = await request.get('/performance/results', { params })
  return normalizePerformance(response)
}
