<template>
  <Layout>
    <view class="page">
      <view class="page-header">
        <view class="header-content">
          <text class="page-title">Agent详情</text>
        </view>
      </view>

      <view class="page-content" v-if="agent">
        <view class="agent-header">
          <view class="agent-icon-large">
            <text>{{ getDeptIcon(agent.department) }}</text>
          </view>
          <view class="agent-main">
            <view class="agent-title-row">
              <text class="agent-name">{{ agent.name }}</text>
              <view class="status-badge" :class="agent.status">
                <text>{{ getStatusText(agent.status) }}</text>
              </view>
            </view>
            <text class="agent-desc">{{ agent.description }}</text>
            <view class="agent-meta">
              <view class="meta-item">
                <text class="meta-value">{{ agent.successRate }}%</text>
                <text class="meta-label">成功率</text>
              </view>
              <view class="meta-divider"></view>
              <view class="meta-item">
                <text class="meta-value">{{ agent.avgTime }}s</text>
                <text class="meta-label">响应时间</text>
              </view>
              <view class="meta-divider"></view>
              <view class="meta-item">
                <text class="meta-value">{{ agent.rating || 0 }}⭐</text>
                <text class="meta-label">评分</text>
              </view>
            </view>
          </view>
        </view>

        <view class="section">
          <view class="section-title">
            <text>标签</text>
          </view>
          <view class="tags-wrap">
            <view class="tag" v-for="tag in agent.tags" :key="tag">
              <text>{{ tag }}</text>
            </view>
          </view>
        </view>

        <view class="section">
          <view class="section-title">
            <text>基本信息</text>
          </view>
          <view class="info-list">
            <view class="info-item">
              <text class="info-label">创建者</text>
              <text class="info-value">{{ agent.creatorId || '-' }}</text>
            </view>
            <view class="info-item">
              <text class="info-label">创建时间</text>
              <text class="info-value">{{ agent.createdAt }}</text>
            </view>
            <view class="info-item">
              <text class="info-label">所属部门</text>
              <text class="info-value">{{ getDeptName(agent.department) }}</text>
            </view>
          </view>
        </view>

        <view class="section">
          <view class="section-title">
            <text>使用说明</text>
          </view>
          <view class="guide-content">
            <text class="guide-text">
              1. 点击"立即使用"进入对话界面
              2. 根据提示输入相关参数
              3. 获取Agent返回的结果
              4. 如有问题可在下方反馈
            </text>
          </view>
        </view>

        <view class="section">
          <view class="section-header">
            <text class="section-title">用户评价</text>
            <text class="section-more">查看全部</text>
          </view>
          <view class="reviews-list">
            <view class="review-item" v-for="review in reviews" :key="review.id">
              <view class="review-header">
                <view class="reviewer">
                  <text>👤</text>
                  <text class="reviewer-name">{{ review.user }}</text>
                </view>
                <view class="review-rating">
                  <text v-for="i in 5" :key="i">{{ i <= review.rating ? '⭐' : '☆' }}</text>
                </view>
              </view>
              <text class="review-content">{{ review.content }}</text>
              <text class="review-time">{{ review.time }}</text>
            </view>
          </view>
        </view>
      </view>

      <view class="bottom-bar">
        <view class="action-btn secondary" @click="handleFavorite">
          <text>{{ agent.isFavorite ? '❤️ 已收藏' : '🤍 收藏' }}</text>
        </view>
        <view class="action-btn primary" @click="handleUse">
          <text>立即使用</text>
        </view>
      </view>
    </view>
  </Layout>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import Layout from '@/components/Layout.vue'
import { agentService } from '@/services/agent'
import type { Agent } from '@/data/agents'

const contentHeight = ref(600)
const agent = ref<Agent | null>(null)

const reviews = ref<{ id: string; user: string; rating: number; content: string; time: string }[]>([])

const getDeptIcon = (dept: string) => {
  const icons: Record<string, string> = {
    operation: '🚚',
    qc: '✅',
    customer: '💬',
    finance: '💰'
  }
  return icons[dept] || '🤖'
}

const getDeptName = (dept: string) => {
  const names: Record<string, string> = {
    operation: '运营部',
    qc: '质控部',
    customer: '客服部',
    finance: '财务部'
  }
  return names[dept] || dept
}

const getStatusText = (status: string) => {
  const texts: Record<string, string> = {
    online: '在线',
    offline: '离线',
    pending: '审核中'
  }
  return texts[status] || status
}

const handleFavorite = () => {
  if (!agent.value) return
  agent.value.isFavorite = !agent.value.isFavorite
  uni.showToast({
    title: agent.value.isFavorite ? '已收藏' : '取消收藏',
    icon: 'none'
  })
}

