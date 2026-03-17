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

package org.apache.roller.weblogger.ui.struts2.editor;

import org.apache.roller.weblogger.pojos.BugReport;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for BugReportBean copyFrom/copyTo mapping.
 */
public class BugReportBeanTest {

    @Test
    public void testCopyFrom() {
        BugReport report = new BugReport();
        report.setTitle("Title");
        report.setDescription("Desc");
        report.setReportType(BugReport.ReportType.UI_ISSUE);
        report.setSeverity(BugReport.Severity.LOW);
        report.setPageUrl("http://example.com");
        report.setStepsToReproduce("Steps");
        report.setExpectedBehavior("Expected");
        report.setActualBehavior("Actual");

        BugReportBean bean = new BugReportBean();
        bean.copyFrom(report);

        assertEquals(report.getId(), bean.getId());
        assertEquals("Title", bean.getTitle());
        assertEquals("Desc", bean.getDescription());
        assertEquals("UI_ISSUE", bean.getReportType());
        assertEquals("LOW", bean.getSeverity());
        assertEquals("http://example.com", bean.getPageUrl());
        assertEquals("Steps", bean.getStepsToReproduce());
        assertEquals("Expected", bean.getExpectedBehavior());
        assertEquals("Actual", bean.getActualBehavior());
    }

    @Test
    public void testCopyTo() {
        BugReportBean bean = new BugReportBean();
        bean.setTitle("New Title");
        bean.setDescription("New Desc");
        bean.setReportType("BROKEN_LINK");
        bean.setSeverity("HIGH");
        bean.setPageUrl("http://newurl.com");
        bean.setStepsToReproduce("New Steps");
        bean.setExpectedBehavior("New Expected");
        bean.setActualBehavior("New Actual");

        BugReport report = new BugReport();
        bean.copyTo(report);

        assertEquals("New Title", report.getTitle());
        assertEquals("New Desc", report.getDescription());
        assertEquals(BugReport.ReportType.BROKEN_LINK, report.getReportType());
        assertEquals(BugReport.Severity.HIGH, report.getSeverity());
        assertEquals("http://newurl.com", report.getPageUrl());
        assertEquals("New Steps", report.getStepsToReproduce());
        assertEquals("New Expected", report.getExpectedBehavior());
        assertEquals("New Actual", report.getActualBehavior());
    }

    @Test
    public void testCopyToWithNullEnums() {
        BugReportBean bean = new BugReportBean();
        bean.setTitle("Title");
        bean.setDescription("Desc");
        // leave reportType and severity null

        BugReport report = new BugReport();
        bean.copyTo(report);

        // should keep defaults
        assertEquals(BugReport.ReportType.OTHER, report.getReportType());
        assertEquals(BugReport.Severity.MEDIUM, report.getSeverity());
    }

    @Test
    public void testRoundTrip() {
        BugReport original = new BugReport();
        original.setTitle("Round Trip");
        original.setDescription("Testing round trip");
        original.setReportType(BugReport.ReportType.BROKEN_BUTTON);
        original.setSeverity(BugReport.Severity.MEDIUM);

        BugReportBean bean = new BugReportBean();
        bean.copyFrom(original);

        BugReport copy = new BugReport();
        bean.copyTo(copy);

        assertEquals(original.getTitle(), copy.getTitle());
        assertEquals(original.getDescription(), copy.getDescription());
        assertEquals(original.getReportType(), copy.getReportType());
        assertEquals(original.getSeverity(), copy.getSeverity());
    }
}
