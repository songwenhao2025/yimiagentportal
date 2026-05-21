<template>
  <Layout>
    <view class="page">
      <!-- Header -->
      <view class="page-header">
        <text class="page-title">工作台</text>
        <text class="page-subtitle">欢迎回来，这里是您的 AI Agent 管理中心</text>
      </view>

      <!-- Stats Cards -->
      <view class="stats-grid">
        <view class="stat-card">
          <view class="stat-icon primary">🤖</view>
          <view class="stat-content">
            <text class="stat-value">12</text>
            <text class="stat-label">可用 Agent</text>
          </view>
        </view>
        <view class="stat-card">
          <view class="stat-icon success">📊</view>
          <view class="stat-content">
            <text class="stat-value">3,450</text>
            <text class="stat-label">本周调用</text>
          </view>
        </view>
        <view class="stat-card">
          <view class="stat-icon info">⚡</view>
          <view class="stat-content">
            <text class="stat-value">8</text>
            <text class="stat-label">运行中流程</text>
          </view>
        </view>
        <view class="stat-card">
          <view class="stat-icon warning">💰</view>
          <view class="stat-content">
            <text class="stat-value">¥2,400</text>
            <text class="stat-label">本月消耗</text>
          </view>
        </view>
      </view>

      <!-- Main Content -->
      <view class="content-grid">
        <!-- Left Column -->
        <view class="column">
          <!-- Common Agents -->
          <view class="card">
            <view class="card-header">
              <text class="card-title">常用 Agent</text>
              <text class="card-more" @click="goToMarket">查看全部 →</text>
            </view>
            <view class="agent-grid">
              <view class="agent-item" v-for="agent in agents.slice(0, 4)" :key="agent.id" @click="goToChat(agent.id)">
                <view class="agent-icon">{{ getDeptIcon(agent.department) }}</view>
                <text class="agent-name">{{ agent.name }}</text>
                <text class="agent-status" :class="agent.status">{{ getStatusText(agent.status) }}</text>
              </view>
            </view>
          </view>

          <!-- Recent Tasks -->
          <view class="card">
            <view class="card-header">
              <text class="card-title">待办任务</text>
              <text class="card-more">全部 →</text>
            </view>
            <view class="task-list">
              <view class="task-item" v-for="task in tasks" :key="task.id">
                <view class="task-priority" :class="task.priority"></view>
                <view class="task-content">
                  <text class="task-title">{{ task.title }}</text>
                  <text class="task-desc">{{ task.desc }}</text>
                </view>
                <text class="task-time">{{ task.time }}</text>
              </view>
            </view>
          </view>
        </view>

        <!-- Right Column -->
        <view class="column">
          <!-- Recommended Agents -->
          <view class="card">
            <view class="card-header">
              <text class="card-title">推荐 Agent</text>
            </view>
            <view class="recommend-list">
              <view class="recommend-item" v-for="agent in agents.slice(4, 8)" :key="agent.id">
                <view class="recommend-icon">{{ getDeptIcon(agent.department) }}</view>
                <view class="recommend-info">
                  <text class="recommend-name">{{ agent.name }}</text>
                  <text class="recommend-desc">{{ agent.description }}</text>
                </view>
                <view class="recommend-action" @click.stop="goToChat(agent.id)">
                  <text>使用</text>
                </view>
              </view>
            </view>
          </view>

          <!-- Notice -->
          <view class="notice-card">
            <text class="notice-title">📢 新功能上线</text>
            <text class="notice-desc">支持多 Agent 协作编排，复杂业务一键自动化。</text>
            <text class="notice-time">2024-04-15</text>
          </view>
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

const agents = ref<Agent[]>([])
const tasks = ref([
  { id: '1', title: '审核新 Agent 申请', desc: '车线管理 Agent v2.0', priority: 'high', time: '10分钟前' },
  { id: '2', title: '查看成本报表', desc: '4月第一周', priority: 'medium', time: '1小时前' },
  { id: '3', title: '技能更新提醒', desc: '查询路由技能已更新', priority: 'low', time: '3小时前' }
])

