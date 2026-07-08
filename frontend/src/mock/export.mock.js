const exportTables = {
  orders: {
    headers: [
      'o_orderkey',
      'o_custkey',
      'o_orderstatus',
      'o_totalprice',
      'o_orderdate',
      'o_orderpriority',
      'o_clerk',
      'o_shippriority'
    ],
    rows: [
      [1, 1, 'O', 172799.49, '2019-01-02', '1-URGENT', 'Clerk#000000001', 0],
      [2, 2, 'F', 88621.17, '2019-03-18', '2-HIGH', 'Clerk#000000002', 0],
      [3, 3, 'O', 92314.52, '2019-08-24', '5-LOW', 'Clerk#000000003', 0],
      [4, 4, 'F', 121450.66, '2020-02-12', '3-MEDIUM', 'Clerk#000000004', 0]
    ]
  },
  lineitem: {
    headers: [
      'l_orderkey',
      'l_partkey',
      'l_suppkey',
      'l_linenumber',
      'l_quantity',
      'l_extendedprice',
      'l_discount',
      'l_tax',
      'l_returnflag',
      'l_linestatus',
      'l_shipdate',
      'l_shipmode'
    ],
    rows: [
      [1, 1001, 501, 1, 17, 21168.23, 0.04, 0.02, 'N', 'O', '2019-02-01', 'MAIL'],
      [1, 1002, 502, 2, 36, 45983.12, 0.06, 0.03, 'N', 'O', '2019-02-03', 'SHIP'],
      [2, 1003, 503, 1, 8, 9821.55, 0.02, 0.01, 'R', 'F', '2019-04-12', 'RAIL'],
      [3, 1004, 504, 1, 22, 30210.9, 0.05, 0.04, 'A', 'F', '2019-09-05', 'MAIL']
    ]
  },
  partsupp: {
    headers: ['ps_partkey', 'ps_suppkey', 'ps_availqty', 'ps_supplycost', 'ps_comment'],
    rows: [
      [1001, 501, 8400, 18.2, 'mock partsupp row 1'],
      [1002, 502, 6200, 22.4, 'mock partsupp row 2'],
      [1003, 503, 9100, 16.7, 'mock partsupp row 3'],
      [1004, 504, 7300, 19.5, 'mock partsupp row 4']
    ]
  },
  customer: {
    headers: ['c_custkey', 'c_name', 'n_name', 'c_acctbal', 'c_mktsegment'],
    rows: [
      [1, 'Customer#000000001', 'CHINA', 711.56, 'BUILDING'],
      [2, 'Customer#000000002', 'JAPAN', 121.65, 'AUTOMOBILE'],
      [3, 'Customer#000000003', 'INDIA', 7498.12, 'MACHINERY'],
      [4, 'Customer#000000004', 'CHINA', 3280.44, 'HOUSEHOLD']
    ]
  },
  supplier: {
    headers: ['s_suppkey', 's_name', 'n_name', 's_phone', 's_acctbal'],
    rows: [
      [501, 'Supplier#000000501', 'CHINA', '13-111-111-1111', 5210.33],
      [502, 'Supplier#000000502', 'JAPAN', '13-222-222-2222', 3021.18],
      [503, 'Supplier#000000503', 'INDIA', '13-333-333-3333', 841.92]
    ]
  },
  part: {
    headers: ['p_partkey', 'p_name', 'p_brand', 'p_type', 'p_size', 'p_retailprice'],
    rows: [
      [1001, 'Part-1001', 'Brand#11', 'PROMO BRUSHED COPPER', 12, 901.23],
      [1002, 'Part-1002', 'Brand#23', 'STANDARD POLISHED STEEL', 8, 621.1],
      [1003, 'Part-1003', 'Brand#34', 'PROMO ANODIZED TIN', 20, 1180.42]
    ]
  }
}

function escapeCsv(value) {
  const text = String(value ?? '')
  if (/[",\n\r]/.test(text)) {
    return `"${text.replaceAll('"', '""')}"`
  }
  return text
}

export function buildExportCsv(tableName) {
  const table = exportTables[tableName] || {
    headers: ['table_name', 'status'],
    rows: [[tableName, 'mock_export_ready']]
  }
  const lines = [
    table.headers.map(escapeCsv).join(','),
    ...table.rows.map((row) => row.map(escapeCsv).join(','))
  ]
  return `${lines.join('\r\n')}\r\n`
}
