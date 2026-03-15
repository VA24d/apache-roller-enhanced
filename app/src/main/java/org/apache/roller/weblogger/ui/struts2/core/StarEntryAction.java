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

import java.sql.Timestamp;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.StarManager;
import org.apache.roller.weblogger.business.WeblogEntryManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.UserEntryStar;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;


/**
 * Star / unstar a weblog entry.
 */
public class StarEntryAction extends UIAction {

    private static final Log LOG = LogFactory.getLog(StarEntryAction.class);

    private String entryId = null;
    private String entryPermalink = null;


    public StarEntryAction() {
        this.pageTitle = "starEntry.title";
    }


    @Override
    public boolean isWeblogRequired() {
        return false;
    }


    public String star() {
        try {
            StarManager starMgr = WebloggerFactory.getWeblogger().getStarManager();
            WeblogEntryManager entryMgr = WebloggerFactory.getWeblogger().getWeblogEntryManager();
            WeblogEntry entry = entryMgr.getWeblogEntry(entryId);

            if (entry == null) {
                addError("starEntry.notFound");
                return ERROR;
            }

            entryPermalink = entry.getPermalink();

            // Check if already starred
            UserEntryStar existing = starMgr.getEntryStarByUserAndEntry(
                    getAuthenticatedUser(), entry);
            if (existing == null) {
                UserEntryStar star = new UserEntryStar();
                star.setUser(getAuthenticatedUser());
                star.setEntry(entry);
                star.setStarredTime(new Timestamp(System.currentTimeMillis()));
                starMgr.saveEntryStar(star);
                WebloggerFactory.getWeblogger().flush();
            }

        } catch (WebloggerException e) {
            LOG.error("Error starring entry " + entryId, e);
        }
        return SUCCESS;
    }


    public String unstar() {
        try {
            StarManager starMgr = WebloggerFactory.getWeblogger().getStarManager();
            WeblogEntryManager entryMgr = WebloggerFactory.getWeblogger().getWeblogEntryManager();
            WeblogEntry entry = entryMgr.getWeblogEntry(entryId);

            if (entry == null) {
                addError("starEntry.notFound");
                return ERROR;
            }

            entryPermalink = entry.getPermalink();

            UserEntryStar existing = starMgr.getEntryStarByUserAndEntry(
                    getAuthenticatedUser(), entry);
            if (existing != null) {
                starMgr.removeEntryStar(existing);
                WebloggerFactory.getWeblogger().flush();
            }

        } catch (WebloggerException e) {
            LOG.error("Error unstarring entry " + entryId, e);
        }
        return SUCCESS;
    }


    public String getEntryId() {
        return entryId;
    }

    public void setEntryId(String entryId) {
        this.entryId = entryId;
    }

    public String getEntryPermalink() {
        return entryPermalink;
    }
}
