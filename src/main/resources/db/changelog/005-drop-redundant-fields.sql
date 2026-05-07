--liquibase formatted sql

--changeset kb-ielts:005-drop-redundant-fields
ALTER TABLE ielts_speaking_topics DROP COLUMN key_vocabulary, DROP COLUMN useful_phrases;
ALTER TABLE ielts_reading_items    DROP COLUMN key_vocabulary;
ALTER TABLE ielts_writing_tasks    DROP COLUMN key_phrases;
