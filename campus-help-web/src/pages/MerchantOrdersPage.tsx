import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Loader2 } from 'lucide-react'
import { fetchMerchantPending, merchantConfirm } from '@/api/order'
import { formatYuan } from '@/lib/utils'

export function MerchantOrdersPage() {
  const qc = useQueryClient()
  const { data, isLoading, error, refetch } = useQuery({
    queryKey: ['orders', 'merchant', 'pending'],
    queryFn: () => fetchMerchantPending(50),
  })

  const m = useMutation({
    mutationFn: (id: number) => merchantConfirm(id),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['orders', 'merchant', 'pending'] })
    },
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
    <div className="space-y-6">
      <section className="rounded-3xl border border-emerald-100 bg-gradient-to-br from-emerald-50 to-white px-6 py-7">
        <p className="text-xs font-semibold uppercase tracking-[0.2em] text-emerald-700/80">Merchant workspace</p>
        <h1 className="mt-2 text-2xl font-semibold text-slate-900">商家首页</h1>
        <p className="mt-1 text-sm text-slate-600">优先处理已支付订单，减少超时与取消率。</p>
      </section>

      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-xl font-semibold text-slate-900">待处理订单</h2>
          <p className="mt-1 text-sm text-slate-600">已支付、待您确认的外卖订单</p>
        </div>
        <button
          type="button"
          onClick={() => refetch()}
          className="rounded-lg border border-slate-200 bg-white px-3 py-1.5 text-xs text-slate-700 hover:bg-slate-50"
        >
          刷新
        </button>
      </div>

      <ul className="space-y-3">
        {data?.map((o) => (
          <li
            key={o.id}
            className="flex flex-wrap items-center justify-between gap-3 rounded-2xl border border-slate-200 bg-white px-5 py-4 shadow-sm transition hover:border-emerald-200 hover:shadow-md"
          >
            <div>
              <p className="font-mono text-xs text-slate-500">{o.orderNo}</p>
              <p className="mt-1 text-sm text-slate-800">门店订单 #{o.id}</p>
            </div>
            <div className="flex items-center gap-3">
              <span className="text-lg font-semibold text-teal-700">{formatYuan(o.totalAmountCent)}</span>
              <button
                type="button"
                disabled={m.isPending}
                onClick={() => m.mutate(o.id)}
                className="rounded-lg bg-teal-600 px-3 py-1.5 text-sm font-medium text-white hover:bg-teal-700 disabled:opacity-50"
              >
                确认接单
              </button>
            </div>
          </li>
        ))}
      </ul>

      {!data?.length && <p className="py-12 text-center text-slate-500">暂无待处理订单</p>}
    </div>
  )
}
