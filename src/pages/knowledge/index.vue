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

      <!-- Preview Dialog -->
      <view class="dialog-overlay" v-if="showPreview">
        <view class="dialog-mask" @click="showPreview = false"></view>
        <view class="dialog large">
          <view class="dialog-header">
            <text class="dialog-title">{{ previewDocData?.title }}</text>
            <text class="dialog-close" @click="showPreview = false">×</text>
          </view>
          <view class="dialog-body preview-body">
            <scroll-view scroll-y class="preview-scroll">
              <text class="preview-content">{{ previewDocData?.content }}</text>
            </scroll-view>
          </view>
          <view class="dialog-footer">
            <view class="btn secondary" @click="showPreview = false">
              <text>关闭</text>
            </view>
          </view>
        </view>
      </view>

      <!-- Upload Dialog -->
      <view class="dialog-overlay" v-if="showUploadDialog">
        <view class="dialog-mask" @click="showUploadDialog = false"></view>
        <view class="dialog">
          <view class="dialog-header">
            <text class="dialog-title">上传文档</text>
            <text class="dialog-close" @click="showUploadDialog = false">×</text>
          </view>
          <view class="dialog-body">
            <view class="form-group">
              <view class="form-label">文档标题</view>
              <input class="form-input" type="text" v-model="uploadForm.title" placeholder="请输入文档标题" />
            </view>
            <view class="form-group">
              <view class="form-label">分类</view>
              <select class="form-select" v-model="uploadForm.category">
                <option value="logistics">物流</option>
                <option value="customer">客服</option>
                <option value="finance">财务</option>
                <option value="quality">质控</option>
              </select>
            </view>
            <view class="form-group">
              <view class="form-label">选择文件</view>
              <view class="upload-area" @click="chooseFile">
                <text class="upload-icon">📁</text>
                <text class="upload-text">{{ selectedFileName || '点击选择文件' }}</text>
              </view>
            </view>
            <view class="form-group">
              <view class="form-label">文档内容</view>
              <textarea class="form-textarea" v-model="uploadForm.content" placeholder="请输入文档内容..." rows="5"></textarea>
            </view>
          </view>
          <view class="dialog-footer">
            <view class="btn secondary" @click="showUploadDialog = false">
              <text>取消</text>
            </view>
            <view class="btn primary" @click="submitUpload">
              <text>上传</text>
            </view>
          </view>
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
const showPreview = ref(false)
const previewDocData = ref<KnowledgeDocument | null>(null)

const showUploadDialog = ref(false)
const uploadForm = ref({
  title: '',
  category: 'logistics',
  content: '',
  size: 0
})
const selectedFileName = ref('')

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
  showUploadDialog.value = true
}

const chooseFile = () => {
  const input = document.createElement('input')
  input.type = 'file'
  input.accept = '.pdf,.doc,.docx,.txt,.md'
  input.onchange = (e: any) => {
    if (e.target.files && e.target.files[0]) {
      const file = e.target.files[0]
      selectedFileName.value = file.name
      uploadForm.value.size = file.size
      
      const reader = new FileReader()
      reader.onload = (event) => {
        uploadForm.value.content = event.target?.result as string
      }
      reader.readAsText(file)
    }
  }
  input.click()
}

const submitUpload = async () => {
  if (!uploadForm.value.title.trim()) {
    uni.showToast({ title: '请输入文档标题', icon: 'none' })
    return
  }
  try {
    await knowledgeService.create({
      title: uploadForm.value.title,
      category: uploadForm.value.category || 'logistics',
      content: uploadForm.value.content,
      type: 'markdown',
      uploadedBy: 'system',
      size: uploadForm.value.size
    } as any)
    uni.showToast({ title: '上传成功', icon: 'success' })
    showUploadDialog.value = false
    uploadForm.value = { title: '', category: '', content: '', size: 0 }
    selectedFileName.value = ''
    loadDocuments()
  } catch (error) {
    console.error(error)
    uni.showToast({ title: '上传失败', icon: 'none' })
  }
}

onMounted(() => {
  loadCategories()
  loadDocuments()
})

const previewDoc = (doc: KnowledgeDocument) => {
  previewDocData.value = doc
  showPreview.value = true
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

.dialog-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
}

.dialog-mask {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
}

.dialog {
  position: relative;
  background: #fff;
  border-radius: 12px;
  width: 90%;
  max-width: 600px;
  max-height: 80vh;
  overflow: hidden;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);

  &.large {
    max-width: 800px;
  }
}

.dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
}

.dialog-title {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
}

.dialog-close {
  font-size: 24px;
  color: #999;
  cursor: pointer;
  line-height: 1;

  &:hover {
    color: #666;
  }
}

.dialog-body {
  padding: 20px;
}

.preview-body {
  max-height: 60vh;
}

.preview-scroll {
  max-height: 50vh;
}

.preview-content {
  font-size: 14px;
  line-height: 1.8;
  color: #333;
  white-space: pre-wrap;
  word-break: break-all;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-label {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.form-input, .form-select, .form-textarea {
  padding: 10px 12px;
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  font-size: 14px;
  background: #fff;
  color: #333;
  transition: all 0.2s;

  &:focus {
    outline: none;
    border-color: #1890ff;
    box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.1);
  }

  &::placeholder {
    color: #bbb;
  }
}

.form-textarea {
  resize: vertical;
  min-height: 80px;
}

.upload-area {
  width: 100%;
  padding: 30px;
  border: 2px dashed #d9d9d9;
  border-radius: 8px;
  text-align: center;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    border-color: #1890ff;
    background: #f6ffed;
  }
}

.upload-icon {
  font-size: 32px;
  display: block;
  margin-bottom: 8px;
}

.upload-text {
  font-size: 14px;
  color: #666;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 16px 20px;
  border-top: 1px solid #f0f0f0;
}

.btn {
  padding: 8px 20px;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
  border: none;

  &.primary {
    background: #1890ff;
    color: #fff;
    &:hover { background: #40a9ff; }
  }

  &.secondary {
    background: #f5f5f5;
    color: #666;
    &:hover { background: #e8e8e8; }
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 16px 20px;
  border-top: 1px solid #f0f0f0;
}
</style>
