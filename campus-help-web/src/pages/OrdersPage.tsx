import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Loader2 } from 'lucide-react'
import {
  cancelOrder,
  completeOrder,
  fetchMyOrders,
  payOrder,
} from '@/api/order'
import type { ChOrder } from '@/api/types'
import { formatYuan } from '@/lib/utils'

const typeLabel: Record<string, string> = {
  TAKEOUT: '外卖',
  ERRAND: '跑腿',
  SECONDHAND: '二手',
  TICKET: '活动票',
}

function statusLabel(o: ChOrder): string {
  const t = o.orderType
  if (t === 'TICKET' && o.status === 'COMPLETED') return '抢票成功'
  if (t === 'ERRAND' && o.status === 'PAID') return '已支付 · 待骑手接单'
  if (t === 'SECONDHAND' && o.status === 'PAID') return '已支付 · 面交可确认完成 / 配送待骑手'
  if (t === 'SECONDHAND' && o.status === 'MERCHANT_CONFIRMED') return o.status
  if (t === 'TAKEOUT' && o.status === 'PAID') return '已支付（待商家确认）'
  const base: Record<string, string> = {
    CREATED: '待支付',
    PAID: '已支付',
    MERCHANT_CONFIRMED: '商家已接单',
    RIDER_TAKEN: '骑手已接单',
    DELIVERING: '配送中',
    COMPLETED: '已完成',
    CANCELLED: '已取消',
  }
  return base[o.status] ?? o.status
}

export function OrdersPage() {
  const qc = useQueryClient()
  const { data, isLoading, error, refetch } = useQuery({
    queryKey: ['orders', 'mine'],
    queryFn: () => fetchMyOrders(40),
  })

  const pay = useMutation({
    mutationFn: (id: number) => payOrder(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['orders', 'mine'] }),
  })
  const cancel = useMutation({
    mutationFn: (id: number) => cancelOrder(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['orders', 'mine'] }),
  })
  const complete = useMutation({
    mutationFn: (id: number) => completeOrder(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['orders', 'mine'] }),
  })

  if (isLoading) {
    return (
      <div className="flex justify-center py-20">
        <Loader2 className="h-8 w-8 animate-spin text-teal-600" />
      </div>
    )
  }

  if (error) {
    return (
      <div className="rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-800">
        {error instanceof Error ? error.message : '加载失败'}
      </div>
    )
  }

  return (
    <div>
      <div className="mb-6 flex items-center justify-between">
        <div>
          <h2 className="text-xl font-semibold text-slate-900">我的订单</h2>
          <p className="mt-1 text-sm text-slate-600">外卖 · 二手 · 跑腿 · 活动票</p>
        </div>
        <button
          type="button"
          onClick={() => refetch()}
          className="rounded-lg border border-slate-200 bg-white px-3 py-1.5 text-xs text-slate-700 shadow-sm hover:bg-slate-50"
        >
          刷新
        </button>
      </div>

      <ul className="space-y-3">
        {data?.map((o) => (
          <li
            key={o.id}
            className="rounded-2xl border border-slate-200 bg-white px-5 py-4 shadow-sm"
          >
            <div className="flex flex-wrap items-start justify-between gap-2">
              <div>
                <p className="font-mono text-xs text-slate-500">{o.orderNo}</p>
                <p className="mt-1 text-xs font-medium text-teal-800">
                  {typeLabel[o.orderType] ?? o.orderType}
                </p>
                <p className="mt-1 text-sm text-slate-800">{statusLabel(o)}</p>
                <p className="mt-1 text-xs text-slate-500">支付状态：{o.payStatus}</p>
              </div>
              <div className="text-right">
                <p className="text-lg font-semibold text-teal-700">{formatYuan(o.totalAmountCent)}</p>
                {o.deliveryFeeCent > 0 && (
                  <p className="text-xs text-slate-500">含配送 {formatYuan(o.deliveryFeeCent)}</p>
                )}
              </div>
            </div>
            <div className="mt-4 flex flex-wrap gap-2">
              {o.status === 'CREATED' && o.payStatus === 'UNPAID' && (
                <>
                  <button
                    type="button"
                    disabled={pay.isPending}
                    onClick={() => pay.mutate(o.id)}
                    className="rounded-lg bg-teal-600 px-3 py-1.5 text-xs font-medium text-white hover:bg-teal-700 disabled:opacity-50"
                  >
                    去支付
                  </button>
                  <button
                    type="button"
                    disabled={cancel.isPending}
                    onClick={() => cancel.mutate(o.id)}
                    className="rounded-lg border border-slate-200 px-3 py-1.5 text-xs text-slate-700 hover:bg-slate-50 disabled:opacity-50"
                  >
                    取消订单
                  </button>
                </>
              )}
              {o.status === 'DELIVERING' && (
                <button
                  type="button"
                  disabled={complete.isPending}
                  onClick={() => complete.mutate(o.id)}
                  className="rounded-lg bg-cyan-600 px-3 py-1.5 text-xs font-medium text-white hover:bg-cyan-700 disabled:opacity-50"
                >
                  确认收货
                </button>
              )}
              {o.status === 'PAID' && o.orderType === 'SECONDHAND' && o.payStatus === 'PAID' && (
                <button
                  type="button"
                  disabled={complete.isPending}
                  onClick={() => complete.mutate(o.id)}
                  className="rounded-lg border border-emerald-300 bg-emerald-50 px-3 py-1.5 text-xs font-medium text-emerald-900 hover:bg-emerald-100 disabled:opacity-50"
                >
                  确认完成（面交）
                </button>
              )}
            </div>
          </li>
        ))}
      </ul>

      {!data?.length && <p className="py-12 text-center text-slate-500">暂无订单</p>}
    </div>
  )
}
