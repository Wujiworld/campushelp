import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { Loader2, Plus, Search } from 'lucide-react'
import { useEffect, useMemo, useState } from 'react'
import { fetchCampuses } from '@/api/product'
import { fetchAgentItems } from '@/api/agent'
import { formatYuan } from '@/lib/utils'

export function AgentListPage() {
  const { data: campuses } = useQuery({ queryKey: ['campuses'], queryFn: fetchCampuses })
  const [campusId, setCampusId] = useState<number | null>(null)
  const [keyword, setKeyword] = useState('')

  useEffect(() => {
    if (campuses?.length && campusId === null) setCampusId(campuses[0].id)
  }, [campuses, campusId])

  const { data, isLoading, error } = useQuery({
    queryKey: ['agent', campusId],
    queryFn: () => fetchAgentItems({ campusId: campusId ?? undefined }),
    enabled: campusId != null,
  })

  const filtered = useMemo(() => {
    const k = keyword.trim().toLowerCase()
    if (!k) return data ?? []
    return (data ?? []).filter((x) => x.title?.toLowerCase().includes(k))
  }, [data, keyword])

  return (
    <div>
      <div className="mb-6 flex flex-wrap items-center justify-between gap-4">
        <div>
          <h2 className="text-xl font-semibold text-slate-900">代购市场</h2>
          <p className="mt-1 text-sm text-slate-600">校内代买代办 · 支持评论与点赞</p>
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
          <Link
            to="/agent/new"
            className="inline-flex items-center gap-1.5 rounded-xl bg-sky-600 px-4 py-2 text-sm font-medium text-white shadow-sm hover:bg-sky-700"
          >
            <Plus className="h-4 w-4" />
            发布代购
          </Link>
        </div>
      </div>

      {isLoading && (
        <div className="flex justify-center py-16">
          <Loader2 className="h-8 w-8 animate-spin text-sky-600" />
        </div>
      )}
      {error && (
        <p className="rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-800">
          {error instanceof Error ? error.message : '加载失败'}
        </p>
      )}

      <ul className="grid gap-4 sm:grid-cols-2">
        {filtered.map((it) => (
          <li key={it.id}>
            <Link
              to={`/agent/${it.id}`}
              className="block rounded-2xl border border-slate-200 bg-white p-4 shadow-sm transition hover:border-sky-200"
            >
              <h3 className="font-medium text-slate-900 line-clamp-2">{it.title}</h3>
              {it.description && <p className="mt-2 text-sm text-slate-600 line-clamp-2">{it.description}</p>}
              <p className="mt-3 text-lg font-semibold text-sky-700">{formatYuan(it.priceCent)}</p>
              <p className="mt-1 text-xs text-slate-400">校区 #{it.campusId} · 卖家 {it.sellerUserId}</p>
            </Link>
          </li>
        ))}
      </ul>

      {!isLoading && !filtered.length && <p className="py-12 text-center text-slate-500">暂无代购条目</p>}
    </div>
  )
}

