<script setup lang="ts">
import { onLaunch, onShow, onHide } from '@dcloudio/uni-app'

onLaunch(() => {
  console.log('App Launch')
})

onShow(() => {
  console.log('App Show')
})

onHide(() => {
  console.log('App Hide')
})
</script>

<style lang="scss">
@import './uni.scss';

page {
  background-color: #f5f7fa;
  min-height: 100vh;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  color: #333333;
}

view, text {
  box-sizing: border-box;
}

image {
  display: block;
}

button {
  margin: 0;
  padding: 0;
  background: none;
  border: none;
  
  &::after {
    border: none;
  }
}

input, textarea, select {
  background: #ffffff;
  border: 1px solid #e8ecf1;
  color: #333333;
  border-radius: 8rpx;
  padding: 20rpx 24rpx;
  font-size: 28rpx;
  pointer-events: auto;
  cursor: text;
  user-select: text;
  -webkit-user-select: text;
  -webkit-appearance: none;
  
  &:focus {
    outline: none;
    border-color: #1890ff;
    background: #ffffff;
  }
  
  &::placeholder {
    color: #999999;
  }
}

scroll-view {
  -webkit-overflow-scrolling: touch;
}

.page-container {
  padding: 32rpx;
  min-height: 100vh;
}

.card {
  background: #ffffff;
  border-radius: 16rpx;
  padding: 32rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.05);
  margin-bottom: 24rpx;
}

.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 20rpx 40rpx;
  border-radius: 8rpx;
  font-size: 28rpx;
  font-weight: 500;
  transition: all 0.3s ease;
  cursor: pointer;
  
  &.primary {
    background: #1890ff;
    color: #ffffff;
    
    &:hover {
      background: #40a9ff;
    }
  }
  
  &.secondary {
    background: #ffffff;
    color: #666666;
    border: 1px solid #d9d9d9;
    
    &:hover {
      border-color: #1890ff;
      color: #1890ff;
    }
  }
  
  &.danger {
    background: #ff4466;
    color: #ffffff;
  }
}

.divider {
  height: 1px;
  background: #f0f0f0;
  margin: 32rpx 0;
}
</style>
