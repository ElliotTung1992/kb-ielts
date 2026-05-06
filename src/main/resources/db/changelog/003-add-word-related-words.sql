--liquibase formatted sql

--changeset ielts:003-add-related-words
ALTER TABLE ielts_words ADD COLUMN IF NOT EXISTS related_words TEXT;