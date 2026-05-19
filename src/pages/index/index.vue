<template>
  <Layout>
    <view class="page">
      <view class="page-header">
        <view class="header-content">
          <view class="header-left">
            <text class="page-title">工作台</text>
            <text class="page-subtitle">欢迎回来，张主管</text>
          </view>
          <view class="header-right">
            <view class="header-action">
              <text>🔔</text>
              <view class="badge">3</view>
            </view>
            <text class="header-date">{{ currentDate }}</text>
          </view>
        </view>
      </view>

      <view class="page-content">
        <view class="stats-section">
          <view class="stat-card">
            <view class="stat-icon primary">🤖</view>
            <view class="stat-info">
              <text class="stat-value">{{ stats.totalAgents }}</text>
              <text class="stat-label">可用Agent</text>
            </view>
          </view>
          <view class="stat-card">
            <view class="stat-icon success">📊</view>
            <view class="stat-info">
              <text class="stat-value">{{ formatNumber(stats.totalCalls) }}</text>
              <text class="stat-label">总调用次数</text>
            </view>
          </view>
          <view class="stat-card">
            <view class="stat-icon info">💰</view>
            <view class="stat-info">
              <text class="stat-value">¥{{ stats.totalCost.toFixed(2) }}</text>
              <text class="stat-label">总成本</text>
            </view>
          </view>
        </view>

        <view class="main-grid">
          <view class="left-column">
            <view class="card">
              <view class="card-header">
                <text class="card-title">常用Agent</text>
                <text class="card-more" @click="goToMarket">查看全部 →</text>
              </view>
              <view class="agents-grid">
                <view class="agent-item" v-for="agent in favoriteAgents" :key="agent.id" @click="goToChat(agent.id)">
                  <view class="agent-icon">{{ getDeptIcon(agent.department) }}</view>
                  <text class="agent-name">{{ agent.name }}</text>
                  <text class="agent-status" :class="agent.status">{{ getStatusText(agent.status) }}</text>
                </view>
              </view>
            </view>

            <view class="card">
              <view class="card-header">
                <text class="card-title">待办任务</text>
                <text class="card-more">全部 →</text>
              </view>
              <view class="todo-list">
                <view class="todo-item" v-for="todo in todos" :key="todo.id">
                  <view class="todo-priority" :class="todo.priority"></view>
                  <view class="todo-content">
                    <text class="todo-title">{{ todo.title }}</text>
                    <text class="todo-desc">{{ todo.desc }}</text>
                  </view>
                  <text class="todo-time">{{ todo.time }}</text>
                </view>
              </view>
            </view>
          </view>

          <view class="right-column">
            <view class="card">
              <view class="card-header">
                <text class="card-title">推荐Agent</text>
              </view>
              <view class="recommend-list">
                <AgentCard 
                  v-for="agent in recommendAgents" 
                  :key="agent.id" 
                  :agent="agent"
                  @use="goToChat"
                  compact
                />
              </view>
            </view>

            <view class="card notice-card">
              <view class="card-header">
                <text class="card-title">📢 系统公告</text>
              </view>
              <view class="notice-content">
                <text class="notice-title">新功能上线：多Agent流程编排</text>
                <text class="notice-desc">现在支持拖拽式流程编排，轻松构建复杂业务场景</text>
                <text class="notice-time">2024-04-15</text>
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
import AgentCard from '@/components/AgentCard.vue'
import { agentService } from '@/services/agent'
import { adminService } from '@/services/admin'
import type { Agent } from '@/data/agents'

const currentDate = new Date().toLocaleDateString('zh-CN', {
  year: 'numeric',
  month: 'long',
  day: 'numeric'
})

const favoriteAgents = ref<Agent[]>([])
const recommendAgents = ref<Agent[]>([])
const stats = ref({
  totalAgents: 0,
  totalCalls: 0,
  totalCost: 0
})

const todos = ref([
  { id: '1', title: '审核新Agent申请', desc: '车线管理Agent v2.0', priority: 'high', time: '10分钟前' },
  { id: '2', title: '查看成本报表', desc: '4月第一周', priority: 'medium', time: '1小时前' },
  { id: '3', title: '技能更新提醒', desc: '查询路由技能已更新', priority: 'low', time: '3小时前' }
])

const loadAgents = async () => {
  try {
    const response = await agentService.list({ page: 1, size: 100 })
    const allAgents = response.list || []
    favoriteAgents.value = allAgents.filter(a => a.isFavorite)
    recommendAgents.value = allAgents.filter(a => !a.isFavorite).slice(0, 3)
  } catch (error) {
    console.error('Failed to load agents:', error)
  }
}

