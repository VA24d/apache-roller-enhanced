/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  The ASF licenses this file to You
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

package org.apache.roller.weblogger.business.jpa.search;

import java.util.Date;
import java.util.Objects;

import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.WeblogEntryComment.ApprovalStatus;

/**
 * Parameter object for comment deletion and filtering operations.
 * Encapsulates multiple search criteria into a single object to simplify method signatures.
 */
public final class CommentSearchParams {

    private final Weblog weblog;
    private final WeblogEntry entry;
    private final String searchString;
    private final Date startDate;
    private final Date endDate;
    private final ApprovalStatus status;

    public CommentSearchParams(Weblog weblog, WeblogEntry entry, String searchString,
                              Date startDate, Date endDate, ApprovalStatus status) {
        this.weblog = Objects.requireNonNull(weblog, "weblog cannot be null");
        this.entry = entry;  // Can be null
        this.searchString = searchString;  // Can be null
        this.startDate = startDate != null ? new Date(startDate.getTime()) : null;
        this.endDate = endDate != null ? new Date(endDate.getTime()) : null;
        this.status = status;  // Can be null
    }

    public Weblog getWeblog() {
        return weblog;
    }

    public WeblogEntry getEntry() {
        return entry;
    }

    public String getSearchString() {
        return searchString;
    }

    public Date getStartDate() {
        return startDate != null ? new Date(startDate.getTime()) : null;
    }

    public Date getEndDate() {
        return endDate != null ? new Date(endDate.getTime()) : null;
    }

    public ApprovalStatus getStatus() {
        return status;
    }

    public boolean hasEntry() {
        return entry != null;
    }

    public boolean hasSearchString() {
        return searchString != null && !searchString.isEmpty();
    }

    public boolean hasStartDate() {
        return startDate != null;
    }

    public boolean hasEndDate() {
        return endDate != null;
    }

    public boolean hasStatus() {
        return status != null;
    }

    @Override
    public String toString() {
        return "CommentSearchParams{" +
                "weblog=" + weblog.getHandle() +
                ", entry=" + (entry != null ? entry.getId() : "null") +
                ", searchString=" + searchString +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", status=" + status +
                '}';
    }
}
