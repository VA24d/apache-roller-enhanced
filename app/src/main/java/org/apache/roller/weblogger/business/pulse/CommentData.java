/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file for additional
 * information regarding copyright ownership.
 */
package org.apache.roller.weblogger.business.pulse;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.roller.weblogger.pojos.WeblogEntryComment;

/**
 * Immutable snapshot of comment data for a single weblog entry.
 * Pre-fetched once and passed to all indicators and breakdown methods
 * to avoid redundant DB queries.
 */
public class CommentData {

    private final String entryId;
    private final String entryTitle;
    private final Timestamp entryPubTime;
    private final List<WeblogEntryComment> comments;

    public CommentData(String entryId, String entryTitle,
                       Timestamp entryPubTime,
                       List<WeblogEntryComment> comments) {
        this.entryId = entryId;
        this.entryTitle = entryTitle;
        this.entryPubTime = entryPubTime;
        this.comments = comments != null
                ? Collections.unmodifiableList(new ArrayList<>(comments))
                : Collections.emptyList();
    }

    public String getEntryId() { return entryId; }

    public String getEntryTitle() { return entryTitle; }

    public Timestamp getEntryPubTime() { return entryPubTime; }

    public List<WeblogEntryComment> getComments() { return comments; }

    public int getCommentCount() { return comments.size(); }

    public boolean isEmpty() { return comments.isEmpty(); }
}
