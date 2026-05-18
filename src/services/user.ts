// =====================================================
// 壹米AI Agent门户 - 用户服务
// =====================================================

import { http } from '@/utils/request'
import type { User } from '@/data/admin'

export interface LoginInput {
  username: string
  password: string
}

export interface LoginResult {
  token: string
  user: User
}

export const userService = {
  /**
   * 用户登录
   */
  async login(data: LoginInput): Promise<LoginResult> {
    return http.post('/api/auth/login', data)
  },

  /**
   * 用户登出
   */
  async logout(): Promise<void> {
    return http.post('/api/auth/logout')
  },

  /**
   * 获取当前用户信息
   */
  async getCurrentUser(): Promise<User> {
    return http.get('/api/auth/me')
  },

  /**
   * 获取用户列表
   */
  async list(params?: {
    page?: number
    size?: number
    department?: string
    role?: string
    keyword?: string
  }): Promise<{ list: User[]; total: number }> {
    return http.get('/api/users', params)
  },

  /**
   * 获取用户详情
   */
  async get(id: string): Promise<User> {
    return http.get(`/api/users/${id}`)
  },

  /**
   * 创建用户
   */
  async create(data: Omit<User, 'id' | 'createdAt'>): Promise<User> {
    return http.post('/api/users', data)
  },

  /**
   * 更新用户
   */
  async update(id: string, data: Partial<User>): Promise<User> {
    return http.put(`/api/users/${id}`, data)
  },

  /**
   * 删除用户
   */
  async delete(id: string): Promise<void> {
    return http.delete(`/api/users/${id}`)
  },

  /**
   * 重置密码
   */
  async resetPassword(id: string, password: string): Promise<void> {
    return http.put(`/api/users/${id}/password`, { password })
  },
}