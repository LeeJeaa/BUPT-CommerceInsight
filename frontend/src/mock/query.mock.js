const customerRecords = [
  {
    customerKey: 1,
    customerName: 'Customer#000000001',
    nationName: 'CHINA',
    accountBalance: 711.56,
    marketSegment: 'BUILDING'
  },
  {
    customerKey: 2,
    customerName: 'Customer#000000002',
    nationName: 'JAPAN',
    accountBalance: 121.65,
    marketSegment: 'AUTOMOBILE'
  },
  {
    customerKey: 3,
    customerName: 'Customer#000000003',
    nationName: 'INDIA',
    accountBalance: 7498.12,
    marketSegment: 'MACHINERY'
  },
  {
    customerKey: 4,
    customerName: 'Customer#000000004',
    nationName: 'CHINA',
    accountBalance: 3280.44,
    marketSegment: 'HOUSEHOLD'
  },
  {
    customerKey: 5,
    customerName: 'Customer#000000005',
    nationName: 'JAPAN',
    accountBalance: 968.2,
    marketSegment: 'BUILDING'
  }
]

const orderRevenueRecords = [
  { orderKey: 1, orderDate: '2019-01-02', customerName: 'Customer#000000001', revenue: 172799.49 },
  { orderKey: 2, orderDate: '2019-03-18', customerName: 'Customer#000000002', revenue: 88621.17 },
  { orderKey: 3, orderDate: '2019-08-24', customerName: 'Customer#000000003', revenue: 92314.52 },
  { orderKey: 4, orderDate: '2020-02-12', customerName: 'Customer#000000004', revenue: 121450.66 },
  { orderKey: 5, orderDate: '2020-09-03', customerName: 'Customer#000000005', revenue: 65420.35 },
  { orderKey: 6, orderDate: '2021-04-21', customerName: 'Customer#000000006', revenue: 143880.9 }
]

function page(records, pageNo = 1, pageSize = 20) {
  const start = (Number(pageNo) - 1) * Number(pageSize)
  const end = start + Number(pageSize)
  return {
    pageNo: Number(pageNo),
    pageSize: Number(pageSize),
    total: records.length,
    records: records.slice(start, end)
  }
}

export function filterCustomersMock(params = {}) {
  const keyword = (params.keyword || '').trim().toLowerCase()
  const nationName = (params.nationName || '').trim().toUpperCase()
  const records = customerRecords.filter((item) => {
    const matchesKeyword = !keyword || item.customerName.toLowerCase().includes(keyword)
    const matchesNation = !nationName || item.nationName === nationName
    return matchesKeyword && matchesNation
  })
  return {
    code: 200,
    message: 'success',
    data: page(records, params.pageNo, params.pageSize)
  }
}

export function filterOrderRevenueMock(params = {}) {
  const startDate = params.startDate || '0000-01-01'
  const endDate = params.endDate || '9999-12-31'
  const records = orderRevenueRecords.filter((item) => item.orderDate >= startDate && item.orderDate <= endDate)
  return {
    code: 200,
    message: 'success',
    data: page(records, params.pageNo, params.pageSize)
  }
}

export const customersMock = {
  code: 200,
  message: 'success',
  data: {
    ...page(customerRecords)
  }
}

export const orderRevenueMock = {
  code: 200,
  message: 'success',
  data: {
    ...page(orderRevenueRecords)
  }
}

export const partSupplierMock = {
  code: 200,
  message: 'success',
  data: {
    pageNo: 1,
    pageSize: 20,
    total: 3,
    records: [
      { partKey: 1001, partName: 'Part-1001', supplierName: 'Supplier#000000001', nationName: 'CHINA', availQty: 8400, supplyCost: 18.2 },
      { partKey: 1002, partName: 'Part-1002', supplierName: 'Supplier#000000002', nationName: 'JAPAN', availQty: 6200, supplyCost: 22.4 },
      { partKey: 1003, partName: 'Part-1003', supplierName: 'Supplier#000000003', nationName: 'INDIA', availQty: 9100, supplyCost: 16.7 }
    ]
  }
}
