<template>
  <view class="layout">
    <view class="sidebar">
      <view class="sidebar-header">
        <view class="logo">
          <image class="logo-icon" src="@/assets/logo-mascot.svg" mode="aspectFit" />
          <text class="logo-text">滴答灵机</text>
        </view>
      </view>

      <view class="nav-menu">
        <view
          class="nav-item"
          v-for="item in menuItems"
          :key="item.path"
          :class="{ active: currentPath === item.path }"
          @click="navigateTo(item.path)"
        >
          <text class="nav-icon">{{ item.icon }}</text>
          <text class="nav-text">{{ item.name }}</text>
        </view>
      </view>

      <view class="sidebar-footer">
        <view class="user-info">
          <view class="user-avatar">👤</view>
          <view class="user-detail">
            <text class="user-name">张主管</text>
            <text class="user-role">管理员</text>
          </view>
        </view>
      </view>
    </view>

    <view class="main-content">
      <slot></slot>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'

const currentPath = ref('/pages/index/index')

const menuItems = [
  { path: '/pages/index/index', name: '工作台', icon: '🏠' },
  { path: '/pages/market/index', name: 'Agent市场', icon: '🛒' },
  { path: '/pages/builder/index', name: 'Agent构建', icon: '🔧' },
  { path: '/pages/skills/index', name: '技能管理', icon: '⚙️' },
  { path: '/pages/workflow/index', name: '流程编排', icon: '🔗' },
  { path: '/pages/knowledge/index', name: '知识库', icon: '📚' },
  { path: '/pages/monitor/index', name: '监控分析', icon: '📊' },
  { path: '/pages/admin/index', name: '管理中心', icon: '⚡' }
]

const navigateTo = (path: string) => {
  currentPath.value = path
  uni.redirectTo({ url: path })
}

onMounted(() => {
  const pages = getCurrentPages()
  if (pages.length > 0) {
    const currentPage = pages[pages.length - 1]
    const route = '/' + currentPage.route
    currentPath.value = route
  }
})
</script>

<style lang="scss">
.layout {
  display: flex;
  min-height: 100vh;
  background: #f5f7fa;
}

.sidebar {
  width: 240px;
  background: #ffffff;
  display: flex;
  flex-direction: column;
  position: fixed;
  left: 0;
  top: 0;
  bottom: 0;
  z-index: 100;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.05);
}

.sidebar-header {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: 1px solid #f0f0f0;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
}

.logo-icon {
  width: 36px;
  height: 36px;
}

.logo-text {
  font-size: 18px;
  font-weight: 700;
  color: #1a1a1a;
  letter-spacing: 1px;
}

.nav-menu {
  flex: 1;
  padding: 12px 0;
  overflow-y: auto;
}

.nav-item {
  display: flex;
  align-items: center;
  height: 48px;
  padding: 0 24px;
  margin: 4px 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;

  &:hover {
    color: #1890ff;
    background-color: #e6f7ff;
  }

  &.active {
    color: #fff;
    background: linear-gradient(90deg, #1890ff, #096dd9);
    box-shadow: 0 2px 6px rgba(24, 144, 255, 0.3);

    .nav-icon, .nav-text {
      color: #fff;
    }
  }
}

.nav-icon {
  font-size: 18px;
  margin-right: 12px;
  width: 20px;
  text-align: center;
  color: #666;
}

.nav-text {
  font-size: 14px;
  color: #666;
  font-weight: 500;
}

.sidebar-footer {
  padding: 16px 24px;
  border-top: 1px solid #f0f0f0;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #f0f2f5;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
}

.user-detail {
  display: flex;
  flex-direction: column;
}

.user-name {
  font-size: 13px;
  font-weight: 600;
  color: #333;
}

.user-role {
  font-size: 11px;
  color: #999;
}

.main-content {
  flex: 1;
  margin-left: 240px;
  min-height: 100vh;
  padding: 24px;
}

.card {
  background: #ffffff;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
  margin-bottom: 24px;
}

.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 8px 16px;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
  border: none;

  &.primary {
    background: #1890ff;
    color: #fff;

    &:hover {
      background: #40a9ff;
    }
  }

  &.secondary {
    background: #fff;
    border: 1px solid #d9d9d9;
    color: #666;

    &:hover {
      border-color: #1890ff;
      color: #1890ff;
    }
  }
}
</style>
