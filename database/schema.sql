-- =====================================================
-- 壹米AI Agent门户 - 数据库初始化脚本
-- 版本: 1.0.0
-- 日期: 2024-04-15
-- 说明: 包含建库、建表、索引、外键约束
-- =====================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `yimi_ai_portal` 
DEFAULT CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE `yimi_ai_portal`;

-- =====================================================
-- 1. 用户表 (users)
-- =====================================================
CREATE TABLE IF NOT EXISTS `users` (
  `id` VARCHAR(36) NOT NULL COMMENT '用户ID',
  `name` VARCHAR(100) NOT NULL COMMENT '用户姓名',
  `department` VARCHAR(50) NOT NULL COMMENT '部门',
  `role` ENUM('admin', 'developer', 'user', 'operator') NOT NULL DEFAULT 'user' COMMENT '角色',
  `email` VARCHAR(100) NOT NULL COMMENT '邮箱',
  `phone` VARCHAR(20) NOT NULL COMMENT '手机号',
  `status` ENUM('active', 'inactive') NOT NULL DEFAULT 'active' COMMENT '状态',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_email` (`email`),
  UNIQUE KEY `uk_phone` (`phone`),
  KEY `idx_department` (`department`),
  KEY `idx_role` (`role`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- =====================================================
-- 2. Agent表 (agents)
-- =====================================================
CREATE TABLE IF NOT EXISTS `agents` (
  `id` VARCHAR(36) NOT NULL COMMENT 'Agent ID',
  `name` VARCHAR(100) NOT NULL COMMENT 'Agent名称',
  `description` TEXT COMMENT '描述',
  `department` ENUM('operation', 'qc', 'customer', 'finance') NOT NULL COMMENT '所属部门',
  `tags` JSON COMMENT '标签列表',
  `success_rate` DECIMAL(5,2) NOT NULL DEFAULT 0.00 COMMENT '成功率(%)',
  `avg_time` DECIMAL(5,2) NOT NULL DEFAULT 0.00 COMMENT '平均响应时间(s)',
  `daily_calls` INT NOT NULL DEFAULT 0 COMMENT '日调用量',
  `usage_count` INT NOT NULL DEFAULT 0 COMMENT '总使用次数',
  `creator_id` VARCHAR(36) NOT NULL COMMENT '创建者ID',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `status` ENUM('online', 'offline', 'pending') NOT NULL DEFAULT 'pending' COMMENT '状态',
  `is_favorite` BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否收藏',
  `rating` DECIMAL(2,1) COMMENT '评分(1-5)',
  PRIMARY KEY (`id`),
  KEY `idx_creator` (`creator_id`),
  KEY `idx_department` (`department`),
  KEY `idx_status` (`status`),
  KEY `idx_created_at` (`created_at`),
  CONSTRAINT `fk_agents_creator` FOREIGN KEY (`creator_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Agent表';

-- =====================================================
-- 3. 技能表 (skills)
-- =====================================================
CREATE TABLE IF NOT EXISTS `skills` (
  `id` VARCHAR(36) NOT NULL COMMENT '技能ID',
  `name` VARCHAR(100) NOT NULL COMMENT '技能名称',
  `description` TEXT COMMENT '描述',
  `type` ENUM('api', 'function') NOT NULL COMMENT '类型',
  `category` VARCHAR(50) NOT NULL COMMENT '分类',
  `parameters` JSON COMMENT '参数列表',
  `return_type` VARCHAR(100) COMMENT '返回类型',
  `version` VARCHAR(20) NOT NULL COMMENT '版本号',
  `creator_id` VARCHAR(36) NOT NULL COMMENT '创建者ID',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `status` ENUM('published', 'draft', 'review') NOT NULL DEFAULT 'draft' COMMENT '状态',
  `usage_count` INT NOT NULL DEFAULT 0 COMMENT '使用次数',
  `timeout` INT NOT NULL DEFAULT 30 COMMENT '超时时间(s)',
  PRIMARY KEY (`id`),
  KEY `idx_creator` (`creator_id`),
  KEY `idx_category` (`category`),
  KEY `idx_status` (`status`),
  KEY `idx_type` (`type`),
  CONSTRAINT `fk_skills_creator` FOREIGN KEY (`creator_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='技能表';

-- =====================================================
-- 4. 工作流表 (workflows)
-- =====================================================
CREATE TABLE IF NOT EXISTS `workflows` (
  `id` VARCHAR(36) NOT NULL COMMENT '工作流ID',
  `name` VARCHAR(100) NOT NULL COMMENT '工作流名称',
  `description` TEXT COMMENT '描述',
  `creator_id` VARCHAR(36) NOT NULL COMMENT '创建者ID',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `status` ENUM('draft', 'active', 'inactive') NOT NULL DEFAULT 'draft' COMMENT '状态',
  `trigger_type` ENUM('api', 'cron', 'event') NOT NULL COMMENT '触发方式',
  `cron_expression` VARCHAR(100) COMMENT 'Cron表达式',
  `execution_count` INT NOT NULL DEFAULT 0 COMMENT '执行次数',
  `success_rate` DECIMAL(5,2) NOT NULL DEFAULT 0.00 COMMENT '成功率(%)',
  PRIMARY KEY (`id`),
  KEY `idx_creator` (`creator_id`),
  KEY `idx_status` (`status`),
  KEY `idx_trigger_type` (`trigger_type`),
  KEY `idx_created_at` (`created_at`),
  CONSTRAINT `fk_workflows_creator` FOREIGN KEY (`creator_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='工作流表';

-- =====================================================
-- 5. 工作流节点表 (workflow_nodes)
-- =====================================================
CREATE TABLE IF NOT EXISTS `workflow_nodes` (
  `id` VARCHAR(36) NOT NULL COMMENT '节点ID',
  `workflow_id` VARCHAR(36) NOT NULL COMMENT '工作流ID',
  `type` ENUM('agent', 'skill', 'condition', 'loop', 'approval', 'start', 'end') NOT NULL COMMENT '节点类型',
  `name` VARCHAR(100) NOT NULL COMMENT '节点名称',
  `config` JSON COMMENT '节点配置',
  `position_x` INT NOT NULL DEFAULT 0 COMMENT 'X坐标',
  `position_y` INT NOT NULL DEFAULT 0 COMMENT 'Y坐标',
  `agent_id` VARCHAR(36) COMMENT '关联Agent ID',
  `skill_id` VARCHAR(36) COMMENT '关联Skill ID',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_workflow` (`workflow_id`),
  KEY `idx_agent` (`agent_id`),
  KEY `idx_skill` (`skill_id`),
  KEY `idx_type` (`type`),
  CONSTRAINT `fk_workflow_nodes_workflow` FOREIGN KEY (`workflow_id`) REFERENCES `workflows` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_workflow_nodes_agent` FOREIGN KEY (`agent_id`) REFERENCES `agents` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_workflow_nodes_skill` FOREIGN KEY (`skill_id`) REFERENCES `skills` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='工作流节点表';

-- =====================================================
-- 6. 工作流边表 (workflow_edges)
-- =====================================================
CREATE TABLE IF NOT EXISTS `workflow_edges` (
  `id` VARCHAR(36) NOT NULL COMMENT '边ID',
  `workflow_id` VARCHAR(36) NOT NULL COMMENT '工作流ID',
  `source_node_id` VARCHAR(36) NOT NULL COMMENT '源节点ID',
  `target_node_id` VARCHAR(36) NOT NULL COMMENT '目标节点ID',
  `source_handle` VARCHAR(50) COMMENT '源节点连接点',
  `target_handle` VARCHAR(50) COMMENT '目标节点连接点',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_workflow` (`workflow_id`),
  KEY `idx_source_node` (`source_node_id`),
  KEY `idx_target_node` (`target_node_id`),
  CONSTRAINT `fk_workflow_edges_workflow` FOREIGN KEY (`workflow_id`) REFERENCES `workflows` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_workflow_edges_source` FOREIGN KEY (`source_node_id`) REFERENCES `workflow_nodes` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_workflow_edges_target` FOREIGN KEY (`target_node_id`) REFERENCES `workflow_nodes` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='工作流边表';

-- =====================================================
-- 7. 知识库文档表 (knowledge_documents)
-- =====================================================
CREATE TABLE IF NOT EXISTS `knowledge_documents` (
  `id` VARCHAR(36) NOT NULL COMMENT '文档ID',
  `title` VARCHAR(200) NOT NULL COMMENT '文档标题',
  `type` ENUM('pdf', 'word', 'excel', 'markdown', 'url') NOT NULL COMMENT '文档类型',
  `size` BIGINT NOT NULL DEFAULT 0 COMMENT '文档大小(bytes)',
  `uploaded_by` VARCHAR(36) NOT NULL COMMENT '上传者ID',
  `uploaded_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `status` ENUM('uploading', 'processing', 'ready', 'failed') NOT NULL DEFAULT 'uploading' COMMENT '处理状态',
  `vector_status` ENUM('pending', 'indexing', 'completed') NOT NULL DEFAULT 'pending' COMMENT '向量化状态',
  `chunk_count` INT NOT NULL DEFAULT 0 COMMENT '分块数量',
  `hit_count` INT NOT NULL DEFAULT 0 COMMENT '命中次数',
  `category` VARCHAR(50) NOT NULL COMMENT '分类',
  `file_path` VARCHAR(500) COMMENT '文件存储路径',
  `file_url` VARCHAR(500) COMMENT '文件访问URL',
  PRIMARY KEY (`id`),
  KEY `idx_uploader` (`uploaded_by`),
  KEY `idx_category` (`category`),
  KEY `idx_status` (`status`),
  KEY `idx_vector_status` (`vector_status`),
  KEY `idx_uploaded_at` (`uploaded_at`),
  CONSTRAINT `fk_knowledge_uploader` FOREIGN KEY (`uploaded_by`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='知识库文档表';

-- =====================================================
-- 8. 审计日志表 (audit_logs)
-- =====================================================
CREATE TABLE IF NOT EXISTS `audit_logs` (
  `id` VARCHAR(36) NOT NULL COMMENT '日志ID',
  `user_id` VARCHAR(36) NOT NULL COMMENT '用户ID',
  `user_name` VARCHAR(100) NOT NULL COMMENT '用户姓名',
  `action` VARCHAR(100) NOT NULL COMMENT '操作类型',
  `resource` VARCHAR(50) NOT NULL COMMENT '资源类型',
  `resource_id` VARCHAR(36) COMMENT '资源ID',
  `timestamp` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  `ip` VARCHAR(50) COMMENT 'IP地址',
  `result` ENUM('success', 'failed') NOT NULL COMMENT '操作结果',
  `error_message` TEXT COMMENT '错误信息',
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`),
  KEY `idx_resource` (`resource`),
  KEY `idx_timestamp` (`timestamp`),
  KEY `idx_action` (`action`),
  KEY `idx_result` (`result`),
  CONSTRAINT `fk_audit_logs_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审计日志表';

-- =====================================================
-- 9. 成本记录表 (cost_records)
-- =====================================================
CREATE TABLE IF NOT EXISTS `cost_records` (
  `id` VARCHAR(36) NOT NULL COMMENT '记录ID',
  `department` VARCHAR(50) NOT NULL COMMENT '部门',
  `agent_id` VARCHAR(36) NOT NULL COMMENT 'Agent ID',
  `agent_name` VARCHAR(100) NOT NULL COMMENT 'Agent名称',
  `token_usage` BIGINT NOT NULL DEFAULT 0 COMMENT 'Token使用量',
  `api_calls` INT NOT NULL DEFAULT 0 COMMENT 'API调用次数',
  `cost` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '成本(元)',
  `date` DATE NOT NULL COMMENT '日期',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_agent` (`agent_id`),
  KEY `idx_department` (`department`),
  KEY `idx_date` (`date`),
  CONSTRAINT `fk_cost_records_agent` FOREIGN KEY (`agent_id`) REFERENCES `agents` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='成本记录表';

-- =====================================================
-- 10. Agent收藏表 (agent_favorites)
-- =====================================================
CREATE TABLE IF NOT EXISTS `agent_favorites` (
  `id` VARCHAR(36) NOT NULL COMMENT '收藏ID',
  `user_id` VARCHAR(36) NOT NULL COMMENT '用户ID',
  `agent_id` VARCHAR(36) NOT NULL COMMENT 'Agent ID',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_agent` (`user_id`, `agent_id`),
  KEY `idx_user` (`user_id`),
  KEY `idx_agent` (`agent_id`),
  CONSTRAINT `fk_agent_favorites_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_agent_favorites_agent` FOREIGN KEY (`agent_id`) REFERENCES `agents` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Agent收藏表';

-- =====================================================
-- 11. Agent评分表 (agent_ratings)
-- =====================================================
CREATE TABLE IF NOT EXISTS `agent_ratings` (
  `id` VARCHAR(36) NOT NULL COMMENT '评分ID',
  `agent_id` VARCHAR(36) NOT NULL COMMENT 'Agent ID',
  `user_id` VARCHAR(36) NOT NULL COMMENT '用户ID',
  `rating` DECIMAL(2,1) NOT NULL COMMENT '评分(1-5)',
  `comment` TEXT COMMENT '评价内容',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评价时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_agent_user` (`agent_id`, `user_id`),
  KEY `idx_agent` (`agent_id`),
  KEY `idx_user` (`user_id`),
  KEY `idx_rating` (`rating`),
  CONSTRAINT `fk_agent_ratings_agent` FOREIGN KEY (`agent_id`) REFERENCES `agents` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_agent_ratings_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Agent评分表';

-- =====================================================
-- 12. Agent调用记录表 (agent_call_logs)
-- =====================================================
CREATE TABLE IF NOT EXISTS `agent_call_logs` (
  `id` VARCHAR(36) NOT NULL COMMENT '调用ID',
  `agent_id` VARCHAR(36) NOT NULL COMMENT 'Agent ID',
  `user_id` VARCHAR(36) NOT NULL COMMENT '用户ID',
  `input` TEXT COMMENT '输入内容',
  `output` TEXT COMMENT '输出内容',
  `status` ENUM('success', 'failed', 'timeout') NOT NULL COMMENT '调用状态',
  `duration` INT COMMENT '耗时(ms)',
  `token_usage` INT COMMENT 'Token使用量',
  `error_message` TEXT COMMENT '错误信息',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '调用时间',
  PRIMARY KEY (`id`),
  KEY `idx_agent` (`agent_id`),
  KEY `idx_user` (`user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_created_at` (`created_at`),
  CONSTRAINT `fk_agent_call_logs_agent` FOREIGN KEY (`agent_id`) REFERENCES `agents` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_agent_call_logs_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Agent调用记录表';

-- =====================================================
-- 13. 工作流执行记录表 (workflow_executions)
-- =====================================================
CREATE TABLE IF NOT EXISTS `workflow_executions` (
  `id` VARCHAR(36) NOT NULL COMMENT '执行ID',
  `workflow_id` VARCHAR(36) NOT NULL COMMENT '工作流ID',
  `triggered_by` VARCHAR(36) COMMENT '触发用户ID',
  `trigger_type` ENUM('api', 'cron', 'event') NOT NULL COMMENT '触发方式',
  `status` ENUM('running', 'completed', 'failed', 'cancelled') NOT NULL DEFAULT 'running' COMMENT '执行状态',
  `start_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '开始时间',
  `end_time` DATETIME COMMENT '结束时间',
  `duration` INT COMMENT '耗时(ms)',
  `input_data` JSON COMMENT '输入数据',
  `output_data` JSON COMMENT '输出数据',
  `error_message` TEXT COMMENT '错误信息',
  PRIMARY KEY (`id`),
  KEY `idx_workflow` (`workflow_id`),
  KEY `idx_triggered_by` (`triggered_by`),
  KEY `idx_status` (`status`),
  KEY `idx_start_time` (`start_time`),
  CONSTRAINT `fk_workflow_executions_workflow` FOREIGN KEY (`workflow_id`) REFERENCES `workflows` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_workflow_executions_user` FOREIGN KEY (`triggered_by`) REFERENCES `users` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='工作流执行记录表';

-- =====================================================
-- 初始化数据
-- =====================================================

-- 插入默认管理员用户
INSERT INTO `users` (`id`, `name`, `department`, `role`, `email`, `phone`, `status`) VALUES
('u1', '张三', 'IT部', 'admin', 'zhangsan@company.com', '13800138001', 'active'),
('u2', '李四', '运营部', 'developer', 'lisi@company.com', '13800138002', 'active'),
('u3', '王五', '质控部', 'developer', 'wangwu@company.com', '13800138003', 'active'),
('u4', '赵六', '客服部', 'user', 'zhaoliu@company.com', '13800138004', 'active'),
('u5', '钱七', '财务部', 'user', 'qianqi@company.com', '13800138005', 'inactive')
ON DUPLICATE KEY UPDATE `updated_at` = CURRENT_TIMESTAMP;

-- =====================================================
-- 数据库初始化完成
-- =====================================================
