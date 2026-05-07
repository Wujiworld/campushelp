import { Link } from 'react-router-dom'
import { Megaphone, ShieldCheck, Sparkles } from 'lucide-react'

const cards = [
  {
    to: '/admin/activities/new',
    title: '创建活动与票种',
    desc: '快速发布活动并配置票种库存',
    icon: Sparkles,
  },
  {
    to: '/admin/governance',
    title: '治理中心',
    desc: '角色审核、内容审核、审计日志',
    icon: ShieldCheck,
  },
  {
    to: '/profile',
    title: '系统广播入口',
    desc: '进入个人中心使用管理员能力菜单',
    icon: Megaphone,
  },
] as const

export function AdminHomePage() {
  return (
    <div className="space-y-6">
      <section className="rounded-3xl border border-violet-100 bg-gradient-to-br from-violet-50 to-white px-6 py-8">
        <p className="text-xs font-semibold uppercase tracking-widest text-violet-700">Admin workspace</p>
        <h1 className="mt-2 text-2xl font-semibold text-slate-900">管理员工作台</h1>
        <p className="mt-2 max-w-2xl text-sm text-slate-600">以治理效率为中心：先配置活动，再巡检内容，最后处理全局通知。</p>
      </section>
      <section className="grid gap-4 md:grid-cols-3">
        {cards.map((card) => (
          <Link
            key={card.title}
            to={card.to}
            className="group rounded-2xl border border-slate-200 bg-white p-5 shadow-sm transition hover:-translate-y-0.5 hover:border-violet-200 hover:shadow-md"
          >
            <div className="mb-3 inline-flex rounded-xl bg-violet-50 p-2 text-violet-600">
              <card.icon className="h-5 w-5" />
            </div>
            <h2 className="font-semibold text-slate-900">{card.title}</h2>
            <p className="mt-1 text-sm text-slate-600">{card.desc}</p>
          </Link>
        ))}
      </section>
    </div>
  )
}
