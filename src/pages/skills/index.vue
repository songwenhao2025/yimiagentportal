<template>
  <Layout>
    <view class="page">
      <view class="page-header">
        <text class="page-title">Skills工作台</text>
        <view class="header-actions">
          <view class="btn primary" @click="showCreateDialog = true">
            <text>+ 创建技能</text>
          </view>
        </view>
      </view>

      <view class="page-content">
        <view class="content-layout">
          <view class="skills-list">
            <view class="list-header">
              <view class="search-box">
                <text class="search-icon">🔍</text>
                <input class="search-input" type="text" v-model="searchQuery" placeholder="搜索技能..." />
              </view>
              <view class="filter-tabs">
                <view class="tab" :class="{ active: filterType === '' }" @click="filterType = ''">
                  <text>全部</text>
                </view>
                <view class="tab" :class="{ active: filterType === 'api' }" @click="filterType = 'api'">
                  <text>🌐 API</text>
                </view>
                <view class="tab" :class="{ active: filterType === 'function' }" @click="filterType = 'function'">
                  <text>⚡ 函数</text>
                </view>
              </view>
            </view>

            <view class="skills-grid">
              <view class="skill-card" v-for="skill in filteredSkills" :key="skill.id">
                <view class="skill-header">
                  <view class="skill-type" :class="skill.type">
                    <text>{{ skill.type === 'api' ? '🌐 API' : '⚡ 函数' }}</text>
                  </view>
                  <view class="skill-status" :class="skill.status">
                    <text>{{ getStatusText(skill.status) }}</text>
                  </view>
                </view>

                <text class="skill-name">{{ skill.name }}</text>
                <text class="skill-desc">{{ skill.description }}</text>

                <view class="skill-meta">
                  <text class="meta-item">{{ skill.category }}</text>
                  <text class="meta-item">v{{ skill.version }}</text>
                  <text class="meta-item">{{ skill.usageCount }}次调用</text>
                </view>

                <view class="skill-actions">
                  <view class="action-btn" @click="testSkill(skill)">
                    <text>测试</text>
                  </view>
                  <view class="action-btn primary">
                    <text>编辑</text>
                  </view>
                </view>
              </view>
            </view>
          </view>

          <view class="skill-detail">
            <view class="detail-header">
              <text class="detail-title">技能详情</text>
            </view>
            <view class="detail-content" v-if="selectedSkill">
              <view class="detail-section">
                <text class="section-label">接口定义</text>
                <view class="code-block">
                  <text class="code">{{ selectedSkill.apiEndpoint || 'cloud_function://' + selectedSkill.name.toLowerCase().replace(/\s/g, '_') }}</text>
                </view>
              </view>

              <view class="detail-section">
                <text class="section-label">参数说明</text>
                <view class="param-list">
                  <view class="param-item" v-for="param in selectedSkill.parameters" :key="param.name">
                    <text class="param-name">{{ param.name }}</text>
                    <text class="param-type">{{ param.type }}</text>
                    <text class="param-required" v-if="param.required">必填</text>
                    <text class="param-desc">{{ param.description }}</text>
                  </view>
                </view>
              </view>
            </view>
            <view class="detail-empty" v-else>
              <text>选择技能查看详情</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 创建技能弹窗 -->
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
              <textarea class="form-textarea" v-model="createFormData.description" placeholder="请描述技能功能" :maxlength="200" />
            </view>
            <view class="form-row">
              <view class="form-group">
                <text class="form-label">技能类型</text>
                <picker class="form-picker" mode="selector" :range="skillTypes" range-key="label" :value="createTypeIndex" @change="onTypeChange">
                  <view class="picker-value">{{ skillTypes[createTypeIndex].label }}</view>
                </picker>
              </view>
              <view class="form-group">
                <text class="form-label">分类</text>
                <picker class="form-picker" mode="selector" :range="skillCategories" range-key="label" :value="createCategoryIndex" @change="onCategoryChange">
                  <view class="picker-value">{{ skillCategories[createCategoryIndex].label }}</view>
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
const selectedSkill = ref<Skill | null>(null)
const skills = ref<Skill[]>([])
const loading = ref(true)
const showCreateDialog = ref(false)

const skillTypes = [{ label: 'API', value: 'api' }, { label: '函数', value: 'function' }]
const skillCategories = [
  { label: '物流', value: '物流' },
  { label: '客服', value: '客服' },
  { label: '财务', value: '财务' },
  { label: '质控', value: '质控' }
]
const createTypeIndex = ref(0)
const createCategoryIndex = ref(0)

const onTypeChange = (e: any) => {
  createTypeIndex.value = e.detail.value
  createFormData.value.type = skillTypes[e.detail.value].value
}

const onCategoryChange = (e: any) => {
  createCategoryIndex.value = e.detail.value
  createFormData.value.category = skillCategories[e.detail.value].value
}

const createFormData = ref({
  name: '',
  description: '',
  type: 'api' as 'api' | 'function',
  category: '物流',
  timeout: 30,
  apiEndpoint: ''
})

const filteredSkills = computed(() => {
  let result = [...skills.value]

  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    result = result.filter(s =>
      s.name.toLowerCase().includes(query) ||
      s.description.toLowerCase().includes(query)
    )
  }

  if (filterType.value) {
    result = result.filter(s => s.type === filterType.value)
  }

  return result
})

const getStatusText = (status: string) => {
  const texts: Record<string, string> = {
    active: '已发布',
    draft: '草稿',
    deprecated: '已废弃'
  }
  return texts[status] || status
}

