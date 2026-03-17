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
import org.apache.roller.weblogger.pojos.WeblogPermission;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;

/**
 * User-facing action for submitting, viewing, editing and deleting own bug reports.
 */
public class BugReports extends UIAction {

    private static final Log log = LogFactory.getLog(BugReports.class);

    private BugReportBean bean = new BugReportBean();

    private List<BugReport> reports = Collections.emptyList();

    private BugReport bugReport = null;

    private String reportId;

    public BugReports() {
        this.actionName = "bugReports";
        this.desiredMenu = "editor";
        this.pageTitle = "bugReportForm.title";
    }

    @Override
    public List<String> requiredWeblogPermissionActions() {
        return Collections.singletonList(WeblogPermission.EDIT_DRAFT);
    }

    @Override
    public void myPrepare() {
        if (!StringUtils.isEmpty(getBean().getId())) {
            try {
                BugReportManager mgr = WebloggerFactory.getWeblogger().getBugReportManager();
                bugReport = mgr.getBugReport(getBean().getId());
            } catch (WebloggerException ex) {
                log.error("Error looking up bug report", ex);
            }
        }
    }

    /**
     * Show the list of user's bug reports.
     */
    @Override
    public String execute() {
        try {
            BugReportManager mgr = WebloggerFactory.getWeblogger().getBugReportManager();
            reports = mgr.getBugReportsByReporter(getAuthenticatedUser().getUserName());
        } catch (WebloggerException ex) {
            log.error("Error retrieving bug reports", ex);
            addError("generic.error.check.logs");
        }
        return LIST;
    }

    /**
     * Show the add/edit form.
     */
    public String edit() {
        if (!StringUtils.isEmpty(getBean().getId())) {
            if (bugReport == null) {
                addError("bugReportForm.error.notFound");
                return ERROR;
            }
            if (!bugReport.isOwnedBy(getAuthenticatedUser().getUserName())) {
                addError("bugReportForm.error.notOwner");
                return ERROR;
            }
            getBean().copyFrom(bugReport);
        }
        return INPUT;
    }

    /**
     * Save a new or updated bug report.
     */
    public String save() {
        myValidate();
        if (hasActionErrors()) {
            return INPUT;
        }

        try {
            boolean isNew = (bugReport == null);

            if (isNew) {
                bugReport = new BugReport();
                bugReport.setReporterUserName(getAuthenticatedUser().getUserName());
            } else if (!bugReport.isOwnedBy(getAuthenticatedUser().getUserName())) {
                addError("bugReportForm.error.notOwner");
                return ERROR;
            }

            getBean().copyTo(bugReport);
            BugReportWorkflowService svc = BugReportServiceFactory.createWorkflowService();
            if (isNew) {
                svc.create(bugReport, getAuthenticatedUser().getUserName());
            } else {
                svc.update(bugReport, getAuthenticatedUser().getUserName());
            }
            WebloggerFactory.getWeblogger().flush();

            addMessage(isNew ? "bugReportForm.created" : "bugReportForm.updated");
            return SUCCESS;

        } catch (WebloggerException ex) {
            log.error("Error saving bug report", ex);
            addError("generic.error.check.logs");
        }
        return INPUT;
    }

    /**
     * Delete the user's own bug report.
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
            } else if (!report.isOwnedBy(getAuthenticatedUser().getUserName())) {
                addError("bugReportForm.error.notOwner");
            } else {
                BugReportWorkflowService svc = BugReportServiceFactory.createWorkflowService();
                svc.delete(report, getAuthenticatedUser().getUserName(), false);
                WebloggerFactory.getWeblogger().flush();
                addMessage("bugReportForm.deleted");
            }
        } catch (WebloggerException ex) {
            log.error("Error deleting bug report", ex);
            addError("generic.error.check.logs");
        }
        return execute();
    }

    public void myValidate() {
        if (StringUtils.isBlank(getBean().getTitle())) {
            addError("bugReportForm.error.titleRequired");
        }
        if (StringUtils.isBlank(getBean().getDescription())) {
            addError("bugReportForm.error.descriptionRequired");
        }
    }

    // -- accessors --

    public BugReportBean getBean() {
        return bean;
    }

    public void setBean(BugReportBean bean) {
        this.bean = bean;
    }

    public List<BugReport> getReports() {
        return reports;
    }

    public void setReports(List<BugReport> reports) {
        this.reports = reports;
    }

    public BugReport getBugReport() {
        return bugReport;
    }

    public String getReportId() {
        return reportId;
    }

    public void setReportId(String reportId) {
        this.reportId = reportId;
    }
}
