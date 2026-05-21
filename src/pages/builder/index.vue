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
                  <view class="form-label">Agent名称 <span class="required">*</span></view>
                  <input class="form-input" type="text" v-model="formData.name" placeholder="请输入Agent名称" />
                </view>

                <view class="form-group">
                  <view class="form-label">Agent描述 <span class="required">*</span></view>
                  <textarea class="form-textarea" v-model="formData.description" placeholder="请详细描述Agent的功能和使用场景" rows="4"></textarea>
                </view>

                <view class="form-row">
                  <view class="form-group">
                    <view class="form-label">所属部门</view>
                    <select class="form-select" v-model="formData.department">
                      <option value="">请选择</option>
                      <option value="operation">运营部</option>
                      <option value="qc">质控部</option>
                      <option value="customer">客服部</option>
                      <option value="finance">财务部</option>
                    </select>
                  </view>

                  <view class="form-group">
                    <view class="form-label">可见范围</view>
                    <select class="form-select" v-model="formData.visibility">
                      <option value="public">全公司</option>
                      <option value="department">本部门</option>
                      <option value="private">私有</option>
                    </select>
                  </view>
                </view>

                <view class="form-group">
                  <view class="form-label">标签</view>
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
                  <view class="form-label">选择模型</view>
                  <select class="form-select" v-model="formData.model">
                    <option value="qwen-max">qwen-max (旗舰版)</option>
                    <option value="qwen-plus">qwen-plus (增强版)</option>
                    <option value="qwen-turbo">qwen-turbo (快速版)</option>
                  </select>
                </view>

                <view class="form-group">
                  <view class="form-label">系统指令 (System Prompt)</view>
                  <textarea class="form-textarea code" v-model="formData.systemPrompt" placeholder="定义Agent的角色、行为规则和约束..." rows="8"></textarea>
                  <text class="form-hint">支持变量引用：{{"{{"}}user_name}}、{{"{{"}}department}} 等</text>
                </view>

                <view class="form-group">
                  <view class="form-label">Few-shot 示例</view>
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
                  <view class="form-label">关联知识库文档</view>
                  <view class="knowledge-selector">
                    <view class="knowledge-item" v-for="kb in availableKnowledge" :key="kb.id" @click="toggleKnowledge(kb.id)">
                      <view class="kb-checkbox" :class="{ checked: formData.knowledge.includes(kb.id) }">
                        <text v-if="formData.knowledge.includes(kb.id)">✓</text>
                      </view>
                      <view class="kb-info">
                        <text class="kb-name">{{ kb.title }}</text>
                        <text class="kb-desc">{{ kb.category }}</text>
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
import { ref, onMounted } from 'vue'
import Layout from '@/components/Layout.vue'
import { skillService } from '@/services/skill'
import { knowledgeService } from '@/services/knowledge'
import { agentService } from '@/services/agent'

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

const availableSkills = ref<any[]>([])
const availableKnowledge = ref<{ id: string; title: string; category: string }[]>([])

const loadSkills = async () => {
  try {
    const response = await skillService.list({ page: 1, size: 100 })
    availableSkills.value = response.list || []
  } catch (error) {
    console.error('Failed to load skills:', error)
  }
}

const loadKnowledge = async () => {
  try {
    const response = await knowledgeService.list({ page: 1, size: 100 })
    if (response && response.list) {
      availableKnowledge.value = response.list.map(doc => ({
        id: doc.id,
        title: doc.title,
        category: doc.category || ''
      }))
    }
  } catch (error) {
    console.error('Failed to load knowledge documents:', error)
  }
}

const agentId = ref('')

const loadAgentData = async (id: string) => {
  try {
    const agent = await agentService.get(id)
    formData.value = {
      name: agent.name || '',
      description: agent.description || '',
      department: agent.department || '',
      visibility: agent.visibility || 'public',
      tags: agent.tags || [],
      model: agent.model || 'qwen-plus',
      systemPrompt: agent.systemPrompt || '',
      examples: agent.examples || [],
      skills: agent.skills || [],
      knowledge: agent.knowledge || []
    }
  } catch (error) {
    console.error('Failed to load agent data:', error)
  }
}

onMounted(() => {
  loadSkills()
  loadKnowledge()
  
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const options = (currentPage as any)?.options || {}
  
  if (options.id) {
    agentId.value = options.id
    loadAgentData(options.id)
  }
})

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
  return availableSkills.value.find(s => s.id === id)?.name || id
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

const toggleKnowledge = (kbId: string) => {
  const idx = formData.value.knowledge.indexOf(kbId)
  if (idx === -1) {
    formData.value.knowledge.push(kbId)
  } else {
    formData.value.knowledge.splice(idx, 1)
  }
}
</script>

<style lang="scss">
.page {
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: flex-end;
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
  gap: 12px;
}

