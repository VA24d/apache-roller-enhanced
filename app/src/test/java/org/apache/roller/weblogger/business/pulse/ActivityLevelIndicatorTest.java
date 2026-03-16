package org.apache.roller.weblogger.business.pulse;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ActivityLevelIndicatorTest {

    private ActivityLevelIndicator indicator;

    @BeforeEach
    void setUp() {
        indicator = new ActivityLevelIndicator();
    }

    @Test
    void testNameAndLabel() {
        assertEquals("activityLevel", indicator.getName());
        assertEquals("Discussion Activity Level", indicator.getLabel());
    }

    @Test
    void testEmptyComments() {
        CommentData data = new CommentData("e1", "Test Entry", now(), Collections.emptyList());
        Map<String, Object> result = indicator.compute(data);

        assertEquals("Silent", result.get("level"));
        assertEquals(0, result.get("totalComments"));
    }

    @Test
    void testColdLevel() {
        // 2 comments in 1 week = ~0.3/day => Cold
        long now = System.currentTimeMillis();
        CommentData data = new CommentData("e1", "Test", new Timestamp(now - 7 * 86400000L),
                Arrays.asList(
                    TestHelper.comment("Alice", "Nice post", new Timestamp(now - 5 * 86400000L)),
                    TestHelper.comment("Bob", "Thanks", new Timestamp(now))
                ));

        Map<String, Object> result = indicator.compute(data);
        assertEquals("Cold", result.get("level"));
        assertEquals(2, result.get("totalComments"));
    }

    @Test
    void testWarmLevel() {
        // 7 comments over 5 days = 1.4/day => Warm
        long now = System.currentTimeMillis();
        CommentData data = new CommentData("e1", "Test", new Timestamp(now - 5 * 86400000L),
                TestHelper.generateComments(7, now - 5 * 86400000L, now));

        Map<String, Object> result = indicator.compute(data);
        assertEquals("Warm", result.get("level"));
    }

    @Test
    void testHotLevel() {
        // 20 comments over 5 days = 4/day => Hot
        long now = System.currentTimeMillis();
        CommentData data = new CommentData("e1", "Test", new Timestamp(now - 5 * 86400000L),
                TestHelper.generateComments(20, now - 5 * 86400000L, now));

        Map<String, Object> result = indicator.compute(data);
        assertEquals("Hot", result.get("level"));
    }

    @Test
    void testOnFireLevel() {
        // 50 comments over 3 days = ~17/day => On Fire
        long now = System.currentTimeMillis();
        CommentData data = new CommentData("e1", "Test", new Timestamp(now - 3 * 86400000L),
                TestHelper.generateComments(50, now - 3 * 86400000L, now));

        Map<String, Object> result = indicator.compute(data);
        assertEquals("On Fire", result.get("level"));
    }

    private Timestamp now() {
        return new Timestamp(System.currentTimeMillis());
    }
}
