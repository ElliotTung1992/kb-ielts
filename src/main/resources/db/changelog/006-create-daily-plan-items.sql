--liquibase formatted sql

--changeset kb-ielts:006-create-daily-plan-items
CREATE TABLE IF NOT EXISTS ielts_daily_plan_items (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    plan_id      UUID NOT NULL REFERENCES ielts_daily_plans(id) ON DELETE CASCADE,
    content_type VARCHAR(30) NOT NULL CHECK (content_type IN ('WORD','PHRASE','PARAPHRASE','PRONUNCIATION','GRAMMAR_POINT','GRAMMAR_EXERCISE','SPEAKING','LISTENING','READING','WRITING')),
    content_id   UUID NOT NULL,
    record_id    UUID REFERENCES ielts_study_records(id) ON DELETE SET NULL,
    study_mode   VARCHAR(10) NOT NULL CHECK (study_mode IN ('NEW','REVIEW')),
    status       VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING','COMPLETED','SKIPPED')),
    summary      TEXT,
    sort_order   INT NOT NULL DEFAULT 0,
    completed_at TIMESTAMPTZ,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (plan_id, content_type, content_id, study_mode)
);

CREATE INDEX IF NOT EXISTS idx_daily_plan_items_plan ON ielts_daily_plan_items (plan_id, sort_order);
CREATE INDEX IF NOT EXISTS idx_daily_plan_items_record ON ielts_daily_plan_items (record_id);
CREATE INDEX IF NOT EXISTS idx_daily_plan_items_content ON ielts_daily_plan_items (content_type, content_id);
