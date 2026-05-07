import { api, unwrap } from './http'
import type { ApiResult, ChOrder } from './types'

export interface ChActivity {
  id: number
  campusId: number
  title: string
  description?: string
  place?: string
  startTime: string
  endTime: string
  status: string
  likeCount?: number
}

export interface ChTicketType {
  id: number
  activityId: number
  name: string
  priceCent: number
  stockTotal: number
  stockSold: number
  perUserLimit: number
  saleStartTime: string
  saleEndTime: string
  status: string
}

export async function fetchActivities(campusId: number | undefined) {
  const res = await api.get<ApiResult<ChActivity[]>>('/api/v3/activities', { params: { campusId } })
  return unwrap<ChActivity[]>(res)
}

export async function fetchActivity(id: number) {
  const res = await api.get<ApiResult<ChActivity>>(`/api/v3/activities/${id}`)
  return unwrap<ChActivity>(res)
}

export async function fetchActivityTickets(activityId: number) {
  const res = await api.get<ApiResult<ChTicketType[]>>(`/api/v3/activities/${activityId}/tickets`)
  return unwrap<ChTicketType[]>(res)
}

export async function createTicketOrder(campusId: number, ticketTypeId: number) {
  const res = await api.post<ApiResult<ChOrder>>('/api/v3/activities/orders', { campusId, ticketTypeId })
  return unwrap<ChOrder>(res)
}

export async function createSeckillTicketOrder(campusId: number, ticketTypeId: number) {
  const res = await api.post('/api/v3/seckill/ticket-orders', { campusId, ticketTypeId })
  return unwrap<{ accepted: boolean; ticketTypeId: number; idempotencyKey: string }>(res)
}

export interface AdminActivityCreateBody {
  campusId: number
  title: string
  description?: string
  place?: string
  startTime: string
  endTime: string
  status?: string
}

export interface AdminTicketTypeCreateBody {
  name: string
  priceCent: number
  stockTotal: number
  perUserLimit: number
  saleStartTime: string
  saleEndTime: string
  status?: string
}

export async function adminCreateActivity(body: AdminActivityCreateBody) {
  const res = await api.post<ApiResult<ChActivity>>('/api/v3/admin/activities', body)
  return unwrap<ChActivity>(res)
}

export async function adminCreateTicketType(activityId: number, body: AdminTicketTypeCreateBody) {
  const res = await api.post<ApiResult<ChTicketType>>(`/api/v3/admin/activities/${activityId}/tickets`, body)
  return unwrap<ChTicketType>(res)
}