const handleUse = () => {
  if (!agent.value) return
  uni.redirectTo({ url: `/pages/agent/chat?id=${agent.value.id}` })
}

onMounted(async () => {
  uni.getSystemInfo({
    success: (res) => {
      contentHeight.value = res.windowHeight - 300
    }
  })

  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const options = (currentPage as unknown as { options?: { id?: string } }).options
  if (options?.id) {
    try {
      const agentData = await agentService.get(options.id)
      agent.value = agentData as Agent
    } catch (e) {
      console.error('Failed to load agent:', e)
    }
  }
})
</script>

<style lang="scss">
.page {
  min-height: 100vh;
  background: #f0f2f5;
  display: flex;
  flex-direction: column;
}

.page-header {
  background: #fff;
  padding: 16px 32px;
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

.page-content {
  flex: 1;
  padding: 24px 32px;
  padding-bottom: 120px;
}

.agent-header {
  display: flex;
  gap: 20px;
  padding: 24px;
  background: #fff;
  border-radius: 16px;
  margin-bottom: 20px;
}

.agent-icon-large {
  width: 80px;
  height: 80px;
  border-radius: 16px;
  background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  
  text {
    font-size: 36px;
  }
}

.agent-main {
  flex: 1;
}

.agent-title-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.agent-name {
  font-size: 24px;
  font-weight: 700;
  color: #1f2937;
}

.status-badge {
  padding: 4px 12px;
  border-radius: 12px;
  
  text {
    font-size: 12px;
  }
  
  &.online {
    background: #d1fae5;
    text { color: #065f46; }
  }
  
  &.offline {
    background: #f3f4f6;
    text { color: #6b7280; }
  }
  
  &.pending {
    background: #fef3c7;
    text { color: #d97706; }
  }
}

.agent-desc {
  font-size: 14px;
  color: #6b7280;
  margin-top: 8px;
  line-height: 1.5;
}

.agent-meta {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #f3f4f6;
}

.meta-item {
  display: flex;
  flex-direction: column;
}

.meta-value {
  font-size: 18px;
  font-weight: 600;
  color: #4f46e5;
}

.meta-label {
  font-size: 12px;
  color: #9ca3af;
  margin-top: 4px;
}

.meta-divider {
  width: 1px;
  height: 32px;
  background: #e5e7eb;
}

.section {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
}

.section-more {
  font-size: 13px;
  color: #4f46e5;
  cursor: pointer;
}

.tags-wrap {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 12px;
}

.tag {
  padding: 6px 14px;
  background: #eef2ff;
  border-radius: 6px;
  
  text {
    font-size: 13px;
    color: #4f46e5;
  }
}

.info-list {
  margin-top: 12px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  padding: 12px 0;
  border-bottom: 1px solid #f3f4f6;
  
  &:last-child {
    border-bottom: none;
  }
}

.info-label {
  font-size: 14px;
  color: #6b7280;
}

.info-value {
  font-size: 14px;
  color: #1f2937;
  font-weight: 500;
}

.guide-content {
  margin-top: 12px;
}

.guide-text {
  font-size: 14px;
  color: #6b7280;
  line-height: 1.8;
  white-space: pre-wrap;
}

.reviews-list {
  margin-top: 12px;
}

.review-item {
  padding: 16px 0;
  border-bottom: 1px solid #f3f4f6;
  
  &:last-child {
    border-bottom: none;
  }
}

.review-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.reviewer {
  display: flex;
  align-items: center;
  gap: 8px;
  
  text {
    font-size: 18px;
    font-weight: 500;
    color: #1f2937;
  }
}

.reviewer-name {
  font-size: 14px !important;
}

.review-rating {
  text {
    font-size: 16px;
  }
}

.review-content {
  font-size: 14px;
  color: #4b5563;
  margin-top: 10px;
  line-height: 1.5;
}

.review-time {
  font-size: 12px;
  color: #9ca3af;
  margin-top: 8px;
}

.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 200px;
  right: 0;
  display: flex;
  gap: 16px;
  padding: 16px 32px;
  background: #fff;
  border-top: 1px solid #e5e7eb;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.06);
}

.action-btn {
  flex: 1;
  max-width: 200px;
  height: 44px;
  border-radius: 22px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
  
  text {
    font-size: 14px;
    font-weight: 600;
  }
  
  &.secondary {
    background: #f3f4f6;
    text { color: #6b7280; }
    
    &:hover {
      background: #e5e7eb;
    }
  }
  
  &.primary {
    background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%);
    text { color: #fff; }
    
    &:hover {
      opacity: 0.9;
    }
  }
}
</style>