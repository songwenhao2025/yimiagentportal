<template>
  <view class="navbar">
    <view class="navbar-status-bar" :style="{ height: statusBarHeight + 'px' }"></view>
    <view class="navbar-content">
      <view class="navbar-left" @click="handleBack" v-if="showBack">
        <text class="back-icon">←</text>
      </view>
      <view class="navbar-title">
        <text>{{ title }}</text>
      </view>
      <view class="navbar-right">
        <slot name="right"></slot>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'

defineProps<{
  title: string
  showBack?: boolean
}>()

const statusBarHeight = ref(20)

uni.getSystemInfo({
  success: (res) => {
    statusBarHeight.value = res.statusBarHeight || 20
  }
})

const handleBack = () => {
  uni.navigateBack({
    fail: () => {
      uni.switchTab({ url: '/pages/index/index' })
    }
  })
}
</script>

<style lang="scss">
.navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 999;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
}

.navbar-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 88rpx;
  padding: 0 24rpx;
}

.navbar-left {
  width: 80rpx;
  display: flex;
  align-items: center;
}

.back-icon {
  font-size: 40rpx;
  color: #fff;
}

.navbar-title {
  flex: 1;
  text-align: center;
  
  text {
    font-size: 34rpx;
    font-weight: 600;
    color: #fff;
  }
}

.navbar-right {
  width: 80rpx;
  display: flex;
  justify-content: flex-end;
}
</style>
