// =====================================================
// 壹米AI Agent门户 - 工作流服务
// =====================================================

import axios from 'axios'
import type { Workflow, WorkflowNode, WorkflowEdge } from '@/data/workflows'

const workflowRequest = axios.create({
  baseURL: 'http://localhost:8084',
  timeout: 60000,
  headers: {
    'Content-Type': 'application/json',
  },
})

workflowRequest.interceptors.response.use(
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
  async list(params?: {
    page?: number
    size?: number
    status?: string
    keyword?: string
  }): Promise<{ list: Workflow[]; total: number }> {
    return workflowRequest.get('/api/workflows', { params })
  },

  async get(id: string): Promise<Workflow> {
    return workflowRequest.get(`/api/workflows/${id}`)
  },

  async create(data: WorkflowCreateInput): Promise<Workflow> {
    return workflowRequest.post('/api/workflows', data)
  },

  async update(id: string, data: Partial<Workflow>): Promise<Workflow> {
    return workflowRequest.put(`/api/workflows/${id}`, data)
  },

  async delete(id: string): Promise<void> {
    return workflowRequest.delete(`/api/workflows/${id}`)
  },

  async activate(id: string): Promise<Workflow> {
    return workflowRequest.post(`/api/workflows/${id}/activate`)
  },

  async deactivate(id: string): Promise<Workflow> {
    return workflowRequest.post(`/api/workflows/${id}/deactivate`)
  },

  async execute(id: string, input?: Record<string, any>): Promise<WorkflowExecutionResult> {
    return workflowRequest.post(`/api/workflows/${id}/execute`, input)
  },

  async getExecutions(id: string, params?: { page?: number; size?: number }): Promise<{ list: WorkflowExecutionResult[]; total: number }> {
    return workflowRequest.get(`/api/workflows/${id}/executions`, { params })
  },
}