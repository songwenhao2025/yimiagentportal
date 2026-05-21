<template>
  <Layout>
    <view class="page">
      <!-- Header -->
      <view class="page-header">
        <text class="page-title">技能管理</text>
        <view class="header-actions">
          <view class="search-box">
            <text class="search-icon">🔍</text>
            <input class="search-input" type="text" v-model="searchQuery" placeholder="搜索技能..." />
          </view>
          <view class="btn primary" @click="showCreateDialog = true">
            <text>+ 创建技能</text>
          </view>
        </view>
      </view>

      <!-- Filters -->
      <view class="filter-bar">
        <view class="filter-chips">
          <view class="chip" :class="{ active: filterType === '' }" @click="filterType = ''">
            <text>全部</text>
          </view>
          <view class="chip" :class="{ active: filterType === 'api' }" @click="filterType = 'api'">
            <text>🌐 API</text>
          </view>
          <view class="chip" :class="{ active: filterType === 'function' }" @click="filterType = 'function'">
            <text>⚡ 函数</text>
          </view>
        </view>
      </view>

      <!-- Skills Grid -->
      <view class="skills-grid">
        <view class="skill-card" v-for="skill in filteredSkills" :key="skill.id">
          <view class="skill-header">
            <view class="skill-type-badge" :class="skill.type">
              <text>{{ skill.type === 'api' ? '🌐 API' : '⚡ 函数' }}</text>
            </view>
            <view class="skill-status" :class="skill.status">
              <text>{{ getStatusText(skill.status) }}</text>
            </view>
          </view>
          <text class="skill-name">{{ skill.name }}</text>
          <text class="skill-desc">{{ skill.description }}</text>
          <view class="skill-meta">
            <text class="meta-tag">{{ skill.category }}</text>
            <text class="meta-tag">v{{ skill.version }}</text>
            <text class="meta-tag">{{ skill.usageCount }}次</text>
          </view>
          <view class="skill-actions">
            <view class="action-btn secondary" @click="openEditDialog(skill)">
              <text>编辑</text>
            </view>
            <view class="action-btn primary" @click="openDetailDialog(skill)">
              <text>详情</text>
            </view>
          </view>
        </view>
      </view>

      <!-- Empty -->
      <view class="empty-state" v-if="filteredSkills.length === 0">
        <text class="empty-icon">⚙️</text>
        <text class="empty-text">暂无技能</text>
      </view>

      <!-- Create Dialog -->
      <view class="dialog-overlay" v-if="showCreateDialog">
        <view class="dialog-mask" @click="showCreateDialog = false"></view>
        <view class="dialog">
          <view class="dialog-header">
            <text class="dialog-title">创建技能</text>
            <text class="dialog-close" @click="showCreateDialog = false">×</text>
          </view>
          <view class="dialog-body">
            <view class="form-group">
              <text class="form-label">技能名称 <text class="required">*</text></text>
              <input class="form-input" type="text" v-model="createFormData.name" placeholder="请输入技能名称" />
            </view>
            <view class="form-group">
              <text class="form-label">技能描述</text>
              <textarea class="form-textarea" v-model="createFormData.description" placeholder="请描述技能功能" rows="3"></textarea>
            </view>
            <view class="form-row">
              <view class="form-group">
                <text class="form-label">技能类型</text>
                <picker class="form-select" mode="selector" :range="skillTypes" range-key="label" :value="createTypeIndex" @change="onTypeChange">
                  <view class="select-value">{{ skillTypes[createTypeIndex].label }}</view>
                </picker>
              </view>
              <view class="form-group">
                <text class="form-label">分类</text>
                <picker class="form-select" mode="selector" :range="skillCategories" range-key="label" :value="createCategoryIndex" @change="onCategoryChange">
                  <view class="select-value">{{ skillCategories[createCategoryIndex].label }}</view>
                </picker>
              </view>
            </view>
            <view class="form-row">
              <view class="form-group">
                <text class="form-label">超时时间(秒)</text>
                <input class="form-input" type="number" v-model.number="createFormData.timeout" placeholder="30" />
              </view>
              <view class="form-group" v-if="createFormData.type === 'api'">
                <text class="form-label">API地址</text>
                <input class="form-input" type="text" v-model="createFormData.apiEndpoint" placeholder="https://..." />
              </view>
            </view>
          </view>
          <view class="dialog-footer">
            <view class="btn secondary" @click="showCreateDialog = false">
              <text>取消</text>
            </view>
            <view class="btn primary" @click="createSkill">
              <text>创建</text>
            </view>
          </view>
        </view>
      </view>

      <!-- Edit Dialog -->
      <view class="dialog-overlay" v-if="showEditDialog">
        <view class="dialog-mask" @click="showEditDialog = false"></view>
        <view class="dialog">
          <view class="dialog-header">
            <text class="dialog-title">编辑技能</text>
            <text class="dialog-close" @click="showEditDialog = false">×</text>
          </view>
          <view class="dialog-body">
            <view class="form-group">
              <text class="form-label">技能名称 <text class="required">*</text></text>
              <input class="form-input" type="text" v-model="createFormData.name" placeholder="请输入技能名称" />
            </view>
            <view class="form-group">
              <text class="form-label">技能描述</text>
              <textarea class="form-textarea" v-model="createFormData.description" placeholder="请描述技能功能" rows="3"></textarea>
            </view>
            <view class="form-row">
              <view class="form-group">
                <text class="form-label">技能类型</text>
                <picker class="form-select" mode="selector" :range="skillTypes" range-key="label" :value="createTypeIndex" @change="onTypeChange">
                  <view class="select-value">{{ skillTypes[createTypeIndex].label }}</view>
                </picker>
              </view>
              <view class="form-group">
                <text class="form-label">分类</text>
                <picker class="form-select" mode="selector" :range="skillCategories" range-key="label" :value="createCategoryIndex" @change="onCategoryChange">
                  <view class="select-value">{{ skillCategories[createCategoryIndex].label }}</view>
                </picker>
              </view>
            </view>
            <view class="form-row">
              <view class="form-group">
                <text class="form-label">超时时间(秒)</text>
                <input class="form-input" type="number" v-model.number="createFormData.timeout" placeholder="30" />
              </view>
              <view class="form-group" v-if="createFormData.type === 'api'">
                <text class="form-label">API地址</text>
                <input class="form-input" type="text" v-model="createFormData.apiEndpoint" placeholder="https://..." />
              </view>
            </view>
          </view>
          <view class="dialog-footer">
            <view class="btn secondary" @click="showCreateDialog = false">
              <text>取消</text>
            </view>
            <view class="btn primary" @click="createSkill">
              <text>保存</text>
            </view>
          </view>
        </view>
      </view>

      <!-- Detail Dialog -->
      <view class="dialog-overlay" v-if="showDetailDialog">
        <view class="dialog-mask" @click="showDetailDialog = false"></view>
        <view class="dialog">
          <view class="dialog-header">
            <text class="dialog-title">技能详情</text>
            <text class="dialog-close" @click="showDetailDialog = false">×</text>
          </view>
          <view class="dialog-body">
            <view class="detail-item">
              <text class="detail-label">技能名称</text>
              <text class="detail-value">{{ currentSkill?.name }}</text>
            </view>
            <view class="detail-item">
              <text class="detail-label">技能类型</text>
              <text class="detail-value">{{ currentSkill?.type === 'api' ? 'API' : '函数' }}</text>
            </view>
            <view class="detail-item">
              <text class="detail-label">分类</text>
              <text class="detail-value">{{ currentSkill?.category }}</text>
            </view>
            <view class="detail-item">
              <text class="detail-label">版本</text>
              <text class="detail-value">v{{ currentSkill?.version }}</text>
            </view>
            <view class="detail-item">
              <text class="detail-label">状态</text>
              <text class="detail-value">{{ getStatusText(currentSkill?.status || '') }}</text>
            </view>
            <view class="detail-item">
              <text class="detail-label">使用次数</text>
              <text class="detail-value">{{ currentSkill?.usageCount }}次</text>
            </view>
            <view class="detail-item">
              <text class="detail-label">创建时间</text>
              <text class="detail-value">{{ currentSkill?.createdAt }}</text>
            </view>
            <view class="detail-item" v-if="currentSkill?.description">
              <text class="detail-label">描述</text>
              <text class="detail-value">{{ currentSkill?.description }}</text>
            </view>
          </view>
          <view class="dialog-footer">
            <view class="btn danger" @click="deleteSkill">
              <text>删除</text>
            </view>
            <view class="btn secondary" @click="showDetailDialog = false">
              <text>关闭</text>
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
import { skillService } from '@/services/skill'
import type { Skill } from '@/data/skills'

