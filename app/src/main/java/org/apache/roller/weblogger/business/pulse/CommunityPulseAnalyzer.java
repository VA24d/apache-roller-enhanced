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

/**
 * Facade for the Community Pulse Dashboard (Task 6).
 * Fetches comments once, then delegates to 6A indicators and 6B breakdown.
 *
 * Design pattern: Facade — provides a single entry point that coordinates
 * the discussion overview (6A) and conversation breakdown (6B) subsystems.
 */
public class CommunityPulseAnalyzer {

    private static final Log log = LogFactory.getLog(CommunityPulseAnalyzer.class);

    private final DiscussionOverview discussionOverview;
    private final BreakdownStrategySelector strategySelector;

    public CommunityPulseAnalyzer() {
        this.discussionOverview = new DiscussionOverview();
        this.strategySelector = new BreakdownStrategySelector();
    }

    /**
     * Run the full Community Pulse analysis for a weblog entry.
     *
     * @param entryId the weblog entry ID
     * @return full pulse result with indicators and breakdown
     */
    public PulseResult analyze(String entryId) throws WebloggerException {
        return analyze(entryId, null);
    }

    /**
     * Run analysis with an explicit strategy name override.
     *
     * @param entryId the weblog entry ID
     * @param strategyName optional strategy name ("tfidf" or "hybrid-llm");
     *                     null for automatic selection
     */
    public PulseResult analyze(String entryId, String strategyName)
            throws WebloggerException {

        WeblogEntryManager mgr = WebloggerFactory.getWeblogger().getWeblogEntryManager();
        WeblogEntry entry = mgr.getWeblogEntry(entryId);
        if (entry == null) {
            throw new WebloggerException("Entry not found: " + entryId);
        }

        // Fetch all approved comments for this entry
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
        BreakdownStrategy strategy;
        if (strategyName != null && !strategyName.isEmpty()) {
            strategy = strategySelector.getStrategyByName(strategyName);
        } else {
            strategy = strategySelector.selectStrategy(data.getCommentCount());
        }

        ConversationBreakdown breakdown = strategy.analyze(data);

        return new PulseResult(entry, indicators, breakdown,
                strategySelector.getAvailableStrategies());
    }
}
