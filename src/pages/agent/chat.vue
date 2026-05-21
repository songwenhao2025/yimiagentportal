<template>
  <Layout>
    <view class="page">
      <view class="page-header">
        <view class="header-left">
          <text class="page-title">{{ agent.name }}</text>
          <view class="status-badge" :class="agent.status">
            <text>{{ getStatusText(agent.status) }}</text>
          </view>
        </view>
        <view class="more-btn" @click="showMenu">
          <text>⋮</text>
        </view>
      </view>

      <view class="chat-container">
        <scroll-view
          scroll-y
          class="chat-scroll"
          :scroll-into-view="scrollToId"
          scroll-with-animation
          :scroll-top="scrollTop"
          enable-flex
        >
          <view class="welcome-card">
            <view class="welcome-icon">🤖</view>
            <text class="welcome-title">欢迎使用{{ agent.name }}</text>
            <text class="welcome-desc">{{ agent.description }}</text>
            <view class="quick-actions">
              <view class="quick-btn" v-for="action in quickActions" :key="action" @click="sendQuick(action)">
                <text>{{ action }}</text>
              </view>
            </view>
          </view>

          <view class="message-list">
            <view
              class="message-item"
              :class="{ user: msg.isUser }"
              v-for="msg in messages"
              :key="msg.id"
              :id="'msg-' + msg.id"
            >
              <view class="avatar" :class="{ user: msg.isUser }">
                <text>{{ msg.isUser ? '👤' : '🤖' }}</text>
              </view>
              <view class="bubble" :class="{ user: msg.isUser }">
                <text>{{ msg.content }}</text>
              </view>
              <text class="time">{{ msg.time }}</text>
            </view>

            <view class="typing" v-if="isTyping">
              <view class="avatar">🤖</view>
              <view class="dots">
                <view class="dot"></view>
                <view class="dot"></view>
                <view class="dot"></view>
              </view>
            </view>
          </view>
        </scroll-view>

        <view class="input-bar">
          <input
            class="chat-input"
            v-model="inputText"
            placeholder="输入问题或指令..."
            @confirm="sendMessage"
          />
          <view class="send-btn" :class="{ active: inputText.trim() }" @click="sendMessage">
            <text>发送</text>
          </view>
        </view>
      </view>
    </view>
  </Layout>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import Layout from '@/components/Layout.vue'
import { agentService } from '@/services/agent'
import type { Agent } from '@/data/agents'

interface Message {
  id: string
  content: string
  isUser: boolean
  time: string
}

const inputText = ref('')
const isTyping = ref(false)
const scrollToId = ref('')
const scrollTop = ref(0)
const agent = ref<Agent>({
  id: '', name: '', description: '', department: '', status: 'pending',
  tags: [], successRate: 0, avgTime: 0, dailyCalls: 0, usageCount: 0,
  creatorId: '', createdAt: '', isFavorite: false, rating: 0
})

const quickActions = ['查询路由', '计算时效', '创建工单', '查看报表']
const messages = ref<Message[]>([])

const getStatusText = (status: string) => {
  const map: Record<string, string> = { online: '在线', offline: '离线', pending: '待审核' }
  return map[status] || status
}

const sendMessage = async () => {
  if (!inputText.value.trim()) return
  if (!agent.value.id) return

  const userMsg: Message = {
    id: Date.now().toString(),
    content: inputText.value,
    isUser: true,
    time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  }

  messages.value.push(userMsg)
  const currentInput = inputText.value
  inputText.value = ''
  scrollToBottom()
  isTyping.value = true

  try {
    const res = await agentService.call(agent.value.id, currentInput)
    isTyping.value = false
    const replyMsg: Message = {
      id: (Date.now() + 1).toString(),
      content: res.output || '抱歉，AI服务未能返回有效回复。',
      isUser: false,
      time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
    }
    messages.value.push(replyMsg)
    scrollToBottom()
  } catch (e: any) {
    isTyping.value = false
    const replyMsg: Message = {
      id: (Date.now() + 1).toString(),
      content: 'AI服务调用失败：' + (e.message || '未知错误'),
      isUser: false,
      time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
    }
    messages.value.push(replyMsg)
    scrollToBottom()
  }
}

const sendQuick = (action: string) => {
  inputText.value = action
  sendMessage()
}

const scrollToBottom = () => {
  setTimeout(() => {
    scrollTop.value = 99999
    scrollToId.value = 'msg-' + messages.value[messages.value.length - 1]?.id
  }, 100)
}

