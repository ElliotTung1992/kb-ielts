--liquibase formatted sql

--changeset kb-ielts:004-create-content-links
CREATE TABLE ielts_content_links (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    source_type VARCHAR(30) NOT NULL CHECK (source_type IN ('LISTENING','READING','WRITING','SPEAKING')),
    source_id   UUID NOT NULL,
    target_type VARCHAR(30) NOT NULL CHECK (target_type IN ('WORD','PHRASE','PARAPHRASE','PRONUNCIATION','GRAMMAR_POINT')),
    target_id   UUID NOT NULL,
    link_type   VARCHAR(30) CHECK (link_type IN ('vocabulary','paraphrase','grammar','pronunciation','signal')),
    note        TEXT,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (source_type, source_id, target_type, target_id)
);
CREATE INDEX idx_links_source ON ielts_content_links (source_type, source_id);
CREATE INDEX idx_links_target ON ielts_content_links (target_type, target_id);
