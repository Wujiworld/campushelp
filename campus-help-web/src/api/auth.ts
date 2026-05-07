import { api, unwrap } from './http'
import type { LoginResponse, UserProfile } from './types'

export async function login(phone: string, password: string) {
  const res = await api.post('/api/v3/auth/login', { phone, password })
  return unwrap<LoginResponse>(res)
}

export async function fetchMe() {
  const res = await api.get('/api/v3/auth/me')
  return unwrap<UserProfile>(res)
}
