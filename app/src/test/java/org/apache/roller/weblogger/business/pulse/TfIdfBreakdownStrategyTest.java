package org.apache.roller.weblogger.business.pulse;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TfIdfBreakdownStrategyTest {

    private TfIdfBreakdownStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new TfIdfBreakdownStrategy();
    }

    @Test
    void testNameAndDescription() {
        assertEquals("tfidf", strategy.getName());
        assertNotNull(strategy.getDescription());
    }

    @Test
    void testEmptyComments() {
        CommentData data = new CommentData("e1", "Test", null, Collections.emptyList());
        ConversationBreakdown result = strategy.analyze(data);

        assertNotNull(result);
        assertTrue(result.getThemes().isEmpty());
        assertEquals("No comments to analyze.", result.getRecap());
        assertEquals("tfidf", result.getMethodUsed());
    }

    @Test
    void testSingleComment() {
        CommentData data = new CommentData("e1", "Test", null, Arrays.asList(
                TestHelper.comment("Alice", "The performance of the database is terrible")
        ));

        ConversationBreakdown result = strategy.analyze(data);
        assertNotNull(result);
        assertNotNull(result.getRecap());
    }

    @Test
    void testMultipleThemes() {
        CommentData data = new CommentData("e1", "Test", new Timestamp(System.currentTimeMillis()),
                Arrays.asList(
                        TestHelper.comment("Alice", "The database performance is very slow and needs optimization"),
                        TestHelper.comment("Bob", "Database queries take forever, we need better indexing"),
                        TestHelper.comment("Carol", "Performance tuning on the database is critical"),
                        TestHelper.comment("Dave", "The user interface design looks great and modern"),
                        TestHelper.comment("Eve", "Love the interface redesign, much better navigation"),
                        TestHelper.comment("Frank", "Interface improvements make the application easier to use"),
                        TestHelper.comment("Grace", "Security vulnerabilities need to be addressed immediately"),
                        TestHelper.comment("Henry", "We found security issues in the authentication module")
                ));

        ConversationBreakdown result = strategy.analyze(data);

        assertNotNull(result);
        assertFalse(result.getThemes().isEmpty());
        assertTrue(result.getThemes().size() <= 5);

        // Each theme should have keywords
        for (ConversationTheme theme : result.getThemes()) {
            assertNotNull(theme.getLabel());
            assertFalse(theme.getKeywords().isEmpty());
            assertTrue(theme.getCommentCount() > 0);
        }

        // Recap should mention number of comments
        assertTrue(result.getRecap().contains("8 comment(s)"));
    }

    @Test
    void testRepresentativeComments() {
        CommentData data = new CommentData("e1", "Test", null, Arrays.asList(
                TestHelper.comment("Alice", "The performance bottleneck is in the database layer"),
                TestHelper.comment("Bob", "Performance testing shows the database is the slowest component"),
                TestHelper.comment("Carol", "We need database performance optimization urgently")
        ));

        ConversationBreakdown result = strategy.analyze(data);

        if (!result.getThemes().isEmpty()) {
            ConversationTheme theme = result.getThemes().get(0);
            assertNotNull(theme.getRepresentativeComments());
            assertFalse(theme.getRepresentativeComments().isEmpty());
            assertTrue(theme.getRepresentativeComments().size() <= 2);
        }
    }

    @Test
    void testHtmlContentHandled() {
        CommentData data = new CommentData("e1", "Test", null, Arrays.asList(
                TestHelper.comment("Alice", "<p>The <b>deployment</b> process needs improvement</p>"),
                TestHelper.comment("Bob", "<div>Deployment automation would help reduce errors</div>")
        ));

        ConversationBreakdown result = strategy.analyze(data);
        assertNotNull(result);
        // Should not crash on HTML content
    }

    @Test
    void testNullContent() {
        CommentData data = new CommentData("e1", "Test", null, Arrays.asList(
                TestHelper.comment("Alice", null),
                TestHelper.comment("Bob", ""),
                TestHelper.comment("Carol", "Some actual content here")
        ));

        ConversationBreakdown result = strategy.analyze(data);
        assertNotNull(result);
    }
}
