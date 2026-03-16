package org.apache.roller.weblogger.business.pulse;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ResponseTypeIndicatorTest {

    private ResponseTypeIndicator indicator;

    @BeforeEach
    void setUp() {
        indicator = new ResponseTypeIndicator();
    }

    @Test
    void testNameAndLabel() {
        assertEquals("responseTypes", indicator.getName());
        assertEquals("Response Type Breakdown", indicator.getLabel());
    }

    @Test
    void testEmptyComments() {
        CommentData data = new CommentData("e1", "Test", null, Collections.emptyList());
        Map<String, Object> result = indicator.compute(data);

        assertEquals(0, result.get("questions"));
        assertEquals(0, result.get("positive"));
        assertEquals(0, result.get("debate"));
        assertEquals(0, result.get("general"));
    }

    @Test
    void testQuestionDetection() {
        CommentData data = new CommentData("e1", "Test", null, Arrays.asList(
                TestHelper.comment("Alice", "How does this work?"),
                TestHelper.comment("Bob", "What is the purpose of this?")
        ));

        Map<String, Object> result = indicator.compute(data);
        assertEquals(2, result.get("questions"));
    }

    @Test
    void testPositiveFeedback() {
        CommentData data = new CommentData("e1", "Test", null, Arrays.asList(
                TestHelper.comment("Alice", "Great post, thanks for sharing!"),
                TestHelper.comment("Bob", "This is awesome and very helpful")
        ));

        Map<String, Object> result = indicator.compute(data);
        assertEquals(2, result.get("positive"));
    }

    @Test
    void testDebateDetection() {
        CommentData data = new CommentData("e1", "Test", null, Arrays.asList(
                TestHelper.comment("Alice", "I disagree with this approach"),
                TestHelper.comment("Bob", "However, I think the issue is different")
        ));

        Map<String, Object> result = indicator.compute(data);
        assertEquals(2, result.get("debate"));
    }

    @Test
    void testGeneralFallback() {
        CommentData data = new CommentData("e1", "Test", null, Arrays.asList(
                TestHelper.comment("Alice", "Interesting read today")
        ));

        Map<String, Object> result = indicator.compute(data);
        assertEquals(1, result.get("general"));
    }

    @Test
    void testMixedTypes() {
        CommentData data = new CommentData("e1", "Test", null, Arrays.asList(
                TestHelper.comment("Alice", "How does this work?"),
                TestHelper.comment("Bob", "Great post!"),
                TestHelper.comment("Carol", "I disagree"),
                TestHelper.comment("Dave", "Just here reading")
        ));

        Map<String, Object> result = indicator.compute(data);
        assertTrue((int) result.get("questions") >= 1);
        assertTrue((int) result.get("positive") >= 1);
        assertTrue((int) result.get("debate") >= 1);
    }

    @Test
    void testNullContent() {
        CommentData data = new CommentData("e1", "Test", null, Arrays.asList(
                TestHelper.comment("Alice", null)
        ));

        Map<String, Object> result = indicator.compute(data);
        assertEquals(1, result.get("general"));
    }
}
