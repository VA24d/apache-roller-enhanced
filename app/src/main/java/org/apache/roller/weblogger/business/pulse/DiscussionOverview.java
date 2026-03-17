/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file for additional
 * information regarding copyright ownership.
 */
package org.apache.roller.weblogger.business.pulse;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Aggregates all discussion indicators for a single entry's comments (Task 6A).
 * Computes each registered indicator and collects results.
 */
public class DiscussionOverview {

    private static final Log log = LogFactory.getLog(DiscussionOverview.class);

    private final List<DiscussionIndicator> indicators;

    public DiscussionOverview() {
        this.indicators = new ArrayList<>();
        // Register all 5 indicators
        indicators.add(new ActivityLevelIndicator());
        indicators.add(new ResponseTypeIndicator());
        indicators.add(new RecurringKeywordsIndicator());
        indicators.add(new TopContributorsIndicator());
        indicators.add(new UniqueCommenterIndicator());
    }

    /**
     * Compute all indicators for the given comment data.
     *
     * @param data pre-fetched comments for a single entry
     * @return ordered map of indicator name → result map
     */
    public Map<String, Map<String, Object>> computeAll(CommentData data) {
        Map<String, Map<String, Object>> results = new LinkedHashMap<>();

        for (DiscussionIndicator indicator : indicators) {
            try {
                Map<String, Object> indicatorResult = indicator.compute(data);
                indicatorResult.put("_label", indicator.getLabel());
                results.put(indicator.getName(), indicatorResult);
            } catch (Exception e) {
                log.warn("Error computing indicator: " + indicator.getName(), e);
            }
        }

        return results;
    }

    public List<DiscussionIndicator> getIndicators() {
        return indicators;
    }
}
