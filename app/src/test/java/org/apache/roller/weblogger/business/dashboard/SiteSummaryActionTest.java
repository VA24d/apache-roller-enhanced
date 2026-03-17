/*
 * Licensed under the Apache License, Version 2.0.
 */
package org.apache.roller.weblogger.business.dashboard;

import java.util.List;

import org.apache.roller.weblogger.pojos.GlobalPermission;
import org.apache.roller.weblogger.ui.struts2.admin.SiteSummary;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


class SiteSummaryActionTest {

    @Test
    void testDefaultViewIsFull() {
        SiteSummary action = new SiteSummary();
        assertEquals("full", action.getView());
    }

    @Test
    void testSetViewMinimalist() {
        SiteSummary action = new SiteSummary();
        action.setView("minimalist");
        assertEquals("minimalist", action.getView());
    }

    @Test
    void testRequiresAdminPermission() {
        SiteSummary action = new SiteSummary();
        List<String> perms = action.requiredGlobalPermissionActions();
        assertEquals(1, perms.size());
        assertEquals(GlobalPermission.ADMIN, perms.get(0));
    }

    @Test
    void testWeblogNotRequired() {
        SiteSummary action = new SiteSummary();
        assertFalse(action.isWeblogRequired());
    }

    @Test
    void testReportIsNullBeforeExecute() {
        SiteSummary action = new SiteSummary();
        assertNull(action.getReport());
    }
}
