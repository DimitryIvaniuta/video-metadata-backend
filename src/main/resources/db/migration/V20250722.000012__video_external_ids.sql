-- the collection table:
CREATE TABLE IF NOT EXISTS video_import_submission_external_ids
(
    submission_fk BIGINT      NOT NULL,
    ord           INT         NOT NULL,
    external_id   VARCHAR(200) NOT NULL,
    PRIMARY KEY (submission_fk, ord),
    CONSTRAINT fk_vid_imp_sub_ids_submission
    FOREIGN KEY (submission_fk) REFERENCES video_import_submissions(id) ON DELETE CASCADE
);

-- Optional index to speed up duplicate checks per submission
CREATE INDEX IF NOT EXISTS idx_vid_imp_sub_ids_external_id
    ON video_import_submission_external_ids (external_id);