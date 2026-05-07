import { useQuery } from '@tanstack/react-query'
import { ArrowLeft, Eye, Heart, Loader2 } from 'lucide-react'
import { Link } from 'react-router-dom'
import { fetchLikedSecondhandBriefs } from '@/api/likes'
import { formatYuan } from '@/lib/utils'

export function LikedSecondhandPage() {
  const { data, isLoading, error } = useQuery({
    queryKey: ['likes', 'secondhand', 'mine'],
    queryFn: fetchLikedSecondhandBriefs,
  })

  if (isLoading) {
    return (
      <div className="flex justify-center py-20">
        <Loader2 className="h-8 w-8 animate-spin text-rose-500" />
      </div>
    )
  }

  if (error) {
    return (
      <p className="text-red-700">{error instanceof Error ? error.message : '加载失败'}</p>
    )
  }

  return (
    <div className="mx-auto max-w-lg">
      <div className="mb-6 flex items-center gap-3">
        <Link
          to="/profile"
          className="inline-flex h-9 w-9 items-center justify-center rounded-full bg-slate-100 text-slate-700 hover:bg-slate-200"
        >
          <ArrowLeft className="h-4 w-4" />
        </Link>
        <div>
          <h1 className="text-lg font-semibold text-slate-900">赞过</h1>
          <p className="text-xs text-slate-500">你点赞过的闲置（最近 200 条）</p>
        </div>
      </div>

      <ul className="space-y-3">
        {data?.map((it) => (
          <li key={it.id}>
            <Link
              to={`/secondhand/${it.id}`}
              className="flex gap-4 rounded-2xl border border-slate-200 bg-white p-4 shadow-sm transition hover:border-rose-200"
            >
              <div className="h-24 w-24 shrink-0 overflow-hidden rounded-xl bg-slate-100">
                {it.coverUrl ? (
                  <img src={it.coverUrl} alt="" className="h-full w-full object-cover" />
                ) : (
                  <div className="flex h-full items-center justify-center text-xs text-slate-400">无图</div>
                )}
              </div>
              <div className="min-w-0 flex-1">
                <h3 className="font-medium text-slate-900 line-clamp-2">{it.title}</h3>
                <p className="mt-1 text-lg font-semibold text-emerald-700">{formatYuan(it.priceCent)}</p>
                <div className="mt-2 flex gap-3 text-xs text-slate-500">
                  <span className="inline-flex items-center gap-0.5">
                    <Heart className="h-3.5 w-3.5 fill-rose-400 text-rose-400" />
                    {it.likeCount ?? 0}
                  </span>
                  <span className="inline-flex items-center gap-0.5">
                    <Eye className="h-3.5 w-3.5" />
                    {it.viewCount ?? 0}
                  </span>
                </div>
              </div>
            </Link>
          </li>
        ))}
      </ul>

      {!data?.length && (
        <p className="py-12 text-center text-sm text-slate-500">还没有赞过商品，去二手市集逛逛吧</p>
      )}
    </div>
  )
}
