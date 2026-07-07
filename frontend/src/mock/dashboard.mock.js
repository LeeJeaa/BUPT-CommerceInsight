export const dashboardMock = {
  tableCount: 24,
  databaseName: 'tpc_commerce',
  dataScale: 'SF=0.2 / 课程数据集',
  dockerStatus: 'PostgreSQL 16 ready',
  rowCounts: [
    { tableName: 'region', rowCount: 5 },
    { tableName: 'nation', rowCount: 25 },
    { tableName: 'supplier', rowCount: 2000 },
    { tableName: 'customer', rowCount: 30000 },
    { tableName: 'part', rowCount: 40000 },
    { tableName: 'partsupp', rowCount: 160000 },
    { tableName: 'orders', rowCount: 300000 },
    { tableName: 'lineitem', rowCount: 1199969 }
  ],
  modules: [
    { name: 'TPC-H Q1/Q5/Q12/Q14', status: 'ready' },
    { name: 'TPC-C New-Order/Payment', status: 'ready' },
    { name: '导入清洗与错误日志', status: 'ready' },
    { name: '并发性能结果展示', status: 'mock-ready' }
  ]
}
