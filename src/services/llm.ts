import request from '@/utils/request';

export interface Message {
  role: string;
  content: string;
}

export interface ChatRequest {
  model?: string;
  temperature?: number;
  maxTokens?: number;
  messages: Message[];
}

export interface ChatResponse {
  id: string;
  model: string;
  choices: {
    index: number;
    message: Message;
    finishReason: string;
  }[];
  usage: {
    promptTokens: number;
    completionTokens: number;
    totalTokens: number;
  };
}

export interface SkillGenerateRequest {
  description: string;
  category: string;
  inputExample?: string;
  outputExample?: string;
}

export interface SkillGenerateResponse {
  name: string;
  description: string;
  category: string;
  parameters: {
    name: string;
    type: string;
    description: string;
    required: boolean;
  }[];
  code: string;
}

export interface WorkflowGenerateRequest {
  description: string;
  triggerType?: string;
  requiredSkills?: string[];
}

export interface WorkflowGenerateResponse {
  name: string;
  description: string;
  triggerType: string;
  nodes: {
    id: string;
    type: string;
    skillId?: string;
    skillName: string;
    description: string;
    positionX: number;
    positionY: number;
  }[];
  edges: {
    id: string;
    source: string;
    target: string;
    condition?: string;
  }[];
}

export interface KnowledgeQARequest {
  question: string;
  documents: string[];
  maxTokens?: number;
}

export interface KnowledgeQAResponse {
  answer: string;
  references: {
    documentIndex: number;
    contentSnippet: string;
    relevanceScore: number;
  }[];
  confidence: number;
}

export interface LogAnalysisRequest {
  logs: string[];
  analysisType?: string;
}

export interface LogAnalysisResponse {
  summary: string;
  issues: {
    type: string;
    description: string;
    severity: string;
    count: number;
  }[];
  suggestions: string[];
}

export async function chat(params: ChatRequest): Promise<ChatResponse> {
  const response = await request({
    url: '/api/llm/chat',
    method: 'POST',
    data: params,
  });
  return response.data;
}

export async function generateSkill(params: SkillGenerateRequest): Promise<SkillGenerateResponse> {
  const response = await request({
    url: '/api/llm/skills/generate',
    method: 'POST',
    data: params,
  });
  return response.data;
}

export async function generateWorkflow(params: WorkflowGenerateRequest): Promise<WorkflowGenerateResponse> {
  const response = await request({
    url: '/api/llm/workflows/generate',
    method: 'POST',
    data: params,
  });
  return response.data;
}

export async function knowledgeQA(params: KnowledgeQARequest): Promise<KnowledgeQAResponse> {
  const response = await request({
    url: '/api/llm/knowledge/qa',
    method: 'POST',
    data: params,
  });
  return response.data;
}

export async function analyzeLogs(params: LogAnalysisRequest): Promise<LogAnalysisResponse> {
  const response = await request({
    url: '/api/llm/logs/analyze',
    method: 'POST',
    data: params,
  });
  return response.data;
}

export async function summarize(text: string, maxLength: number = 200): Promise<string> {
  const response = await request({
    url: '/api/llm/summarize',
    method: 'POST',
    params: {
      text,
      maxLength,
    },
  });
  return response.data;
}

export async function extractKeywords(text: string, count: number = 10): Promise<string> {
  const response = await request({
    url: '/api/llm/keywords',
    method: 'POST',
    params: {
      text,
      count,
    },
  });
  return response.data;
}
