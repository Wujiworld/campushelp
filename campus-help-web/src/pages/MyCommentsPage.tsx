import { useQuery } from '@tanstack/react-query'
import { ArrowLeft, Loader2 } from 'lucide-react'
import { Link } from 'react-router-dom'
import { fetchMyComments } from '@/api/comments'

export function MyCommentsPage() {
  const { data, isLoading, error } = useQuery({
    queryKey: ['comments', 'mine'],
    queryFn: () => fetchMyComments(0, 50),
  })

  if (isLoading) {
    return (
      <div className="flex justify-center py-20">
        <Loader2 className="h-8 w-8 animate-spin text-slate-600" />
      </div>
    )
  }
  if (error) {
    return <p className="text-red-700">{error instanceof Error ? error.message : '加载失败'}</p>
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
          <h1 className="text-lg font-semibold text-slate-900">我的评论</h1>
          <p className="text-xs text-slate-500">最近 50 条</p>
        </div>
      </div>

      <ul className="space-y-3">
        {data?.map((c) => (
          <li key={c.id} className="rounded-2xl border border-slate-200 bg-white p-4 shadow-sm">
            <p className="text-xs text-slate-400">
              {c.targetType} #{c.targetId} · {c.createdAt?.replace('T', ' ')}
            </p>
            <p className="mt-2 text-sm text-slate-700">{c.content}</p>
          </li>
        ))}
      </ul>

      {!data?.length && <p className="py-12 text-center text-sm text-slate-500">还没有发表过评论</p>}
    </div>
  )
}

