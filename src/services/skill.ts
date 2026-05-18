// =====================================================
// 壹米AI Agent门户 - 技能服务
// =====================================================

import { http } from '@/utils/request'
import type { Skill } from '@/data/skills'

export const skillService = {
  /**
   * 获取技能列表
   */
  async list(params?: {
    page?: number
    size?: number
    category?: string
    status?: string
    keyword?: string
  }): Promise<{ list: Skill[]; total: number }> {
    return http.get('/api/skills', params)
  },

  /**
   * 获取技能详情
   */
  async get(id: string): Promise<Skill> {
    return http.get(`/api/skills/${id}`)
  },

  /**
   * 创建技能
   */
  async create(data: Omit<Skill, 'id' | 'createdAt'>): Promise<Skill> {
    return http.post('/api/skills', data)
  },

  /**
   * 更新技能
   */
  async update(id: string, data: Partial<Skill>): Promise<Skill> {
    return http.put(`/api/skills/${id}`, data)
  },

  /**
   * 删除技能
   */
  async delete(id: string): Promise<void> {
    return http.delete(`/api/skills/${id}`)
  },

  /**
   * 发布技能
   */
  async publish(id: string): Promise<Skill> {
    return http.post(`/api/skills/${id}/publish`)
  },

  /**
   * 测试技能
   */
  async test(id: string, params: Record<string, any>): Promise<any> {
    return http.post(`/api/skills/${id}/test`, params)
  },
}