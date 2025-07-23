ALTER TABLE video_import_submissions
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0;