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

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.BugReportManager;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.pojos.BugReport;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Fast unit tests that verify workflow methods invoke notification hooks.
 * These tests do not require creating users/weblogs through the UI.
 */
public class BugReportWorkflowServiceNotificationTest {

    @Test
    public void createNotifiesAdminsOnly() throws Exception {
        RecordingBugReportManager manager = new RecordingBugReportManager();
        RecordingNotificationService notifications = new RecordingNotificationService();
        BugReportWorkflowService service = new BugReportWorkflowService(manager, notifications);

        BugReport report = buildReport();
        service.create(report, "tester1");

        assertNotNull(manager.saved);
        assertEquals(1, notifications.adminEvents.size());
        assertEquals(BugReportEventType.CREATED, notifications.adminEvents.get(0).getEventType());
        assertEquals(0, notifications.reporterEvents.size());
    }

    @Test
    public void updateNotifiesAdminsOnly() throws Exception {
        RecordingBugReportManager manager = new RecordingBugReportManager();
        RecordingNotificationService notifications = new RecordingNotificationService();
        BugReportWorkflowService service = new BugReportWorkflowService(manager, notifications);

        BugReport report = buildReport();
        report.setId("bug-123");
        service.update(report, "tester1");

        assertNotNull(manager.saved);
        assertEquals(1, notifications.adminEvents.size());
        assertEquals(BugReportEventType.UPDATED, notifications.adminEvents.get(0).getEventType());
        assertEquals(0, notifications.reporterEvents.size());
    }

    @Test
    public void changeStatusNotifiesAdminsAndReporter() throws Exception {
        RecordingBugReportManager manager = new RecordingBugReportManager();
        RecordingNotificationService notifications = new RecordingNotificationService();
        BugReportWorkflowService service = new BugReportWorkflowService(manager, notifications);

        BugReport report = buildReport();
        report.touch("tester1");
        service.changeStatus(report, BugReport.Status.TRIAGED, "admin");

        assertNotNull(manager.saved);
        assertEquals(BugReport.Status.TRIAGED, manager.saved.getStatus());
        assertEquals(1, notifications.adminEvents.size());
        assertEquals(1, notifications.reporterEvents.size());
        assertEquals(BugReportEventType.STATUS_CHANGED, notifications.adminEvents.get(0).getEventType());
        assertEquals(BugReportEventType.STATUS_CHANGED, notifications.reporterEvents.get(0).getEventType());
    }

    @Test
    public void deleteNotifiesAdminsAndReporterWhenRequested() throws Exception {
        RecordingBugReportManager manager = new RecordingBugReportManager();
        RecordingNotificationService notifications = new RecordingNotificationService();
        BugReportWorkflowService service = new BugReportWorkflowService(manager, notifications);

        BugReport report = buildReport();
        service.delete(report, "admin", true);

        assertNotNull(manager.removed);
        assertEquals(1, notifications.adminEvents.size());
        assertEquals(1, notifications.reporterEvents.size());
        assertEquals(BugReportEventType.DELETED, notifications.adminEvents.get(0).getEventType());
        assertEquals(BugReportEventType.DELETED, notifications.reporterEvents.get(0).getEventType());
    }

    @Test
    public void deleteNotifiesOnlyAdminsWhenReporterNotifyDisabled() throws Exception {
        RecordingBugReportManager manager = new RecordingBugReportManager();
        RecordingNotificationService notifications = new RecordingNotificationService();
        BugReportWorkflowService service = new BugReportWorkflowService(manager, notifications);

        BugReport report = buildReport();
        service.delete(report, "tester1", false);

        assertNotNull(manager.removed);
        assertEquals(1, notifications.adminEvents.size());
        assertEquals(0, notifications.reporterEvents.size());
        assertEquals(BugReportEventType.DELETED, notifications.adminEvents.get(0).getEventType());
    }

    @Test
    public void createThenStatusChangeExercisesBothAdminAndReporterPaths() throws Exception {
        RecordingBugReportManager manager = new RecordingBugReportManager();
        RecordingNotificationService notifications = new RecordingNotificationService();
        BugReportWorkflowService service = new BugReportWorkflowService(manager, notifications);

        BugReport report = buildReport();
        service.create(report, "tester1");
        service.changeStatus(report, BugReport.Status.TRIAGED, "admin");

        assertNotNull(manager.saved);
        assertEquals(BugReport.Status.TRIAGED, manager.saved.getStatus());

        // Admins should be notified for both create and status-change.
        assertEquals(2, notifications.adminEvents.size());
        assertEquals(BugReportEventType.CREATED, notifications.adminEvents.get(0).getEventType());
        assertEquals(BugReportEventType.STATUS_CHANGED, notifications.adminEvents.get(1).getEventType());

        // Reporter is notified on status-change path.
        assertEquals(1, notifications.reporterEvents.size());
        assertEquals(BugReportEventType.STATUS_CHANGED, notifications.reporterEvents.get(0).getEventType());
    }

    private static BugReport buildReport() {
        BugReport report = new BugReport();
        report.setTitle("Broken save button");
        report.setDescription("Save button does not work on profile page");
        report.setReporterUserName("tester1");
        report.setSeverity(BugReport.Severity.HIGH);
        report.setReportType(BugReport.ReportType.BROKEN_BUTTON);
        return report;
    }

    private static final class RecordingBugReportManager implements BugReportManager {
        private BugReport saved;
        private BugReport removed;

        @Override
        public void saveBugReport(BugReport bugReport) throws WebloggerException {
            this.saved = bugReport;
        }

        @Override
        public void removeBugReport(BugReport bugReport) throws WebloggerException {
            this.removed = bugReport;
        }

        @Override
        public BugReport getBugReport(String id) throws WebloggerException {
            return null;
        }

        @Override
        public List<BugReport> getBugReports() throws WebloggerException {
            return Collections.emptyList();
        }

        @Override
        public List<BugReport> getBugReportsByReporter(String reporterUserName) throws WebloggerException {
            return Collections.emptyList();
        }

        @Override
        public List<BugReport> getBugReportsByStatus(BugReport.Status status) throws WebloggerException {
            return Collections.emptyList();
        }

        @Override
        public void release() {
        }
    }

    private static final class RecordingNotificationService extends BugReportNotificationService {

        private final List<BugReportEvent> adminEvents = new ArrayList<>();
        private final List<BugReportEvent> reporterEvents = new ArrayList<>();

        private RecordingNotificationService() {
            super(noopUserManager(), new BugNotificationChannelFactory());
        }

        @Override
        public void notifyAdmins(BugReportEvent event) throws WebloggerException {
            adminEvents.add(event);
        }

        @Override
        public void notifyReporter(BugReportEvent event) throws WebloggerException {
            reporterEvents.add(event);
        }

        private static UserManager noopUserManager() {
            return (UserManager) Proxy.newProxyInstance(
                    UserManager.class.getClassLoader(),
                    new Class[]{UserManager.class},
                    (proxy, method, args) -> {
                        throw new UnsupportedOperationException("Not needed for this test");
                    });
        }
    }
}
