import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { ArrowLeft, Loader2 } from 'lucide-react'
import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { fetchProfileFull, patchProfile } from '@/api/profile'
import { fetchCampuses } from '@/api/product'
import { ImageUploader } from '@/components/ImageUploader'

export function ProfileEditPage() {
  const qc = useQueryClient()
  const { data: profile, isLoading, error } = useQuery({
    queryKey: ['profile'],
    queryFn: fetchProfileFull,
  })
  const { data: campuses } = useQuery({ queryKey: ['campuses'], queryFn: fetchCampuses })

  const [nickname, setNickname] = useState('')
  const [avatarUrl, setAvatarUrl] = useState('')
  const [campusId, setCampusId] = useState<number | ''>('')

  useEffect(() => {
    if (profile) {
      setNickname(profile.nickname ?? '')
      setAvatarUrl(profile.avatarUrl ?? '')
      setCampusId(profile.campusId ?? '')
    }
  }, [profile])

  const saveMut = useMutation({
    mutationFn: () =>
      patchProfile({
        nickname: nickname.trim() || undefined,
        avatarUrl: avatarUrl.trim() || undefined,
        campusId: campusId === '' ? undefined : Number(campusId),
      }),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['profile'] })
      qc.invalidateQueries({ queryKey: ['me'] })
    },
  })

  if (isLoading) {
    return (
      <div className="flex justify-center py-20">
        <Loader2 className="h-8 w-8 animate-spin text-amber-600" />
      </div>
    )
  }

  if (error || !profile) {
    return (
      <p className="text-red-700">{error instanceof Error ? error.message : '加载失败'}</p>
    )
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
        <h1 className="text-lg font-semibold text-slate-900">编辑资料</h1>
      </div>

      <div className="space-y-5 rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
        <div className="flex items-center gap-4">
          <div className="h-20 w-20 overflow-hidden rounded-full bg-slate-100 ring-2 ring-amber-200">
            {avatarUrl ? (
              <img src={avatarUrl} alt="" className="h-full w-full object-cover" />
            ) : (
              <div className="flex h-full items-center justify-center text-xs text-slate-400">无头像</div>
            )}
          </div>
          <div className="flex-1">
            <ImageUploader
              value={avatarUrl ? [avatarUrl] : []}
              onChange={(urls) => setAvatarUrl(urls[0] ?? '')}
              maxCount={1}
              helpText="头像仅 1 张；默认走 OSS 直传（未启用可粘贴外链）"
            />
          </div>
        </div>

        <label className="block text-sm text-slate-700">
          头像 URL
          <input
            className="mt-1 w-full rounded-lg border border-slate-200 px-3 py-2 text-sm"
            value={avatarUrl}
            onChange={(e) => setAvatarUrl(e.target.value)}
            placeholder="https://..."
          />
        </label>

        <label className="block text-sm text-slate-700">
          昵称
          <input
            className="mt-1 w-full rounded-lg border border-slate-200 px-3 py-2 text-sm"
            value={nickname}
            onChange={(e) => setNickname(e.target.value)}
            placeholder="展示名"
          />
        </label>

        <label className="block text-sm text-slate-700">
          主校区
          <select
            className="mt-1 w-full rounded-lg border border-slate-200 px-3 py-2 text-sm"
            value={campusId}
            onChange={(e) => setCampusId(e.target.value === '' ? '' : Number(e.target.value))}
          >
            <option value="">不设置</option>
            {campuses?.map((c) => (
              <option key={c.id} value={c.id}>
                {c.name}
              </option>
            ))}
          </select>
        </label>

        <button
          type="button"
          disabled={saveMut.isPending}
          onClick={() => saveMut.mutate()}
          className="w-full rounded-xl bg-amber-500 py-3 text-sm font-medium text-white hover:bg-amber-600 disabled:opacity-50"
        >
          {saveMut.isPending ? '保存中…' : '保存'}
        </button>
        {saveMut.isError && (
          <p className="text-center text-xs text-red-600">
            {saveMut.error instanceof Error ? saveMut.error.message : '保存失败'}
          </p>
        )}
        {saveMut.isSuccess && (
          <p className="text-center text-xs text-emerald-600">已保存</p>
        )}
      </div>
    </div>
  )
}
