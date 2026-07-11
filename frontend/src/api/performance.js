import request, { mockResolve, useMock } from './request'
import { buildPerformanceMock, normalizePerformance } from '../mock/performance.mock'

export async function getPerformanceResults(params) {
  if (useMock) {
    return mockResolve(buildPerformanceMock(params?.testType))
  }
  const response = await request.get('/performance/results', {
    params,
    silentStatuses: [404]
  })
  return normalizePerformance(response)
}
