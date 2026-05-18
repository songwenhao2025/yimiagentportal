<template>
  <Layout>
    <view class="page">
      <view class="page-header">
        <view class="header-content">
          <text class="page-title">监控分析</text>
          <view class="time-range">
            <view 
              class="range-item" 
              :class="{ active: timeRange === '7d' }"
              @click="timeRange = '7d'"
            >
              <text>7天</text>
            </view>
            <view 
              class="range-item" 
              :class="{ active: timeRange === '30d' }"
              @click="timeRange = '30d'"
            >
              <text>30天</text>
            </view>
            <view 
              class="range-item" 
              :class="{ active: timeRange === '90d' }"
              @click="timeRange = '90d'"
            >
              <text>90天</text>
            </view>
          </view>
        </view>
      </view>

      <view class="page-content">
        <view class="stats-grid">
          <StatCard icon="📊" value="12,345" label="今日调用" :trend="12" color="primary" />
          <StatCard icon="✅" value="98.5%" label="成功率" :trend="2" color="success" />
          <StatCard icon="⏱️" value="2.3s" label="平均延迟" :trend="-5" color="info" />
          <StatCard icon="💰" value="¥1,234" label="本月消耗" :trend="8" color="warning" />
        </view>

        <view class="main-grid">
          <view class="left-column">
            <view class="section-card">
              <view class="section-header">
                <text class="section-title">调用趋势</text>
              </view>
              <view class="chart-container">
                <view class="chart-bar">
                  <view class="bar" v-for="(val, idx) in chartData" :key="idx" :style="{ height: val + '%' }">
                    <text class="bar-value">{{ val }}</text>
                  </view>
                </view>
                <view class="chart-labels">
                  <text v-for="(label, idx) in chartLabels" :key="idx">{{ label }}</text>
                </view>
              </view>
            </view>

            <view class="section-card">
              <view class="section-header">
                <text class="section-title">用户满意度</text>
              </view>
              <view class="satisfaction-content">
                <view class="score-circle">
                  <text class="score-value">4.8</text>
                  <text class="score-label">平均分</text>
                </view>
                <view class="rating-dist">
                  <view class="dist-item" v-for="i in 5" :key="i">
                    <text class="dist-label">{{ i }}星</text>
                    <view class="dist-bar">
                      <view class="dist-fill" :style="{ width: ratingData[5 - i] + '%' }"></view>
                    </view>
                    <text class="dist-value">{{ ratingData[5 - i] }}%</text>
                  </view>
                </view>
              </view>
            </view>
          </view>

          <view class="right-column">
            <view class="section-card">
              <view class="section-header">
                <text class="section-title">热门Agent排行</text>
              </view>
              <view class="ranking-list">
                <view class="ranking-item" v-for="(item, idx) in agentRanking" :key="item.name">
                  <view class="rank-badge" :class="getRankClass(idx)">
                    <text>{{ idx + 1 }}</text>
                  </view>
                  <view class="rank-info">
                    <text class="rank-name">{{ item.name }}</text>
                    <text class="rank-desc">{{ item.department }}</text>
                  </view>
                  <view class="rank-value">
                    <text>{{ item.count }}</text>
                    <text class="rank-unit">次</text>
                  </view>
                </view>
              </view>
            </view>

            <view class="section-card">
              <view class="section-header">
                <text class="section-title">热门技能排行</text>
              </view>
              <view class="ranking-list">
                <view class="ranking-item" v-for="(item, idx) in skillRanking" :key="item.name">
                  <view class="rank-badge" :class="getRankClass(idx)">
                    <text>{{ idx + 1 }}</text>
                  </view>
                  <view class="rank-info">
                    <text class="rank-name">{{ item.name }}</text>
                    <text class="rank-desc">{{ item.category }}</text>
                  </view>
                  <view class="rank-value">
                    <text>{{ item.count }}</text>
                    <text class="rank-unit">次</text>
                  </view>
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
import { ref } from 'vue'
import Layout from '@/components/Layout.vue'
import StatCard from '@/components/StatCard.vue'

const timeRange = ref('7d')

const chartData = [45, 68, 52, 89, 73, 95, 82]
const chartLabels = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']

const agentRanking = [
  { name: '智能客服Agent', department: '客服部', count: 3240 },
  { name: '路由规划Agent', department: '运营部', count: 2890 },
  { name: '时效洞察Agent', department: '质控部', count: 2340 },
  { name: '报表审核Agent', department: '财务部', count: 1890 },
  { name: '车线管理Agent', department: '运营部', count: 1560 }
]

