package com.github.dimitryivaniuta.videometadata.imports;

public final class ImportMetrics {
    private ImportMetrics() { }

    public static final String TIMER_EXECUTION = "import.execution";
    public static final String COUNTER_SUBMISSIONS = "import.submissions";
    public static final String COUNTER_REJECTED = "import.submissions.rejected";
    public static final String COUNTER_DUP_JOB = "import.submissions.duplicate.job";
    public static final String COUNTER_RATE_LIMIT_REJECT = "import.submissions.rate_limited";
    public static final String COUNTER_IMPORTED_VIDEOS = "import.videos.persisted";
    public static final String COUNTER_SKIPPED_DUP = "import.videos.skipped.duplicate";
    public static final String DIST_BATCH_SIZE = "import.batch.size";
    public static final String GAUGE_ACTIVE_JOBS = "import.jobs.active";
    public static final String COUNTER_LOCK_FAIL = "import.jobs.lock.fail";
}
