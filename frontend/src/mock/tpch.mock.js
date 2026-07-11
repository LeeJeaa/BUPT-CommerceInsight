export const tpchMocks = {
  q1: {
    code: 200,
    message: 'success',
    data: {
      queryName: 'TPC-H Q1 定价汇总报表',
      elapsedMs: 820,
      rowCount: 4,
      records: [
        { returnFlag: 'A', lineStatus: 'F', sumQuantity: 37734107, sumBasePrice: 56586554400.73, sumDiscountedPrice: 53758257134.87, sumCharge: 55909065222.83, avgQuantity: 25.52, avgPrice: 38273.13, avgDisc: 0.05, countOrder: 1478493 },
        { returnFlag: 'N', lineStatus: 'F', sumQuantity: 991417, sumBasePrice: 1487504710.38, sumDiscountedPrice: 1413082168.05, sumCharge: 1469649223.19, avgQuantity: 25.52, avgPrice: 38284.47, avgDisc: 0.05, countOrder: 38854 },
        { returnFlag: 'N', lineStatus: 'O', sumQuantity: 74476040, sumBasePrice: 111701729697.74, sumDiscountedPrice: 106118230307.61, sumCharge: 110367043872.5, avgQuantity: 25.5, avgPrice: 38249.12, avgDisc: 0.05, countOrder: 2920374 },
        { returnFlag: 'R', lineStatus: 'F', sumQuantity: 37719753, sumBasePrice: 56568041380.9, sumDiscountedPrice: 53741292684.6, sumCharge: 55889619119.83, avgQuantity: 25.51, avgPrice: 38250.85, avgDisc: 0.05, countOrder: 1478870 }
      ],
      chartData: {
        xAxis: ['A-F', 'N-F', 'N-O', 'R-F'],
        series: [55909065222.83, 1469649223.19, 110367043872.5, 55889619119.83]
      },
      explainPlan: 'Aggregate -> Seq Scan on lineitem -> Sort by l_returnflag, l_linestatus'
    }
  },
  q5: {
    code: 200,
    message: 'success',
    data: {
      queryName: 'TPC-H Q5 本地供应商收入分析',
      elapsedMs: 3521,
      rowCount: 5,
      records: [
        { nationName: 'CHINA', revenue: 9234567.89 },
        { nationName: 'INDIA', revenue: 8765432.1 },
        { nationName: 'JAPAN', revenue: 7654321.11 },
        { nationName: 'INDONESIA', revenue: 6543210.22 },
        { nationName: 'VIETNAM', revenue: 5432109.33 }
      ],
      chartData: {
        xAxis: ['CHINA', 'INDIA', 'JAPAN', 'INDONESIA', 'VIETNAM'],
        series: [9234567.89, 8765432.1, 7654321.11, 6543210.22, 5432109.33]
      },
      explainPlan: 'Hash Join -> GroupAggregate -> Sort revenue desc'
    }
  },
  q12: {
    code: 200,
    message: 'success',
    data: {
      queryName: 'TPC-H Q12 运送方式与订单优先级',
      elapsedMs: 1240,
      rowCount: 2,
      records: [
        { shipMode: 'MAIL', highLineCount: 6202, lowLineCount: 9324 },
        { shipMode: 'SHIP', highLineCount: 6200, lowLineCount: 9262 }
      ],
      chartData: {
        xAxis: ['MAIL', 'SHIP'],
        highSeries: [6202, 6200],
        lowSeries: [9324, 9262]
      },
      explainPlan: 'Aggregate -> Bitmap Heap Scan on lineitem'
    }
  },
  q14: {
    code: 200,
    message: 'success',
    data: {
      queryName: 'TPC-H Q14 促销收入占比',
      elapsedMs: 680,
      rowCount: 1,
      records: [{ promoRevenuePercent: 16.38 }],
      chartData: {
        gauge: 16.38,
        pieData: [
          { name: '促销收入', value: 16.38 },
          { name: '非促销收入', value: 83.62 }
        ]
      },
      explainPlan: 'Hash Join -> Aggregate promo revenue percent'
    }
  }
}
