<template>
  <view class="search-bar">
    <view class="search-input-wrap">
      <text class="search-icon">🔍</text>
      <input 
        class="search-input" 
        type="text" 
        :placeholder="placeholder"
        :value="modelValue"
        @input="handleInput"
        @confirm="handleConfirm"
      />
      <view class="clear-btn" v-if="modelValue" @click="handleClear">
        <text>✕</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
defineProps<{
  modelValue: string
  placeholder?: string
}>()

const emit = defineEmits<{
  'update:modelValue': [value: string]
  search: [value: string]
}>()

const handleInput = (e: any) => {
  emit('update:modelValue', e.detail.value)
}

const handleConfirm = () => {
  // handled by parent
}

const handleClear = () => {
  emit('update:modelValue', '')
}
</script>

<style lang="scss">
.search-bar {
  padding: 0 24rpx;
}

.search-input-wrap {
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: 40rpx;
  padding: 16rpx 24rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);
}

.search-icon {
  font-size: 32rpx;
  margin-right: 16rpx;
}

.search-input {
  flex: 1;
  font-size: 28rpx;
  color: #1f2937;
}

.clear-btn {
  width: 40rpx;
  height: 40rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #e5e7eb;
  border-radius: 50%;
  
  text {
    font-size: 24rpx;
    color: #6b7280;
  }
}
</style>
