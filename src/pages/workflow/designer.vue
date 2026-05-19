<template>
  <Layout>
    <view class="page">
      <view class="designer-header">
        <view class="header-left">
          <view class="btn back-btn" @click="goBack">
            <text>← 返回</text>
          </view>
          <input class="workflow-name" v-model="workflowName" placeholder="未命名流程" />
          <view class="status-badge" :class="workflowStatus">
            <text>{{ getStatusText(workflowStatus) }}</text>
          </view>
        </view>
        <view class="header-actions">
          <view class="btn secondary" @click="saveDraft">
            <text>保存草稿</text>
          </view>
          <view class="btn primary" @click="publishWorkflow">
            <text>发布</text>
          </view>
        </view>
      </view>

      <view class="designer-body">
        <!-- Left: Node Palette -->
        <view class="node-palette">
          <view class="palette-title">节点类型</view>
          <view class="palette-section">
            <text class="section-label">基础节点</text>
            <view
              class="palette-item"
              v-for="item in baseNodes"
              :key="item.type"
              draggable="true"
              @touchstart="startDrag(item)"
            >
              <text class="node-icon">{{ item.icon }}</text>
              <text class="node-label">{{ item.label }}</text>
            </view>
          </view>
          <view class="palette-section">
            <text class="section-label">AI节点</text>
            <view
              class="palette-item"
              v-for="item in aiNodes"
              :key="item.type"
              @touchstart="startDrag(item)"
            >
              <text class="node-icon">{{ item.icon }}</text>
              <text class="node-label">{{ item.label }}</text>
            </view>
          </view>
          <view class="palette-section">
            <text class="section-label">逻辑节点</text>
            <view
              class="palette-item"
              v-for="item in logicNodes"
              :key="item.type"
              @touchstart="startDrag(item)"
            >
              <text class="node-icon">{{ item.icon }}</text>
              <text class="node-label">{{ item.label }}</text>
            </view>
          </view>
        </view>

        <!-- Center: Canvas -->
        <view class="canvas-area" @click="deselectAll">
          <view class="canvas-toolbar">
            <view class="toolbar-btn" @click="zoomIn"><text>+</text></view>
            <text class="zoom-level">{{ Math.round(zoom * 100) }}%</text>
            <view class="toolbar-btn" @click="zoomOut"><text>-</text></view>
            <view class="toolbar-btn" @click="resetZoom"><text>⟲</text></view>
          </view>

          <view
            class="canvas"
            :style="{ transform: `scale(${zoom})`, transformOrigin: '0 0' }"
            @touchmove.prevent="onCanvasTouchMove"
            @touchend="onCanvasTouchEnd"
          >
            <!-- SVG lines for edges -->
            <svg class="edges-layer" :width="canvasWidth" :height="canvasHeight">
              <defs>
                <marker id="arrowhead" markerWidth="10" markerHeight="7" refX="10" refY="3.5" orient="auto">
                  <polygon points="0 0, 10 3.5, 0 7" fill="#94a3b8" />
                </marker>
              </defs>
              <path
                v-for="edge in edges"
                :key="edge.id"
                :d="getEdgePath(edge)"
                fill="none"
                stroke="#94a3b8"
                stroke-width="2"
                marker-end="url(#arrowhead)"
              />
              <!-- Drawing new edge -->
              <path
                v-if="drawingEdge"
                :d="drawingEdgePath"
                fill="none"
                stroke="#4f46e5"
                stroke-width="2"
                stroke-dasharray="5,5"
              />
            </svg>

            <!-- Nodes -->
            <view
              class="canvas-node"
              v-for="node in nodes"
              :key="node.id"
              :class="{ selected: selectedNode?.id === node.id }"
              :style="{ left: node.x + 'px', top: node.y + 'px' }"
              @click.stop="selectNode(node)"
              @touchstart.stop="startNodeDrag($event, node)"
            >
              <view class="node-header" :class="node.type">
                <text class="node-type-icon">{{ getNodeTypeIcon(node.type) }}</text>
                <text class="node-title">{{ node.name }}</text>
              </view>
              <view class="node-body">
                <text class="node-desc">{{ getNodeDesc(node) }}</text>
              </view>
              <!-- Connection handles -->
              <view class="handle input-handle" :data-node-id="node.id"></view>
              <view class="handle output-handle" :data-node-id="node.id" @click.stop="startEdge($event, node)"></view>
              <!-- Delete button -->
              <view class="node-delete" @click.stop="deleteNode(node)" v-if="selectedNode?.id === node.id">
                <text>×</text>
              </view>
            </view>
          </view>
        </view>

        <!-- Right: Config Panel -->
        <view class="config-panel" v-if="selectedNode">
          <view class="panel-header">
            <text class="panel-title">节点配置</text>
            <text class="panel-close" @click="deselectAll">×</text>
          </view>
          <view class="panel-body">
            <view class="config-section">
              <text class="config-label">节点名称</text>
              <input class="config-input" v-model="selectedNode.name" placeholder="节点名称" />
            </view>

            <!-- Agent node config -->
            <view class="config-section" v-if="selectedNode.type === 'agent'">
              <text class="config-label">关联Agent</text>
              <select class="config-select" v-model="selectedNode.agentId">
                <option value="">请选择Agent</option>
                <option v-for="agent in availableAgents" :key="agent.id" :value="agent.id">
                  {{ agent.name }}
                </option>
              </select>
            </view>

            <!-- Skill node config -->
            <view class="config-section" v-if="selectedNode.type === 'skill'">
              <text class="config-label">关联技能</text>
              <select class="config-select" v-model="selectedNode.skillId">
                <option value="">请选择技能</option>
                <option v-for="skill in availableSkills" :key="skill.id" :value="skill.id">
                  {{ skill.name }}
                </option>
              </select>
            </view>

            <!-- Condition node config -->
            <view class="config-section" v-if="selectedNode.type === 'condition'">
              <text class="config-label">条件表达式</text>
              <textarea class="config-textarea" v-model="conditionExpr" placeholder="如: {{ input.status }} == 'error'" rows="3"></textarea>
              <text class="config-hint">使用 {{ "{{" }} }} 引用上游节点输出变量</text>
            </view>

            <!-- Loop node config -->
            <view class="config-section" v-if="selectedNode.type === 'loop'">
              <text class="config-label">循环类型</text>
              <select class="config-select" v-model="loopType">
                <option value="count">固定次数</option>
                <option value="list">遍历列表</option>
                <option value="while">条件循环</option>
              </select>
              <view class="config-section" v-if="loopType === 'count'">
                <text class="config-label">循环次数</text>
                <input class="config-input" type="number" v-model="loopCount" placeholder="3" />
              </view>
            </view>

            <!-- Approval node config -->
            <view class="config-section" v-if="selectedNode.type === 'approval'">
              <text class="config-label">审批人</text>
              <input class="config-input" v-model="approver" placeholder="审批人ID或姓名" />
              <text class="config-label" style="margin-top: 12px;">超时时间(分钟)</text>
              <input class="config-input" type="number" v-model="approvalTimeout" placeholder="60" />
            </view>

            <!-- Variable mapping -->
            <view class="config-section">
              <text class="config-label">变量映射</text>
              <textarea class="config-textarea" v-model="selectedNode.config" placeholder='{"input": "{{ upstream.output }}"}' rows="3"></textarea>
              <text class="config-hint">JSON格式，定义输入输出变量映射关系</text>
            </view>
          </view>
        </view>
      </view>
    </view>
  </Layout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import Layout from '@/components/Layout.vue'
