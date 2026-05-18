<template>
  <Layout>
    <view class="page">
      <view class="page-header">
        <text class="page-title">Skills工作台</text>
        <view class="header-actions">
          <view class="btn primary">
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
    </view>
  </Layout>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import Layout from '@/components/Layout.vue'
import { mockSkills, type Skill } from '@/data/skills'

const searchQuery = ref('')
const filterType = ref('')
const selectedSkill = ref<Skill | null>(null)

const filteredSkills = computed(() => {
  let result = [...mockSkills]
  
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
</style>
