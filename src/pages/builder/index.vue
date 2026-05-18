<template>
  <Layout>
    <view class="page">
      <view class="page-header">
        <view class="header-content">
          <text class="page-title">Agent构建器</text>
          <view class="header-actions">
            <view class="btn secondary">
              <text>保存草稿</text>
            </view>
            <view class="btn primary">
              <text>提交审核</text>
            </view>
          </view>
        </view>
      </view>

      <view class="page-content">
        <view class="builder-layout">
          <view class="builder-main">
            <view class="builder-tabs">
              <view 
                class="tab-item" 
                :class="{ active: activeTab === 'basic' }"
                @click="activeTab = 'basic'"
              >
                <text>基础配置</text>
              </view>
              <view 
                class="tab-item" 
                :class="{ active: activeTab === 'prompt' }"
                @click="activeTab = 'prompt'"
              >
                <text>提示词工坊</text>
              </view>
              <view 
                class="tab-item" 
                :class="{ active: activeTab === 'skills' }"
                @click="activeTab = 'skills'"
              >
                <text>技能绑定</text>
              </view>
              <view 
                class="tab-item" 
                :class="{ active: activeTab === 'knowledge' }"
                @click="activeTab = 'knowledge'"
              >
                <text>知识库</text>
              </view>
            </view>

            <view class="builder-content">
              <view class="form-section" v-show="activeTab === 'basic'">
                <view class="form-group">
                  <label class="form-label">Agent名称 <span class="required">*</span></label>
                  <input class="form-input" type="text" v-model="formData.name" placeholder="请输入Agent名称" />
                </view>
                
                <view class="form-group">
                  <label class="form-label">Agent描述 <span class="required">*</span></label>
                  <textarea class="form-textarea" v-model="formData.description" placeholder="请详细描述Agent的功能和使用场景" rows="4"></textarea>
                </view>
                
                <view class="form-row">
                  <view class="form-group">
                    <label class="form-label">所属部门</label>
                    <select class="form-select" v-model="formData.department">
                      <option value="">请选择</option>
                      <option value="operation">运营部</option>
                      <option value="qc">质控部</option>
                      <option value="customer">客服部</option>
                      <option value="finance">财务部</option>
                    </select>
                  </view>
                  
                  <view class="form-group">
                    <label class="form-label">可见范围</label>
                    <select class="form-select" v-model="formData.visibility">
                      <option value="public">全公司</option>
                      <option value="department">本部门</option>
                      <option value="private">私有</option>
                    </select>
                  </view>
                </view>
                
                <view class="form-group">
                  <label class="form-label">标签</label>
                  <view class="tag-input">
                    <view class="tag-item" v-for="tag in formData.tags" :key="tag">
                      <text>{{ tag }}</text>
                      <text class="tag-remove" @click="removeTag(tag)">×</text>
                    </view>
                    <input class="tag-add-input" type="text" placeholder="添加标签" @keyup.enter="addTag" />
                  </view>
                </view>
              </view>

              <view class="form-section" v-show="activeTab === 'prompt'">
                <view class="form-group">
                  <label class="form-label">选择模型</label>
                  <select class="form-select" v-model="formData.model">
                    <option value="qwen-max">qwen-max (旗舰版)</option>
                    <option value="qwen-plus">qwen-plus (增强版)</option>
                    <option value="qwen-turbo">qwen-turbo (快速版)</option>
                  </select>
                </view>
                
                <view class="form-group">
                  <label class="form-label">系统指令 (System Prompt)</label>
                  <textarea class="form-textarea code" v-model="formData.systemPrompt" placeholder="定义Agent的角色、行为规则和约束..." rows="8"></textarea>
                  <text class="form-hint">支持变量引用：{{"{{"}}user_name}}、{{"{{"}}department}} 等</text>
                </view>
                
                <view class="form-group">
                  <label class="form-label">Few-shot 示例</label>
                  <view class="example-list">
                    <view class="example-item" v-for="(example, idx) in formData.examples" :key="idx">
                      <view class="example-header">
                        <text>示例 {{ idx + 1 }}</text>
                        <text class="example-remove" @click="removeExample(idx)">删除</text>
                      </view>
                      <textarea class="form-textarea" v-model="example.input" placeholder="用户输入" rows="2"></textarea>
                      <textarea class="form-textarea" v-model="example.output" placeholder="期望输出" rows="2"></textarea>
                    </view>
                    <view class="add-example-btn" @click="addExample">
                      <text>+ 添加示例</text>
                    </view>
                  </view>
                </view>
              </view>

              <view class="form-section" v-show="activeTab === 'skills'">
                <view class="skills-market">
                  <text class="section-label">从技能市场选择</text>
                  <view class="skills-grid">
                    <view 
                      class="skill-card" 
                      v-for="skill in availableSkills" 
                      :key="skill.id"
                      :class="{ selected: formData.skills.includes(skill.id) }"
                      @click="toggleSkill(skill.id)"
                    >
                      <view class="skill-header">
                        <text class="skill-icon">{{ skill.type === 'api' ? '🌐' : '⚡' }}</text>
                        <text class="skill-name">{{ skill.name }}</text>
                      </view>
                      <text class="skill-desc">{{ skill.description }}</text>
                      <text class="skill-category">{{ skill.category }}</text>
                    </view>
                  </view>
                </view>
                
                <view class="selected-skills" v-if="formData.skills.length > 0">
                  <text class="section-label">已选技能 ({{ formData.skills.length }})</text>
                  <view class="selected-list">
                    <view class="selected-item" v-for="skillId in formData.skills" :key="skillId">
                      <text>{{ getSkillName(skillId) }}</text>
                      <text class="remove-btn" @click="toggleSkill(skillId)">×</text>
                    </view>
                  </view>
                </view>
              </view>

              <view class="form-section" v-show="activeTab === 'knowledge'">
                <view class="form-group">
                  <label class="form-label">关联知识库</label>
                  <view class="knowledge-selector">
                    <view class="knowledge-item" v-for="kb in availableKnowledge" :key="kb.id">
                      <view class="kb-checkbox" :class="{ checked: formData.knowledge.includes(kb.id) }">
                        <text v-if="formData.knowledge.includes(kb.id)">✓</text>
                      </view>
                      <view class="kb-info">
                        <text class="kb-name">{{ kb.name }}</text>
                        <text class="kb-desc">{{ kb.docCount }} 篇文档</text>
                      </view>
                    </view>
                  </view>
                </view>
              </view>
            </view>
          </view>

          <view class="builder-preview">
            <view class="preview-header">
              <text>预览</text>
            </view>
            <view class="preview-content">
              <view class="preview-card">
                <view class="preview-agent-icon">{{ getDeptIcon(formData.department) }}</view>
                <text class="preview-agent-name">{{ formData.name || '未命名Agent' }}</text>
                <text class="preview-agent-desc">{{ formData.description || '请填写Agent描述' }}</text>
                <view class="preview-tags">
                  <text class="tag" v-for="tag in formData.tags" :key="tag">{{ tag }}</text>
                </view>
              </view>
              
              <view class="preview-chat">
                <view class="chat-message bot">
                  <text>你好！我是{{ formData.name || '智能助手' }}，有什么可以帮你的吗？</text>
                </view>
              </view>
            </view>
          </view>
        </view>
      </view>
    </view>
  </Layout>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import Layout from '@/components/Layout.vue'
