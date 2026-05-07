import { useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { fetchMyFans, fetchMyFollows, toggleFollow } from '@/api/follow'

export function FollowCenterPage() {
  const qc = useQueryClient()
  const [targetId, setTargetId] = useState('')
  const { data: follows } = useQuery({ queryKey: ['my-follows'], queryFn: fetchMyFollows })
  const { data: fans } = useQuery({ queryKey: ['my-fans'], queryFn: fetchMyFans })
  const toggle = useMutation({
    mutationFn: (id: number) => toggleFollow(id),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['my-follows'] })
      qc.invalidateQueries({ queryKey: ['my-fans'] })
    },
  })

  return (
    <div className="mx-auto max-w-2xl space-y-5">
      <h1 className="text-2xl font-semibold">关注中心</h1>
      <section className="rounded-xl border bg-white p-4">
        <h2 className="mb-2 font-medium">关注/取关用户</h2>
        <div className="flex gap-2">
          <input
            className="flex-1 rounded border px-3 py-2"
            value={targetId}
            onChange={(e) => setTargetId(e.target.value)}
            placeholder="输入用户ID"
          />
          <button
            className="rounded bg-slate-900 px-3 py-2 text-white disabled:opacity-50"
            onClick={() => toggle.mutate(Number(targetId))}
            disabled={!targetId || Number.isNaN(Number(targetId))}
          >
            切换关注
          </button>
        </div>
      </section>
      <section className="grid gap-4 md:grid-cols-2">
        <div className="rounded-xl border bg-white p-4">
          <h2 className="mb-2 font-medium">我关注的</h2>
          <div className="space-y-1 text-sm">
            {follows?.map((id) => (
              <div key={id} className="rounded border px-2 py-1">
                用户 {id}
              </div>
            ))}
            {!follows?.length ? <p className="text-slate-500">暂无关注</p> : null}
          </div>
        </div>
        <div className="rounded-xl border bg-white p-4">
          <h2 className="mb-2 font-medium">我的粉丝</h2>
          <div className="space-y-1 text-sm">
            {fans?.map((id) => (
              <div key={id} className="rounded border px-2 py-1">
                用户 {id}
              </div>
            ))}
            {!fans?.length ? <p className="text-slate-500">暂无粉丝</p> : null}
          </div>
        </div>
      </section>
    </div>
  )
}
