import { useMutation, useQueries, useQuery } from '@tanstack/react-query'
import { useParams, useSearchParams } from 'react-router-dom'
import { Loader2, Minus, Plus, ShoppingCart, Trash2 } from 'lucide-react'
import { useState } from 'react'
import { fetchProducts, fetchSkus } from '@/api/product'
import { createOrder } from '@/api/order'
import { formatYuan } from '@/lib/utils'
import type { ChProductSku, OrderCreateRequest } from '@/api/types'
import { fetchAddresses } from '@/api/address'
import { useCartStore } from '@/store/cartStore'

const DELIVERY_FEE = 300

export function StorePage() {
  const { storeId } = useParams<{ storeId: string }>()
  const [searchParams] = useSearchParams()
  const sid = Number(storeId)
  const campusId = Number(searchParams.get('campus') ?? '1')

  const cart = useCartStore((s) => s.getStoreCart(sid))
  const addLine = useCartStore((s) => s.addLine)
  const clearStore = useCartStore((s) => s.clearStore)

  const { data: products, isLoading: loadingProducts } = useQuery({
    queryKey: ['products', sid],
    queryFn: () => fetchProducts(sid),
    enabled: Number.isFinite(sid),
  })

  const skuQueries = useQueries({
    queries: (products ?? []).map((p) => ({
      queryKey: ['skus', p.id] as const,
      queryFn: () => fetchSkus(p.id),
      enabled: !!products?.length,
    })),
  })

  const [msg, setMsg] = useState<string | null>(null)

  const { data: addresses } = useQuery({
    queryKey: ['addresses'],
    queryFn: fetchAddresses,
  })

  const orderMutation = useMutation({
    mutationFn: createOrder,
    onSuccess: () => {
      setMsg('下单成功，请到「我的订单」完成支付。')
      clearStore(sid)
    },
    onError: (e: Error) => setMsg(e.message),
  })

  if (!Number.isFinite(sid)) {
    return <p className="text-red-700">无效门店</p>
  }

  if (loadingProducts) {
    return (
      <div className="flex justify-center py-20">
        <Loader2 className="h-8 w-8 animate-spin text-teal-600" />
      </div>
    )
  }

  function buildOrder(): OrderCreateRequest | null {
    if (!cart?.lines?.length) return null
    const addr = (addresses ?? []).find((a) => a.isDefault === 1) ?? (addresses ?? [])[0]
    if (!addr) {
      setMsg('请先到「我的-收货地址」新增地址并设置默认地址')
      return null
    }
    return {
      orderType: 'TAKEOUT',
      campusId,
      storeId: sid,
      addressId: addr.id,
      deliveryFeeCent: DELIVERY_FEE,
      remark: 'App 下单',
      items: cart.lines.map((l) => ({
        skuId: l.skuId,
        title: l.title,
        unitPriceCent: l.unitPriceCent,
        quantity: l.quantity,
      })),
    }
  }

  function submit() {
    setMsg(null)
    const body = buildOrder()
    if (!body) {
      setMsg('请先选择商品数量')
      return
    }
    orderMutation.mutate(body)
  }

  return (
    <div>
      <h2 className="text-xl font-semibold text-slate-900">门店菜单</h2>
      <p className="mt-1 text-sm text-slate-600">
        门店 #{sid} · 校区 #{campusId}
      </p>

      <div className="mt-8 space-y-8">
        {(products ?? []).map((p, i) => {
          const qres = skuQueries[i]
          const skus = qres?.data
          const loading = qres?.isLoading
          return (
            <section key={p.id} className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
              <h3 className="font-medium text-slate-900">{p.name}</h3>
              <p className="text-xs text-slate-500">{p.category}</p>
              {loading ? (
                <div className="mt-3 text-sm text-slate-500">加载规格…</div>
              ) : (
                <ul className="mt-4 space-y-3">
                  {(skus ?? []).map((sku: ChProductSku) => (
                    <li
                      key={sku.id}
                      className="flex flex-wrap items-center justify-between gap-3 rounded-xl border border-slate-100 bg-slate-50 px-3 py-2"
                    >
                      <div>
                        <span className="text-sm text-slate-800">{sku.skuName}</span>
                        <span className="ml-2 text-sm font-medium text-teal-700">
                          {formatYuan(sku.priceCent)}
                        </span>
                        <span className="ml-2 text-xs text-slate-500">库存 {sku.stock}</span>
                      </div>
                      <div className="flex items-center gap-2">
                        <button
                          type="button"
                          className="rounded-lg border border-slate-200 p-1 text-slate-600 hover:bg-slate-100"
                          onClick={() =>
                            addLine(
                              sid,
                              campusId,
                              {
                                skuId: sku.id,
                                title: `${p.name} · ${sku.skuName}`,
                                unitPriceCent: sku.priceCent,
                              },
                              -1,
                            )
                          }
                        >
                          <Minus className="h-4 w-4" />
                        </button>
                        <span className="w-6 text-center text-sm tabular-nums">
                          {cart?.lines?.find((l) => l.skuId === sku.id)?.quantity ?? 0}
                        </span>
                        <button
                          type="button"
                          className="rounded-lg border border-slate-200 p-1 text-slate-600 hover:bg-slate-100"
                          onClick={() =>
                            addLine(
                              sid,
                              campusId,
                              {
                                skuId: sku.id,
                                title: `${p.name} · ${sku.skuName}`,
                                unitPriceCent: sku.priceCent,
                              },
                              1,
                            )
                          }
                        >
                          <Plus className="h-4 w-4" />
                        </button>
                      </div>
                    </li>
                  ))}
                </ul>
              )}
            </section>
          )
        })}
      </div>

      <div className="mt-10 flex flex-col items-stretch gap-3 sm:flex-row sm:items-center sm:justify-between">
        {msg && (
          <p className="text-sm text-teal-800" role="status">
            {msg}
          </p>
        )}
        <div className="rounded-2xl border border-slate-200 bg-white p-4 shadow-sm">
          <div className="flex items-center justify-between gap-3">
            <p className="text-sm font-medium text-slate-900">
              购物车：{(cart?.lines?.reduce((a, b) => a + b.quantity, 0) ?? 0) || 0} 件
            </p>
            <button
              type="button"
              className="inline-flex items-center gap-1 rounded-lg border border-slate-200 px-2 py-1 text-xs text-slate-600 hover:bg-slate-50"
              onClick={() => clearStore(sid)}
              disabled={!cart?.lines?.length}
            >
              <Trash2 className="h-3.5 w-3.5" />
              清空
            </button>
          </div>

          {cart?.lines?.length ? (
            <ul className="mt-3 space-y-2">
              {cart.lines.map((l) => (
                <li key={l.skuId} className="flex items-center justify-between gap-3 text-sm">
                  <span className="min-w-0 flex-1 truncate text-slate-700">{l.title}</span>
                  <span className="tabular-nums text-slate-500">×{l.quantity}</span>
                  <span className="tabular-nums font-medium text-slate-900">
                    {formatYuan(l.unitPriceCent * l.quantity)}
                  </span>
                </li>
              ))}
            </ul>
          ) : (
            <p className="mt-3 text-xs text-slate-500">还没有加购，去选点想吃的吧</p>
          )}

          <div className="mt-4 flex items-center justify-between">
            <p className="text-sm text-slate-700">
              合计：{' '}
              <span className="font-semibold text-teal-700">
                {formatYuan(
                  (cart?.lines?.reduce((sum, l) => sum + l.unitPriceCent * l.quantity, 0) ?? 0) + DELIVERY_FEE,
                )}
              </span>
              <span className="ml-2 text-xs text-slate-400">含配送 {formatYuan(DELIVERY_FEE)}</span>
            </p>
            <button
              type="button"
              onClick={submit}
              disabled={orderMutation.isPending || !cart?.lines?.length}
              className="inline-flex items-center justify-center gap-2 rounded-xl bg-gradient-to-r from-teal-500 to-cyan-600 px-5 py-2.5 text-sm font-medium text-white disabled:opacity-50"
            >
              {orderMutation.isPending ? (
                <Loader2 className="h-4 w-4 animate-spin" />
              ) : (
                <ShoppingCart className="h-4 w-4" />
              )}
              去下单
            </button>
          </div>
        </div>
      </div>
    </div>
  )
}
