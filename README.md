# AI招投标助手

面向企业投标团队的 MVP 工作台，覆盖企业知识库、AI 审标、AI 编标、标书归档四个核心闭环。

## 技术栈

- 前端：Vue 3 + TypeScript + Vite + Element Plus
- 后端：Java 21 + Spring Boot 3 + PostgreSQL
- AI 服务：Python + FastAPI + OpenAI API
- 中间件：PostgreSQL + pgvector、Redis、MinIO
- 部署：Docker Compose

## 本地启动

确保 Docker Desktop 已启动，并在项目根目录存在 `.env.local`，其中包含 `OPENAI_API_KEY`。

```powershell
docker compose --env-file .env.local up -d --build
```

访问：

- 前端：http://localhost:5173
- 后端健康检查：http://localhost:8088/api/health
- AI 服务健康检查：http://localhost:8000/health
- MinIO 控制台：http://localhost:9001

## 开发模式

只启动中间件：

```powershell
docker compose -f infra/docker-compose.middleware.yml --env-file infra/.env.middleware.example up -d postgres redis minio
```

然后分别启动：

```powershell
cd backend
mvn spring-boot:run
```

```powershell
cd ai-service
uvicorn main:app --reload
```

```powershell
cd frontend
npm run dev
```