import { mockSkills } from '@/data/skills'

const activeTab = ref('basic')

const formData = ref({
  name: '',
  description: '',
  department: '',
  visibility: 'public',
  tags: [] as string[],
  model: 'qwen-plus',
  systemPrompt: '',
  examples: [] as { input: string; output: string }[],
  skills: [] as string[],
  knowledge: [] as string[]
})

const availableSkills = mockSkills

const availableKnowledge = [
  { id: '1', name: '物流操作SOP', docCount: 45 },
  { id: '2', name: '客户服务FAQ', docCount: 128 },
  { id: '3', name: '财务报销指南', docCount: 32 }
]

const getDeptIcon = (dept: string) => {
  const icons: Record<string, string> = {
    operation: '🚚',
    qc: '✅',
    customer: '💬',
    finance: '💰'
  }
  return icons[dept] || '🤖'
}

const getSkillName = (id: string) => {
  return mockSkills.find(s => s.id === id)?.name || id
}

const addTag = (e: any) => {
  const value = e.detail.value?.trim()
  if (value && !formData.value.tags.includes(value)) {
    formData.value.tags.push(value)
  }
}

const removeTag = (tag: string) => {
  formData.value.tags = formData.value.tags.filter(t => t !== tag)
}

const addExample = () => {
  formData.value.examples.push({ input: '', output: '' })
}

const removeExample = (idx: number) => {
  formData.value.examples.splice(idx, 1)
}

const toggleSkill = (skillId: string) => {
  const idx = formData.value.skills.indexOf(skillId)
  if (idx === -1) {
    formData.value.skills.push(skillId)
  } else {
    formData.value.skills.splice(idx, 1)
  }
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
  gap: 12px;
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
  }
  
  &.secondary {
    background: #f3f4f6;
    color: #6b7280;
    border: 1px solid #e5e7eb;
  }
}

.page-content {
  padding: 24px 32px;
}

.builder-layout {
  display: flex;
  gap: 24px;
}

.builder-main {
  flex: 1;
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
}

.builder-tabs {
  display: flex;
  border-bottom: 1px solid #e8e8e8;
}

.tab-item {
  padding: 16px 24px;
  cursor: pointer;
  border-bottom: 2px solid transparent;
  transition: all 0.2s;
  
  text {
    font-size: 14px;
    color: #6b7280;
  }
  
  &:hover {
    background: #f9fafb;
  }
  
  &.active {
    border-bottom-color: #4f46e5;
    
    text {
      color: #4f46e5;
      font-weight: 500;
    }
  }
}

.builder-content {
  padding: 24px;
}

