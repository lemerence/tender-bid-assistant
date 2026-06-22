const API_BASE = import.meta.env.VITE_API_BASE_URL || '/api';

export type Attachment = {
  id: number;
  usage: string;
  originalFilename: string;
  contentType?: string;
  sizeBytes: number;
  createdAt: string;
};

export type KnowledgeItem = {
  id?: number;
  title: string;
  category: string;
  content: string;
  tags?: string;
  attachments?: Attachment[];
};

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

export type ReviewIssue = {
  category: string;
  severity: string;
  requirement: string;
  finding: string;
  suggestion: string;
  source: string;
};

export type ReviewResponse = {
  summary: string;
  riskLevel: string;
  issues: ReviewIssue[];
  checklist: string[];
};

export type DraftResponse = {
  title: string;
  section: string;
  content: string;
};

async function request<T>(path: string, options?: RequestInit): Promise<T> {
  const isFormData = options?.body instanceof FormData;
  const response = await fetch(`${API_BASE}${path}`, {
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

function appendFiles(formData: FormData, key: string, files: File[]) {
  files.forEach((file) => formData.append(key, file));
}

export const api = {
  health: () => request<{ status: string }>('/health'),
  listKnowledge: (keyword = '') => request<KnowledgeItem[]>(`/knowledge${keyword ? `?keyword=${encodeURIComponent(keyword)}` : ''}`),
  createKnowledge: (payload: KnowledgeItem) => request<KnowledgeItem>('/knowledge', { method: 'POST', body: JSON.stringify(payload) }),
  createKnowledgeWithFiles: (payload: KnowledgeItem, files: File[]) => {
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
