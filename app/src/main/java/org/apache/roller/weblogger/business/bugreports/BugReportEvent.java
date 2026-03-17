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

package org.apache.roller.weblogger.business.bugreports;

import java.sql.Timestamp;
import org.apache.roller.weblogger.pojos.BugReport;

/**
 * Immutable event payload for bug report lifecycle changes.
 */
public class BugReportEvent {

    private final BugReportEventType eventType;
    private final BugReport bugReport;
    private final String actorUserName;
    private final Timestamp when;

    public BugReportEvent(BugReportEventType eventType, BugReport bugReport, String actorUserName) {
        this.eventType = eventType;
        this.bugReport = bugReport;
        this.actorUserName = actorUserName;
        this.when = new Timestamp(System.currentTimeMillis());
    }

    public BugReportEventType getEventType() {
        return eventType;
    }

    public BugReport getBugReport() {
        return bugReport;
    }

    public String getActorUserName() {
        return actorUserName;
    }

    public Timestamp getWhen() {
        return when;
    }
}
