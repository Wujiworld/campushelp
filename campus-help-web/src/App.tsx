import type { ReactNode } from 'react'
import { Navigate, Route, Routes } from 'react-router-dom'
import { Layout } from '@/components/Layout'
import { ActivitiesPage } from '@/pages/ActivitiesPage'
import { ActivityDetailPage } from '@/pages/ActivityDetailPage'
import { ErrandNewPage } from '@/pages/ErrandNewPage'
import { HomePage } from '@/pages/HomePage'
import { SecondhandDetailPage } from '@/pages/SecondhandDetailPage'
import { SecondhandListPage } from '@/pages/SecondhandListPage'
import { SecondhandPublishPage } from '@/pages/SecondhandPublishPage'
import { TakeoutPage } from '@/pages/TakeoutPage'
import { LoginPage } from '@/pages/LoginPage'
import { MerchantOrdersPage } from '@/pages/MerchantOrdersPage'
import { OrdersPage } from '@/pages/OrdersPage'
import { AddressBookPage } from '@/pages/AddressBookPage'
import { LikedSecondhandPage } from '@/pages/LikedSecondhandPage'
import { MySecondhandPage } from '@/pages/MySecondhandPage'
import { ProfileEditPage } from '@/pages/ProfileEditPage'
import { ProfilePage } from '@/pages/ProfilePage'
import { RiderPoolPage } from '@/pages/RiderPoolPage'
import { StorePage } from '@/pages/StorePage'
import { AgentListPage } from '@/pages/AgentListPage'
import { AgentDetailPage } from '@/pages/AgentDetailPage'
import { AgentPublishPage } from '@/pages/AgentPublishPage'
import { MyCommentsPage } from '@/pages/MyCommentsPage'
import { AdminActivityNewPage } from '@/pages/AdminActivityNewPage'
import { useAuthStore } from '@/store/authStore'
import type { RoleCode } from '@/store/authStore'
import { ForbiddenPage } from '@/pages/ForbiddenPage'
import { AdminHomePage } from '@/pages/AdminHomePage'
import { AdminGovernancePage } from '@/pages/AdminGovernancePage'
import { FinancePage } from '@/pages/FinancePage'
import { FollowCenterPage } from '@/pages/FollowCenterPage'

function RequireAuth({ children }: { children: ReactNode }) {
  const token = useAuthStore((s) => s.token)
  if (!token) {
    return <Navigate to="/login" replace />
  }
  return <>{children}</>
}

function RequireRole({ children, allowedRoles }: { children: ReactNode; allowedRoles: RoleCode[] }) {
  const roles = useAuthStore((s) => s.roles)
  const ok = allowedRoles.some((r) => roles.includes(r))
  if (!ok) {
    return <Navigate to="/forbidden" replace />
  }
  return <>{children}</>
}

function RoleHomeRedirect() {
  const path = useAuthStore((s) => s.getRoleHomePath())
  return <Navigate to={path} replace />
}

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/forbidden" element={<ForbiddenPage />} />
      <Route
        element={
          <RequireAuth>
            <Layout />
          </RequireAuth>
        }
      >
        <Route path="/" element={<RoleHomeRedirect />} />
        <Route path="/student" element={<HomePage />} />
        <Route path="/takeout" element={<TakeoutPage />} />
        <Route path="/secondhand" element={<SecondhandListPage />} />
        <Route path="/secondhand/new" element={<SecondhandPublishPage />} />
        <Route path="/secondhand/:id" element={<SecondhandDetailPage />} />
        <Route path="/errand/new" element={<ErrandNewPage />} />
        <Route path="/activities" element={<ActivitiesPage />} />
        <Route path="/activities/:id" element={<ActivityDetailPage />} />
        <Route path="/agent" element={<AgentListPage />} />
        <Route path="/agent/new" element={<AgentPublishPage />} />
        <Route path="/agent/:id" element={<AgentDetailPage />} />
        <Route path="/store/:storeId" element={<StorePage />} />
        <Route path="/orders" element={<OrdersPage />} />
        <Route path="/profile" element={<ProfilePage />} />
        <Route path="/profile/addresses" element={<AddressBookPage />} />
        <Route path="/profile/edit" element={<ProfileEditPage />} />
        <Route path="/profile/my-items" element={<MySecondhandPage />} />
        <Route path="/profile/likes" element={<LikedSecondhandPage />} />
        <Route path="/profile/comments" element={<MyCommentsPage />} />
        <Route path="/profile/follow" element={<FollowCenterPage />} />
        <Route
          path="/profile/finance"
          element={
            <RequireRole allowedRoles={['MERCHANT', 'RIDER']}>
              <FinancePage />
            </RequireRole>
          }
        />
        <Route
          path="/merchant"
          element={
            <RequireRole allowedRoles={['MERCHANT']}>
              <MerchantOrdersPage />
            </RequireRole>
          }
        />
        <Route
          path="/rider"
          element={
            <RequireRole allowedRoles={['RIDER']}>
              <RiderPoolPage />
            </RequireRole>
          }
        />
        <Route
          path="/admin"
          element={
            <RequireRole allowedRoles={['ADMIN']}>
              <AdminHomePage />
            </RequireRole>
          }
        />
        <Route
          path="/admin/activities/new"
          element={
            <RequireRole allowedRoles={['ADMIN']}>
              <AdminActivityNewPage />
            </RequireRole>
          }
        />
        <Route
          path="/admin/governance"
          element={
            <RequireRole allowedRoles={['ADMIN']}>
              <AdminGovernancePage />
            </RequireRole>
          }
        />
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}
