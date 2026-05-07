import { api, unwrap } from './http'
import type { ApiResult } from './types'

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

export interface AgentOrderCreateBody {
  campusId: number
  agentItemId: number
  addressId: number
  remark?: string
}

export async function fetchAgentItems(params: { campusId?: number; status?: string }) {
  const res = await api.get<ApiResult<AgentItem[]>>('/api/v3/agent/items', {
    params: { campusId: params.campusId, status: params.status ?? 'ON_SALE' },
  })
  return unwrap<AgentItem[]>(res)
}

export async function fetchAgentItemDetail(id: number) {
  const res = await api.get<ApiResult<AgentItem>>(`/api/v3/agent/items/${id}`)
  return unwrap<AgentItem>(res)
}

export async function publishAgentItem(campusId: number, body: { title: string; description?: string; priceCent: number }) {
  const res = await api.post<ApiResult<AgentItem>>('/api/v3/agent/items', body, { params: { campusId } })
  return unwrap<AgentItem>(res)
}

export async function offlineAgentItem(id: number) {
  const res = await api.post<ApiResult<null>>(`/api/v3/agent/items/${id}/offline`)
  return unwrap<null>(res)
}

export async function createAgentOrder(body: AgentOrderCreateBody) {
  const res = await api.post<ApiResult<any>>('/api/v3/agent/orders', body)
  return unwrap<any>(res)
}

