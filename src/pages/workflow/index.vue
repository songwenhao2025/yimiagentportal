<template>
  <Layout>
    <view class="page">
      <view class="page-header">
        <view class="header-content">
          <text class="page-title">流程编排</text>
          <view class="header-actions">
            <view class="btn primary" @click="goToDesigner">
              <text>+ 创建新流程</text>
            </view>
          </view>
        </view>
      </view>

      <view class="page-content">
        <view class="workflow-grid">
          <view class="workflow-card" v-for="wf in workflows" :key="wf.id" @click="selectWorkflow(wf)">
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
              <text>创建者: {{ wf.creator }}</text>
              <view class="wf-actions">
                <view class="action-btn" @click.stop="runWorkflow(wf)">
                  <text>运行</text>
                </view>
                <view class="action-btn primary" @click.stop="editWorkflow(wf)">
                  <text>编辑</text>
                </view>
              </view>
            </view>
          </view>
        </view>
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
  const texts: Record<string, string> = {
    draft: '草稿',
    active: '运行中',
    inactive: '已停用'
  }
  return texts[status] || status
}

const getTriggerText = (type: string) => {
  const texts: Record<string, string> = {
    api: 'API触发',
    cron: '定时触发',
    event: '事件触发'
  }
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

const selectWorkflow = (wf: Workflow) => {
  uni.showToast({ title: `查看: ${wf.name}`, icon: 'none' })
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

const editWorkflow = (wf: Workflow) => {
  uni.navigateTo({ url: `/pages/workflow/designer?id=${wf.id}` })
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
  min-height: 100vh;
  background: #f0f2f5;
}

.page-header {
  background: #fff;
  padding: 20px 32px;
  border-bottom: 1px solid #e8e8e8;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.page-title {
  font-size: 24px;
  font-weight: 700;
  color: #1f2937;
}

.header-actions {
  display: flex;
  gap: 16px;
}

.btn {
  padding: 10px 20px;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
  
  &.primary {
    background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%);
    color: #fff;
    
    &:hover {
      opacity: 0.9;
    }
  }

  &.secondary {
    background: #f3f4f6;
    color: #6b7280;
    border: 1px solid #e5e7eb;
  }
}

.page-content {
  padding: 24px 32px;
}

.workflow-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}


.workflow-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid #f3f4f6;
  
  &:hover {
    border-color: #4f46e5;
    box-shadow: 0 4px 12px rgba(79, 70, 229, 0.1);
  }
}

.wf-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.wf-name {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
}

.wf-status {
  padding: 4px 12px;
  border-radius: 6px;
  
  text {
    font-size: 12px;
  }
  
  &.draft {
    background: #f3f4f6;
    text { color: #6b7280; }
  }
  
  &.active {
    background: #d1fae5;
    text { color: #065f46; }
  }
  
  &.inactive {
    background: #fee2e2;
    text { color: #dc2626; }
  }
}

.wf-desc {
  font-size: 14px;
  color: #6b7280;
  margin-top: 10px;
  line-height: 1.4;
}

.wf-meta {
  display: flex;
  gap: 24px;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #f3f4f6;
}

.meta-item {
  display: flex;
  flex-direction: column;
}

.meta-label {
  font-size: 12px;
  color: #9ca3af;
}

.meta-value {
  font-size: 14px;
  font-weight: 500;
  color: #1f2937;
  margin-top: 4px;
  
  &.success { color: #10b981; }
  &.warning { color: #f59e0b; }
}

.wf-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #f3f4f6;
  
  text {
    font-size: 13px;
    color: #9ca3af;
  }
}

.wf-actions {
  display: flex;
  gap: 10px;
}

.action-btn {
  padding: 8px 16px;
  background: #f3f4f6;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
  
  text {
    font-size: 13px;
    color: #6b7280;
  }
  
  &:hover {
    background: #e5e7eb;
  }
  
  &.primary {
    background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%);
    
    text {
      color: #fff;
    }
    
    &:hover {
      opacity: 0.9;
    }
  }
}
</style>
