/*
 * Licensed under the Apache License, Version 2.0.
 */
package org.apache.roller.weblogger.business.dashboard;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.business.WebloggerFactory;

public class TotalUsersMetric implements DashboardMetric {

    private static final Log LOG = LogFactory.getLog(TotalUsersMetric.class);

    @Override public String getName() { return "totalUsers"; }
    @Override public String getLabel() { return "Total Registered Users"; }

    @Override
    public MetricResult compute() {
        try {
            long count = WebloggerFactory.getWeblogger()
                    .getUserManager().getUserCount();
            return new MetricResult(getName(), getLabel(), String.valueOf(count));
        } catch (Exception e) {
            LOG.error("Failed to compute total users", e);
            return new MetricResult(getName(), getLabel(), "N/A");
        }
    }
}
