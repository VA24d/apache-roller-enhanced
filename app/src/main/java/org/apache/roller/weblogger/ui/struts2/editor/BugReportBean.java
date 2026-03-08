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

/**
 * Form bean for bug report submission and editing.
 */
public class BugReportBean {

    private String id;
    private String title;
    private String description;
    private String reportType;
    private String severity;
    private String pageUrl;
    private String stepsToReproduce;
    private String expectedBehavior;
    private String actualBehavior;

    public void copyFrom(BugReport report) {
        this.id = report.getId();
        this.title = report.getTitle();
        this.description = report.getDescription();
        this.reportType = report.getReportType() != null ? report.getReportType().name() : null;
        this.severity = report.getSeverity() != null ? report.getSeverity().name() : null;
        this.pageUrl = report.getPageUrl();
        this.stepsToReproduce = report.getStepsToReproduce();
        this.expectedBehavior = report.getExpectedBehavior();
        this.actualBehavior = report.getActualBehavior();
    }

    public void copyTo(BugReport report) {
        report.setTitle(this.title);
        report.setDescription(this.description);
        if (this.reportType != null) {
            report.setReportType(BugReport.ReportType.valueOf(this.reportType));
        }
        if (this.severity != null) {
            report.setSeverity(BugReport.Severity.valueOf(this.severity));
        }
        report.setPageUrl(this.pageUrl);
        report.setStepsToReproduce(this.stepsToReproduce);
        report.setExpectedBehavior(this.expectedBehavior);
        report.setActualBehavior(this.actualBehavior);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getReportType() {
        return reportType;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getPageUrl() {
        return pageUrl;
    }

    public void setPageUrl(String pageUrl) {
        this.pageUrl = pageUrl;
    }

    public String getStepsToReproduce() {
        return stepsToReproduce;
    }

    public void setStepsToReproduce(String stepsToReproduce) {
        this.stepsToReproduce = stepsToReproduce;
    }

    public String getExpectedBehavior() {
        return expectedBehavior;
    }

    public void setExpectedBehavior(String expectedBehavior) {
        this.expectedBehavior = expectedBehavior;
    }

    public String getActualBehavior() {
        return actualBehavior;
    }

    public void setActualBehavior(String actualBehavior) {
        this.actualBehavior = actualBehavior;
    }
}
