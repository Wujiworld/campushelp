import { api, unwrap } from './http'
import type { ChOrder, OrderCreateRequest } from './types'

export async function createOrder(body: OrderCreateRequest) {
  const res = await api.post('/api/v3/orders', body)
  return unwrap<ChOrder>(res)
}

export async function fetchMyOrders(limit = 20) {
  const res = await api.get('/api/v3/orders/mine', { params: { limit } })
  return unwrap<ChOrder[]>(res)
}

export async function fetchMerchantPending(limit = 50) {
  const res = await api.get('/api/v3/orders/merchant/pending', { params: { limit } })
  return unwrap<ChOrder[]>(res)
}

export async function fetchRiderPool(limit = 50) {
  const res = await api.get('/api/v3/orders/rider/pool', { params: { limit } })
  return unwrap<ChOrder[]>(res)
}

export async function payOrder(id: number) {
  const res = await api.post(`/api/v3/orders/${id}/pay`)
  return unwrap<ChOrder>(res)
}

export async function cancelOrder(id: number) {
  const res = await api.post(`/api/v3/orders/${id}/cancel`)
  return unwrap<ChOrder>(res)
}

export async function completeOrder(id: number) {
  const res = await api.post(`/api/v3/orders/${id}/complete`)
  return unwrap<ChOrder>(res)
}

export async function merchantConfirm(id: number) {
  const res = await api.post(`/api/v3/orders/${id}/merchant/confirm`)
  return unwrap<ChOrder>(res)
}

export async function riderTake(id: number) {
  const res = await api.post(`/api/v3/orders/${id}/rider/take`)
  return unwrap<ChOrder>(res)
}

export async function riderPickup(id: number) {
  const res = await api.post(`/api/v3/orders/${id}/rider/pickup`)
  return unwrap<ChOrder>(res)
}
