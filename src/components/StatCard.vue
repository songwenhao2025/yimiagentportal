<template>
  <view class="stat-card" :class="colorClass">
    <view class="stat-icon">
      <text>{{ icon }}</text>
    </view>
    <view class="stat-content">
      <text class="stat-value">{{ value }}</text>
      <text class="stat-label">{{ label }}</text>
    </view>
    <view class="stat-trend" v-if="trend !== undefined">
      <text :class="trend > 0 ? 'up' : 'down'">
        {{ trend > 0 ? '↑' : '↓' }} {{ Math.abs(trend) }}%
      </text>
    </view>
  </view>
</template>

<script setup lang="ts">
defineProps<{
  icon: string
  value: string | number
  label: string
  trend?: number
  color?: 'primary' | 'success' | 'warning' | 'info'
}>()

const colorClass = (props: { color?: string }) => props.color || 'primary'
</script>

<style lang="scss">
.stat-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  display: flex;
  align-items: center;
  gap: 20rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.06);
  
  &.primary {
    .stat-icon {
      background: linear-gradient(135deg, #eef2ff 0%, #ddd6fe 100%);
    }
    .stat-value { color: #4f46e5; }
  }
  
  &.success {
    .stat-icon {
      background: linear-gradient(135deg, #d1fae5 0%, #a7f3d0 100%);
    }
    .stat-value { color: #10b981; }
  }
  
  &.warning {
    .stat-icon {
      background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
    }
    .stat-value { color: #f59e0b; }
  }
  
  &.info {
    .stat-icon {
      background: linear-gradient(135deg, #dbeafe 0%, #bfdbfe 100%);
    }
    .stat-value { color: #3b82f6; }
  }
}

.stat-icon {
  width: 80rpx;
  height: 80rpx;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  
  text {
    font-size: 36rpx;
  }
}

.stat-content {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 40rpx;
  font-weight: 700;
}

.stat-label {
  font-size: 24rpx;
  color: #9ca3af;
  margin-top: 4rpx;
}

.stat-trend {
  text {
    font-size: 22rpx;
    font-weight: 500;
    
    &.up { color: #10b981; }
    &.down { color: #ef4444; }
  }
}
</style>
