/*
 * Licensed under the Apache License, Version 2.0.
 */
package org.apache.roller.weblogger.business.dashboard;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.business.WebloggerFactory;

public class TotalCommentsMetric implements DashboardMetric {

    private static final Log LOG = LogFactory.getLog(TotalCommentsMetric.class);

    @Override public String getName() { return "totalComments"; }
    @Override public String getLabel() { return "Total Comments"; }

    @Override
    public MetricResult compute() {
        try {
            long count = WebloggerFactory.getWeblogger()
                    .getWeblogEntryManager().getCommentCount();
            return new MetricResult(getName(), getLabel(), String.valueOf(count));
        } catch (Exception e) {
            LOG.error("Failed to compute total comments", e);
            return new MetricResult(getName(), getLabel(), "N/A");
        }
    }
}
