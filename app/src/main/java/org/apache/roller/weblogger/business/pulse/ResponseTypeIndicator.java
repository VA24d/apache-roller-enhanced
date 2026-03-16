/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file for additional
 * information regarding copyright ownership.
 */
package org.apache.roller.weblogger.business.pulse;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.roller.weblogger.pojos.WeblogEntryComment;

/**
 * Indicator 2: Response Type Breakdown.
 * Classifies each comment as a question, feedback (positive), debate, or general
 * based on keyword/punctuation analysis. Returns percentage breakdown.
 */
public class ResponseTypeIndicator implements DiscussionIndicator {

    private static final Pattern QUESTION_PATTERN = Pattern.compile(
            "\\?|\\b(how|why|what|when|where|who|which|can|could|would|should|does|did|is|are)\\b",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern POSITIVE_PATTERN = Pattern.compile(
            "\\b(thanks|thank you|great|awesome|love|excellent|amazing|helpful|good|nice|well done|agree|agreed|exactly|correct|right)\\b",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern DEBATE_PATTERN = Pattern.compile(
            "\\b(but|however|disagree|wrong|incorrect|actually|not true|no way|I think|on the contrary|conversely|issue|problem)\\b",
            Pattern.CASE_INSENSITIVE);

    @Override
    public String getName() { return "responseTypes"; }

    @Override
    public String getLabel() { return "Response Type Breakdown"; }

    @Override
    public Map<String, Object> compute(CommentData data) {
        Map<String, Object> result = new HashMap<>();

        int questions = 0;
        int positive = 0;
        int debate = 0;
        int general = 0;

        for (WeblogEntryComment comment : data.getComments()) {
            String content = comment.getContent();
            if (content == null || content.trim().isEmpty()) {
                general++;
                continue;
            }

            boolean matched = false;
            if (QUESTION_PATTERN.matcher(content).find()) {
                questions++;
                matched = true;
            }
            if (POSITIVE_PATTERN.matcher(content).find()) {
                positive++;
                matched = true;
            }
            if (DEBATE_PATTERN.matcher(content).find()) {
                debate++;
                matched = true;
            }
            if (!matched) {
                general++;
            }
        }

        int total = data.getCommentCount();
        result.put("questions", questions);
        result.put("positive", positive);
        result.put("debate", debate);
        result.put("general", general);

        if (total > 0) {
            result.put("questionsPct", Math.round(questions * 100.0 / total));
            result.put("positivePct", Math.round(positive * 100.0 / total));
            result.put("debatePct", Math.round(debate * 100.0 / total));
            result.put("generalPct", Math.round(general * 100.0 / total));
        }

        return result;
    }
}
