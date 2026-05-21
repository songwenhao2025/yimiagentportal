<template>
  <Layout>
    <view class="page">
      <view class="page-header">
        <view class="header-left">
          <view class="btn back-btn" @click="goBack">
            <text>← 返回</text>
          </view>
          <text class="page-title">执行历史</text>
        </view>
      </view>

      <view class="page-content">
        <view class="filter-bar">
          <view class="filter-item" :class="{ active: statusFilter === '' }" @click="setStatusFilter('')">
            <text>全部</text>
          </view>
          <view class="filter-item" :class="{ active: statusFilter === 'running' }" @click="setStatusFilter('running')">
            <text>🟢 运行中</text>
          </view>
          <view class="filter-item" :class="{ active: statusFilter === 'completed' }" @click="setStatusFilter('completed')">
            <text>✅ 已完成</text>
          </view>
          <view class="filter-item" :class="{ active: statusFilter === 'failed' }" @click="setStatusFilter('failed')">
            <text>❌ 失败</text>
          </view>
        </view>

        <view class="execution-list">
          <view class="exec-card" v-for="exec in filteredExecutions" :key="exec.id">
            <view class="exec-header">
              <view class="exec-id">
                <text class="exec-icon">{{ getStatusIcon(exec.status) }}</text>
                <text class="exec-hash">{{ exec.id.substring(0, 8) }}</text>
              </view>
              <view class="exec-status" :class="exec.status">
                <text>{{ getStatusText(exec.status) }}</text>
              </view>
            </view>

            <view class="exec-meta">
              <view class="meta-row">
                <text class="meta-label">触发方式</text>
                <text class="meta-value">{{ getTriggerText(exec.triggerType) }}</text>
              </view>
              <view class="meta-row">
                <text class="meta-label">开始时间</text>
                <text class="meta-value">{{ formatTime(exec.startTime) }}</text>
              </view>
              <view class="meta-row" v-if="exec.duration">
                <text class="meta-label">耗时</text>
                <text class="meta-value">{{ formatDuration(exec.duration) }}</text>
              </view>
            </view>

            <view class="exec-actions">
              <view class="action-btn" @click="viewDetail(exec)">
                <text>查看详情</text>
              </view>
              <view class="action-btn" v-if="exec.status === 'running'" @click="stopExecution(exec)">
                <text class="danger">停止</text>
              </view>
              <view class="action-btn" v-if="exec.status === 'failed'" @click="retryExecution(exec)">
                <text class="primary">重试</text>
              </view>
            </view>
          </view>

          <view class="empty-state" v-if="filteredExecutions.length === 0 && !loading">
            <text class="empty-icon">📭</text>
            <text class="empty-text">暂无执行记录</text>
          </view>
        </view>
      </view>
    </view>
  </Layout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import Layout from '@/components/Layout.vue'
import { workflowService } from '@/services/workflow'

const workflowId = ref('')
const statusFilter = ref('')
const executions = ref<any[]>([])
const loading = ref(true)

const filteredExecutions = computed(() => {
  if (!statusFilter.value) return executions.value
  return executions.value.filter(e => e.status === statusFilter.value)
})

const setStatusFilter = (status: string) => { statusFilter.value = status }

const getStatusIcon = (status: string) => {
  const map: Record<string, string> = { running: '🟢', completed: '✅', failed: '❌', cancelled: '⏹' }
  return map[status] || '❓'
}

const getStatusText = (status: string) => {
  const map: Record<string, string> = { running: '运行中', completed: '已完成', failed: '失败', cancelled: '已取消' }
  return map[status] || status
}

const getTriggerText = (type: string) => {
  const map: Record<string, string> = { api: 'API触发', cron: '定时触发', event: '事件触发', user: '手动触发' }
  return map[type] || type
}

