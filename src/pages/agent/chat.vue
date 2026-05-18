<template>
  <Layout>
    <view class="page">
      <view class="page-header">
        <view class="header-content">
          <view class="header-left">
            <text class="page-title">{{ agent.name }}</text>
            <text class="page-subtitle">{{ agent.description }}</text>
          </view>
          <view class="header-right">
            <view class="agent-status" :class="agent.status">
              <view class="status-dot"></view>
              <text>{{ agent.status === 'online' ? '在线' : agent.status === 'offline' ? '离线' : '待审核' }}</text>
            </view>
            <view class="more-btn" @click="showMenu">
              <text>⋮</text>
            </view>
          </view>
        </view>
      </view>

      <view class="chat-container">
        <scroll-view 
          scroll-y 
          class="chat-content" 
          :style="{ height: contentHeight + 'px' }"
          :scroll-into-view="scrollToId"
          scroll-with-animation
        >
          <view class="message-list">
            <view class="welcome-message">
              <view class="welcome-icon">🤖</view>
              <text class="welcome-title">欢迎使用{{ agent.name }}</text>
              <text class="welcome-desc">{{ agent.description }}</text>
              <view class="quick-actions">
                <view class="quick-btn" v-for="action in quickActions" :key="action" @click="sendQuick(action)">
                  <text>{{ action }}</text>
                </view>
              </view>
            </view>

            <view 
              class="message-item" 
              :class="{ user: msg.isUser }"
              v-for="msg in messages" 
              :key="msg.id"
              :id="'msg-' + msg.id"
            >
              <view class="message-avatar">
                <text>{{ msg.isUser ? '👤' : '🤖' }}</text>
              </view>
              <view class="message-content">
                <view class="message-bubble" :class="{ user: msg.isUser }">
                  <text>{{ msg.content }}</text>
                </view>
                <text class="message-time">{{ msg.time }}</text>
              </view>
            </view>

            <view class="typing-indicator" v-if="isTyping">
              <view class="typing-dots">
                <view class="dot"></view>
                <view class="dot"></view>
                <view class="dot"></view>
              </view>
              <text>正在思考...</text>
            </view>
          </view>
        </scroll-view>

        <view class="input-bar">
          <view class="input-wrap">
            <input 
              class="chat-input" 
              v-model="inputText" 
              placeholder="输入问题或指令..."
              @confirm="sendMessage"
            />
          </view>
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
import { mockAgents, type Agent } from '@/data/agents'

interface Message {
  id: string
  content: string
  isUser: boolean
  time: string
}

const contentHeight = ref(500)
const inputText = ref('')
const isTyping = ref(false)
const scrollToId = ref('')
const agent = ref<Agent>(mockAgents[0])

const quickActions = ['查询路由', '计算时效', '创建工单', '查看报表']

const messages = ref<Message[]>([])

const sendMessage = () => {
  if (!inputText.value.trim()) return
  
  const userMsg: Message = {
    id: Date.now().toString(),
    content: inputText.value,
    isUser: true,
    time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  }
  
  messages.value.push(userMsg)
  inputText.value = ''
  scrollToBottom()
  
  isTyping.value = true
  
  setTimeout(() => {
    isTyping.value = false
    
    const replyMsg: Message = {
      id: (Date.now() + 1).toString(),
      content: getReply(),
      isUser: false,
      time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
    }
    
    messages.value.push(replyMsg)
    scrollToBottom()
  }, 1500 + Math.random() * 1000)
}

const sendQuick = (action: string) => {
  inputText.value = action
  sendMessage()
}

const getReply = () => {
  const replies = [
    '好的，我已收到您的请求，正在处理中...\n\n根据您的需求，我查询到以下信息：\n- 路由状态：正常\n- 预计送达：今天下午18:00\n- 当前位置：北京市朝阳区',
    '已为您完成分析。结果如下：\n\n📊 数据分析\n- 平均时效：2.5小时\n- 异常率：3.2%\n- 建议：优化配送路线',
    '操作已完成！\n\n✅ 工单已创建\n📋 工单号：WD20240415001\n⏰ 预计处理时间：2小时',
    '报表数据已生成：\n\n📈 本周趋势\n- 调用量：+12%\n- 成功率：98.5%\n- 平均响应：2.3秒'
  ]
  return replies[Math.floor(Math.random() * replies.length)]
}

