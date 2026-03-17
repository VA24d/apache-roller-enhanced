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

package org.apache.roller.weblogger.business;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.TestUtils;
import org.apache.roller.weblogger.pojos.BugReport;
import org.apache.roller.weblogger.pojos.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for BugReportManager CRUD operations.
 * Uses TestUtils to bootstrap the Weblogger environment with a Derby DB.
 */
public class BugReportCRUDTest {

    private static final Log log = LogFactory.getLog(BugReportCRUDTest.class);

    private User testUser = null;

    @BeforeEach
    public void setUp() {
        log.info("BEGIN setUp");
        try {
            TestUtils.setupWeblogger();
            testUser = TestUtils.setupUser("bugReportCRUDTestUser");
            TestUtils.endSession(true);
        } catch (Exception ex) {
            log.error("Error in setUp", ex);
            fail("Failed to set up test environment: " + ex.getMessage());
        }
        log.info("END setUp");
    }

    @AfterEach
    public void tearDown() {
        log.info("BEGIN tearDown");
        try {
            // clean up all bug reports created during test
            BugReportManager mgr = WebloggerFactory.getWeblogger().getBugReportManager();
            List<BugReport> reports = mgr.getBugReports();
            for (BugReport report : reports) {
                mgr.removeBugReport(report);
            }
            WebloggerFactory.getWeblogger().flush();

            TestUtils.teardownUser(testUser.getUserName());
            TestUtils.endSession(true);
        } catch (Exception ex) {
            log.error("Error in tearDown", ex);
        }
        log.info("END tearDown");
    }

    @Test
    public void testCreateBugReport() throws Exception {
        log.info("BEGIN testCreateBugReport");

        BugReportManager mgr = WebloggerFactory.getWeblogger().getBugReportManager();

        BugReport report = new BugReport();
        report.setTitle("Test Bug");
        report.setDescription("Something is broken");
        report.setReportType(BugReport.ReportType.BROKEN_BUTTON);
        report.setSeverity(BugReport.Severity.HIGH);
        report.setReporterUserName(testUser.getUserName());
        report.touch(testUser.getUserName());

        mgr.saveBugReport(report);
        String id = report.getId();
        TestUtils.endSession(true);

        // verify it was persisted
        BugReport fetched = mgr.getBugReport(id);
        assertNotNull(fetched);
        assertEquals("Test Bug", fetched.getTitle());
        assertEquals("Something is broken", fetched.getDescription());
        assertEquals(BugReport.ReportType.BROKEN_BUTTON, fetched.getReportType());
        assertEquals(BugReport.Severity.HIGH, fetched.getSeverity());
        assertEquals(BugReport.Status.OPEN, fetched.getStatus());
        assertEquals(testUser.getUserName(), fetched.getReporterUserName());
        assertNotNull(fetched.getCreatedAt());
        assertNotNull(fetched.getUpdatedAt());

        log.info("END testCreateBugReport");
    }

    @Test
    public void testUpdateBugReport() throws Exception {
        log.info("BEGIN testUpdateBugReport");

        BugReportManager mgr = WebloggerFactory.getWeblogger().getBugReportManager();

        // create
        BugReport report = new BugReport();
        report.setTitle("Original Title");
        report.setDescription("Original Description");
        report.setReporterUserName(testUser.getUserName());
        report.touch(testUser.getUserName());
        mgr.saveBugReport(report);
        String id = report.getId();
        TestUtils.endSession(true);

        // update
        BugReport fetched = mgr.getBugReport(id);
        assertNotNull(fetched);
        fetched.setTitle("Updated Title");
        fetched.setDescription("Updated Description");
        fetched.setSeverity(BugReport.Severity.LOW);
        fetched.touch(testUser.getUserName());
        mgr.saveBugReport(fetched);
        TestUtils.endSession(true);

        // verify update
        BugReport updated = mgr.getBugReport(id);
        assertNotNull(updated);
        assertEquals("Updated Title", updated.getTitle());
        assertEquals("Updated Description", updated.getDescription());
        assertEquals(BugReport.Severity.LOW, updated.getSeverity());

        log.info("END testUpdateBugReport");
    }

    @Test
    public void testDeleteBugReport() throws Exception {
        log.info("BEGIN testDeleteBugReport");

        BugReportManager mgr = WebloggerFactory.getWeblogger().getBugReportManager();

        // create
        BugReport report = new BugReport();
        report.setTitle("To Delete");
        report.setDescription("Will be deleted");
        report.setReporterUserName(testUser.getUserName());
        report.touch(testUser.getUserName());
        mgr.saveBugReport(report);
        String id = report.getId();
        TestUtils.endSession(true);

        // verify exists
        assertNotNull(mgr.getBugReport(id));

        // delete
        BugReport toDelete = mgr.getBugReport(id);
        mgr.removeBugReport(toDelete);
        TestUtils.endSession(true);

        // verify deleted
        assertNull(mgr.getBugReport(id));

        log.info("END testDeleteBugReport");
    }

