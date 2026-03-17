package org.apache.roller.weblogger.business.pulse;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class UniqueCommenterIndicatorTest {

    private UniqueCommenterIndicator indicator;

    @BeforeEach
    void setUp() {
        indicator = new UniqueCommenterIndicator();
    }

    @Test
    void testNameAndLabel() {
        assertEquals("uniqueCommenters", indicator.getName());
        assertEquals("Unique Commenter Count", indicator.getLabel());
    }

    @Test
    void testEmptyComments() {
        CommentData data = new CommentData("e1", "Test", null, Collections.emptyList());
        Map<String, Object> result = indicator.compute(data);
        assertEquals(0, result.get("uniqueCount"));
        assertEquals(0, result.get("totalComments"));
    }

    @Test
    void testHighDiversity() {
        // 5 unique names out of 5 comments = 1.0 ratio
        CommentData data = new CommentData("e1", "Test", null, Arrays.asList(
                TestHelper.comment("Alice", "Comment 1"),
                TestHelper.comment("Bob", "Comment 2"),
                TestHelper.comment("Carol", "Comment 3"),
                TestHelper.comment("Dave", "Comment 4"),
                TestHelper.comment("Eve", "Comment 5")
        ));

        Map<String, Object> result = indicator.compute(data);
        assertEquals(5, result.get("uniqueCount"));
        assertEquals(5, result.get("totalComments"));
        assertEquals(1.0, result.get("diversityRatio"));
        assertEquals("Highly Diverse", result.get("diversityLabel"));
    }

    @Test
    void testLowDiversity() {
        // 2 unique out of 10 = 0.2 ratio
        CommentData data = new CommentData("e1", "Test", null, Arrays.asList(
                TestHelper.comment("Alice", "C1"), TestHelper.comment("Alice", "C2"),
                TestHelper.comment("Alice", "C3"), TestHelper.comment("Alice", "C4"),
                TestHelper.comment("Alice", "C5"), TestHelper.comment("Bob", "C6"),
                TestHelper.comment("Bob", "C7"), TestHelper.comment("Bob", "C8"),
                TestHelper.comment("Bob", "C9"), TestHelper.comment("Bob", "C10")
        ));

        Map<String, Object> result = indicator.compute(data);
        assertEquals(2, result.get("uniqueCount"));
        assertEquals(10, result.get("totalComments"));
        assertEquals("Dominated by Few", result.get("diversityLabel"));
    }

    @Test
    void testCaseInsensitiveNames() {
        CommentData data = new CommentData("e1", "Test", null, Arrays.asList(
                TestHelper.comment("Alice", "C1"),
                TestHelper.comment("alice", "C2"),
                TestHelper.comment("ALICE", "C3")
        ));

        Map<String, Object> result = indicator.compute(data);
        assertEquals(1, result.get("uniqueCount"));
    }
}
