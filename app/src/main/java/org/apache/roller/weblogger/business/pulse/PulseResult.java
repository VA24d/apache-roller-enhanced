/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file for additional
 * information regarding copyright ownership.
 */
package org.apache.roller.weblogger.business.pulse;

import java.util.Map;

import org.apache.roller.weblogger.pojos.WeblogEntry;

/**
 * Complete result of a Community Pulse analysis for a single entry.
 * Combines the 6A discussion indicators and 6B conversation breakdown.
 */
public class PulseResult {

    private final WeblogEntry entry;
    private final Map<String, Map<String, Object>> indicators;
    private final ConversationBreakdown breakdown;
    private final BreakdownStrategy[] availableStrategies;

    public PulseResult(WeblogEntry entry,
                       Map<String, Map<String, Object>> indicators,
                       ConversationBreakdown breakdown,
                       BreakdownStrategy[] availableStrategies) {
        this.entry = entry;
        this.indicators = indicators;
        this.breakdown = breakdown;
        this.availableStrategies = availableStrategies;
    }

    public WeblogEntry getEntry() { return entry; }

    public Map<String, Map<String, Object>> getIndicators() { return indicators; }

    public ConversationBreakdown getBreakdown() { return breakdown; }

    public BreakdownStrategy[] getAvailableStrategies() { return availableStrategies; }
}
