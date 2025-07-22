-- Video import submissions table to track async job progress
CREATE TABLE video_import_submissions
(
    id                     BIGINT                   NOT NULL PRIMARY KEY DEFAULT nextval('VM_UNIQUE_ID'),
    submission_id          VARCHAR(64)              NOT NULL,
    username               VARCHAR(150)             NOT NULL,
    provider               SMALLINT                 NOT NULL             DEFAULT 0,
    forced                 BOOLEAN                  NOT NULL             DEFAULT FALSE,
    external_playlist_id   VARCHAR(255),
    external_playlist_meta JSONB,

    total_requested        INT                      NOT NULL             DEFAULT 0,
    accepted_count         INT                      NOT NULL             DEFAULT 0,
    skipped_duplicates     INT                      NOT NULL             DEFAULT 0,

    status                 SMALLINT                 NOT NULL, -- enum ordinal
    error_message          TEXT,

    queued_at              TIMESTAMP WITH TIME ZONE NOT NULL,
    started_at             TIMESTAMP WITH TIME ZONE,
    finished_at            TIMESTAMP WITH TIME ZONE,

    created_at             TIMESTAMP WITH TIME ZONE NOT NULL             DEFAULT NOW(),
    updated_at             TIMESTAMP WITH TIME ZONE NOT NULL             DEFAULT NOW(),

    requested_count        INT                      NOT NULL             DEFAULT 0,
    accepted_count         INT                      NOT NULL             DEFAULT 0,
    skipped_duplicates     INT                      NOT NULL             DEFAULT 0,
    succeeded_count        INT                      NOT NULL             DEFAULT 0,
    failed_count           INT                      NOT NULL             DEFAULT 0
);

-- Ensure uniqueness of public identifier
CREATE UNIQUE INDEX uq_video_import_submissions_submission_id
    ON video_import_submissions (submission_id);

-- Helper indexes for queries
CREATE INDEX idx_video_import_submissions_username
    ON video_import_submissions (username);

CREATE INDEX idx_video_import_submissions_status
    ON video_import_submissions (status);

-- Trigger to maintain updated_at automatically (PostgreSQL)
CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_video_import_submissions_updated
    BEFORE UPDATE ON video_import_submissions
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();
