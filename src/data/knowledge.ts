export interface KnowledgeDocument {
  id: string
  title: string
  type: 'pdf' | 'word' | 'excel' | 'markdown' | 'url'
  size: number
  uploadedBy: string
  uploadedAt: string
  status: 'uploading' | 'processing' | 'ready' | 'failed'
  vectorStatus: 'pending' | 'indexing' | 'completed'
  chunkCount: number
  hitCount: number
  category: string
}

export const mockDocuments: KnowledgeDocument[] = [
  {
    id: 'k1',
    title: '物流配送SOP手册',
    type: 'pdf',
    size: 2456789,
    uploadedBy: '张主管',
    uploadedAt: '2024-01-10',
    status: 'ready',
    vectorStatus: 'completed',
    chunkCount: 156,
    hitCount: 3420,
    category: '操作手册'
  },
  {
    id: 'k2',
    title: '常见问题FAQ',
    type: 'markdown',
    size: 45678,
    uploadedBy: '赵客服',
    uploadedAt: '2024-02-15',
    status: 'ready',
    vectorStatus: 'completed',
    chunkCount: 89,
    hitCount: 8900,
    category: 'FAQ'
  },
  {
    id: 'k3',
    title: '财务报表规范',
    type: 'word',
    size: 1234567,
    uploadedBy: '周会计',
    uploadedAt: '2024-03-01',
    status: 'ready',
    vectorStatus: 'completed',
    chunkCount: 45,
    hitCount: 1200,
    category: '财务文档'
  },
  {
    id: 'k4',
    title: 'TMS系统API文档',
    type: 'markdown',
    size: 89012,
    uploadedBy: '李工程师',
    uploadedAt: '2024-03-20',
    status: 'ready',
    vectorStatus: 'completed',
    chunkCount: 234,
    hitCount: 5600,
    category: '技术文档'
  },
  {
    id: 'k5',
    title: 'WMS系统数据表结构',
    type: 'excel',
    size: 456789,
    uploadedBy: '王工',
    uploadedAt: '2024-04-01',
    status: 'processing',
    vectorStatus: 'indexing',
    chunkCount: 0,
    hitCount: 0,
    category: '技术文档'
  },
  {
    id: 'k6',
    title: '配送时效标准',
    type: 'pdf',
    size: 789012,
    uploadedBy: '陈主管',
    uploadedAt: '2024-04-10',
    status: 'ready',
    vectorStatus: 'completed',
    chunkCount: 67,
    hitCount: 2300,
    category: '标准规范'
  }
]

export const documentCategories = [
  { id: 'all', name: '全部' },
  { id: 'operation', name: '操作手册' },
  { id: 'faq', name: 'FAQ' },
  { id: 'finance', name: '财务文档' },
  { id: 'tech', name: '技术文档' },
  { id: 'standard', name: '标准规范' }
]
