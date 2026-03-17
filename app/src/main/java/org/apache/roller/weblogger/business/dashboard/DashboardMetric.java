/*
 * Licensed under the Apache License, Version 2.0.
 * See the NOTICE file for additional copyright information.
 */
package org.apache.roller.weblogger.business.dashboard;

/**
 * A single site-wide metric for the admin dashboard.
 *
 * <p>Each implementation encapsulates how to fetch one piece of site data
 * (e.g. total users, top category). The Builder assembles which metrics
 * go into each view, keeping view definition separate from data-fetching.
 */
public interface DashboardMetric {

    /** Short identifier (e.g. "totalUsers"). */
    String getName();

    /** Human-readable label (e.g. "Total Users"). */
    String getLabel();

    /** Compute and return the metric result. */
    MetricResult compute();
}
