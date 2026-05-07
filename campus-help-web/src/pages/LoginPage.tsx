import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Loader2, Lock, Smartphone } from 'lucide-react'
import { login } from '@/api/auth'
import { useAuthStore } from '@/store/authStore'
import { cn } from '@/lib/utils'

export function LoginPage() {
  const navigate = useNavigate()
  const setAuth = useAuthStore((s) => s.setAuth)
  const getRoleHomePath = useAuthStore((s) => s.getRoleHomePath)
  const token = useAuthStore((s) => s.token)
  const [phone, setPhone] = useState('')
  const [password, setPassword] = useState('')
  const [loading, setLoading] = useState(false)
  const [err, setErr] = useState<string | null>(null)

  useEffect(() => {
    if (token) {
      navigate(getRoleHomePath(), { replace: true })
    }
  }, [token, navigate, getRoleHomePath])

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault()
    setErr(null)
    setLoading(true)
    try {
      const data = await login(phone.trim(), password)
      setAuth(data.token, data.userId, data.roles as string[])
      navigate(useAuthStore.getState().getRoleHomePath(), { replace: true })
    } catch (ex: unknown) {
      setErr(ex instanceof Error ? ex.message : '登录失败')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="flex min-h-screen flex-col items-center justify-center px-4">
      <div className="w-full max-w-md">
        <div className="mb-8 text-center">
          <div className="mx-auto mb-4 flex h-14 w-14 items-center justify-center rounded-2xl bg-gradient-to-br from-emerald-400 to-cyan-500 text-2xl font-bold text-slate-900 shadow-xl shadow-emerald-500/30">
            校
          </div>
          <h1 className="text-2xl font-semibold tracking-tight text-slate-900">校园帮</h1>
          <p className="mt-2 text-sm text-slate-600">校园一站式生活服务</p>
        </div>

        <div className="rounded-2xl border border-slate-200 bg-white p-8 shadow-lg shadow-slate-200/50">
          <form onSubmit={onSubmit} className="space-y-5">
            <div>
              <label className="mb-1.5 block text-xs font-medium uppercase tracking-wide text-slate-500">
                手机号
              </label>
              <div className="relative">
                <Smartphone className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
                <input
                  className="w-full rounded-xl border border-slate-200 bg-white py-2.5 pl-10 pr-3 text-sm text-slate-900 outline-none ring-teal-500/30 transition placeholder:text-slate-400 focus:border-teal-500 focus:ring-2"
                  placeholder="注册手机号"
                  value={phone}
                  onChange={(e) => setPhone(e.target.value)}
                  autoComplete="username"
                />
              </div>
            </div>
            <div>
              <label className="mb-1.5 block text-xs font-medium uppercase tracking-wide text-slate-500">
                密码
              </label>
              <div className="relative">
                <Lock className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
                <input
                  type="password"
                  className="w-full rounded-xl border border-slate-200 bg-white py-2.5 pl-10 pr-3 text-sm text-slate-900 outline-none ring-teal-500/30 transition placeholder:text-slate-400 focus:border-teal-500 focus:ring-2"
                  placeholder="登录密码"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  autoComplete="current-password"
                />
              </div>
            </div>

            {err && (
              <div className="rounded-lg border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-800">
                {err}
              </div>
            )}

            <button
              type="submit"
              disabled={loading}
              className={cn(
                'flex w-full items-center justify-center gap-2 rounded-xl py-3 text-sm font-medium text-white transition',
                'bg-gradient-to-r from-teal-600 to-cyan-600 hover:from-teal-500 hover:to-cyan-500',
                'disabled:cursor-not-allowed disabled:opacity-60',
              )}
            >
              {loading ? <Loader2 className="h-4 w-4 animate-spin" /> : null}
              登录
            </button>
          </form>
        </div>
      </div>
    </div>
  )
}
