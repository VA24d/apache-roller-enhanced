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

import org.apache.commons.lang3.StringUtils;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.BugReportManager;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.pojos.BugReport;
import org.apache.roller.weblogger.pojos.User;

/**
 * Application service for bug workflows. Executes commands and handles notifications.
 */
public class BugReportWorkflowService {

    private final BugReportManager bugReportManager;
    private final BugReportNotificationService notificationService;

    public BugReportWorkflowService(BugReportManager bugReportManager, UserManager userManager) {
        this.bugReportManager = bugReportManager;
        this.notificationService = new BugReportNotificationService(userManager, new BugNotificationChannelFactory());
    }

    public BugReport create(BugReport report, String actorUserName) throws WebloggerException {
        validateForCreate(report);
        report.touch(actorUserName);
        BugReport saved = new CreateBugReportCommand(report).execute(bugReportManager);

        BugReportEvent event = new BugReportEvent(BugReportEventType.CREATED, saved, actorUserName);
        notificationService.notifyAdmins(event);

        return saved;
    }

    public BugReport update(BugReport report, String actorUserName) throws WebloggerException {
        validateForUpdate(report);
        report.touch(actorUserName);
        BugReport saved = new UpdateBugReportCommand(report).execute(bugReportManager);

        BugReportEvent event = new BugReportEvent(BugReportEventType.UPDATED, saved, actorUserName);
        notificationService.notifyAdmins(event);

        return saved;
    }

    public BugReport changeStatus(BugReport report, BugReport.Status status, String actorUserName)
            throws WebloggerException {
        BugReport saved = new ChangeStatusBugReportCommand(report, status, actorUserName).execute(bugReportManager);

        BugReportEvent event = new BugReportEvent(BugReportEventType.STATUS_CHANGED, saved, actorUserName);
        notificationService.notifyAdmins(event);
        notificationService.notifyReporter(event);

        return saved;
    }

    public void delete(BugReport report, String actorUserName, boolean notifyReporter) throws WebloggerException {
        new DeleteBugReportCommand(report).execute(bugReportManager);

        BugReportEvent event = new BugReportEvent(BugReportEventType.DELETED, report, actorUserName);
        notificationService.notifyAdmins(event);
        if (notifyReporter) {
            notificationService.notifyReporter(event);
        }
    }

    private void validateForCreate(BugReport report) {
        if (StringUtils.isBlank(report.getReporterUserName())) {
            throw new IllegalArgumentException("Reporter username is required");
        }
        validateCommon(report);
    }

    private void validateForUpdate(BugReport report) {
        if (StringUtils.isBlank(report.getId())) {
            throw new IllegalArgumentException("Bug id is required for update");
        }
        validateCommon(report);
    }

    private void validateCommon(BugReport report) {
        if (StringUtils.isBlank(report.getTitle())) {
            throw new IllegalArgumentException("Bug title is required");
        }
        if (StringUtils.isBlank(report.getDescription())) {
            throw new IllegalArgumentException("Bug description is required");
        }
    }

    public boolean canUserManageReport(User actor, BugReport report) {
        return actor != null && (actor.hasGlobalPermission("admin") || report.isOwnedBy(actor.getUserName()));
    }
}
