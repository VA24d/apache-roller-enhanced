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

import org.apache.roller.weblogger.pojos.BugReport;

public class ReporterStatusNotificationMessageStrategy implements BugNotificationMessageStrategy {

    @Override
    public boolean supports(BugReportEventType eventType, BugReportAudience audience) {
        return audience == BugReportAudience.REPORTER
                && (eventType == BugReportEventType.STATUS_CHANGED || eventType == BugReportEventType.DELETED);
    }

    @Override
    public BugNotificationMessage buildMessage(BugReportEvent event, String siteUrl) {
        BugReport report = event.getBugReport();

        String subject = "[Roller] Your bug report status changed: " + report.getTitle();
        if (event.getEventType() == BugReportEventType.DELETED) {
            subject = "[Roller] Your bug report was removed: " + report.getTitle();
        }

        StringBuilder body = new StringBuilder();
        body.append("Your submitted bug report has been updated.\n\n");
        body.append("Bug ID: ").append(report.getId()).append("\n");
        body.append("Title: ").append(report.getTitle()).append("\n");
        body.append("Current status: ").append(report.getStatus()).append("\n");
        if (report.getAdminNotes() != null && !report.getAdminNotes().isBlank()) {
            body.append("Admin notes: ").append(report.getAdminNotes()).append("\n");
        }
        body.append("\nView your reports: ").append(siteUrl).append("/roller-ui/bugReports.rol\n");

        return new BugNotificationMessage(subject, body.toString());
    }
}
