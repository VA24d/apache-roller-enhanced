/*
 * Licensed under the Apache License, Version 2.0.
 */
package org.apache.roller.weblogger.business.dashboard;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.business.WeblogManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;

/**
 * Leaderboard of top 3 most active users based on number of weblogs they own.
 * Uses existing {@link WeblogManager#getUserWeblogs(User, boolean)} for counts.
 */
public class TopActiveUsersMetric implements DashboardMetric {

    private static final Log LOG = LogFactory.getLog(TopActiveUsersMetric.class);

    @Override public String getName() { return "topActiveUsers"; }
    @Override public String getLabel() { return "Top 3 Most Active Users"; }

    @Override
    public MetricResult compute() {
        try {
            UserManager umgr = WebloggerFactory.getWeblogger().getUserManager();
            WeblogManager wmgr = WebloggerFactory.getWeblogger().getWeblogManager();

            // Get all enabled users (limited to first 100 for efficiency)
            List<User> users = umgr.getUsers(Boolean.TRUE, null, null, 0, 100);

            // Rank by number of weblogs they own
            List<UserScore> scores = new ArrayList<>();
            for (User user : users) {
                List<Weblog> weblogs = wmgr.getUserWeblogs(user, true);
                if (!weblogs.isEmpty()) {
                    scores.add(new UserScore(user.getScreenName(), weblogs.size()));
                }
            }

            scores.sort((a, b) -> Integer.compare(b.score, a.score));

            List<String> details = new ArrayList<>();
            int limit = Math.min(3, scores.size());
            StringBuilder topLine = new StringBuilder();
            for (int i = 0; i < limit; i++) {
                UserScore us = scores.get(i);
                String entry = (i + 1) + ". " + us.name + " (" + us.score + " weblogs)";
                details.add(entry);
                if (i > 0) {
                    topLine.append(", ");
                }
                topLine.append(us.name);
            }

            if (details.isEmpty()) {
                return new MetricResult(getName(), getLabel(), "No active users yet");
            }

            return new MetricResult(getName(), getLabel(),
                    topLine.toString(), details);
        } catch (Exception e) {
            LOG.error("Failed to compute top active users", e);
            return new MetricResult(getName(), getLabel(), "N/A");
        }
    }

    private static class UserScore {
        final String name;
        final int score;
        UserScore(String name, int score) {
            this.name = name;
            this.score = score;
        }
    }
}
