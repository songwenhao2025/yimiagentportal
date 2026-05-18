// =====================================================
// 壹米AI Agent门户 - 工作流服务
// =====================================================

import { http } from '@/utils/request'
import type { Workflow, WorkflowNode, WorkflowEdge } from '@/data/workflows'

export interface WorkflowCreateInput {
  name: string
  description?: string
  nodes: WorkflowNode[]
  edges: WorkflowEdge[]
  triggerType: 'api' | 'cron' | 'event'
  cronExpression?: string
}

export interface WorkflowExecutionResult {
  id: string
  workflowId: string
  status: 'running' | 'completed' | 'failed' | 'cancelled'
  startTime: string
  endTime?: string
  duration?: number
  output?: any
  errorMessage?: string
}

export const workflowService = {
  /**
   * 获取工作流列表
   */
  async list(params?: {
    page?: number
    size?: number
    status?: string
    keyword?: string
  }): Promise<{ list: Workflow[]; total: number }> {
    return http.get('/api/workflows', params)
  },

  /**
   * 获取工作流详情
   */
  async get(id: string): Promise<Workflow> {
    return http.get(`/api/workflows/${id}`)
  },

  /**
   * 创建工作流
   */
  async create(data: WorkflowCreateInput): Promise<Workflow> {
    return http.post('/api/workflows', data)
  },

  /**
   * 更新工作流
   */
  async update(id: string, data: Partial<Workflow>): Promise<Workflow> {
    return http.put(`/api/workflows/${id}`, data)
  },

  /**
   * 删除工作流
   */
  async delete(id: string): Promise<void> {
    return http.delete(`/api/workflows/${id}`)
  },

  /**
   * 启动工作流
   */
  async activate(id: string): Promise<Workflow> {
    return http.post(`/api/workflows/${id}/activate`)
  },

  /**
   * 停用工作流
   */
  async deactivate(id: string): Promise<Workflow> {
    return http.post(`/api/workflows/${id}/deactivate`)
  },

  /**
   * 执行工作流
   */
  async execute(id: string, input?: Record<string, any>): Promise<WorkflowExecutionResult> {
    return http.post(`/api/workflows/${id}/execute`, input)
  },

  /**
   * 获取工作流执行记录
   */
  async getExecutions(id: string, params?: { page?: number; size?: number }): Promise<{ list: WorkflowExecutionResult[]; total: number }> {
    return http.get(`/api/workflows/${id}/executions`, params)
  },
}