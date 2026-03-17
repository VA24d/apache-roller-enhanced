/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file for additional
 * information regarding copyright ownership.
 */
package org.apache.roller.weblogger.business.pulse;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.roller.weblogger.pojos.WeblogEntryComment;

/**
 * Indicator 1: Discussion Activity Level.
 * Classifies the discussion as Cold / Warm / Hot / On Fire based on
 * comment count and comments-per-day rate.
 */
public class ActivityLevelIndicator implements DiscussionIndicator {

    @Override
    public String getName() { return "activityLevel"; }

    @Override
    public String getLabel() { return "Discussion Activity Level"; }

    @Override
    public Map<String, Object> compute(CommentData data) {
        Map<String, Object> result = new HashMap<>();

        int count = data.getCommentCount();
        double commentsPerDay = computeCommentsPerDay(data);

        String level;
        if (count == 0) {
            level = "Silent";
        } else if (count <= 3 && commentsPerDay < 0.5) {
            level = "Cold";
        } else if (count <= 10 && commentsPerDay < 2.0) {
            level = "Warm";
        } else if (count <= 30 && commentsPerDay < 5.0) {
            level = "Hot";
        } else {
            level = "On Fire";
        }

        result.put("level", level);
        result.put("totalComments", count);
        result.put("commentsPerDay", Math.round(commentsPerDay * 100.0) / 100.0);

        return result;
    }

    private double computeCommentsPerDay(CommentData data) {
        List<WeblogEntryComment> comments = data.getComments();
        if (comments.isEmpty()) {
            return 0.0;
        }

        Timestamp earliest = data.getEntryPubTime();
        Timestamp latest = comments.get(0).getPostTime();

        if (earliest == null || latest == null) {
            return 0.0;
        }

        // Find actual earliest and latest comment times
        for (WeblogEntryComment c : comments) {
            if (c.getPostTime() != null) {
                if (c.getPostTime().before(earliest)) {
                    earliest = c.getPostTime();
                }
                if (c.getPostTime().after(latest)) {
                    latest = c.getPostTime();
                }
            }
        }

        long diffMs = latest.getTime() - earliest.getTime();
        double days = diffMs / (1000.0 * 60 * 60 * 24);
        if (days < 1.0) {
            days = 1.0; // minimum 1 day to avoid divide-by-zero
        }

        return data.getCommentCount() / days;
    }
}
