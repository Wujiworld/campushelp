import { api, unwrap } from './http'
import type { ApiResult, UserProfile } from './types'

export interface ProfileStats {
  secondhandOnSale: number
  secondhandSold: number
  buyerOrderCount: number
  likesGiven: number
}

export async function fetchProfileFull() {
  const res = await api.get<ApiResult<UserProfile>>('/api/v3/users/me')
  return unwrap<UserProfile>(res)
}

export async function patchProfile(body: { nickname?: string; avatarUrl?: string; campusId?: number }) {
  const res = await api.patch<ApiResult<UserProfile>>('/api/v3/users/me', body)
  return unwrap<UserProfile>(res)
}

export async function fetchProfileStats() {
  const res = await api.get<ApiResult<ProfileStats>>('/api/v3/profile/stats')
  return unwrap<ProfileStats>(res)
}
