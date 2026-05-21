<template>
  <Layout>
    <view class="page">
      <!-- Header -->
      <view class="page-header">
        <text class="page-title">监控分析</text>
        <view class="time-range">
          <view class="range-chip" :class="{ active: timeRange === '7d' }" @click="timeRange = '7d'">
            <text>7天</text>
          </view>
          <view class="range-chip" :class="{ active: timeRange === '30d' }" @click="timeRange = '30d'">
            <text>30天</text>
          </view>
          <view class="range-chip" :class="{ active: timeRange === '90d' }" @click="timeRange = '90d'">
            <text>90天</text>
          </view>
        </view>
      </view>

      <!-- Stats -->
      <view class="stats-grid">
        <view class="stat-card">
          <view class="stat-icon primary">📊</view>
          <view class="stat-info">
            <text class="stat-value">{{ totalCallsFormatted }}</text>
            <text class="stat-label">总调用次数</text>
          </view>
        </view>
        <view class="stat-card">
          <view class="stat-icon success">✅</view>
          <view class="stat-info">
            <text class="stat-value">{{ stats ? stats.totalAgents : '0' }}个</text>
            <text class="stat-label">Agent数量</text>
          </view>
        </view>
        <view class="stat-card">
          <view class="stat-icon info">👥</view>
          <view class="stat-info">
            <text class="stat-value">{{ stats ? stats.activeUsers : '0' }}人</text>
            <text class="stat-label">活跃用户</text>
          </view>
        </view>
        <view class="stat-card">
          <view class="stat-icon warning">💰</view>
          <view class="stat-info">
            <text class="stat-value">¥{{ stats ? stats.totalCost.toFixed(2) : '0.00' }}</text>
            <text class="stat-label">总费用</text>
          </view>
        </view>
      </view>

      <!-- Content -->
      <view class="content-grid">
        <!-- Left: Charts -->
        <view class="column">
          <view class="card">
            <text class="card-title">调用趋势</text>
            <view class="chart-box">
              <view class="chart-bars">
                <view class="bar-item" v-for="(val, idx) in chartData" :key="idx">
                  <view class="bar" :style="{ height: val * 2 + 'px' }"></view>
                  <text class="bar-label">{{ chartLabels[idx] }}</text>
                </view>
              </view>
            </view>
          </view>

          <view class="card">
            <text class="card-title">用户满意度</text>
            <view class="satisfaction-box">
              <view class="score-ring">
                <text class="score-value">4.8</text>
                <text class="score-label">平均分</text>
              </view>
              <view class="rating-bars">
                <view class="rating-item" v-for="i in 5" :key="i">
                  <text class="rating-label">{{ i }}星</text>
                  <view class="rating-track">
                    <view class="rating-fill" :style="{ width: ratingData[5 - i] + '%' }"></view>
                  </view>
                  <text class="rating-pct">{{ ratingData[5 - i] }}%</text>
                </view>
              </view>
            </view>
          </view>
        </view>

        <!-- Right: Rankings -->
        <view class="column">
          <view class="card">
            <text class="card-title">热门Agent排行</text>
            <view class="rank-list">
              <view class="rank-item" v-for="(item, idx) in agentRanking" :key="item.name">
                <view class="rank-badge" :class="getRankClass(idx)">
                  <text>{{ idx + 1 }}</text>
                </view>
                <view class="rank-info">
                  <text class="rank-name">{{ item.name }}</text>
                  <text class="rank-desc">{{ item.department }}</text>
                </view>
                <text class="rank-val">{{ item.count }}次</text>
              </view>
            </view>
          </view>

          <view class="card">
            <text class="card-title">热门技能排行</text>
            <view class="rank-list">
              <view class="rank-item" v-for="(item, idx) in skillRanking" :key="item.name">
                <view class="rank-badge" :class="getRankClass(idx)">
                  <text>{{ idx + 1 }}</text>
                </view>
                <view class="rank-info">
                  <text class="rank-name">{{ item.name }}</text>
                  <text class="rank-desc">{{ item.category }}</text>
                </view>
                <text class="rank-val">{{ item.count }}次</text>
              </view>
            </view>
          </view>
        </view>
      </view>
    </view>
  </Layout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import Layout from '@/components/Layout.vue'
