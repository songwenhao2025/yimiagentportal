<template>
  <view class="agent-card" :class="{ compact }" @click="$emit('click')">
    <view class="card-header">
      <view class="agent-avatar">
        <text>{{ getDeptIcon(agent.department) }}</text>
      </view>
      <view class="agent-info">
        <text class="agent-name">{{ agent.name }}</text>
        <text class="agent-dept">{{ getDeptName(agent.department) }}</text>
      </view>
      <view class="agent-tags">
        <text class="tag" v-for="tag in agent.tags.slice(0, 2)" :key="tag">{{ tag }}</text>
      </view>
    </view>
    
    <text class="agent-desc">{{ agent.description }}</text>
    
    <view class="card-footer">
      <view class="stats">
        <view class="stat-item">
          <text class="stat-label">成功率</text>
          <text class="stat-value success">{{ agent.successRate }}%</text>
        </view>
        <view class="stat-item">
          <text class="stat-label">日调用</text>
          <text class="stat-value">{{ agent.dailyCalls }}</text>
        </view>
      </view>
      <view class="actions">
        <view class="action-btn use" @click.stop="$emit('use', agent)">
          <text>使用</text>
        </view>
        <view class="action-btn favorite" :class="{ active: agent.isFavorite }" @click.stop="toggleFavorite">
          <text>{{ agent.isFavorite ? '★' : '☆' }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import type { Agent } from '@/data/agents'

defineProps<{
  agent: Agent
  compact?: boolean
}>()

defineEmits<{
  click: []
  use: [agent: Agent]
}>()

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

const toggleFavorite = () => {
  // TODO: implement favorite toggle
}
</script>

<style lang="scss">
.agent-card {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid #f3f4f6;
  
  &:hover {
    border-color: #4f46e5;
    box-shadow: 0 4px 12px rgba(79, 70, 229, 0.1);
  }
  
  &.compact {
    padding: 12px;
    
    .card-header {
      margin-bottom: 8px;
    }
    
    .agent-avatar {
      width: 36px;
      height: 36px;
      font-size: 16px;
    }
    
    .agent-name {
      font-size: 14px;
    }
    
    .agent-desc {
      font-size: 12px;
      -webkit-line-clamp: 2;
    }
  }
}

.card-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.agent-avatar {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
}

.agent-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.agent-name {
  font-size: 15px;
  font-weight: 600;
  color: #1f2937;
}

.agent-dept {
  font-size: 12px;
  color: #9ca3af;
  margin-top: 2px;
}

.agent-tags {
  display: flex;
  gap: 6px;
}

.tag {
  padding: 4px 8px;
  background: #eef2ff;
  border-radius: 4px;
  font-size: 11px;
  color: #4f46e5;
}

.agent-desc {
  font-size: 13px;
  color: #6b7280;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
  margin-bottom: 12px;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stats {
  display: flex;
  gap: 16px;
}

.stat-item {
  display: flex;
  flex-direction: column;
}

.stat-label {
  font-size: 11px;
  color: #9ca3af;
}

.stat-value {
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
  
  &.success {
    color: #10b981;
  }
}

.actions {
  display: flex;
  gap: 8px;
}

.action-btn {
  padding: 6px 14px;
  border-radius: 6px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
  
  &.use {
    background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%);
    color: #fff;
    
    &:hover {
      opacity: 0.9;
    }
  }
  
  &.favorite {
    background: #f3f4f6;
    color: #9ca3af;
    
    &:hover {
      background: #e5e7eb;
    }
    
    &.active {
      color: #f59e0b;
    }
  }
}
</style>