onMounted(async () => {
  try {
    const res = await agentService.list({ page: 1, size: 100 })
    agents.value = res.list || []
  } catch (e) {
    console.error(e)
  }
})

const getDeptIcon = (dept: string) => {
  const map: Record<string, string> = { operation: '🚚', qc: '✅', customer: '💬', finance: '💰' }
  return map[dept] || '🤖'
}

const getStatusText = (status: string) => {
  const map: Record<string, string> = { online: '在线', offline: '离线', pending: '审核中' }
  return map[status] || status
}

const goToMarket = () => { uni.navigateTo({ url: '/pages/market/index' }) }
const goToChat = (id: string) => { uni.navigateTo({ url: `/pages/agent/chat?id=${id}` }) }
</script>

<style lang="scss">
.page {
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 24px;
}

.page-title {
  font-size: 24px;
  font-weight: 700;
  color: #1a1a1a;
}

.page-subtitle {
  font-size: 14px;
  color: #888;
  margin-top: 4px;
}

/* Stats Grid */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.stat-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  transition: transform 0.2s;

  &:hover {
    transform: translateY(-2px);
  }
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;

  &.primary { background: #e6f7ff; }
  &.success { background: #f6ffed; }
  &.info { background: #e6fffb; }
  &.warning { background: #fffbe6; }
}

.stat-content {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #1a1a1a;
}

.stat-label {
  font-size: 13px;
  color: #888;
  margin-top: 4px;
}

/* Content Grid */
.content-grid {
  display: grid;
  grid-template-columns: 1fr 400px;
  gap: 24px;
}

.column {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* Common Card */
.card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
}

.card-more {
  font-size: 13px;
  color: #1890ff;
  cursor: pointer;

  &:hover { text-decoration: underline; }
}

/* Agent Grid */
.agent-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.agent-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 16px 12px;
  border-radius: 8px;
  background: #f8f9fa;
  cursor: pointer;
  transition: background 0.2s;

  &:hover { background: #e6f7ff; }
}

.agent-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background: linear-gradient(135deg, #1890ff, #096dd9);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  margin-bottom: 8px;
}

.agent-name {
  font-size: 13px;
  font-weight: 500;
  color: #333;
  text-align: center;
  margin-bottom: 4px;
}

.agent-status {
  font-size: 11px;

  &.online { color: #52c41a; }
  &.offline { color: #999; }
  &.pending { color: #faad14; }
}

/* Task List */
.task-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.task-item {
  display: flex;
  align-items: center;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 8px;
  gap: 12px;
}

.task-priority {
  width: 8px;
  height: 8px;
  border-radius: 50%;

  &.high { background: #ff4d4f; }
  &.medium { background: #faad14; }
  &.low { background: #52c41a; }
}

.task-content {
  flex: 1;
}

.task-title {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.task-desc {
  font-size: 12px;
  color: #888;
  margin-top: 2px;
}

.task-time {
  font-size: 12px;
  color: #999;
}

/* Recommend List */
.recommend-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.recommend-item {
  display: flex;
  align-items: center;
  padding: 12px;
  border-radius: 8px;
  background: #f8f9fa;
  gap: 12px;
}

.recommend-icon {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  background: linear-gradient(135deg, #1890ff, #096dd9);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
}

.recommend-info {
  flex: 1;
}

.recommend-name {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.recommend-desc {
  font-size: 12px;
  color: #888;
  margin-top: 2px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.recommend-action {
  padding: 6px 12px;
  border-radius: 4px;
  background: #fff;
  color: #1890ff;
  font-size: 12px;
  cursor: pointer;
  border: 1px solid #1890ff;

  &:hover { background: #e6f7ff; }
}

/* Notice Card */
.notice-card {
  background: linear-gradient(135deg, #e6f7ff, #bae7ff);
  border-radius: 12px;
  padding: 24px;
  border: 1px solid #91d5ff;
}

.notice-title {
  font-size: 16px;
  font-weight: 600;
  color: #0050b3;
}

.notice-desc {
  font-size: 13px;
  color: #003a8c;
  margin-top: 8px;
  line-height: 1.5;
}

.notice-time {
  font-size: 12px;
  color: #1890ff;
  margin-top: 12px;
}
</style>
