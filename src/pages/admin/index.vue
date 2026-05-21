<template>
  <Layout>
    <view class="page">
      <view class="page-header">
        <text class="page-title">管理中心</text>
      </view>

      <view class="content-grid">
        <view class="column">
          <!-- Menu Grid -->
          <view class="card">
            <text class="card-title">系统功能</text>
            <view class="menu-grid">
              <view class="menu-card" v-for="item in menuItems" :key="item.title" @click="handleMenuClick(item)">
                <view class="menu-icon" :class="item.color">
                  <text>{{ item.icon }}</text>
                </view>
                <view class="menu-content">
                  <text class="menu-title">{{ item.title }}</text>
                  <text class="menu-desc">{{ item.desc }}</text>
                </view>
              </view>
            </view>
          </view>

          <!-- Audit List -->
          <view class="card">
            <view class="card-header">
              <text class="card-title">待审核任务</text>
              <text class="card-more">查看全部 →</text>
            </view>
            <view class="audit-list">
              <view class="audit-item" v-for="item in auditItems" :key="item.id">
                <view class="audit-info">
                  <text class="audit-title">{{ item.title }}</text>
                  <text class="audit-meta">{{ item.type }} · {{ item.time }}</text>
                </view>
                <view class="audit-actions">
                  <view class="btn approve" @click="approve(item)">
                    <text>通过</text>
                  </view>
                  <view class="btn reject" @click="reject(item)">
                    <text>拒绝</text>
                  </view>
                </view>
              </view>
            </view>
          </view>
        </view>

        <view class="column">
          <!-- Overview -->
          <view class="card">
            <text class="card-title">系统概览</text>
            <view class="overview-grid">
              <view class="overview-item">
                <text class="overview-value">156</text>
                <text class="overview-label">注册用户</text>
              </view>
              <view class="overview-item">
                <text class="overview-value">28</text>
                <text class="overview-label">Agent总数</text>
              </view>
              <view class="overview-item">
                <text class="overview-value">45</text>
                <text class="overview-label">技能总数</text>
              </view>
              <view class="overview-item">
                <text class="overview-value">¥12,580</text>
                <text class="overview-label">本月消耗</text>
              </view>
            </view>
          </view>
        </view>
      </view>
    </view>
  </Layout>
</template>

<script setup lang="ts">
import Layout from '@/components/Layout.vue'

const menuItems = [
  { icon: '👥', title: '用户管理', desc: '管理组织架构和账户', color: 'blue', path: '' },
  { icon: '🔐', title: '权限控制', desc: '配置数据源和接口权限', color: 'purple', path: '' },
  { icon: '📋', title: '审计日志', desc: '查看调用记录和操作日志', color: 'green', path: '' },
  { icon: '💰', title: '成本中心', desc: '部门消耗统计和预算', color: 'orange', path: '' },
  { icon: '⚙️', title: '系统设置', desc: '基础配置和参数调整', color: 'gray', path: '' }
]

const auditItems = [
  { id: '1', title: '车线管理Agent v2.0', type: 'Agent发布', time: '10分钟前' },
  { id: '2', title: '发送短信技能', type: '技能更新', time: '1小时前' },
  { id: '3', title: '时效洞察Agent', type: '配置变更', time: '2小时前' }
]

const handleMenuClick = (item: any) => {
  uni.showToast({ title: item.title, icon: 'none' })
}

const approve = (item: any) => {
  uni.showToast({ title: `已通过: ${item.title}`, icon: 'success' })
}

const reject = (item: any) => {
  uni.showToast({ title: `已拒绝: ${item.title}`, icon: 'none' })
}
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

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.card-more {
  font-size: 13px;
  color: #1890ff;
  cursor: pointer;
}

/* Menu Grid */
.menu-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
}

.menu-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px;
  background: #f9f9f9;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;

  &:hover { background: #e6f7ff; transform: translateY(-2px); }
}

.menu-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;

  &.blue { background: #e6f7ff; }
  &.purple { background: #f9f0ff; }
  &.green { background: #f6ffed; }
  &.orange { background: #fff7e6; }
  &.gray { background: #f5f5f5; }
}

.menu-content {
  flex: 1;
}

.menu-title {
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.menu-desc {
  font-size: 12px;
  color: #888;
  margin-top: 4px;
  display: block;
}

/* Audit List */
.audit-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.audit-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  background: #f9f9f9;
  border-radius: 8px;
}

.audit-info {
  display: flex;
  flex-direction: column;
}

.audit-title {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.audit-meta {
  font-size: 12px;
  color: #999;
  margin-top: 2px;
}

.audit-actions {
  display: flex;
  gap: 8px;
}

.btn {
  padding: 6px 14px;
  border-radius: 6px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;

  &.approve { background: #52c41a; color: #fff; }
  &.reject { background: #f5f5f5; color: #666; }
}

/* Overview */
.overview-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.overview-item {
  text-align: center;
  padding: 16px;
  background: #f9f9f9;
  border-radius: 8px;
}

.overview-value {
  font-size: 24px;
  font-weight: 700;
  color: #1890ff;
  display: block;
}

.overview-label {
  font-size: 13px;
  color: #888;
  margin-top: 4px;
  display: block;
}
</style>
