-- Add external video identifier (nullable first, then optionally backfill, then constrain)
ALTER TABLE videos
    ADD COLUMN external_video_id VARCHAR(255);

-- If you can immediately guarantee a value for all rows, enforce NOT NULL:
-- UPDATE videos SET external_video_id = CONCAT('LEGACY-', id) WHERE external_video_id IS NULL;
-- ALTER TABLE videos ALTER COLUMN external_video_id SET NOT NULL;

-- Create a unique constraint to prevent duplicate imports per provider + external id.
-- We rely on video_provider integer ordinal + external_video_id
ALTER TABLE videos
    ADD CONSTRAINT uq_videos_provider_externalid
        UNIQUE (video_provider, external_video_id);

-- Supporting index (Postgres can use the unique index; separate index not strictly required)
-- But if you plan partial queries filtering only by external_video_id:
CREATE INDEX idx_videos_external_video_id ON videos(external_video_id);
