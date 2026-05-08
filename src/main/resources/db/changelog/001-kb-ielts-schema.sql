--liquibase formatted sql

-- 001-create-ielts-tables.sql
-- Core IELTS content, study records, review logs and daily plans.

--changeset ielts:001-words
CREATE TABLE IF NOT EXISTS ielts_words (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    word            VARCHAR(100) NOT NULL UNIQUE,
    phonetic_uk     VARCHAR(100),
    phonetic_us     VARCHAR(100),
    part_of_speech  VARCHAR(20),
    definition_zh   TEXT,
    definition_en   TEXT,
    example_sentence        TEXT,
    example_translation     TEXT,
    frequency_level SMALLINT DEFAULT 3 CHECK (frequency_level BETWEEN 1 AND 5),
    word_list       VARCHAR(50),
    difficulty      SMALLINT DEFAULT 2 CHECK (difficulty BETWEEN 1 AND 3),
    skill_tags      VARCHAR(100),
    topic_tags      VARCHAR(255),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

--changeset ielts:001-phrases
CREATE TABLE IF NOT EXISTS ielts_phrases (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    phrase              VARCHAR(300) NOT NULL UNIQUE,
    meaning_zh          TEXT,
    usage_note          TEXT,
    example_sentence    TEXT,
    example_translation TEXT,
    category            VARCHAR(50),
    difficulty          SMALLINT DEFAULT 2 CHECK (difficulty BETWEEN 1 AND 3),
    skill_tags          VARCHAR(100),
    topic_tags          VARCHAR(255),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

--changeset ielts:001-paraphrase-groups
CREATE TABLE IF NOT EXISTS ielts_paraphrase_groups (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    group_name          VARCHAR(200) NOT NULL,
    core_expression     VARCHAR(200) NOT NULL,
    synonyms            TEXT,
    usage_note          TEXT,
    example_original    TEXT,
    example_paraphrased TEXT,
    difficulty          SMALLINT DEFAULT 2 CHECK (difficulty BETWEEN 1 AND 3),
    skill_tags          VARCHAR(100),
    topic_tags          VARCHAR(255),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

--changeset ielts:001-pronunciation-points
CREATE TABLE IF NOT EXISTS ielts_pronunciation_points (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title           VARCHAR(200) NOT NULL,
    category        VARCHAR(50) CHECK (category IN ('stress','linking','weak-form','intonation','elision','assimilation')),
    explanation_zh  TEXT,
    rule_summary    TEXT,
    examples        TEXT,
    common_mistakes TEXT,
    difficulty      SMALLINT DEFAULT 2 CHECK (difficulty BETWEEN 1 AND 3),
    skill_tags      VARCHAR(100),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

--changeset ielts:001-grammar-points
CREATE TABLE IF NOT EXISTS ielts_grammar_points (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title           VARCHAR(200) NOT NULL,
    category        VARCHAR(50),
    explanation_zh  TEXT,
    explanation_en  TEXT,
    key_rules       TEXT,
    examples        TEXT,
    common_errors   TEXT,
    difficulty      SMALLINT DEFAULT 2 CHECK (difficulty BETWEEN 1 AND 3),
    skill_tags      VARCHAR(100),
    topic_tags      VARCHAR(255),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

--changeset ielts:001-grammar-exercises
CREATE TABLE IF NOT EXISTS ielts_grammar_exercises (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    grammar_point_id UUID REFERENCES ielts_grammar_points(id) ON DELETE SET NULL,
    question_type    VARCHAR(30) NOT NULL CHECK (question_type IN ('fill-in-blank','error-correction','sentence-transformation','multiple-choice')),
    question         TEXT NOT NULL,
    options          TEXT,
    answer           TEXT NOT NULL,
    explanation      TEXT,
    difficulty       SMALLINT DEFAULT 2 CHECK (difficulty BETWEEN 1 AND 3),
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_grammar_exercises_point ON ielts_grammar_exercises (grammar_point_id);

--changeset ielts:001-speaking-topics
CREATE TABLE IF NOT EXISTS ielts_speaking_topics (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title            VARCHAR(200) NOT NULL,
    part             SMALLINT NOT NULL CHECK (part IN (1, 2, 3)),
    question         TEXT NOT NULL,
    reference_answer TEXT,
    key_vocabulary   TEXT,
    useful_phrases   TEXT,
    difficulty       SMALLINT DEFAULT 2 CHECK (difficulty BETWEEN 1 AND 3),
    topic_tags       VARCHAR(255),
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT now()
);

--changeset ielts:001-listening-items
CREATE TABLE IF NOT EXISTS ielts_listening_items (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title               VARCHAR(200) NOT NULL,
    section             SMALLINT NOT NULL CHECK (section BETWEEN 1 AND 4),
    question_type       VARCHAR(50),
    context_description TEXT,
    script_excerpt      TEXT,
    questions           TEXT,
    answers             TEXT,
    tips                TEXT,
    difficulty          SMALLINT DEFAULT 2 CHECK (difficulty BETWEEN 1 AND 3),
    topic_tags          VARCHAR(255),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

--changeset ielts:001-reading-items
CREATE TABLE IF NOT EXISTS ielts_reading_items (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title           VARCHAR(300) NOT NULL,
    training_type   VARCHAR(20) NOT NULL DEFAULT 'ACADEMIC' CHECK (training_type IN ('ACADEMIC','GENERAL')),
    difficulty      SMALLINT DEFAULT 2 CHECK (difficulty BETWEEN 1 AND 3),
    passage_text    TEXT,
    question_type   VARCHAR(50),
    questions       TEXT,
    answers         TEXT,
    key_vocabulary  TEXT,
    tips            TEXT,
    topic_tags      VARCHAR(255),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

--changeset ielts:001-writing-tasks
CREATE TABLE IF NOT EXISTS ielts_writing_tasks (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title             VARCHAR(200) NOT NULL,
    task_number       SMALLINT NOT NULL CHECK (task_number IN (1, 2)),
    training_type     VARCHAR(20) NOT NULL DEFAULT 'ACADEMIC' CHECK (training_type IN ('ACADEMIC','GENERAL')),
    task_type         VARCHAR(50),
    prompt            TEXT NOT NULL,
    image_description TEXT,
    model_answer      TEXT,
    band_score_note   TEXT,
    key_phrases       TEXT,
    difficulty        SMALLINT DEFAULT 2 CHECK (difficulty BETWEEN 1 AND 3),
    topic_tags        VARCHAR(255),
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT now()
);

--changeset ielts:001-study-records
CREATE TABLE IF NOT EXISTS ielts_study_records (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    content_type     VARCHAR(30) NOT NULL CHECK (content_type IN ('WORD','PHRASE','PARAPHRASE','PRONUNCIATION','GRAMMAR_POINT','GRAMMAR_EXERCISE','SPEAKING','LISTENING','READING','WRITING')),
    content_id       UUID NOT NULL,
    status           VARCHAR(20) NOT NULL DEFAULT 'LEARNING' CHECK (status IN ('LEARNING','REVIEWING','MASTERED')),
    ease_factor      DECIMAL(4,2) NOT NULL DEFAULT 2.50,
    interval_days    INT NOT NULL DEFAULT 1,
    repetition_count INT NOT NULL DEFAULT 0,
    next_review_at   DATE,
    last_reviewed_at TIMESTAMPTZ,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (content_type, content_id)
);
CREATE INDEX IF NOT EXISTS idx_study_records_review ON ielts_study_records (next_review_at, status);
CREATE INDEX IF NOT EXISTS idx_study_records_type ON ielts_study_records (content_type, status);

--changeset ielts:001-review-logs
CREATE TABLE IF NOT EXISTS ielts_review_logs (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    record_id   UUID NOT NULL REFERENCES ielts_study_records(id) ON DELETE CASCADE,
    rating      VARCHAR(10) NOT NULL CHECK (rating IN ('AGAIN','HARD','GOOD','EASY')),
    reviewed_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_review_logs_date ON ielts_review_logs (reviewed_at);

--changeset ielts:001-daily-plans
CREATE TABLE IF NOT EXISTS ielts_daily_plans (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    plan_date       DATE NOT NULL UNIQUE,
    total_items     INT NOT NULL DEFAULT 0,
    completed_items INT NOT NULL DEFAULT 0,
    generated_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- 002-create-ielts-examples.sql
-- Shared examples table and legacy example migration.

--changeset ielts:002-create-examples
CREATE TABLE IF NOT EXISTS ielts_examples (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    content_type VARCHAR(30) NOT NULL,
    content_id   UUID NOT NULL,
    sentence     TEXT,
    translation  TEXT,
    note         TEXT,
    sort_order   INT NOT NULL DEFAULT 0,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_examples_content ON ielts_examples (content_type, content_id);

--changeset ielts:002-migrate-word-examples
INSERT INTO ielts_examples (id, content_type, content_id, sentence, translation, sort_order, created_at)
SELECT gen_random_uuid(), 'WORD', id, example_sentence, example_translation, 0, now()
FROM ielts_words
WHERE example_sentence IS NOT NULL;

--changeset ielts:002-migrate-phrase-examples
INSERT INTO ielts_examples (id, content_type, content_id, sentence, translation, sort_order, created_at)
SELECT gen_random_uuid(), 'PHRASE', id, example_sentence, example_translation, 0, now()
FROM ielts_phrases
WHERE example_sentence IS NOT NULL;

--changeset ielts:002-migrate-paraphrase-examples
INSERT INTO ielts_examples (id, content_type, content_id, sentence, translation, sort_order, created_at)
SELECT gen_random_uuid(), 'PARAPHRASE', id, example_original, example_paraphrased, 0, now()
FROM ielts_paraphrase_groups
WHERE example_original IS NOT NULL;

--changeset ielts:002-migrate-pronunciation-examples
INSERT INTO ielts_examples (id, content_type, content_id, sentence, translation, sort_order, created_at)
SELECT gen_random_uuid(), 'PRONUNCIATION', id, examples, NULL, 0, now()
FROM ielts_pronunciation_points
WHERE examples IS NOT NULL;

--changeset ielts:002-migrate-grammar-examples
INSERT INTO ielts_examples (id, content_type, content_id, sentence, translation, sort_order, created_at)
SELECT gen_random_uuid(), 'GRAMMAR_POINT', id, examples, NULL, 0, now()
FROM ielts_grammar_points
WHERE examples IS NOT NULL;

--changeset ielts:002-drop-word-example-columns
ALTER TABLE ielts_words DROP COLUMN IF EXISTS example_sentence;
ALTER TABLE ielts_words DROP COLUMN IF EXISTS example_translation;

--changeset ielts:002-drop-phrase-example-columns
ALTER TABLE ielts_phrases DROP COLUMN IF EXISTS example_sentence;
ALTER TABLE ielts_phrases DROP COLUMN IF EXISTS example_translation;

--changeset ielts:002-drop-paraphrase-example-columns
ALTER TABLE ielts_paraphrase_groups DROP COLUMN IF EXISTS example_original;
ALTER TABLE ielts_paraphrase_groups DROP COLUMN IF EXISTS example_paraphrased;

--changeset ielts:002-drop-pronunciation-example-columns
ALTER TABLE ielts_pronunciation_points DROP COLUMN IF EXISTS examples;

--changeset ielts:002-drop-grammar-example-columns
ALTER TABLE ielts_grammar_points DROP COLUMN IF EXISTS examples;

-- 003-add-word-related-words.sql
-- Word related words extension.

--changeset ielts:003-add-related-words
ALTER TABLE ielts_words ADD COLUMN IF NOT EXISTS related_words TEXT;

-- 004-create-content-links.sql
-- Cross-skill content links.

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

-- 005-drop-redundant-fields.sql
-- Drop fields replaced by content links.

--changeset kb-ielts:005-drop-redundant-fields
ALTER TABLE ielts_speaking_topics DROP COLUMN key_vocabulary, DROP COLUMN useful_phrases;
ALTER TABLE ielts_reading_items    DROP COLUMN key_vocabulary;
ALTER TABLE ielts_writing_tasks    DROP COLUMN key_phrases;

-- 006-create-daily-plan-items.sql
-- Persisted daily plan item details.

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

-- 007-extend-review-logs.sql
-- Review algorithm before/after snapshots.

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

-- 008-create-business-features.sql
-- Business features: profile, mistakes, mocks, quality, writing and speaking assets.

--changeset kb-ielts:008-create-business-features
CREATE TABLE IF NOT EXISTS ielts_study_profile (
    id                     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    target_overall_score   DECIMAL(3,1),
    target_listening_score DECIMAL(3,1),
    target_reading_score   DECIMAL(3,1),
    target_writing_score   DECIMAL(3,1),
    target_speaking_score  DECIMAL(3,1),
    current_overall_score  DECIMAL(3,1),
    exam_date              DATE,
    daily_minutes          INT NOT NULL DEFAULT 60,
    training_type          VARCHAR(20) DEFAULT 'ACADEMIC' CHECK (training_type IN ('ACADEMIC','GENERAL')),
    accent_preference      VARCHAR(20) DEFAULT 'UK',
    focus_skills           VARCHAR(100),
    created_at             TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at             TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS ielts_mistake_logs (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    content_type VARCHAR(30) NOT NULL,
    content_id   UUID NOT NULL,
    record_id    UUID REFERENCES ielts_study_records(id) ON DELETE SET NULL,
    mistake_type VARCHAR(50) NOT NULL,
    note         TEXT,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_mistake_logs_type_date ON ielts_mistake_logs (mistake_type, created_at);
CREATE INDEX IF NOT EXISTS idx_mistake_logs_content ON ielts_mistake_logs (content_type, content_id);
CREATE INDEX IF NOT EXISTS idx_mistake_logs_record ON ielts_mistake_logs (record_id);

CREATE TABLE IF NOT EXISTS ielts_mock_tests (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    test_date     DATE NOT NULL,
    source        VARCHAR(100),
    overall_score DECIMAL(3,1),
    notes         TEXT,
    next_focus    TEXT,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS ielts_mock_test_sections (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    mock_test_id  UUID NOT NULL REFERENCES ielts_mock_tests(id) ON DELETE CASCADE,
    skill         VARCHAR(20) NOT NULL CHECK (skill IN ('LISTENING','READING','WRITING','SPEAKING')),
    score         DECIMAL(3,1),
    raw_score     INT,
    question_count INT,
    wrong_count   INT,
    main_issues   TEXT,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_mock_sections_test ON ielts_mock_test_sections (mock_test_id);

CREATE TABLE IF NOT EXISTS ielts_content_quality (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    content_type      VARCHAR(30) NOT NULL,
    content_id        UUID NOT NULL,
    source            VARCHAR(100),
    quality_status    VARCHAR(20) NOT NULL DEFAULT 'VERIFIED' CHECK (quality_status IN ('DRAFT','VERIFIED','NEEDS_REVIEW','ARCHIVED')),
    quality_score     SMALLINT DEFAULT 3 CHECK (quality_score BETWEEN 1 AND 5),
    verified_at       TIMESTAMPTZ,
    last_practiced_at TIMESTAMPTZ,
    usage_count       INT NOT NULL DEFAULT 0,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (content_type, content_id)
);

CREATE TABLE IF NOT EXISTS ielts_writing_submissions (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    task_id          UUID REFERENCES ielts_writing_tasks(id) ON DELETE SET NULL,
    original_text    TEXT NOT NULL,
    revised_text     TEXT,
    target_band      DECIMAL(3,1),
    estimated_band   DECIMAL(3,1),
    feedback         TEXT,
    criteria_scores  TEXT,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS ielts_speaking_materials (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    category         VARCHAR(50) NOT NULL,
    title            VARCHAR(200) NOT NULL,
    content          TEXT NOT NULL,
    topic_tags       VARCHAR(255),
    usable_for_parts VARCHAR(20),
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT now()
);