const skillRanking = [
  { name: '发送短信', category: '通知服务', count: 23400 },
  { name: '创建工单', category: '工单管理', count: 15600 },
  { name: '查询路由', category: '物流查询', count: 12580 },
  { name: '时效计算', category: '物流计算', count: 8920 },
  { name: '查询库存', category: '仓储管理', count: 6750 }
]

const ratingData = [45, 32, 15, 6, 2]

const getRankClass = (idx: number) => {
  if (idx === 0) return 'gold'
  if (idx === 1) return 'silver'
  if (idx === 2) return 'bronze'
  return ''
}
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

.time-range {
  display: flex;
  gap: 12px;
}

.range-item {
  padding: 10px 20px;
  background: #f3f4f6;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  
  text {
    font-size: 14px;
    color: #6b7280;
  }
  
  &:hover {
    background: #e5e7eb;
  }
  
  &.active {
    background: #eef2ff;
    
    text {
      color: #4f46e5;
      font-weight: 600;
    }
  }
}

.page-content {
  padding: 24px 32px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.main-grid {
  display: grid;
  grid-template-columns: 1fr 400px;
  gap: 24px;
}

.left-column {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.right-column {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.section-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
}

.section-header {
  margin-bottom: 20px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
}

.chart-container {
  margin-top: 20px;
}

.chart-bar {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  height: 200px;
  padding-top: 40px;
}

.bar {
  width: 12%;
  background: linear-gradient(180deg, #4f46e5 0%, #7c3aed 100%);
  border-radius: 8px 8px 0 0;
  position: relative;
  transition: height 0.3s;
  
  &:hover {
    background: linear-gradient(180deg, #3730a3 0%, #6d28d9 100%);
  }
}

.bar-value {
  position: absolute;
  top: -36px;
  left: 50%;
  transform: translateX(-50%);
  font-size: 12px;
  color: #6b7280;
}

.chart-labels {
  display: flex;
  justify-content: space-between;
  margin-top: 16px;
  
  text {
    font-size: 13px;
    color: #9ca3af;
  }
}

.satisfaction-content {
  display: flex;
  gap: 32px;
  margin-top: 20px;
}

.score-circle {
  width: 140px;
  height: 140px;
  border-radius: 50%;
  background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.score-value {
  font-size: 40px;
  font-weight: 700;
  color: #fff;
}

.score-label {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.8);
}

.rating-dist {
  flex: 1;
}

.dist-item {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  
  &:last-child {
    margin-bottom: 0;
  }
}

.dist-label {
  width: 50px;
  font-size: 13px;
  color: #6b7280;
}

.dist-bar {
  flex: 1;
  height: 12px;
  background: #f3f4f6;
  border-radius: 6px;
  overflow: hidden;
}

.dist-fill {
  height: 100%;
  background: linear-gradient(90deg, #4f46e5 0%, #7c3aed 100%);
  border-radius: 6px;
  transition: width 0.5s;
}

.dist-value {
  width: 60px;
  font-size: 13px;
  color: #6b7280;
  text-align: right;
}

.ranking-list {
  margin-top: 16px;
}

.ranking-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 0;
  border-bottom: 1px solid #f3f4f6;
  
  &:last-child {
    border-bottom: none;
  }
}

.rank-badge {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #f3f4f6;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  
  text {
    font-size: 14px;
    font-weight: 600;
    color: #6b7280;
  }
  
  &.gold {
    background: linear-gradient(135deg, #fbbf24 0%, #f59e0b 100%);
    text { color: #fff; }
  }
  
  &.silver {
    background: linear-gradient(135deg, #9ca3af 0%, #6b7280 100%);
    text { color: #fff; }
  }
  
  &.bronze {
    background: linear-gradient(135deg, #d97706 0%, #b45309 100%);
    text { color: #fff; }
  }
}

.rank-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.rank-name {
  font-size: 14px;
  font-weight: 500;
  color: #1f2937;
}

.rank-desc {
  font-size: 12px;
  color: #9ca3af;
  margin-top: 4px;
}

.rank-value {
  display: flex;
  align-items: baseline;
  
  text {
    font-size: 16px;
    font-weight: 600;
    color: #4f46e5;
  }
}

.rank-unit {
  font-size: 12px;
  color: #9ca3af;
  margin-left: 4px;
}
</style>