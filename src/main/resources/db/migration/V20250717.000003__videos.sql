-- Videos table
CREATE TABLE videos
(
    id          BIGINT                   NOT NULL PRIMARY KEY DEFAULT nextval('VM_UNIQUE_ID'),
    title       VARCHAR(255)             NOT NULL,
    source      VARCHAR(100)             NOT NULL,
    duration    BIGINT                   NOT NULL,
    upload_date TIMESTAMP WITH TIME ZONE NOT NULL
);

-- Indexes for performance under high load
CREATE INDEX idx_videos_source        ON videos(source);
CREATE INDEX idx_videos_upload_date   ON videos(upload_date);
