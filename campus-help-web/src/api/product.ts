import { api, unwrap } from './http'
import type { ChCampus, ChProduct, ChProductSku, ChStore } from './types'

export async function fetchCampuses() {
  const res = await api.get('/api/v3/campuses')
  return unwrap<ChCampus[]>(res)
}

export async function fetchStores(campusId: number) {
  const res = await api.get('/api/v3/stores', { params: { campusId } })
  return unwrap<ChStore[]>(res)
}

export async function fetchProducts(storeId: number) {
  const res = await api.get(`/api/v3/stores/${storeId}/products`)
  return unwrap<ChProduct[]>(res)
}

export async function fetchSkus(productId: number) {
  const res = await api.get(`/api/v3/products/${productId}/skus`)
  return unwrap<ChProductSku[]>(res)
}