const searchQuery = ref('')
const filterType = ref('')
const skills = ref<Skill[]>([])
const loading = ref(true)
const showCreateDialog = ref(false)
const showEditDialog = ref(false)
const showDetailDialog = ref(false)
const currentSkill = ref<Skill | null>(null)

const skillTypes = [{ label: 'API', value: 'api' }, { label: '函数', value: 'function' }]
const skillCategories = [
  { label: '物流', value: '物流' },
  { label: '客服', value: '客服' },
  { label: '财务', value: '财务' },
  { label: '质控', value: '质控' }
]

const createTypeIndex = ref(0)
const createCategoryIndex = ref(0)

const createFormData = ref({
  name: '',
  description: '',
  type: 'api' as 'api' | 'function',
  category: '物流',
  timeout: 30,
  apiEndpoint: ''
})

const onTypeChange = (e: any) => {
  createTypeIndex.value = e.detail.value
  createFormData.value.type = skillTypes[e.detail.value].value
}

const onCategoryChange = (e: any) => {
  createCategoryIndex.value = e.detail.value
  createFormData.value.category = skillCategories[e.detail.value].value
}

const filteredSkills = computed(() => {
  let result = [...skills.value]
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    result = result.filter(s => s.name.toLowerCase().includes(query) || s.description.toLowerCase().includes(query))
  }
  if (filterType.value) {
    result = result.filter(s => s.type === filterType.value)
  }
  return result
})

