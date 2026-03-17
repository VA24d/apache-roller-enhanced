/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file for additional
 * information regarding copyright ownership.
 */
package org.apache.roller.weblogger.business.pulse;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.roller.weblogger.pojos.WeblogEntryComment;

/**
 * Indicator 4: Top Contributors.
 * Shows the top 3 most active commenters with their comment counts.
 */
public class TopContributorsIndicator implements DiscussionIndicator {

    private static final int MAX_CONTRIBUTORS = 3;

    @Override
    public String getName() { return "topContributors"; }

    @Override
    public String getLabel() { return "Top Contributors"; }

    @Override
    public Map<String, Object> compute(CommentData data) {
        Map<String, Object> result = new HashMap<>();

        Map<String, Integer> nameCounts = new HashMap<>();
        for (WeblogEntryComment comment : data.getComments()) {
            String name = comment.getName();
            if (name == null || name.trim().isEmpty()) {
                name = "Anonymous";
            }
            nameCounts.merge(name.trim(), 1, Integer::sum);
        }

        List<Map.Entry<String, Integer>> sorted = nameCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder()))
                .limit(MAX_CONTRIBUTORS)
                .collect(Collectors.toList());

        List<Map<String, Object>> contributors = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : sorted) {
            Map<String, Object> contrib = new LinkedHashMap<>();
            contrib.put("name", entry.getKey());
            contrib.put("commentCount", entry.getValue());
            contributors.add(contrib);
        }

        result.put("contributors", contributors);
        result.put("totalContributors", nameCounts.size());

        return result;
    }
}