    @Test
    public void testGetAllBugReports() throws Exception {
        log.info("BEGIN testGetAllBugReports");

        BugReportManager mgr = WebloggerFactory.getWeblogger().getBugReportManager();

        // initially empty
        List<BugReport> reports = mgr.getBugReports();
        int initialCount = reports.size();

        // add two
        BugReport r1 = new BugReport();
        r1.setTitle("Bug One");
        r1.setDescription("First bug");
        r1.setReporterUserName(testUser.getUserName());
        r1.touch(testUser.getUserName());
        mgr.saveBugReport(r1);

        BugReport r2 = new BugReport();
        r2.setTitle("Bug Two");
        r2.setDescription("Second bug");
        r2.setReporterUserName(testUser.getUserName());
        r2.touch(testUser.getUserName());
        mgr.saveBugReport(r2);
        TestUtils.endSession(true);

        reports = mgr.getBugReports();
        assertEquals(initialCount + 2, reports.size());

        log.info("END testGetAllBugReports");
    }

    @Test
    public void testGetBugReportsByReporter() throws Exception {
        log.info("BEGIN testGetBugReportsByReporter");

        BugReportManager mgr = WebloggerFactory.getWeblogger().getBugReportManager();

        BugReport report = new BugReport();
        report.setTitle("By Reporter");
        report.setDescription("Reporter query test");
        report.setReporterUserName(testUser.getUserName());
        report.touch(testUser.getUserName());
        mgr.saveBugReport(report);
        TestUtils.endSession(true);

        List<BugReport> reports = mgr.getBugReportsByReporter(testUser.getUserName());
        assertFalse(reports.isEmpty());
        assertEquals(testUser.getUserName(), reports.get(0).getReporterUserName());

        // query for non-existent reporter
        List<BugReport> empty = mgr.getBugReportsByReporter("nonExistentUser");
        assertTrue(empty.isEmpty());

        log.info("END testGetBugReportsByReporter");
    }

    @Test
    public void testGetBugReportsByStatus() throws Exception {
        log.info("BEGIN testGetBugReportsByStatus");

        BugReportManager mgr = WebloggerFactory.getWeblogger().getBugReportManager();

        BugReport report = new BugReport();
        report.setTitle("Status Filter");
        report.setDescription("Status query test");
        report.setReporterUserName(testUser.getUserName());
        report.touch(testUser.getUserName());
        mgr.saveBugReport(report);
        TestUtils.endSession(true);

        // should be OPEN by default
        List<BugReport> openReports = mgr.getBugReportsByStatus(BugReport.Status.OPEN);
        assertFalse(openReports.isEmpty());

        // nothing RESOLVED yet
        List<BugReport> resolvedReports = mgr.getBugReportsByStatus(BugReport.Status.RESOLVED);
        assertTrue(resolvedReports.isEmpty());

        log.info("END testGetBugReportsByStatus");
    }

    @Test
    public void testStatusTransitionViaManager() throws Exception {
        log.info("BEGIN testStatusTransitionViaManager");

        BugReportManager mgr = WebloggerFactory.getWeblogger().getBugReportManager();

        BugReport report = new BugReport();
        report.setTitle("Status Transition");
        report.setDescription("Test transitions");
        report.setReporterUserName(testUser.getUserName());
        report.touch(testUser.getUserName());
        mgr.saveBugReport(report);
        String id = report.getId();
        TestUtils.endSession(true);

        // OPEN -> TRIAGED
        BugReport fetched = mgr.getBugReport(id);
        fetched.markStatus(BugReport.Status.TRIAGED, "admin");
        mgr.saveBugReport(fetched);
        TestUtils.endSession(true);

        fetched = mgr.getBugReport(id);
        assertEquals(BugReport.Status.TRIAGED, fetched.getStatus());

        // TRIAGED -> RESOLVED
        fetched.markStatus(BugReport.Status.RESOLVED, "admin");
        mgr.saveBugReport(fetched);
        TestUtils.endSession(true);

        fetched = mgr.getBugReport(id);
        assertEquals(BugReport.Status.RESOLVED, fetched.getStatus());
        assertNotNull(fetched.getResolvedAt());

        log.info("END testStatusTransitionViaManager");
    }
}
