/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  The ASF licenses this file to You
 * under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.  For additional information regarding
 * copyright in this work, please see the NOTICE file in the top level
 * directory of this distribution.
 */

package org.apache.roller.weblogger.ui.struts2.core;

import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.business.StarManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.UserEntryStar;
import org.apache.roller.weblogger.ui.struts2.pagers.StarredEntriesPager;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;


/**
 * Displays the current user's starred entries with pagination.
 */
public class StarredEntriesAction extends UIAction {

    private static final Log LOG = LogFactory.getLog(StarredEntriesAction.class);

    private static final int PAGE_SIZE = 30;

    private StarredEntriesPager pager = null;
    private int page = 0;


    public StarredEntriesAction() {
        this.pageTitle = "starredEntries.title";
    }


    @Override
    public boolean isWeblogRequired() {
        return false;
    }


    @Override
    public String execute() {
        try {
            StarManager starMgr = WebloggerFactory.getWeblogger().getStarManager();

            // fetch PAGE_SIZE + 1 to detect if there are more
            int offset = page * PAGE_SIZE;
            List<UserEntryStar> entries = starMgr.getStarredEntries(
                    getAuthenticatedUser(), offset, PAGE_SIZE + 1);

            boolean hasMore = false;
            if (entries.size() > PAGE_SIZE) {
                hasMore = true;
                entries = entries.subList(0, PAGE_SIZE);
            }

            pager = new StarredEntriesPager("starredEntries.rol", page, entries, hasMore);

        } catch (Exception e) {
            LOG.error("Error loading starred entries", e);
            addError("starredEntries.error");
        }
        return SUCCESS;
    }


    public StarredEntriesPager getPager() {
        return pager;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }
}
