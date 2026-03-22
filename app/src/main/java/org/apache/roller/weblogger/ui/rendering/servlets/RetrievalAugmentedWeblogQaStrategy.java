package org.apache.roller.weblogger.ui.rendering.servlets;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Retrieval-augmented answering strategy.
 *
 * It first retrieves the most relevant entries, then ranks supporting passages
 * only within those entries before synthesizing an answer.
 */
public class RetrievalAugmentedWeblogQaStrategy implements WeblogQaAnswerStrategy {

    private static final int MAX_RETRIEVED_ENTRIES = 8;
    private static final int MAX_PASSAGES = 6;
    private final GeminiQaSynthesisSupport.ContentGenerator contentGenerator;

    public RetrievalAugmentedWeblogQaStrategy() {
        this(GeminiQaSynthesisSupport.defaultGenerator());
    }

    RetrievalAugmentedWeblogQaStrategy(GeminiQaSynthesisSupport.ContentGenerator contentGenerator) {
        this.contentGenerator = contentGenerator;
    }

    @Override
    public String getName() {
        return "rag";
    }

    @Override
    public WeblogQaAnswer answer(String question, List<WeblogQaEntryDocument> documents) {
        List<String> queryTokens = WeblogQaTextSupport.tokenizeQuestion(question);
        if (documents.isEmpty()) {
            return new WeblogQaAnswer(getName(), question,
                    "I could not find any published entries in this weblog yet.",
                    List.of(), 0, 0, false,
                    "RAG is intended for focused questions that can be answered from the most relevant entries.");
        }

        List<RankedEntry> rankedEntries = new ArrayList<>();
        for (WeblogQaEntryDocument document : documents) {
            String combined = document.getCombinedText();
            double score = WeblogQaTextSupport.scoreText(queryTokens, question, combined, false)
                    + WeblogQaTextSupport.scoreText(queryTokens, question, document.getTitle(), true);
            if (score > 0.0d || queryTokens.isEmpty()) {
                rankedEntries.add(new RankedEntry(document, score));
            }
        }

        if (rankedEntries.isEmpty()) {
            return new WeblogQaAnswer(getName(), question,
                    "I could not retrieve any relevant passages for that question from this weblog.",
                    List.of(), documents.size(), 0, false,
                    "RAG retrieved the most relevant entries first, but none of them contained enough grounded evidence.");
        }

        WeblogQaTextSupport.sortByScoreDescThenDate(rankedEntries);
        List<RankedEntry> retrievedEntries = rankedEntries.subList(0, Math.min(MAX_RETRIEVED_ENTRIES, rankedEntries.size()));

        List<LongContextWeblogQaStrategy.RankedPassage> rankedPassages = new ArrayList<>();
        for (RankedEntry rankedEntry : retrievedEntries) {
            for (String passage : WeblogQaTextSupport.splitIntoPassages(rankedEntry.getDocument().getCombinedText())) {
                double passageScore = rankedEntry.getScore()
                        + WeblogQaTextSupport.scoreText(queryTokens, question, passage, false);
                rankedPassages.add(new LongContextWeblogQaStrategy.RankedPassage(
                        rankedEntry.getDocument(), passage, passageScore));
            }
        }

        if (rankedPassages.isEmpty()) {
            return new WeblogQaAnswer(getName(), question,
                    "I retrieved some entries, but could not isolate strong supporting passages for that question.",
                    List.of(), documents.size(), 0, false,
                    "RAG found candidate entries, but the extracted passages were not strong enough to support an answer.");
        }

        WeblogQaTextSupport.sortByScoreDescThenDate(rankedPassages);
        if (rankedPassages.size() > MAX_PASSAGES) {
            rankedPassages = new ArrayList<>(rankedPassages.subList(0, MAX_PASSAGES));
        }

        return GeminiQaSynthesisSupport.buildGeminiAnswer(getName(), question, rankedPassages,
                documents.size(), false, 3, contentGenerator,
                "RAG was used because this question looks focused enough to answer from the most relevant retrieved entries.");
    }

    private static final class RankedEntry implements WeblogQaTextSupport.RankedDocument {
        private final WeblogQaEntryDocument document;
        private final double score;

        private RankedEntry(WeblogQaEntryDocument document, double score) {
            this.document = document;
            this.score = score;
        }

        private WeblogQaEntryDocument getDocument() {
            return document;
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
