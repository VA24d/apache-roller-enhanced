package org.apache.roller.weblogger.ui.rendering.servlets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * JSON response payload for weblog QA.
 */
public class WeblogQaAnswer {

    private final String strategy;
    private final String question;
    private final String answer;
    private final List<WeblogQaSource> sources;
    private final int entryCount;
    private final int supportingPassageCount;
    private final boolean truncatedContext;
    private final String strategyReason;

    public WeblogQaAnswer(String strategy, String question, String answer,
            List<WeblogQaSource> sources, int entryCount,
            int supportingPassageCount, boolean truncatedContext,
            String strategyReason) {
        this.strategy = strategy;
        this.question = question;
        this.answer = answer;
        this.sources = Collections.unmodifiableList(new ArrayList<>(sources));
        this.entryCount = entryCount;
        this.supportingPassageCount = supportingPassageCount;
        this.truncatedContext = truncatedContext;
        this.strategyReason = strategyReason;
    }

    public String getStrategy() {
        return strategy;
    }

    public String getQuestion() {
        return question;
    }

    public String getAnswer() {
        return answer;
    }

    public List<WeblogQaSource> getSources() {
        return sources;
    }

    public int getEntryCount() {
        return entryCount;
    }

    public int getSupportingPassageCount() {
        return supportingPassageCount;
    }

    public boolean isTruncatedContext() {
        return truncatedContext;
    }

    public String getStrategyReason() {
        return strategyReason;
    }
}
