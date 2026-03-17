/*
 * Licensed under the Apache License, Version 2.0.
 */
package org.apache.roller.weblogger.business.dashboard;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.business.StarManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.Weblog;

/**
 * Finds the weblog with the most stars using the existing
 * {@link StarManager#getTrendingWeblogs(int)} aggregate query.
 */
public class MostStarredBlogMetric implements DashboardMetric {

    private static final Log LOG = LogFactory.getLog(MostStarredBlogMetric.class);

    @Override public String getName() { return "mostStarredBlog"; }
    @Override public String getLabel() { return "Most Starred Blog"; }

    @Override
    public MetricResult compute() {
        try {
            StarManager smgr = WebloggerFactory.getWeblogger().getStarManager();
            List<Object[]> trending = smgr.getTrendingWeblogs(1);

            if (trending != null && !trending.isEmpty()) {
                Object[] row = trending.get(0);
                Weblog weblog = (Weblog) row[0];
                Long stars = (Long) row[1];
                return new MetricResult(getName(), getLabel(),
                        weblog.getName(),
                        List.of(stars + " stars", "Handle: " + weblog.getHandle()));
            }

            return new MetricResult(getName(), getLabel(), "No starred blogs yet");
        } catch (Exception e) {
            LOG.error("Failed to compute most starred blog", e);
            return new MetricResult(getName(), getLabel(), "N/A");
        }
    }
}
