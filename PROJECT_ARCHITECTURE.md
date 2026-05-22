# 壹米AI Agent门户 — 项目架构文档

> 生成时间: 2026-05-19
> 最后更新: 2026-05-21

---

## 目录

1. [系统总览](#1-系统总览)
2. [技术栈](#2-技术栈)
3. [整体架构](#3-整体架构)
4. [数据库设计](#4-数据库设计)
5. [后端服务详解](#5-后端服务详解)
6. [前端架构](#6-前端架构)
7. [核心调用链路](#7-核心调用链路)
8. [关键设计决策与现状](#8-关键设计决策与现状)

---

## 1. 系统总览

**壹米AI Agent门户** 是一个面向物流快递行业的 AI Agent 管理平台，提供 Agent 的创建、管理、调用、技能编排、工作流执行、知识库问答、监控分析等功能。

系统采用 **前后端分离 + 后端微服务** 架构：
- **后端**: 8 个 Spring Boot 微服务模块（共享同一个 Maven 多模块项目），运行在 7 个不同端口（8081-8087）
- **前端**: uni-app + Vue 3 + TypeScript 跨平台应用（支持 H5、微信小程序等）
- **数据库**: 单一 MySQL 数据库（`yimi_ai_portal`），13 张核心表
- **AI 能力**: 通过 LLM 服务接入 Anthropic Claude 模型（默认代理到本地 `localhost:15721`）

---

## 2. 技术栈

### 后端
| 技术 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 3.2.0 | 应用框架 |
| Spring Cloud | 2023.0.0 | 微服务生态（引入但未激活使用） |
| Spring Security | 6.2.0 | JWT 认证 + RBAC 权限控制 |
| Spring Data JPA / Hibernate | 6.3.1 | ORM 数据访问 |
| Java | 21 | 编程语言 |
| Maven | - | 构建工具（多模块） |
| Lombok | 1.18.38 | 代码简化 |
| JJWT | 0.12.3 | JWT 令牌生成与验证 |
| Anthropic Java SDK | - | Claude API 调用 |
| Jackson | - | JSON 序列化 |
| BCrypt | - | 密码加密 |

### 前端
| 技术 | 版本 | 用途 |
|------|------|------|
| Vue | 3.4 | UI 框架（Composition API） |
| uni-app | - | 跨平台框架（H5 / 微信小程序） |
| TypeScript | 4.9 | 类型系统 |
| Vite | 5.2.8 | 构建工具 + 开发代理 |
| Axios | 1.16 | HTTP 客户端 |
| vue-i18n | 9.1.9 | 国际化 |
| SCSS | - | CSS 预处理器 |

### 基础设施
| 组件 | 说明 |
|------|------|
| MySQL | `10.206.20.129:3306`，数据库 `yimi_ai_portal` |
| Anthropic API | 默认指向本地代理 `localhost:15721`（可配置为真实 API） |

---

## 3. 整体架构

### 3.1 模块依赖关系

```
                        ┌─────────────────┐
                        │    common       │  ← 共享模块
                        │  (entities,     │     实体/枚举/异常/响应/JWT/Filter
                        │   enums, util)  │
                        └────────┬────────┘
                                 │ (Maven依赖)
         ┌───────┬──────────────┼───────┬───────┬───────┬───────┐
         ▼       ▼       ▼       ▼       ▼       ▼       ▼       ▼
     ┌──────┐┌──────┐┌──────┐┌──────┐┌──────┐┌──────┐┌──────┐┌──────┐
     │auth  ││agent ││skill ││work- ││know- ││admin ││llm   │
     │:8081 ││:8082 ││:8083 ││flow  ││ledge ││:8086 ││:8087 │
     │      ││      ││      ││:8084 ││:8085 ││      ││      │
     └──────┘└──────┘└──────┘└──────┘└──────┘└──────┘└──────┘
         │       │       │       │       │       │       │
         └───────┴───────┴───────┴───────┴───────┴───────┘
                                 │
                         ┌───────┴───────┐
                         │    MySQL      │
                         │ yimi_ai_portal│
                         └───────────────
```

### 3.2 服务职责划分

| 服务 | 端口 | 数据库表 | 核心职责 |
|------|------|----------|----------|
| **auth-service** | 8081 | users | 用户认证、登录、用户 CRUD、JWT 管理 |
| **agent-service** | 8082 | agents, agent_call_logs, agent_ratings, agent_favorites | Agent CRUD、调用（接入 LLM）、评分、收藏、调用日志 |
| **skill-service** | 8083 | skills | 技能定义管理、发布、执行 |
| **workflow-service** | 8084 | workflows, workflow_nodes, workflow_edges, workflow_executions | 工作流定义、可视化编排、节点图执行引擎、执行记录 |
| **knowledge-service** | 8085 | knowledge_documents | 知识文档管理、检索、文件上传 |
| **admin-service** | 8086 | audit_logs, cost_records | 审计日志、费用统计、监控 Dashboard（趋势/排行） |
| **llm-service** | 8087 | 无（无状态） | Claude AI 能力输出（聊天、生成、问答、分析） |
| **common** | - | 无 | 共享实体、枚举、转换器、异常处理、JWT、Security Filter |

### 3.3 服务间通信

各服务之间通过 HTTP 调用进行通信：
1. **agent-service → llm-service**: Agent 调用时通过 RestTemplate 调用 `POST /api/llm/chat`
2. **workflow-service → agent-service**: 工作流节点执行时调用 `POST /api/agents/{id}/call`
3. **admin-service → skill-service**: 技能排行榜通过 RestTemplate 调用 skill-service API
4. **agent-service → skill-service**: Agent 调用时并行调用技能执行 `POST /api/skills/{id}/execute`
5. **agent-service → knowledge-service**: Agent 调用时并行检索知识库 `GET /api/knowledge/search`

此外：
- **共享 common 模块**（Maven 依赖）：所有实体类、枚举、JPA Converter、异常定义统一维护
- **共享数据库**：各服务操作不同的表，通过外键关联
- **独立对外暴露 REST API**：前端通过 Vite 开发代理（或 Nginx 生产代理）将不同 `/api/*` 前缀路由到对应服务

### 3.4 前端 API 代理路由

```
前端请求                        Vite代理                  后端服务
/api/auth/*                 →   localhost:8081        auth-service
/api/users/*                →   localhost:8081        auth-service
/api/agents/*               →   localhost:8082        agent-service
/api/agent-call-logs/*      →   localhost:8082        agent-service
/api/skills/*               →   localhost:8083        skill-service
/api/workflows/*            →   localhost:8084        workflow-service
/api/workflow-executions/*  →   localhost:8084        workflow-service
/api/knowledge/*            →   localhost:8085        knowledge-service
/api/admin/*                →   localhost:8086        admin-service
/api/llm/*                  →   localhost:8087        llm-service
```

---

## 4. 数据库设计

### 4.1 表结构总览

```
┌─────────────────────────────────────────────────────────────────┐
│                      用户与权限                                  │
─────────────────────────────────────────────────────────────────┤
│  users                                                          │
│  ┌──────────────────────────────────────────────┐               │
│  │ id(PK) | name | email(UQ) | phone(UQ)        │               │
│  │ password | role | department | status         │               │
│  │ created_at | updated_at                       │               │
│  └──────────────────────────────────────────────┘               │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                      Agent 核心                                  │
├─────────────────────────────────────────────────────────────────┤
│  agents                                                         │
│  ┌──────────────────────────────────────────────┐               │
│  │ id(PK) | name | description | department      │               │
│  │ status | tags(JSON) | creator_id              │               │
│  │ success_rate | avg_time | daily_calls         │               │
│  │ usage_count | rating | is_favorite             │               │
│  │ skills(JSON) | knowledge(JSON) | system_prompt  │               │
│  │ examples(JSON) | model | visibility            │               │
│  │ created_at | updated_at                        │               │
│  └──────────────────────────────────────────────┘               │
│                                                                 │
│  agent_call_logs                    agent_ratings               │
│  ┌────────────────────────┐        ┌──────────────────────┐     │
│  │ id(PK) | agent_id(FK)  │        │ id(PK) | agent_id(FK) │     │
│  │ user_id | input | output│        │ user_id(FK) | rating  │     │
│  │ status | duration      │        │ comment | created_at  │     │
│  │ token_usage | error_msg │        └──────────────────────┘     │
│  │ created_at              │                                     │
│  ────────────────────────┘        agent_favorites               │
│                                   ┌──────────────────────┐       │
│                                   │ id(PK) | user_id(FK)  │       │
│                                   │ agent_id(FK)          │       │
│                                   └──────────────────────┘       │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                      技能与工作流                                 │
├─────────────────────────────────────────────────────────────────┤
│  skills                                                         │
│  ┌──────────────────────────────────────────────┐               │
│  │ id(PK) | name | description | type            │               │
│  │ category | parameters(JSON) | return_type     │               │
│  │ version | status | usage_count | timeout       │               │
│  │ api_endpoint | creator_id | created_at         │               │
│  └──────────────────────────────────────────────┘               │
│                                                                 │
│  workflows ──┬── workflow_nodes                                 │
│  ┌─────────┐ │   ────────────────────────┐                     │
│  │id(PK)   │ │   │id(PK)| workflow_id(FK) │                     │
│  │name     │ └──▶│node_type | position_x/y│                     │
│  │desc     │     │agent_id(FK)|skill_id(FK)│                    │
│  │trigger  │     │config(JSON)             │                     │
│  │status   │     └────────────────────────┘                     │
│  │exec_cnt │                                                      │
│  │rate     │     workflow_edges                                 │
│  └────────┘     ┌────────────────────────                      │
│                 │id(PK)| workflow_id(FK) │                      │
│                 │source_node|target_node │                      │
│                 │source_handle|target_hdl│                      │
│                 └────────────────────────┘                      │
│                                                                 │
│  workflow_executions                                            │
│  ┌──────────────────────────────────────┐                       │
│  │id(PK)| workflow_id(FK) | triggered_by│                       │
│  │trigger_type | status | input_data     │                       │
│  │output_data | error_msg | duration     │                       │
│  │start_time | end_time                  │                       │
│  └──────────────────────────────────────┘                       │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                      知识与运维                                   │
├─────────────────────────────────────────────────────────────────┤
│  knowledge_documents              audit_logs                    │
│  ┌──────────────────────────    ┌──────────────────────────┐   │
│  │id(PK)| title | type      │    │id(PK)| user_id | action   │   │
│  │size | uploaded_by        │    │resource | result | ip     │   │
│  │status | vector_status    │    │error_msg | timestamp      │   │
│  │chunk_count | hit_count   │    └──────────────────────────┘   │
│  │category | file_path/url  │                                    │
│  └──────────────────────────┘    cost_records                    │
│                                   ┌──────────────────────────┐   │
│                                   │id(PK)| department         │   │
│                                   │agent_id | agent_name      │   │
│                                   │token_usage | api_calls    │   │
│                                   │cost(RMB) | date           │   │
│                                   └──────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
```

### 4.2 核心枚举值

| 枚举 | 取值 |
|------|------|
| **UserRole** | ADMIN, DEVELOPER, USER, OPERATOR |
| **UserStatus** | ACTIVE, INACTIVE |
| **Department** | OPERATION, QC, CUSTOMER, FINANCE, IT |
| **AgentStatus** | ONLINE, OFFLINE, PENDING |
| **CallStatus** | SUCCESS, FAILED, TIMEOUT |
| **SkillType** | API, FUNCTION |
| **SkillStatus** | PUBLISHED, DRAFT, REVIEW |
| **NodeType** | AGENT, SKILL, CONDITION, LOOP, APPROVAL, START, END |
| **WorkflowStatus** | DRAFT, ACTIVE, INACTIVE |
| **TriggerType** | API, CRON, EVENT |
| **ExecutionStatus** | RUNNING, COMPLETED, FAILED, CANCELLED |
| **DocumentType** | PDF, WORD, EXCEL, MARKDOWN, URL (枚举值: pdf/word/excel/markdown/url) |
| **DocumentStatus** | UPLOADING, PROCESSING, READY, FAILED (枚举值: uploading/processing/ready/failed) |
| **VectorStatus** | PENDING, INDEXING, COMPLETED (枚举值: pending/indexing/completed) |
| **AuditResult** | SUCCESS, FAILED |

### 4.3 JPA 转换器 (AttributeConverter)

所有枚举类均配备 JPA `@Converter(autoApply = true)`，实现数据库字符串值与 Java 枚举的自动双向转换：

| Converter | 目标枚举 | 说明 |
|-----------|----------|------|
| `DepartmentConverter` | Department | 按 code 匹配，支持 name/code/description 三种形式 |
| `UserRoleConverter` | UserRole | 按 code 匹配 |
| `UserStatusConverter` | UserStatus | 按 code 匹配 |
| `AgentStatusConverter` | AgentStatus | 按 code 匹配 |
| `SkillStatusConverter` | SkillStatus | 按 code 匹配 |
| `SkillTypeConverter` | SkillType | 按 code 匹配 |

---

## 5. 后端服务详解

### 5.1 common 共享模块

**位置**: `backend/common/`

所有服务通过 Maven 依赖引入此模块，共享以下组件：

#### 实体类 (15个)
所有实体使用 JPA `@Entity` + Lombok `@Data`/`@Builder`，通过 `@PrePersist`/`@PreUpdate` 自动维护 `createdAt`/`updatedAt` 时间戳。

#### 枚举类 (17个) + 转换器 (6个)
每个枚举类有独立的 `fromCode(String)` 方法和对应的 `@Converter(autoApply = true)` JPA 转换器。

#### 统一响应格式
```
ApiResponse<T> { code: int, message: String, data: T }
PageResponse<T> { list: List<T>, total: long, page: int, size: int }
```

#### JWT 工具 (JwtUtil)
- 密钥从 `jwt.secret-key` 配置读取（要求至少 32 字节）
- Token 默认有效期 24 小时
- Claims 包含 `userId`（subject）、`role`、`department`
- 提供 `generateToken()`, `extractUserId()`, `validateToken()`, `extractAllClaims()` 方法

#### JWT 认证过滤器 (JwtAuthenticationFilter)
- `OncePerRequestFilter` 实现，注册到所有服务的安全过滤器链
- 从 `Authorization: Bearer <token>` header 提取 JWT
- 验证 token 有效性，解析 claims 后设置 `SecurityContextHolder`
- 支持基于角色的认证（`ROLE_ADMIN`, `ROLE_DEVELOPER` 等）

#### 全局异常处理 (GlobalExceptionHandler)
`@RestControllerAdvice` 统一捕获：
- `BusinessException` → 根据 code 映射 HTTP 状态码
- `UnauthorizedException` → 401
- 其他 `Exception` → 500 "服务器内部错误"（同时记录 error 日志）

---

### 5.2 auth-service (8081)

**职责**: 用户认证与用户管理

#### API 端点

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/auth/login` | 用户登录（邮箱+密码），返回 JWT |
| GET | `/api/auth/me` | 获取当前用户信息 |
| GET | `/api/users` | 分页查询用户列表（需 ADMIN 角色） |
| GET | `/api/users/{id}` | 获取单个用户（需 ADMIN 角色） |
| POST | `/api/users` | 创建用户（需 ADMIN 角色） |
| PUT | `/api/users/{id}` | 更新用户（需 ADMIN 角色） |
| DELETE | `/api/users/{id}` | 删除用户（需 ADMIN 角色） |
| PUT | `/api/users/{id}/password` | 重置密码（需 ADMIN 角色） |

#### 安全配置
- Spring Security 关闭 CSRF，采用无状态会话
- `/api/auth/**` 放行（登录不需要认证）
- `/api/users/**` 需要 `ADMIN` 角色
- 所有端点注册 `JwtAuthenticationFilter`
- `DataInitializer` 在数据库为空时自动初始化 3 个默认用户

---

### 5.3 agent-service (8082)

**职责**: Agent 全生命周期管理、调用、评分、调用日志

#### API 端点

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/agents` | 分页查询 Agent 列表 |
| GET | `/api/agents/{id}` | 获取 Agent 详情 |
| POST | `/api/agents` | 创建 Agent |
| PUT | `/api/agents/{id}` | 更新 Agent |
| DELETE | `/api/agents/{id}` | 删除 Agent |
| POST | `/api/agents/{id}/call` | 调用 Agent（接入 LLM 真实响应） |
| GET | `/api/agent-call-logs` | 查询调用日志 |
| POST | `/api/agents/{agentId}/ratings` | 创建评分 |
| PUT | `/api/agents/{agentId}/ratings` | 更新评分 |
| DELETE | `/api/agents/{agentId}/ratings/{id}` | 删除评分 |
| GET | `/api/agents/{agentId}/ratings` | 查询评分列表 |

#### Agent 调用链路（已接入 LLM + 并行技能/知识库）
```
POST /api/agents/{id}/call
  → agentRepository.findById(id)
  → 检查 status == ONLINE
  → 解析 skills/knowledge JSON 数组
  → CompletableFuture 并行执行:
     ├─ callSkills(): RestTemplate POST → skill-service /api/skills/{id}/execute
     └─ searchKnowledge(): RestTemplate GET → knowledge-service /api/knowledge/search
  → 合并技能和知识库结果到增强 system prompt
  → RestTemplate POST → http://localhost:8087/api/llm/chat
     (system prompt: 角色 + systemPrompt + 技能结果 + 知识库结果 + 示例对话)
  → 解析 LLM 响应文本
  → 更新 Agent 指标 (usageCount, successRate, avgTime)
  → 返回 { output, duration }
```

---

### 5.4 skill-service (8083)

**职责**: 技能定义管理与发布

#### API 端点

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/skills` | 分页查询技能列表 |
| GET | `/api/skills/{id}` | 获取技能详情 |
| POST | `/api/skills` | 创建技能（默认 DRAFT） |
| PUT | `/api/skills/{id}` | 更新技能 |
| DELETE | `/api/skills/{id}` | 删除技能 |
| POST | `/api/skills/{id}/publish` | 发布技能（DRAFT/REVIEW → PUBLISHED） |
| POST | `/api/skills/{id}/execute` | 执行技能（传入 input 参数，返回执行结果） |

#### 核心逻辑
- 创建技能时默认状态为 `DRAFT`，版本号 `1.0.0`，超时 30 秒
- 参数列表 (`List<SkillParameter>`) 通过 Jackson 序列化为 JSON 字符串存储
- 发布时检查是否已发布，已发布则返回 400 错误
- 技能执行 (`execute`) 检查技能状态必须为 `PUBLISHED`，根据技能名称匹配内置逻辑返回模拟数据，每次执行递增 `usageCount`

---

### 5.5 workflow-service (8084)

**职责**: 工作流定义、可视化编排、节点图执行、执行记录追踪

#### API 端点

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/workflows` | 分页查询工作流列表 |
| GET | `/api/workflows/{id}` | 获取工作流详情（含 nodes/edges） |
| POST | `/api/workflows` | 创建工作流（含节点/边） |
| PUT | `/api/workflows/{id}` | 更新工作流（替换 nodes/edges） |
| DELETE | `/api/workflows/{id}` | 删除工作流 |
| POST | `/api/workflows/{id}/activate` | 激活 (DRAFT → ACTIVE) |
| POST | `/api/workflows/{id}/deactivate` | 停用 (→ INACTIVE) |
| POST | `/api/workflows/{id}/execute` | 执行工作流（节点图遍历 + 真实执行） |
| POST | `/api/workflow-executions` | 创建执行记录 |
| PUT | `/api/workflow-executions/{id}/complete` | 标记完成 |
| PUT | `/api/workflow-executions/{id}/fail` | 标记失败 |
| GET | `/api/workflows/{id}/executions` | 查询某工作流的执行历史 |

#### 工作流执行引擎
```
POST /api/workflows/{id}/execute
  → 获取工作流定义（含 nodes + edges）
  → 构建邻接表（节点图遍历）
  → 查找 start 节点作为入口
  → 逐节点执行：
     ├─ agent 节点: RestTemplate → agent-service:8082/api/agents/{agentId}/call
     ├─ skill 节点: 调用 skill-service API（框架已实现）
     ├─ condition 节点: 评估条件表达式，选择分支
     ├─ loop 节点: 循环控制（模拟）
     ├─ approval 节点: 人工审批（标记 pending_approval）
     └─ start/end 节点: 直接通过
  → 节点间数据通过 ObjectNode context 传递（以 nodeId 为 key）
  → 更新 WorkflowExecution 状态和 outputData
```

#### 节点配置
- Agent 节点: 关联 `agentId`，执行时调用对应 Agent
- Skill 节点: 关联 `skillId`，执行时调用对应技能
- 条件节点: `config` 存储条件表达式
- 循环节点: `config` 存储循环类型（固定次数/遍历列表/条件循环）和次数
- 审批节点: `config` 存储审批人 ID 和超时时间
- 所有节点支持变量映射（JSON 格式的输入输出映射）

---

### 5.6 knowledge-service (8085)

**职责**: 知识文档管理与检索

#### API 端点

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/knowledge` | 分页查询文档列表 |
| GET | `/api/knowledge/{id}` | 获取文档详情 |
| POST | `/api/knowledge` | 创建文档记录 |
| PUT | `/api/knowledge/{id}` | 更新文档 |
| DELETE | `/api/knowledge/{id}` | 删除文档 |
| GET | `/api/knowledge/search?query=&limit=` | 搜索文档（标题模糊匹配） |
| GET | `/api/knowledge/categories` | 获取分类列表 |

#### 核心逻辑
- 创建文档默认 `status=UPLOADING`, `vectorStatus=PENDING`
- 搜索基于 `title LIKE '%query%'` 模糊匹配
- 支持按分类筛选
- 文档实体包含 `content` 字段（LONGTEXT），用于存储文档正文内容，支持预览
- 枚举值统一使用小写命名（`pdf/word/excel/markdown/url`、`uploading/processing/ready/failed`、`pending/indexing/completed`）

---

### 5.7 admin-service (8086)

**职责**: 审计日志查询、费用统计、系统监控 Dashboard

#### API 端点

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/admin/logs` | 审计日志查询（分页 + 多条件筛选） |
| GET | `/api/admin/costs` | 费用记录查询（分页 + 多条件筛选） |
| GET | `/api/admin/statistics` | 系统统计数据（从数据库聚合） |
| GET | `/api/admin/departments` | 部门列表 |
| GET | `/api/admin/roles` | 角色列表 |
| GET | `/api/admin/dashboard/trends?days=7` | 调用趋势（按天聚合 cost_records.api_calls） |
| GET | `/api/admin/dashboard/agent-ranking?topN=5` | Agent 使用量排行榜 |
| GET | `/api/admin/dashboard/skill-ranking?topN=5` | 技能使用量排行榜 |

#### 统计数据来源
- **总 Agent 数**: `SELECT COUNT(*) FROM agents`
- **总调用次数**: `SELECT SUM(usageCount) FROM agents`
- **总费用**: `SELECT SUM(cost) FROM cost_records`
- **活跃用户**: `SELECT COUNT(*) FROM agents WHERE updatedAt BETWEEN ? AND ?`
- **调用趋势**: 按日期聚合 `cost_records` 的 `api_calls` 总和
- **Agent 排行**: `SELECT * FROM agents ORDER BY usageCount DESC LIMIT N`
- **技能排行**: 通过 HTTP 调用 skill-service 获取并按 `usageCount` 排序

---

### 5.8 llm-service (8087)

**职责**: Claude AI 能力集成，提供聊天、生成、问答、分析等 AI 服务

#### API 端点

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/llm/chat` | 通用聊天（支持 system + user 消息） |
| POST | `/api/llm/skills/generate` | AI 生成技能定义 |
| POST | `/api/llm/workflows/generate` | AI 生成工作流定义 |
| POST | `/api/llm/knowledge/qa` | 知识库问答 |
| POST | `/api/llm/logs/analyze` | 日志分析 |
| POST | `/api/llm/summarize?text=&maxLength=` | 文本摘要 |
| POST | `/api/llm/keywords?text=&count=` | 关键词提取 |

#### Claude 配置
- 使用 Anthropic Java SDK (`AnthropicClient`)
- 默认模型: `claude-3-5-sonnet-20241022`
- 默认 API 地址: `http://localhost:15721`（本地代理/mock，可配置为真实 API）
- API Key 从 `anthropic.api-key` 配置读取

---

## 6. 前端架构

### 6.1 技术架构

```
┌──────────────────────────────────────────────┐
│                  uni-app                      │
│  ┌────────────────────────────────────────┐  │
│  │           Vue 3 (Composition API)      │  │
│  │  ┌──────┐  ┌──────────┐  ┌──────────┐ │  │
│  │  │Pages │  │Components│  │Services  │ │  │
│  │  │(12个)│  │  (5个)   │  │  (7个)   │ │  │
│  │  └──────┘  └──────────┘  └──────────┘ │  │
│  │  ┌──────  ┌──────────┐               │  │
│  │  │ Data │  │  Utils   │               │  │
│  │  │(Mock)│  │(request) │               │  │
│  │  └──────┘  └──────────┘               │  │
│  └────────────────────────────────────────┘  │
│  ┌────────────────────────────────────────┐  │
│  │  Vite dev server (proxy to backend)    │  │
│  └────────────────────────────────────────┘  │
└──────────────────────────────────────────────┘
```

### 6.2 页面路由

| 路由 | 页面 | 说明 |
|------|------|------|
| `/pages/index/index` | 工作台 | 首页仪表盘，统计概览，常用 Agent |
| `/pages/login/index` | 登录页 | 邮箱+密码登录，JWT 存储 |
| `/pages/market/index` | Agent 市场 | Agent 浏览、搜索、筛选、创建入口 |
| `/pages/agent/detail` | Agent 详情 | Agent 详细信息、评价、使用指南 |
| `/pages/agent/chat` | Agent 对话 | 聊天界面，消息气泡，LLM 真实响应 |
| `/pages/builder/index` | Agent 构建器 | 多 Tab 表单：基础配置/提示词/技能/知识库 |
| `/pages/skills/index` | Skills 工作台 | 技能列表、搜索、创建弹窗 |
| `/pages/workflow/index` | 流程编排 | 工作流卡片列表、运行/编辑 |
| `/pages/workflow/designer` | 流程设计器 | 画布编排：节点拖拽、连线、配置、发布 |
| `/pages/workflow/execution` | 执行历史 | 执行记录查看、状态筛选、重试 |
| `/pages/knowledge/index` | 知识库管理 | 文档列表、分类筛选、文件上传 |
| `/pages/monitor/index` | 监控分析 | 趋势图、排行榜、全部实时数据 |
| `/pages/admin/index` | 管理中心 | 管理菜单、审计日志、系统概览 |

### 6.3 前端数据流

```
用户操作 (点击/输入)
  │
  ▼
Vue Component (Pages/Components)
  │
  ├── 使用 Mock Data (chat, detail, admin 页面)
  │   └── src/data/*.ts → 本地数组
  │
  └── 调用 Service 层
      └── src/services/*.ts (7 个服务文件)
          │
          ▼
      utils/request.ts (Axios 封装)
          │
          ├─ 请求拦截: 添加 Bearer Token
          ├─ 响应拦截: 统一错误处理 (401→登录, 网络异常)
          └─ HTTP 错误静默处理，由页面自行决定提示
              │
              ▼
      Vite Proxy (开发环境, 10 条路由)
          │
          ▼
      后端微服务 (8081-8087)
```

### 6.4 组件体系

| 组件 | 用途 |
|------|------|
| `Layout.vue` | 全局布局：左侧可折叠导航栏 + 右侧内容区 |
| `AgentCard.vue` | Agent 卡片：头像、名称、部门、标签、指标、收藏按钮 |
| `NavBar.vue` | 顶部导航栏：标题、返回按钮、自适应状态栏高度 |
| `SearchBar.vue` | 搜索输入框：支持 v-model、清除按钮、回车搜索 |
| `StatCard.vue` | 统计卡片：图标、数值、标签、趋势百分比 |

### 6.5 前端完成度

| 页面 | 数据源 | API 调用 | 备注 |
|------|--------|----------|------|
| 登录页 | API | `userService.login()` | 完整登录流程，JWT 存储 |
| 工作台 | API | `agentService.list()`, `adminService.getStatistics()` | 已接入后端 |
| Agent 市场 | API | `agentService.list()`, `adminService.getDepartments()` | 已接入后端，支持加载态和搜索筛选 |
| Agent 详情 | API + Mock | `agentService.get()` | 详情从 API 读取，评价仍为 mock |
| Agent 对话 | API | `llmService.chat()` | 已接入 LLM 真实响应 |
| Agent 构建器 | API | `skillService.list()`, `knowledgeService.getCategories()` | 已接入后端获取选项列表 |
| Skills | API | `skillService.list()`, `skillService.create()` | 已接入后端，picker 选择器 |
| 流程编排 | API | `workflowService.*` | 已接入后端 |
| 流程设计器 | API | `workflowService.*`, `agentService.list()`, `skillService.list()` | 画布拖拽/连线/配置完整 |
| 执行历史 | API | `workflowService.getExecutions()` | 状态筛选、重试功能 |
| 知识库 | API | `knowledgeService.list()`, `knowledgeService.getCategories()` | 已接入后端，支持上传 |
| 监控分析 | API | `adminService.getStatistics/trends/agentRanking/skillRanking()` | 全部实时数据 |
| 管理中心 | API + Mock | `adminService.getAuditLogs()` | 审计日志已接入，概览为 mock |

---

## 7. 核心调用链路

### 7.1 用户登录链路

```
前端                          auth-service:8081                MySQL
  │                               │                              │
  │ POST /api/auth/login          │                              │
  │ { email, password }           │                              │
  │──────────────────────────────▶│                              │
  │                               │  userRepository.findByEmail() │
  │                               │─────────────────────────────▶│
  │                               │◀─────────────────────────────│
  │                               │  返回 User 实体               │
  │                               │                              │
  │                               │  BCrypt 密码校验              │
  │                               │  JwtUtil.generateToken()     │
  │                               │                              │
  │◀──────────────────────────────│                              │
  │ { code: 200,                  │                              │
  │   data: { token, user } }     │                              │
  │                               │                              │
  │  uni.setStorageSync('token')  │                              │
```

### 7.2 Agent 调用链路（已接入 LLM + 技能 + 知识库）

```
前端                  agent-service:8082        llm-service:8087      skill-service:8083   knowledge-service:8085   MySQL
  │                        │                        │                      │                       │                  │
  │ POST /api/agents/{id}/call                     │                      │                       │                  │
  │ { input: "查询路由信息" }                       │                      │                       │                  │
  │─────────────────────────▶│                      │                      │                       │                  │
  │                         │  agentRepository.findById()                                    │                      │                  │
  │                         │───────────────────────────────────────────────────────────────▶│                      │                  │
  │                         │◀───────────────────────────────────────────────────────────────│                      │                  │
  │                         │                        │                      │                       │              │
  │                         │  检查 status == ONLINE  │                      │                       │              │
  │                         │  解析 skills/knowledge  │                      │                       │              │
  │                         │  JSON 数组              │                      │                       │              │
  │                         │                        │                      │                       │              │
  │                         │  ┌─── 并行执行 ───────┐│                      │                       │              │
  │                         │  │ CompletableFuture  ││                      │                       │              │
  │                         │  │                    ││                      │                       │              │
  │                         │  │ callSkills()       ││                      │                       │              │
  │                         │  │───────────────────────────────────────────▶│                       │              │
  │                         │  │ POST /api/skills/  ││                      │                       │              │
  │                         │  │ {id}/execute       ││                      │                       │              │
  │                         │  │◀───────────────────││                      │                       │              │
  │                         │  │                    ││                      │                       │              │
  │                         │  │ searchKnowledge()  ││                      │                       │              │
  │                         │  │──────────────────────────────────────────────────────────────────▶│              │
  │                         │  │ GET /api/knowledge/││                      │                       │              │
  │                         │  │ search?query=...   ││                      │                       │              │
  │                         │  │◀───────────────────││                      │                       │              │
  │                         │  └────────────────────┘│                      │                       │              │
  │                         │                        │                      │                       │              │
  │                         │  构造增强 system prompt│                      │                       │              │
  │                         │  (角色 + 技能结果      │                      │                       │              │
  │                         │   + 知识库结果)        │                      │                       │              │
  │                         │  POST /api/llm/chat     │                      │                       │              │
  │                         │───────────────────────▶│                      │                       │              │
  │                         │                        │  Claude API           │                       │              │
  │                         │                        │──────────────▶│                      │              │
  │                         │                        │◀──────────────│                      │              │
  │                         │◀───────────────────────│                      │                       │              │
  │                         │  返回 AI 响应文本       │                      │                       │              │
  │                         │                        │                      │                       │              │
  │                         │  更新 metrics           │                      │                       │              │
  │                         │  agentRepository.save() │                      │                       │              │
  │                         │───────────────────────────────────────────────────────────────────────▶│              │
  │                         │                        │                      │                       │              │
  │◀─────────────────────────│                        │                      │                       │              │
  │ { output: "AI回复...",   │                        │                      │                       │              │
  │   duration: 3200 }       │                        │                      │                       │              │
```

### 7.3 工作流执行链路

```
前端                  workflow-service:8084         agent-service:8082    MySQL
  │                        │                            │                  │
  │ POST /api/workflows/{id}/execute                  │                  │
  │────────────────────────▶│                          │                  │
  │                        │  获取 workflow (含 nodes/edges)            │
  │                        │───────────────────────────────────────────▶│
  │                        │◀───────────────────────────────────────────│
  │                        │                            │               │
  │                        │  构建邻接表 → 查找 start 节点               │
  │                        │                            │               │
  │                        │  [遍历节点图]               │               │
  │                        │  ├─ agent 节点:            │               │
  │                        │  │  POST → agent-service   │               │
  │                        │  │  /api/agents/{id}/call  │               │
  │                        │  │─────────────────────────▶│               │
  │                        │  │◀─────────────────────────│               │
  │                        │  ├─ skill 节点: 调用技能     │               │
  │                        │  ├─ condition 节点: 评估条件 │               │
  │                        │  └─ loop/approval: 状态标记  │               │
  │                        │                            │               │
  │                        │  节点间数据通过 context     │               │
  │                        │  (ObjectNode) 传递          │               │
  │                        │                            │               │
  │                        │  更新 WorkflowExecution     │               │
  │                        │  (COMPLETED/FAILED)         │               │
  │                        │───────────────────────────────────────────▶│
  │◀────────────────────────│                            │               │
  │ { status: "completed", │                            │               │
  │   outputData: {...} }  │                            │               │
```

### 7.4 监控分析数据加载链路

```
前端 (monitor/index.vue)               Vite Proxy              admin-service:8086
  │                                       │                        │
  │ onMounted()                           │                        │
  │   ├─ getStatistics()                  │                        │
  │   ├─ getCallTrends(7)                 │                        │
  │   ├─ getAgentRanking(5)               │                        │
  │   └─ getSkillRanking(5)               │                        │
  │──────────────────────────────────────▶│                        │
  │                                       │ 4个并行请求              │
  │                                       │───────────────────────▶│
  │                                       │                        │
  │                                       │  getStatistics:        │
  │                                       │  → COUNT(agents)       │
  │                                       │  → SUM(usageCount)     │
  │                                       │  → SUM(cost)           │
  │                                       │  → COUNT(active users) │
  │                                       │                        │
  │                                       │  getCallTrends:        │
  │                                       │  → 按日期聚合 api_calls │
  │                                       │                        │
  │                                       │  getAgentRanking:      │
  │                                       │  → ORDER BY usageCount │
  │                                       │                        │
  │                                       │  getSkillRanking:      │
  │                                       │  → HTTP skill-service  │
  │                                       │◀───────────────────────│
  │◀──────────────────────────────────────│                        │
  │  渲染统计卡片、趋势图、排行榜           │                        │
```

---

## 8. 关键设计决策与现状

### 8.1 已完成的功能

1. **用户认证系统**: 登录、JWT 生成、用户 CRUD（BCrypt 密码加密）
2. **JWT 认证过滤器**: `JwtAuthenticationFilter` 集成到所有 7 个服务
3. **Agent 管理**: 完整的 CRUD + 调用（接入 LLM 真实响应 + 并行技能/知识库调用）+ 指标自动计算 + 评分 + 调用日志
4. **技能管理**: CRUD + 发布流程 + 参数 JSON 序列化 + **技能执行端点** (`POST /api/skills/{id}/execute`)
5. **工作流引擎**: CRUD + 激活/停用 + **节点图执行引擎**（Agent/Skill 真实调用）+ 执行记录 + 可视化设计器
6. **知识文档管理**: CRUD + 标题搜索 + 文件上传（uni.chooseFile + uni.uploadFile）+ **文档内容字段**（LONGTEXT）+ 预览功能
7. **LLM 集成**: Claude API 封装（聊天/技能生成/工作流生成/知识问答/日志分析/摘要/关键词提取）
8. **监控 Dashboard**: 数据库聚合统计 + 调用趋势 + Agent/Skill 排行榜
9. **登录页面**: 完整登录流程，JWT 存储，401 自动跳转
10. **前端页面**: 13 个页面，大部分已接入真实后端 API
11. **Agent 增强配置**: Agent 实体扩展 `skills`、`knowledge`、`systemPrompt`、`examples`、`model`、`visibility` 字段
12. **Agent 并行调用引擎**: 使用 `CompletableFuture` 并发调用技能和知识库，结果合并注入 LLM system prompt

### 8.2 架构特点

- **共享数据库的微服务**: 7 个服务共享同一 MySQL 数据库，通过 `common` 模块共享实体定义。这是模块化单体（Modular Monolith）架构
- **服务间 HTTP 调用**: agent-service → llm-service, workflow-service → agent-service, admin-service → skill-service, **agent-service → skill-service (技能执行), agent-service → knowledge-service (知识检索)**
- **JPA 转换器模式**: 所有枚举类配备 `@Converter(autoApply = true)`，数据库存 code 值，Java 层用枚举
- **枚举小写命名**: DocumentType、DocumentStatus、VectorStatus 枚举值统一使用小写（如 `pdf`、`uploading`、`pending`），配合 `@JdbcTypeCode(SqlTypes.CHAR)` 映射
- **Agent 并行调用引擎**: `ExecutorService(4 线程池)` + `CompletableFuture` 并发执行技能和知识库检索，结果合并后注入 LLM
- **无服务注册/发现**: 虽然引入了 Spring Cloud 和 Spring Cloud Alibaba，但代码中未使用 Nacos、Eureka 等注册中心
- **无 API 网关**: 前端直接通过路径前缀访问各服务，依赖 Vite/Nginx 代理路由
- **Lombok 大量使用**: 所有实体和 DTO 使用 `@Data`/`@Builder` 简化样板代码
- **环境变量配置**: 已统一为 Vite 的 `import.meta.env.VITE_*` 语法
- **前端超时配置**: API 超时从 30s 调整为 120s（`.env.development`），默认回退值 60s

### 8.3 项目文件清单

```
yimiagentportal/
├── backend/
│   ├── pom.xml                          # Maven 父 POM
│   ├── common/                          # 共享模块 (43 文件)
│   ├── auth-service/                    # 认证服务 (13 文件, :8081)
│   ├── agent-service/                   # Agent 服务 (21 文件, :8082)
│   ├── skill-service/                   # 技能服务 (9 文件, :8083)
│   ├── workflow-service/                # 工作流服务 (15 文件, :8084)
│   ├── knowledge-service/               # 知识服务 (9 文件, :8085)
│   ├── admin-service/                   # 管理服务 (13 文件, :8086)
│   └── llm-service/                     # LLM 服务 (17 文件, :8087)
├── src/                                 # 前端源码 (36 文件)
│   ├── main.ts / App.vue                # 入口
│   ├── config/index.ts                  # 配置 (Vite 环境变量)
│   ├── utils/request.ts                 # Axios 封装
│   ├── services/*.ts                    # API 服务层 (7 文件)
│   ├── data/*.ts                        # Mock 数据 (5 文件)
│   ├── components/*.vue                 # 组件 (5 文件)
│   └── pages/**/                        # 页面 (13 文件)
├── database/schema.sql                  # 数据库建表脚本 (13 表)
├── vite.config.ts                       # Vite 构建 + 代理配置 (10 条路由)
├── package.json                         # 前端依赖
├── tsconfig.json                        # TypeScript 配置
├── .env.development / .env.production   # 环境变量 (VITE_ 前缀)
└── index.html                           # HTML 入口
```

> 后端总计约 141 个 Java 源文件

### 8.4 启动方式

**后端**:
```bash
cd backend
# 编译所有模块（使用 Maven Wrapper）
./mvnw clean package -DskipTests
# 串行启动各服务（避免 DDL 锁表）
java -jar auth-service/target/auth-service-1.0.0.jar --spring.profiles.active=dev
java -jar agent-service/target/agent-service-1.0.0.jar --spring.profiles.active=dev
# ... 依次启动，间隔 10-15 秒
```

**前端**:
```bash
npm install
npm run dev:h5       # 开发模式 (H5) → http://localhost:5173
npm run build:h5     # 构建 H5 产物
```

### 8.5 功能完成状态

| 模块 | 状态 | 说明 |
|------|------|------|
| **认证** | ✅ 已实现 | JWT Filter 集成到所有服务，端点配置了读写权限分离（GET 公开，写操作需登录） |
| **工作流引擎** | ✅ 已实现 | 节点图遍历执行、条件分支 `{{var}}` 表达式评估、Agent/Skill 真实调用、失败自动重试 3 次 |
| **工作流调度** | ✅ 已实现 | 添加 `@EnableScheduling`，支持 Cron 表达式定时触发 |
| **知识库** | ✅ 已实现 | 文件上传本地存储（`/uploads/`），加权关键词搜索（标题权重高），文档内容字段支持预览 |
| **前端交互** | ✅ 已实现 | 工作流画布支持 H5 鼠标拖拽和连线（mousedown/move/up 事件） |
| **前端数据** | ✅ 已实现 | 监控 Dashboard 趋势图/排行榜、Agent 详情评价、管理中心概览全部接入真实 API |
| **全局样式** | ✅ 已实现 | 蓝白 Ant Design 风格统一，所有 13 个页面已适配 |
| **Agent 技能/知识库** | ✅ 已实现 | Agent 实体扩展 skills/knowledge/systemPrompt/examples/model/visibility 字段，调用时并行执行技能和检索知识库，结果合并注入 LLM |
| **技能执行** | ✅ 已实现 | `POST /api/skills/{id}/execute` 端点，检查 PUBLISHED 状态后执行内置逻辑 |

### 8.6 与阿里悟空 (Wukong) 产品功能对比

阿里于 2026 年 3 月 17 日发布的企业级 AI Agent 平台"悟空"（ATH 事业群首秀）定位为**全球首个企业级 AI 原生工作平台**，核心能力是**多 Agent 协同**、**跨系统操作**、**企业级安全沙箱**和**Skill 生态市场**。悟空内置于钉钉（超 2000 万企业组织），同时作为独立应用运行，首批提供十大行业 OPT（One Person Team）开箱即用方案。以下是对比分析：

| 功能维度 | 阿里悟空 (Wukong) | 壹米 AI Agent 门户 | 差距说明 |
|---------|------------------|-------------------|---------|
| **Agent 市场** | 全球 Skill 市场生态，付费调用分发 | ✅ 基础技能市场 | 缺少技能审核/版本管理/测试沙箱/市场分发机制 |
| **多 Agent 协作** | 多 Agent 同时协调工作（龙虾军团） | ⚠️ 单 Agent 调用 | Agent 可关联多个技能/知识库但仅单次串行，无多 Agent 对话编排 |
| **工作流编排** | 可视化 + 自然语言创建 + AI 推荐节点 | ✅ 可视化画布编排 | 缺少自然语言创建、AI 智能推荐节点 |
| **跨系统操作** | 原生操作电脑/浏览器/云端 + 无影 AgentBay | ❌ 未实现 | 仅内部 HTTP 调用，无外部系统/桌面集成能力 |
| **钉钉/IM 集成** | 深度集成钉钉上千项能力，支持微信/Slack | ❌ 未实现 | 无 IM 平台集成、无消息推送 |
| **企业权限** | 自动继承钉钉组织架构和权限 | ⚠️ 基础 RBAC | 无组织架构树、无部门级数据隔离、无权限继承 |
| **安全沙箱** | 六层递进安全体系 + 操作隔离 | ❌ 未实现 | 无代码执行沙箱、无多租户隔离、无敏感凭据加密 |
| **Token 成本管控** | 实时 Token 消耗可视化 + 预算管理 | ⚠️ 基础费用记录 | 有 cost_records 聚合统计，无实时消耗监控和预算控制 |
| **行业解决方案** | 十大行业 OPT 开箱即用（电商/跨境电商/开发/设计等） | ❌ 未实现 | 无行业模板、无预置一人团队方案 |
| **开发者生态** | 开发→审核→上架→分发全链路，企业内部共享 | ⚠️ 基础 CRUD | 缺少 Skill 开发审核流程、上架分发机制 |
| **长期记忆** | 用户偏好/习惯/上下文跨会话记忆 | ❌ 未实现 | 无用户画像、无对话历史持久化、无个性化推荐 |
| **Agent 构建** | AI 辅助构建 + 模板市场 + 一键部署 | ⚠️ 手动表单构建 | 缺少 AI 辅助创建、模板库、一键部署 |
| **多模型支持** | Qwen 全家桶 + 企业专属模型部署 | ⚠️ 仅 Claude | Agent 已有 `model` 字段但未实现多模型切换逻辑 |
| **多端支持** | 电脑端 + 手机端远程唤起 | ⚠️ H5 + 微信小程序 | uni-app 跨平台能力已有，但功能完整度不如独立客户端 |
| **实时监控** | 全局 Agent 状态监控 + 性能指标告警 | ✅ 基础监控 Dashboard | 缺少实时在线状态监控、Token 消耗监控、自动告警 |
| **数据看板** | 行业解决方案数据看板 + 自定义报表 | ✅ 基础统计 | 缺少行业模板、自定义报表、数据导出 |

### 8.7 核心差距总结

| 差距层级 | 说明 |
|---------|------|
| **平台级差距** | 悟空是**平台级产品**（Skill 市场、开发者生态、行业解决方案），壹米是**单租户应用** |
| **安全级差距** | 悟空从设计之初围绕企业级安全（沙箱、权限继承、凭据加密），壹米缺少安全隔离层 |
| **集成级差距** | 悟空原生集成钉钉生态 + 桌面操作能力，壹米仅内部 HTTP 微服务调用 |
| **生态级差距** | 悟空目标是"全球最大 ToB Skill 市场"，壹米技能系统仍为封闭 CRUD |

### 8.8 待完善功能清单（按优先级排序）

#### 🔴 P0 — 核心能力缺失

| # | 功能 | 优先级 | 工作量 | 说明 |
|---|------|--------|--------|------|
| 1 | **多 Agent 协作对话** | P0 | 大 | Agent 对话页支持多 Agent 切换编排，对话上下文共享，类似悟空"龙虾军团" |
| 2 | **多模型支持** | P0 | 中 | 支持切换 Claude/GPT/通义千问等多模型（Agent 已有 `model` 字段，需实现切换逻辑） |
| 3 | **组织架构管理** | P0 | 大 | 部门树形结构、用户归属、数据权限隔离、权限自动继承 |
| 4 | **Agent 行业模板** | P0 | 中 | 预置十大行业一人团队方案（电商/开发/设计/客服等），开箱即用 |

#### 🟡 P1 — 体验增强

| # | 功能 | 优先级 | 工作量 | 说明 |
|---|------|--------|--------|------|
| 5 | **AI 辅助构建 Agent** | P1 | 中 | 输入描述自动生成 Agent 配置和提示词，AI 智能推荐工作流节点 |
| 6 | **用户长期记忆** | P1 | 大 | 对话历史持久化存储、用户偏好学习、个性化推荐 |
| 7 | **知识库语义搜索** | P1 | 中 | 引入向量数据库 (Milvus/FAISS) + Embedding 模型，替代当前模糊匹配 |
| 8 | **文档自动解析** | P1 | 中 | PDF/Word 文本提取 (Apache Tika)，自动分块向量化 |
| 9 | **IM 消息推送** | P1 | 中 | 钉钉/企业微信/Slack 消息通知集成，支持远程唤起 |

#### 🟢 P2 — 企业级能力

| # | 功能 | 优先级 | 工作量 | 说明 |
|---|------|--------|--------|------|
| 10 | **Token 成本管控** | P2 | 中 | 实时 Token 消耗可视化、预算管理、按部门/Agent 成本分摊 |
| 11 | **Skill 审核与分发** | P2 | 大 | 技能开发→审核→上架→分发全链路，支持企业内部共享/市场付费调用 |
| 12 | **安全沙箱** | P2 | 大 | Skill 代码执行沙箱、多租户隔离、敏感凭据加密管理、操作审计全链路 |
| 13 | **跨系统集成** | P2 | 大 | API Connector 配置、钉钉/微信等 IM 原生能力调用、外部系统数据同步 |
| 14 | **审批工作流** | P2 | 中 | 节点级人工审批、超时自动通过/拒绝、审批流与权限联动 |
| 15 | **数据导出报表** | P2 | 小 | CSV/Excel 导出、自定义报表、定时邮件推送 |

#### 🔵 P3 — 生态扩展

| # | 功能 | 优先级 | 工作量 | 说明 |
|---|------|--------|--------|------|
| 16 | **Skill 版本管理** | P3 | 中 | 技能版本控制、灰度发布、回滚 |
| 17 | **Agent 对话分享** | P3 | 小 | 精彩对话一键分享、公开/私有设置 |
| 18 | **自定义 Dashboard** | P3 | 中 | 拖拽式看板、自定义指标组件 |
| 19 | **API 网关** | P3 | 中 | Spring Cloud Gateway 统一入口、限流熔断 |
| 20 | **自然语言工作流** | P3 | 大 | 通过 LLM 将自然语言描述自动转换为工作流定义 |

---

*文档完*
