// =====================================================
// 壹米AI Agent门户 - 管理服务（审计日志、成本记录等）
// =====================================================

import { http } from '@/utils/request'
import type { AuditLog, CostRecord } from '@/data/admin'

export const adminService = {
  /**
   * 获取审计日志列表
   */
  async getAuditLogs(params?: {
    page?: number
    size?: number
    userId?: string
    action?: string
    resource?: string
    startTime?: string
    endTime?: string
  }): Promise<{ list: AuditLog[]; total: number }> {
    return http.get('/api/admin/logs', params)
  },

  /**
   * 获取成本记录列表
   */
  async getCostRecords(params?: {
    page?: number
    size?: number
    department?: string
    agentId?: string
    startTime?: string
    endTime?: string
  }): Promise<{ list: CostRecord[]; total: number }> {
    return http.get('/api/admin/costs', params)
  },

  /**
   * 获取统计数据
   */
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
    return http.get('/api/admin/statistics', params)
  },

  /**
   * 获取部门列表
   */
  async getDepartments(): Promise<{ id: string; name: string }[]> {
    return http.get('/api/admin/departments')
  },

  /**
   * 获取角色列表
   */
  async getRoles(): Promise<{ id: string; name: string; description: string }[]> {
    return http.get('/api/admin/roles')
  },

  /**
   * 获取调用趋势数据
   */
  async getCallTrends(days: number = 7): Promise<number[]> {
    return http.get('/api/admin/dashboard/trends', { days })
  },

  /**
   * 获取Agent排行榜
   */
  async getAgentRanking(topN: number = 5): Promise<{ name: string; department: string; count: number }[]> {
    return http.get('/api/admin/dashboard/agent-ranking', { topN })
  },

  /**
   * 获取技能排行榜
   */
  async getSkillRanking(topN: number = 5): Promise<{ name: string; category: string; count: number }[]> {
    return http.get('/api/admin/dashboard/skill-ranking', { topN })
  },
}