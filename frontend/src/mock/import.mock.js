export const importTaskMock = {
  code: 200,
  message: 'success',
  data: {
    taskId: 1001,
    tableName: 'orders',
    fileName: 'orders_sample.txt',
    status: 'success',
    totalRows: 1000,
    successRows: 990,
    failedRows: 10,
    elapsedMs: 1250,
    startedAt: '2026-07-06 10:00:00',
    endedAt: '2026-07-06 10:00:02'
  }
}

export const importErrorsMock = {
  code: 200,
  message: 'success',
  data: {
    pageNo: 1,
    pageSize: 20,
    total: 2,
    records: [
      {
        lineNumber: 18,
        fieldName: 'o_totalprice',
        fieldValue: '-1',
        errorReason: '金额不能为负数'
      },
      {
        lineNumber: 42,
        fieldName: 'o_orderdate',
        fieldValue: '99-13-01',
        errorReason: '日期格式错误'
      }
    ]
  }
}
