import request, { mockResolve, useMock } from './request'
import { dashboardMock } from '../mock/dashboard.mock'

export function queryDashboardSummary() {
  return useMock ? mockResolve({ code: 200, message: 'success', data: dashboardMock }) : request.get('/dashboard/summary')
}
