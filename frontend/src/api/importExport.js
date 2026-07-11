import request, { mockResolve, useMock } from './request'
import { buildExportCsv } from '../mock/export.mock'
import { importErrorsMock, importTaskMock } from '../mock/import.mock'

export function createImportTask({ tableName, file }) {
  if (useMock) {
    return mockResolve({
      ...importTaskMock,
      data: {
        ...importTaskMock.data,
        tableName,
        fileName: file?.name || importTaskMock.data.fileName
      }
    })
  }
  const formData = new FormData()
  formData.append('tableName', tableName)
  if (file) {
    formData.append('file', file)
  }
  return request.post('/import/tasks', formData)
}

export function getImportTask(taskId) {
  return useMock ? mockResolve(importTaskMock) : request.get(`/import/tasks/${taskId}`)
}

export function getImportErrors(taskId, params) {
  return useMock ? mockResolve(importErrorsMock) : request.get(`/import/tasks/${taskId}/errors`, { params })
}

export async function exportTable(tableName) {
  if (useMock) {
    const csv = buildExportCsv(tableName)
    return new Blob([csv], { type: 'text/csv;charset=utf-8' })
  }
  const response = await request.get(`/export/table/${tableName}`, { responseType: 'blob' })
  return response.data
}
