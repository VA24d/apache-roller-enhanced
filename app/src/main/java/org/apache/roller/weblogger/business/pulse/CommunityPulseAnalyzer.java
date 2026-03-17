/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file for additional
 * information regarding copyright ownership.
 */
package org.apache.roller.weblogger.business.pulse;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.WeblogEntryManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.CommentSearchCriteria;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.WeblogEntryComment;
import org.apache.roller.weblogger.util.cache.Cache;
import org.apache.roller.weblogger.util.cache.CacheManager;

/**
 * Facade for the Community Pulse Dashboard (Task 6).
 * Fetches comments once, then delegates to 6A indicators and 6B breakdown.
 *
 * Caching: results are cached per entry+strategy for up to 6 hours.
 * The cache is also invalidated when 20+ new comments arrive since
 * the last analysis.  A manual refresh flag bypasses the cache entirely.
 *
 * Design pattern: Facade — provides a single entry point that coordinates
 * the discussion overview (6A) and conversation breakdown (6B) subsystems.
 */
public class CommunityPulseAnalyzer {

    private static final Log log = LogFactory.getLog(CommunityPulseAnalyzer.class);

    /** 6 hours in seconds */
    private static final String CACHE_TIMEOUT_SECONDS = String.valueOf(6L * 60 * 60);

    /** Max cached entries (different entry+strategy combos) */
    private static final String CACHE_MAX_SIZE = "50";

    /**
     * Shared cache — static so it survives across action invocations.
     * Key: "entryId:strategyName", Value: CachedPulseEntry.
     * Built via CacheManager so the default ExpiringLRU factory is used.
     */
    private static final Cache cache;

    static {
        java.util.Map<String, String> props = new java.util.HashMap<>();
        props.put("id", "pulse.resultCache");
        props.put("size", CACHE_MAX_SIZE);
        props.put("timeout", CACHE_TIMEOUT_SECONDS);
        cache = CacheManager.constructCache(null, props);
    }

    private final DiscussionOverview discussionOverview;
    private final BreakdownStrategySelector strategySelector;

    public CommunityPulseAnalyzer() {
        this.discussionOverview = new DiscussionOverview();
        this.strategySelector = new BreakdownStrategySelector();
    }

    /**
     * Run the full Community Pulse analysis for a weblog entry.
     */
    public PulseResult analyze(String entryId) throws WebloggerException {
        return analyze(entryId, null, false);
    }

    /**
     * Run analysis with an explicit strategy name override.
     */
    public PulseResult analyze(String entryId, String strategyName)
            throws WebloggerException {
        return analyze(entryId, strategyName, false);
    }

    /**
     * Run analysis with optional strategy override and cache bypass.
     *
     * @param entryId       the weblog entry ID
     * @param strategyName  optional strategy name ("tfidf" or "hybrid-llm"); null for auto
     * @param forceRefresh  if true, bypass cache and recompute
     * @return full pulse result with indicators and breakdown
     */
    public PulseResult analyze(String entryId, String strategyName, boolean forceRefresh)
            throws WebloggerException {

        WeblogEntryManager mgr = WebloggerFactory.getWeblogger().getWeblogEntryManager();
        WeblogEntry entry = mgr.getWeblogEntry(entryId);
        if (entry == null) {
            throw new WebloggerException("Entry not found: " + entryId);
        }

        // Resolve the strategy name for cache key (auto-select needs comment count)
        // We do a cheap count query first to check cache validity
        int currentCommentCount = countApprovedComments(mgr, entry);

        String effectiveStrategy = strategyName;
        if (effectiveStrategy == null || effectiveStrategy.isEmpty()) {
            effectiveStrategy = strategySelector.selectStrategy(currentCommentCount).getName();
        }

        String cacheKey = entryId + ":" + effectiveStrategy;

        // Check cache unless forced refresh
        if (!forceRefresh) {
            CachedPulseEntry cached = (CachedPulseEntry) cache.get(cacheKey);
            if (cached != null && !cached.isStaleByComments(currentCommentCount)) {
                log.debug("Pulse cache HIT for " + cacheKey);
                return cached.getResult();
            }
            if (cached != null) {
                log.debug("Pulse cache STALE (comment delta) for " + cacheKey);
                cache.remove(cacheKey);
            }
        } else {
            log.debug("Pulse cache BYPASS (force refresh) for " + cacheKey);
            cache.remove(cacheKey);
        }

        // Cache miss — run full analysis
        CommentSearchCriteria csc = new CommentSearchCriteria();
        csc.setEntry(entry);
        csc.setStatus(WeblogEntryComment.ApprovalStatus.APPROVED);
        csc.setReverseChrono(false); // chronological for analysis
        List<WeblogEntryComment> comments = mgr.getComments(csc);

        Timestamp pubTime = entry.getPubTime() != null
                ? new Timestamp(entry.getPubTime().getTime()) : null;
        CommentData data = new CommentData(
                entryId, entry.getTitle(), pubTime, comments);

        // 6A: Discussion Overview indicators
        Map<String, Map<String, Object>> indicators = discussionOverview.computeAll(data);

        // 6B: Conversation Breakdown
        BreakdownStrategy strategy = strategySelector.getStrategyByName(effectiveStrategy);
        ConversationBreakdown breakdown = strategy.analyze(data);

        PulseResult result = new PulseResult(entry, indicators, breakdown,
                strategySelector.getAvailableStrategies());

        // Store in cache
        cache.put(cacheKey, new CachedPulseEntry(result, currentCommentCount));
        log.debug("Pulse cache STORE for " + cacheKey + " (" + currentCommentCount + " comments)");

        return result;
    }

    /**
     * Count approved comments for an entry — cheaper than fetching full objects.
     */
    private int countApprovedComments(WeblogEntryManager mgr, WeblogEntry entry)
            throws WebloggerException {
        CommentSearchCriteria csc = new CommentSearchCriteria();
        csc.setEntry(entry);
        csc.setStatus(WeblogEntryComment.ApprovalStatus.APPROVED);
        return mgr.getComments(csc).size();
    }

    /** Expose for testing / admin: clear all cached pulse results. */
    public static void clearCache() {
        cache.clear();
    }
}
