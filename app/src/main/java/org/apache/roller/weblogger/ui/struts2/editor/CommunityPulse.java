/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file for additional
 * information regarding copyright ownership.
 */
package org.apache.roller.weblogger.ui.struts2.editor;

import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.business.WeblogEntryManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.pulse.CommunityPulseAnalyzer;
import org.apache.roller.weblogger.business.pulse.PulseResult;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.WeblogPermission;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;
import org.apache.roller.weblogger.pojos.CommentSearchCriteria;
import org.apache.roller.weblogger.pojos.WeblogEntryComment;

/**
 * Struts2 action for the Community Pulse Dashboard (Task 6).
 * Allows blog authors to view discussion analytics for their entries.
 */
public class CommunityPulse extends UIAction {

    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog(CommunityPulse.class);

    // Input: entry to analyze
    private String entryId;

    // Input: optional strategy override
    private String strategy;

    // Output: the pulse analysis result
    private PulseResult pulseResult;

    // Output: list of entries for selection
    private List<WeblogEntry> recentEntries;

    public CommunityPulse() {
        this.actionName = "communityPulse";
        this.desiredMenu = "editor";
        this.pageTitle = "communityPulse.title";
    }

    @Override
    public List<String> requiredWeblogPermissionActions() {
        return Collections.singletonList(WeblogPermission.POST);
    }

    /**
     * Default: show entry selector with recent entries.
     */
    @Override
    public String execute() {
        loadRecentEntries();
        return LIST;
    }

    /**
     * Analyze a specific entry's comments.
     */
    public String analyze() {
        loadRecentEntries();

        if (entryId == null || entryId.trim().isEmpty()) {
            addError("communityPulse.noEntrySelected");
            return LIST;
        }

        try {
            CommunityPulseAnalyzer analyzer = new CommunityPulseAnalyzer();
            pulseResult = analyzer.analyze(entryId, strategy);
        } catch (Exception e) {
            log.error("Error analyzing community pulse for entry: " + entryId, e);
            addError("communityPulse.analysisError");
        }

        return LIST;
    }

    private void loadRecentEntries() {
        try {
            WeblogEntryManager mgr = WebloggerFactory.getWeblogger().getWeblogEntryManager();

            // Get entries that have comments
            CommentSearchCriteria csc = new CommentSearchCriteria();
            csc.setWeblog(getActionWeblog());
            csc.setStatus(WeblogEntryComment.ApprovalStatus.APPROVED);
            csc.setReverseChrono(true);
            csc.setMaxResults(200);

            List<WeblogEntryComment> comments = mgr.getComments(csc);

            // Extract unique entries, preserving order
            java.util.LinkedHashMap<String, WeblogEntry> entryMap = new java.util.LinkedHashMap<>();
            for (WeblogEntryComment c : comments) {
                WeblogEntry entry = c.getWeblogEntry();
                if (!entryMap.containsKey(entry.getId())) {
                    entryMap.put(entry.getId(), entry);
                }
            }
            recentEntries = new java.util.ArrayList<>(entryMap.values());

            // Limit to 20 most recent
            if (recentEntries.size() > 20) {
                recentEntries = recentEntries.subList(0, 20);
            }

        } catch (Exception e) {
            log.error("Error loading recent entries", e);
            recentEntries = Collections.emptyList();
        }
    }

    // Getters and setters for Struts

    public String getEntryId() { return entryId; }
    public void setEntryId(String entryId) { this.entryId = entryId; }

    public String getStrategy() { return strategy; }
    public void setStrategy(String strategy) { this.strategy = strategy; }

    public PulseResult getPulseResult() { return pulseResult; }

    public List<WeblogEntry> getRecentEntries() { return recentEntries; }
}
