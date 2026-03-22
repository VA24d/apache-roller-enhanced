package org.apache.roller.weblogger.ui.rendering.servlets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Collectors;
import org.apache.roller.weblogger.WebloggerException;
import org.junit.jupiter.api.Test;

class WeblogQaServiceTest {

    private final List<WeblogQaEntryDocument> documents = List.of(
            document("privacy-2026", "Data Privacy Update",
                    "A short summary on privacy changes.",
                    "We discussed data privacy, retention windows, and user consent controls in detail.",
                    "2026-03-12", "https://example.test/privacy-2026"),
            document("privacy-2025", "Privacy Checklist",
                    "Operational notes for privacy work.",
                    "This post talks about data privacy reviews, audit trails, and account deletion workflows.",
                    "2025-08-18", "https://example.test/privacy-2025"),
            document("travel", "Weekend Travel Notes",
                    "",
                    "This post is about hiking, food, and train journeys rather than policy topics.",
                    "2025-01-03", "https://example.test/travel"));

    @Test
    void defaultsToRagAndReturnsGroundedSources() throws Exception {
        WeblogQaService service = new WeblogQaService(new StaticRepository(documents),
                List.of(
                        new RetrievalAugmentedWeblogQaStrategy((strategy, question, rankedPassages) ->
                                "Gemini summary for RAG about privacy and deletion."),
                        new LongContextWeblogQaStrategy((strategy, question, rankedPassages) ->
                                "Gemini summary for long-context about privacy and deletion.")),
                question -> new WeblogQaService.StrategyDecision(
                        "rag",
                        "Auto-picked RAG because the question is focused."));

        WeblogQaAnswer answer = service.answerQuestion("demo", "What has this blog said about data privacy?", null);

        assertEquals("rag", answer.getStrategy());
        assertEquals(3, answer.getEntryCount());
        assertTrue(answer.getSupportingPassageCount() > 0);
        assertFalse(answer.getSources().isEmpty());
        assertTrue(answer.getAnswer().toLowerCase().contains("privacy"));
        List<String> sourceTitles = answer.getSources().stream()
                .map(WeblogQaSource::getTitle)
                .collect(Collectors.toList());
        assertTrue(sourceTitles.contains("Data Privacy Update") || sourceTitles.contains("Privacy Checklist"));
    }

    @Test
    void longContextHighlightsMostRecentRelevantEntryForTemporalQuestions() throws Exception {
        WeblogQaService service = new WeblogQaService(new StaticRepository(documents),
                List.of(
                        new RetrievalAugmentedWeblogQaStrategy((strategy, question, rankedPassages) ->
                                "Gemini RAG answer."),
                        new LongContextWeblogQaStrategy((strategy, question, rankedPassages) ->
                                "Gemini long-context answer with March 12, 2026 and Data Privacy Update.")),
                question -> new WeblogQaService.StrategyDecision(
                        "rag",
                        "Auto-picked RAG because the question is focused."));

        WeblogQaAnswer answer = service.answerQuestion("demo", "When was data privacy last discussed?", "long-context");

        assertEquals("long-context", answer.getStrategy());
        assertTrue(answer.getAnswer().contains("Data Privacy Update"));
        assertTrue(answer.getAnswer().contains("March 12, 2026"));
        assertFalse(answer.getSources().isEmpty());
    }

    @Test
    void autoPicksLongContextForHistoricalQuestions() throws Exception {
        WeblogQaService service = new WeblogQaService(new StaticRepository(documents),
                List.of(
                        new RetrievalAugmentedWeblogQaStrategy((strategy, question, rankedPassages) ->
                                "Gemini RAG answer."),
                        new LongContextWeblogQaStrategy((strategy, question, rankedPassages) ->
                                "Gemini long-context answer.")),
                question -> new WeblogQaService.StrategyDecision(
                        "long-context",
                        "Gemini auto-picked Long Context because the question asks for change over time."));

        WeblogQaAnswer answer = service.answerQuestion("demo",
                "How has the blog's thinking about privacy changed over time?", "auto");

        assertEquals("long-context", answer.getStrategy());
        assertTrue(answer.getStrategyReason().contains("Gemini auto-picked Long Context"));
    }

    @Test
    void autoPicksRagForFocusedQuestions() throws Exception {
        WeblogQaService service = new WeblogQaService(new StaticRepository(documents),
                List.of(
                        new RetrievalAugmentedWeblogQaStrategy((strategy, question, rankedPassages) ->
                                "Gemini RAG answer."),
                        new LongContextWeblogQaStrategy((strategy, question, rankedPassages) ->
                                "Gemini long-context answer.")),
                question -> new WeblogQaService.StrategyDecision(
                        "rag",
                        "Gemini auto-picked RAG because the question is narrow and focused."));

        WeblogQaAnswer answer = service.answerQuestion("demo",
                "Did the blog mention account deletion?", "auto");

        assertEquals("rag", answer.getStrategy());
        assertTrue(answer.getStrategyReason().contains("Gemini auto-picked RAG"));
    }

    @Test
    void rejectsBlankQuestions() {
        WeblogQaService service = new WeblogQaService(new StaticRepository(documents),
                List.of(
                        new RetrievalAugmentedWeblogQaStrategy((strategy, question, rankedPassages) -> "unused"),
                        new LongContextWeblogQaStrategy((strategy, question, rankedPassages) -> "unused")),
                question -> new WeblogQaService.StrategyDecision(
                        "rag",
                        "Gemini auto-picked RAG."));

        assertThrows(IllegalArgumentException.class, () -> service.answerQuestion("demo", "   ", "rag"));
    }

    private static WeblogQaEntryDocument document(String id, String title, String summary,
            String content, String date, String url) {
        return new WeblogQaEntryDocument(id, title, summary, content, url,
                java.sql.Date.valueOf(date));
    }

    private static final class StaticRepository implements WeblogQaEntryRepository {
        private final List<WeblogQaEntryDocument> documents;

        private StaticRepository(List<WeblogQaEntryDocument> documents) {
            this.documents = documents;
        }

        @Override
        public List<WeblogQaEntryDocument> getPublishedEntries(String weblogHandle) throws WebloggerException {
            return documents;
        }
    }
}
