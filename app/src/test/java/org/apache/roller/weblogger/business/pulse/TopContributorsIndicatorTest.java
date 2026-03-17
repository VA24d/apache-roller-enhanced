package org.apache.roller.weblogger.business.pulse;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TopContributorsIndicatorTest {

    private TopContributorsIndicator indicator;

    @BeforeEach
    void setUp() {
        indicator = new TopContributorsIndicator();
    }

    @Test
    void testNameAndLabel() {
        assertEquals("topContributors", indicator.getName());
        assertEquals("Top Contributors", indicator.getLabel());
    }

    @Test
    void testEmptyComments() {
        CommentData data = new CommentData("e1", "Test", null, Collections.emptyList());
        Map<String, Object> result = indicator.compute(data);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> contributors = (List<Map<String, Object>>) result.get("contributors");
        assertTrue(contributors.isEmpty());
        assertEquals(0, result.get("totalContributors"));
    }

    @Test
    void testTopThreeContributors() {
        CommentData data = new CommentData("e1", "Test", null, Arrays.asList(
                TestHelper.comment("Alice", "Comment 1"),
                TestHelper.comment("Alice", "Comment 2"),
                TestHelper.comment("Alice", "Comment 3"),
                TestHelper.comment("Bob", "Comment 4"),
                TestHelper.comment("Bob", "Comment 5"),
                TestHelper.comment("Carol", "Comment 6"),
                TestHelper.comment("Dave", "Comment 7")
        ));

        Map<String, Object> result = indicator.compute(data);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> contributors = (List<Map<String, Object>>) result.get("contributors");
        assertEquals(3, contributors.size());

        // Alice should be first (3 comments)
        assertEquals("Alice", contributors.get(0).get("name"));
        assertEquals(3, contributors.get(0).get("commentCount"));

        // Bob second (2 comments)
        assertEquals("Bob", contributors.get(1).get("name"));
        assertEquals(2, contributors.get(1).get("commentCount"));

        assertEquals(4, result.get("totalContributors"));
    }

    @Test
    void testAnonymousComments() {
        CommentData data = new CommentData("e1", "Test", null, Arrays.asList(
                TestHelper.comment(null, "Comment 1"),
                TestHelper.comment("", "Comment 2"),
                TestHelper.comment("Alice", "Comment 3")
        ));

        Map<String, Object> result = indicator.compute(data);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> contributors = (List<Map<String, Object>>) result.get("contributors");
        assertEquals(2, contributors.size());

        // Anonymous should appear (2 comments from null + empty)
        boolean hasAnon = contributors.stream()
                .anyMatch(c -> "Anonymous".equals(c.get("name")));
        assertTrue(hasAnon);
    }
}