const loadStats = async () => {
  try {
    const response = await adminService.getStatistics()
    stats.value = {
      totalAgents: response.totalAgents || 0,
      totalCalls: response.totalCalls || 0,
      totalCost: response.totalCost || 0
    }
  } catch (error) {
    console.error('Failed to load stats:', error)
  }
}

onMounted(() => {
  loadAgents()
  loadStats()
})

const formatNumber = (num: number) => {
  return num.toLocaleString('zh-CN')
}

const getDeptIcon = (dept: string) => {
  const icons: Record<string, string> = {
    operation: '🚚',
    qc: '✅',
    customer: '💬',
    finance: '💰'
  }
  return icons[dept] || '🤖'
}

const getStatusText = (status: string) => {
  const texts: Record<string, string> = {
    online: '在线',
    offline: '离线',
    pending: '审核中'
  }
  return texts[status] || status
}

const goToMarket = () => {
  uni.navigateTo({ url: '/pages/market/index' })
}

const goToChat = (agent: Agent | string) => {
  const id = typeof agent === 'string' ? agent : agent.id
  uni.navigateTo({ url: `/pages/agent/chat?id=${id}` })
}
</script>

<style lang="scss">
.page {
  min-height: 100vh;
  background: #f0f2f5;
}

.page-header {
  background: #fff;
  padding: 24px 32px;
  border-bottom: 1px solid #e8e8e8;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  flex-direction: column;
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  color: #1f2937;
}

.page-subtitle {
  font-size: 14px;
  color: #6b7280;
  margin-top: 4px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.header-action {
  position: relative;
  width: 40px;
  height: 40px;
  border-radius: 8px;
  background: #f3f4f6;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  
  text {
    font-size: 18px;
  }
}

.badge {
  position: absolute;
  top: -4px;
  right: -4px;
  min-width: 18px;
  height: 18px;
  background: #ef4444;
  border-radius: 9px;
  font-size: 12px;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 4px;
}

.header-date {
  font-size: 14px;
  color: #6b7280;
}

.page-content {
  padding: 24px 32px;
}

.stats-section {
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
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  
  &.primary { background: linear-gradient(135deg, #eef2ff 0%, #ddd6fe 100%); }
  &.success { background: linear-gradient(135deg, #d1fae5 0%, #a7f3d0 100%); }
  &.info { background: linear-gradient(135deg, #dbeafe 0%, #bfdbfe 100%); }
  &.warning { background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%); }
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #1f2937;
}

.stat-label {
  font-size: 13px;
  color: #9ca3af;
  margin-top: 2px;
}

.stat-trend {
  font-size: 13px;
  font-weight: 500;
  
  &.up { color: #10b981; }
  &.down { color: #ef4444; }
}

.main-grid {
  display: grid;
  grid-template-columns: 1fr 400px;
  gap: 24px;
}

.left-column, .right-column {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
}

.card-more {
  font-size: 13px;
  color: #4f46e5;
  cursor: pointer;
  
  &:hover {
    text-decoration: underline;
  }
}

.agents-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.agent-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 16px 12px;
  background: #f9fafb;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s;
  
  &:hover {
    background: #eef2ff;
    transform: translateY(-2px);
  }
}

.agent-icon {
  width: 48px;
  height: 48px;
  border-radius: 10px;
  background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  margin-bottom: 8px;
}

.agent-name {
  font-size: 13px;
  font-weight: 500;
  color: #1f2937;
  text-align: center;
}

.agent-status {
  font-size: 11px;
  margin-top: 4px;
  
  &.online { color: #10b981; }
  &.offline { color: #9ca3af; }
  &.pending { color: #f59e0b; }
}

.todo-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.todo-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: #f9fafb;
  border-radius: 8px;
}

.todo-priority {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  
  &.high { background: #ef4444; }
  &.medium { background: #f59e0b; }
  &.low { background: #10b981; }
}

.todo-content {
  flex: 1;
}

.todo-title {
  font-size: 14px;
  font-weight: 500;
  color: #1f2937;
}

.todo-desc {
  font-size: 12px;
  color: #9ca3af;
  margin-top: 2px;
}

.todo-time {
  font-size: 12px;
  color: #9ca3af;
}

.recommend-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.notice-card {
  background: linear-gradient(135deg, #fffbeb 0%, #fef3c7 100%);
  border: 1px solid #fde68a;
}

.notice-content {
  display: flex;
  flex-direction: column;
}

.notice-title {
  font-size: 14px;
  font-weight: 600;
  color: #92400e;
}

.notice-desc {
  font-size: 13px;
  color: #b45309;
  margin-top: 8px;
  line-height: 1.5;
}

.notice-time {
  font-size: 12px;
  color: #d97706;
  margin-top: 8px;
}
</style>
