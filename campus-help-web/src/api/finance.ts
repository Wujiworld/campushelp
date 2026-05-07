import { api, unwrap } from './http'

export interface WithdrawApply {
  id: number
  userId: number
  userRole: string
  amountCent: number
  status: string
  accountNo: string
  accountName: string
  remark?: string
  createdAt?: string
}

export async function fetchBalance(role: 'MERCHANT' | 'RIDER') {
  const res = await api.get('/api/v3/finance/balance', { params: { role } })
  return unwrap<{ availableAmountCent: number; role: string; userId: number }>(res)
}

export async function applyWithdraw(body: {
  role: 'MERCHANT' | 'RIDER'
  amountCent: number
  accountNo: string
  accountName: string
  remark?: string
}) {
  const res = await api.post('/api/v3/finance/withdraws', body)
  return unwrap<WithdrawApply>(res)
}

export async function fetchMyWithdraws() {
  const res = await api.get('/api/v3/finance/withdraws/mine')
  return unwrap<WithdrawApply[]>(res)
}
