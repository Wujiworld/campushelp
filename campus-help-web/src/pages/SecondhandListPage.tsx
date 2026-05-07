import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { Eye, Heart, Loader2, Plus, Search } from 'lucide-react'
import { useEffect, useState } from 'react'
import { fetchCampuses } from '@/api/product'
import { fetchSecondhandItems } from '@/api/secondhand'
import { formatYuan } from '@/lib/utils'

export function SecondhandListPage() {
  const { data: campuses } = useQuery({ queryKey: ['campuses'], queryFn: fetchCampuses })
  const [campusId, setCampusId] = useState<number | null>(null)
  const [keyword, setKeyword] = useState('')
  const [minPrice, setMinPrice] = useState('')
  const [maxPrice, setMaxPrice] = useState('')
  const [sort, setSort] = useState<'NEWEST' | 'PRICE_ASC' | 'PRICE_DESC'>('NEWEST')

  useEffect(() => {
    if (campuses?.length && campusId === null) setCampusId(campuses[0].id)
  }, [campuses, campusId])

  const { data, isLoading, error } = useQuery({
    queryKey: ['secondhand', campusId, keyword, minPrice, maxPrice, sort],
    queryFn: () =>
      fetchSecondhandItems({
        campusId: campusId ?? undefined,
        keyword: keyword.trim() || undefined,
        minPriceCent: minPrice ? Number(minPrice) : undefined,
        maxPriceCent: maxPrice ? Number(maxPrice) : undefined,
        sort,
      }),
    enabled: campusId != null,
  })

  return (
    <div>
      <div className="mb-6 flex flex-wrap items-center justify-between gap-4">
        <div>
          <h2 className="text-xl font-semibold text-slate-900">二手市集</h2>
          <p className="mt-1 text-sm text-slate-600">支持搜索与价格筛选 · 支持 OSS 图床</p>
        </div>
        <div className="flex flex-wrap items-center gap-3">
          {campuses && (
            <select
              className="rounded-lg border border-slate-200 bg-white px-3 py-2 text-sm"
              value={campusId ?? ''}
              onChange={(e) => setCampusId(Number(e.target.value))}
            >
              {campuses.map((c) => (
                <option key={c.id} value={c.id}>
                  {c.name}
                </option>
              ))}
            </select>
          )}
          <div className="relative">
            <Search className="pointer-events-none absolute left-3 top-2.5 h-4 w-4 text-slate-400" />
            <input
              className="w-44 rounded-lg border border-slate-200 bg-white py-2 pl-9 pr-3 text-sm"
              value={keyword}
              onChange={(e) => setKeyword(e.target.value)}
              placeholder="搜索标题"
            />
          </div>
          <input
            className="w-24 rounded-lg border border-slate-200 bg-white px-3 py-2 text-sm"
            value={minPrice}
            onChange={(e) => setMinPrice(e.target.value)}
            placeholder="最低分"
            inputMode="numeric"
          />
          <input
            className="w-24 rounded-lg border border-slate-200 bg-white px-3 py-2 text-sm"
            value={maxPrice}
            onChange={(e) => setMaxPrice(e.target.value)}
            placeholder="最高分"
            inputMode="numeric"
          />
          <select
            className="rounded-lg border border-slate-200 bg-white px-3 py-2 text-sm"
            value={sort}
            onChange={(e) => setSort(e.target.value as any)}
          >
            <option value="NEWEST">最新</option>
            <option value="PRICE_ASC">价格升序</option>
            <option value="PRICE_DESC">价格降序</option>
          </select>
          <Link
            to="/secondhand/new"
            className="inline-flex items-center gap-1.5 rounded-xl bg-emerald-600 px-4 py-2 text-sm font-medium text-white shadow-sm hover:bg-emerald-700"
          >
            <Plus className="h-4 w-4" />
            发布闲置
          </Link>
        </div>
      </div>

      {isLoading && (
        <div className="flex justify-center py-16">
          <Loader2 className="h-8 w-8 animate-spin text-emerald-600" />
        </div>
      )}
      {error && (
        <p className="rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-800">
          {error instanceof Error ? error.message : '加载失败'}
        </p>
      )}

      <ul className="grid gap-4 sm:grid-cols-2">
        {data?.map((it) => (
          <li key={it.id}>
            <Link
              to={`/secondhand/${it.id}`}
              className="flex gap-4 rounded-2xl border border-slate-200 bg-white p-4 shadow-sm transition hover:border-emerald-300"
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
                <p className="mt-2 text-lg font-semibold text-emerald-700">{formatYuan(it.priceCent)}</p>
                <p className="mt-1 text-xs text-slate-500">{it.negotiable ? '可议价' : '不议价'}</p>
                <div className="mt-2 flex gap-3 text-xs text-slate-400">
                  <span className="inline-flex items-center gap-0.5">
                    <Heart className="h-3 w-3" />
                    {it.likeCount ?? 0}
                  </span>
                  <span className="inline-flex items-center gap-0.5">
                    <Eye className="h-3 w-3" />
                    {it.viewCount ?? 0}
                  </span>
                </div>
              </div>
            </Link>
          </li>
        ))}
      </ul>

      {!isLoading && !data?.length && <p className="py-12 text-center text-slate-500">暂无在售商品</p>}
    </div>
  )
}
