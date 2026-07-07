export const usersMock = {
  code: 200,
  message: 'success',
  data: {
    pageNo: 1,
    pageSize: 20,
    total: 3,
    records: [
      {
        userId: 1,
        username: 'admin',
        realName: '管理员',
        email: 'admin@example.com',
        role: 'admin',
        status: 'approved',
        createdAt: '2026-07-06 10:00:00'
      },
      {
        userId: 2,
        username: 'user',
        realName: '普通用户',
        email: 'user@example.com',
        role: 'user',
        status: 'approved',
        createdAt: '2026-07-06 10:15:00'
      },
      {
        userId: 3,
        username: 'alice',
        realName: 'Alice',
        email: 'alice@example.com',
        role: 'user',
        status: 'pending',
        createdAt: '2026-07-06 11:00:00'
      }
    ]
  }
}

export const statusMock = (userId, status) => ({
  code: 200,
  message: 'success',
  data: { userId, status }
})
