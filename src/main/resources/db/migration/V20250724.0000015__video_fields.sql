ALTER TABLE videos RENAME COLUMN duration_millis       TO duration_ms;
ALTER TABLE videos RENAME COLUMN upload_date    TO upload_date_time;

ALTER TABLE videos
  ADD COLUMN external_video_id VARCHAR(255),
  ADD COLUMN external_id      VARCHAR(255),
  ADD COLUMN created_by_user_id BIGINT,
  ADD COLUMN description       TEXT,
  ADD COLUMN imported_at       TIMESTAMPTZ,
  ADD COLUMN created_at        TIMESTAMPTZ,
  ADD COLUMN updated_at        TIMESTAMPTZ,
  ADD COLUMN version           BIGINT DEFAULT 0;

UPDATE videos
SET imported_at = NOW(),
    created_at  = NOW(),
    updated_at  = NOW();

ALTER TABLE videos
    ALTER COLUMN provider       SET NOT NULL,
ALTER COLUMN duration_ms    SET NOT NULL,
  ALTER COLUMN upload_date_time SET NOT NULL,
  ALTER COLUMN title          SET NOT NULL;

CREATE INDEX idx_videos_imported_at       ON videos(imported_at);
