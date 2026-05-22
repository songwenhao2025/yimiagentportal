<template>
  <Layout>
    <view class="page">
      <!-- Header -->
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

      <!-- Content -->
      <view class="page-content">
        <!-- Filters -->
        <view class="filter-bar">
          <view class="filter-group">
            <text class="filter-label">部门:</text>
            <view class="filter-chips">
              <view
                class="chip"
                :class="{ active: selectedDept === '' }"
                @click="selectedDept = ''"
              >
                <text>全部</text>
              </view>
              <view
                v-for="dept in departments"
                :key="dept.id"
                class="chip"
                :class="{ active: selectedDept === dept.id }"
                @click="selectedDept = dept.id"
              >
                <text>{{ dept.name }}</text>
              </view>
            </view>
          </view>

          <view class="filter-group">
            <text class="filter-label">排序:</text>
            <view class="filter-chips">
              <view class="chip" :class="{ active: sortBy === 'hot' }" @click="sortBy = 'hot'">
                <text>🔥 热门</text>
              </view>
              <view class="chip" :class="{ active: sortBy === 'rate' }" @click="sortBy = 'rate'">
                <text>⭐ 评分</text>
              </view>
              <view class="chip" :class="{ active: sortBy === 'new' }" @click="sortBy = 'new'">
                <text>🕐 最新</text>
              </view>
            </view>
          </view>
        </view>

        <!-- Agent Grid -->
        <view class="agents-grid">
          <view class="agent-card" v-for="agent in filteredAgents" :key="agent.id" @click="goToDetail(agent.id)">
            <view class="agent-header">
              <view class="agent-avatar">{{ getDeptIcon(agent.department) }}</view>
              <view class="agent-status" :class="agent.status">
                <text>{{ getStatusText(agent.status) }}</text>
              </view>
            </view>
            <text class="agent-name">{{ agent.name }}</text>
            <text class="agent-desc">{{ agent.description }}</text>
            <view class="agent-tags">
              <text class="agent-tag" v-for="tag in agent.tags" :key="tag">{{ tag }}</text>
            </view>
            <view class="agent-footer">
              <view class="agent-stats">
                <text class="stat-item">👍 {{ agent.successRate }}%</text>
                <text class="stat-item">⚡ {{ agent.avgTime }}s</text>
              </view>
              <view class="agent-actions">
                <view class="edit-btn" @click.stop="editAgent(agent)">
                  <text>编辑</text>
                </view>
                <view class="delete-btn" @click.stop="confirmDelete(agent)">
                  <text>删除</text>
                </view>
                <view class="use-btn" @click.stop="goToChat(agent)">
                  <text>使用</text>
                </view>
              </view>
            </view>
          </view>
        </view>

        <!-- Empty State -->
        <view class="empty-state" v-if="filteredAgents.length === 0 && !loading">
          <text class="empty-icon">🔍</text>
          <text class="empty-text">未找到匹配的Agent</text>
        </view>

        <view class="loading-state" v-if="loading">
          <text class="loading-icon">⏳</text>
          <text class="loading-text">加载中...</text>
        </view>
      </view>

      <!-- Delete Confirm Dialog -->
      <view class="dialog-overlay" v-if="showDeleteConfirm" @click="showDeleteConfirm = false">
        <view class="dialog-content" @click.stop>
          <text class="dialog-title">确认删除</text>
          <text class="dialog-message">确定要删除 "{{ deletingAgent?.name }}" 吗？此操作不可恢复。</text>
          <view class="dialog-actions">
            <view class="dialog-btn cancel" @click="showDeleteConfirm = false">
              <text>取消</text>
            </view>
            <view class="dialog-btn confirm" @click="deleteAgent">
              <text>确认删除</text>
            </view>
          </view>
        </view>
      </view>
    </view>
  </Layout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import Layout from '@/components/Layout.vue'
import { agentService } from '@/services/agent'
import { adminService } from '@/services/admin'
import type { Agent } from '@/data/agents'

const searchQuery = ref('')
const selectedDept = ref('')
const sortBy = ref('hot')
const agents = ref<Agent[]>([])
const loading = ref(true)
const departments = ref<{ id: string; name: string }[]>([])

const showDeleteConfirm = ref(false)
const deletingAgent = ref<Agent | null>(null)

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

const getDeptIcon = (dept: string) => {
  const map: Record<string, string> = { operation: '🚚', qc: '✅', customer: '💬', finance: '💰' }
  return map[dept] || '🤖'
}

const getStatusText = (status: string) => {
  const map: Record<string, string> = { online: '在线', offline: '离线', pending: '审核中' }
  return map[status] || status
}

const loadDepartments = async () => {
  try {
    const response = await adminService.getDepartments()
    if (Array.isArray(response)) {
      departments.value = response
    }
  } catch (error) {
    console.error(error)
  }
}

