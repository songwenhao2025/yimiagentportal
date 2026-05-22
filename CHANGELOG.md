# 更新日志 (CHANGELOG)

本文件记录壹米 AI Agent 门户项目的重要改动。格式参考 [Keep a Changelog](https://keepachangelog.com/zh-CN/)。

## [未发布]

### 修复 (Fixed)

- **登录功能：修复全新部署环境下所有用户无法登录的问题。**
  - 现象：使用默认账号（如 `zhangsan@company.com` / `123456`）登录，接口返回 `401 邮箱或密码错误`。
  - 根因：`database/schema.sql` 预置的 5 个用户 INSERT 语句缺少 `password` 列，导致用户密码为 `NULL`；而 `auth-service` 的 `DataInitializer` 仅在用户表为空时才会创建带密码的用户。二者冲突，使得任何全新部署的环境都没有可用的登录凭证。
  - 修复：在 `schema.sql` 的用户 INSERT 中补全 `password` 字段（BCrypt 加密的默认密码 `123456`），并在 `ON DUPLICATE KEY UPDATE` 中同步修复 `password`，确保对已存在脏数据的环境重复执行脚本也能修正。
