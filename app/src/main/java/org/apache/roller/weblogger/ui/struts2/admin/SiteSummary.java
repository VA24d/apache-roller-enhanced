/*
 * Licensed under the Apache License, Version 2.0.
 */
package org.apache.roller.weblogger.ui.struts2.admin;

import java.util.Collections;
import java.util.List;

import org.apache.roller.weblogger.business.dashboard.DashboardReport;
import org.apache.roller.weblogger.business.dashboard.DashboardReportBuilder;
import org.apache.roller.weblogger.pojos.GlobalPermission;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;


/**
 * Admin action for the Site Summary Dashboard.
 *
 * <p>Supports two view modes (Minimalist and Full) toggled via the
 * {@code view} parameter. Uses the Builder pattern to assemble the
 * appropriate report.
 */
public class SiteSummary extends UIAction {

    // Input: "minimalist" or "full"
    private String view = "full";

    // Output: the assembled report
    private DashboardReport report;


    public SiteSummary() {
        this.actionName = "siteSummary";
        this.desiredMenu = "admin";
        this.pageTitle = "siteSummary.title";
    }


    @Override
    public List<String> requiredGlobalPermissionActions() {
        return Collections.singletonList(GlobalPermission.ADMIN);
    }

    @Override
    public boolean isWeblogRequired() {
        return false;
    }


    @Override
    public String execute() {
        if ("minimalist".equalsIgnoreCase(view)) {
            report = DashboardReportBuilder.buildMinimalistReport();
        } else {
            report = DashboardReportBuilder.buildFullReport();
        }
        return SUCCESS;
    }


    // --- Getters / Setters ---

    public String getView() { return view; }
    public void setView(String view) { this.view = view; }

    public DashboardReport getReport() { return report; }
    public void setReport(DashboardReport report) { this.report = report; }
}
