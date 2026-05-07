import { useMutation, useQuery } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import { Loader2 } from 'lucide-react'
import { useEffect, useState } from 'react'
import { fetchCampuses } from '@/api/product'
import { publishSecondhandItem } from '@/api/secondhand'
import { ImageUploader } from '@/components/ImageUploader'

export function SecondhandPublishPage() {
  const navigate = useNavigate()
  const { data: campuses } = useQuery({ queryKey: ['campuses'], queryFn: fetchCampuses })
  const [campusId, setCampusId] = useState<number | null>(null)
  const [title, setTitle] = useState('')
  const [description, setDescription] = useState('')
  const [priceCent, setPriceCent] = useState('1999')
  const [negotiable, setNegotiable] = useState(true)
  const [manualUrls, setManualUrls] = useState('')

  useEffect(() => {
    if (campuses?.length && campusId === null) setCampusId(campuses[0].id)
  }, [campuses, campusId])

  const publish = useMutation({
    mutationFn: async () => {
      const lines = manualUrls
        .split('\n')
        .map((s) => s.trim())
        .filter(Boolean)
      const body = {
        title,
        description,
        priceCent: Number(priceCent),
        negotiable,
        imageUrls: lines,
      }
      return publishSecondhandItem(campusId!, body)
    },
    onSuccess: (item) => navigate(`/secondhand/${item.id}`),
  })

  return (
    <div className="mx-auto max-w-lg">
      <h2 className="text-xl font-semibold text-slate-900">发布闲置</h2>
      <p className="mt-1 text-sm text-slate-600">图片可走预签名直传 OSS；未配置时请在下方粘贴外链。</p>

      <form
        className="mt-6 space-y-4 rounded-2xl border border-slate-200 bg-white p-5 shadow-sm"
        onSubmit={(e) => {
          e.preventDefault()
          if (!campusId || !title.trim()) return
          publish.mutate()
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
          />
        </label>
        <label className="block text-sm">
          <span className="text-slate-600">描述</span>
          <textarea
            className="mt-1 w-full rounded-lg border border-slate-200 px-3 py-2"
            rows={3}
            value={description}
            onChange={(e) => setDescription(e.target.value)}
          />
        </label>
        <label className="block text-sm">
          <span className="text-slate-600">价格（分，如 1999=¥19.99）</span>
          <input
            type="number"
            required
            min={1}
            className="mt-1 w-full rounded-lg border border-slate-200 px-3 py-2"
            value={priceCent}
            onChange={(e) => setPriceCent(e.target.value)}
          />
        </label>
        <label className="flex items-center gap-2 text-sm">
          <input type="checkbox" checked={negotiable} onChange={(e) => setNegotiable(e.target.checked)} />
          可议价
        </label>

        <div className="border-t border-slate-100 pt-4">
          <ImageUploader
            value={manualUrls.split('\n').map((s) => s.trim()).filter(Boolean)}
            onChange={(urls) => setManualUrls(urls.join('\n'))}
            maxCount={9}
            helpText="最多 9 张；默认走 OSS 直传（未启用可粘贴外链）"
          />
          <label className="mt-3 block text-sm">
            <span className="text-slate-600">图片 URL（每行一个，占位图或 OSS 地址均可）</span>
            <textarea
              className="mt-1 w-full rounded-lg border border-slate-200 px-3 py-2 font-mono text-xs"
              rows={4}
              value={manualUrls}
              onChange={(e) => setManualUrls(e.target.value)}
              placeholder="https://..."
            />
          </label>
        </div>

        <button
          type="submit"
          disabled={publish.isPending || !title.trim()}
          className="w-full rounded-xl bg-emerald-600 py-3 text-sm font-medium text-white hover:bg-emerald-700 disabled:opacity-50"
        >
          {publish.isPending ? (
            <span className="inline-flex items-center gap-2">
              <Loader2 className="h-4 w-4 animate-spin" /> 发布中
            </span>
          ) : (
            '发布'
          )}
        </button>
      </form>
    </div>
  )
}
