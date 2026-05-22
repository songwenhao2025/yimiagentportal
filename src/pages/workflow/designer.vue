<template>
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
        <text class="palette-title">节点类型</text>
        <view class="palette-section">
          <text class="section-label">基础节点</text>
          <view
            class="palette-item"
            v-for="item in baseNodes"
            :key="item.type"
            @click="addNodeFromPalette(item)"
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
            @click="addNodeFromPalette(item)"
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
            @click="addNodeFromPalette(item)"
          >
            <text class="node-icon">{{ item.icon }}</text>
            <text class="node-label">{{ item.label }}</text>
          </view>
        </view>
      </view>

      <!-- Center: Canvas -->
      <view class="canvas-area" ref="canvasArea" @click="deselectAll">
        <view class="canvas-toolbar">
          <view class="toolbar-btn" @click.stop="zoomIn"><text>+</text></view>
          <text class="zoom-level">{{ Math.round(zoom * 100) }}%</text>
          <view class="toolbar-btn" @click.stop="zoomOut"><text>-</text></view>
          <view class="toolbar-btn" @click.stop="resetZoom"><text>⟲</text></view>
          <view class="toolbar-divider"></view>
          <view class="toolbar-btn" @click.stop="showExecutions"><text>⏱</text></view>
        </view>

        <view
          class="canvas"
          :style="{ transform: `scale(${zoom}) translate(${panX}px, ${panY}px)`, transformOrigin: '0 0', width: canvasWidth + 'px', height: canvasHeight + 'px' }"
          @mousedown="onCanvasMouseDown"
          @mousemove="onCanvasMouseMove"
          @mouseup="onCanvasMouseUp"
        >
          <!-- SVG lines for edges -->
          <svg class="edges-layer" :width="canvasWidth" :height="canvasHeight">
            <defs>
              <marker id="arrowhead" markerWidth="10" markerHeight="7" refX="10" refY="3.5" orient="auto">
                <polygon points="0 0, 10 3.5, 0 7" fill="#94a3b8" />
              </marker>
              <marker id="arrowhead-active" markerWidth="10" markerHeight="7" refX="10" refY="3.5" orient="auto">
                <polygon points="0 0, 10 3.5, 0 7" fill="#4f46e5" />
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
              @click.stop="deleteEdge(edge)"
              style="cursor: pointer;"
            />
            <!-- Drawing new edge -->
            <path
              v-if="drawingEdge"
              :d="getDrawingEdgePath()"
              fill="none"
              stroke="#4f46e5"
              stroke-width="2"
              stroke-dasharray="5,5"
              marker-end="url(#arrowhead-active)"
            />
          </svg>

          <!-- Nodes -->
          <view
            class="canvas-node"
            v-for="node in nodes"
            :key="node.id"
            :class="{ selected: selectedNode?.id === node.id, [node.type]: true }"
            :style="{ left: node.x + 'px', top: node.y + 'px', width: nodeWidth + 'px' }"
            @click.stop="selectNode(node)"
            @mousedown.stop="startNodeDrag($event, node)"
          >
            <view class="node-header" :class="node.type">
              <text class="node-type-icon">{{ getNodeTypeIcon(node.type) }}</text>
              <text class="node-title">{{ node.name }}</text>
            </view>
            <view class="node-body">
              <text class="node-desc">{{ getNodeDesc(node) }}</text>
            </view>
            <!-- Connection handles -->
            <view class="handle input-handle" :data-node-id="node.id" @click.stop="completeEdge(node)"></view>
            <view class="handle output-handle" :data-node-id="node.id" @click.stop="startEdge(node)"></view>
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
            <picker class="config-picker" mode="selector" :range="availableAgents" range-key="name" :value="selectedAgentIndex" @change="onAgentChange">
              <view class="picker-value">{{ selectedAgentIndex >= 0 ? availableAgents[selectedAgentIndex].name : '请选择Agent' }}</view>
            </picker>
          </view>

          <!-- Skill node config -->
          <view class="config-section" v-if="selectedNode.type === 'skill'">
            <text class="config-label">关联技能</text>
            <picker class="config-picker" mode="selector" :range="availableSkills" range-key="name" :value="selectedSkillIndex" @change="onSkillChange">
              <view class="picker-value">{{ selectedSkillIndex >= 0 ? availableSkills[selectedSkillIndex].name : '请选择技能' }}</view>
            </picker>
          </view>

          <!-- Condition node config -->
          <view class="config-section" v-if="selectedNode.type === 'condition'">
            <text class="config-label">条件表达式</text>
            <textarea class="config-textarea" v-model="conditionExpr" placeholder="如: {{ input.status }} == 'error'" rows="3"></textarea>
            <text class="config-hint">使用 &#123;&#123; 变量名 &#125;&#125; 引用上游输出</text>
          </view>

          <!-- Loop node config -->
          <view class="config-section" v-if="selectedNode.type === 'loop'">
            <text class="config-label">循环类型</text>
            <picker class="config-picker" mode="selector" :range="loopTypeOptions" range-key="label" :value="loopTypeIndex" @change="onLoopTypeChange">
              <view class="picker-value">{{ loopTypeOptions[loopTypeIndex].label }}</view>
            </picker>
            <view class="config-section" v-if="loopType === 'count'">
              <text class="config-label">循环次数</text>
              <input class="config-input" type="number" v-model.number="loopCount" placeholder="3" />
            </view>
          </view>

          <!-- Approval node config -->
          <view class="config-section" v-if="selectedNode.type === 'approval'">
            <text class="config-label">审批人</text>
            <input class="config-input" v-model="approver" placeholder="审批人ID或姓名" />
            <text class="config-label" style="margin-top: 12px; display: block;">超时时间(分钟)</text>
            <input class="config-input" type="number" v-model.number="approvalTimeout" placeholder="60" />
          </view>

          <!-- Variable mapping -->
          <view class="config-section">
            <text class="config-label">变量映射 (JSON)</text>
            <textarea class="config-textarea" v-model="selectedNode.config" placeholder='{"input": "{{ upstream.output }}"}' rows="3"></textarea>
            <text class="config-hint">JSON格式，定义输入输出变量映射关系</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
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
const panX = ref(0)
const panY = ref(0)
const canvasWidth = 3000
const canvasHeight = 2000
const nodeWidth = 160
const availableAgents = ref<any[]>([])
const availableSkills = ref<any[]>([])

