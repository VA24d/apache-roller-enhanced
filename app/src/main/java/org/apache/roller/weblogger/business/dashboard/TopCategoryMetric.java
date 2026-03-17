/*
 * Licensed under the Apache License, Version 2.0.
 */
package org.apache.roller.weblogger.business.dashboard;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.business.WeblogEntryManager;
import org.apache.roller.weblogger.business.WeblogManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogCategory;

/**
 * Finds the category with the most blog entries across all weblogs.
 * Iterates weblogs → categories → entry count per category name.
 */
public class TopCategoryMetric implements DashboardMetric {

    private static final Log LOG = LogFactory.getLog(TopCategoryMetric.class);

    @Override public String getName() { return "topCategory"; }
    @Override public String getLabel() { return "Top Category"; }

    @Override
    public MetricResult compute() {
        try {
            WeblogManager wmgr = WebloggerFactory.getWeblogger().getWeblogManager();
            WeblogEntryManager emgr = WebloggerFactory.getWeblogger().getWeblogEntryManager();

            List<Weblog> weblogs = wmgr.getWeblogs(Boolean.TRUE, null,
                    null, null, 0, 200);

            // Aggregate entry counts by category name across all weblogs
            Map<String, Integer> catCounts = new HashMap<>();
            for (Weblog weblog : weblogs) {
                List<WeblogCategory> cats = emgr.getWeblogCategories(weblog);
                for (WeblogCategory cat : cats) {
                    int entryCount = cat.retrieveWeblogEntries(true).size();
                    if (entryCount > 0) {
                        catCounts.merge(cat.getName(), entryCount, Integer::sum);
                    }
                }
            }

            if (catCounts.isEmpty()) {
                return new MetricResult(getName(), getLabel(), "No data yet");
            }

            // Find category with most entries
            Map.Entry<String, Integer> top = catCounts.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .orElse(null);

            return new MetricResult(getName(), getLabel(),
                    top.getKey(),
                    List.of(top.getValue() + " entries"));

        } catch (Exception e) {
            LOG.error("Failed to compute top category", e);
            return new MetricResult(getName(), getLabel(), "N/A");
        }
    }
}