const testSkill = (skill: Skill) => {
  selectedSkill.value = skill
  uni.showToast({ title: `测试: ${skill.name}`, icon: 'none' })
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
    createFormData.value = {
      name: '',
      description: '',
      type: 'api',
      category: '物流',
      timeout: 30,
      apiEndpoint: ''
    }
    loadSkills()
  } catch (error) {
    console.error('Failed to create skill:', error)
  }
}

const loadSkills = async () => {
  loading.value = true
  try {
    const params: any = {}
    if (searchQuery.value) params.keyword = searchQuery.value
    if (filterType.value) params.type = filterType.value
    const response = await skillService.list(params)
    skills.value = response.list || []
  } catch (error) {
    console.error('Failed to load skills:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadSkills()
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
  gap: 12px;
}

.btn {
  padding: 10px 20px;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;

  &.primary {
    background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%);
    color: #fff;
  }
}

.page-content {
  padding: 24px 32px;
}

.content-layout {
  display: flex;
  gap: 24px;
}

.skills-list {
  flex: 1;
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  gap: 16px;
}

.search-box {
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: 8px;
  padding: 10px 16px;
  flex: 1;
  max-width: 300px;
}

.search-icon {
  margin-right: 10px;
}

.search-input {
  flex: 1;
  font-size: 14px;
}

.filter-tabs {
  display: flex;
  gap: 8px;
}

.tab {
  padding: 8px 16px;
  background: #fff;
  border-radius: 6px;
  cursor: pointer;

  text {
    font-size: 13px;
    color: #6b7280;
  }

  &.active {
    background: #4f46e5;

    text {
      color: #fff;
    }
  }
}

.skills-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.skill-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
}

.skill-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
}

.skill-type {
  padding: 4px 10px;
  border-radius: 4px;
  font-size: 12px;

  &.api {
    background: #dbeafe;
    color: #1d4ed8;
  }

  &.function {
    background: #fef3c7;
    color: #b45309;
  }
}

.skill-status {
  padding: 4px 10px;
  border-radius: 4px;
  font-size: 12px;

  &.active {
    background: #d1fae5;
    color: #065f46;
  }

  &.draft {
    background: #f3f4f6;
    color: #6b7280;
  }
}

.skill-name {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  display: block;
  margin-bottom: 6px;
}

.skill-desc {
  font-size: 13px;
  color: #6b7280;
  display: block;
  margin-bottom: 12px;
  line-height: 1.5;
}

.skill-meta {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;

  .meta-item {
    font-size: 12px;
    color: #9ca3af;

    &::before {
      content: '·';
      margin-right: 12px;
    }

    &:first-child::before {
      content: '';
      margin-right: 0;
    }
  }
}

.skill-actions {
  display: flex;
  gap: 8px;
}

.action-btn {
  flex: 1;
  padding: 8px;
  background: #f3f4f6;
  border-radius: 6px;
  text-align: center;

  text {
    font-size: 13px;
    color: #6b7280;
  }

  &.primary {
    background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%);

    text {
      color: #fff;
    }
  }
}

.skill-detail {
  width: 400px;
  flex-shrink: 0;
  background: #fff;
  border-radius: 12px;
}

.detail-header {
  padding: 16px 20px;
  border-bottom: 1px solid #e8e8e8;
}

.detail-title {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
}

.detail-content {
  padding: 20px;
}

.detail-section {
  margin-bottom: 20px;
}

.section-label {
  font-size: 14px;
  font-weight: 500;
  color: #374151;
  display: block;
  margin-bottom: 10px;
}

.code-block {
  background: #1f2937;
  border-radius: 8px;
  padding: 14px;

  .code {
    font-family: 'Monaco', 'Menlo', monospace;
    font-size: 13px;
    color: #10b981;
  }
}

.param-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.param-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px;
  background: #f9fafb;
  border-radius: 6px;
}

.param-name {
  font-size: 13px;
  font-weight: 500;
  color: #4f46e5;
  font-family: 'Monaco', 'Menlo', monospace;
}

.param-type {
  font-size: 12px;
  color: #6b7280;
  background: #e5e7eb;
  padding: 2px 8px;
  border-radius: 4px;
}

.param-required {
  font-size: 11px;
  color: #dc2626;
  background: #fee2e2;
  padding: 2px 6px;
  border-radius: 4px;
}

.param-desc {
  font-size: 12px;
  color: #9ca3af;
  flex: 1;
}

.detail-empty {
  padding: 60px 20px;
  text-align: center;

  text {
    font-size: 14px;
    color: #9ca3af;
  }
}

.dialog-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}

.dialog-mask {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 0;
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
  padding: 16px 20px;
  border-bottom: 1px solid #e8e8e8;
}

.dialog-title {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
}

.dialog-close {
  font-size: 24px;
  color: #6b7280;
  cursor: pointer;

  &:hover {
    color: #1f2937;
  }
}

.dialog-body {
  padding: 20px;
}

.form-row {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 16px;
}

.form-label {
  font-size: 14px;
  font-weight: 500;
  color: #374151;
}

.required {
  color: #ef4444;
}

.form-input, .form-picker, .form-textarea {
  padding: 10px 14px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  font-size: 14px;
  background: #fff;

  &:focus {
    outline: none;
    border-color: #4f46e5;
  }
}

.form-textarea {
  resize: vertical;
  min-height: 80px;
}

.form-picker {
  cursor: pointer;
}

.picker-value {
  color: #1f2937;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 16px 20px;
  border-top: 1px solid #e8e8e8;
}

.btn {
  padding: 10px 20px;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;

  &.primary {
    background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%);
    color: #fff;
  }

  &.secondary {
    background: #f3f4f6;
    color: #6b7280;
    border: 1px solid #e5e7eb;
  }
}
</style>
