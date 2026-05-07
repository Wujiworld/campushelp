import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useLocation, useNavigate, useParams } from 'react-router-dom'
import { useState } from 'react'
import { Heart, Loader2 } from 'lucide-react'
import { createTicketOrder, fetchActivity, fetchActivityTickets } from '@/api/activity'
import { createSeckillTicketOrder } from '@/api/activity'
import { formatYuan } from '@/lib/utils'
import { fetchCampuses } from '@/api/product'
import { fetchLikeState, toggleLike } from '@/api/likes'
import { fetchComments, postComment } from '@/api/comments'

export function ActivityDetailPage() {
  const { id } = useParams()
  const activityId = Number(id)
  const navigate = useNavigate()
  const location = useLocation()
  const qc = useQueryClient()
  const navCampusId = (location.state as { campusId?: number } | null)?.campusId
  const [commentText, setCommentText] = useState('')

  const { data: campuses } = useQuery({ queryKey: ['campuses'], queryFn: fetchCampuses })
  const campusId = navCampusId ?? campuses?.[0]?.id

  const { data: activity, isLoading: actLoading, error: actErr } = useQuery({
    queryKey: ['activity', activityId],
    queryFn: () => fetchActivity(activityId),
    enabled: Number.isFinite(activityId),
  })

  const { data: likeState } = useQuery({
    queryKey: ['like', 'ACTIVITY', activityId],
    queryFn: () => fetchLikeState('ACTIVITY', activityId),
    enabled: Number.isFinite(activityId),
  })

  const { data: tickets, isLoading: tkLoading, error: tkErr } = useQuery({
    queryKey: ['activity-tickets', activityId],
    queryFn: () => fetchActivityTickets(activityId),
    enabled: Number.isFinite(activityId),
  })

  const { data: comments } = useQuery({
    queryKey: ['comments', 'ACTIVITY', activityId],
    queryFn: () => fetchComments('ACTIVITY', activityId),
    enabled: Number.isFinite(activityId),
  })

  const commentMut = useMutation({
    mutationFn: () =>
      postComment({ targetType: 'ACTIVITY', targetId: activityId, content: commentText.trim() }),
    onSuccess: () => {
      setCommentText('')
      qc.invalidateQueries({ queryKey: ['comments', 'ACTIVITY', activityId] })
    },
  })

  const likeMut = useMutation({
    mutationFn: () => toggleLike({ targetType: 'ACTIVITY', targetId: activityId }),
    onSuccess: (d) => {
      qc.setQueryData(['like', 'ACTIVITY', activityId], d)
      qc.invalidateQueries({ queryKey: ['activity', activityId] })
    },
  })

  const buy = useMutation({
    mutationFn: (ticketTypeId: number) => {
      if (campusId == null) throw new Error('缺少校区信息')
      // 若秒杀启用则优先尝试；未启用/无服务则自动回退到同步下单
      return createSeckillTicketOrder(campusId, ticketTypeId).catch(() => createTicketOrder(campusId, ticketTypeId))
    },
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['orders', 'mine'] })
      navigate('/orders')
    },
  })

  if (!Number.isFinite(activityId)) return <p className="text-slate-600">无效活动</p>

  if (actLoading || tkLoading) {
    return (
      <div className="flex justify-center py-20">
        <Loader2 className="h-8 w-8 animate-spin text-purple-600" />
      </div>
    )
  }

  if (actErr || tkErr || !activity || !tickets) {
    const err = actErr || tkErr
    return <p className="text-red-700">{err instanceof Error ? err.message : '加载失败'}</p>
  }

  const likes = likeState?.likeCount ?? activity.likeCount ?? 0
  const liked = likeState?.liked ?? false

  return (
    <div>
      <div className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
        <div className="flex flex-wrap items-start justify-between gap-3">
          <div>
            <h2 className="text-xl font-semibold text-slate-900">{activity.title}</h2>
            {activity.place && <p className="mt-1 text-sm text-slate-500">{activity.place}</p>}
            <p className="mt-2 text-xs text-slate-400">
              {activity.startTime?.replace('T', ' ')} — {activity.endTime?.replace('T', ' ')}
            </p>
          </div>
          <button
            type="button"
            onClick={() => likeMut.mutate()}
            disabled={likeMut.isPending}
            className="inline-flex shrink-0 items-center gap-1.5 rounded-full border border-rose-200 bg-rose-50 px-3 py-1.5 text-sm font-medium text-rose-700 hover:bg-rose-100 disabled:opacity-50"
          >
            <Heart className={`h-4 w-4 ${liked ? 'fill-rose-500 text-rose-500' : ''}`} />
            {likes}
          </button>
        </div>
        {activity.description && (
          <p className="mt-4 whitespace-pre-wrap text-sm text-slate-600">{activity.description}</p>
        )}
      </div>

      <h3 className="mt-8 text-lg font-semibold text-slate-900">选择票种</h3>
      <p className="mt-1 text-sm text-slate-600">下单后请在订单页支付；支付成功即报名成功。</p>

      <ul className="mt-6 space-y-3">
        {tickets.map((t) => {
          const left = t.stockTotal - t.stockSold
          const now = Date.now()
          const saleStart = new Date(t.saleStartTime).getTime()
          const saleEnd = new Date(t.saleEndTime).getTime()
          const inWindow = now >= saleStart && now <= saleEnd
          const off = t.status !== 'ON'
          const disabled = buy.isPending || left <= 0 || campusId == null || off || !inWindow
          return (
            <li
              key={t.id}
              className="flex flex-wrap items-center justify-between gap-3 rounded-2xl border border-slate-200 bg-white p-4 shadow-sm"
            >
              <div>
                <p className="font-medium text-slate-900">{t.name}</p>
                <p className="text-sm text-slate-500">
                  库存 {left} / {t.stockTotal} · 限购 {t.perUserLimit}
                </p>
                <p className="mt-1 text-xs text-slate-400">
                  {t.saleStartTime?.replace('T', ' ')} — {t.saleEndTime?.replace('T', ' ')}
                </p>
              </div>
              <div className="flex items-center gap-3">
                <span className="text-lg font-semibold text-purple-700">{formatYuan(t.priceCent)}</span>
                <button
                  type="button"
                  disabled={disabled}
                  onClick={() => buy.mutate(t.id)}
                  className="rounded-xl bg-purple-600 px-4 py-2 text-sm font-medium text-white hover:bg-purple-700 disabled:opacity-50"
                >
                  {left <= 0 ? '售罄' : !inWindow ? '未开售/已结束' : off ? '已下架' : '抢购'}
                </button>
              </div>
            </li>
          )
        })}
      </ul>

      <div className="mt-8 rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
        <h3 className="text-lg font-semibold text-slate-900">活动讨论</h3>
        <ul className="mt-3 space-y-3">
          {comments?.map((c) => (
            <li key={c.id} className="border-b border-slate-100 pb-3 text-sm last:border-0">
              <span className="text-xs text-slate-400">用户 {c.userId}</span>
              <p className="mt-1 text-slate-700">{c.content}</p>
            </li>
          ))}
        </ul>
        {!comments?.length && <p className="text-sm text-slate-500">暂无评论</p>}
        <textarea
          className="mt-4 w-full rounded-lg border border-slate-200 px-3 py-2 text-sm"
          rows={3}
          placeholder="发表评论…"
          value={commentText}
          onChange={(e) => setCommentText(e.target.value)}
        />
        <button
          type="button"
          disabled={commentMut.isPending || !commentText.trim()}
          onClick={() => commentMut.mutate()}
          className="mt-2 rounded-lg bg-purple-700 px-4 py-2 text-sm text-white disabled:opacity-50"
        >
          {commentMut.isPending ? '发送中…' : '发送'}
        </button>
        {commentMut.isError && (
          <p className="mt-2 text-xs text-red-600">
            {commentMut.error instanceof Error ? commentMut.error.message : '发送失败'}
          </p>
        )}
      </div>
    </div>
  )
}
