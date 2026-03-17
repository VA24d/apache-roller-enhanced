/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file for additional
 * information regarding copyright ownership.
 */
package org.apache.roller.weblogger.business.pulse;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.roller.weblogger.pojos.WeblogEntryComment;

/**
 * Indicator 5: Unique Commenter Count.
 * Shows how many distinct people are engaging, plus a diversity ratio
 * (unique/total — high means diverse, low means a few dominating).
 */
public class UniqueCommenterIndicator implements DiscussionIndicator {

    @Override
    public String getName() { return "uniqueCommenters"; }

    @Override
    public String getLabel() { return "Unique Commenter Count"; }

    @Override
    public Map<String, Object> compute(CommentData data) {
        Map<String, Object> result = new HashMap<>();

        Set<String> uniqueNames = new HashSet<>();
        for (WeblogEntryComment comment : data.getComments()) {
            String name = comment.getName();
            if (name == null || name.trim().isEmpty()) {
                name = "Anonymous";
            }
            uniqueNames.add(name.trim().toLowerCase());
        }

        int unique = uniqueNames.size();
        int total = data.getCommentCount();

        result.put("uniqueCount", unique);
        result.put("totalComments", total);

        if (total > 0) {
            double ratio = (double) unique / total;
            result.put("diversityRatio", Math.round(ratio * 100.0) / 100.0);

            String diversityLabel;
            if (ratio >= 0.8) {
                diversityLabel = "Highly Diverse";
            } else if (ratio >= 0.5) {
                diversityLabel = "Moderately Diverse";
            } else if (ratio >= 0.3) {
                diversityLabel = "Low Diversity";
            } else {
                diversityLabel = "Dominated by Few";
            }
            result.put("diversityLabel", diversityLabel);
        }

        return result;
    }
}