import { workflowService } from '@/services/workflow'
import { agentService } from '@/services/agent'
import { skillService } from '@/services/skill'

const workflowId = ref('')
const workflowName = ref('')
const workflowStatus = ref('draft')
const nodes = ref<any[]>([])
const edges = ref<any[]>([])
const selectedNode = ref<any>(null)
const zoom = ref(1)
const canvasWidth = ref(2000)
const canvasHeight = ref(1500)
const availableAgents = ref<any[]>([])
const availableSkills = ref<any[]>([])

// Drag state
const draggingNode = ref<any>(null)
const dragOffset = ref({ x: 0, y: 0 })
const drawingEdge = ref<any>(null)

// Condition config
const conditionExpr = ref('')
const loopType = ref('count')
const loopCount = ref(3)
const approver = ref('')
const approvalTimeout = ref(60)

const nodeTemplates = {
  start: { icon: '▶', label: '开始' },
  end: { icon: '⏹', label: '结束' },
  agent: { icon: '🤖', label: 'Agent节点' },
  skill: { icon: '⚡', label: 'Skill节点' },
  condition: { icon: '🔀', label: '条件分支' },
  loop: { icon: '🔄', label: '循环' },
  approval: { icon: '✅', label: '人工审批' }
}

const baseNodes = [
  { type: 'start', icon: '▶', label: '开始' },
  { type: 'end', icon: '⏹', label: '结束' }
]

