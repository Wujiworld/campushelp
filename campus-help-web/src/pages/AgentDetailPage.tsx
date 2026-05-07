import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useNavigate, useParams } from 'react-router-dom'
import { Heart, Loader2 } from 'lucide-react'
import { useState } from 'react'
import { createAgentOrder, fetchAgentItemDetail } from '@/api/agent'
import { fetchAddresses } from '@/api/address'
import { fetchLikeState, toggleLike } from '@/api/likes'
import { fetchComments, postComment } from '@/api/comments'
import { formatYuan } from '@/lib/utils'

export function AgentDetailPage() {
  const { id } = useParams()
  const agentId = Number(id)
  const navigate = useNavigate()
  const qc = useQueryClient()
  const [commentText, setCommentText] = useState('')
  const [addressId, setAddressId] = useState('')

  const { data: item, isLoading, error } = useQuery({
    queryKey: ['agent', agentId],
    queryFn: () => fetchAgentItemDetail(agentId),
    enabled: Number.isFinite(agentId),
  })

  const { data: likeState } = useQuery({
    queryKey: ['like', 'AGENT_ITEM', agentId],
    queryFn: () => fetchLikeState('AGENT_ITEM', agentId),
    enabled: Number.isFinite(agentId),
  })

  const likeMut = useMutation({
    mutationFn: () => toggleLike({ targetType: 'AGENT_ITEM', targetId: agentId }),
    onSuccess: (d) => qc.setQueryData(['like', 'AGENT_ITEM', agentId], d),
  })

  const { data: comments } = useQuery({
    queryKey: ['comments', 'AGENT_ITEM', agentId],
    queryFn: () => fetchComments('AGENT_ITEM', agentId),
    enabled: Number.isFinite(agentId),
  })

  const { data: addresses } = useQuery({
    queryKey: ['addresses'],
    queryFn: fetchAddresses,
  })

  const commentMut = useMutation({
    mutationFn: () => postComment({ targetType: 'AGENT_ITEM', targetId: agentId, content: commentText.trim() }),
    onSuccess: () => {
      setCommentText('')
      qc.invalidateQueries({ queryKey: ['comments', 'AGENT_ITEM', agentId] })
    },
  })

  const buy = useMutation({
    mutationFn: () =>
      createAgentOrder({
        campusId: item!.campusId,
        agentItemId: item!.id,
        addressId: Number(addressId),
      }),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['orders', 'mine'] })
      navigate('/orders')
    },
  })

  if (!Number.isFinite(agentId)) return <p className="text-slate-600">无效条目</p>
  if (isLoading) {
    return (
      <div className="flex justify-center py-20">
        <Loader2 className="h-8 w-8 animate-spin text-sky-600" />
      </div>
    )
  }
  if (error || !item) {
    return <p className="text-red-700">{error instanceof Error ? error.message : '加载失败'}</p>
  }

  const liked = likeState?.liked ?? false
  const likes = likeState?.likeCount ?? 0

  return (
    <div className="mx-auto max-w-lg">
      <div className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
        <div className="flex flex-wrap items-start justify-between gap-3">
          <div>
            <h2 className="text-xl font-semibold text-slate-900">{item.title}</h2>
            <p className="mt-2 text-lg font-semibold text-sky-700">{formatYuan(item.priceCent)}</p>
            <p className="mt-1 text-xs text-slate-400">校区 #{item.campusId} · 卖家 {item.sellerUserId}</p>
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
        {item.description && <p className="mt-4 whitespace-pre-wrap text-sm text-slate-600">{item.description}</p>}
      </div>

      {item.status === 'ON_SALE' && (
        <div className="mt-6 rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
          <p className="text-sm font-medium text-slate-800">下单</p>
          <label className="mt-3 block text-sm">
            <span className="text-slate-600">配送地址</span>
            <select
              className="mt-1 w-full rounded-lg border border-slate-200 px-3 py-2 text-sm"
              value={addressId}
              onChange={(e) => setAddressId(e.target.value)}
            >
              <option value="">请选择地址</option>
              {addresses?.map((a) => (
                <option key={a.id} value={a.id}>
                  {a.contactName} {a.contactPhone} · {a.detail}
                  {a.isDefault === 1 ? '（默认）' : ''}
                </option>
              ))}
            </select>
            <p className="mt-1 text-xs text-slate-400">
              没有地址？去{' '}
              <button
                type="button"
                className="text-amber-700 underline"
                onClick={() => navigate('/profile/addresses')}
              >
                地址管理
              </button>{' '}
              新增
            </p>
          </label>
          <button
            type="button"
            disabled={buy.isPending || !addressId}
            onClick={() => buy.mutate()}
            className="mt-4 w-full rounded-xl bg-sky-600 py-3 text-sm font-medium text-white hover:bg-sky-700 disabled:opacity-50"
          >
            {buy.isPending ? '提交中…' : '立即下单'}
          </button>
        </div>
      )}

      <div className="mt-8 rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
        <h3 className="text-lg font-semibold text-slate-900">讨论区</h3>
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
          className="mt-2 rounded-lg bg-sky-700 px-4 py-2 text-sm text-white disabled:opacity-50"
        >
          {commentMut.isPending ? '发送中…' : '发送'}
        </button>
      </div>
    </div>
  )
}

