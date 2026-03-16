/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file for additional
 * information regarding copyright ownership.
 */
package org.apache.roller.weblogger.business.pulse;

import java.util.List;

/**
 * Result of a conversation breakdown analysis (Task 6B).
 * Contains themes, representative comments per theme, and an overall recap.
 */
public class ConversationBreakdown {

    private List<ConversationTheme> themes;
    private String recap;
    private String methodUsed;

    public ConversationBreakdown() {}

    public ConversationBreakdown(List<ConversationTheme> themes,
                                  String recap, String methodUsed) {
        this.themes = themes;
        this.recap = recap;
        this.methodUsed = methodUsed;
    }

    public List<ConversationTheme> getThemes() { return themes; }
    public void setThemes(List<ConversationTheme> themes) { this.themes = themes; }

    public String getRecap() { return recap; }
    public void setRecap(String recap) { this.recap = recap; }

    public String getMethodUsed() { return methodUsed; }
    public void setMethodUsed(String methodUsed) { this.methodUsed = methodUsed; }
}
