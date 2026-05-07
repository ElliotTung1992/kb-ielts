--liquibase formatted sql

--changeset kb-ielts:007-extend-review-logs
ALTER TABLE ielts_review_logs
    ADD COLUMN IF NOT EXISTS before_status VARCHAR(20),
    ADD COLUMN IF NOT EXISTS after_status VARCHAR(20),
    ADD COLUMN IF NOT EXISTS before_interval_days INT,
    ADD COLUMN IF NOT EXISTS after_interval_days INT,
    ADD COLUMN IF NOT EXISTS before_repetition_count INT,
    ADD COLUMN IF NOT EXISTS after_repetition_count INT,
    ADD COLUMN IF NOT EXISTS before_ease_factor DECIMAL(4,2),
    ADD COLUMN IF NOT EXISTS after_ease_factor DECIMAL(4,2);