.form-section {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-row {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.form-label {
  font-size: 14px;
  font-weight: 500;
  color: #374151;
}

.required {
  color: #ef4444;
}

.form-input, .form-select, .form-textarea {
  padding: 10px 14px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  font-size: 14px;
  background: #fff;
  transition: border-color 0.2s;
  
  &:focus {
    outline: none;
    border-color: #4f46e5;
  }
  
  &::placeholder {
    color: #9ca3af;
  }
}

.form-textarea {
  resize: vertical;
  
  &.code {
    font-family: 'Monaco', 'Menlo', monospace;
    font-size: 13px;
    line-height: 1.6;
  }
}

.form-hint {
  font-size: 12px;
  color: #9ca3af;
}

.tag-input {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 8px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  min-height: 42px;
}

.tag-item {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  background: #eef2ff;
  border-radius: 4px;
  
  text {
    font-size: 13px;
    color: #4f46e5;
  }
}

.tag-remove {
  cursor: pointer;
  color: #6b7280;
  
  &:hover {
    color: #ef4444;
  }
}

.tag-add-input {
  flex: 1;
  min-width: 100px;
  border: none;
  font-size: 13px;
  
  &:focus {
    outline: none;
  }
}

.example-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.example-item {
  padding: 16px;
  background: #f9fafb;
  border-radius: 8px;
  
  .form-textarea {
    margin-top: 8px;
  }
}

.example-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  
  text:first-child {
    font-size: 14px;
    font-weight: 500;
    color: #1f2937;
  }
}

.example-remove {
  font-size: 13px;
  color: #ef4444;
  cursor: pointer;
}

.add-example-btn {
  padding: 12px;
  border: 1px dashed #d1d5db;
  border-radius: 8px;
  text-align: center;
  cursor: pointer;
  
  text {
    font-size: 14px;
    color: #6b7280;
  }
  
  &:hover {
    border-color: #4f46e5;
    
    text {
      color: #4f46e5;
    }
  }
}

.section-label {
  font-size: 14px;
  font-weight: 500;
  color: #374151;
  margin-bottom: 12px;
  display: block;
}

.skills-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.skill-card {
  padding: 14px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  
  &:hover {
    border-color: #4f46e5;
  }
  
  &.selected {
    border-color: #4f46e5;
    background: #eef2ff;
  }
}

.skill-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}

.skill-icon {
  font-size: 16px;
}

.skill-name {
  font-size: 14px;
  font-weight: 500;
  color: #1f2937;
}

.skill-desc {
  font-size: 12px;
  color: #6b7280;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
}

.skill-category {
  font-size: 11px;
  color: #9ca3af;
  margin-top: 6px;
  display: block;
}

.selected-skills {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #e8e8e8;
}

.selected-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.selected-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  background: #4f46e5;
  border-radius: 6px;
  
  text {
    font-size: 13px;
    color: #fff;
  }
}

.remove-btn {
  cursor: pointer;
  font-size: 16px;
  
  &:hover {
    color: #fee2e2;
  }
}

.knowledge-selector {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.knowledge-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  cursor: pointer;
  
  &:hover {
    background: #f9fafb;
  }
}

.kb-checkbox {
  width: 20px;
  height: 20px;
  border: 2px solid #d1d5db;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  
  &.checked {
    background: #4f46e5;
    border-color: #4f46e5;
    
    text {
      color: #fff;
      font-size: 12px;
    }
  }
}

.kb-info {
  display: flex;
  flex-direction: column;
}

.kb-name {
  font-size: 14px;
  font-weight: 500;
  color: #1f2937;
}

.kb-desc {
  font-size: 12px;
  color: #9ca3af;
}

.builder-preview {
  width: 360px;
  flex-shrink: 0;
  background: #fff;
  border-radius: 12px;
}

.preview-header {
  padding: 16px 20px;
  border-bottom: 1px solid #e8e8e8;
  
  text {
    font-size: 14px;
    font-weight: 600;
    color: #1f2937;
  }
}

.preview-content {
  padding: 20px;
}

.preview-card {
  padding: 20px;
  background: linear-gradient(135deg, #eef2ff 0%, #ddd6fe 100%);
  border-radius: 12px;
  text-align: center;
  margin-bottom: 16px;
}

.preview-agent-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%);
  margin: 0 auto 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.preview-agent-name {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  display: block;
  margin-bottom: 6px;
}

.preview-agent-desc {
  font-size: 13px;
  color: #6b7280;
  display: block;
  margin-bottom: 12px;
}

.preview-tags {
  display: flex;
  justify-content: center;
  gap: 6px;
  flex-wrap: wrap;
  
  .tag {
    padding: 3px 8px;
    background: rgba(79, 70, 229, 0.2);
    border-radius: 4px;
    font-size: 11px;
    color: #4f46e5;
  }
}

.preview-chat {
  background: #f9fafb;
  border-radius: 12px;
  padding: 16px;
}

.chat-message {
  max-width: 80%;
  
  &.bot {
    margin-right: auto;
    
    text {
      background: #fff;
      color: #1f2937;
    }
  }
  
  text {
    display: inline-block;
    padding: 10px 14px;
    border-radius: 12px;
    font-size: 13px;
    line-height: 1.5;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  }
}
</style>
