CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE knowledge_items (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    category VARCHAR(80) NOT NULL,
    content TEXT NOT NULL,
    tags VARCHAR(500),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE bid_projects (
    id BIGSERIAL PRIMARY KEY,
    project_name VARCHAR(255) NOT NULL,
    tender_no VARCHAR(120),
    tenderer VARCHAR(255),
    agency VARCHAR(255),
    industry VARCHAR(120),
    region VARCHAR(120),
    budget_amount NUMERIC(18, 2),
    bid_amount NUMERIC(18, 2),
    deadline TIMESTAMPTZ,
    status VARCHAR(40) NOT NULL,
    result VARCHAR(40),
    owner_name VARCHAR(80),
    notes TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE review_reports (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT REFERENCES bid_projects(id) ON DELETE SET NULL,
    title VARCHAR(255) NOT NULL,
    risk_level VARCHAR(40) NOT NULL,
    tender_text TEXT NOT NULL,
    bid_text TEXT NOT NULL,
    report_json TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE draft_documents (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT REFERENCES bid_projects(id) ON DELETE SET NULL,
    title VARCHAR(255) NOT NULL,
    section VARCHAR(120) NOT NULL,
    prompt TEXT NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_knowledge_items_category ON knowledge_items(category);
CREATE INDEX idx_bid_projects_status ON bid_projects(status);
CREATE INDEX idx_review_reports_project_id ON review_reports(project_id);
CREATE INDEX idx_draft_documents_project_id ON draft_documents(project_id);
