/*
 * Licensed under the Apache License, Version 2.0.
 */
package org.apache.roller.weblogger.business.dashboard;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


class DashboardReportTest {

    @Test
    void testViewNameAndResults() {
        MetricResult r1 = new MetricResult("a", "A", "1");
        MetricResult r2 = new MetricResult("b", "B", "2");
        DashboardReport report = new DashboardReport("Full", Arrays.asList(r1, r2));

        assertEquals("Full", report.getViewName());
        assertEquals(2, report.getResults().size());
        assertEquals(2, report.getMetricCount());
    }

    @Test
    void testNullResultsBecomesEmptyList() {
        DashboardReport report = new DashboardReport("Empty", null);
        assertNotNull(report.getResults());
        assertTrue(report.getResults().isEmpty());
        assertEquals(0, report.getMetricCount());
    }

    @Test
    void testResultsListIsUnmodifiable() {
        MetricResult r = new MetricResult("a", "A", "1");
        DashboardReport report = new DashboardReport("Test", Collections.singletonList(r));
        assertThrows(UnsupportedOperationException.class,
                () -> report.getResults().add(new MetricResult("b", "B", "2")));
    }

    @Test
    void testResultsOrderPreserved() {
        MetricResult r1 = new MetricResult("first", "First", "1");
        MetricResult r2 = new MetricResult("second", "Second", "2");
        MetricResult r3 = new MetricResult("third", "Third", "3");
        DashboardReport report = new DashboardReport("Full",
                Arrays.asList(r1, r2, r3));

        assertEquals("first", report.getResults().get(0).getName());
        assertEquals("second", report.getResults().get(1).getName());
        assertEquals("third", report.getResults().get(2).getName());
    }
}
