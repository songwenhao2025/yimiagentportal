export interface Skill {
  id: string
  name: string
  description: string
  type: 'api' | 'function'
  category: string
  parameters: { name: string; type: string; required: boolean; description: string }[]
  returnType: string
  version: string
  creator: string
  createdAt: string
  status: 'published' | 'draft' | 'review'
  usageCount: number
  timeout: number
  apiEndpoint?: string
}

export const mockSkills: Skill[] = [
  {
    id: 's1',
    name: '查询路由',
    description: '根据运单号查询配送路由信息',
    type: 'api',
    category: '物流查询',
    parameters: [
      { name: 'trackingNo', type: 'string', required: true, description: '运单号' },
      { name: 'carrier', type: 'string', required: false, description: '承运商' }
    ],
    returnType: 'RouteInfo',
    version: '1.0.0',
    creator: '张工',
    createdAt: '2024-01-10',
    status: 'published',
    usageCount: 12580,
    timeout: 30
  },
  {
    id: 's2',
    name: '时效计算',
    description: '计算预计送达时间',
    type: 'api',
    category: '物流计算',
    parameters: [
      { name: 'origin', type: 'string', required: true, description: '起点城市' },
      { name: 'destination', type: 'string', required: true, description: '终点城市' },
      { name: 'serviceType', type: 'string', required: false, description: '服务类型' }
    ],
    returnType: 'DeliveryTime',
    version: '2.1.0',
    creator: '李工程师',
    createdAt: '2024-02-15',
    status: 'published',
    usageCount: 8920,
    timeout: 20
  },
  {
    id: 's3',
    name: '创建工单',
    description: '创建客服工单',
    type: 'api',
    category: '工单管理',
    parameters: [
      { name: 'title', type: 'string', required: true, description: '工单标题' },
      { name: 'content', type: 'string', required: true, description: '工单内容' },
      { name: 'priority', type: 'string', required: false, description: '优先级' }
    ],
    returnType: 'Ticket',
    version: '1.0.2',
    creator: '赵客服',
    createdAt: '2024-01-20',
    status: 'published',
    usageCount: 15600,
    timeout: 15
  },
  {
    id: 's4',
    name: '查询库存',
    description: '查询仓库库存信息',
    type: 'api',
    category: '仓储管理',
    parameters: [
      { name: 'sku', type: 'string', required: true, description: '商品SKU' },
      { name: 'warehouse', type: 'string', required: false, description: '仓库编码' }
    ],
    returnType: 'Inventory',
    version: '1.1.0',
    creator: '王仓管',
    createdAt: '2024-03-01',
    status: 'published',
    usageCount: 6750,
    timeout: 25
  },
  {
    id: 's5',
    name: '价格计算',
    description: '计算物流费用',
    type: 'function',
    category: '财务计算',
    parameters: [
      { name: 'weight', type: 'number', required: true, description: '重量(kg)' },
      { name: 'distance', type: 'number', required: true, description: '距离(km)' },
      { name: 'serviceType', type: 'string', required: true, description: '服务类型' }
    ],
    returnType: 'Price',
    version: '1.0.0',
    creator: '周会计',
    createdAt: '2024-03-20',
    status: 'review',
    usageCount: 0,
    timeout: 10
  },
  {
    id: 's6',
    name: '发送短信',
    description: '发送通知短信',
    type: 'api',
    category: '通知服务',
    parameters: [
      { name: 'phone', type: 'string', required: true, description: '手机号' },
      { name: 'content', type: 'string', required: true, description: '短信内容' }
    ],
    returnType: 'boolean',
    version: '1.0.1',
    creator: '郑运维',
    createdAt: '2024-02-28',
    status: 'published',
    usageCount: 23400,
    timeout: 10
  }
]
