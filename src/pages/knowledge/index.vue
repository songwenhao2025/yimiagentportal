<template>
  <Layout>
    <view class="page">
      <view class="page-header">
        <view class="header-content">
          <text class="page-title">知识库管理</text>
          <view class="header-actions">
            <view class="search-box">
              <text class="search-icon">🔍</text>
              <input 
                class="search-input" 
                v-model="searchText" 
                placeholder="搜索文档..."
              />
            </view>
            <view class="btn primary" @click="uploadDocument">
              <text>📤 上传文档</text>
            </view>
          </view>
        </view>
      </view>

      <view class="page-content">
        <view class="filter-tabs">
          <view 
            class="tab-item" 
            :class="{ active: activeCategory === item.id }"
            v-for="item in categories" 
            :key="item.id"
            @click="activeCategory = item.id"
          >
            <text>{{ item.name }}</text>
          </view>
        </view>

        <view class="document-list">
          <view class="doc-card" v-for="doc in filteredDocs" :key="doc.id">
            <view class="doc-icon">
              <text>{{ getTypeIcon(doc.type) }}</text>
            </view>
            <view class="doc-info">
              <text class="doc-title">{{ doc.title }}</text>
              <view class="doc-meta">
                <text>{{ doc.category }}</text>
                <text>·</text>
                <text>{{ formatSize(doc.size) }}</text>
                <text>·</text>
                <text>{{ doc.uploadedBy }}</text>
              </view>
              <view class="doc-status">
                <view class="status-item" :class="doc.status">
                  <text>{{ getStatusText(doc.status) }}</text>
                </view>
                <view class="status-item" :class="doc.vectorStatus">
                  <text>{{ getVectorStatusText(doc.vectorStatus) }}</text>
                </view>
              </view>
            </view>
            <view class="doc-actions">
              <view class="action-btn" @click="previewDoc(doc)">
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
            <text class="empty-hint">点击上方按钮上传文档</text>
          </view>
        </view>
      </view>
    </view>
  </Layout>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import Layout from '@/components/Layout.vue'
import { mockDocuments, documentCategories, type KnowledgeDocument } from '@/data/knowledge'

const searchText = ref('')
const activeCategory = ref('all')
const docs = ref(mockDocuments)

const categories = documentCategories

const filteredDocs = computed(() => {
  let result = docs.value

  if (activeCategory.value !== 'all') {
    result = result.filter(d => d.category === getCategoryName(activeCategory.value))
  }

  if (searchText.value) {
    const keyword = searchText.value.toLowerCase()
    result = result.filter(d => d.title.toLowerCase().includes(keyword))
  }

  return result
})

const getCategoryName = (id: string) => {
  const map: Record<string, string> = {
    all: '',
    operation: '操作手册',
    faq: 'FAQ',
    finance: '财务文档',
    tech: '技术文档',
    standard: '标准规范'
  }
  return map[id] || ''
}

const getTypeIcon = (type: string) => {
  const icons: Record<string, string> = {
    pdf: '📕',
    word: '📘',
    excel: '📗',
    markdown: '📝',
    url: '🔗'
  }
  return icons[type] || '📄'
}

const formatSize = (bytes: number) => {
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(1) + ' MB'
}

const getStatusText = (status: string) => {
  const texts: Record<string, string> = {
    uploading: '上传中',
    processing: '处理中',
    ready: '已就绪',
    failed: '失败'
  }
  return texts[status] || status
}

const getVectorStatusText = (status: string) => {
  const texts: Record<string, string> = {
    pending: '待向量化',
    indexing: '向量化中',
    completed: '已完成'
  }
  return texts[status] || status
}

const uploadDocument = () => {
  uni.showActionSheet({
    itemList: ['上传PDF', '上传Word', '上传Excel', '上传Markdown'],
    success: (res) => {
      uni.showToast({ title: '上传功能开发中', icon: 'none' })
    }
  })
}

const previewDoc = (doc: KnowledgeDocument) => {
  uni.showToast({ title: `预览: ${doc.title}`, icon: 'none' })
}

const deleteDoc = (doc: KnowledgeDocument) => {
  uni.showModal({
    title: '确认删除',
    content: `确定要删除 "${doc.title}" 吗？`,
    success: (res) => {
      if (res.confirm) {
        const idx = docs.value.findIndex(d => d.id === doc.id)
        if (idx !== -1) {
          docs.value.splice(idx, 1)
          uni.showToast({ title: '已删除', icon: 'success' })
        }
      }
    }
  })
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
  
  &.danger {
    background: #fee2e2;
    color: #dc2626;
    
    &:hover {
      background: #fecaca;
    }
  }
}

.page-content {
  padding: 24px 32px;
}

.filter-tabs {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
  flex-wrap: wrap;
}

.tab-item {
  padding: 10px 20px;
  background: #fff;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  
  text {
    font-size: 14px;
    color: #6b7280;
  }
  
  &:hover {
    background: #f3f4f6;
  }
  
  &.active {
    background: #eef2ff;
    
    text {
      color: #4f46e5;
      font-weight: 600;
    }
  }
}

.document-list {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
}

.doc-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  border-bottom: 1px solid #f3f4f6;
  
  &:last-child {
    border-bottom: none;
  }
  
  &:hover {
    background: #f9fafb;
    border-radius: 8px;
  }
}

.doc-icon {
  width: 56px;
  height: 56px;
  border-radius: 10px;
  background: #f3f4f6;
  display: flex;
  align-items: center;
  justify-content: center;
  
  text {
    font-size: 28px;
  }
}

.doc-info {
  flex: 1;
}

.doc-title {
  font-size: 15px;
  font-weight: 600;
  color: #1f2937;
}

.doc-meta {
  display: flex;
  gap: 8px;
  margin-top: 6px;
  
  text {
    font-size: 13px;
    color: #9ca3af;
  }
}

.doc-status {
  display: flex;
  gap: 12px;
  margin-top: 8px;
}

.status-item {
  padding: 4px 10px;
  border-radius: 4px;
  
  text {
    font-size: 12px;
  }
  
  &.uploading, &.processing {
    background: #fef3c7;
    text { color: #d97706; }
  }
  
  &.ready {
    background: #d1fae5;
    text { color: #065f46; }
  }
  
  &.failed {
    background: #fee2e2;
    text { color: #dc2626; }
  }
  
  &.pending {
    background: #f3f4f6;
    text { color: #6b7280; }
  }
  
  &.indexing {
    background: #dbeafe;
    text { color: #1d4ed8; }
  }
  
  &.completed {
    background: #d1fae5;
    text { color: #065f46; }
  }
}

.doc-actions {
  display: flex;
  gap: 10px;
}

.action-btn {
  padding: 8px 14px;
  border-radius: 6px;
  background: #f3f4f6;
  cursor: pointer;
  transition: all 0.2s;
  
  text {
    font-size: 13px;
    color: #6b7280;
  }
  
  &:hover {
    background: #e5e7eb;
  }
  
  &.danger {
    background: #fee2e2;
    
    text {
      color: #dc2626;
    }
    
    &:hover {
      background: #fecaca;
    }
  }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
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