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

package org.apache.roller.weblogger.pojos;

import java.io.Serializable;
import java.sql.Timestamp;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.roller.util.UUIDGenerator;


/**
 * Represents a user's star (favourite) on a weblog entry (blog post).
 */
public class UserEntryStar implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id = UUIDGenerator.generateUUID();
    private User user = null;
    private WeblogEntry entry = null;
    private Timestamp starredTime = null;


    public UserEntryStar() {
    }


    //------------------------------------------------------- Good citizenship

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("{");
        buf.append(getId());
        buf.append(", user=").append(getUser() != null ? getUser().getUserName() : "null");
        buf.append(", entry=").append(getEntry() != null ? getEntry().getAnchor() : "null");
        buf.append("}");
        return buf.toString();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof UserEntryStar)) {
            return false;
        }
        final UserEntryStar that = (UserEntryStar) other;
        return new EqualsBuilder()
                .append(getUser(), that.getUser())
                .append(getEntry(), that.getEntry())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(getUser())
                .append(getEntry())
                .toHashCode();
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public WeblogEntry getEntry() {
        return entry;
    }

    public void setEntry(WeblogEntry entry) {
        this.entry = entry;
    }

    public Timestamp getStarredTime() {
        return starredTime;
    }

    public void setStarredTime(Timestamp starredTime) {
        this.starredTime = starredTime;
    }

}
