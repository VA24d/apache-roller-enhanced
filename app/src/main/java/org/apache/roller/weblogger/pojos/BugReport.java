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

import java.io.Serializable;
import java.sql.Timestamp;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.roller.util.UUIDGenerator;
import org.apache.roller.weblogger.util.HTMLSanitizer;

/**
 * User submitted bug report.
 */
public class BugReport implements Serializable {

    public static final long serialVersionUID = 5848804894300110991L;

    public enum ReportType {
        BROKEN_BUTTON,
        BROKEN_LINK,
        UI_ISSUE,
        OTHER
    }

    public enum Severity {
        LOW,
        MEDIUM,
        HIGH
    }

    public enum Status {
        OPEN,
        TRIAGED,
        RESOLVED
    }

    private String id = UUIDGenerator.generateUUID();
    private String title;
    private String description;
    private ReportType reportType = ReportType.OTHER;
    private Severity severity = Severity.MEDIUM;
    private Status status = Status.OPEN;
    private String pageUrl;
    private String stepsToReproduce;
    private String expectedBehavior;
    private String actualBehavior;
    private String adminNotes;
    private String reporterUserName;
    private String lastModifiedBy;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private Timestamp resolvedAt;

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
        this.title = HTMLSanitizer.conditionallySanitize(title);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = HTMLSanitizer.conditionallySanitize(description);
    }

    public ReportType getReportType() {
        return reportType;
    }

    public void setReportType(ReportType reportType) {
        this.reportType = reportType;
    }

    public Severity getSeverity() {
        return severity;
    }

    public void setSeverity(Severity severity) {
        this.severity = severity;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getPageUrl() {
        return pageUrl;
    }

    public void setPageUrl(String pageUrl) {
        this.pageUrl = HTMLSanitizer.conditionallySanitize(pageUrl);
    }

    public String getStepsToReproduce() {
        return stepsToReproduce;
    }

    public void setStepsToReproduce(String stepsToReproduce) {
        this.stepsToReproduce = HTMLSanitizer.conditionallySanitize(stepsToReproduce);
    }

    public String getExpectedBehavior() {
        return expectedBehavior;
    }

    public void setExpectedBehavior(String expectedBehavior) {
        this.expectedBehavior = HTMLSanitizer.conditionallySanitize(expectedBehavior);
    }

    public String getActualBehavior() {
        return actualBehavior;
    }

    public void setActualBehavior(String actualBehavior) {
        this.actualBehavior = HTMLSanitizer.conditionallySanitize(actualBehavior);
    }

    public String getAdminNotes() {
        return adminNotes;
    }

    public void setAdminNotes(String adminNotes) {
        this.adminNotes = HTMLSanitizer.conditionallySanitize(adminNotes);
    }

    public String getReporterUserName() {
        return reporterUserName;
    }

    public void setReporterUserName(String reporterUserName) {
        this.reporterUserName = HTMLSanitizer.conditionallySanitize(reporterUserName);
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = HTMLSanitizer.conditionallySanitize(lastModifiedBy);
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Timestamp getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(Timestamp resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public void markStatus(Status newStatus, String actorUserName) {
        if (!isValidStatusTransition(this.status, newStatus)) {
            throw new IllegalArgumentException("Invalid bug status transition from "
                    + this.status + " to " + newStatus);
        }

        this.status = newStatus;
        this.lastModifiedBy = actorUserName;
        this.updatedAt = new Timestamp(System.currentTimeMillis());
        this.resolvedAt = (newStatus == Status.RESOLVED) ? this.updatedAt : null;
    }

    public void touch(String actorUserName) {
        this.lastModifiedBy = actorUserName;
        this.updatedAt = new Timestamp(System.currentTimeMillis());
        if (this.createdAt == null) {
            this.createdAt = this.updatedAt;
        }
    }

    private boolean isValidStatusTransition(Status oldStatus, Status newStatus) {
        if (oldStatus == newStatus) {
            return true;
        }
        if (oldStatus == Status.OPEN) {
            return newStatus == Status.TRIAGED || newStatus == Status.RESOLVED;
        }
        if (oldStatus == Status.TRIAGED) {
            return newStatus == Status.OPEN || newStatus == Status.RESOLVED;
        }
        return oldStatus == Status.RESOLVED && newStatus == Status.TRIAGED;
    }

    public boolean isOwnedBy(String userName) {
        return !StringUtils.isBlank(userName) && userName.equals(this.reporterUserName);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof BugReport)) {
            return false;
        }
        BugReport o = (BugReport) other;
        return new EqualsBuilder().append(getId(), o.getId()).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(getId()).toHashCode();
    }
}
