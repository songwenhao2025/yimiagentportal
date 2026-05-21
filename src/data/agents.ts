export interface Agent {
  id: string
  name: string
  description: string
  department: 'operation' | 'qc' | 'customer' | 'finance'
  tags: string[]
  successRate: number
  avgTime: number
  dailyCalls: number
  usageCount: number
  creatorId: string
  createdAt: string
  status: 'online' | 'offline' | 'pending'
  isFavorite?: boolean
  rating?: number
}

export const departments = [
  { id: 'all', name: '全部', icon: '🏢' },
  { id: 'operation', name: '运营', icon: '🚚' },
  { id: 'qc', name: '质控', icon: '✅' },
  { id: 'customer', name: '客服', icon: '💬' },
  { id: 'finance', name: '财务', icon: '💰' }
]

export const mockAgents: Agent[] = [
  {
    id: '1',
    name: '路由规划Agent',
    description: '基于实时路况和车辆信息，智能规划最优配送路线',
    department: 'operation',
    tags: ['路由', '规划', '实时'],
    successRate: 98.5,
    avgTime: 2.3,
    dailyCalls: 120,
    usageCount: 1580,
    creatorId: '张工',
    createdAt: '2024-01-15',
    status: 'online',
    isFavorite: true,
    rating: 4.8
  },
  {
    id: '2',
    name: '车线管理Agent',
    description: '管理车辆调度、车线分配和运力优化',
    department: 'operation',
    tags: ['车线', '调度', '运力'],
    successRate: 96.2,
    avgTime: 1.8,
    dailyCalls: 95,
    usageCount: 1240,
    creatorId: '李经理',
    createdAt: '2024-02-20',
    status: 'online',
    rating: 4.6
  },
  {
    id: '3',
    name: '时效洞察Agent',
    description: '分析配送时效数据，识别延迟原因',
    department: 'qc',
    tags: ['时效', '分析', '洞察'],
    successRate: 97.8,
    avgTime: 3.1,
    dailyCalls: 78,
    usageCount: 980,
    creatorId: '王分析师',
    createdAt: '2024-03-10',
    status: 'online',
    isFavorite: true,
    rating: 4.9
  },
  {
    id: '4',
    name: '智能客服Agent',
    description: '自动响应客户咨询，处理常见问题',
    department: 'customer',
    tags: ['客服', '问答', '自动'],
    successRate: 95.6,
    avgTime: 0.8,
    dailyCalls: 260,
    usageCount: 3200,
    creatorId: '赵客服',
    createdAt: '2024-01-25',
    status: 'online',
    rating: 4.5
  },
  {
    id: '5',
    name: '工单质检Agent',
    description: '自动质检工单内容，识别违规和问题',
    department: 'qc',
    tags: ['质检', '工单', '合规'],
    successRate: 94.3,
    avgTime: 1.2,
    dailyCalls: 150,
    usageCount: 1850,
    creatorId: '孙主管',
    createdAt: '2024-02-05',
    status: 'online',
    rating: 4.7
  },
  {
    id: '6',
    name: '报表审核Agent',
    description: '自动审核财务报表，识别异常数据',
    department: 'finance',
    tags: ['报表', '审核', '财务'],
    successRate: 99.1,
    avgTime: 2.5,
    dailyCalls: 45,
    usageCount: 680,
    creatorId: '周会计',
    createdAt: '2024-03-01',
    status: 'online',
    rating: 4.8
  },
  {
    id: '7',
    name: '异常预警Agent',
    description: '实时监控配送异常，提前预警风险',
    department: 'operation',
    tags: ['预警', '监控', '风险'],
    successRate: 92.8,
    avgTime: 0.5,
    dailyCalls: 35,
    usageCount: 420,
    creatorId: '陈工程师',
    createdAt: '2024-04-15',
    status: 'pending'
  },
  {
    id: '8',
    name: '成本分析Agent',
    description: '分析物流成本构成，优化成本结构',
    department: 'finance',
    tags: ['成本', '分析', '优化'],
    successRate: 96.8,
    avgTime: 3.5,
    dailyCalls: 52,
    usageCount: 560,
    creatorId: '吴财务',
    createdAt: '2024-04-01',
    status: 'online',
    rating: 4.6
  }
]
