CREATE TABLE attachments (
    id BIGSERIAL PRIMARY KEY,
    owner_type VARCHAR(40) NOT NULL,
    owner_id BIGINT NOT NULL,
    usage VARCHAR(80) NOT NULL,
    original_filename VARCHAR(500) NOT NULL,
    object_key VARCHAR(700) NOT NULL,
    content_type VARCHAR(200),
    size_bytes BIGINT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_attachments_owner ON attachments(owner_type, owner_id);
CREATE INDEX idx_attachments_usage ON attachments(usage);
