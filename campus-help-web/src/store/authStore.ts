import { create } from 'zustand'
import { persist } from 'zustand/middleware'

export type RoleCode = 'STUDENT' | 'MERCHANT' | 'RIDER' | 'ADMIN'

const ROLE_HOME_PATH: Record<RoleCode, string> = {
  STUDENT: '/student',
  MERCHANT: '/merchant',
  RIDER: '/rider',
  ADMIN: '/admin',
}

function asRoleCodes(roles?: string[]): RoleCode[] {
  if (!Array.isArray(roles)) {
    return []
  }
  return roles.filter((r): r is RoleCode => r in ROLE_HOME_PATH)
}

interface AuthState {
  token: string | null
  userId: number | null
  roles: RoleCode[]
  activeRole: RoleCode | null
  setAuth: (token: string, userId: number, roles?: string[]) => void
  setActiveRole: (role: RoleCode) => void
  resolveDefaultRole: (roles?: string[]) => RoleCode
  getRoleHomePath: () => string
  logout: () => void
  hasRole: (code: string) => boolean
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      token: null,
      userId: null,
      roles: [],
      activeRole: null,
      setAuth: (token, userId, roles) =>
        set((state) => {
          const roleList = asRoleCodes(roles)
          const remembered = state.activeRole
          const activeRole = remembered && roleList.includes(remembered) ? remembered : roleList[0] ?? 'STUDENT'
          return { token, userId, roles: roleList, activeRole }
        }),
      setActiveRole: (role) =>
        set((state) => {
          if (!state.roles.includes(role)) {
            return {}
          }
          return { activeRole: role }
        }),
      resolveDefaultRole: (roles) => {
        const roleList = asRoleCodes(roles)
        const remembered = get().activeRole
        if (remembered && roleList.includes(remembered)) {
          return remembered
        }
        return roleList[0] ?? 'STUDENT'
      },
      getRoleHomePath: () => {
        const s = get()
        const active = s.activeRole
        if (active && s.roles.includes(active)) {
          return ROLE_HOME_PATH[active]
        }
        const fallback = s.roles[0] ?? 'STUDENT'
        return ROLE_HOME_PATH[fallback]
      },
      logout: () => set({ token: null, userId: null, roles: [], activeRole: null }),
      hasRole: (code: string) => {
        const r = get().roles
        return Array.isArray(r) && (code in ROLE_HOME_PATH ? r.includes(code as RoleCode) : false)
      },
    }),
    { name: 'campus-help-auth' },
  ),
)
