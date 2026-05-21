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
  background: rgba(30, 41, 59, 0.6);
  backdrop-filter: blur(20px);
  border-radius: 16px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid #2d3a4f;
  
  &:hover {
    border-color: rgba(0, 102, 255, 0.5);
    box-shadow: 0 8px 30px rgba(0, 102, 255, 0.15);
    transform: translateY(-4px);
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
  gap: 14px;
  margin-bottom: 14px;
}

.agent-avatar {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: linear-gradient(135deg, #0066ff 0%, #00aaff 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  box-shadow: 0 4px 15px rgba(0, 102, 255, 0.3);
}

.agent-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.agent-name {
  font-size: 16px;
  font-weight: 600;
  color: #ffffff;
}

.agent-dept {
  font-size: 12px;
  color: #607080;
  margin-top: 2px;
}

.agent-tags {
  display: flex;
  gap: 6px;
}

.tag {
  padding: 4px 10px;
  background: rgba(0, 102, 255, 0.15);
  border: 1px solid rgba(0, 102, 255, 0.3);
  border-radius: 20px;
  font-size: 11px;
  color: #00aaff;
}

.agent-desc {
  font-size: 13px;
  color: #a0b0c0;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
  margin-bottom: 14px;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 14px;
  border-top: 1px solid #2d3a4f;
}

.stats {
  display: flex;
  gap: 20px;
}

.stat-item {
  display: flex;
  flex-direction: column;
}

.stat-label {
  font-size: 11px;
  color: #607080;
}

.stat-value {
  font-size: 14px;
  font-weight: 600;
  color: #ffffff;
  
  &.success {
    color: #00cc88;
  }
}

.actions {
  display: flex;
  gap: 10px;
}

.action-btn {
  padding: 8px 16px;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  
  &.use {
    background: linear-gradient(135deg, #0066ff 0%, #00aaff 100%);
    color: #fff;
    box-shadow: 0 4px 15px rgba(0, 102, 255, 0.4);
    
    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 6px 20px rgba(0, 102, 255, 0.5);
    }
  }
  
  &.favorite {
    background: rgba(45, 58, 79, 0.8);
    color: #607080;
    border: 1px solid #2d3a4f;
    
    &:hover {
      background: rgba(0, 102, 255, 0.1);
      border-color: rgba(0, 102, 255, 0.3);
      color: #a0b0c0;
    }
    
    &.active {
      color: #ffaa00;
      border-color: rgba(255, 170, 0, 0.3);
      background: rgba(255, 170, 0, 0.1);
    }
  }
}
</style>