const loadAgents = async () => {
  loading.value = true
  try {
    const params: any = {}
    if (selectedDept.value) params.department = selectedDept.value
    if (searchQuery.value) params.keyword = searchQuery.value
    const response = await agentService.list(params)
    agents.value = response.list || []
  } catch (error) {
    console.error(error)
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

const editAgent = (agent: Agent) => {
  uni.navigateTo({ url: `/pages/builder/index?id=${agent.id}` })
}

const confirmDelete = (agent: Agent) => {
  deletingAgent.value = agent
  showDeleteConfirm.value = true
}

const deleteAgent = async () => {
  if (!deletingAgent.value) return
  
  try {
    await agentService.delete(deletingAgent.value.id)
    uni.showToast({ title: '删除成功', icon: 'success' })
    agents.value = agents.value.filter(a => a.id !== deletingAgent.value?.id)
  } catch (error) {
    console.error('Failed to delete agent:', error)
    uni.showToast({ title: '删除失败', icon: 'error' })
  } finally {
    showDeleteConfirm.value = false
    deletingAgent.value = null
  }
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
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 24px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.page-title {
  font-size: 24px;
  font-weight: 700;
  color: #1a1a1a;
}

.header-actions {
  display: flex;
  gap: 16px;
  align-items: center;
}

.search-box {
  display: flex;
  align-items: center;
  background: #fff;
  border: 1px solid #d9d9d9;
  border-radius: 24px;
  padding: 8px 16px;
  width: 300px;

  &:focus-within {
    border-color: #1890ff;
  }
}

.search-icon {
  margin-right: 8px;
}

.search-input {
  flex: 1;
  font-size: 14px;
  background: transparent;

  &::placeholder { color: #999; }
}

.btn {
  padding: 8px 20px;
  border-radius: 24px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;

  &.primary {
    background: #1890ff;
    color: #fff;

    &:hover { background: #40a9ff; }
  }
}

/* Filters */
.filter-bar {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 24px;
  background: #fff;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.filter-group {
  display: flex;
  align-items: center;
  gap: 12px;
}

.filter-label {
  font-size: 14px;
  font-weight: 500;
  color: #666;
}

.filter-chips {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.chip {
  padding: 6px 14px;
  border-radius: 16px;
  background: #f5f5f5;
  cursor: pointer;
  transition: all 0.2s;

  text { font-size: 13px; color: #666; }

  &:hover { background: #e6f7ff; }

  &.active {
    background: #1890ff;

    text { color: #fff; }
  }
}

/* Agent Grid */
.agents-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.agent-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  cursor: pointer;
  transition: all 0.3s;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 20px rgba(0, 0, 0, 0.08);
  }
}

.agent-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.agent-avatar {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: linear-gradient(135deg, #1890ff, #096dd9);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.agent-status {
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 11px;

  &.online { background: #f6ffed; color: #52c41a; }
  &.offline { background: #f5f5f5; color: #999; }
  &.pending { background: #fffbe6; color: #faad14; }
}

.agent-name {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 8px;
}

.agent-desc {
  font-size: 13px;
  color: #888;
  line-height: 1.5;
  margin-bottom: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.agent-tags {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}

.agent-tag {
  padding: 4px 8px;
  border-radius: 4px;
  background: #f0f2f5;
  font-size: 11px;
  color: #666;
}

.agent-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-top: 1px solid #f0f0f0;
  padding-top: 12px;
}

.agent-stats {
  display: flex;
  gap: 12px;
}

.stat-item {
  font-size: 12px;
  color: #888;
}

.agent-actions {
  display: flex;
  gap: 6px;
  flex-shrink: 0;
}

.edit-btn, .use-btn, .delete-btn {
  padding: 6px 14px;
  border-radius: 16px;
  font-size: 12px;
  cursor: pointer;
  white-space: nowrap;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 50px;
}

.edit-btn {
  background: #f5f5f5;
  color: #666;
  border: 1px solid #d9d9d9;

  &:hover { background: #e6e6e6; }
}

.use-btn {
  background: #1890ff;
  color: #fff;
  border: none;

  &:hover { background: #40a9ff; }
}

.delete-btn {
  background: #fff;
  color: #ff4d4f;
  border: 1px solid #ffccc7;

  &:hover { 
    background: #fff1f0;
    border-color: #ff9c9c;
  }
}

/* States */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px;
  background: #fff;
  border-radius: 12px;
}

.empty-icon { font-size: 48px; opacity: 0.5; margin-bottom: 12px; }
.empty-text { font-size: 16px; color: #888; }

.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px;
}

.loading-icon { font-size: 48px; animation: spin 1s linear infinite; color: #1890ff; }
.loading-text { font-size: 14px; color: #888; margin-top: 12px; }

@keyframes spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }

/* Dialog Styles */
.dialog-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.dialog-content {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  width: 90%;
  max-width: 400px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
}

.dialog-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  display: block;
  margin-bottom: 12px;
}

.dialog-message {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
  display: block;
  margin-bottom: 20px;
}

.dialog-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.dialog-btn {
  padding: 8px 20px;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;

  &.cancel {
    background: #f5f5f5;
    color: #666;
    border: 1px solid #d9d9d9;

    &:hover { background: #e6e6e6; }
  }

  &.confirm {
    background: #ff4d4f;
    color: #fff;

    &:hover { background: #ff7875; }
  }
}
</style>
