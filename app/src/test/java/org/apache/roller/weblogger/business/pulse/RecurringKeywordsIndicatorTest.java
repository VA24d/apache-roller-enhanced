package org.apache.roller.weblogger.business.pulse;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class RecurringKeywordsIndicatorTest {

    private RecurringKeywordsIndicator indicator;

    @BeforeEach
    void setUp() {
        indicator = new RecurringKeywordsIndicator();
    }

    @Test
    void testNameAndLabel() {
        assertEquals("recurringKeywords", indicator.getName());
        assertEquals("Recurring Keywords / Top Concerns", indicator.getLabel());
    }

    @Test
    void testEmptyComments() {
        CommentData data = new CommentData("e1", "Test", null, Collections.emptyList());
        Map<String, Object> result = indicator.compute(data);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> keywords = (List<Map<String, Object>>) result.get("keywords");
        assertTrue(keywords.isEmpty());
    }

    @Test
    void testKeywordExtraction() {
        CommentData data = new CommentData("e1", "Test", null, Arrays.asList(
                TestHelper.comment("Alice", "The performance issue is critical for the application"),
                TestHelper.comment("Bob", "I noticed the performance problem too, needs optimization"),
                TestHelper.comment("Carol", "Performance is definitely the main concern here")
        ));

        Map<String, Object> result = indicator.compute(data);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> keywords = (List<Map<String, Object>>) result.get("keywords");
        assertFalse(keywords.isEmpty());

        // "performance" should be the top keyword (appears in all 3)
        assertEquals("performance", keywords.get(0).get("word"));
        assertEquals(3, keywords.get(0).get("count"));
    }

    @Test
    void testStopWordsFiltered() {
        CommentData data = new CommentData("e1", "Test", null, Arrays.asList(
                TestHelper.comment("Alice", "the the the this that and or but"),
                TestHelper.comment("Bob", "is was are were have has had")
        ));

        Map<String, Object> result = indicator.compute(data);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> keywords = (List<Map<String, Object>>) result.get("keywords");
        assertTrue(keywords.isEmpty());
    }

    @Test
    void testHtmlStripped() {
        CommentData data = new CommentData("e1", "Test", null, Arrays.asList(
                TestHelper.comment("Alice", "<p>The <strong>database</strong> migration is important</p>"),
                TestHelper.comment("Bob", "<b>database</b> changes need testing")
        ));

        Map<String, Object> result = indicator.compute(data);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> keywords = (List<Map<String, Object>>) result.get("keywords");
        assertFalse(keywords.isEmpty());

        // "database" should appear (HTML tags stripped)
        boolean found = keywords.stream()
                .anyMatch(kw -> "database".equals(kw.get("word")));
        assertTrue(found);
    }

    @Test
    void testMaxFiveKeywords() {
        CommentData data = new CommentData("e1", "Test", null, Arrays.asList(
                TestHelper.comment("A", "alpha bravo charlie delta echo foxtrot golf hotel india juliet"),
                TestHelper.comment("B", "alpha bravo charlie delta echo foxtrot golf hotel india juliet"),
                TestHelper.comment("C", "alpha bravo charlie delta echo foxtrot golf hotel india juliet")
        ));

        Map<String, Object> result = indicator.compute(data);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> keywords = (List<Map<String, Object>>) result.get("keywords");
        assertTrue(keywords.size() <= 5);
    }
}
