/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file for additional
 * information regarding copyright ownership.
 */
package org.apache.roller.weblogger.business.pulse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.config.WebloggerConfig;

/**
 * Dynamically selects the appropriate breakdown strategy based on:
 * 1. Number of comments (small sets don't need LLM overhead)
 * 2. Configuration (whether LLM is enabled/available)
 *
 * Design pattern: Factory + Strategy — the selector acts as a factory
 * that chooses the right strategy implementation at runtime.
 *
 * Thresholds:
 * - 0-5 comments:   TF-IDF only (too few for meaningful clustering)
 * - 6-30 comments:   TF-IDF by default; Hybrid if LLM configured
 * - 31+ comments:    Hybrid preferred (LLM adds value for large discussions);
 *                     falls back to TF-IDF if LLM unavailable
 */
public class BreakdownStrategySelector {

    private static final Log log = LogFactory.getLog(BreakdownStrategySelector.class);

    private static final int SMALL_THRESHOLD = 5;
    private static final int MEDIUM_THRESHOLD = 30;

    private final TfIdfBreakdownStrategy tfidfStrategy = new TfIdfBreakdownStrategy();
    private final HybridLlmBreakdownStrategy hybridStrategy = new HybridLlmBreakdownStrategy();

    /**
     * Select the best strategy for the given comment count.
     */
    public BreakdownStrategy selectStrategy(int commentCount) {
        boolean llmAvailable = isLlmConfigured();

        if (commentCount <= SMALL_THRESHOLD) {
            log.debug("Small comment set (" + commentCount + "), using TF-IDF");
            return tfidfStrategy;
        }

        if (commentCount <= MEDIUM_THRESHOLD) {
            if (llmAvailable) {
                log.debug("Medium comment set (" + commentCount + "), LLM available, using Hybrid");
                return hybridStrategy;
            }
            log.debug("Medium comment set (" + commentCount + "), no LLM, using TF-IDF");
            return tfidfStrategy;
        }

        // Large comment sets benefit most from LLM polish
        if (llmAvailable) {
            log.debug("Large comment set (" + commentCount + "), using Hybrid");
            return hybridStrategy;
        }

        log.debug("Large comment set (" + commentCount + "), no LLM, using TF-IDF");
        return tfidfStrategy;
    }

    /**
     * Force a specific strategy by name. Used when the user explicitly
     * selects a method from the dashboard.
     */
    public BreakdownStrategy getStrategyByName(String name) {
        if ("hybrid-llm".equals(name)) {
            return hybridStrategy;
        }
        return tfidfStrategy; // default
    }

    /**
     * Get all available strategies.
     */
    public BreakdownStrategy[] getAvailableStrategies() {
        if (isLlmConfigured()) {
            return new BreakdownStrategy[] { tfidfStrategy, hybridStrategy };
        }
        return new BreakdownStrategy[] { tfidfStrategy };
    }

    private boolean isLlmConfigured() {
        String apiKey = WebloggerConfig.getProperty("pulse.llm.apiKey", "");
        return apiKey != null && !apiKey.trim().isEmpty();
    }
}
