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
                v-for="dept in departments" 
                :key="dept.id"
                class="filter-item" 
                :class="{ active: selectedDept === dept.id }"
                @click="selectedDept = dept.id"
              >
                <text>{{ dept.name }}</text>
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
              <view 
                class="filter-tag" 
                :class="{ active: selectedTags.includes(tag) }"
                v-for="tag in allTags" 
                :key="tag" 
                @click="toggleTag(tag)"
              >
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
          
          <view class="empty-state" v-if="filteredAgents.length === 0 && !loading">
            <text class="empty-icon">🔍</text>
            <text class="empty-text">未找到匹配的Agent</text>
            <text class="empty-hint">尝试调整筛选条件或搜索关键词</text>
          </view>

          <view class="loading-state" v-if="loading">
            <text class="loading-icon">⏳</text>
            <text class="loading-text">加载中...</text>
          </view>
        </view>
      </view>
    </view>
  </Layout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import Layout from '@/components/Layout.vue'
import AgentCard from '@/components/AgentCard.vue'
import { agentService } from '@/services/agent'
import { adminService } from '@/services/admin'
import type { Agent } from '@/data/agents'

const searchQuery = ref('')
const selectedDept = ref('')
const sortBy = ref('hot')
const selectedTags = ref<string[]>([])
const agents = ref<Agent[]>([])
const loading = ref(true)
const departments = ref<{ id: string; name: string }[]>([])

const allTags = computed(() => {
  const tags = new Set<string>()
  agents.value.forEach(agent => {
    if (agent.tags && Array.isArray(agent.tags)) {
      agent.tags.forEach((tag: string) => tags.add(tag))
    }
  })
  return Array.from(tags)
})

const filteredAgents = computed(() => {
  let result = [...agents.value]
  
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    result = result.filter(a => 
      a.name.toLowerCase().includes(query) || 
      (a.description && a.description.toLowerCase().includes(query))
    )
  }
  
  if (selectedDept.value) {
    result = result.filter(a => a.department === selectedDept.value)
  }
  
  if (selectedTags.value.length > 0) {
    result = result.filter(a => {
      if (!a.tags || !Array.isArray(a.tags)) return false
      return selectedTags.value.some(tag => a.tags.includes(tag))
    })
  }
  
  switch (sortBy.value) {
    case 'rate':
      result.sort((a, b) => (b.successRate || 0) - (a.successRate || 0))
      break
    case 'new':
      result.sort((a, b) => (b.createdAt || '').localeCompare(a.createdAt || ''))
      break
    default:
      result.sort((a, b) => (b.usageCount || 0) - (a.usageCount || 0))
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

const loadDepartments = async () => {
  try {
    const response = await adminService.getDepartments()
    // 响应拦截器已解包 ApiResponse.data，此处 response 就是数组
    if (Array.isArray(response)) {
      departments.value = response
    }
  } catch (error) {
    console.error('Failed to load departments:', error)
  }
}

const loadAgents = async () => {
  loading.value = true
  try {
    const params: any = {}
    if (selectedDept.value) {
      params.department = selectedDept.value
    }
    if (searchQuery.value) {
      params.keyword = searchQuery.value
    }
    
    const response = await agentService.list(params)
    agents.value = response.list || []
  } catch (error) {
    console.error('Failed to load agents:', error)
  } finally {
    loading.value = false
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

onMounted(() => {
  loadDepartments()
  loadAgents()
})

watch([searchQuery, selectedDept, sortBy], () => {
  loadAgents()
})
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

.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
}

.loading-icon {
  font-size: 48px;
  animation: spin 1s linear infinite;
}

.loading-text {
  font-size: 14px;
  color: #9ca3af;
  margin-top: 16px;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
</style>