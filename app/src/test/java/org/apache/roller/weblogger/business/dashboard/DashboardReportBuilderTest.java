/*
 * Licensed under the Apache License, Version 2.0.
 */
package org.apache.roller.weblogger.business.dashboard;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


class DashboardReportBuilderTest {

    /** A simple test metric that always returns a fixed result. */
    private static class StubMetric implements DashboardMetric {
        private final String name;
        private final String label;
        private final String value;

        StubMetric(String name, String label, String value) {
            this.name = name;
            this.label = label;
            this.value = value;
        }

        @Override public String getName() { return name; }
        @Override public String getLabel() { return label; }
        @Override
        public MetricResult compute() {
            return new MetricResult(name, label, value);
        }
    }

    /** A metric that always throws when computed. */
    private static class FailingMetric implements DashboardMetric {
        @Override public String getName() { return "failing"; }
        @Override public String getLabel() { return "Failing"; }
        @Override
        public MetricResult compute() {
            throw new RuntimeException("Simulated failure");
        }
    }

    @Test
    void testBuildWithNoMetrics() {
        DashboardReport report = new DashboardReportBuilder()
                .setViewName("Empty")
                .build();
        assertEquals("Empty", report.getViewName());
        assertTrue(report.getResults().isEmpty());
    }

    @Test
    void testBuildWithSingleMetric() {
        DashboardReport report = new DashboardReportBuilder()
                .setViewName("Test")
                .addMetric(new StubMetric("users", "Users", "10"))
                .build();

        assertEquals(1, report.getMetricCount());
        assertEquals("users", report.getResults().get(0).getName());
        assertEquals("10", report.getResults().get(0).getValue());
    }

    @Test
    void testBuildWithMultipleMetrics() {
        DashboardReport report = new DashboardReportBuilder()
                .setViewName("Multi")
                .addMetric(new StubMetric("a", "A", "1"))
                .addMetric(new StubMetric("b", "B", "2"))
                .addMetric(new StubMetric("c", "C", "3"))
                .build();

        assertEquals(3, report.getMetricCount());
        assertEquals("a", report.getResults().get(0).getName());
        assertEquals("c", report.getResults().get(2).getName());
    }

    @Test
    void testFailingMetricDoesNotPreventOthers() {
        DashboardReport report = new DashboardReportBuilder()
                .setViewName("Mixed")
                .addMetric(new StubMetric("ok1", "OK1", "good"))
                .addMetric(new FailingMetric())
                .addMetric(new StubMetric("ok2", "OK2", "fine"))
                .build();

        assertEquals(3, report.getMetricCount());
        assertEquals("good", report.getResults().get(0).getValue());
        assertEquals("Error", report.getResults().get(1).getValue());
        assertEquals("fine", report.getResults().get(2).getValue());
    }

    @Test
    void testBuilderMethodChaining() {
        DashboardReportBuilder builder = new DashboardReportBuilder();
        DashboardReportBuilder returned = builder.setViewName("Test");
        assertSame(builder, returned, "setViewName should return the same builder");

        returned = builder.addMetric(new StubMetric("x", "X", "0"));
        assertSame(builder, returned, "addMetric should return the same builder");
    }

    @Test
    void testViewNameIsPreserved() {
        DashboardReport minimalist = new DashboardReportBuilder()
                .setViewName("Minimalist")
                .build();
        DashboardReport full = new DashboardReportBuilder()
                .setViewName("Full")
                .build();

        assertEquals("Minimalist", minimalist.getViewName());
        assertEquals("Full", full.getViewName());
    }

    @Test
    void testMetricOrderMatchesAddOrder() {
        DashboardReport report = new DashboardReportBuilder()
                .setViewName("Ordered")
                .addMetric(new StubMetric("first", "First", "1"))
                .addMetric(new StubMetric("second", "Second", "2"))
                .addMetric(new StubMetric("third", "Third", "3"))
                .build();

        List<MetricResult> results = report.getResults();
        assertEquals("first", results.get(0).getName());
        assertEquals("second", results.get(1).getName());
        assertEquals("third", results.get(2).getName());
    }

    @Test
    void testAllFailingMetrics() {
        DashboardReport report = new DashboardReportBuilder()
                .setViewName("AllFail")
                .addMetric(new FailingMetric())
                .addMetric(new FailingMetric())
                .build();

        assertEquals(2, report.getMetricCount());
        assertEquals("Error", report.getResults().get(0).getValue());
        assertEquals("Error", report.getResults().get(1).getValue());
    }
}
