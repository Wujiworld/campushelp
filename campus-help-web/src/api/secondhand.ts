import { api, unwrap } from './http'
import type { ApiResult } from './types'
import type { ChOrder } from './types'

export interface SecondhandBrief {
  id: number
  campusId: number
  title: string
  priceCent: number
  negotiable: number
  status: string
  createdAt?: string
  coverUrl?: string | null
  likeCount?: number
  viewCount?: number
}

export interface SecondhandDetailResponse {
  item: {
    id: number
    sellerUserId: number
    campusId: number
    title: string
    description?: string
    priceCent: number
    negotiable: number
    status: string
    likeCount?: number
    viewCount?: number
  }
  imageUrls: string[]
}

export async function fetchSecondhandItems(params: {
  campusId?: number
  status?: string
  keyword?: string
  minPriceCent?: number
  maxPriceCent?: number
  sort?: 'NEWEST' | 'PRICE_ASC' | 'PRICE_DESC'
}) {
  const res = await api.get<ApiResult<SecondhandBrief[]>>('/api/v3/secondhand/items', {
    params: {
      campusId: params.campusId,
      status: params.status ?? 'ON_SALE',
      keyword: params.keyword,
      minPriceCent: params.minPriceCent,
      maxPriceCent: params.maxPriceCent,
      sort: params.sort ?? 'NEWEST',
    },
  })
  return unwrap<SecondhandBrief[]>(res)
}

export async function fetchSecondhandDetail(id: number) {
  const res = await api.get<ApiResult<SecondhandDetailResponse>>(`/api/v3/secondhand/items/${id}`)
  return unwrap<SecondhandDetailResponse>(res)
}

export async function publishSecondhandItem(campusId: number, body: Record<string, unknown>) {
  const res = await api.post<ApiResult<{ id: number }>>('/api/v3/secondhand/items', body, {
    params: { campusId },
  })
  return unwrap<{ id: number }>(res)
}

export async function offlineSecondhandItem(id: number) {
  const res = await api.post<ApiResult<null>>(`/api/v3/secondhand/items/${id}/offline`)
  return unwrap<null>(res)
}

export async function createSecondhandOrder(body: {
  campusId: number
  itemId: number
  deliveryMode: string
  addressId?: number
}) {
  const res = await api.post<ApiResult<ChOrder>>('/api/v3/secondhand/orders', body)
  return unwrap<ChOrder>(res)
}

export async function fetchMySecondhandItems() {
  const res = await api.get<ApiResult<SecondhandBrief[]>>('/api/v3/secondhand/my/items')
  return unwrap<SecondhandBrief[]>(res)
}
