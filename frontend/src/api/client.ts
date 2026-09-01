/** 后端 API 访问层，统一维护业务数据类型、请求处理和附件表单构造。 */
const API_BASE = import.meta.env.VITE_API_BASE_URL || '/api';

/** 附件元数据。文件内容保存在对象存储中，不通过列表接口直接返回。 */
export type Attachment = {
  id: number;
  usage: string;
  originalFilename: string;
  contentType?: string;
  sizeBytes: number;
  createdAt: string;
};

/** 企业知识库条目。 */
export type KnowledgeItem = {
  id?: number;
  title: string;
  category: string;
  content: string;
  tags?: string;
  attachments?: Attachment[];
};

/** 招投标项目及归档状态。 */
export type BidProject = {
  id?: number;
  projectName: string;
  tenderNo?: string;
  tenderer?: string;
  agency?: string;
  industry?: string;
  region?: string;
  budgetAmount?: number;
  bidAmount?: number;
  deadline?: string;
  status?: string;
  result?: string;
  ownerName?: string;
  notes?: string;
};

/** 审标报告中的单个风险问题。 */
export type ReviewIssue = {
  category: string;
  severity: string;
  requirement: string;
  finding: string;
  suggestion: string;
  source: string;
};

/** AI 审标接口返回结果。 */
export type ReviewResponse = {
  summary: string;
  riskLevel: string;
  issues: ReviewIssue[];
  checklist: string[];
};

/** AI 编标接口返回的章节初稿。 */
export type DraftResponse = {
  title: string;
  section: string;
  content: string;
};

/**
 * 发送后端请求并统一处理错误和 JSON 响应。
 *
 * @param path 相对于 API_BASE 的接口路径
 * @param options Fetch 请求配置
 */
async function request<T>(path: string, options?: RequestInit): Promise<T> {
  const isFormData = options?.body instanceof FormData;
  const response = await fetch(`${API_BASE}${path}`, {
    // 浏览器会为 FormData 自动生成包含 boundary 的 Content-Type，手工设置会导致后端无法解析附件。
    headers: isFormData ? { ...(options?.headers || {}) } : { 'Content-Type': 'application/json', ...(options?.headers || {}) },
    ...options,
  });
  if (!response.ok) {
    const message = await response.text();
    throw new Error(message || `HTTP ${response.status}`);
  }
  if (response.status === 204) {
    return undefined as T;
  }
  return response.json() as Promise<T>;
}

/** 将文件集合按后端约定的字段名追加到 multipart 表单。 */
function appendFiles(formData: FormData, key: string, files: File[]) {
  files.forEach((file) => formData.append(key, file));
}

/** 供各业务视图调用的后端接口集合。 */
export const api = {
  health: () => request<{ status: string }>('/health'),
  listKnowledge: (keyword = '') => request<KnowledgeItem[]>(`/knowledge${keyword ? `?keyword=${encodeURIComponent(keyword)}` : ''}`),
  createKnowledge: (payload: KnowledgeItem) => request<KnowledgeItem>('/knowledge', { method: 'POST', body: JSON.stringify(payload) }),
  createKnowledgeWithFiles: (payload: KnowledgeItem, files: File[]) => {
    // 含附件请求使用 multipart；纯文本请求继续使用 JSON，避免额外的表单解析开销。
    const formData = new FormData();
    formData.append('title', payload.title);
    formData.append('category', payload.category);
    formData.append('content', payload.content);
    if (payload.tags) formData.append('tags', payload.tags);
    appendFiles(formData, 'files', files);
    return request<KnowledgeItem>('/knowledge/with-files', { method: 'POST', body: formData });
  },
  listProjects: () => request<BidProject[]>('/projects'),
  createProject: (payload: BidProject) => request<BidProject>('/projects', { method: 'POST', body: JSON.stringify(payload) }),
  archive: (id: number) => request<{ project: BidProject; reviews: unknown[]; drafts: unknown[] }>(`/projects/${id}/archive`),
  review: (payload: { projectId?: number; title: string; tenderText: string; bidText: string }) =>
    request<ReviewResponse>('/ai/review', { method: 'POST', body: JSON.stringify(payload) }),
  reviewWithFiles: (payload: { projectId?: number; title: string; tenderText: string; bidText: string }, tenderFiles: File[], bidFiles: File[]) => {
    const formData = new FormData();
    if (payload.projectId) formData.append('projectId', String(payload.projectId));
    formData.append('title', payload.title);
    formData.append('tenderText', payload.tenderText);
    formData.append('bidText', payload.bidText);
    appendFiles(formData, 'tenderFiles', tenderFiles);
    appendFiles(formData, 'bidFiles', bidFiles);
    return request<ReviewResponse>('/ai/review-with-files', { method: 'POST', body: formData });
  },
  draft: (payload: { projectId?: number; title: string; section: string; tenderText: string; knowledgeContext?: string; userRequirement?: string }) =>
    request<DraftResponse>('/ai/draft', { method: 'POST', body: JSON.stringify(payload) }),
  draftWithFiles: (
    payload: { projectId?: number; title: string; section: string; tenderText: string; knowledgeContext?: string; userRequirement?: string },
    tenderFiles: File[],
    materialFiles: File[],
  ) => {
    const formData = new FormData();
    if (payload.projectId) formData.append('projectId', String(payload.projectId));
    formData.append('title', payload.title);
    formData.append('section', payload.section);
    formData.append('tenderText', payload.tenderText);
    if (payload.knowledgeContext) formData.append('knowledgeContext', payload.knowledgeContext);
    if (payload.userRequirement) formData.append('userRequirement', payload.userRequirement);
    appendFiles(formData, 'tenderFiles', tenderFiles);
    appendFiles(formData, 'materialFiles', materialFiles);
    return request<DraftResponse>('/ai/draft-with-files', { method: 'POST', body: formData });
  },
};
