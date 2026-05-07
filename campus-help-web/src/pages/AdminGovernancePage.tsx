import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
  approveContent,
  approveRoleApplication,
  fetchAdminDashboard,
  fetchAuditLogs,
  fetchPendingContent,
  fetchRoleApplications,
  rejectContent,
  rejectRoleApplication,
} from '@/api/admin'

export function AdminGovernancePage() {
  const qc = useQueryClient()
  const { data: dashboard } = useQuery({ queryKey: ['admin-dashboard'], queryFn: fetchAdminDashboard })
  const { data: roleApps } = useQuery({ queryKey: ['role-apps'], queryFn: () => fetchRoleApplications('PENDING', 30) })
  const { data: pendingContent } = useQuery({ queryKey: ['pending-content'], queryFn: () => fetchPendingContent(30) })
  const { data: auditLogs } = useQuery({ queryKey: ['audit-logs'], queryFn: () => fetchAuditLogs(30) })

  const approveRole = useMutation({
    mutationFn: (id: number) => approveRoleApplication(id, '审核通过'),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['role-apps'] }),
  })
  const rejectRole = useMutation({
    mutationFn: (id: number) => rejectRoleApplication(id, '资料不完整'),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['role-apps'] }),
  })
  const approveContentMut = useMutation({
    mutationFn: ({ type, id }: { type: 'SECONDHAND' | 'AGENT' | 'COMMENT'; id: number }) => approveContent(type, id),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['pending-content'] }),
  })
  const rejectContentMut = useMutation({
    mutationFn: ({ type, id }: { type: 'SECONDHAND' | 'AGENT' | 'COMMENT'; id: number }) => rejectContent(type, id),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['pending-content'] }),
  })

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-semibold">治理与审核中心</h1>

      <section className="grid gap-3 md:grid-cols-3">
        <Metric title="总订单" value={dashboard?.totalOrders} />
        <Metric title="GMV(分)" value={dashboard?.gmvPaidCent} />
        <Metric title="近7天活跃下单用户" value={dashboard?.activeOrderUsers7d} />
      </section>

      <section className="rounded-xl border bg-white p-4">
        <h2 className="mb-3 font-semibold">角色申请待审</h2>
        <div className="space-y-2">
          {roleApps?.map((r) => (
            <div key={r.id} className="flex items-center justify-between rounded border px-3 py-2 text-sm">
              <div>
                <div>申请ID #{r.id} · 用户 {r.userId}</div>
                <div className="text-slate-500">
                  角色 {r.roleCode} · 证件 {r.docType || '-'} · 说明 {r.submitRemark || '-'}
                </div>
              </div>
              <div className="flex gap-2">
                <button className="rounded bg-emerald-600 px-2 py-1 text-white" onClick={() => approveRole.mutate(r.id)}>
                  通过
                </button>
                <button className="rounded bg-rose-600 px-2 py-1 text-white" onClick={() => rejectRole.mutate(r.id)}>
                  驳回
                </button>
              </div>
            </div>
          ))}
          {!roleApps?.length ? <p className="text-sm text-slate-500">暂无待审申请</p> : null}
        </div>
      </section>

      <section className="rounded-xl border bg-white p-4">
        <h2 className="mb-3 font-semibold">内容审核待审</h2>
        <AuditBlock
          title="二手"
          type="SECONDHAND"
          rows={pendingContent?.secondhand ?? []}
          onApprove={(id) => approveContentMut.mutate({ type: 'SECONDHAND', id })}
          onReject={(id) => rejectContentMut.mutate({ type: 'SECONDHAND', id })}
        />
        <AuditBlock
          title="代购"
          type="AGENT"
          rows={pendingContent?.agent ?? []}
          onApprove={(id) => approveContentMut.mutate({ type: 'AGENT', id })}
          onReject={(id) => rejectContentMut.mutate({ type: 'AGENT', id })}
        />
        <AuditBlock
          title="评论"
          type="COMMENT"
          rows={pendingContent?.comment ?? []}
          onApprove={(id) => approveContentMut.mutate({ type: 'COMMENT', id })}
          onReject={(id) => rejectContentMut.mutate({ type: 'COMMENT', id })}
        />
      </section>

      <section className="rounded-xl border bg-white p-4">
        <h2 className="mb-3 font-semibold">关键操作审计日志</h2>
        <div className="space-y-1 text-sm">
          {auditLogs?.map((a) => (
            <div key={a.id} className="rounded border px-2 py-1">
              [{a.operatorRole || '-'}#{a.operatorUserId || '-'}] {a.action} {a.targetType}:{a.targetId || '-'} {a.detail || ''}
            </div>
          ))}
          {!auditLogs?.length ? <p className="text-slate-500">暂无审计日志</p> : null}
        </div>
      </section>
    </div>
  )
}

function Metric({ title, value }: { title: string; value?: number }) {
  return (
    <div className="rounded-xl border bg-white p-4">
      <p className="text-xs text-slate-500">{title}</p>
      <p className="mt-1 text-2xl font-semibold">{value ?? 0}</p>
    </div>
  )
}

function AuditBlock({
  title,
  rows,
  onApprove,
  onReject,
}: {
  title: string
  type: 'SECONDHAND' | 'AGENT' | 'COMMENT'
  rows: Array<{ id: number; title?: string; content?: string; status?: string }>
  onApprove: (id: number) => void
  onReject: (id: number) => void
}) {
  return (
    <div className="mb-4">
      <h3 className="mb-2 text-sm font-medium">{title}</h3>
      {rows.length ? (
        <div className="space-y-2">
          {rows.map((x) => (
            <div key={x.id} className="flex items-center justify-between rounded border px-3 py-2 text-sm">
              <div>
                #{x.id} {x.title || x.content || '-'}
              </div>
              <div className="flex gap-2">
                <button className="rounded bg-emerald-600 px-2 py-1 text-white" onClick={() => onApprove(x.id)}>
                  通过
                </button>
                <button className="rounded bg-rose-600 px-2 py-1 text-white" onClick={() => onReject(x.id)}>
                  驳回
                </button>
              </div>
            </div>
          ))}
        </div>
      ) : (
        <p className="text-sm text-slate-500">暂无待审</p>
      )}
    </div>
  )
}
