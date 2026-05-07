import { useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { applyWithdraw, fetchBalance, fetchMyWithdraws } from '@/api/finance'
import { useAuthStore } from '@/store/authStore'

export function FinancePage() {
  const qc = useQueryClient()
  const activeRole = useAuthStore((s) => s.activeRole)
  const role = activeRole === 'MERCHANT' || activeRole === 'RIDER' ? activeRole : 'MERCHANT'
  const [amount, setAmount] = useState(100)
  const [accountNo, setAccountNo] = useState('')
  const [accountName, setAccountName] = useState('')

  const { data: balance } = useQuery({
    queryKey: ['finance-balance', role],
    queryFn: () => fetchBalance(role),
  })
  const { data: withdraws } = useQuery({
    queryKey: ['finance-withdraws'],
    queryFn: fetchMyWithdraws,
  })
  const applyMut = useMutation({
    mutationFn: () => applyWithdraw({ role, amountCent: amount, accountNo, accountName }),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['finance-balance'] })
      qc.invalidateQueries({ queryKey: ['finance-withdraws'] })
      setAmount(100)
    },
  })

  return (
    <div className="mx-auto max-w-2xl space-y-5">
      <h1 className="text-2xl font-semibold">资金与提现</h1>
      <section className="rounded-xl border bg-white p-4">
        <p className="text-sm text-slate-500">当前角色：{role}</p>
        <p className="mt-1 text-xl font-semibold">可提现余额：{balance?.availableAmountCent ?? 0} 分</p>
      </section>

      <section className="rounded-xl border bg-white p-4">
        <h2 className="mb-3 font-medium">发起提现</h2>
        <div className="grid gap-3">
          <input
            className="rounded border px-3 py-2"
            type="number"
            value={amount}
            onChange={(e) => setAmount(Number(e.target.value))}
            placeholder="提现金额（分）"
          />
          <input className="rounded border px-3 py-2" value={accountNo} onChange={(e) => setAccountNo(e.target.value)} placeholder="收款账号" />
          <input className="rounded border px-3 py-2" value={accountName} onChange={(e) => setAccountName(e.target.value)} placeholder="收款人" />
          <button
            className="rounded bg-slate-900 px-3 py-2 text-white disabled:opacity-50"
            onClick={() => applyMut.mutate()}
            disabled={!accountNo || !accountName || amount <= 0 || applyMut.isPending}
          >
            {applyMut.isPending ? '提交中...' : '提交提现申请'}
          </button>
        </div>
      </section>

      <section className="rounded-xl border bg-white p-4">
        <h2 className="mb-3 font-medium">提现记录</h2>
        <div className="space-y-2 text-sm">
          {withdraws?.map((w) => (
            <div key={w.id} className="rounded border px-3 py-2">
              #{w.id} · {w.amountCent} 分 · {w.status}
            </div>
          ))}
          {!withdraws?.length ? <p className="text-slate-500">暂无记录</p> : null}
        </div>
      </section>
    </div>
  )
}
