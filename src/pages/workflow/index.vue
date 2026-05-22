<template>
  <Layout>
    <view class="page">
      <view class="page-header">
        <text class="page-title">流程编排</text>
        <view class="btn primary" @click="goToDesigner">
          <text>+ 创建新流程</text>
        </view>
      </view>

      <view class="workflow-grid">
        <view class="workflow-card" v-for="wf in workflows" :key="wf.id" @click="editWorkflow(wf.id)">
          <view class="wf-header">
            <text class="wf-name">{{ wf.name }}</text>
            <view class="wf-status" :class="wf.status">
              <text>{{ getStatusText(wf.status) }}</text>
            </view>
          </view>
          <text class="wf-desc">{{ wf.description }}</text>
          <view class="wf-meta">
            <view class="meta-item">
              <text class="meta-label">触发方式</text>
              <text class="meta-value">{{ getTriggerText(wf.triggerType) }}</text>
            </view>
            <view class="meta-item">
              <text class="meta-label">执行次数</text>
              <text class="meta-value">{{ wf.executionCount }}</text>
            </view>
            <view class="meta-item">
              <text class="meta-label">成功率</text>
              <text class="meta-value" :class="wf.successRate >= 95 ? 'success' : 'warning'">
                {{ wf.successRate }}%
              </text>
            </view>
          </view>
          <view class="wf-footer">
            <text class="creator">创建者: {{ wf.creator }}</text>
            <view class="wf-actions">
              <view class="action-btn" @click.stop="runWorkflow(wf)">
                <text>▶ 运行</text>
              </view>
              <view class="action-btn" @click.stop="viewExecutions(wf.id)">
                <text>📋 记录</text>
              </view>
              <view class="action-btn primary" @click.stop="editWorkflow(wf.id)">
                <text>编辑</text>
              </view>
            </view>
          </view>
        </view>
      </view>

      <view class="empty-state" v-if="workflows.length === 0">
        <text class="empty-icon">🔗</text>
        <text class="empty-text">暂无流程</text>
      </view>
    </view>
  </Layout>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import Layout from '@/components/Layout.vue'
import { workflowService } from '@/services/workflow'
import type { Workflow } from '@/data/workflows'

const workflows = ref<Workflow[]>([])

const getStatusText = (status: string) => {
  const texts: Record<string, string> = { draft: '草稿', active: '运行中', inactive: '已停用' }
  return texts[status] || status
}

const getTriggerText = (type: string) => {
  const texts: Record<string, string> = { api: 'API触发', cron: '定时触发', event: '事件触发' }
  return texts[type] || type
}

const loadWorkflows = async () => {
  try {
    const response = await workflowService.list({ page: 1, size: 100 })
    workflows.value = response.list || []
  } catch (error) {
    console.error('Failed to load workflows:', error)
  }
}

const runWorkflow = (wf: Workflow) => {
  uni.showModal({
    title: '运行流程',
    content: `确认运行 "${wf.name}" 吗？`,
    success: async (res) => {
      if (res.confirm) {
        try {
          await workflowService.execute(wf.id)
          uni.showToast({ title: '流程已启动', icon: 'success' })
          loadWorkflows()
        } catch (error) {
          console.error('Failed to run workflow:', error)
        }
      }
    }
  })
}

const viewExecutions = (id: string) => {
  uni.navigateTo({ url: `/pages/workflow/execution?id=${id}` })
}

const editWorkflow = (id: string) => {
  uni.navigateTo({ url: `/pages/workflow/designer?id=${id}` })
}

const goToDesigner = () => {
  uni.navigateTo({ url: '/pages/workflow/designer' })
}

onMounted(() => {
  loadWorkflows()
})
</script>

<style lang="scss">
.page {
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-title {
  font-size: 24px;
  font-weight: 700;
  color: #1a1a1a;
}

.btn {
  padding: 8px 20px;
  border-radius: 24px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;

  &.primary { background: #1890ff; color: #fff; &:hover { background: #40a9ff; } }
}

.workflow-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
}

.workflow-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  cursor: pointer;
  transition: all 0.3s;

  &:hover { transform: translateY(-4px); box-shadow: 0 8px 20px rgba(0, 0, 0, 0.08); }
}

.wf-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.wf-name { font-size: 16px; font-weight: 600; color: #1a1a1a; flex: 1; }

.wf-status {
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 11px;

  &.active { background: #f6ffed; color: #52c41a; }
  &.draft { background: #f5f5f5; color: #999; }
  &.inactive { background: #fffbe6; color: #faad14; }
}

.wf-desc {
  font-size: 13px;
  color: #888;
  line-height: 1.5;
  margin-bottom: 16px;
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.wf-meta {
  display: flex;
  gap: 16px;
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.meta-item {
  display: flex;
  flex-direction: column;
  flex: 1;
}

.meta-label { font-size: 11px; color: #999; margin-bottom: 4px; }
.meta-value { font-size: 13px; font-weight: 500; color: #333; &.success { color: #52c41a; } &.warning { color: #faad14; } }

.wf-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.creator { font-size: 12px; color: #999; }

.wf-actions { display: flex; gap: 8px; }

.action-btn {
  padding: 6px 12px;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;

  &.primary { background: #1890ff; color: #fff; }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 60px;
  background: #fff;
  border-radius: 12px;
}

.empty-icon { font-size: 48px; opacity: 0.5; margin-bottom: 12px; }
.empty-text { font-size: 16px; color: #888; }
</style>