const selectedAgentIndex = ref(-1)
const selectedSkillIndex = ref(-1)
const selectedConditionIndex = ref(-1)
const conditionExpr = ref('')
const loopType = ref('count')
const loopTypeOptions = [{ label: '固定次数' }, { label: '遍历列表' }, { label: '条件循环' }]
const loopTypeIndex = ref(0)
const loopCount = ref(3)
const approver = ref('')
const approvalTimeout = ref(60)

const nodeTemplates: Record<string, { icon: string; label: string }> = {
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

// Mouse/touch interaction
const draggingNode = ref<any>(null)
const dragOffset = ref({ x: 0, y: 0 })
const drawingEdge = ref<any>(null)
const isPanning = ref(false)
const panStart = ref({ x: 0, y: 0 })

// Global mouseup handler for drag end
const handleGlobalMouseUp = () => {
  isPanning.value = false
  draggingNode.value = null
}

const handleGlobalMouseMove = (event: MouseEvent) => {
  if (draggingNode.value) {
    const canvasEl = document.querySelector('.canvas') as HTMLElement
    if (canvasEl) {
      const rect = canvasEl.getBoundingClientRect()
      const scaleX = rect.width / canvasEl.offsetWidth
      const scaleY = rect.height / canvasEl.offsetHeight
      draggingNode.value.x = (event.clientX - rect.left - dragOffset.value.x * scaleX) / zoom.value + panX.value / zoom.value
      draggingNode.value.y = (event.clientY - rect.top - dragOffset.value.y * scaleY) / zoom.value + panY.value / zoom.value
    }
  }
}

const getNodeTypeIcon = (type: string) => nodeTemplates[type]?.icon || '📦'

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

const goBack = () => { uni.navigateBack() }

const addNodeFromPalette = (item: any) => {
  const node = {
    id: 'node_' + Date.now(),
    type: item.type,
    name: item.label,
    x: 400 + Math.random() * 200,
    y: 200 + Math.random() * 200,
    agentId: '',
    skillId: '',
    config: ''
  }
  nodes.value.push(node)
  selectNode(node)
}

const selectNode = (node: any) => {
  selectedNode.value = node
  if (node.config) {
    try {
      const cfg = JSON.parse(node.config)
      if (cfg.condition) conditionExpr.value = cfg.condition
      if (cfg.loopType) {
        const idx = loopTypeOptions.findIndex((o: any) => o.label === cfg.loopType)
        if (idx >= 0) { loopTypeIndex.value = idx; loopType.value = cfg.loopType }
      }
      if (cfg.loopCount) loopCount.value = cfg.loopCount
      if (cfg.approver) approver.value = cfg.approver
      if (cfg.approvalTimeout) approvalTimeout.value = cfg.approvalTimeout
    } catch (e) { /* ignore */ }
  }
  // Set picker indices
  if (node.type === 'agent' && node.agentId) {
    selectedAgentIndex.value = availableAgents.value.findIndex((a: any) => a.id === node.agentId)
  }
  if (node.type === 'skill' && node.skillId) {
    selectedSkillIndex.value = availableSkills.value.findIndex((s: any) => s.id === node.skillId)
  }
}

const deselectAll = () => {
  if (selectedNode.value) {
    const cfg: any = {}
    if (conditionExpr.value) cfg.condition = conditionExpr.value
    if (loopType.value !== 'count') cfg.loopType = loopTypeOptions[loopTypeIndex.value]?.label
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

const deleteEdge = (edge: any) => {
  edges.value = edges.value.filter(e => e.id !== edge.id)
}

const startEdge = (sourceNode: any) => {
  drawingEdge.value = { source: sourceNode.id, mouseX: 0, mouseY: 0 }
}

const completeEdge = (targetNode: any) => {
  if (!drawingEdge.value || drawingEdge.value.source === targetNode.id) return
  // Check if edge already exists
  const exists = edges.value.some(e =>
    e.source === drawingEdge.value.source && e.target === targetNode.id
  )
  if (!exists) {
    edges.value.push({
      id: 'edge_' + Date.now(),
      source: drawingEdge.value.source,
      target: targetNode.id
    })
  }
  drawingEdge.value = null
}

const getEdgePath = (edge: any) => {
  const source = nodes.value.find(n => n.id === edge.source)
  const target = nodes.value.find(n => n.id === edge.target)
  if (!source || !target) return ''
  const x1 = source.x + nodeWidth
  const y1 = source.y + 30
  const x2 = target.x
  const y2 = target.y + 30
  const cp = Math.max(Math.abs(x2 - x1) * 0.5, 50)
  return `M ${x1} ${y1} C ${x1 + cp} ${y1}, ${x2 - cp} ${y2}, ${x2} ${y2}`
}

const getDrawingEdgePath = () => {
  if (!drawingEdge.value) return ''
  const source = nodes.value.find(n => n.id === drawingEdge.value.source)
  if (!source) return ''
  const x1 = source.x + nodeWidth
  const y1 = source.y + 30
  return `M ${x1} ${y1} L ${drawingEdge.value.mouseX} ${drawingEdge.value.mouseY}`
}

const zoomIn = () => { zoom.value = Math.min(zoom.value + 0.1, 2) }
const zoomOut = () => { zoom.value = Math.max(zoom.value - 0.1, 0.3) }
const resetZoom = () => { zoom.value = 1; panX.value = 0; panY.value = 0 }

const showExecutions = () => {
  if (workflowId.value) {
    uni.navigateTo({ url: `/pages/workflow/execution?id=${workflowId.value}` })
  } else {
    uni.showToast({ title: '请先保存流程', icon: 'none' })
  }
}

// Mouse event handlers for canvas
const onCanvasMouseDown = (event: MouseEvent) => {
  // Check if clicking on empty canvas (not on a node)
  if (drawingEdge.value) {
    drawingEdge.value = null
    return
  }
  isPanning.value = true
  panStart.value = { x: event.clientX - panX.value / zoom.value, y: event.clientY - panY.value / zoom.value }
}

const onCanvasMouseMove = (event: MouseEvent) => {
  if (isPanning.value) {
    panX.value = (event.clientX - panStart.value.x) * zoom.value
    panY.value = (event.clientY - panStart.value.y) * zoom.value
  }
  if (draggingNode.value) {
    const rect = (event.currentTarget as HTMLElement).closest('.canvas-area')?.getBoundingClientRect()
    if (rect) {
      draggingNode.value.x = event.clientX - dragOffset.value.x
      draggingNode.value.y = event.clientY - dragOffset.value.y
    }
  }
  if (drawingEdge.value) {
    const canvasEl = document.querySelector('.canvas') as HTMLElement
    if (canvasEl) {
      const rect = canvasEl.getBoundingClientRect()
      drawingEdge.value.mouseX = (event.clientX - rect.left) / zoom.value
      drawingEdge.value.mouseY = (event.clientY - rect.top) / zoom.value
    }
  }
}

const onCanvasMouseUp = () => {
  isPanning.value = false
  draggingNode.value = null
}

// Node drag handlers
const startNodeDrag = (event: MouseEvent, node: any) => {
  event.stopPropagation()
  draggingNode.value = node
  const canvasEl = document.querySelector('.canvas') as HTMLElement
  if (canvasEl) {
    const rect = canvasEl.getBoundingClientRect()
    dragOffset.value = { 
      x: event.clientX - rect.left - node.x * zoom.value + panX.value, 
      y: event.clientY - rect.top - node.y * zoom.value + panY.value 
    }
  }
}

// Picker change handlers
const onAgentChange = (e: any) => {
  selectedAgentIndex.value = e.detail.value
  if (selectedNode.value) {
    selectedNode.value.agentId = availableAgents.value[e.detail.value]?.id || ''
  }
}

const onSkillChange = (e: any) => {
  selectedSkillIndex.value = e.detail.value
  if (selectedNode.value) {
    selectedNode.value.skillId = availableSkills.value[e.detail.value]?.id || ''
  }
}

const onLoopTypeChange = (e: any) => {
  loopTypeIndex.value = e.detail.value
  loopType.value = loopTypeOptions[e.detail.value]?.label || 'count'
}

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
        config: n.config || '', x: n.x, y: n.y,
        agentId: n.agentId || '', skillId: n.skillId || ''
      })),
      edges: edges.value
    }
    let result
    if (workflowId.value) {
      result = await workflowService.update(workflowId.value, data as any)
    } else {
      result = await workflowService.create(data as any)
      workflowId.value = result.id
      workflowStatus.value = result.status || 'draft'
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

const loadWorkflow = async (id: string) => {
  if (id) {
    workflowId.value = id
    try {
      const wf = await workflowService.get(id)
      workflowName.value = wf.name
      workflowStatus.value = wf.status || 'draft'
      if (wf.nodes && wf.nodes.length > 0) {
        nodes.value = wf.nodes.map((n: any) => ({
          ...n, x: n.x || 100, y: n.y || 100
        }))
      }
      if (wf.edges && wf.edges.length > 0) {
        edges.value = wf.edges.map((e: any) => ({
          id: e.id || ('edge_' + Date.now()),
          source: e.source || e.sourceNode || '',
          target: e.target || e.targetNode || ''
        }))
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

onLoad((options: any) => {
  if (options?.id) {
    loadWorkflow(options.id)
  }
})

onMounted(() => {
  loadAgents()
  loadSkills()
  document.addEventListener('mouseup', handleGlobalMouseUp)
  document.addEventListener('mousemove', handleGlobalMouseMove)
})

onBeforeUnmount(() => {
  document.removeEventListener('mouseup', handleGlobalMouseUp)
  document.removeEventListener('mousemove', handleGlobalMouseMove)
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
  flex-shrink: 0;
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
  user-select: none;

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
  flex-shrink: 0;
}

.palette-title {
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 16px;
  display: block;
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
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    background: #eef2ff;
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
  position: sticky;
  top: 12px;
  right: 12px;
  float: right;
  margin-right: 16px;
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
  user-select: none;

  text { font-size: 16px; color: #6b7280; }

  &:hover { background: #e5e7eb; }
}

.toolbar-divider {
  width: 1px;
  height: 20px;
  background: #e5e7eb;
}

.zoom-level {
  font-size: 12px;
  color: #6b7280;
  min-width: 40px;
  text-align: center;
}

.canvas {
  position: relative;
}

.edges-layer {
  position: absolute;
  top: 0;
  left: 0;
  pointer-events: none;

  path {
    pointer-events: stroke;
  }
}

.canvas-node {
  position: absolute;
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
  flex-shrink: 0;
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

.config-input, .config-picker, .config-textarea {
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

.config-picker {
  cursor: pointer;
}

.picker-value {
  color: #1f2937;
  min-height: 18px;
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