const formatTime = (time: string) => {
  if (!time) return '-'
  const d = new Date(time)
  return d.toLocaleString('zh-CN', { month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit', second: '2-digit' })
}

const formatDuration = (ms: number) => {
  if (ms < 1000) return ms + 'ms'
  return (ms / 1000).toFixed(1) + 's'
}

const goBack = () => { uni.navigateBack() }

const viewDetail = (exec: any) => {
  uni.showModal({
    title: '执行详情',
    content: `执行ID: ${exec.id}\n状态: ${getStatusText(exec.status)}\n耗时: ${formatDuration(exec.duration || 0)}\n触发方式: ${getTriggerText(exec.triggerType)}\n开始时间: ${formatTime(exec.startTime)}${exec.outputData ? '\n\n输出数据:\n' + exec.outputData : ''}${exec.errorMessage ? '\n\n错误信息:\n' + exec.errorMessage : ''}`,
    showCancel: false
  })
}

const stopExecution = (exec: any) => {
  uni.showToast({ title: '停止功能开发中', icon: 'none' })
}

const retryExecution = async (exec: any) => {
  try {
    uni.showLoading({ title: '重试中...' })
    await workflowService.execute(exec.workflowId, exec.inputData ? JSON.parse(exec.inputData) : undefined)
    uni.hideLoading()
    uni.showToast({ title: '已启动重试', icon: 'success' })
    loadExecutions()
  } catch (e) {
    uni.hideLoading()
    uni.showToast({ title: '重试失败', icon: 'none' })
  }
}

const loadExecutions = async () => {
  loading.value = true
  try {
    if (workflowId.value) {
      const res = await workflowService.getExecutions(workflowId.value, { page: 1, size: 100 })
      executions.value = res.list || []
    }
  } catch (error) {
    console.error('Failed to load executions:', error)
  } finally {
    loading.value = false
  }
}

onLoad((options: any) => {
  if (options?.id) {
    workflowId.value = options.id
  }
})

onMounted(() => { loadExecutions() })
</script>

<style lang="scss">
.page {
  min-height: 100vh;
  background: #f0f2f5;
}

.page-header {
  background: #fff;
  padding: 16px 32px;
  border-bottom: 1px solid #e8e8e8;
  display: flex;
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

.page-title {
  font-size: 20px;
  font-weight: 700;
  color: #1f2937;
}

.page-content {
  padding: 24px 32px;
}

.filter-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
}

.filter-item {
  padding: 8px 16px;
  background: #fff;
  border-radius: 8px;
  cursor: pointer;

  text { font-size: 13px; color: #6b7280; }

  &.active {
    background: #4f46e5;
    text { color: #fff; }
  }
}

.execution-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 16px;
}

.exec-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  border: 1px solid #e8e8e8;
}

.exec-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.exec-id {
  display: flex;
  align-items: center;
  gap: 8px;
}

.exec-icon { font-size: 18px; }

.exec-hash {
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
  font-family: 'Monaco', 'Menlo', monospace;
}

.exec-status {
  padding: 4px 12px;
  border-radius: 6px;
  text { font-size: 12px; }

  &.running { background: #d1fae5; text { color: #065f46; } }
  &.completed { background: #dbeafe; text { color: #1d4ed8; } }
  &.failed { background: #fee2e2; text { color: #dc2626; } }
  &.cancelled { background: #f3f4f6; text { color: #6b7280; } }
}

.exec-meta {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 16px;
}

.meta-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.meta-label { font-size: 13px; color: #9ca3af; }
.meta-value { font-size: 13px; color: #1f2937; font-weight: 500; }

.exec-actions {
  display: flex;
  gap: 10px;
  padding-top: 12px;
  border-top: 1px solid #f3f4f6;
}

.action-btn {
  flex: 1;
  padding: 8px;
  background: #f3f4f6;
  border-radius: 6px;
  text-align: center;
  cursor: pointer;

  text { font-size: 13px; color: #6b7280; }

  &:hover { background: #e5e7eb; }

  .danger { color: #dc2626; }
  .primary { color: #4f46e5; font-weight: 600; }
}

.empty-state {
  grid-column: 1 / -1;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 60px 20px;
}

.empty-icon { font-size: 48px; margin-bottom: 16px; }
.empty-text { font-size: 16px; color: #9ca3af; }
</style>
