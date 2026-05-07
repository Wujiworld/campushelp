import { api, unwrap } from './http'
import type { ApiResult } from './types'

export interface CommentView {
  id: number
  userId: number
  content: string
  createdAt?: string
}

export interface MyCommentView extends CommentView {
  targetType: string
  targetId: number
}

export async function fetchComments(targetType: string, targetId: number, page = 0, size = 20) {
  const res = await api.get<ApiResult<CommentView[]>>('/api/v3/comments', {
    params: { targetType, targetId, page, size },
  })
  return unwrap<CommentView[]>(res)
}

export async function postComment(body: { targetType: string; targetId: number; content: string }) {
  const res = await api.post<ApiResult<CommentView>>('/api/v3/comments', body)
  return unwrap<CommentView>(res)
}

export async function fetchMyComments(page = 0, size = 20) {
  const res = await api.get<ApiResult<MyCommentView[]>>('/api/v3/comments/mine', { params: { page, size } })
  return unwrap<MyCommentView[]>(res)
}
