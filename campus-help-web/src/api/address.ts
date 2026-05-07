import { api, unwrap } from './http'
import type { ApiResult } from './types'

export interface ChAddress {
  id: number
  userId: number
  campusId: number
  buildingId?: number | null
  contactName: string
  contactPhone: string
  detail: string
  label?: string | null
  isDefault: number
  createdAt?: string
  updatedAt?: string
}

export interface AddressRequestBody {
  campusId: number
  buildingId?: number
  contactName: string
  contactPhone: string
  detail: string
  label?: string
  defaultAddress?: boolean
}

export async function fetchAddresses() {
  const res = await api.get<ApiResult<ChAddress[]>>('/api/v3/addresses')
  return unwrap<ChAddress[]>(res)
}

export async function createAddress(body: AddressRequestBody) {
  const res = await api.post<ApiResult<ChAddress>>('/api/v3/addresses', body)
  return unwrap<ChAddress>(res)
}

export async function updateAddress(id: number, body: AddressRequestBody) {
  const res = await api.put<ApiResult<ChAddress>>(`/api/v3/addresses/${id}`, body)
  return unwrap<ChAddress>(res)
}

export async function deleteAddress(id: number) {
  const res = await api.delete<ApiResult<null>>(`/api/v3/addresses/${id}`)
  return unwrap<null>(res)
}

export async function setDefaultAddress(id: number) {
  const res = await api.post<ApiResult<ChAddress>>(`/api/v3/addresses/${id}/default`)
  return unwrap<ChAddress>(res)
}