const getStatusText = (status: string) => {
  const map: Record<string, string> = { published: '已发布', draft: '草稿', review: '审核中' }
  return map[status] || status
}

const loadSkills = async () => {
  loading.value = true
  try {
    const res = await skillService.list({ page: 1, size: 100 })
    skills.value = res.list || []
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const createSkill = async () => {
  if (!createFormData.value.name.trim()) {
    uni.showToast({ title: '请输入技能名称', icon: 'none' })
    return
  }
  try {
    await skillService.create(createFormData.value as any)
    uni.showToast({ title: '创建成功', icon: 'success' })
    showCreateDialog.value = false
    createFormData.value = { name: '', description: '', type: 'api', category: '物流', timeout: 30, apiEndpoint: '' }
    loadSkills()
  } catch (error) {
    console.error(error)
  }
}

const openEditDialog = (skill: Skill) => {
  currentSkill.value = skill
  createFormData.value = {
    name: skill.name,
    description: skill.description || '',
    type: skill.type as 'api' | 'function',
    category: skill.category || '物流',
    timeout: skill.timeout || 30,
    apiEndpoint: skill.apiEndpoint || ''
  }
  showEditDialog.value = true
}

const openDetailDialog = (skill: Skill) => {
  currentSkill.value = skill
  showDetailDialog.value = true
}

const updateSkill = async () => {
  if (!currentSkill.value || !createFormData.value.name.trim()) {
    uni.showToast({ title: '请输入技能名称', icon: 'none' })
    return
  }
  try {
    await skillService.update(currentSkill.value.id, createFormData.value as any)
    uni.showToast({ title: '更新成功', icon: 'success' })
    showEditDialog.value = false
    loadSkills()
  } catch (error) {
    console.error(error)
  }
}

const deleteSkill = async () => {
  if (!currentSkill.value) return
  try {
    await skillService.delete(currentSkill.value.id)
    uni.showToast({ title: '删除成功', icon: 'success' })
    showDetailDialog.value = false
    loadSkills()
  } catch (error) {
    console.error(error)
  }
}

onMounted(() => {
  loadSkills()
})
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
  &.secondary { background: #fff; border: 1px solid #d9d9d9; color: #666; &:hover { border-color: #1890ff; color: #1890ff; } }
}

.filter-bar {
  margin-bottom: 24px;
  display: flex;
  gap: 16px;
}

.filter-chips {
  display: flex;
  gap: 8px;
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

.skills-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.skill-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  transition: all 0.3s;

  &:hover { transform: translateY(-4px); box-shadow: 0 8px 20px rgba(0, 0, 0, 0.08); }
}

.skill-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
}

.skill-type-badge {
  padding: 4px 8px;
  border-radius: 6px;
  font-size: 11px;

  &.api { background: #e6f7ff; color: #1890ff; }
  &.function { background: #f6ffed; color: #52c41a; }
}

.skill-status {
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 11px;

  &.published { background: #f6ffed; color: #52c41a; }
  &.draft { background: #f5f5f5; color: #999; }
  &.review { background: #fffbe6; color: #faad14; }
}

.skill-name { font-size: 16px; font-weight: 600; color: #1a1a1a; margin-bottom: 8px; }
.skill-desc { font-size: 13px; color: #888; line-height: 1.5; margin-bottom: 12px; display: block; }

.skill-meta {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}

.meta-tag {
  padding: 4px 8px;
  border-radius: 4px;
  background: #f0f2f5;
  font-size: 11px;
  color: #666;
}

.skill-actions {
  display: flex;
  gap: 8px;
  border-top: 1px solid #f0f0f0;
  padding-top: 12px;
}

.action-btn {
  flex: 1;
  padding: 8px;
  border-radius: 6px;
  text-align: center;
  font-size: 13px;
  cursor: pointer;

  &.primary { background: #1890ff; color: #fff; }
  &.secondary { background: #f5f5f5; color: #666; }
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

/* Dialog */
.dialog-overlay {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.dialog-mask {
  position: absolute;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0, 0, 0, 0.45);
}

.dialog {
  position: relative;
  z-index: 1;
  background: #fff;
  border-radius: 12px;
  width: 500px;
  max-width: 90vw;
}

.dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  border-bottom: 1px solid #f0f0f0;
}

.dialog-title { font-size: 16px; font-weight: 600; color: #1a1a1a; }
.dialog-close { font-size: 24px; color: #999; cursor: pointer; }

.dialog-body { padding: 24px; }

.form-group { margin-bottom: 16px; }
.form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }

.form-label { font-size: 14px; font-weight: 500; color: #333; margin-bottom: 8px; display: block; }
.required { color: #ff4d4f; }

.form-input, .form-textarea {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  font-size: 14px;
  background: #fff;

  &:focus { border-color: #1890ff; outline: none; }
}

.form-textarea { resize: vertical; min-height: 80px; }

.form-select {
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  background: #fff;
}

.select-value {
  padding: 8px 12px;
  font-size: 14px;
  color: #333;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 16px 24px;
  border-top: 1px solid #f0f0f0;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  padding: 12px 0;
  border-bottom: 1px solid #f5f5f5;
  
  &:last-child { border-bottom: none; }
}

.detail-label {
  font-size: 14px;
  color: #888;
}

.detail-value {
  font-size: 14px;
  color: #333;
  font-weight: 500;
  text-align: right;
  max-width: 60%;
}

.btn.danger {
  background: #fff1f0;
  border: 1px solid #ffa39e;
  color: #ff4d4f;
  
  &:hover {
    background: #ff4d4f;
    color: #fff;
  }
}
</style>
