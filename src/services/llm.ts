import { http } from '@/utils/request';

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
  const response = await http.post('/api/llm/chat', params);
  return response;
}

export async function generateSkill(params: SkillGenerateRequest): Promise<SkillGenerateResponse> {
  const response = await http.post('/api/llm/skills/generate', params);
  return response;
}

export async function generateWorkflow(params: WorkflowGenerateRequest): Promise<WorkflowGenerateResponse> {
  const response = await http.post('/api/llm/workflows/generate', params);
  return response;
}

export async function knowledgeQA(params: KnowledgeQARequest): Promise<KnowledgeQAResponse> {
  const response = await http.post('/api/llm/knowledge/qa', params);
  return response;
}

export async function analyzeLogs(params: LogAnalysisRequest): Promise<LogAnalysisResponse> {
  const response = await http.post('/api/llm/logs/analyze', params);
  return response;
}

export async function summarize(text: string, maxLength: number = 200): Promise<string> {
  const response = await http.post('/api/llm/summarize', undefined, { params: { text, maxLength } });
  return response;
}

export async function extractKeywords(text: string, count: number = 10): Promise<string> {
  const response = await http.post('/api/llm/keywords', undefined, { params: { text, count } });
  return response;
}