const scrollToBottom = () => {
  setTimeout(() => {
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

onMounted(() => {
  uni.getSystemInfo({
    success: (res) => {
      contentHeight.value = res.windowHeight - 200
    }
  })
  
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const options = (currentPage as unknown as { options?: { id?: string } }).options
  if (options?.id) {
    const found = mockAgents.find(a => a.id === options.id)
    if (found) {
      agent.value = found
    }
  }
})
</script>

<style lang="scss">
.page {
  min-height: 100vh;
  background: #f0f2f5;
  display: flex;
  flex-direction: column;
}

.page-header {
  background: #fff;
  padding: 16px 32px;
  border-bottom: 1px solid #e8e8e8;
  flex-shrink: 0;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  color: #1f2937;
}

.page-subtitle {
  font-size: 14px;
  color: #6b7280;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.agent-status {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 12px;
  border-radius: 20px;
  
  text {
    font-size: 13px;
  }
  
  &.online {
    background: #d1fae5;
    text { color: #065f46; }
    .status-dot { background: #10b981; }
  }
  
  &.offline {
    background: #f3f4f6;
    text { color: #6b7280; }
    .status-dot { background: #9ca3af; }
  }
  
  &.pending {
    background: #fef3c7;
    text { color: #d97706; }
    .status-dot { background: #f59e0b; }
  }
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.more-btn {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  
  text {
    font-size: 24px;
    color: #6b7280;
  }
}

.chat-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 24px;
}

.chat-content {
  flex: 1;
  overflow: hidden;
}

.message-list {
  height: 100%;
}

.welcome-message {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 32px;
  background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%);
  border-radius: 16px;
  margin-bottom: 24px;
}

.welcome-icon {
  font-size: 48px;
  margin-bottom: 12px;
}

.welcome-title {
  font-size: 20px;
  font-weight: 700;
  color: #fff;
}

.welcome-desc {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.8);
  margin-top: 6px;
  text-align: center;
}

.quick-actions {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 12px;
  margin-top: 20px;
}

.quick-btn {
  padding: 8px 18px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.2s;
  
  text {
    font-size: 13px;
    color: #fff;
  }
  
  &:hover {
    background: rgba(255, 255, 255, 0.3);
  }
}

.message-item {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  
  &.user {
    flex-direction: row-reverse;
    
    .message-content {
      align-items: flex-end;
    }
    
    .message-bubble {
      background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%);
      
      text { color: #fff; }
    }
    
    .message-time {
      text-align: right;
    }
  }
}

.message-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: #e5e7eb;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  
  text {
    font-size: 24px;
  }
}

.message-content {
  display: flex;
  flex-direction: column;
  max-width: 70%;
  gap: 6px;
}

.message-bubble {
  padding: 14px 18px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  
  text {
    font-size: 14px;
    color: #1f2937;
    line-height: 1.6;
    white-space: pre-wrap;
  }
}

.message-time {
  text {
    font-size: 12px;
    color: #9ca3af;
  }
}

.typing-indicator {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 14px;
  background: #fff;
  border-radius: 16px;
  width: 160px;
  
  text {
    font-size: 13px;
    color: #6b7280;
  }
}

.typing-dots {
  display: flex;
  gap: 6px;
}

.dot {
  width: 8px;
  height: 8px;
  background: #9ca3af;
  border-radius: 50%;
  animation: typing 1.4s infinite ease-in-out;
  
  &:nth-child(1) { animation-delay: 0s; }
  &:nth-child(2) { animation-delay: 0.2s; }
  &:nth-child(3) { animation-delay: 0.4s; }
}

@keyframes typing {
  0%, 80%, 100% { transform: scale(0.6); opacity: 0.5; }
  40% { transform: scale(1); opacity: 1; }
}

.input-bar {
  display: flex;
  gap: 12px;
  padding: 16px;
  background: #fff;
  border-radius: 12px;
  margin-top: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.input-wrap {
  flex: 1;
  background: #f3f4f6;
  border-radius: 24px;
  padding: 0 18px;
}

.chat-input {
  width: 100%;
  height: 48px;
  font-size: 14px;
}

.send-btn {
  width: 80px;
  height: 48px;
  border-radius: 24px;
  background: #e5e7eb;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
  
  text {
    font-size: 14px;
    color: #9ca3af;
    font-weight: 500;
  }
  
  &.active {
    background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%);
    
    text {
      color: #fff;
    }
  }
}
</style>