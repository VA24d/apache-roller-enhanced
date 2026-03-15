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

package org.apache.roller.weblogger.ui.struts2.admin;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.BugReportManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.bugreports.BugReportServiceFactory;
import org.apache.roller.weblogger.business.bugreports.BugReportWorkflowService;
import org.apache.roller.weblogger.pojos.BugReport;
import org.apache.roller.weblogger.pojos.GlobalPermission;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;

/**
 * Admin action for managing all bug reports: list, filter, change status, delete.
 */
public class BugReportsAdmin extends UIAction {

    private static final Log log = LogFactory.getLog(BugReportsAdmin.class);

    private List<BugReport> reports = Collections.emptyList();

    private String reportId;

    private String newStatus;

    private String adminNotes;

    private String filterStatus;

    public BugReportsAdmin() {
        this.actionName = "bugReportsAdmin";
        this.desiredMenu = "admin";
        this.pageTitle = "bugReportsAdmin.title";
    }

    @Override
    public boolean isWeblogRequired() {
        return false;
    }

    @Override
    public List<String> requiredGlobalPermissionActions() {
        return Collections.singletonList(GlobalPermission.ADMIN);
    }

    @Override
    public List<String> requiredWeblogPermissionActions() {
        return Collections.emptyList();
    }

    /**
     * List all bug reports, optionally filtered by status.
     */
    @Override
    public String execute() {
        try {
            BugReportManager mgr = WebloggerFactory.getWeblogger().getBugReportManager();
            if (!StringUtils.isEmpty(filterStatus)) {
                BugReport.Status status = BugReport.Status.valueOf(filterStatus);
                reports = mgr.getBugReportsByStatus(status);
            } else {
                reports = mgr.getBugReports();
            }
        } catch (WebloggerException ex) {
            log.error("Error retrieving bug reports", ex);
            addError("generic.error.check.logs");
        }
        return LIST;
    }

    /**
     * Change the status of a bug report (triage / resolve / reopen).
     */
    public String changeStatus() {
        if (StringUtils.isEmpty(reportId) || StringUtils.isEmpty(newStatus)) {
            addError("bugReportsAdmin.error.missingParams");
            return execute();
        }

        try {
            BugReportManager mgr = WebloggerFactory.getWeblogger().getBugReportManager();
            BugReport report = mgr.getBugReport(reportId);
            if (report == null) {
                addError("bugReportForm.error.notFound");
                return execute();
            }

            if (!StringUtils.isEmpty(adminNotes)) {
                report.setAdminNotes(adminNotes);
            }

            BugReport.Status status = BugReport.Status.valueOf(newStatus);
            BugReportWorkflowService svc = BugReportServiceFactory.createWorkflowService();
            svc.changeStatus(report, status, getAuthenticatedUser().getUserName());
            WebloggerFactory.getWeblogger().flush();

            addMessage("bugReportsAdmin.statusChanged");
        } catch (IllegalArgumentException ex) {
            addError("bugReportsAdmin.error.invalidTransition");
        } catch (WebloggerException ex) {
            log.error("Error changing bug report status", ex);
            addError("generic.error.check.logs");
        }

        return execute();
    }

    /**
     * Delete a bug report.
     */
    public String delete() {
        if (StringUtils.isEmpty(reportId)) {
            addError("bugReportForm.error.notFound");
            return execute();
        }

        try {
            BugReportManager mgr = WebloggerFactory.getWeblogger().getBugReportManager();
            BugReport report = mgr.getBugReport(reportId);
            if (report == null) {
                addError("bugReportForm.error.notFound");
            } else {
                BugReportWorkflowService svc = BugReportServiceFactory.createWorkflowService();
                svc.delete(report, getAuthenticatedUser().getUserName(), true);
                WebloggerFactory.getWeblogger().flush();
                addMessage("bugReportsAdmin.deleted");
            }
        } catch (WebloggerException ex) {
            log.error("Error deleting bug report", ex);
            addError("generic.error.check.logs");
        }

        return execute();
    }

    // -- accessors --

    public List<BugReport> getReports() {
        return reports;
    }

    public void setReports(List<BugReport> reports) {
        this.reports = reports;
    }

    public String getReportId() {
        return reportId;
    }

    public void setReportId(String reportId) {
        this.reportId = reportId;
    }

    public String getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(String newStatus) {
        this.newStatus = newStatus;
    }

    public String getAdminNotes() {
        return adminNotes;
    }

    public void setAdminNotes(String adminNotes) {
        this.adminNotes = adminNotes;
    }

    public String getFilterStatus() {
        return filterStatus;
    }

    public void setFilterStatus(String filterStatus) {
        this.filterStatus = filterStatus;
    }
}
