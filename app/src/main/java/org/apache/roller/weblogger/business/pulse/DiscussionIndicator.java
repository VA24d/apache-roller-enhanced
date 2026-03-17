/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file for additional
 * information regarding copyright ownership.
 */
package org.apache.roller.weblogger.business.pulse;

import java.util.Map;

/**
 * Interface for lightweight discussion indicators (Task 6A).
 * Each indicator computes a classical, computationally inexpensive metric
 * from a list of comments — no LLM involvement.
 */
public interface DiscussionIndicator {

    /** Short machine-readable name, e.g. "activityLevel". */
    String getName();

    /** Human-readable label for the dashboard, e.g. "Discussion Activity Level". */
    String getLabel();

    /**
     * Compute the indicator from the given comment data.
     *
     * @param data pre-fetched comment data for a single weblog entry
     * @return a map of result keys to display values
     */
    Map<String, Object> compute(CommentData data);
}
