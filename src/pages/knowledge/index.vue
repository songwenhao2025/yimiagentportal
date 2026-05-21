<template>
  <Layout>
    <view class="page">
      <!-- Header -->
      <view class="page-header">
        <view class="header-content">
          <text class="page-title">知识库管理</text>
          <view class="header-actions">
            <view class="search-box">
              <text class="search-icon">🔍</text>
              <input class="search-input" v-model="searchText" placeholder="搜索文档..." />
            </view>
            <view class="btn primary" @click="uploadDocument">
              <text>📤 上传文档</text>
            </view>
          </view>
        </view>
      </view>

      <!-- Filters -->
      <view class="filter-bar">
        <view class="filter-chips">
          <view
            class="chip"
            :class="{ active: activeCategory === item.id }"
            v-for="item in categories"
            :key="item.id"
            @click="activeCategory = item.id"
          >
            <text>{{ item.name }}</text>
          </view>
        </view>
      </view>

      <!-- Document List -->
      <view class="doc-list">
        <view class="doc-card" v-for="doc in filteredDocs" :key="doc.id">
          <view class="doc-icon">{{ getTypeIcon(doc.type) }}</view>
          <view class="doc-info">
            <text class="doc-title">{{ doc.title }}</text>
            <view class="doc-meta">
              <text class="meta-tag">{{ doc.category }}</text>
              <text class="meta-tag">{{ formatSize(doc.size) }}</text>
              <text class="meta-tag">{{ doc.uploadedBy }}</text>
            </view>
            <view class="doc-status">
              <view class="status-badge" :class="doc.status">
                <text>{{ getStatusText(doc.status) }}</text>
              </view>
              <view class="status-badge vector" :class="doc.vectorStatus">
                <text>{{ getVectorStatusText(doc.vectorStatus) }}</text>
              </view>
            </view>
          </view>
          <view class="doc-actions">
            <view class="action-btn secondary" @click="previewDoc(doc)">
              <text>👁️ 预览</text>
            </view>
            <view class="action-btn danger" @click="deleteDoc(doc)">
              <text>🗑️ 删除</text>
            </view>
          </view>
        </view>

        <view class="empty-state" v-if="filteredDocs.length === 0">
          <text class="empty-icon">📭</text>
          <text class="empty-text">暂无文档</text>
        </view>
      </view>
    </view>
  </Layout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import Layout from '@/components/Layout.vue'
import { knowledgeService } from '@/services/knowledge'
import type { KnowledgeDocument } from '@/data/knowledge'

const searchText = ref('')
const activeCategory = ref('all')
const docs = ref<KnowledgeDocument[]>([])
const categories = ref<{ id: string; name: string }[]>([])

const filteredDocs = computed(() => {
  let result = docs.value
  if (searchText.value) {
    const keyword = searchText.value.toLowerCase()
    result = result.filter(d => d.title.toLowerCase().includes(keyword))
  }
  return result
})

const getTypeIcon = (type: string) => {
  const icons: Record<string, string> = { pdf: '📕', word: '📘', excel: '📗', markdown: '📝', url: '🔗' }
  return icons[type] || '📄'
}

const formatSize = (bytes: number) => {
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(1) + ' MB'
}

const getStatusText = (status: string) => {
  const texts: Record<string, string> = { uploading: '上传中', processing: '处理中', ready: '已就绪', failed: '失败' }
  return texts[status] || status
}

const getVectorStatusText = (status: string) => {
  const texts: Record<string, string> = { pending: '待向量化', indexing: '向量化中', completed: '已完成' }
  return texts[status] || status
}

const loadDocuments = async () => {
  try {
    const params: any = {}
    if (activeCategory.value !== 'all') {
      params.category = getCategoryName(activeCategory.value)
    }
    const response = await knowledgeService.list({ page: 1, size: 100, ...params })
    docs.value = response.list || []
  } catch (error) {
    console.error(error)
  }
}

const loadCategories = async () => {
  try {
    const cats = await knowledgeService.getCategories()
    if (Array.isArray(cats)) {
      categories.value = [
        { id: 'all', name: '全部' },
        ...cats.map(c => ({ id: c.id, name: c.name }))
      ]
    }
  } catch (error) {
    console.error(error)
  }
}

const uploadDocument = () => {
  uni.showToast({ title: '上传功能开发中', icon: 'none' })
}

onMounted(() => {
  loadCategories()
  loadDocuments()
})

const previewDoc = (doc: KnowledgeDocument) => {
  uni.showToast({ title: `预览: ${doc.title}`, icon: 'none' })
}

const deleteDoc = async (doc: KnowledgeDocument) => {
  uni.showModal({
    title: '确认删除',
    content: `确定要删除 "${doc.title}" 吗？`,
    success: async (res) => {
      if (res.confirm) {
        try {
          await knowledgeService.delete(doc.id)
          uni.showToast({ title: '已删除', icon: 'success' })
          loadDocuments()
        } catch (e) {
          console.error(e)
        }
      }
    }
  })
}
</script>

<style lang="scss">
.page {
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
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
  width: 240px;

  &:focus-within { border-color: #1890ff; }
}

.search-icon { margin-right: 8px; }
.search-input { flex: 1; font-size: 14px; background: transparent; &::placeholder { color: #999; } }

.btn {
  padding: 8px 20px;
  border-radius: 24px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;

  &.primary { background: #1890ff; color: #fff; &:hover { background: #40a9ff; } }
}

.filter-bar {
  margin-bottom: 24px;
}

.filter-chips {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.chip {
  padding: 6px 14px;
  border-radius: 16px;
  background: #fff;
  border: 1px solid #d9d9d9;
  cursor: pointer;

  text { font-size: 13px; color: #666; }

  &:hover { border-color: #1890ff; }

  &.active { background: #1890ff; border-color: #1890ff; text { color: #fff; } }
}

.doc-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.doc-card {
  display: flex;
  align-items: center;
  gap: 16px;
  background: #fff;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  transition: all 0.2s;

  &:hover { transform: translateY(-2px); box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08); }
}

.doc-icon {
  width: 48px;
  height: 48px;
  background: #f0f2f5;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.doc-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.doc-title { font-size: 16px; font-weight: 600; color: #1a1a1a; }

.doc-meta { display: flex; gap: 8px; }
.meta-tag { font-size: 12px; color: #888; background: #f5f5f5; padding: 2px 6px; border-radius: 4px; }

.doc-status { display: flex; gap: 8px; }
.status-badge { padding: 4px 8px; border-radius: 4px; font-size: 11px; background: #f0f2f5; color: #666; &.ready, &.completed { background: #f6ffed; color: #52c41a; } &.failed { background: #fff1f0; color: #ff4d4f; } &.uploading, &.processing, &.indexing { background: #e6f7ff; color: #1890ff; } }

.doc-actions {
  display: flex;
  gap: 8px;
}

.action-btn {
  padding: 6px 12px;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;

  &.secondary { background: #f5f5f5; color: #666; }
  &.danger { background: #fff1f0; color: #ff4d4f; }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 60px;
  background: #fff;
  border-radius: 12px;
}

.empty-icon { font-size: 48px; opacity: 0.5; margin-bottom: 12px; }
.empty-text { font-size: 16px; color: #888; }
</style>
