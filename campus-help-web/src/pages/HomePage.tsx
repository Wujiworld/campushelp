import { Link } from 'react-router-dom'
import { ArrowRight, Bike, PartyPopper, Recycle, UtensilsCrossed } from 'lucide-react'

const cards = [
  {
    to: '/takeout',
    title: '校园外卖',
    desc: '食堂档口 · 奶茶咖啡 · 配送到寝',
    icon: UtensilsCrossed,
    accent: 'from-amber-400 to-orange-500',
    border: 'border-amber-200 hover:border-amber-300',
  },
  {
    to: '/secondhand',
    title: '二手市集',
    desc: '教材数码 · 发布闲置 · 面交或配送',
    icon: Recycle,
    accent: 'from-emerald-400 to-teal-600',
    border: 'border-emerald-200 hover:border-emerald-300',
  },
  {
    to: '/errand/new',
    title: '跑腿任务',
    desc: '代取快递 · 代购代办 · 赏金下单',
    icon: Bike,
    accent: 'from-sky-400 to-indigo-500',
    border: 'border-sky-200 hover:border-sky-300',
  },
  {
    to: '/activities',
    title: '活动抢票',
    desc: '讲座演出 · 早鸟票 · 库存实时扣减',
    icon: PartyPopper,
    accent: 'from-fuchsia-400 to-purple-600',
    border: 'border-fuchsia-200 hover:border-fuchsia-300',
  },
] as const

export function HomePage() {
  return (
    <div className="space-y-7">
      <div className="rounded-3xl border border-teal-100 bg-gradient-to-br from-teal-50 via-white to-emerald-50/70 px-6 py-8 shadow-sm">
        <p className="text-xs font-semibold uppercase tracking-[0.22em] text-teal-700/80">Student workspace</p>
        <h1 className="mt-2 text-2xl font-bold text-slate-900 sm:text-3xl">学生首页</h1>
        <p className="mt-2 max-w-2xl text-slate-600">围绕“下单、发布、接收消息”打造首屏路径：优先完成今天最常做的任务。</p>
        <div className="mt-4 flex flex-wrap gap-2">
          <Link to="/orders" className="inline-flex items-center gap-1 rounded-lg bg-slate-900 px-3 py-2 text-sm font-medium text-white hover:bg-slate-800">
            查看我的订单
            <ArrowRight className="h-4 w-4" />
          </Link>
          <Link to="/secondhand/new" className="inline-flex items-center gap-1 rounded-lg border border-slate-200 bg-white px-3 py-2 text-sm text-slate-700 hover:bg-slate-50">
            发布闲置
          </Link>
        </div>
      </div>

      <ul className="grid gap-5 sm:grid-cols-2">
        {cards.map((c) => (
          <li key={c.to}>
            <Link
              to={c.to}
              className={`group flex gap-4 rounded-2xl border bg-white p-6 shadow-sm transition ${c.border} hover:-translate-y-0.5 hover:shadow-md`}
            >
              <span
                className={`flex h-14 w-14 shrink-0 items-center justify-center rounded-2xl bg-gradient-to-br ${c.accent} text-white shadow-md`}
              >
                <c.icon className="h-7 w-7" />
              </span>
              <div>
                <h2 className="text-lg font-semibold text-slate-900">{c.title}</h2>
                <p className="mt-1 text-sm text-slate-600">{c.desc}</p>
                <p className="mt-3 inline-flex items-center gap-1 text-xs font-medium text-slate-500 group-hover:text-slate-700">
                  立即进入
                  <ArrowRight className="h-3 w-3" />
                </p>
              </div>
            </Link>
          </li>
        ))}
      </ul>
    </div>
  )
}
