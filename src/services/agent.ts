// =====================================================
// 壹米AI Agent门户 - Agent服务
// =====================================================

import { http } from '@/utils/request'
import type { Agent } from '@/data/agents'

export const agentService = {
  /**
   * 获取Agent列表
   */
  async list(params?: {
    page?: number
    size?: number
    department?: string
    status?: string
    keyword?: string
  }): Promise<{ list: Agent[]; total: number }> {
    return http.get('/api/agents', params)
  },

  /**
   * 获取Agent详情
   */
  async get(id: string): Promise<Agent> {
    return http.get(`/api/agents/${id}`)
  },

  /**
   * 创建Agent
   */
  async create(data: Omit<Agent, 'id' | 'createdAt'>): Promise<Agent> {
    return http.post('/api/agents', data)
  },

  /**
   * 更新Agent
   */
  async update(id: string, data: Partial<Agent>): Promise<Agent> {
    return http.put(`/api/agents/${id}`, data)
  },

  /**
   * 删除Agent
   */
  async delete(id: string): Promise<void> {
    return http.delete(`/api/agents/${id}`)
  },

  /**
   * 收藏Agent
   */
  async favorite(id: string): Promise<void> {
    return http.post(`/api/agents/${id}/favorite`)
  },

  /**
   * 取消收藏Agent
   */
  async unfavorite(id: string): Promise<void> {
    return http.delete(`/api/agents/${id}/favorite`)
  },

  /**
   * 获取Agent调用记录
   */
  async getCallLogs(id: string, params?: { page?: number; size?: number }): Promise<{ list: any[]; total: number }> {
    return http.get(`/api/agents/${id}/logs`, params)
  },

  /**
   * 调用Agent
   */
  async call(id: string, input: string): Promise<{ output: string; duration: number }> {
    return http.post(`/api/agents/${id}/call`, { input })
  },
}