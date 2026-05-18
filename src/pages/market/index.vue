<template>
  <Layout>
    <view class="page">
      <view class="page-header">
        <view class="header-content">
          <text class="page-title">Agent市场</text>
          <view class="header-actions">
            <view class="search-box">
              <text class="search-icon">🔍</text>
              <input 
                class="search-input" 
                type="text" 
                placeholder="搜索Agent名称或描述..."
                v-model="searchQuery"
              />
            </view>
            <view class="btn primary" @click="goToBuilder">
              <text>+ 创建Agent</text>
            </view>
          </view>
        </view>
      </view>

      <view class="page-content">
        <view class="sidebar-filter">
          <view class="filter-section">
            <text class="filter-title">部门分类</text>
            <view class="filter-options">
              <view 
                class="filter-item" 
                :class="{ active: selectedDept === '' }"
                @click="selectedDept = ''"
              >
                <text>全部</text>
              </view>
              <view 
                class="filter-item" 
                :class="{ active: selectedDept === 'operation' }"
                @click="selectedDept = 'operation'"
              >
                <text>🚚 运营部</text>
              </view>
              <view 
                class="filter-item" 
                :class="{ active: selectedDept === 'qc' }"
                @click="selectedDept = 'qc'"
              >
                <text>✅ 质控部</text>
              </view>
              <view 
                class="filter-item" 
                :class="{ active: selectedDept === 'customer' }"
                @click="selectedDept = 'customer'"
              >
                <text>💬 客服部</text>
              </view>
              <view 
                class="filter-item" 
                :class="{ active: selectedDept === 'finance' }"
                @click="selectedDept = 'finance'"
              >
                <text>💰 财务部</text>
              </view>
            </view>
          </view>

          <view class="filter-section">
            <text class="filter-title">排序方式</text>
            <view class="filter-options">
              <view 
                class="filter-item" 
                :class="{ active: sortBy === 'hot' }"
                @click="sortBy = 'hot'"
              >
                <text>🔥 热门</text>
              </view>
              <view 
                class="filter-item" 
                :class="{ active: sortBy === 'rate' }"
                @click="sortBy = 'rate'"
              >
                <text>⭐ 评分</text>
              </view>
              <view 
                class="filter-item" 
                :class="{ active: sortBy === 'new' }"
                @click="sortBy = 'new'"
              >
                <text>🕐 最新</text>
              </view>
            </view>
          </view>

          <view class="filter-section">
            <text class="filter-title">标签筛选</text>
            <view class="filter-tags">
              <view class="filter-tag" v-for="tag in allTags" :key="tag" @click="toggleTag(tag)">
                <text>{{ tag }}</text>
              </view>
            </view>
          </view>
        </view>

        <view class="main-area">
          <view class="agents-grid">
            <AgentCard 
              v-for="agent in filteredAgents" 
              :key="agent.id" 
              :agent="agent"
              @click="goToDetail(agent.id)"
              @use="goToChat"
            />
          </view>
          
          <view class="empty-state" v-if="filteredAgents.length === 0">
            <text class="empty-icon">🔍</text>
            <text class="empty-text">未找到匹配的Agent</text>
            <text class="empty-hint">尝试调整筛选条件或搜索关键词</text>
          </view>
        </view>
      </view>
    </view>
  </Layout>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import Layout from '@/components/Layout.vue'
import AgentCard from '@/components/AgentCard.vue'
import { mockAgents, type Agent } from '@/data/agents'

const searchQuery = ref('')
const selectedDept = ref('')
const sortBy = ref('hot')
const selectedTags = ref<string[]>([])

const allTags = computed(() => {
  const tags = new Set<string>()
  mockAgents.forEach(agent => agent.tags.forEach(tag => tags.add(tag)))
  return Array.from(tags)
})

const filteredAgents = computed(() => {
  let result = [...mockAgents]
  
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    result = result.filter(a => 
      a.name.toLowerCase().includes(query) || 
      a.description.toLowerCase().includes(query)
    )
  }
  
  if (selectedDept.value) {
    result = result.filter(a => a.department === selectedDept.value)
  }
  
  if (selectedTags.value.length > 0) {
    result = result.filter(a => 
      selectedTags.value.some(tag => a.tags.includes(tag))
    )
  }
  
  switch (sortBy.value) {
    case 'rate':
      result.sort((a, b) => b.successRate - a.successRate)
      break
    case 'new':
      result.sort((a, b) => 0)
      break
    default:
      result.sort((a, b) => b.usageCount - a.usageCount)
  }
  
  return result
})

const toggleTag = (tag: string) => {
  const idx = selectedTags.value.indexOf(tag)
  if (idx === -1) {
    selectedTags.value.push(tag)
  } else {
    selectedTags.value.splice(idx, 1)
  }
}

const goToDetail = (id: string) => {
  uni.navigateTo({ url: `/pages/agent/detail?id=${id}` })
}

const goToChat = (agent: Agent) => {
  uni.navigateTo({ url: `/pages/agent/chat?id=${agent.id}` })
}

const goToBuilder = () => {
  uni.navigateTo({ url: '/pages/builder/index' })
}
</script>

<style lang="scss">
.page {
  min-height: 100vh;
  background: #f0f2f5;
}

.page-header {
  background: #fff;
  padding: 20px 32px;
  border-bottom: 1px solid #e8e8e8;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.page-title {
  font-size: 24px;
  font-weight: 700;
  color: #1f2937;
}

.header-actions {
  display: flex;
  gap: 16px;
}

.search-box {
  display: flex;
  align-items: center;
  background: #f3f4f6;
  border-radius: 8px;
  padding: 10px 16px;
  width: 300px;
}

.search-icon {
  font-size: 16px;
  margin-right: 10px;
}

.search-input {
  flex: 1;
  font-size: 14px;
  background: transparent;
  color: #1f2937;
  
  &::placeholder {
    color: #9ca3af;
  }
}

.btn {
  padding: 10px 20px;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
  
  &.primary {
    background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%);
    color: #fff;
    
    &:hover {
      opacity: 0.9;
    }
  }
}

.page-content {
  display: flex;
  padding: 24px 32px;
  gap: 24px;
}

.sidebar-filter {
  width: 220px;
  flex-shrink: 0;
}

.filter-section {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 16px;
}

.filter-title {
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 12px;
  display: block;
}

.filter-options {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.filter-item {
  padding: 10px 12px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
  
  text {
    font-size: 13px;
    color: #6b7280;
  }
  
  &:hover {
    background: #f3f4f6;
  }
  
  &.active {
    background: #eef2ff;
    
    text {
      color: #4f46e5;
      font-weight: 500;
    }
  }
}

.filter-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.filter-tag {
  padding: 6px 10px;
  background: #f3f4f6;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
  
  text {
    font-size: 12px;
    color: #6b7280;
  }
  
  &:hover {
    background: #e5e7eb;
  }
  
  &.active {
    background: #4f46e5;
    
    text {
      color: #fff;
    }
  }
}

.main-area {
  flex: 1;
}

.agents-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  background: #fff;
  border-radius: 12px;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.empty-text {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
}

.empty-hint {
  font-size: 14px;
  color: #9ca3af;
  margin-top: 8px;
}
</style>
