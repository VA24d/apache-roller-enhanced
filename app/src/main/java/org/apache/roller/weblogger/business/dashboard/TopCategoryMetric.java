/*
 * Licensed under the Apache License, Version 2.0.
 */
package org.apache.roller.weblogger.business.dashboard;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.business.WeblogManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.StatCount;

/**
 * Finds the top-performing weblog by comment count as a proxy for
 * "top category" engagement. Uses the existing efficient aggregate query
 * {@link WeblogManager#getMostCommentedWeblogs}.
 */
public class TopCategoryMetric implements DashboardMetric {

    private static final Log LOG = LogFactory.getLog(TopCategoryMetric.class);

    @Override public String getName() { return "topCategory"; }
    @Override public String getLabel() { return "Top-Performing Category"; }

    @Override
    public MetricResult compute() {
        try {
            WeblogManager wmgr = WebloggerFactory.getWeblogger().getWeblogManager();
            List<StatCount> mostCommented = wmgr.getMostCommentedWeblogs(
                    null, null, 0, 1);

            if (mostCommented != null && !mostCommented.isEmpty()) {
                StatCount top = mostCommented.get(0);
                return new MetricResult(getName(), getLabel(),
                        top.getWeblogHandle(),
                        List.of(top.getCount() + " comments"));
            }

            return new MetricResult(getName(), getLabel(), "No data yet");
        } catch (Exception e) {
            LOG.error("Failed to compute top category", e);
            return new MetricResult(getName(), getLabel(), "N/A");
        }
    }
}
