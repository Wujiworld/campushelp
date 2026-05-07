import { api, unwrap } from './http'

export async function toggleFollow(followeeUserId: number) {
  const res = await api.post('/api/v3/follows/toggle', null, { params: { followeeUserId } })
  return unwrap<{ followeeUserId: number; following: boolean }>(res)
}

export async function fetchMyFollows() {
  const res = await api.get('/api/v3/follows/mine')
  return unwrap<number[]>(res)
}

export async function fetchMyFans() {
  const res = await api.get('/api/v3/follows/fans')
  return unwrap<number[]>(res)
}
