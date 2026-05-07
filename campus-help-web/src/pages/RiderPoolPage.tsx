import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Loader2 } from 'lucide-react'
import { fetchRiderPool, riderTake } from '@/api/order'
import { formatYuan } from '@/lib/utils'

export function RiderPoolPage() {
  const qc = useQueryClient()
  const { data, isLoading, error, refetch } = useQuery({
    queryKey: ['orders', 'rider', 'pool'],
    queryFn: () => fetchRiderPool(50),
  })

  const take = useMutation({
    mutationFn: (id: number) => riderTake(id),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['orders', 'rider', 'pool'] })
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
      <section className="rounded-3xl border border-sky-100 bg-gradient-to-br from-sky-50 to-white px-6 py-7">
        <p className="text-xs font-semibold uppercase tracking-[0.2em] text-sky-700/80">Rider workspace</p>
        <h1 className="mt-2 text-2xl font-semibold text-slate-900">骑手首页</h1>
        <p className="mt-1 text-sm text-slate-600">先看高价值与近距离订单，提升接单效率。</p>
      </section>

      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-xl font-semibold text-slate-900">抢单池</h2>
          <p className="mt-1 text-sm text-slate-600">外卖（商家已确认）· 跑腿（已支付）· 二手配送（已支付）</p>
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
            className="flex flex-wrap items-center justify-between gap-3 rounded-2xl border border-slate-200 bg-white px-5 py-4 shadow-sm transition hover:border-sky-200 hover:shadow-md"
          >
            <div>
              <p className="font-mono text-xs text-slate-500">{o.orderNo}</p>
              <p className="mt-1 text-sm text-slate-800">
                订单 #{o.id} · {o.orderType} · 校区 {o.campusId}
              </p>
            </div>
            <div className="flex items-center gap-3">
              <span className="text-lg font-semibold text-teal-700">{formatYuan(o.totalAmountCent)}</span>
              <button
                type="button"
                disabled={take.isPending}
                onClick={() => take.mutate(o.id)}
                className="rounded-lg bg-cyan-600 px-3 py-1.5 text-sm font-medium text-white hover:bg-cyan-700 disabled:opacity-50"
              >
                接单
              </button>
            </div>
          </li>
        ))}
      </ul>

      {!data?.length && <p className="py-12 text-center text-slate-500">暂无可抢订单</p>}
    </div>
  )
}
