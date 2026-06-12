const API_BASE = import.meta.env.VITE_API_BASE_URL || '/api';

export type KnowledgeItem = {
  id?: number;
  title: string;
  category: string;
  content: string;
  tags?: string;
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
  const response = await fetch(`${API_BASE}${path}`, {
    headers: { 'Content-Type': 'application/json', ...(options?.headers || {}) },
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

export const api = {
  health: () => request<{ status: string }>('/health'),
  listKnowledge: (keyword = '') => request<KnowledgeItem[]>(`/knowledge${keyword ? `?keyword=${encodeURIComponent(keyword)}` : ''}`),
  createKnowledge: (payload: KnowledgeItem) => request<KnowledgeItem>('/knowledge', { method: 'POST', body: JSON.stringify(payload) }),
  listProjects: () => request<BidProject[]>('/projects'),
  createProject: (payload: BidProject) => request<BidProject>('/projects', { method: 'POST', body: JSON.stringify(payload) }),
  archive: (id: number) => request<{ project: BidProject; reviews: unknown[]; drafts: unknown[] }>(`/projects/${id}/archive`),
  review: (payload: { projectId?: number; title: string; tenderText: string; bidText: string }) =>
    request<ReviewResponse>('/ai/review', { method: 'POST', body: JSON.stringify(payload) }),
  draft: (payload: { projectId?: number; title: string; section: string; tenderText: string; knowledgeContext?: string; userRequirement?: string }) =>
    request<DraftResponse>('/ai/draft', { method: 'POST', body: JSON.stringify(payload) }),
};
