import { useQuery } from '@tanstack/react-query'
import {
  ChevronRight,
  Heart,
  Loader2,
  MapPin,
  Package,
  Pencil,
  ShoppingBag,
  Sparkles,
  User,
  MessageSquare,
} from 'lucide-react'
import { Link } from 'react-router-dom'
import { fetchMe } from '@/api/auth'
import { fetchProfileStats } from '@/api/profile'
import { useAuthStore } from '@/store/authStore'

function maskPhone(phone: string) {
  if (phone.length >= 11) return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
  return phone
}

export function ProfilePage() {
  const roles = useAuthStore((s) => s.roles)
  const activeRole = useAuthStore((s) => s.activeRole)
  const { data, isLoading, error } = useQuery({
    queryKey: ['me'],
    queryFn: fetchMe,
  })
  const { data: stats, isLoading: statsLoading } = useQuery({
    queryKey: ['profile-stats'],
    queryFn: fetchProfileStats,
  })

  if (isLoading) {
    return (
      <div className="flex justify-center py-20">
        <Loader2 className="h-8 w-8 animate-spin text-amber-600" />
      </div>
    )
  }

  if (error) {
    return (
      <div className="rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-800">
        {error instanceof Error ? error.message : '加载失败'}
      </div>
    )
  }

  const displayRoles = data?.roles?.length ? data.roles : roles

  const menu = [
    {
      to: '/profile/my-items',
      icon: Package,
      title: '我发布的',
      sub: '闲置管理',
    },
    {
      to: '/profile/likes',
      icon: Heart,
      title: '赞过',
      sub: '收藏的闲置',
    },
    {
      to: '/profile/comments',
      icon: MessageSquare,
      title: '我的评论',
      sub: '最近互动记录',
    },
    {
      to: '/profile/follow',
      icon: User,
      title: '关注中心',
      sub: '关注 / 粉丝',
    },
    {
      to: '/profile/addresses',
      icon: MapPin,
      title: '收货地址',
      sub: '二手配送 / 外卖',
    },
    {
      to: '/profile/edit',
      icon: Pencil,
      title: '编辑资料',
      sub: '昵称 · 头像 · 校区',
    },
    {
      to: '/orders',
      icon: ShoppingBag,
      title: '我的订单',
      sub: '外卖 · 二手 · 活动',
    },
  ]

  if (displayRoles.includes('ADMIN')) {
    menu.unshift({
      to: '/admin/governance',
      icon: Sparkles,
      title: '治理中心',
      sub: '审核、看板、审计日志',
    })
    menu.unshift({
      to: '/admin/activities/new',
      icon: Sparkles,
      title: '活动管理',
      sub: '创建活动与票种（ADMIN）',
    })
  }
  if (displayRoles.includes('MERCHANT') || displayRoles.includes('RIDER')) {
    menu.unshift({
      to: '/profile/finance',
      icon: ShoppingBag,
      title: '资金与提现',
      sub: '余额查询 / 提现申请',
    })
  }

  return (
    <div className="mx-auto max-w-lg">
      <section className="relative overflow-hidden rounded-3xl bg-gradient-to-br from-amber-400 via-amber-300 to-orange-200 px-5 pb-8 pt-10 text-slate-900 shadow-lg shadow-amber-500/20">
        <div className="pointer-events-none absolute -right-8 -top-8 h-32 w-32 rounded-full bg-white/20 blur-2xl" />
        <div className="pointer-events-none absolute -bottom-10 left-1/4 h-24 w-24 rounded-full bg-orange-300/40 blur-xl" />

        <div className="relative flex items-start gap-4">
          <div className="h-20 w-20 shrink-0 overflow-hidden rounded-2xl bg-white/90 shadow-md ring-4 ring-white/50">
            {data?.avatarUrl ? (
              <img src={data.avatarUrl} alt="" className="h-full w-full object-cover" />
            ) : (
              <div className="flex h-full items-center justify-center text-amber-700/70">
                <User className="h-10 w-10" />
              </div>
            )}
          </div>
          <div className="min-w-0 flex-1 pt-1">
            <div className="flex items-center gap-2">
              <h1 className="truncate text-xl font-bold tracking-tight">
                {data?.nickname?.trim() || '未设置昵称'}
              </h1>
              <Sparkles className="h-4 w-4 shrink-0 text-amber-900/60" />
            </div>
            <p className="mt-1 text-sm text-amber-950/70">{maskPhone(data?.phone ?? '')}</p>
            {data?.campusId != null && (
              <p className="mt-0.5 text-xs text-amber-950/55">主校区 ID · {data.campusId}</p>
            )}
          </div>
        </div>

        <div className="relative mt-8 grid grid-cols-4 gap-2 rounded-2xl bg-white/90 p-3 text-center shadow-inner backdrop-blur-sm">
          {statsLoading ? (
            <div className="col-span-4 flex justify-center py-2">
              <Loader2 className="h-5 w-5 animate-spin text-amber-600" />
            </div>
          ) : (
            <>
              <div>
                <p className="text-lg font-bold text-slate-900">{stats?.secondhandOnSale ?? 0}</p>
                <p className="text-[10px] font-medium text-slate-500">在售</p>
              </div>
              <div>
                <p className="text-lg font-bold text-slate-900">{stats?.secondhandSold ?? 0}</p>
                <p className="text-[10px] font-medium text-slate-500">已售</p>
              </div>
              <div>
                <p className="text-lg font-bold text-slate-900">{stats?.buyerOrderCount ?? 0}</p>
                <p className="text-[10px] font-medium text-slate-500">订单</p>
              </div>
              <div>
                <p className="text-lg font-bold text-slate-900">{stats?.likesGiven ?? 0}</p>
                <p className="text-[10px] font-medium text-slate-500">互动</p>
              </div>
            </>
          )}
        </div>
      </section>

      <section className="mt-6 rounded-2xl border border-slate-200/80 bg-white shadow-sm">
        <p className="border-b border-slate-100 px-4 py-3 text-xs font-semibold uppercase tracking-wide text-slate-400">
          我的服务
        </p>
        <ul>
          {menu.map((row) => {
            const Icon = row.icon
            return (
            <li key={row.to} className="border-b border-slate-50 last:border-0">
              <Link
                to={row.to}
                className="flex items-center gap-3 px-4 py-3.5 transition hover:bg-amber-50/50"
              >
                <span className="flex h-10 w-10 items-center justify-center rounded-xl bg-amber-100 text-amber-800">
                  <Icon className="h-5 w-5" />
                </span>
                <div className="min-w-0 flex-1">
                  <p className="font-medium text-slate-900">{row.title}</p>
                  <p className="text-xs text-slate-500">{row.sub}</p>
                </div>
                <ChevronRight className="h-4 w-4 shrink-0 text-slate-300" />
              </Link>
            </li>
            )
          })}
        </ul>
      </section>

      <section className="mt-6 rounded-2xl border border-slate-200 bg-white p-4 shadow-sm">
        <p className="text-xs font-semibold uppercase text-slate-400">账号角色</p>
        <div className="mt-3 flex flex-wrap gap-2">
          {displayRoles.map((r) => (
            <span
              key={r}
              className={`rounded-full px-3 py-1 text-xs font-medium ${
                r === activeRole ? 'bg-slate-900 text-white' : 'bg-slate-100 text-slate-700'
              }`}
            >
              {r}
              {r === activeRole ? ' · 当前' : ''}
            </span>
          ))}
        </div>
      </section>
    </div>
  )
}
