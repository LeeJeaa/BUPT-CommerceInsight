export const loginMock = ({ username }) => ({
  code: 200,
  message: 'success',
  data: {
    token: `mock-token-${username || 'admin'}`,
    userId: username === 'user' || username === 'user1' ? 2 : 1,
    username: username || 'admin',
    role: username === 'user' || username === 'user1' ? 'user' : 'admin',
    status: 'approved'
  }
})

export const registerMock = ({ username }) => ({
  code: 200,
  message: 'success',
  data: {
    userId: 1002,
    username,
    status: 'pending'
  }
})
