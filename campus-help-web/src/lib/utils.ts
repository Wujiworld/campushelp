import { clsx, type ClassValue } from 'clsx'
import { twMerge } from 'tailwind-merge'

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs))
}

export function formatYuan(cent: number) {
  return (cent / 100).toLocaleString('zh-CN', { style: 'currency', currency: 'CNY' })
}
