import { useMutation, useQuery } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import { Loader2 } from 'lucide-react'
import { useEffect, useState } from 'react'
import { fetchCampuses } from '@/api/product'
import { publishAgentItem } from '@/api/agent'

export function AgentPublishPage() {
  const navigate = useNavigate()
  const { data: campuses } = useQuery({ queryKey: ['campuses'], queryFn: fetchCampuses })
  const [campusId, setCampusId] = useState<number | null>(null)
  const [title, setTitle] = useState('')
  const [description, setDescription] = useState('')
  const [priceCent, setPriceCent] = useState('800')

  useEffect(() => {
    if (campuses?.length && campusId === null) setCampusId(campuses[0].id)
  }, [campuses, campusId])

  const mut = useMutation({
    mutationFn: async () => publishAgentItem(campusId!, { title, description, priceCent: Number(priceCent) }),
    onSuccess: (it) => navigate(`/agent/${it.id}`),
  })

  return (
    <div className="mx-auto max-w-lg">
      <h2 className="text-xl font-semibold text-slate-900">发布代购</h2>
      <p className="mt-1 text-sm text-slate-600">学生/商家可发布；骑手不可发布。</p>

      <form
        className="mt-6 space-y-4 rounded-2xl border border-slate-200 bg-white p-5 shadow-sm"
        onSubmit={(e) => {
          e.preventDefault()
          if (!campusId || !title.trim()) return
          mut.mutate()
        }}
      >
        {campuses && (
          <label className="block text-sm">
            <span className="text-slate-600">校区</span>
            <select
              className="mt-1 w-full rounded-lg border border-slate-200 px-3 py-2"
              value={campusId ?? ''}
              onChange={(e) => setCampusId(Number(e.target.value))}
            >
              {campuses.map((c) => (
                <option key={c.id} value={c.id}>
                  {c.name}
                </option>
              ))}
            </select>
          </label>
        )}
        <label className="block text-sm">
          <span className="text-slate-600">标题</span>
          <input
            required
            className="mt-1 w-full rounded-lg border border-slate-200 px-3 py-2"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            placeholder="例如：超市代购 / 药店代买"
          />
        </label>
        <label className="block text-sm">
          <span className="text-slate-600">描述</span>
          <textarea
            className="mt-1 w-full rounded-lg border border-slate-200 px-3 py-2"
            rows={4}
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            placeholder="例如：可帮买可送到寝；备注品牌与数量"
          />
        </label>
        <label className="block text-sm">
          <span className="text-slate-600">报价（分，如 800=¥8.00）</span>
          <input
            type="number"
            required
            min={1}
            className="mt-1 w-full rounded-lg border border-slate-200 px-3 py-2"
            value={priceCent}
            onChange={(e) => setPriceCent(e.target.value)}
          />
        </label>

        <button
          type="submit"
          disabled={mut.isPending || !title.trim()}
          className="w-full rounded-xl bg-sky-600 py-3 text-sm font-medium text-white hover:bg-sky-700 disabled:opacity-50"
        >
          {mut.isPending ? (
            <span className="inline-flex items-center gap-2">
              <Loader2 className="h-4 w-4 animate-spin" /> 发布中
            </span>
          ) : (
            '发布'
          )}
        </button>

        {mut.isError && (
          <p className="text-center text-xs text-red-600">
            {mut.error instanceof Error ? mut.error.message : '发布失败'}
          </p>
        )}
      </form>
    </div>
  )
}

