import { api, unwrap } from './http'

export interface RoleApplication {
  id: number
  userId: number
  roleCode: string
  status: 'PENDING' | 'APPROVED' | 'REJECTED'
  docType?: string
  docNo?: string
  submitRemark?: string
  auditRemark?: string
  createdAt?: string
}

export interface AuditLog {
  id: number
  operatorUserId?: number
  operatorRole?: string
  action: string
  targetType: string
  targetId?: string
  detail?: string
  createdAt?: string
}

export async function fetchRoleApplications(status = 'PENDING', limit = 50) {
  const res = await api.get('/api/v3/admin/role-applications', { params: { status, limit } })
  return unwrap<RoleApplication[]>(res)
}

export async function approveRoleApplication(id: number, remark: string) {
  const res = await api.post(`/api/v3/admin/role-applications/${id}/approve`, { remark })
  return unwrap<RoleApplication>(res)
}

export async function rejectRoleApplication(id: number, remark: string) {
  const res = await api.post(`/api/v3/admin/role-applications/${id}/reject`, { remark })
  return unwrap<RoleApplication>(res)
}

export async function fetchAdminDashboard() {
  const res = await api.get('/api/v3/admin/system/dashboard')
  return unwrap<Record<string, number>>(res)
}

export async function fetchAuditLogs(limit = 100) {
  const res = await api.get('/api/v3/admin/system/audit-logs', { params: { limit } })
  return unwrap<AuditLog[]>(res)
}

export async function fetchPendingContent(limit = 50) {
  const res = await api.get('/api/v3/admin/content/pending', { params: { limit } })
  return unwrap<Record<string, Array<{ id: number; title?: string; content?: string; status?: string }>>>(res)
}

export async function approveContent(contentType: 'SECONDHAND' | 'AGENT' | 'COMMENT', id: number) {
  const res = await api.post(`/api/v3/admin/content/${contentType}/${id}/approve`)
  return unwrap<void>(res)
}

export async function rejectContent(contentType: 'SECONDHAND' | 'AGENT' | 'COMMENT', id: number) {
  const res = await api.post(`/api/v3/admin/content/${contentType}/${id}/reject`)
  return unwrap<void>(res)
}