import { adminService } from '@/services/admin'

const timeRange = ref('7d')
const stats = ref<any>(null)
const loading = ref(true)

const chartData = ref<number[]>([45, 68, 52, 89, 73, 95, 82])
const chartLabels = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']

const agentRanking = ref<{ name: string; department: string; count: number }[]>([])
const skillRanking = ref<{ name: string; category: string; count: number }[]>([])
const ratingData = [45, 32, 15, 6, 2]

const getRankClass = (idx: number) => {
  if (idx === 0) return 'gold'
  if (idx === 1) return 'silver'
  if (idx === 2) return 'bronze'
  return ''
}

const totalCallsFormatted = computed(() => {
  if (!stats.value) return '0'
  return stats.value.totalCalls.toLocaleString()
})

const loadDashboard = async () => {
  loading.value = true
  try {
    const days = timeRange.value === '7d' ? 7 : timeRange.value === '30d' ? 30 : 90
    const [statistics, trends, agents, skills] = await Promise.all([
      adminService.getStatistics(),
      adminService.getCallTrends(days),
      adminService.getAgentRanking(5),
      adminService.getSkillRanking(5)
    ])
    stats.value = statistics
    chartData.value = trends.length > 0 ? trends : Array(days).fill(0)
    agentRanking.value = agents || []
    skillRanking.value = skills || []
  } catch (e) {
    console.error('Failed to load dashboard:', e)
  } finally {
    loading.value = false
  }
}

onMounted(() => { loadDashboard() })
watch(timeRange, () => { loadDashboard() })
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

.time-range {
  display: flex;
  gap: 8px;
}

.range-chip {
  padding: 6px 16px;
  border-radius: 20px;
  background: #fff;
  border: 1px solid #d9d9d9;
  cursor: pointer;

  text { font-size: 13px; color: #666; }

  &.active {
    background: #1890ff;
    border-color: #1890ff;
    text { color: #fff; }
  }
}

/* Stats */
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

/* Content */
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

.card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 20px;
  display: block;
}

/* Charts */
.chart-box {
  height: 200px;
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  padding-top: 20px;
  border-bottom: 1px solid #f0f0f0;
}

.chart-bars {
  display: flex;
  justify-content: space-around;
  width: 100%;
  height: 100%;
  align-items: flex-end;
}

.bar-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  flex: 1;
}

.bar {
  width: 24px;
  background: linear-gradient(to top, #1890ff, #69c0ff);
  border-radius: 4px 4px 0 0;
  transition: height 0.5s;
}

.bar-label {
  font-size: 12px;
  color: #999;
}

/* Satisfaction */
.satisfaction-box {
  display: flex;
  gap: 32px;
}

.score-ring {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  background: linear-gradient(135deg, #1890ff, #096dd9);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.score-value { font-size: 36px; font-weight: 700; }
.score-label { font-size: 12px; opacity: 0.8; }

.rating-bars {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12px;
  justify-content: center;
}

.rating-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.rating-label { width: 30px; font-size: 13px; color: #666; text-align: right; }

.rating-track {
  flex: 1;
  height: 8px;
  background: #f5f5f5;
  border-radius: 4px;
  overflow: hidden;
}

.rating-fill {
  height: 100%;
  background: #1890ff;
  border-radius: 4px;
  transition: width 0.5s;
}

.rating-pct { width: 40px; font-size: 13px; color: #666; }

/* Rankings */
.rank-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.rank-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 8px;
}

.rank-badge {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: #e6e8eb;
  display: flex;
  align-items: center;
  justify-content: center;

  text { font-size: 12px; font-weight: 600; color: #666; }

  &.gold { background: #ffd700; text { color: #fff; } }
  &.silver { background: #c0c0c0; text { color: #fff; } }
  &.bronze { background: #cd7f32; text { color: #fff; } }
}

.rank-info { flex: 1; }
.rank-name { font-size: 14px; font-weight: 500; color: #333; }
.rank-desc { font-size: 12px; color: #999; margin-top: 2px; }
.rank-val { font-size: 14px; font-weight: 600; color: #1890ff; }
</style>
