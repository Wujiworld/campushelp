import { Link, NavLink, Outlet, useNavigate } from 'react-router-dom'
import { Bike, Home, LogOut, ShoppingBag, Store, UserRound, PackageSearch, ShieldCheck } from 'lucide-react'
import { useAuthStore, type RoleCode } from '@/store/authStore'
import { cn } from '@/lib/utils'
import { RoleSwitcher } from '@/components/RoleSwitcher'

const NAV_BY_ROLE: Record<RoleCode, Array<{ to: string; label: string; icon?: typeof Home }>> = {
  STUDENT: [
    { to: '/student', label: '首页', icon: Home },
    { to: '/takeout', label: '外卖', icon: Store },
    { to: '/agent', label: '代购', icon: PackageSearch },
    { to: '/orders', label: '我的订单', icon: ShoppingBag },
  ],
  MERCHANT: [
    { to: '/merchant', label: '商家首页', icon: Store },
    { to: '/orders', label: '订单总览', icon: ShoppingBag },
    { to: '/profile', label: '我的', icon: UserRound },
  ],
  RIDER: [
    { to: '/rider', label: '骑手首页', icon: Bike },
    { to: '/orders', label: '订单总览', icon: ShoppingBag },
    { to: '/profile', label: '我的', icon: UserRound },
  ],
  ADMIN: [
    { to: '/admin', label: '管理首页', icon: ShieldCheck },
    { to: '/admin/activities/new', label: '活动管理', icon: Home },
    { to: '/admin/governance', label: '治理中心', icon: ShieldCheck },
    { to: '/profile', label: '我的', icon: UserRound },
  ],
}

export function Layout() {
  const navigate = useNavigate()
  const logout = useAuthStore((s) => s.logout)
  const activeRole = useAuthStore((s) => s.activeRole)
  const role = activeRole ?? 'STUDENT'
  const roleNav = NAV_BY_ROLE[role]

  return (
    <div className="flex min-h-screen flex-col">
      <header className="sticky top-0 z-40 border-b border-slate-200/80 bg-white/85 backdrop-blur-md">
        <div className="mx-auto flex min-h-16 max-w-6xl flex-wrap items-center justify-between gap-2 px-4 py-2">
          <Link to="/" className="flex items-center gap-2 font-semibold tracking-tight text-slate-900">
            <span className="flex h-8 w-8 items-center justify-center rounded-lg bg-gradient-to-br from-slate-900 to-slate-700 text-white shadow-md">
              校
            </span>
            <span>校园帮</span>
          </Link>
          <div className="flex flex-wrap items-center gap-2">
            <RoleSwitcher />
            <nav className="flex flex-wrap items-center gap-1 text-sm">
              {roleNav.map((item) => {
                const Icon = item.icon
                return (
                  <NavLink
                    key={item.to}
                    to={item.to}
                    end={item.to === '/student' || item.to === '/merchant' || item.to === '/rider' || item.to === '/admin'}
                    className={({ isActive }) =>
                      cn(
                        'inline-flex items-center gap-1.5 rounded-lg px-3 py-1.5 transition',
                        isActive
                          ? 'bg-slate-900 text-white'
                          : 'text-slate-600 hover:bg-slate-100 hover:text-slate-900',
                      )
                    }
                  >
                    {Icon ? <Icon className="h-4 w-4" /> : null}
                    {item.label}
                  </NavLink>
                )
              })}
              <NavLink
                to="/profile"
                className={({ isActive }) =>
                  cn(
                    'inline-flex items-center gap-1.5 rounded-lg px-3 py-1.5 transition',
                    isActive ? 'bg-slate-900 text-white' : 'text-slate-600 hover:bg-slate-100 hover:text-slate-900',
                  )
                }
              >
                <UserRound className="h-4 w-4" />
                我的
              </NavLink>
            <button
              type="button"
              className="ml-2 inline-flex items-center gap-1 rounded-lg px-3 py-1.5 text-slate-600 transition hover:bg-slate-100 hover:text-slate-900"
              onClick={() => {
                logout()
                navigate('/login', { replace: true })
              }}
            >
              <LogOut className="h-4 w-4" />
              退出
            </button>
            </nav>
          </div>
        </div>
      </header>
      <main className="mx-auto w-full max-w-6xl flex-1 px-4 py-10">
        <Outlet />
      </main>
      <footer className="border-t border-slate-200 py-6 text-center text-xs text-slate-500">
        校园帮 · 单体部署 · 本地开发请同时启动后端 campus-help-server 与 Vite 前端
      </footer>
    </div>
  )
}
