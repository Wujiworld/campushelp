import { Link } from 'react-router-dom'
import { ShieldX } from 'lucide-react'

export function ForbiddenPage() {
  return (
    <div className="mx-auto flex min-h-[60vh] max-w-xl flex-col items-center justify-center rounded-3xl border border-slate-200 bg-white px-8 py-14 text-center shadow-sm">
      <div className="mb-4 rounded-2xl bg-red-50 p-4 text-red-600">
        <ShieldX className="h-8 w-8" />
      </div>
      <h1 className="text-2xl font-semibold text-slate-900">无权访问此页面</h1>
      <p className="mt-2 text-sm text-slate-600">当前角色没有对应权限，请切换角色或联系管理员开通权限。</p>
      <div className="mt-6 flex gap-3">
        <Link to="/" className="rounded-lg bg-slate-900 px-4 py-2 text-sm font-medium text-white hover:bg-slate-800">
          返回首页
        </Link>
        <Link to="/profile" className="rounded-lg border border-slate-200 bg-white px-4 py-2 text-sm text-slate-700 hover:bg-slate-50">
          前往个人中心
        </Link>
      </div>
    </div>
  )
}
