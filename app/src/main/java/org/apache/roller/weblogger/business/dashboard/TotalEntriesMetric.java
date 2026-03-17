/*
 * Licensed under the Apache License, Version 2.0.
 */
package org.apache.roller.weblogger.business.dashboard;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.business.WebloggerFactory;

public class TotalEntriesMetric implements DashboardMetric {

    private static final Log LOG = LogFactory.getLog(TotalEntriesMetric.class);

    @Override public String getName() { return "totalEntries"; }
    @Override public String getLabel() { return "Total Blog Entries"; }

    @Override
    public MetricResult compute() {
        try {
            long count = WebloggerFactory.getWeblogger()
                    .getWeblogEntryManager().getEntryCount();
            return new MetricResult(getName(), getLabel(), String.valueOf(count));
        } catch (Exception e) {
            LOG.error("Failed to compute total entries", e);
            return new MetricResult(getName(), getLabel(), "N/A");
        }
    }
}
