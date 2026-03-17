package org.apache.roller.weblogger.business.pulse;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DiscussionOverviewTest {

    private DiscussionOverview overview;

    @BeforeEach
    void setUp() {
        overview = new DiscussionOverview();
    }

    @Test
    void testAllFiveIndicatorsRegistered() {
        assertEquals(5, overview.getIndicators().size());
    }

    @Test
    void testComputeAllReturnsAllIndicators() {
        CommentData data = new CommentData("e1", "Test", new Timestamp(System.currentTimeMillis()),
                Arrays.asList(
                        TestHelper.comment("Alice", "How does performance work?"),
                        TestHelper.comment("Bob", "Great post, thanks!"),
                        TestHelper.comment("Carol", "I disagree with point 3")
                ));

        Map<String, Map<String, Object>> results = overview.computeAll(data);

        assertEquals(5, results.size());
        assertTrue(results.containsKey("activityLevel"));
        assertTrue(results.containsKey("responseTypes"));
        assertTrue(results.containsKey("recurringKeywords"));
        assertTrue(results.containsKey("topContributors"));
        assertTrue(results.containsKey("uniqueCommenters"));

        // Each result should have a _label
        for (Map<String, Object> indicatorResult : results.values()) {
            assertNotNull(indicatorResult.get("_label"));
        }
    }

    @Test
    void testComputeAllWithEmptyComments() {
        CommentData data = new CommentData("e1", "Test", null, Collections.emptyList());

        Map<String, Map<String, Object>> results = overview.computeAll(data);
        assertEquals(5, results.size());
    }

    @Test
    void testIndicatorErrorIsolation() {
        // If one indicator fails, others should still work
        CommentData data = new CommentData("e1", "Test", null,
                Arrays.asList(TestHelper.comment("Alice", "Test comment")));

        Map<String, Map<String, Object>> results = overview.computeAll(data);
        // All 5 should succeed for normal data
        assertEquals(5, results.size());
    }
}
