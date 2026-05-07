import { create } from 'zustand'
import { persist } from 'zustand/middleware'

export interface CartLine {
  skuId: number
  title: string
  unitPriceCent: number
  quantity: number
}

export interface StoreCart {
  storeId: number
  campusId: number
  lines: CartLine[]
  updatedAt: number
}

interface CartState {
  carts: Record<number, StoreCart>
  setCampusForStore: (storeId: number, campusId: number) => void
  addLine: (storeId: number, campusId: number, line: Omit<CartLine, 'quantity'>, delta: number) => void
  clearStore: (storeId: number) => void
  getStoreCart: (storeId: number) => StoreCart | null
}

export const useCartStore = create<CartState>()(
  persist(
    (set, get) => ({
      carts: {},
      setCampusForStore: (storeId, campusId) =>
        set((s) => {
          const cur = s.carts[storeId]
          const next: StoreCart = cur
            ? { ...cur, campusId, updatedAt: Date.now() }
            : { storeId, campusId, lines: [], updatedAt: Date.now() }
          return { carts: { ...s.carts, [storeId]: next } }
        }),
      addLine: (storeId, campusId, line, delta) =>
        set((s) => {
          const cur: StoreCart = s.carts[storeId] ?? {
            storeId,
            campusId,
            lines: [],
            updatedAt: Date.now(),
          }
          const lines = cur.lines.slice()
          const idx = lines.findIndex((x) => x.skuId === line.skuId)
          if (idx >= 0) {
            const q = Math.max(0, (lines[idx].quantity ?? 0) + delta)
            if (q === 0) {
              lines.splice(idx, 1)
            } else {
              lines[idx] = { ...lines[idx], quantity: q }
            }
          } else if (delta > 0) {
            lines.push({ ...line, quantity: delta })
          }
          return {
            carts: {
              ...s.carts,
              [storeId]: { storeId, campusId, lines, updatedAt: Date.now() },
            },
          }
        }),
      clearStore: (storeId) =>
        set((s) => {
          const next = { ...s.carts }
          delete next[storeId]
          return { carts: next }
        }),
      getStoreCart: (storeId) => get().carts[storeId] ?? null,
    }),
    { name: 'campus-help-cart' },
  ),
)