const showMenu = () => {
  uni.showActionSheet({
    itemList: ['收藏Agent', '分享Agent', '反馈问题', '查看帮助'],
    success: (res) => {
      const actions = ['已收藏', '分享功能开发中', '请描述您的问题', '帮助文档开发中']
      uni.showToast({ title: actions[res.tapIndex], icon: 'none' })
    }
  })
}

onMounted(async () => {
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const options = (currentPage as unknown as { options?: { id?: string } }).options
  if (options?.id) {
    try {
      const agentData = await agentService.get(options.id)
      agent.value = agentData as Agent
    } catch (e) {
      console.error('Failed to load agent:', e)
    }
  }
})
</script>

<style lang="scss">
.page {
  display: flex;
  flex-direction: column;
  min-height: calc(100vh - 112px);
  background: #f5f7fa;
  margin: -24px;
  padding: 24px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  background: #fff;
  border-bottom: 1px solid #e8ecf1;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.page-title {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
}

.status-badge {
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;

  &.online { background: #f6ffed; color: #52c41a; }
  &.offline { background: #f5f5f5; color: #999; }
  &.pending { background: #fffbe6; color: #faad14; }
}

.more-btn {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  text { font-size: 20px; color: #999; }
}

.chat-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  min-height: 0;
  height: calc(100vh - 180px);
}

.chat-scroll {
  flex: 1;
  padding: 20px;
  min-height: 0;
  overflow-y: auto;
  height: calc(100vh - 260px);
}

.welcome-card {
  background: linear-gradient(135deg, #1890ff, #096dd9);
  border-radius: 16px;
  padding: 24px;
  text-align: center;
  margin-bottom: 24px;
  color: #fff;
}

.welcome-icon { font-size: 48px; margin-bottom: 12px; }
.welcome-title { font-size: 18px; font-weight: 600; display: block; margin-bottom: 8px; }
.welcome-desc { font-size: 13px; opacity: 0.9; display: block; margin-bottom: 16px; }

.quick-actions {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 12px;
}

.quick-btn {
  padding: 8px 16px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 20px;
  cursor: pointer;
  text { font-size: 13px; color: #fff; }
  &:hover { background: rgba(255, 255, 255, 0.3); }
}

.message-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.message-item {
  display: flex;
  gap: 12px;
  max-width: 85%;

  &.user {
    flex-direction: row-reverse;
    margin-left: auto;

    .bubble {
      background: #1890ff;
      color: #fff;
      border-radius: 16px 16px 4px 16px;
    }

    .time { text-align: right; }
  }
}

.avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #f0f2f5;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  flex-shrink: 0;
}

.bubble {
  padding: 12px 16px;
  background: #fff;
  color: #333;
  border-radius: 16px 16px 16px 4px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);

  text { font-size: 14px; line-height: 1.6; white-space: pre-wrap; }
}

.time {
  font-size: 11px;
  color: #999;
  margin-top: 4px;
}

.typing {
  display: flex;
  gap: 12px;
  align-items: center;
}

.dots {
  display: flex;
  gap: 4px;
  padding: 12px 16px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
}

.dot {
  width: 8px;
  height: 8px;
  background: #1890ff;
  border-radius: 50%;
  animation: bounce 1.4s infinite ease-in-out;

  &:nth-child(1) { animation-delay: 0s; }
  &:nth-child(2) { animation-delay: 0.2s; }
  &:nth-child(3) { animation-delay: 0.4s; }
}

@keyframes bounce {
  0%, 80%, 100% { transform: scale(0.6); opacity: 0.4; }
  40% { transform: scale(1); opacity: 1; }
}

.input-bar {
  display: flex;
  gap: 12px;
  padding: 16px;
  background: #fff;
  border-top: 1px solid #e8ecf1;
}

.chat-input {
  flex: 1;
  padding: 10px 16px;
  border: 1px solid #e8ecf1;
  border-radius: 24px;
  font-size: 14px;
  background: #f9f9f9;
  pointer-events: auto;
  cursor: text;
  user-select: text;
  -webkit-user-select: text;

  &:focus { 
    border-color: #1890ff; 
    background: #fff; 
    outline: none;
    box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.1);
  }
}

.send-btn {
  padding: 0 20px;
  background: #d9d9d9;
  color: #fff;
  border-radius: 24px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;

  &.active { background: #1890ff; }
}
</style>
