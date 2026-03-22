package org.apache.roller.weblogger.ui.rendering.servlets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class GeminiQaSynthesisSupportTest {

    @Test
    void buildsGroundedAnswerFromRankedPassages() {
        List<LongContextWeblogQaStrategy.RankedPassage> passages = List.of(
                new LongContextWeblogQaStrategy.RankedPassage(
                        document("privacy-2026", "Data Privacy Update",
                                "Summary", "The blog discusses data privacy and retention windows.",
                                "2026-03-12", "https://example.test/privacy-2026"),
                        "The blog discusses data privacy and retention windows.", 9.0d),
                new LongContextWeblogQaStrategy.RankedPassage(
                        document("travel", "Weekend Travel Notes",
                                "", "This entry is about hiking and food.",
                                "2025-01-03", "https://example.test/travel"),
                        "This entry is about hiking and food.", 1.0d));

        WeblogQaAnswer answer = GeminiQaSynthesisSupport.buildGeminiAnswer(
                "rag",
                "What has this blog said about data privacy?",
                passages,
                2,
                false,
                3,
                (strategy, question, rankedPassages) ->
                        "Gemini answer grounded in the retrieved privacy passages.",
                "RAG was used because the question looks focused.");

        assertEquals("rag", answer.getStrategy());
        assertEquals(2, answer.getEntryCount());
        assertEquals(2, answer.getSupportingPassageCount());
        assertTrue(answer.getAnswer().contains("privacy"));
        assertFalse(answer.getSources().isEmpty());
        assertEquals("Data Privacy Update", answer.getSources().get(0).getTitle());
        assertEquals("RAG was used because the question looks focused.", answer.getStrategyReason());
    }

    private static WeblogQaEntryDocument document(String id, String title, String summary,
            String content, String date, String url) {
        return new WeblogQaEntryDocument(id, title, summary, content, url,
                java.sql.Date.valueOf(date));
    }
}
