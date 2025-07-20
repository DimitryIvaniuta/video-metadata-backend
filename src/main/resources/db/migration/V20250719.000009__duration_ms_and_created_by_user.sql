-- Add canonical duration in milliseconds (if legacy 'duration' exists you may populate it).
ALTER TABLE videos
    ADD COLUMN duration_millis BIGINT NOT NULL DEFAULT 0;

-- (Optional) Backfill from legacy duration column if it represented seconds:
-- UPDATE videos SET duration_millis = duration * 1000 WHERE duration IS NOT NULL;

-- Add created_by_user_id with FK to users
ALTER TABLE videos
    ADD COLUMN created_by_user_id BIGINT;

ALTER TABLE videos
    ADD CONSTRAINT fk_videos_created_by_user
        FOREIGN KEY (created_by_user_id)
            REFERENCES users (id)
            ON UPDATE CASCADE
            ON DELETE SET NULL;

CREATE INDEX idx_videos_created_by_user ON videos(created_by_user_id);
