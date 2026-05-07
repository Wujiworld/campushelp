import { Loader2, Plus, Trash2 } from 'lucide-react'
import { useRef, useState } from 'react'
import { requestOssPresign, uploadFileToOss } from '@/api/oss'

export function ImageUploader(props: {
  value: string[]
  onChange: (urls: string[]) => void
  maxCount?: number
  className?: string
  helpText?: string
}) {
  const maxCount = props.maxCount ?? 9
  const [busy, setBusy] = useState(false)
  const [err, setErr] = useState<string | null>(null)
  const inputRef = useRef<HTMLInputElement | null>(null)

  async function upload(files: FileList | null) {
    if (!files?.length) return
    setErr(null)
    const remain = Math.max(0, maxCount - props.value.length)
    const picked = Array.from(files).slice(0, remain)
    if (picked.length === 0) return

    setBusy(true)
    try {
      const urls: string[] = []
      for (const file of picked) {
        if (file.size > 6 * 1024 * 1024) {
          throw new Error('单张图片最大 6MB')
        }
        const ext = file.name.includes('.') ? file.name.split('.').pop()! : 'jpg'
        const presign = await requestOssPresign(file.type || 'image/jpeg', ext)
        const url = await uploadFileToOss(file, presign)
        urls.push(url)
      }
      props.onChange([...props.value, ...urls])
      if (inputRef.current) inputRef.current.value = ''
    } catch (e) {
      setErr(
        e instanceof Error ? e.message : '上传失败；可检查 OSS 配置，或改用外链 URL 粘贴',
      )
    } finally {
      setBusy(false)
    }
  }

  function removeAt(i: number) {
    const next = props.value.slice()
    next.splice(i, 1)
    props.onChange(next)
  }

  return (
    <div className={props.className}>
      <div className="flex items-end justify-between gap-3">
        <div>
          <p className="text-sm font-medium text-slate-800">图片</p>
          <p className="mt-1 text-xs text-slate-500">
            {props.helpText ?? `最多 ${maxCount} 张；支持直传 OSS（预签名）`}
          </p>
        </div>
        <label className="inline-flex cursor-pointer items-center gap-2 rounded-xl bg-slate-900 px-3 py-2 text-xs font-medium text-white hover:bg-slate-800">
          {busy ? <Loader2 className="h-4 w-4 animate-spin" /> : <Plus className="h-4 w-4" />}
          {busy ? '上传中…' : '选择图片'}
          <input
            ref={inputRef}
            type="file"
            accept="image/*"
            multiple
            className="hidden"
            onChange={(e) => upload(e.target.files)}
            disabled={busy || props.value.length >= maxCount}
          />
        </label>
      </div>

      {err && <p className="mt-2 text-xs text-red-600">{err}</p>}

      {props.value.length > 0 && (
        <ul className="mt-3 grid grid-cols-3 gap-2 sm:grid-cols-4">
          {props.value.map((url, i) => (
            <li key={url + i} className="group relative overflow-hidden rounded-xl border border-slate-200 bg-slate-50">
              <img src={url} alt="" className="h-24 w-full object-cover" />
              <button
                type="button"
                className="absolute right-1 top-1 hidden rounded-lg bg-black/60 p-1 text-white group-hover:inline-flex"
                onClick={() => removeAt(i)}
                aria-label="删除图片"
              >
                <Trash2 className="h-4 w-4" />
              </button>
            </li>
          ))}
        </ul>
      )}

      {props.value.length < maxCount && (
        <p className="mt-2 text-xs text-slate-400">也可在下方粘贴图片 URL（每行一个）</p>
      )}
    </div>
  )
}

