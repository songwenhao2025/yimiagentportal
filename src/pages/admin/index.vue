<template>
  <Layout>
    <view class="page">
      <view class="page-header">
        <text class="page-title">管理中心</text>
      </view>

      <view class="page-content">
        <view class="admin-grid">
          <view class="admin-card" v-for="item in menuItems" :key="item.title" @click="handleMenuClick(item)">
            <view class="card-icon" :class="item.color">
              <text>{{ item.icon }}</text>
            </view>
            <view class="card-content">
              <text class="card-title">{{ item.title }}</text>
              <text class="card-desc">{{ item.desc }}</text>
            </view>
            <text class="card-arrow">→</text>
          </view>
        </view>

        <view class="admin-sections">
          <view class="section-card">
            <view class="section-header">
              <text class="section-title">待审核任务</text>
              <text class="section-more">查看全部</text>
            </view>
            <view class="audit-list">
              <view class="audit-item" v-for="item in auditItems" :key="item.id">
                <view class="audit-info">
                  <text class="audit-title">{{ item.title }}</text>
                  <text class="audit-meta">{{ item.type }} · {{ item.time }}</text>
                </view>
                <view class="audit-actions">
                  <view class="action-btn approve" @click.stop="approve(item)">
                    <text>通过</text>
                  </view>
                  <view class="action-btn reject" @click.stop="reject(item)">
                    <text>拒绝</text>
                  </view>
                </view>
              </view>
            </view>
          </view>

          <view class="section-card">
            <view class="section-header">
              <text class="section-title">系统概览</text>
            </view>
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

const auditItems = ref([
  { id: '1', title: '车线管理Agent v2.0', type: 'Agent发布', time: '10分钟前' },
  { id: '2', title: '发送短信技能', type: '技能更新', time: '1小时前' },
  { id: '3', title: '时效洞察Agent', type: '配置变更', time: '2小时前' }
])

import { ref } from 'vue'

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
  min-height: 100vh;
  background: #f0f2f5;
}

.page-header {
  background: #fff;
  padding: 20px 32px;
  border-bottom: 1px solid #e8e8e8;
}

.page-title {
  font-size: 24px;
  font-weight: 700;
  color: #1f2937;
}

.page-content {
  padding: 24px 32px;
}

.admin-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.admin-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 14px;
  cursor: pointer;
  transition: all 0.2s;
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  }
}

.card-icon {
  width: 48px;
  height: 48px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  
  &.blue { background: linear-gradient(135deg, #dbeafe 0%, #bfdbfe 100%); }
  &.purple { background: linear-gradient(135deg, #eef2ff 0%, #ddd6fe 100%); }
  &.green { background: linear-gradient(135deg, #d1fae5 0%, #a7f3d0 100%); }
  &.orange { background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%); }
  &.gray { background: linear-gradient(135deg, #f3f4f6 0%, #e5e7eb 100%); }
}

.card-content {
  flex: 1;
}

.card-title {
  font-size: 15px;
  font-weight: 600;
  color: #1f2937;
  display: block;
}

.card-desc {
  font-size: 12px;
  color: #9ca3af;
  margin-top: 2px;
  display: block;
}

.card-arrow {
  font-size: 16px;
  color: #d1d5db;
}

.admin-sections {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
}

.section-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
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
  background: #f9fafb;
  border-radius: 8px;
}

.audit-info {
  display: flex;
  flex-direction: column;
}

.audit-title {
  font-size: 14px;
  font-weight: 500;
  color: #1f2937;
}

.audit-meta {
  font-size: 12px;
  color: #9ca3af;
  margin-top: 2px;
}

.audit-actions {
  display: flex;
  gap: 8px;
}

.action-btn {
  padding: 6px 14px;
  border-radius: 6px;
  font-size: 13px;
  cursor: pointer;
  
  &.approve {
    background: #10b981;
    color: #fff;
  }
  
  &.reject {
    background: #f3f4f6;
    color: #6b7280;
  }
}

.overview-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.overview-item {
  text-align: center;
  padding: 16px;
  background: #f9fafb;
  border-radius: 8px;
}

.overview-value {
  font-size: 24px;
  font-weight: 700;
  color: #4f46e5;
  display: block;
}

.overview-label {
  font-size: 13px;
  color: #9ca3af;
  margin-top: 4px;
  display: block;
}
</style>
