/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file for additional
 * information regarding copyright ownership.
 */
package org.apache.roller.weblogger.business.pulse;

/**
 * Strategy interface for conversation breakdown methods (Task 6B).
 * Implementations provide different approaches to extracting themes,
 * representative comments, and generating recaps from comment data.
 *
 * Design pattern: Strategy — allows switching between methods
 * (TF-IDF local vs Hybrid LLM) depending on available resources.
 */
public interface BreakdownStrategy {

    /** Machine-readable strategy name. */
    String getName();

    /** Human-readable description. */
    String getDescription();

    /**
     * Analyze comments and produce a conversation breakdown.
     *
     * @param data pre-fetched comment data for a single entry
     * @return breakdown with themes, representative comments, and recap
     */
    ConversationBreakdown analyze(CommentData data);
}
