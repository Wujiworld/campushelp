import { useMutation } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import { useEffect, useMemo, useState } from 'react'
import { Loader2, Plus, Ticket } from 'lucide-react'
import { useQuery } from '@tanstack/react-query'
import { fetchCampuses } from '@/api/product'
import { adminCreateActivity, adminCreateTicketType } from '@/api/activity'

function toIso(dtLocal: string) {
  // datetime-local => ISO string, backend expects LocalDateTime; it can parse "2026-04-09T12:30"
  return dtLocal
}

export function AdminActivityNewPage() {
  const navigate = useNavigate()
  const { data: campuses } = useQuery({ queryKey: ['campuses'], queryFn: fetchCampuses })

  const [campusId, setCampusId] = useState<number | null>(null)
  const [title, setTitle] = useState('')
  const [description, setDescription] = useState('')
  const [place, setPlace] = useState('')
  const [startTime, setStartTime] = useState('')
  const [endTime, setEndTime] = useState('')
  const [status, setStatus] = useState<'PUBLISHED' | 'DRAFT'>('PUBLISHED')

  const [createdActivityId, setCreatedActivityId] = useState<number | null>(null)

  const [ticketName, setTicketName] = useState('普通票')
  const [ticketPriceCent, setTicketPriceCent] = useState('0')
  const [ticketStockTotal, setTicketStockTotal] = useState('100')
  const [ticketPerUserLimit, setTicketPerUserLimit] = useState('1')
  const [saleStartTime, setSaleStartTime] = useState('')
  const [saleEndTime, setSaleEndTime] = useState('')

  useEffect(() => {
    if (campuses?.length && campusId == null) setCampusId(campuses[0].id)
  }, [campuses, campusId])

  const canCreate = useMemo(() => {
    return !!campusId && title.trim() && startTime && endTime
  }, [campusId, title, startTime, endTime])

  const createAct = useMutation({
    mutationFn: () =>
      adminCreateActivity({
        campusId: campusId!,
        title: title.trim(),
        description: description.trim() || undefined,
        place: place.trim() || undefined,
        startTime: toIso(startTime),
        endTime: toIso(endTime),
        status,
      }),
    onSuccess: (a) => setCreatedActivityId(a.id),
  })

  const createTicket = useMutation({
    mutationFn: () =>
      adminCreateTicketType(createdActivityId!, {
        name: ticketName.trim(),
        priceCent: Number(ticketPriceCent),
        stockTotal: Number(ticketStockTotal),
        perUserLimit: Number(ticketPerUserLimit),
        saleStartTime: toIso(saleStartTime),
        saleEndTime: toIso(saleEndTime),
        status: 'ON',
      }),
    onSuccess: () => navigate(`/activities/${createdActivityId}`),
  })

  return (
    <div className="mx-auto max-w-xl">
      <div className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
        <h2 className="text-xl font-semibold text-slate-900">活动管理（ADMIN）</h2>
        <p className="mt-1 text-sm text-slate-600">创建活动后，再添加票种。</p>

        <div className="mt-5 grid gap-4">
          {campuses && (
            <label className="block text-sm">
              <span className="text-slate-600">校区</span>
              <select
                className="mt-1 w-full rounded-lg border border-slate-200 px-3 py-2"
                value={campusId ?? ''}
                onChange={(e) => setCampusId(Number(e.target.value))}
                disabled={!!createdActivityId}
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
              className="mt-1 w-full rounded-lg border border-slate-200 px-3 py-2"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              disabled={!!createdActivityId}
              placeholder="如：校园音乐节"
            />
          </label>

          <label className="block text-sm">
            <span className="text-slate-600">地点</span>
            <input
              className="mt-1 w-full rounded-lg border border-slate-200 px-3 py-2"
              value={place}
              onChange={(e) => setPlace(e.target.value)}
              disabled={!!createdActivityId}
              placeholder="如：体育馆"
            />
          </label>

          <label className="block text-sm">
            <span className="text-slate-600">活动开始时间</span>
            <input
              type="datetime-local"
              className="mt-1 w-full rounded-lg border border-slate-200 px-3 py-2"
              value={startTime}
              onChange={(e) => setStartTime(e.target.value)}
              disabled={!!createdActivityId}
            />
          </label>

          <label className="block text-sm">
            <span className="text-slate-600">活动结束时间</span>
            <input
              type="datetime-local"
              className="mt-1 w-full rounded-lg border border-slate-200 px-3 py-2"
              value={endTime}
              onChange={(e) => setEndTime(e.target.value)}
              disabled={!!createdActivityId}
            />
          </label>

          <label className="block text-sm">
            <span className="text-slate-600">状态</span>
            <select
              className="mt-1 w-full rounded-lg border border-slate-200 px-3 py-2"
              value={status}
              onChange={(e) => setStatus(e.target.value as any)}
              disabled={!!createdActivityId}
            >
              <option value="PUBLISHED">发布</option>
              <option value="DRAFT">草稿</option>
            </select>
          </label>

          <label className="block text-sm">
            <span className="text-slate-600">描述</span>
            <textarea
              className="mt-1 w-full rounded-lg border border-slate-200 px-3 py-2"
              rows={4}
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              disabled={!!createdActivityId}
              placeholder="活动介绍、注意事项等"
            />
          </label>

          {!createdActivityId ? (
            <button
              type="button"
              disabled={!canCreate || createAct.isPending}
              onClick={() => createAct.mutate()}
              className="inline-flex w-full items-center justify-center gap-2 rounded-xl bg-slate-900 py-3 text-sm font-medium text-white disabled:opacity-50"
            >
              {createAct.isPending ? <Loader2 className="h-4 w-4 animate-spin" /> : <Plus className="h-4 w-4" />}
              创建活动
            </button>
          ) : (
            <div className="rounded-xl border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm text-emerald-900">
              已创建活动 #{createdActivityId}，继续添加票种。
            </div>
          )}
        </div>
      </div>

      {createdActivityId && (
        <div className="mt-6 rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
          <h3 className="text-lg font-semibold text-slate-900">新增票种</h3>
          <div className="mt-4 grid gap-4">
            <label className="block text-sm">
              <span className="text-slate-600">票种名称</span>
              <input
                className="mt-1 w-full rounded-lg border border-slate-200 px-3 py-2"
                value={ticketName}
                onChange={(e) => setTicketName(e.target.value)}
              />
            </label>
            <div className="grid grid-cols-2 gap-3">
              <label className="block text-sm">
                <span className="text-slate-600">价格（分）</span>
                <input
                  type="number"
                  min={0}
                  className="mt-1 w-full rounded-lg border border-slate-200 px-3 py-2"
                  value={ticketPriceCent}
                  onChange={(e) => setTicketPriceCent(e.target.value)}
                />
              </label>
              <label className="block text-sm">
                <span className="text-slate-600">总库存</span>
                <input
                  type="number"
                  min={1}
                  className="mt-1 w-full rounded-lg border border-slate-200 px-3 py-2"
                  value={ticketStockTotal}
                  onChange={(e) => setTicketStockTotal(e.target.value)}
                />
              </label>
            </div>
            <label className="block text-sm">
              <span className="text-slate-600">每人限购</span>
              <input
                type="number"
                min={1}
                className="mt-1 w-full rounded-lg border border-slate-200 px-3 py-2"
                value={ticketPerUserLimit}
                onChange={(e) => setTicketPerUserLimit(e.target.value)}
              />
            </label>
            <div className="grid grid-cols-2 gap-3">
              <label className="block text-sm">
                <span className="text-slate-600">售卖开始</span>
                <input
                  type="datetime-local"
                  className="mt-1 w-full rounded-lg border border-slate-200 px-3 py-2"
                  value={saleStartTime}
                  onChange={(e) => setSaleStartTime(e.target.value)}
                />
              </label>
              <label className="block text-sm">
                <span className="text-slate-600">售卖结束</span>
                <input
                  type="datetime-local"
                  className="mt-1 w-full rounded-lg border border-slate-200 px-3 py-2"
                  value={saleEndTime}
                  onChange={(e) => setSaleEndTime(e.target.value)}
                />
              </label>
            </div>

            <button
              type="button"
              disabled={
                createTicket.isPending ||
                !ticketName.trim() ||
                !saleStartTime ||
                !saleEndTime ||
                Number(ticketStockTotal) <= 0 ||
                Number(ticketPerUserLimit) <= 0 ||
                Number.isNaN(Number(ticketPriceCent))
              }
              onClick={() => createTicket.mutate()}
              className="inline-flex w-full items-center justify-center gap-2 rounded-xl bg-sky-700 py-3 text-sm font-medium text-white disabled:opacity-50"
            >
              {createTicket.isPending ? (
                <Loader2 className="h-4 w-4 animate-spin" />
              ) : (
                <Ticket className="h-4 w-4" />
              )}
              创建票种并前往活动详情
            </button>
          </div>
        </div>
      )}
    </div>
  )
}

