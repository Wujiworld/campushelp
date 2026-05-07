import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { ArrowLeft, Loader2, MapPin, Plus, Star, Trash2 } from 'lucide-react'
import { useState } from 'react'
import { Link } from 'react-router-dom'
import {
  type AddressRequestBody,
  type ChAddress,
  createAddress,
  deleteAddress,
  fetchAddresses,
  setDefaultAddress,
  updateAddress,
} from '@/api/address'
import { fetchCampuses } from '@/api/product'

const emptyForm: AddressRequestBody = {
  campusId: 0,
  contactName: '',
  contactPhone: '',
  detail: '',
  label: '',
  defaultAddress: false,
}

export function AddressBookPage() {
  const qc = useQueryClient()
  const { data: campuses } = useQuery({ queryKey: ['campuses'], queryFn: fetchCampuses })
  const { data: list, isLoading, error } = useQuery({
    queryKey: ['addresses'],
    queryFn: fetchAddresses,
  })

  const [editingId, setEditingId] = useState<number | null>(null)
  const [showForm, setShowForm] = useState(false)
  const [form, setForm] = useState<AddressRequestBody>({ ...emptyForm })

  const defaultCampus = campuses?.[0]?.id ?? 0

  const resetForm = () => {
    setForm({
      ...emptyForm,
      campusId: defaultCampus,
    })
    setEditingId(null)
  }

  const openAdd = () => {
    resetForm()
    setForm((f) => ({ ...f, campusId: defaultCampus || f.campusId }))
    setShowForm(true)
  }

  const openEdit = (a: ChAddress) => {
    setEditingId(a.id)
    setForm({
      campusId: a.campusId,
      buildingId: a.buildingId ?? undefined,
      contactName: a.contactName,
      contactPhone: a.contactPhone,
      detail: a.detail,
      label: a.label ?? '',
      defaultAddress: a.isDefault === 1,
    })
    setShowForm(true)
  }

  const saveMut = useMutation({
    mutationFn: async () => {
      if (!form.campusId) throw new Error('请选择校区')
      if (editingId != null) return updateAddress(editingId, form)
      return createAddress(form)
    },
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['addresses'] })
      setShowForm(false)
      resetForm()
    },
  })

  const delMut = useMutation({
    mutationFn: (id: number) => deleteAddress(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['addresses'] }),
  })

  const defMut = useMutation({
    mutationFn: (id: number) => setDefaultAddress(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['addresses'] }),
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
      <p className="text-red-700">{error instanceof Error ? error.message : '加载失败'}</p>
    )
  }

  return (
    <div className="mx-auto max-w-lg">
      <div className="mb-6 flex items-center gap-3">
        <Link
          to="/profile"
          className="inline-flex h-9 w-9 items-center justify-center rounded-full bg-slate-100 text-slate-700 hover:bg-slate-200"
        >
          <ArrowLeft className="h-4 w-4" />
        </Link>
        <div>
          <h1 className="text-lg font-semibold text-slate-900">收货地址</h1>
          <p className="text-xs text-slate-500">二手配送到寝、外卖等共用</p>
        </div>
      </div>

      <button
        type="button"
        onClick={() => openAdd()}
        className="mb-4 flex w-full items-center justify-center gap-2 rounded-2xl border-2 border-dashed border-amber-300 bg-amber-50/50 py-3 text-sm font-medium text-amber-900 hover:bg-amber-50"
      >
        <Plus className="h-4 w-4" />
        新增地址
      </button>

      {showForm && (
        <div className="mb-6 rounded-2xl border border-slate-200 bg-white p-4 shadow-sm">
          <p className="text-sm font-medium text-slate-800">{editingId != null ? '编辑地址' : '新地址'}</p>
          <div className="mt-3 space-y-3">
            <label className="block text-xs text-slate-600">
              校区
              <select
                className="mt-1 w-full rounded-lg border border-slate-200 px-3 py-2 text-sm"
                value={form.campusId || ''}
                onChange={(e) => setForm((f) => ({ ...f, campusId: Number(e.target.value) }))}
              >
                <option value="">选择校区</option>
                {campuses?.map((c) => (
                  <option key={c.id} value={c.id}>
                    {c.name}
                  </option>
                ))}
              </select>
            </label>
            <label className="block text-xs text-slate-600">
              楼栋 ID（可选）
              <input
                type="number"
                className="mt-1 w-full rounded-lg border border-slate-200 px-3 py-2 text-sm"
                value={form.buildingId ?? ''}
                onChange={(e) =>
                  setForm((f) => ({
                    ...f,
                    buildingId: e.target.value ? Number(e.target.value) : undefined,
                  }))
                }
                placeholder="无则留空"
              />
            </label>
            <label className="block text-xs text-slate-600">
              联系人
              <input
                className="mt-1 w-full rounded-lg border border-slate-200 px-3 py-2 text-sm"
                value={form.contactName}
                onChange={(e) => setForm((f) => ({ ...f, contactName: e.target.value }))}
              />
            </label>
            <label className="block text-xs text-slate-600">
              手机号
              <input
                className="mt-1 w-full rounded-lg border border-slate-200 px-3 py-2 text-sm"
                value={form.contactPhone}
                onChange={(e) => setForm((f) => ({ ...f, contactPhone: e.target.value }))}
              />
            </label>
            <label className="block text-xs text-slate-600">
              详细地址
              <input
                className="mt-1 w-full rounded-lg border border-slate-200 px-3 py-2 text-sm"
                value={form.detail}
                onChange={(e) => setForm((f) => ({ ...f, detail: e.target.value }))}
                placeholder="寝室号、门牌等"
              />
            </label>
            <label className="block text-xs text-slate-600">
              标签（家 / 学校 等）
              <input
                className="mt-1 w-full rounded-lg border border-slate-200 px-3 py-2 text-sm"
                value={form.label ?? ''}
                onChange={(e) => setForm((f) => ({ ...f, label: e.target.value }))}
              />
            </label>
            <label className="flex items-center gap-2 text-sm text-slate-700">
              <input
                type="checkbox"
                checked={!!form.defaultAddress}
                onChange={(e) => setForm((f) => ({ ...f, defaultAddress: e.target.checked }))}
              />
              设为默认地址
            </label>
          </div>
          <div className="mt-4 flex gap-2">
            <button
              type="button"
              disabled={saveMut.isPending}
              onClick={() => saveMut.mutate()}
              className="flex-1 rounded-xl bg-amber-500 py-2.5 text-sm font-medium text-white hover:bg-amber-600 disabled:opacity-50"
            >
              {saveMut.isPending ? '保存中…' : '保存'}
            </button>
            <button
              type="button"
              onClick={() => {
                setShowForm(false)
                resetForm()
              }}
              className="rounded-xl border border-slate-200 px-4 py-2.5 text-sm text-slate-700 hover:bg-slate-50"
            >
              取消
            </button>
          </div>
          {saveMut.isError && (
            <p className="mt-2 text-xs text-red-600">
              {saveMut.error instanceof Error ? saveMut.error.message : '保存失败'}
            </p>
          )}
        </div>
      )}

      <ul className="space-y-3">
        {list?.map((a) => (
          <li
            key={a.id}
            className="rounded-2xl border border-slate-200 bg-white p-4 shadow-sm"
          >
            <div className="flex items-start justify-between gap-2">
              <div className="flex gap-2">
                <MapPin className="mt-0.5 h-4 w-4 shrink-0 text-amber-600" />
                <div>
                  <p className="font-medium text-slate-900">
                    {a.contactName}{' '}
                    <span className="font-normal text-slate-600">{a.contactPhone}</span>
                  </p>
                  {a.label && (
                    <span className="mt-1 inline-block rounded-full bg-slate-100 px-2 py-0.5 text-xs text-slate-600">
                      {a.label}
                    </span>
                  )}
                  <p className="mt-2 text-sm text-slate-600">{a.detail}</p>
                  <p className="mt-1 text-xs text-slate-400">校区 #{a.campusId}</p>
                </div>
              </div>
              {a.isDefault === 1 && (
                <span className="flex shrink-0 items-center gap-0.5 rounded-full bg-amber-100 px-2 py-0.5 text-xs font-medium text-amber-800">
                  <Star className="h-3 w-3 fill-current" />
                  默认
                </span>
              )}
            </div>
            <div className="mt-4 flex flex-wrap gap-2 border-t border-slate-100 pt-3">
              {a.isDefault !== 1 && (
                <button
                  type="button"
                  onClick={() => defMut.mutate(a.id)}
                  disabled={defMut.isPending}
                  className="text-xs font-medium text-amber-700 hover:underline disabled:opacity-50"
                >
                  设为默认
                </button>
              )}
              <button
                type="button"
                onClick={() => openEdit(a)}
                className="text-xs font-medium text-slate-700 hover:underline"
              >
                编辑
              </button>
              <button
                type="button"
                onClick={() => {
                  if (confirm('确定删除该地址？')) delMut.mutate(a.id)
                }}
                className="inline-flex items-center gap-1 text-xs font-medium text-red-600 hover:underline"
              >
                <Trash2 className="h-3 w-3" />
                删除
              </button>
            </div>
          </li>
        ))}
      </ul>

      {!list?.length && !showForm && (
        <p className="py-8 text-center text-sm text-slate-500">暂无地址，点击上方新增</p>
      )}
    </div>
  )
}
