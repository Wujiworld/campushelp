import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { ChevronRight, MapPin } from 'lucide-react'
import { useEffect, useState } from 'react'
import { fetchCampuses, fetchStores } from '@/api/product'

export function TakeoutPage() {
  const { data: campuses, isLoading: loadingCampus } = useQuery({
    queryKey: ['campuses'],
    queryFn: fetchCampuses,
  })

  const [campusId, setCampusId] = useState<number | null>(null)

  useEffect(() => {
    if (campuses?.length && campusId === null) {
      setCampusId(campuses[0].id)
    }
  }, [campuses, campusId])

  const { data, isLoading, error } = useQuery({
    queryKey: ['stores', campusId],
    queryFn: () => fetchStores(campusId!),
    enabled: campusId != null,
  })

  if (loadingCampus) {
    return (
      <div className="flex justify-center py-20 text-slate-500">
        <div className="h-8 w-8 animate-spin rounded-full border-2 border-teal-500 border-t-transparent" />
      </div>
    )
  }

  if (error) {
    return (
      <div className="rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-800">
        加载失败：{error instanceof Error ? error.message : '未知错误'}
      </div>
    )
  }

  return (
    <div>
      <div className="mb-6 flex flex-wrap items-end justify-between gap-4">
        <div>
          <h2 className="text-xl font-semibold text-slate-900">门店与食堂</h2>
          <p className="mt-1 text-sm text-slate-600">选择校区后浏览可配送门店</p>
        </div>
        <div className="flex items-center gap-2">
          <label className="text-xs text-slate-500">校区</label>
          <select
            className="rounded-lg border border-slate-200 bg-white px-3 py-2 text-sm text-slate-800 shadow-sm"
            value={campusId ?? ''}
            onChange={(e) => setCampusId(Number(e.target.value))}
          >
            {(campuses ?? []).map((c) => (
              <option key={c.id} value={c.id}>
                {c.name}
              </option>
            ))}
          </select>
        </div>
      </div>

      {isLoading ? (
        <div className="flex justify-center py-20">
          <div className="h-8 w-8 animate-spin rounded-full border-2 border-teal-500 border-t-transparent" />
        </div>
      ) : (
        <ul className="grid gap-4 sm:grid-cols-2">
          {data?.map((s) => (
            <li key={s.id}>
              <Link
                to={`/store/${s.id}?campus=${campusId ?? 1}`}
                className="group flex items-center justify-between rounded-2xl border border-slate-200/80 bg-white p-5 shadow-sm transition hover:border-teal-300 hover:shadow-md"
              >
                <div>
                  <h3 className="font-medium text-slate-900 group-hover:text-teal-700">{s.name}</h3>
                  <p className="mt-1 flex items-center gap-1 text-xs text-slate-500">
                    <MapPin className="h-3.5 w-3.5" />
                    营业 {s.openTime ?? '--'} - {s.closeTime ?? '--'}
                  </p>
                </div>
                <ChevronRight className="h-5 w-5 text-slate-400 transition group-hover:translate-x-0.5 group-hover:text-teal-600" />
              </Link>
            </li>
          ))}
        </ul>
      )}

      {!isLoading && !data?.length && (
        <p className="py-12 text-center text-slate-500">该校区暂无营业门店</p>
      )}
    </div>
  )
}