const aiNodes = [
  { type: 'agent', icon: '🤖', label: 'Agent节点' },
  { type: 'skill', icon: '⚡', label: 'Skill节点' }
]

const logicNodes = [
  { type: 'condition', icon: '🔀', label: '条件分支' },
  { type: 'loop', icon: '🔄', label: '循环' },
  { type: 'approval', icon: '✅', label: '人工审批' }
]

const getNodeTypeIcon = (type: string) => {
  return nodeTemplates[type as keyof typeof nodeTemplates]?.icon || '📦'
}

const getNodeDesc = (node: any) => {
  switch (node.type) {
    case 'start': return '流程入口'
    case 'end': return '流程出口'
    case 'agent': return node.agentId ? '已关联Agent' : '点击配置Agent'
    case 'skill': return node.skillId ? '已关联Skill' : '点击配置Skill'
    case 'condition': return '条件判断分支'
    case 'loop': return '循环执行'
    case 'approval': return '人工审批节点'
    default: return ''
  }
}

const getStatusText = (status: string) => {
  const map: Record<string, string> = { draft: '草稿', active: '运行中', inactive: '已停用' }
  return map[status] || status
}

const goBack = () => {
  uni.navigateBack()
}

const addNode = (template: any, x: number, y: number) => {
  const node = {
    id: 'node_' + Date.now(),
    type: template.type,
    name: template.label,
    x,
    y,
    agentId: '',
    skillId: '',
    config: ''
  }
  nodes.value.push(node)
  selectNode(node)
}

const selectNode = (node: any) => {
  selectedNode.value = node
  // Load config
  if (node.config) {
    try {
      const cfg = JSON.parse(node.config)
      if (cfg.condition) conditionExpr.value = cfg.condition
      if (cfg.loopType) loopType.value = cfg.loopType
      if (cfg.loopCount) loopCount.value = cfg.loopCount
      if (cfg.approver) approver.value = cfg.approver
      if (cfg.approvalTimeout) approvalTimeout.value = cfg.approvalTimeout
    } catch (e) { /* ignore */ }
  }
}

const deselectAll = () => {
  // Save config before deselecting
  if (selectedNode.value) {
    const cfg: any = {}
    if (conditionExpr.value) cfg.condition = conditionExpr.value
    if (loopType.value !== 'count') cfg.loopType = loopType.value
    if (loopCount.value !== 3) cfg.loopCount = loopCount.value
    if (approver.value) cfg.approver = approver.value
    if (approvalTimeout.value !== 60) cfg.approvalTimeout = approvalTimeout.value
    selectedNode.value.config = Object.keys(cfg).length > 0 ? JSON.stringify(cfg) : ''
  }
  selectedNode.value = null
}

const deleteNode = (node: any) => {
  nodes.value = nodes.value.filter(n => n.id !== node.id)
  edges.value = edges.value.filter(e => e.source !== node.id && e.target !== node.id)
  selectedNode.value = null
}

const startDrag = (item: any) => {
  // For desktop: use drag events; for mobile: just add at center
  const cx = Math.floor(canvasWidth.value / 2) - 80
  const cy = Math.floor(canvasHeight.value / 2) - 40
  addNode(item, cx + Math.random() * 100 - 50, cy + Math.random() * 100 - 50)
}

const startNodeDrag = (event: any, node: any) => {
  const touch = event.touches?.[0]
  if (!touch) return
  draggingNode.value = node
  dragOffset.value = { x: touch.clientX - node.x, y: touch.clientY - node.y }
}

const onCanvasTouchMove = (event: any) => {
  if (!draggingNode.value || !event.touches?.[0]) return
  const touch = event.touches[0]
  draggingNode.value.x = touch.clientX - dragOffset.value.x
  draggingNode.value.y = touch.clientY - dragOffset.value.y
}

const onCanvasTouchEnd = () => {
  draggingNode.value = null
  drawingEdge.value = null
}

const startEdge = (event: any, sourceNode: any) => {
  drawingEdge.value = {
    source: sourceNode.id,
    fromX: sourceNode.x + 160,
    fromY: sourceNode.y + 30
  }
}

