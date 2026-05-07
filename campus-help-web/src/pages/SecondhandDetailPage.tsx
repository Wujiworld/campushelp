import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useNavigate, useParams } from 'react-router-dom'
import { Eye, Heart, Loader2 } from 'lucide-react'
import { useState } from 'react'
import { createSecondhandOrder, fetchSecondhandDetail } from '@/api/secondhand'
import { fetchAddresses } from '@/api/address'
import { fetchLikeState, toggleLike } from '@/api/likes'
import { fetchComments, postComment } from '@/api/comments'
import { formatYuan } from '@/lib/utils'

export function SecondhandDetailPage() {
  const { id } = useParams()
  const itemId = Number(id)
  const navigate = useNavigate()
  const qc = useQueryClient()
  const [deliveryMode, setDeliveryMode] = useState<'MEETUP' | 'DELIVERY'>('MEETUP')
  const [addressId, setAddressId] = useState('')
  const [commentText, setCommentText] = useState('')

  const { data, isLoading, error } = useQuery({
    queryKey: ['secondhand', itemId],
    queryFn: () => fetchSecondhandDetail(itemId),
    enabled: Number.isFinite(itemId),
  })

  const { data: likeState } = useQuery({
    queryKey: ['like', 'SECONDHAND_ITEM', itemId],
    queryFn: () => fetchLikeState('SECONDHAND_ITEM', itemId),
    enabled: Number.isFinite(itemId),
  })

  const { data: addresses } = useQuery({
    queryKey: ['addresses'],
    queryFn: fetchAddresses,
    enabled: deliveryMode === 'DELIVERY',
  })

  const { data: comments } = useQuery({
    queryKey: ['comments', 'SECONDHAND_ITEM', itemId],
    queryFn: () => fetchComments('SECONDHAND_ITEM', itemId),
    enabled: Number.isFinite(itemId),
  })

  const commentMut = useMutation({
    mutationFn: () =>
      postComment({ targetType: 'SECONDHAND_ITEM', targetId: itemId, content: commentText.trim() }),
    onSuccess: () => {
      setCommentText('')
      qc.invalidateQueries({ queryKey: ['comments', 'SECONDHAND_ITEM', itemId] })
    },
  })

  const likeMut = useMutation({
    mutationFn: () => toggleLike({ targetType: 'SECONDHAND_ITEM', targetId: itemId }),
    onSuccess: (d) => {
      qc.setQueryData(['like', 'SECONDHAND_ITEM', itemId], d)
      qc.invalidateQueries({ queryKey: ['secondhand', itemId] })
    },
  })

  const buy = useMutation({
    mutationFn: () =>
      createSecondhandOrder({
        campusId: data!.item.campusId,
        itemId: data!.item.id,
        deliveryMode,
        addressId: deliveryMode === 'DELIVERY' ? Number(addressId) : undefined,
      }),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['orders', 'mine'] })
      navigate('/orders')
    },
  })

  if (!Number.isFinite(itemId)) {
    return <p className="text-slate-600">无效商品</p>
  }

  if (isLoading) {
    return (
      <div className="flex justify-center py-20">
        <Loader2 className="h-8 w-8 animate-spin text-emerald-600" />
      </div>
    )
  }

  if (error || !data) {
    return <p className="text-red-700">{error instanceof Error ? error.message : '加载失败'}</p>
  }

  const { item, imageUrls } = data
  const views = item.viewCount ?? 0
  const likes = likeState?.likeCount ?? item.likeCount ?? 0
  const liked = likeState?.liked ?? false

  return (
    <div className="mx-auto max-w-lg">
      <div className="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm">
        <div className="flex gap-2 overflow-x-auto p-3">
          {imageUrls?.length ? (
            imageUrls.map((u) => (
              <img key={u} src={u} alt="" className="h-48 w-48 shrink-0 rounded-xl object-cover" />
            ))
          ) : (
            <div className="flex h-48 w-full items-center justify-center bg-slate-100 text-slate-400">暂无图片</div>
          )}
        </div>
        <div className="border-t border-slate-100 p-5">
          <div className="flex flex-wrap items-center justify-between gap-3">
            <h1 className="text-xl font-semibold text-slate-900">{item.title}</h1>
            <button
              type="button"
              onClick={() => likeMut.mutate()}
              disabled={likeMut.isPending}
              className="inline-flex items-center gap-1.5 rounded-full border border-rose-200 bg-rose-50 px-3 py-1.5 text-sm font-medium text-rose-700 transition hover:bg-rose-100 disabled:opacity-50"
            >
              <Heart
                className={`h-4 w-4 ${liked ? 'fill-rose-500 text-rose-500' : ''}`}
              />
              {likes}
            </button>
          </div>
          <div className="mt-2 flex flex-wrap items-center gap-4 text-sm text-slate-500">
            <p className="text-2xl font-bold text-emerald-700">{formatYuan(item.priceCent)}</p>
            <span className="inline-flex items-center gap-1 text-xs">
              <Eye className="h-3.5 w-3.5" />
              {views} 浏览
            </span>
          </div>
          {item.description && (
            <p className="mt-4 whitespace-pre-wrap text-sm text-slate-600">{item.description}</p>
          )}
        </div>
      </div>

      {item.status === 'ON_SALE' && (
        <div className="mt-6 rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
          <p className="text-sm font-medium text-slate-800">购买方式</p>
          <div className="mt-3 flex gap-3">
            <label className="flex items-center gap-2 text-sm">
              <input
                type="radio"
                checked={deliveryMode === 'MEETUP'}
                onChange={() => setDeliveryMode('MEETUP')}
              />
              当面交割
            </label>
            <label className="flex items-center gap-2 text-sm">
              <input
                type="radio"
                checked={deliveryMode === 'DELIVERY'}
                onChange={() => setDeliveryMode('DELIVERY')}
              />
              配送到寝
            </label>
          </div>
          {deliveryMode === 'DELIVERY' && (
            <label className="mt-4 block text-sm">
              <span className="text-slate-600">收货地址</span>
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
          )}
          <button
            type="button"
            disabled={buy.isPending || (deliveryMode === 'DELIVERY' && !addressId)}
            onClick={() => buy.mutate()}
            className="mt-5 w-full rounded-xl bg-emerald-600 py-3 text-sm font-medium text-white hover:bg-emerald-700 disabled:opacity-50"
          >
            {buy.isPending ? '提交中…' : '立即下单'}
          </button>
        </div>
      )}

      <div className="mt-6 rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
        <h3 className="text-sm font-semibold text-slate-900">评论</h3>
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
          placeholder="说点什么…"
          value={commentText}
          onChange={(e) => setCommentText(e.target.value)}
        />
        <button
          type="button"
          disabled={commentMut.isPending || !commentText.trim()}
          onClick={() => commentMut.mutate()}
          className="mt-2 rounded-lg bg-slate-800 px-4 py-2 text-sm text-white disabled:opacity-50"
        >
          {commentMut.isPending ? '发送中…' : '发表评论'}
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
