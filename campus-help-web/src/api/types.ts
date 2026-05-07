/** 与后端 ApiResult 对齐 */
export interface ApiResult<T> {
  success: boolean
  code: string
  message: string
  data: T
  timestamp: number
  requestId?: string
  apiVersion?: string
}

export interface LoginResponse {
  token: string
  userId: number
  expiresInMs: number
  roles: string[]
}

export interface UserProfile {
  userId: number
  phone: string
  nickname: string | null
  roles: string[]
  avatarUrl?: string | null
  campusId?: number | null
  createdAt?: string | null
}

export interface ChCampus {
  id: number
  code: string
  name: string
  city?: string
  status: number
}

export interface ChStore {
  id: number
  name: string
  campusId: number
  type: number
  status: number
  openTime?: string
  closeTime?: string
  notice?: string
}

export interface ChProduct {
  id: number
  storeId: number
  name: string
  category?: string
  status: number
}

export interface ChProductSku {
  id: number
  productId: number
  skuName: string
  priceCent: number
  stock: number
  status: number
}

export interface ChOrder {
  id: number
  orderNo: string
  orderType: string
  userId: number
  storeId: number | null
  campusId: number
  status: string
  payStatus: string
  totalAmountCent: number
  payAmountCent: number
  deliveryFeeCent: number
  remark?: string
  createdAt?: string
  expireAt?: string
}

export interface OrderItemRequest {
  skuId: number
  title: string
  unitPriceCent: number
  quantity: number
}

export interface OrderCreateRequest {
  orderType: string
  campusId: number
  storeId: number
  addressId: number
  deliveryFeeCent: number
  remark?: string
  items: OrderItemRequest[]
}
