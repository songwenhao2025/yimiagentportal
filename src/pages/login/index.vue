<template>
  <view class="login-page">
    <view class="login-card">
      <view class="login-header">
        <text class="logo">壹米AI</text>
        <text class="subtitle">Agent 门户系统</text>
      </view>

      <view class="login-form">
        <view class="form-item">
          <text class="label">邮箱</text>
          <input
            class="input"
            type="text"
            placeholder="请输入邮箱"
            v-model="form.email"
            @confirm="handleLogin"
          />
        </view>

        <view class="form-item">
          <text class="label">密码</text>
          <input
            class="input"
            type="password"
            placeholder="请输入密码"
            v-model="form.password"
            @confirm="handleLogin"
          />
        </view>

        <view class="btn-login" @click="handleLogin">
          <text class="btn-text">{{ loading ? '登录中...' : '登 录' }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { userService } from '@/services/user'

const form = reactive({ email: '', password: '' })
const loading = ref(false)

const handleLogin = async () => {
  if (!form.email || !form.password) {
    uni.showToast({ title: '请输入邮箱和密码', icon: 'none' })
    return
  }
  loading.value = true
  try {
    const res = await userService.login(form)
    uni.setStorageSync('token', res.token)
    uni.showToast({ title: '登录成功', icon: 'success' })
    setTimeout(() => {
      uni.redirectTo({ url: '/pages/index/index' })
    }, 500)
  } catch (e: any) {
    const msg = e.message || '登录失败，请检查邮箱和密码'
    uni.showToast({ title: msg, icon: 'none' })
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.login-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.login-card {
  background: #fff;
  border-radius: 16px;
  padding: 40px 32px;
  width: 100%;
  max-width: 400px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
}

.login-header {
  text-align: center;
  margin-bottom: 32px;
}

.logo {
  font-size: 28px;
  font-weight: 700;
  color: #1f2937;
  display: block;
}

.subtitle {
  font-size: 14px;
  color: #9ca3af;
  margin-top: 4px;
  display: block;
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.label {
  font-size: 14px;
  font-weight: 500;
  color: #374151;
}

.input {
  height: 44px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 0 12px;
  font-size: 14px;
  background: #f9fafb;
  color: #1f2937;

  &:focus {
    border-color: #667eea;
    background: #fff;
  }

  &::placeholder {
    color: #9ca3af;
  }
}

.btn-login {
  margin-top: 8px;
  height: 48px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: opacity 0.2s;

  &:active {
    opacity: 0.85;
  }
}

.btn-text {
  font-size: 16px;
  font-weight: 600;
  color: #fff;
  letter-spacing: 4px;
}
</style>