const getEdgePath = (edge: any) => {
  const source = nodes.value.find(n => n.id === edge.source)
  const target = nodes.value.find(n => n.id === edge.target)
  if (!source || !target) return ''
  const x1 = source.x + 160
  const y1 = source.y + 30
  const x2 = target.x
  const y2 = target.y + 30
  const cp = Math.abs(x2 - x1) * 0.5
  return `M ${x1} ${y1} C ${x1 + cp} ${y1}, ${x2 - cp} ${y2}, ${x2} ${y2}`
}

const drawingEdgePath = computed(() => {
  if (!drawingEdge.value) return ''
  return `M ${drawingEdge.value.fromX} ${drawingEdge.value.fromY} L ${drawingEdge.value.fromX + 100} ${drawingEdge.value.fromY}`
})

const zoomIn = () => { zoom.value = Math.min(zoom.value + 0.1, 2) }
const zoomOut = () => { zoom.value = Math.max(zoom.value - 0.1, 0.3) }
const resetZoom = () => { zoom.value = 1 }

const saveDraft = async () => {
  if (!workflowName.value.trim()) {
    uni.showToast({ title: '请输入流程名称', icon: 'none' })
    return
  }
  deselectAll()
  try {
    const data = {
      name: workflowName.value,
      description: '',
      triggerType: 'api',
      nodes: nodes.value.map(n => ({
        id: n.id, type: n.type, name: n.name,
        config: n.config, x: n.x, y: n.y,
        agentId: n.agentId, skillId: n.skillId
      })),
      edges: edges.value
    }
    if (workflowId.value) {
      await workflowService.update(workflowId.value, data)
    } else {
      const result = await workflowService.create(data as any)
      workflowId.value = result.id
    }
    uni.showToast({ title: '保存成功', icon: 'success' })
  } catch (error) {
    console.error('Failed to save workflow:', error)
    uni.showToast({ title: '保存失败', icon: 'error' })
  }
}

const publishWorkflow = async () => {
  if (!workflowId.value) {
    await saveDraft()
    if (!workflowId.value) return
  }
  try {
    await workflowService.activate(workflowId.value)
    workflowStatus.value = 'active'
    uni.showToast({ title: '发布成功', icon: 'success' })
  } catch (error) {
    console.error('Failed to publish:', error)
    uni.showToast({ title: '发布失败', icon: 'error' })
  }
}

const loadWorkflow = async () => {
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1] as any
  const id = currentPage.options?.id
  if (id) {
    workflowId.value = id
    try {
      const wf = await workflowService.get(id)
      workflowName.value = wf.name
      workflowStatus.value = wf.status
      if (wf.nodes) {
        nodes.value = wf.nodes.map((n: any) => ({
          ...n, x: n.x || 100, y: n.y || 100
        }))
      }
      if (wf.edges) {
        edges.value = wf.edges
      }
    } catch (error) {
      console.error('Failed to load workflow:', error)
    }
  }
}

const loadAgents = async () => {
  try {
    const res = await agentService.list({ page: 1, size: 100 })
    availableAgents.value = res.list || []
  } catch (e) { /* ignore */ }
}

const loadSkills = async () => {
  try {
    const res = await skillService.list({ page: 1, size: 100 })
    availableSkills.value = res.list || []
  } catch (e) { /* ignore */ }
}

onMounted(() => {
  loadWorkflow()
  loadAgents()
  loadSkills()
})
</script>

<style lang="scss">
.page {
  min-height: 100vh;
  background: #f0f2f5;
  display: flex;
  flex-direction: column;
}