.btn {
  padding: 8px 20px;
  border-radius: 24px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;

  &.primary { background: #1890ff; color: #fff; &:hover { background: #40a9ff; } }
  &.secondary { background: #fff; border: 1px solid #d9d9d9; color: #666; &:hover { border-color: #1890ff; color: #1890ff; } }
}

.page-content {
  // Remove padding as Layout handles it
}

.builder-layout {
  display: flex;
  gap: 24px;
}

.builder-main {
  flex: 1;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  overflow: hidden;
}

.builder-tabs {
  display: flex;
  border-bottom: 1px solid #f0f0f0;
  background: #fafafa;
}

.tab-item {
  padding: 16px 24px;
  cursor: pointer;
  border-bottom: 2px solid transparent;
  transition: all 0.2s;

  text {
    font-size: 14px;
    color: #666;
  }

  &:hover {
    color: #1890ff;
    text { color: #1890ff; }
  }

  &.active {
    border-bottom-color: #1890ff;
    background: #fff;

    text {
      color: #1890ff;
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
  color: #333;
}

.required {
  color: #ff4d4f;
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

  &.code {
    font-family: 'Monaco', 'Menlo', monospace;
    font-size: 13px;
    line-height: 1.6;
  }
}

.form-hint {
  font-size: 12px;
  color: #999;
}

.tag-input {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 8px;
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  min-height: 48px;
  background: #fff;

  &:focus-within {
    border-color: #1890ff;
    box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.1);
  }
}

.tag-item {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 8px;
  background: #e6f7ff;
  border-radius: 4px;
  border: 1px solid #91d5ff;

  text {
    font-size: 12px;
    color: #1890ff;
  }
}

.tag-remove {
  cursor: pointer;
  color: #999;
  font-size: 14px;
  line-height: 1;

  &:hover {
    color: #ff4d4f;
  }
}

.tag-add-input {
  flex: 1;
  min-width: 100px;
  border: none;
  font-size: 13px;
  background: transparent;
  color: #333;

  &:focus {
    outline: none;
  }

  &::placeholder {
    color: #bbb;
  }
}

.example-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.example-item {
  padding: 16px;
  background: #f9f9f9;
  border-radius: 8px;
  border: 1px solid #f0f0f0;

  .form-textarea {
    margin-top: 8px;
    background: #fff;
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
    color: #333;
  }
}

.example-remove {
  font-size: 13px;
  color: #ff4d4f;
  cursor: pointer;
}

.add-example-btn {
  padding: 12px;
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  text-align: center;
  cursor: pointer;

  text {
    font-size: 14px;
    color: #999;
  }

  &:hover {
    border-color: #1890ff;
    text { color: #1890ff; }
  }
}

.section-label {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-bottom: 12px;
  display: block;
}

.skills-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.skill-card {
  padding: 14px;
  border: 1px solid #e8ecf1;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  background: #fff;

  &:hover {
    border-color: #1890ff;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  }

  &.selected {
    border-color: #1890ff;
    background: #e6f7ff;
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
  color: #333;
}

.skill-desc {
  font-size: 12px;
  color: #888;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
}

.skill-category {
  font-size: 11px;
  color: #999;
  margin-top: 6px;
  display: block;
}

.selected-skills {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
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
  padding: 6px 10px;
  background: #e6f7ff;
  border-radius: 4px;
  border: 1px solid #91d5ff;

  text {
    font-size: 13px;
    color: #1890ff;
  }
}

.remove-btn {
  cursor: pointer;
  font-size: 16px;

  &:hover {
    color: #ff4d4f;
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
  border: 1px solid #e8ecf1;
  border-radius: 8px;
  cursor: pointer;
  background: #fff;

  &:hover {
    background: #f9f9f9;
  }
}

.kb-checkbox {
  width: 20px;
  height: 20px;
  border: 2px solid #d9d9d9;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;

  &.checked {
    background: #1890ff;
    border-color: #1890ff;

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
  color: #333;
}

.kb-desc {
  font-size: 12px;
  color: #999;
}

.builder-preview {
  width: 340px;
  flex-shrink: 0;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  position: sticky;
  top: 24px;
}

.preview-header {
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;

  text {
    font-size: 14px;
    font-weight: 600;
    color: #333;
  }
}

.preview-content {
  padding: 20px;
}

.preview-card {
  padding: 20px;
  background: #f0f7ff;
  border-radius: 12px;
  text-align: center;
  margin-bottom: 16px;
  border: 1px solid #bae7ff;
}

.preview-agent-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
  margin: 0 auto 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  box-shadow: 0 4px 10px rgba(24, 144, 255, 0.2);
}

.preview-agent-name {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  display: block;
  margin-bottom: 6px;
}

.preview-agent-desc {
  font-size: 13px;
  color: #666;
  display: block;
  margin-bottom: 12px;
  line-height: 1.4;
}

.preview-tags {
  display: flex;
  justify-content: center;
  gap: 6px;
  flex-wrap: wrap;

  .tag {
    padding: 3px 8px;
    background: #e6f7ff;
    border-radius: 4px;
    font-size: 11px;
    color: #1890ff;
    border: 1px solid #91d5ff;
  }
}

.preview-chat {
  background: #f9f9f9;
  border-radius: 12px;
  padding: 16px;
  border: 1px solid #f0f0f0;
}

.chat-message {
  max-width: 85%;

  &.bot {
    margin-right: auto;

    text {
      background: #fff;
      color: #333;
      border: 1px solid #e8ecf1;
      box-shadow: 0 1px 2px rgba(0,0,0,0.05);
    }
  }

  text {
    display: inline-block;
    padding: 10px 14px;
    border-radius: 12px;
    font-size: 13px;
    line-height: 1.5;
  }
}

input, textarea, select {
  pointer-events: auto;
}
</style>
