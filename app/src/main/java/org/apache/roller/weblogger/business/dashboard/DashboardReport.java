/*
 * Licensed under the Apache License, Version 2.0.
 */
package org.apache.roller.weblogger.business.dashboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An assembled dashboard report containing computed metric results.
 * Created by {@link DashboardReportBuilder}.
 */
public class DashboardReport {

    private final String viewName;
    private final List<MetricResult> results;

    public DashboardReport(String viewName, List<MetricResult> results) {
        this.viewName = viewName;
        this.results = results != null
                ? Collections.unmodifiableList(new ArrayList<>(results))
                : Collections.emptyList();
    }

    /** "Minimalist" or "Full" */
    public String getViewName() { return viewName; }

    /** Ordered list of computed metric results. */
    public List<MetricResult> getResults() { return results; }

    /** Number of metrics in this report. */
    public int getMetricCount() { return results.size(); }
}
