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

import java.util.Collections;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.business.StarManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;


/**
 * Displays the top 5 trending weblogs and entries (by star count).
 * Uses efficient aggregate queries — no iteration over individual articles.
 */
public class TrendingAction extends UIAction {

    private static final Log LOG = LogFactory.getLog(TrendingAction.class);

    private static final int TRENDING_LIMIT = 5;

    private List<Object[]> trendingWeblogs = Collections.emptyList();
    private List<Object[]> trendingEntries = Collections.emptyList();


    public TrendingAction() {
        this.pageTitle = "trending.title";
    }


    @Override
    public boolean isWeblogRequired() {
        return false;
    }


    @Override
    public String execute() {
        try {
            StarManager starMgr = WebloggerFactory.getWeblogger().getStarManager();
            trendingWeblogs = starMgr.getTrendingWeblogs(TRENDING_LIMIT);
            trendingEntries = starMgr.getTrendingEntries(TRENDING_LIMIT);
        } catch (Exception e) {
            LOG.error("Error loading trending data", e);
            addError("trending.error");
        }
        return SUCCESS;
    }


    public List<Object[]> getTrendingWeblogs() {
        return trendingWeblogs;
    }

    public void setTrendingWeblogs(List<Object[]> trendingWeblogs) {
        this.trendingWeblogs = trendingWeblogs;
    }

    public List<Object[]> getTrendingEntries() {
        return trendingEntries;
    }

    public void setTrendingEntries(List<Object[]> trendingEntries) {
        this.trendingEntries = trendingEntries;
    }
}
