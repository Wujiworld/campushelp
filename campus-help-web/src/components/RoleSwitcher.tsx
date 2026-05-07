import { useNavigate } from 'react-router-dom'
import { useAuthStore, type RoleCode } from '@/store/authStore'

const ROLE_LABEL: Record<RoleCode, string> = {
  STUDENT: '学生',
  MERCHANT: '商家',
  RIDER: '骑手',
  ADMIN: '管理员',
}

export function RoleSwitcher() {
  const navigate = useNavigate()
  const roles = useAuthStore((s) => s.roles)
  const activeRole = useAuthStore((s) => s.activeRole)
  const setActiveRole = useAuthStore((s) => s.setActiveRole)

  if (roles.length <= 1) {
    return null
  }

  return (
    <label className="inline-flex items-center gap-2 text-xs text-slate-500">
      角色
      <select
        value={activeRole ?? roles[0]}
        onChange={(e) => {
          const next = e.target.value as RoleCode
          setActiveRole(next)
          navigate(useAuthStore.getState().getRoleHomePath(), { replace: true })
        }}
        className="rounded-lg border border-slate-200 bg-white px-2 py-1 text-xs font-medium text-slate-700 outline-none transition focus:border-teal-400"
      >
        {roles.map((role) => (
          <option key={role} value={role}>
            {ROLE_LABEL[role]}
          </option>
        ))}
      </select>
    </label>
  )
}
