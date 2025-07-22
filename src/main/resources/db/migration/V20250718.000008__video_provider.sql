-- Adds integer (ordinal) columns for provider to videos table.

ALTER TABLE videos
    ADD COLUMN IF NOT EXISTS provider SMALLINT NOT NULL DEFAULT 0;

-- add simple check constraints to bound future values.
-- Adjust upper bounds when you add more enum constants.
ALTER TABLE videos
    ADD CONSTRAINT chk_videos_provider_range CHECK (video_provider >= 0);

-- Indexes to accelerate provider filtering:
CREATE INDEX idx_videos_provider ON videos(video_provider);