/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file for additional
 * information regarding copyright ownership.
 */
package org.apache.roller.weblogger.business.pulse;

import java.io.Serializable;

/**
 * Wrapper that stores a PulseResult alongside the comment count at cache time.
 * Used to decide whether to invalidate: if 20+ new comments have arrived
 * since the result was cached, the entry is considered stale.
 */
public class CachedPulseEntry implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final int STALE_COMMENT_THRESHOLD = 20;

    private final PulseResult result;
    private final int commentCountAtCacheTime;
    private final long cachedAtMillis;

    public CachedPulseEntry(PulseResult result, int commentCount) {
        this.result = result;
        this.commentCountAtCacheTime = commentCount;
        this.cachedAtMillis = System.currentTimeMillis();
    }

    public PulseResult getResult() {
        return result;
    }

    public int getCommentCountAtCacheTime() {
        return commentCountAtCacheTime;
    }

    public long getCachedAtMillis() {
        return cachedAtMillis;
    }

    /**
     * Returns true if the current comment count exceeds the cached count
     * by at least {@value #STALE_COMMENT_THRESHOLD}.
     */
    public boolean isStaleByComments(int currentCommentCount) {
        return (currentCommentCount - commentCountAtCacheTime) >= STALE_COMMENT_THRESHOLD;
    }
}
