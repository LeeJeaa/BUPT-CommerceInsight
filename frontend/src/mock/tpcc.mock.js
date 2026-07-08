export const newOrderMock = {
  code: 200,
  message: 'success',
  data: {
    transactionId: 'NO-20260706-0001',
    status: 'committed',
    orderId: 3001,
    totalAmount: 125.5,
    elapsedMs: 82
  }
}

export const paymentMock = {
  code: 200,
  message: 'success',
  data: {
    transactionId: 'PAY-20260706-0001',
    status: 'committed',
    customerId: 1,
    newBalance: 520.25,
    elapsedMs: 45
  }
}
