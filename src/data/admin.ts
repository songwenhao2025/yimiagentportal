export interface User {
  id: string
  name: string
  department: string
  role: 'admin' | 'developer' | 'user' | 'operator'
  email: string
  phone: string
  status: 'active' | 'inactive'
  createdAt: string
}

export interface AuditLog {
  id: string
  userId: string
  userName: string
  action: string
  resource: string
  resourceId: string
  timestamp: string
  ip: string
  result: 'success' | 'failed'
}

export interface CostRecord {
  id: string
  department: string
  agentId: string
  agentName: string
  tokenUsage: number
  apiCalls: number
  cost: number
  date: string
}

export const mockUsers: User[] = [
  {
    id: 'u1',
    name: '张三',
    department: 'IT部',
    role: 'admin',
    email: 'zhangsan@company.com',
    phone: '13800138001',
    status: 'active',
    createdAt: '2024-01-01'
  },
  {
    id: 'u2',
    name: '李四',
    department: '运营部',
    role: 'developer',
    email: 'lisi@company.com',
    phone: '13800138002',
    status: 'active',
    createdAt: '2024-01-10'
  },
  {
    id: 'u3',
    name: '王五',
    department: '质控部',
    role: 'developer',
    email: 'wangwu@company.com',
    phone: '13800138003',
    status: 'active',
    createdAt: '2024-02-01'
  },
  {
    id: 'u4',
    name: '赵六',
    department: '客服部',
    role: 'user',
    email: 'zhaoliu@company.com',
    phone: '13800138004',
    status: 'active',
    createdAt: '2024-02-15'
  },
  {
    id: 'u5',
    name: '钱七',
    department: '财务部',
    role: 'user',
    email: 'qianqi@company.com',
    phone: '13800138005',
    status: 'inactive',
    createdAt: '2024-03-01'
  }
]

export const mockAuditLogs: AuditLog[] = [
  {
    id: 'log1',
    userId: 'u1',
    userName: '张三',
    action: '创建Agent',
    resource: 'Agent',
    resourceId: '1',
    timestamp: '2024-04-15 10:30:00',
    ip: '192.168.1.100',
    result: 'success'
  },
  {
    id: 'log2',
    userId: 'u2',
    userName: '李四',
    action: '发布Skill',
    resource: 'Skill',
    resourceId: 's1',
    timestamp: '2024-04-15 11:15:00',
    ip: '192.168.1.101',
    result: 'success'
  },
  {
    id: 'log3',
    userId: 'u3',
    userName: '王五',
    action: '审核Agent',
    resource: 'Agent',
    resourceId: '7',
    timestamp: '2024-04-15 14:20:00',
    ip: '192.168.1.102',
    result: 'failed'
  },
  {
    id: 'log4',
    userId: 'u1',
    userName: '张三',
    action: '创建Workflow',
    resource: 'Workflow',
    resourceId: 'wf1',
    timestamp: '2024-04-15 15:45:00',
    ip: '192.168.1.100',
    result: 'success'
  },
  {
    id: 'log5',
    userId: 'u4',
    userName: '赵六',
    action: '使用Agent',
    resource: 'Agent',
    resourceId: '4',
    timestamp: '2024-04-15 16:30:00',
    ip: '192.168.1.103',
    result: 'success'
  }
]

export const mockCostRecords: CostRecord[] = [
  {
    id: 'c1',
    department: '运营部',
    agentId: '1',
    agentName: '路由规划Agent',
    tokenUsage: 125000,
    apiCalls: 2300,
    cost: 125.00,
    date: '2024-04-01'
  },
  {
    id: 'c2',
    department: '运营部',
    agentId: '2',
    agentName: '车线管理Agent',
    tokenUsage: 89000,
    apiCalls: 1800,
    cost: 89.00,
    date: '2024-04-01'
  },
  {
    id: 'c3',
    department: '质控部',
    agentId: '3',
    agentName: '时效洞察Agent',
    tokenUsage: 234000,
    apiCalls: 4500,
    cost: 234.00,
    date: '2024-04-01'
  },
  {
    id: 'c4',
    department: '客服部',
    agentId: '4',
    agentName: '智能客服Agent',
    tokenUsage: 567000,
    apiCalls: 12000,
    cost: 567.00,
    date: '2024-04-01'
  },
  {
    id: 'c5',
    department: '财务部',
    agentId: '6',
    agentName: '报表审核Agent',
    tokenUsage: 67000,
    apiCalls: 800,
    cost: 67.00,
    date: '2024-04-01'
  }
]
