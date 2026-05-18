<template>
  <view class="layout">
    <view class="sidebar" :class="{ collapsed: sidebarCollapsed }">
      <view class="sidebar-header">
        <view class="logo">
          <text class="logo-icon">🤖</text>
          <text class="logo-text" v-if="!sidebarCollapsed">壹米AI</text>
        </view>
        <view class="collapse-btn" @click="sidebarCollapsed = !sidebarCollapsed">
          <text>{{ sidebarCollapsed ? '→' : '←' }}</text>
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
          <text class="nav-text" v-if="!sidebarCollapsed">{{ item.name }}</text>
        </view>
      </view>
      
      <view class="sidebar-footer">
        <view class="user-info">
          <view class="user-avatar">👤</view>
          <view class="user-detail" v-if="!sidebarCollapsed">
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

const sidebarCollapsed = ref(false)
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
  background: #f0f2f5;
}

.sidebar {
  width: 240px;
  background: #1a1a2e;
  display: flex;
  flex-direction: column;
  transition: width 0.3s;
  position: fixed;
  left: 0;
  top: 0;
  bottom: 0;
  z-index: 100;
  
  &.collapsed {
    width: 72px;
    
    .sidebar-header {
      padding: 16px 12px;
    }
    
    .logo {
      justify-content: center;
    }
    
    .nav-item {
      padding: 12px;
      justify-content: center;
    }
    
    .user-info {
      justify-content: center;
    }
  }
}

.sidebar-header {
  padding: 24px 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo-icon {
  font-size: 32px;
}

.logo-text {
  font-size: 20px;
  font-weight: 700;
  color: #fff;
}

.collapse-btn {
  width: 28px;
  height: 28px;
  border-radius: 6px;
  background: rgba(255, 255, 255, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  
  text {
    font-size: 14px;
    color: rgba(255, 255, 255, 0.7);
  }
  
  &:hover {
    background: rgba(255, 255, 255, 0.2);
  }
}

.nav-menu {
  flex: 1;
  padding: 16px 12px;
  overflow-y: auto;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  border-radius: 8px;
  margin-bottom: 4px;
  cursor: pointer;
  transition: all 0.2s;
  
  &:hover {
    background: rgba(255, 255, 255, 0.1);
  }
  
  &.active {
    background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%);
    
    .nav-icon, .nav-text {
      color: #fff;
    }
  }
}

.nav-icon {
  font-size: 20px;
}

.nav-text {
  font-size: 15px;
  color: rgba(255, 255, 255, 0.7);
}

.sidebar-footer {
  padding: 16px 20px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
}

.user-detail {
  display: flex;
  flex-direction: column;
}

.user-name {
  font-size: 14px;
  font-weight: 600;
  color: #fff;
}

.user-role {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.5);
}

.main-content {
  flex: 1;
  margin-left: 240px;
  min-height: 100vh;
  transition: margin-left 0.3s;
}

.sidebar.collapsed + .main-content {
  margin-left: 72px;
}
</style>
