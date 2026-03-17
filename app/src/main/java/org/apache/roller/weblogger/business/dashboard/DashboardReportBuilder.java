/*
 * Licensed under the Apache License, Version 2.0.
 */
package org.apache.roller.weblogger.business.dashboard;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Builder for constructing dashboard reports with different view modes.
 *
 * <p>The Builder pattern separates <b>view definition</b> (which metrics to
 * include) from <b>data-fetching logic</b> (how each metric computes its value).
 * This allows new views to be added by simply calling different combinations of
 * {@code addMetric()}, and new metrics to be added by implementing
 * {@link DashboardMetric} — neither change affects the other.
 *
 * <p>Two predefined view constructors are provided:
 * <ul>
 *   <li>{@link #buildMinimalistReport()} — user count, weblog count, top category</li>
 *   <li>{@link #buildFullReport()} — all 7 metrics</li>
 * </ul>
 */
public class DashboardReportBuilder {

    private static final Log LOG = LogFactory.getLog(DashboardReportBuilder.class);

    private String viewName;
    private final List<DashboardMetric> metrics = new ArrayList<>();

    public DashboardReportBuilder setViewName(String viewName) {
        this.viewName = viewName;
        return this;
    }

    public DashboardReportBuilder addMetric(DashboardMetric metric) {
        metrics.add(metric);
        return this;
    }

    /**
     * Compute all added metrics and build the report.
     * Each metric is computed independently; one failure does not
     * prevent others from being computed.
     */
    public DashboardReport build() {
        List<MetricResult> results = new ArrayList<>();
        for (DashboardMetric metric : metrics) {
            try {
                results.add(metric.compute());
            } catch (Exception e) {
                LOG.error("Metric failed: " + metric.getName(), e);
                results.add(new MetricResult(
                        metric.getName(), metric.getLabel(), "Error"));
            }
        }
        return new DashboardReport(viewName, results);
    }

    // ---------------------------------------------------------------
    // Predefined view constructors (view definition only, no data logic)
    // ---------------------------------------------------------------

    /**
     * Minimalist View: user count, weblog count, and top category.
     */
    public static DashboardReport buildMinimalistReport() {
        return new DashboardReportBuilder()
                .setViewName("Minimalist")
                .addMetric(new TotalUsersMetric())
                .addMetric(new TotalWeblogsMetric())
                .addMetric(new TopCategoryMetric())
                .build();
    }

    /**
     * Full View: all 7 metrics for comprehensive site overview.
     */
    public static DashboardReport buildFullReport() {
        return new DashboardReportBuilder()
                .setViewName("Full")
                .addMetric(new TotalUsersMetric())
                .addMetric(new TotalWeblogsMetric())
                .addMetric(new TotalEntriesMetric())
                .addMetric(new TotalCommentsMetric())
                .addMetric(new TopCategoryMetric())
                .addMetric(new MostStarredBlogMetric())
                .addMetric(new TopActiveUsersMetric())
                .addMetric(new MostCommentedWeblogMetric())
                .build();
    }
}
