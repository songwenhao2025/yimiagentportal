// =====================================================
// 壹米AI Agent门户 - 知识库服务
// =====================================================

import axios from 'axios'
import type { KnowledgeDocument } from '@/data/knowledge'

const knowledgeRequest = axios.create({
  baseURL: 'http://localhost:8093',
  timeout: 60000,
  headers: {
    'Content-Type': 'application/json',
  },
})

knowledgeRequest.interceptors.response.use(
  (response) => {
    const { data } = response
    if (data.code === 200 || data.success) {
      return data.data || data
    } else {
      uni.showToast({ title: data.message || '请求失败', icon: 'error' })
      return Promise.reject(data)
    }
  },
  (error) => {
    if (!error.response) {
      uni.showToast({ title: '网络异常', icon: 'none' })
    }
    return Promise.reject(error)
  }
)

export const knowledgeService = {
  async list(params?: {
    page?: number
    size?: number
    category?: string
    status?: string
    keyword?: string
  }): Promise<{ list: KnowledgeDocument[]; total: number }> {
    return knowledgeRequest.get('/api/knowledge', { params })
  },

  async get(id: string): Promise<KnowledgeDocument> {
    return knowledgeRequest.get(`/api/knowledge/${id}`)
  },

  async create(data: { title: string; category?: string; content?: string; type?: string }): Promise<KnowledgeDocument> {
    return knowledgeRequest.post('/api/knowledge', data)
  },

  async upload(file: File, category?: string): Promise<KnowledgeDocument> {
    const formData = new FormData()
    formData.append('file', file)
    if (category) {
      formData.append('category', category)
    }
    return knowledgeRequest.post('/api/knowledge/upload', formData)
  },

  async update(id: string, data: Partial<KnowledgeDocument>): Promise<KnowledgeDocument> {
    return knowledgeRequest.put(`/api/knowledge/${id}`, data)
  },

  async delete(id: string): Promise<void> {
    return knowledgeRequest.delete(`/api/knowledge/${id}`)
  },

  async search(query: string, params?: { limit?: number }): Promise<KnowledgeDocument[]> {
    return knowledgeRequest.get('/api/knowledge/search', { params: { query, ...params } })
  },

  async getCategories(): Promise<{ id: string; name: string; count: number }[]> {
    return knowledgeRequest.get('/api/knowledge/categories')
  },
}