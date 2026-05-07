import { api, unwrap } from './http'
import type { ApiResult } from './types'

export interface OssPresignResult {
  uploadUrl: string
  objectKey: string
  objectUrl: string
  expireAtEpochSeconds: number
}

export async function requestOssPresign(contentType?: string, fileExtension?: string) {
  const res = await api.post<ApiResult<OssPresignResult>>('/api/v3/oss/presign', {
    contentType,
    fileExtension,
  })
  return unwrap<OssPresignResult>(res)
}

/** 直传 OSS；失败时抛出（含 503 未启用） */
export async function uploadFileToOss(file: File, presign: OssPresignResult) {
  const ct = file.type || 'application/octet-stream'
  const r = await fetch(presign.uploadUrl, {
    method: 'PUT',
    headers: { 'Content-Type': ct },
    body: file,
  })
  if (!r.ok) {
    throw new Error(`上传失败 HTTP ${r.status}`)
  }
  return presign.objectUrl
}
