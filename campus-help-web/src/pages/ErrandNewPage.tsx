import { useMutation, useQuery } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import { Loader2 } from 'lucide-react'
import { useEffect, useState } from 'react'
import { fetchCampuses } from '@/api/product'
import { createErrandOrder } from '@/api/errand'

export function ErrandNewPage() {
  const navigate = useNavigate()
  const { data: campuses } = useQuery({ queryKey: ['campuses'], queryFn: fetchCampuses })
  const [campusId, setCampusId] = useState<number | null>(null)
  const [addressId, setAddressId] = useState('')
  const [errandType, setErrandType] = useState('EXPRESS_PICKUP')
  const [pickupAddress, setPickupAddress] = useState('')
  const [pickupCode, setPickupCode] = useState('')
  const [listText, setListText] = useState('')
  const [feeCent, setFeeCent] = useState('500')
  const [remark, setRemark] = useState('')

  useEffect(() => {
    if (campuses?.length && campusId === null) setCampusId(campuses[0].id)
  }, [campuses, campusId])

  const submit = useMutation({
    mutationFn: () =>
      createErrandOrder({
        campusId: campusId!,
        addressId: addressId ? Number(addressId) : undefined,
        errandType,
        pickupAddress: pickupAddress || undefined,
        pickupCode: pickupCode || undefined,
        listText: listText || undefined,
        feeCent: Number(feeCent),
        remark: remark || undefined,
      }),
    onSuccess: () => navigate('/orders'),
  })

  return (
    <div className="mx-auto max-w-lg">
      <h2 className="text-xl font-semibold text-slate-900">发布跑腿</h2>
      <p className="mt-1 text-sm text-slate-600">支付后订单进入骑手抢单池。</p>

      <form
        className="mt-6 space-y-4 rounded-2xl border border-slate-200 bg-white p-5 shadow-sm"
        onSubmit={(e) => {
          e.preventDefault()
          if (!campusId) return
          submit.mutate()
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
          <span className="text-slate-600">类型</span>
          <select
            className="mt-1 w-full rounded-lg border border-slate-200 px-3 py-2"
            value={errandType}
            onChange={(e) => setErrandType(e.target.value)}
          >
            <option value="BUY">代购</option>
            <option value="EXPRESS_PICKUP">代取快递</option>
            <option value="DOCUMENT_DELIVERY">文件送达</option>
            <option value="OTHER">其他</option>
          </select>
        </label>
        <label className="block text-sm">
          <span className="text-slate-600">取货地址</span>
          <input
            className="mt-1 w-full rounded-lg border border-slate-200 px-3 py-2"
            value={pickupAddress}
            onChange={(e) => setPickupAddress(e.target.value)}
            placeholder="快递点 / 超市等"
          />
        </label>
        <label className="block text-sm">
          <span className="text-slate-600">取件码</span>
          <input
            className="mt-1 w-full rounded-lg border border-slate-200 px-3 py-2"
            value={pickupCode}
            onChange={(e) => setPickupCode(e.target.value)}
          />
        </label>
        <label className="block text-sm">
          <span className="text-slate-600">清单说明</span>
          <textarea
            className="mt-1 w-full rounded-lg border border-slate-200 px-3 py-2"
            rows={2}
            value={listText}
            onChange={(e) => setListText(e.target.value)}
          />
        </label>
        <label className="block text-sm">
          <span className="text-slate-600">赏金（分）</span>
          <input
            type="number"
            required
            min={1}
            className="mt-1 w-full rounded-lg border border-slate-200 px-3 py-2"
            value={feeCent}
            onChange={(e) => setFeeCent(e.target.value)}
          />
        </label>
        <label className="block text-sm">
          <span className="text-slate-600">配送地址 ID（可选）</span>
          <input
            type="number"
            className="mt-1 w-full rounded-lg border border-slate-200 px-3 py-2"
            value={addressId}
            onChange={(e) => setAddressId(e.target.value)}
          />
        </label>
        <label className="block text-sm">
          <span className="text-slate-600">备注</span>
          <input
            className="mt-1 w-full rounded-lg border border-slate-200 px-3 py-2"
            value={remark}
            onChange={(e) => setRemark(e.target.value)}
          />
        </label>

        <button
          type="submit"
          disabled={submit.isPending}
          className="w-full rounded-xl bg-sky-600 py-3 text-sm font-medium text-white hover:bg-sky-700 disabled:opacity-50"
        >
          {submit.isPending ? (
            <span className="inline-flex items-center gap-2">
              <Loader2 className="h-4 w-4 animate-spin" /> 提交中
            </span>
          ) : (
            '创建订单'
          )}
        </button>
      </form>
    </div>
  )
}
