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

package org.apache.roller.weblogger.pojos;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pure unit tests for BugReport POJO logic — no database required.
 */
public class BugReportTest {

    @Test
    public void testDefaultValues() {
        BugReport report = new BugReport();
        assertNotNull(report.getId());
        assertEquals(BugReport.ReportType.OTHER, report.getReportType());
        assertEquals(BugReport.Severity.MEDIUM, report.getSeverity());
        assertEquals(BugReport.Status.OPEN, report.getStatus());
    }

    @Test
    public void testTouch() {
        BugReport report = new BugReport();
        assertNull(report.getCreatedAt());
        assertNull(report.getUpdatedAt());

        report.touch("testUser");

        assertNotNull(report.getCreatedAt());
        assertNotNull(report.getUpdatedAt());
        assertEquals("testUser", report.getLastModifiedBy());
        assertEquals(report.getCreatedAt(), report.getUpdatedAt());
    }

    @Test
    public void testTouchPreservesCreatedAt() {
        BugReport report = new BugReport();
        report.touch("user1");
        var createdAt = report.getCreatedAt();

        // wait a tiny bit so timestamps differ
        report.touch("user2");
        assertEquals(createdAt, report.getCreatedAt());
        assertEquals("user2", report.getLastModifiedBy());
    }

    @Test
    public void testValidStatusTransition_OpenToTriaged() {
        BugReport report = new BugReport();
        report.touch("user");
        report.markStatus(BugReport.Status.TRIAGED, "admin");
        assertEquals(BugReport.Status.TRIAGED, report.getStatus());
    }

    @Test
    public void testValidStatusTransition_OpenToResolved() {
        BugReport report = new BugReport();
        report.touch("user");
        report.markStatus(BugReport.Status.RESOLVED, "admin");
        assertEquals(BugReport.Status.RESOLVED, report.getStatus());
        assertNotNull(report.getResolvedAt());
    }

    @Test
    public void testValidStatusTransition_TriagedToResolved() {
        BugReport report = new BugReport();
        report.touch("user");
        report.markStatus(BugReport.Status.TRIAGED, "admin");
        report.markStatus(BugReport.Status.RESOLVED, "admin");
        assertEquals(BugReport.Status.RESOLVED, report.getStatus());
        assertNotNull(report.getResolvedAt());
    }

    @Test
    public void testValidStatusTransition_TriagedToOpen() {
        BugReport report = new BugReport();
        report.touch("user");
        report.markStatus(BugReport.Status.TRIAGED, "admin");
        report.markStatus(BugReport.Status.OPEN, "admin");
        assertEquals(BugReport.Status.OPEN, report.getStatus());
    }

    @Test
    public void testValidStatusTransition_ResolvedToTriaged() {
        BugReport report = new BugReport();
        report.touch("user");
        report.markStatus(BugReport.Status.RESOLVED, "admin");
        report.markStatus(BugReport.Status.TRIAGED, "admin");
        assertEquals(BugReport.Status.TRIAGED, report.getStatus());
    }

    @Test
    public void testInvalidStatusTransition_ResolvedToOpen() {
        BugReport report = new BugReport();
        report.touch("user");
        report.markStatus(BugReport.Status.RESOLVED, "admin");

        assertThrows(IllegalArgumentException.class, () ->
                report.markStatus(BugReport.Status.OPEN, "admin"));
    }

    @Test
    public void testSameStatusTransitionIsNoOp() {
        BugReport report = new BugReport();
        report.touch("user");
        // same status should not throw
        report.markStatus(BugReport.Status.OPEN, "user");
        assertEquals(BugReport.Status.OPEN, report.getStatus());
    }

    @Test
    public void testResolvedAtClearedOnReopen() {
        BugReport report = new BugReport();
        report.touch("user");
        report.markStatus(BugReport.Status.RESOLVED, "admin");
        assertNotNull(report.getResolvedAt());

        report.markStatus(BugReport.Status.TRIAGED, "admin");
        assertNull(report.getResolvedAt());
    }

    @Test
    public void testIsOwnedBy() {
        BugReport report = new BugReport();
        report.setReporterUserName("alice");

        assertTrue(report.isOwnedBy("alice"));
        assertFalse(report.isOwnedBy("bob"));
        assertFalse(report.isOwnedBy(null));
        assertFalse(report.isOwnedBy(""));
    }

    @Test
    public void testEquality() {
        BugReport r1 = new BugReport();
        BugReport r2 = new BugReport();

        // different UUIDs
        assertNotEquals(r1, r2);

        // same id
        r2.setId(r1.getId());
        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
    }

    @Test
    public void testSetters() {
        BugReport report = new BugReport();
        report.setTitle("Test Title");
        report.setDescription("Test Desc");
        report.setReportType(BugReport.ReportType.BROKEN_LINK);
        report.setSeverity(BugReport.Severity.HIGH);
        report.setPageUrl("http://example.com/page");
        report.setStepsToReproduce("Step 1, Step 2");
        report.setExpectedBehavior("Should work");
        report.setActualBehavior("Does not work");
        report.setAdminNotes("Looked into it");

        assertEquals("Test Title", report.getTitle());
        assertEquals("Test Desc", report.getDescription());
        assertEquals(BugReport.ReportType.BROKEN_LINK, report.getReportType());
        assertEquals(BugReport.Severity.HIGH, report.getSeverity());
        assertEquals("http://example.com/page", report.getPageUrl());
        assertEquals("Step 1, Step 2", report.getStepsToReproduce());
        assertEquals("Should work", report.getExpectedBehavior());
        assertEquals("Does not work", report.getActualBehavior());
        assertEquals("Looked into it", report.getAdminNotes());
    }
}
