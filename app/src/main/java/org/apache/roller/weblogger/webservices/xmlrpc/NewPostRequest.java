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

package org.apache.roller.weblogger.webservices.xmlrpc;

import java.sql.Timestamp;
import java.util.Date;

import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;

/**
 * Parameter object encapsulating all data needed to populate a new
 * {@link org.apache.roller.weblogger.pojos.WeblogEntry}.
 *
 * <p>Replaces the 8-parameter {@code populateEntryFromRequest()} signature
 * with a single cohesive object, improving readability and maintainability.</p>
 */
public class NewPostRequest {

    private final String title;
    private final String description;
    private final Weblog website;
    private final User user;
    private final Date dateCreated;
    private final Timestamp updateTime;
    private final boolean publish;

    public NewPostRequest(String title, String description, Weblog website,
                          User user, Date dateCreated, Timestamp updateTime,
                          boolean publish) {
        this.title = title;
        this.description = description;
        this.website = website;
        this.user = user;
        this.dateCreated = dateCreated;
        this.updateTime = updateTime;
        this.publish = publish;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Weblog getWebsite() {
        return website;
    }

    public User getUser() {
        return user;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public Timestamp getUpdateTime() {
        return updateTime;
    }

    public boolean isPublish() {
        return publish;
    }
}
