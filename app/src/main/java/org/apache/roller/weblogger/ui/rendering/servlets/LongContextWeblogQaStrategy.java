package org.apache.roller.weblogger.ui.rendering.servlets;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Long-context answering strategy.
 *
 * It scans a wide slice of the weblog archive, concatenating as much context as
 * will fit in a configured character budget before ranking supporting passages.
 */
public class LongContextWeblogQaStrategy implements WeblogQaAnswerStrategy {

    private static final int MAX_CONTEXT_CHARS = 60000;
    private static final int MAX_SOURCES = 3;
    private final GeminiQaSynthesisSupport.ContentGenerator contentGenerator;

    public LongContextWeblogQaStrategy() {
        this(GeminiQaSynthesisSupport.defaultGenerator());
    }

    LongContextWeblogQaStrategy(GeminiQaSynthesisSupport.ContentGenerator contentGenerator) {
        this.contentGenerator = contentGenerator;
    }

    @Override
    public String getName() {
        return "long-context";
    }

    @Override
    public WeblogQaAnswer answer(String question, List<WeblogQaEntryDocument> documents) {
        List<String> queryTokens = WeblogQaTextSupport.tokenizeQuestion(question);
        if (documents.isEmpty()) {
            return new WeblogQaAnswer(getName(), question,
                    "I could not find any published entries in this weblog yet.",
                    List.of(), 0, 0, false,
                    "Long Context is intended for broad or archive-spanning questions.");
        }

        boolean truncatedContext = false;
        int contextChars = 0;
        List<RankedPassage> rankedPassages = new ArrayList<>();

        for (WeblogQaEntryDocument document : documents) {
            String combined = document.getCombinedText();
            if (!combined.isEmpty()) {
                contextChars += combined.length();
                if (contextChars > MAX_CONTEXT_CHARS) {
                    truncatedContext = true;
                    break;
                }
            }

            double entryScore = WeblogQaTextSupport.scoreText(queryTokens, question, combined, false)
                    + WeblogQaTextSupport.scoreText(queryTokens, question, document.getTitle(), true);
            if (entryScore <= 0.0d && !queryTokens.isEmpty()) {
                continue;
            }

            for (String passage : WeblogQaTextSupport.splitIntoPassages(combined)) {
                double passageScore = entryScore + WeblogQaTextSupport.scoreText(queryTokens, question, passage, false);
                rankedPassages.add(new RankedPassage(document, passage, passageScore));
            }
        }

        if (rankedPassages.isEmpty()) {
            return new WeblogQaAnswer(getName(), question,
                    "I could not find enough evidence in the scanned weblog context to answer that yet.",
                    List.of(), documents.size(), 0, truncatedContext,
                    "Long Context scanned a wide slice of the archive but did not find enough grounded evidence.");
        }

        WeblogQaTextSupport.sortByScoreDescThenDate(rankedPassages);
        return GeminiQaSynthesisSupport.buildGeminiAnswer(getName(), question, rankedPassages, documents.size(),
                truncatedContext, MAX_SOURCES, contentGenerator,
                "Long Context was used because this question benefits from a wider archive scan before Gemini synthesis.");
    }

    static final class RankedPassage implements WeblogQaTextSupport.RankedDocument {
        private final WeblogQaEntryDocument document;
        private final String passage;
        private final double score;

        RankedPassage(WeblogQaEntryDocument document, String passage, double score) {
            this.document = document;
            this.passage = passage;
            this.score = score;
        }

        public WeblogQaEntryDocument getDocument() {
            return document;
        }

        public String getPassage() {
            return passage;
        }

        @Override
        public double getScore() {
            return score;
        }

        @Override
        public Date getPublishedAt() {
            return document.getPublishedAt();
        }
    }

}
