/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file for additional
 * information regarding copyright ownership.
 */
package org.apache.roller.weblogger.business.pulse;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single discussion theme extracted from comments.
 * Contains the theme label, representative comments, and keyword list.
 */
public class ConversationTheme {

    private String label;
    private List<String> keywords;
    private List<String> representativeComments;
    private int commentCount;

    public ConversationTheme() {
        this.keywords = new ArrayList<>();
        this.representativeComments = new ArrayList<>();
    }

    public ConversationTheme(String label, List<String> keywords,
                             List<String> representativeComments, int commentCount) {
        this.label = label;
        this.keywords = keywords != null ? keywords : new ArrayList<>();
        this.representativeComments = representativeComments != null
                ? representativeComments : new ArrayList<>();
        this.commentCount = commentCount;
    }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public List<String> getKeywords() { return keywords; }
    public void setKeywords(List<String> keywords) { this.keywords = keywords; }

    public List<String> getRepresentativeComments() { return representativeComments; }
    public void setRepresentativeComments(List<String> representativeComments) {
        this.representativeComments = representativeComments;
    }

    public int getCommentCount() { return commentCount; }
    public void setCommentCount(int commentCount) { this.commentCount = commentCount; }
}
