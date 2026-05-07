import { api, unwrap } from './http'
import type { ApiResult } from './types'
import type { SecondhandBrief } from './secondhand'
import type { ChActivity } from './activity'

export interface AgentItem {
  id: number
  sellerUserId: number
  campusId: number
  title: string
  description?: string
  priceCent: number
  status: string
  createdAt?: string
}

export interface LikeState {
  liked: boolean
  likeCount: number
}

export async function fetchLikeState(targetType: string, targetId: number) {
  const res = await api.get<ApiResult<LikeState>>('/api/v3/likes/state', {
    params: { targetType, targetId },
  })
  return unwrap<LikeState>(res)
}

export async function toggleLike(body: { targetType: string; targetId: number }) {
  const res = await api.post<ApiResult<LikeState>>('/api/v3/likes/toggle', body)
  return unwrap<LikeState>(res)
}

export async function fetchLikedSecondhandBriefs() {
  const res = await api.get<ApiResult<SecondhandBrief[]>>('/api/v3/likes/mine/secondhand')
  return unwrap<SecondhandBrief[]>(res)
}

export async function fetchLikedActivities() {
  const res = await api.get<ApiResult<ChActivity[]>>('/api/v3/likes/mine/activities')
  return unwrap<ChActivity[]>(res)
}

export async function fetchLikedAgentItems() {
  const res = await api.get<ApiResult<AgentItem[]>>('/api/v3/likes/mine/agent')
  return unwrap<AgentItem[]>(res)
}
