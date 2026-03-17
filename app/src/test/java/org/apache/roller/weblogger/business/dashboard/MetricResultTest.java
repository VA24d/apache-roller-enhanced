/*
 * Licensed under the Apache License, Version 2.0.
 */
package org.apache.roller.weblogger.business.dashboard;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


class MetricResultTest {

    @Test
    void testBasicConstructor() {
        MetricResult r = new MetricResult("totalUsers", "Total Users", "42");
        assertEquals("totalUsers", r.getName());
        assertEquals("Total Users", r.getLabel());
        assertEquals("42", r.getValue());
        assertTrue(r.getDetails().isEmpty());
    }

    @Test
    void testConstructorWithDetails() {
        List<String> details = Arrays.asList("10 active", "5 inactive");
        MetricResult r = new MetricResult("totalUsers", "Total Users", "15", details);
        assertEquals("15", r.getValue());
        assertEquals(2, r.getDetails().size());
        assertEquals("10 active", r.getDetails().get(0));
    }

    @Test
    void testNullDetailsBecomesEmptyList() {
        MetricResult r = new MetricResult("m", "M", "0", null);
        assertNotNull(r.getDetails());
        assertTrue(r.getDetails().isEmpty());
    }

    @Test
    void testDetailsListIsUnmodifiable() {
        List<String> details = Arrays.asList("a", "b");
        MetricResult r = new MetricResult("m", "M", "1", details);
        assertThrows(UnsupportedOperationException.class,
                () -> r.getDetails().add("c"));
    }

    @Test
    void testEmptyValue() {
        MetricResult r = new MetricResult("m", "M", "");
        assertEquals("", r.getValue());
    }
}
