import { api, unwrap } from './http'
import type { ApiResult, ChOrder } from './types'

export interface ErrandOrderBody {
  campusId: number
  addressId?: number
  errandType: string
  pickupAddress?: string
  pickupCode?: string
  listText?: string
  feeCent: number
  remark?: string
}

export async function createErrandOrder(body: ErrandOrderBody) {
  const res = await api.post<ApiResult<ChOrder>>('/api/v3/errands/orders', body)
  return unwrap<ChOrder>(res)
}
