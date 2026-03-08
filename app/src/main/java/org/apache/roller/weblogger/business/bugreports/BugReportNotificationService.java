/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  The ASF licenses this file to You
 * under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.  For additional information regarding
 * copyright in this work, please see the NOTICE file in the top level
 * directory of this distribution.
 */

package org.apache.roller.weblogger.business.bugreports;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.config.WebloggerRuntimeConfig;
import org.apache.roller.weblogger.pojos.BugReport;
import org.apache.roller.weblogger.pojos.GlobalPermission;
import org.apache.roller.weblogger.pojos.User;

/**
 * Coordinates recipient resolution + message strategy + channel dispatch.
 */
public class BugReportNotificationService {

    private final UserManager userManager;
    private final BugReportEventPublisher publisher;
    private final List<BugNotificationMessageStrategy> messageStrategies;

    public BugReportNotificationService(UserManager userManager, BugNotificationChannelFactory channelFactory) {
        this.userManager = userManager;
        this.publisher = new BugReportEventPublisher(channelFactory.createChannels());
        this.messageStrategies = Arrays.asList(
                new AdminBugNotificationMessageStrategy(),
                new ReporterStatusNotificationMessageStrategy());
    }

    public void notifyAdmins(BugReportEvent event) throws WebloggerException {
        List<String> recipients = resolveAdminRecipients();
        BugNotificationMessage message = buildMessage(event, BugReportAudience.ADMINS);
        if (message != null && !recipients.isEmpty()) {
            publisher.publish(event, BugReportAudience.ADMINS, recipients, message);
        }
    }

    public void notifyReporter(BugReportEvent event) throws WebloggerException {
        List<String> recipients = resolveReporterRecipients(event.getBugReport());
        BugNotificationMessage message = buildMessage(event, BugReportAudience.REPORTER);
        if (message != null && !recipients.isEmpty()) {
            publisher.publish(event, BugReportAudience.REPORTER, recipients, message);
        }
    }

    private BugNotificationMessage buildMessage(BugReportEvent event, BugReportAudience audience) {
        String siteUrl = WebloggerRuntimeConfig.getAbsoluteContextURL();
        for (BugNotificationMessageStrategy strategy : messageStrategies) {
            if (strategy.supports(event.getEventType(), audience)) {
                return strategy.buildMessage(event, siteUrl);
            }
        }
        return null;
    }

    private List<String> resolveAdminRecipients() throws WebloggerException {
        List<User> users = userManager.getUsers(Boolean.TRUE, null, null, 0, -1);
        List<String> recipients = new ArrayList<>();
        for (User user : users) {
            if (user.hasGlobalPermission(GlobalPermission.ADMIN) && !StringUtils.isBlank(user.getEmailAddress())) {
                recipients.add(user.getEmailAddress());
            }
        }

        if (recipients.isEmpty()) {
            String siteAdmin = WebloggerRuntimeConfig.getProperty("site.adminemail");
            if (!StringUtils.isBlank(siteAdmin)) {
                recipients.add(siteAdmin);
            }
        }

        return recipients;
    }

    private List<String> resolveReporterRecipients(BugReport report) throws WebloggerException {
        if (!WebloggerRuntimeConfig.getBooleanProperty("bugreport.notification.notifyReporterStatus")) {
            return Collections.emptyList();
        }

        User reporter = userManager.getUserByUserName(report.getReporterUserName(), Boolean.TRUE);
        if (reporter == null || StringUtils.isBlank(reporter.getEmailAddress())) {
            return Collections.emptyList();
        }

        return Collections.singletonList(reporter.getEmailAddress());
    }
}
