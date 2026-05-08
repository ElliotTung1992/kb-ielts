--liquibase formatted sql

--changeset kb-ielts:009-create-topic-tags
CREATE TABLE IF NOT EXISTS ielts_topic_tags (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tag_name    VARCHAR(80) NOT NULL UNIQUE,
    category    VARCHAR(50),
    skill_tags  VARCHAR(100),
    description TEXT,
    usage_count INT NOT NULL DEFAULT 0,
    enabled     BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_topic_tags_enabled ON ielts_topic_tags (enabled, tag_name);
CREATE INDEX IF NOT EXISTS idx_topic_tags_category ON ielts_topic_tags (category);
