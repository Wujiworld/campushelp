import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { Loader2 } from 'lucide-react'
import { useEffect, useState } from 'react'
import { fetchCampuses } from '@/api/product'
import { fetchActivities } from '@/api/activity'

export function ActivitiesPage() {
  const { data: campuses } = useQuery({ queryKey: ['campuses'], queryFn: fetchCampuses })
  const [campusId, setCampusId] = useState<number | null>(null)

  useEffect(() => {
    if (campuses?.length && campusId === null) setCampusId(campuses[0].id)
  }, [campuses, campusId])

  const { data, isLoading, error } = useQuery({
    queryKey: ['activities', campusId],
    queryFn: () => fetchActivities(campusId ?? undefined),
    enabled: campusId != null,
  })

  return (
    <div>
      <div className="mb-6 flex flex-wrap items-center justify-between gap-4">
        <div>
          <h2 className="text-xl font-semibold text-slate-900">校园活动</h2>
          <p className="mt-1 text-sm text-slate-600">公开活动与票种，下单后库存原子扣减</p>
        </div>
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
      </div>

      {isLoading && (
        <div className="flex justify-center py-16">
          <Loader2 className="h-8 w-8 animate-spin text-purple-600" />
        </div>
      )}
      {error && (
        <p className="text-red-700">{error instanceof Error ? error.message : '加载失败'}</p>
      )}

      <ul className="space-y-3">
        {data?.map((a) => (
          <li key={a.id}>
            <Link
              to={`/activities/${a.id}`}
              state={{ campusId: a.campusId }}
              className="block rounded-2xl border border-slate-200 bg-white p-5 shadow-sm transition hover:border-purple-300"
            >
              <h3 className="font-semibold text-slate-900">{a.title}</h3>
              {a.place && <p className="mt-1 text-sm text-slate-500">{a.place}</p>}
              <p className="mt-2 text-xs text-slate-400">
                {a.startTime?.replace('T', ' ')} — {a.endTime?.replace('T', ' ')}
                {a.likeCount != null && a.likeCount > 0 && (
                  <span className="ml-2 text-rose-500">♥ {a.likeCount}</span>
                )}
              </p>
            </Link>
          </li>
        ))}
      </ul>

      {!isLoading && !data?.length && <p className="py-12 text-center text-slate-500">暂无活动</p>}
    </div>
  )
}