.designer-header {
  background: #fff;
  padding: 12px 24px;
  border-bottom: 1px solid #e8e8e8;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.back-btn {
  background: #f3f4f6;
  padding: 8px 16px;
  border-radius: 8px;
  cursor: pointer;

  text { font-size: 14px; color: #6b7280; }
}

.workflow-name {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
  border: none;
  background: transparent;
  padding: 4px 8px;
  border-radius: 4px;

  &:focus {
    background: #f3f4f6;
    outline: none;
  }
}

.status-badge {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;

  &.draft { background: #f3f4f6; color: #6b7280; }
  &.active { background: #d1fae5; color: #065f46; }
  &.inactive { background: #fee2e2; color: #dc2626; }
}

.header-actions {
  display: flex;
  gap: 12px;
}

.btn {
  padding: 8px 16px;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;

  &.primary {
    background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%);
    color: #fff;
  }

  &.secondary {
    background: #f3f4f6;
    color: #6b7280;
    border: 1px solid #e5e7eb;
  }
}

.designer-body {
  display: flex;
  flex: 1;
  overflow: hidden;
}

.node-palette {
  width: 200px;
  background: #fff;
  border-right: 1px solid #e8e8e8;
  padding: 16px;
  overflow-y: auto;
}

.palette-title {
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 16px;
}

.palette-section {
  margin-bottom: 16px;
}

.section-label {
  font-size: 12px;
  color: #9ca3af;
  margin-bottom: 8px;
  display: block;
}

.palette-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  background: #f9fafb;
  border-radius: 8px;
  margin-bottom: 6px;
  cursor: grab;
  transition: all 0.2s;

  &:hover {
    background: #eef2ff;
    border-color: #4f46e5;
  }

  .node-icon { font-size: 18px; }
  .node-label { font-size: 13px; color: #374151; }
}

.canvas-area {
  flex: 1;
  position: relative;
  overflow: auto;
  background: #f8fafc;
  background-image: radial-gradient(circle, #e2e8f0 1px, transparent 1px);
  background-size: 20px 20px;
}

.canvas-toolbar {
  position: absolute;
  top: 12px;
  right: 12px;
  display: flex;
  align-items: center;
  gap: 8px;
  background: #fff;
  padding: 8px 12px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
  z-index: 10;
}

.toolbar-btn {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
  cursor: pointer;
  background: #f3f4f6;

  text { font-size: 16px; color: #6b7280; }

  &:hover { background: #e5e7eb; }
}

.zoom-level {
  font-size: 12px;
  color: #6b7280;
  min-width: 40px;
  text-align: center;
}

.canvas {
  position: relative;
  min-width: 100%;
  min-height: 100%;
}

.edges-layer {
  position: absolute;
  top: 0;
  left: 0;
  pointer-events: none;
}

.canvas-node {
  position: absolute;
  width: 160px;
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
  border: 2px solid transparent;
  cursor: move;
  user-select: none;

  &.selected {
    border-color: #4f46e5;
    box-shadow: 0 4px 16px rgba(79,70,229,0.2);
  }
}

.node-header {
  padding: 10px 12px;
  border-radius: 8px 8px 0 0;
  display: flex;
  align-items: center;
  gap: 8px;

  &.start, &.end { background: #f3f4f6; }
  &.agent { background: linear-gradient(135deg, #eef2ff, #ddd6fe); }
  &.skill { background: linear-gradient(135deg, #fef3c7, #fde68a); }
  &.condition { background: linear-gradient(135deg, #d1fae5, #a7f3d0); }
  &.loop { background: linear-gradient(135deg, #dbeafe, #bfdbfe); }
  &.approval { background: linear-gradient(135deg, #fce7f3, #fbcfe8); }
}

.node-type-icon { font-size: 16px; }
.node-title { font-size: 13px; font-weight: 600; color: #1f2937; }

.node-body {
  padding: 8px 12px;
}

.node-desc {
  font-size: 11px;
  color: #9ca3af;
}

.handle {
  position: absolute;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: #fff;
  border: 2px solid #94a3b8;
  cursor: crosshair;
}

.input-handle {
  left: -6px;
  top: 24px;
}

.output-handle {
  right: -6px;
  top: 24px;
  background: #4f46e5;
  border-color: #4f46e5;
}

.node-delete {
  position: absolute;
  top: -8px;
  right: -8px;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: #ef4444;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  cursor: pointer;
}

.config-panel {
  width: 300px;
  background: #fff;
  border-left: 1px solid #e8e8e8;
  display: flex;
  flex-direction: column;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #e8e8e8;
}

.panel-title {
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
}

.panel-close {
  font-size: 20px;
  color: #9ca3af;
  cursor: pointer;
}

.panel-body {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}

.config-section {
  margin-bottom: 16px;
}

.config-label {
  font-size: 13px;
  font-weight: 500;
  color: #374151;
  margin-bottom: 6px;
  display: block;
}

.config-input, .config-select, .config-textarea {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  font-size: 13px;
  background: #fff;

  &:focus {
    outline: none;
    border-color: #4f46e5;
  }
}

.config-textarea {
  resize: vertical;
  min-height: 60px;
}

.config-hint {
  font-size: 11px;
  color: #9ca3af;
  margin-top: 4px;
  display: block;
}
</style>
