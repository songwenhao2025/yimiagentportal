export interface WorkflowNode {
  id: string
  type: 'agent' | 'skill' | 'condition' | 'loop' | 'approval' | 'start' | 'end'
  name: string
  config?: Record<string, unknown>
  position: { x: number; y: number }
}

export interface WorkflowEdge {
  id: string
  source: string
  target: string
  sourceHandle?: string
  targetHandle?: string
}

export interface Workflow {
  id: string
  name: string
  description: string
  nodes: WorkflowNode[]
  edges: WorkflowEdge[]
  creator: string
  createdAt: string
  status: 'draft' | 'active' | 'inactive'
  triggerType: 'api' | 'cron' | 'event'
  cronExpression?: string
  executionCount: number
  successRate: number
}

export const mockWorkflows: Workflow[] = [
  {
    id: 'wf1',
    name: '异常签收处理流程',
    description: '扫描异常签收→时效洞察Agent定位→路由Agent计算补救车线→智能客服Agent自动外呼→生成Case记录',
    nodes: [
      { id: 'n1', type: 'start', name: '开始', position: { x: 100, y: 200 } },
      { id: 'n2', type: 'skill', name: '扫描异常签收', position: { x: 250, y: 200 } },
      { id: 'n3', type: 'agent', name: '时效洞察Agent', position: { x: 420, y: 200 } },
      { id: 'n4', type: 'agent', name: '路由Agent', position: { x: 590, y: 200 } },
      { id: 'n5', type: 'agent', name: '智能客服Agent', position: { x: 760, y: 200 } },
      { id: 'n6', type: 'skill', name: '生成Case记录', position: { x: 930, y: 200 } },
      { id: 'n7', type: 'end', name: '结束', position: { x: 1080, y: 200 } }
    ],
    edges: [
      { id: 'e1', source: 'n1', target: 'n2' },
      { id: 'e2', source: 'n2', target: 'n3' },
      { id: 'e3', source: 'n3', target: 'n4' },
      { id: 'e4', source: 'n4', target: 'n5' },
      { id: 'e5', source: 'n5', target: 'n6' },
      { id: 'e6', source: 'n6', target: 'n7' }
    ],
    creator: '陈主管',
    createdAt: '2024-04-01',
    status: 'active',
    triggerType: 'event',
    executionCount: 1280,
    successRate: 97.2
  },
  {
    id: 'wf2',
    name: '末端网点异常预警',
    description: '时效洞察Agent检测延迟→路由Agent推荐备用车线→自动建单派发整改任务',
    nodes: [
      { id: 'n1', type: 'start', name: '开始', position: { x: 100, y: 200 } },
      { id: 'n2', type: 'agent', name: '时效洞察Agent', position: { x: 280, y: 200 } },
      { id: 'n3', type: 'condition', name: '是否延迟?', position: { x: 460, y: 200 } },
      { id: 'n4', type: 'agent', name: '路由Agent', position: { x: 640, y: 150 } },
      { id: 'n5', type: 'skill', name: '创建整改工单', position: { x: 820, y: 150 } },
      { id: 'n6', type: 'end', name: '结束(正常)', position: { x: 640, y: 300 } },
      { id: 'n7', type: 'end', name: '结束(已处理)', position: { x: 980, y: 150 } }
    ],
    edges: [
      { id: 'e1', source: 'n1', target: 'n2' },
      { id: 'e2', source: 'n2', target: 'n3' },
      { id: 'e3', source: 'n3', target: 'n4', sourceHandle: 'yes' },
      { id: 'e4', source: 'n3', target: 'n6', sourceHandle: 'no' },
      { id: 'e5', source: 'n4', target: 'n5' },
      { id: 'e6', source: 'n5', target: 'n7' }
    ],
    creator: '李经理',
    createdAt: '2024-04-10',
    status: 'active',
    triggerType: 'cron',
    cronExpression: '0 */30 * * * *',
    executionCount: 560,
    successRate: 98.5
  },
  {
    id: 'wf3',
    name: '大客户专属查件流程',
    description: '结合工单Skill和时效Skill，一键给出包含路由与预计到达的完整答复',
    nodes: [
      { id: 'n1', type: 'start', name: '开始', position: { x: 100, y: 200 } },
      { id: 'n2', type: 'skill', name: '查询工单', position: { x: 260, y: 200 } },
      { id: 'n3', type: 'skill', name: '查询路由', position: { x: 420, y: 150 } },
      { id: 'n4', type: 'skill', name: '时效计算', position: { x: 420, y: 280 } },
      { id: 'n5', type: 'agent', name: '智能客服Agent', position: { x: 580, y: 200 } },
      { id: 'n6', type: 'end', name: '结束', position: { x: 740, y: 200 } }
    ],
    edges: [
      { id: 'e1', source: 'n1', target: 'n2' },
      { id: 'e2', source: 'n2', target: 'n3' },
      { id: 'e3', source: 'n2', target: 'n4' },
      { id: 'e4', source: 'n3', target: 'n5' },
      { id: 'e5', source: 'n4', target: 'n5' },
      { id: 'e6', source: 'n5', target: 'n6' }
    ],
    creator: '王主管',
    createdAt: '2024-04-15',
    status: 'draft',
    triggerType: 'api',
    executionCount: 0,
    successRate: 0
  }
]
