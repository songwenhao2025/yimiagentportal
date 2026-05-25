# Nginx 配置说明

本目录提供壹米 AI Agent 门户的 Nginx 反向代理配置，用于**前后端分离部署**。

> 本目录**仅含示例配置**，不含任何机密。实际部署时请按下文替换占位符。

---

## 文件清单

| 文件 | 用途 | 部署位置（建议） |
|------|------|------------------|
| `yimi-portal.conf` | 主 server 配置：托管前端静态资源 + 反代 7 个微服务 | `/etc/nginx/conf.d/yimi-portal.conf` |
| `yimi-proxy.conf` | 公共代理参数片段（被 include 复用） | `/etc/nginx/snippets/yimi-proxy.conf` |

---

## 路由规则

```
浏览器请求                       Nginx                       后端服务
─────────────────────────────────────────────────────────────────
GET  /                       →  静态资源 (index.html)
GET  /assets/*.{js,css,...}  →  静态资源（30 天强缓存）
任意未匹配的路径              →  fallback 到 /index.html (SPA 路由)

POST /api/auth/*             →  127.0.0.1:8081  (auth-service)
POST /api/users/*            →  127.0.0.1:8081  (auth-service)
GET  /api/agents             →  127.0.0.1:8082  (agent-service)
GET  /api/agent-call-logs/*  →  127.0.0.1:8082  (agent-service)
GET  /api/skills/*           →  127.0.0.1:8083  (skill-service)
GET  /api/workflows          →  127.0.0.1:8084  (workflow-service)
GET  /api/workflow-executions/* → 127.0.0.1:8084 (workflow-service)
GET  /api/knowledge/*        →  127.0.0.1:8085  (knowledge-service)
GET  /api/admin/*            →  127.0.0.1:8086  (admin-service)
POST /api/llm/*              →  127.0.0.1:8087  (llm-service, 长超时 + 关闭 buffer)

GET  /healthz                →  返回 "ok"（运维探活）
```

---

## 部署前要替换的占位

打开 `yimi-portal.conf`，按需替换：

| 位置 | 默认值 | 替换为 |
|------|--------|--------|
| `server_name` | `_` | 实际域名，如 `portal.yimi.example.com` |
| `root` | `/var/www/yimi-portal/h5` | 前端静态资源实际路径 |
| `upstream` 中的 `127.0.0.1` | 本机 | 后端如果不在同机，改为后端服务器地址 |
| `client_max_body_size` | `50m` | 视知识库上传文件大小调整 |

> HTTPS 上线时再追加 `listen 443 ssl;` + `ssl_certificate` / `ssl_certificate_key`。本配置不预置证书路径，避免泄露环境信息。

---

## 部署步骤（裸 Linux，不用 Docker 时）

```bash
# 1. 前端打包（在仓库根目录）
npm install
npm run build:h5
# 产物在 dist/build/h5/

# 2. 拷贝静态资源到服务器
scp -r dist/build/h5/* deploy@<server>:/var/www/yimi-portal/h5/

# 3. 拷贝 nginx 配置
scp nginx/yimi-portal.conf deploy@<server>:/etc/nginx/conf.d/
scp nginx/yimi-proxy.conf  deploy@<server>:/etc/nginx/snippets/

# 4. 校验并 reload
sudo nginx -t
sudo nginx -s reload

# 5. 启动 7 个后端微服务（端口 8081–8087），略
```

---

## 部署步骤（Docker 时）

仓库根目录的 `Dockerfile.frontend` 已封装"Node 构建 → Nginx 运行"两阶段：

```bash
# 构建前端镜像（自动跑 npm install + npm run build:h5 + 注入 nginx 配置）
docker build -f Dockerfile.frontend -t yimi-portal-frontend:latest .

# 运行（80 端口对外；后端服务通过 host.docker.internal 或自定义 network 暴露）
docker run -d --name yimi-portal -p 80:80 \
  --add-host=host.docker.internal:host-gateway \
  yimi-portal-frontend:latest
```

> ⚠️ Docker 模式下 `upstream` 的 `127.0.0.1` **不能直接用**，需要改成 `host.docker.internal`（容器访问宿主）或后端服务的 service 名。具体走哪种,等运维明确后再调整。

---

## 与本地开发模式的关系

| 环境 | 前端代理来源 | 改动文件 |
|------|--------------|----------|
| **本地开发** | `vite.config.ts` 的 `server.proxy` | `npm run dev:h5` 直接生效 |
| **生产/测试** | 本目录 `yimi-portal.conf` | 由 Nginx 加载 |

两套代理路由保持一致（都是 `/api/*` → `8081-8087`），前端代码无需感知差异。

---

## 已知后续改动

- HTTPS 证书申请（待运维提供域名 + 证书）
- 限流（`limit_req_zone`）、WAF 规则（视安全要求）
- 监控接入（`stub_status`，配合 Prometheus exporter）
- 多实例后端时把 `upstream` 改为多 server + 健康检查
