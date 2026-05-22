// =====================================================
// 壹米AI Agent门户 - 管理服务（审计日志、成本记录等）
// =====================================================

import axios from 'axios'
import type { AuditLog, CostRecord } from '@/data/admin'

const adminRequest = axios.create({
  baseURL: 'http://localhost:8090',
  timeout: 60000,
  headers: {
    'Content-Type': 'application/json',
  },
})

adminRequest.interceptors.response.use(
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

export const adminService = {
  async getAuditLogs(params?: {
    page?: number
    size?: number
    userId?: string
    action?: string
    resource?: string
    startTime?: string
    endTime?: string
  }): Promise<{ list: AuditLog[]; total: number }> {
    return adminRequest.get('/api/admin/logs', { params })
  },

  async getCostRecords(params?: {
    page?: number
    size?: number
    department?: string
    agentId?: string
    startTime?: string
    endTime?: string
  }): Promise<{ list: CostRecord[]; total: number }> {
    return adminRequest.get('/api/admin/costs', { params })
  },

  async getStatistics(params?: {
    startTime?: string
    endTime?: string
    department?: string
  }): Promise<{
    totalAgents: number
    totalWorkflows: number
    totalSkills: number
    totalDocuments: number
    totalCalls: number
    totalCost: number
    activeUsers: number
  }> {
    return adminRequest.get('/api/admin/statistics', { params })
  },

  async getDepartments(): Promise<{ id: string; name: string }[]> {
    return adminRequest.get('/api/admin/departments')
  },

  async getRoles(): Promise<{ id: string; name: string; description: string }[]> {
    return adminRequest.get('/api/admin/roles')
  },

  async getCallTrends(days: number = 7): Promise<number[]> {
    return adminRequest.get('/api/admin/dashboard/trends', { params: { days } })
  },

  async getAgentRanking(topN: number = 5): Promise<{ name: string; department: string; count: number }[]> {
    return adminRequest.get('/api/admin/dashboard/agent-ranking', { params: { topN } })
  },

  async getSkillRanking(topN: number = 5): Promise<{ name: string; category: string; count: number }[]> {
    return adminRequest.get('/api/admin/dashboard/skill-ranking', { params: { topN } })
  },

  async getSatisfactionData(): Promise<{ averageScore: number; ratingDistribution: number[]; totalRatings: number }> {
    return adminRequest.get('/api/admin/dashboard/satisfaction')
  },
}