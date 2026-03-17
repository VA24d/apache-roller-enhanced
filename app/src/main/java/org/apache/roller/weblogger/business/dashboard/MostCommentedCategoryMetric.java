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
import org.apache.roller.weblogger.pojos.WeblogEntry;

/**
 * Finds the category with the most comments across all weblogs.
 * Iterates weblogs → categories → entries, summing comment counts per category name.
 */
public class MostCommentedCategoryMetric implements DashboardMetric {

    private static final Log LOG = LogFactory.getLog(MostCommentedCategoryMetric.class);

    @Override public String getName() { return "mostCommentedCategory"; }
    @Override public String getLabel() { return "Most Commented Category"; }

    @Override
    public MetricResult compute() {
        try {
            WeblogManager wmgr = WebloggerFactory.getWeblogger().getWeblogManager();
            WeblogEntryManager emgr = WebloggerFactory.getWeblogger().getWeblogEntryManager();

            List<Weblog> weblogs = wmgr.getWeblogs(Boolean.TRUE, null,
                    null, null, 0, 200);

            Map<String, Long> catComments = new HashMap<>();
            for (Weblog weblog : weblogs) {
                List<WeblogCategory> cats = emgr.getWeblogCategories(weblog);
                for (WeblogCategory cat : cats) {
                    List<WeblogEntry> entries = cat.retrieveWeblogEntries(true);
                    long commentTotal = 0;
                    for (WeblogEntry entry : entries) {
                        commentTotal += entry.getCommentCount();
                    }
                    if (commentTotal > 0) {
                        catComments.merge(cat.getName(), commentTotal, Long::sum);
                    }
                }
            }

            if (catComments.isEmpty()) {
                return new MetricResult(getName(), getLabel(), "No comments yet");
            }

            Map.Entry<String, Long> top = catComments.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .orElse(null);

            return new MetricResult(getName(), getLabel(),
                    top.getKey(),
                    List.of(top.getValue() + " comments"));

        } catch (Exception e) {
            LOG.error("Failed to compute most commented category", e);
            return new MetricResult(getName(), getLabel(), "N/A");
        }
    }
}
