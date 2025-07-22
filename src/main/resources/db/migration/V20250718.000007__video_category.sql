-- Adds integer (ordinal) columns for category to videos table.

ALTER TABLE videos
    ADD COLUMN IF NOT EXISTS category SMALLINT NOT NULL DEFAULT 0;

-- add simple check constraints to bound future values.
-- Adjust upper bounds when you add more enum constants.
ALTER TABLE videos
    ADD CONSTRAINT chk_videos_category_range CHECK (video_category >= 0);

-- Indexes to accelerate category filtering:
CREATE INDEX idx_videos_category ON videos(video_category);