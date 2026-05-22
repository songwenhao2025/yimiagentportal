// =====================================================
// 壹米AI Agent门户 - 技能服务
// =====================================================

import axios from 'axios'
import type { Skill } from '@/data/skills'

const skillRequest = axios.create({
  baseURL: 'http://localhost:8092',
  timeout: 60000,
  headers: {
    'Content-Type': 'application/json',
  },
})

skillRequest.interceptors.response.use(
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

export const skillService = {
  async list(params?: {
    page?: number
    size?: number
    category?: string
    status?: string
    keyword?: string
  }): Promise<{ list: Skill[]; total: number }> {
    return skillRequest.get('/api/skills', { params })
  },

  async get(id: string): Promise<Skill> {
    return skillRequest.get(`/api/skills/${id}`)
  },

  async create(data: Omit<Skill, 'id' | 'createdAt'>): Promise<Skill> {
    return skillRequest.post('/api/skills', data)
  },

  async update(id: string, data: Partial<Skill>): Promise<Skill> {
    return skillRequest.put(`/api/skills/${id}`, data)
  },

  async delete(id: string): Promise<void> {
    return skillRequest.delete(`/api/skills/${id}`)
  },

  async publish(id: string): Promise<Skill> {
    return skillRequest.post(`/api/skills/${id}/publish`)
  },

  async test(id: string, params: Record<string, any>): Promise<any> {
    return skillRequest.post(`/api/skills/${id}/test`, params)
  },
}