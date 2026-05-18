// =====================================================
// 壹米AI Agent门户 - 知识库服务
// =====================================================

import { http } from '@/utils/request'
import type { KnowledgeDocument } from '@/data/knowledge'

export const knowledgeService = {
  /**
   * 获取文档列表
   */
  async list(params?: {
    page?: number
    size?: number
    category?: string
    status?: string
    keyword?: string
  }): Promise<{ list: KnowledgeDocument[]; total: number }> {
    return http.get('/api/knowledge', params)
  },

  /**
   * 获取文档详情
   */
  async get(id: string): Promise<KnowledgeDocument> {
    return http.get(`/api/knowledge/${id}`)
  },

  /**
   * 上传文档
   */
  async upload(file: File, category?: string): Promise<KnowledgeDocument> {
    const formData = new FormData()
    formData.append('file', file)
    if (category) {
      formData.append('category', category)
    }
    return http.post('/api/knowledge/upload', formData)
  },

  /**
   * 更新文档
   */
  async update(id: string, data: Partial<KnowledgeDocument>): Promise<KnowledgeDocument> {
    return http.put(`/api/knowledge/${id}`, data)
  },

  /**
   * 删除文档
   */
  async delete(id: string): Promise<void> {
    return http.delete(`/api/knowledge/${id}`)
  },

  /**
   * 搜索文档
   */
  async search(query: string, params?: { limit?: number }): Promise<KnowledgeDocument[]> {
    return http.get('/api/knowledge/search', { query, ...params })
  },

  /**
   * 获取分类列表
   */
  async getCategories(): Promise<{ id: string; name: string; count: number }[]> {
    return http.get('/api/knowledge/categories')
  },
}