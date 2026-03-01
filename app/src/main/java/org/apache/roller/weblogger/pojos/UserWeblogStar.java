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
 * Represents a user's star (favourite) on a weblog.
 */
public class UserWeblogStar implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id = UUIDGenerator.generateUUID();
    private User user = null;
    private Weblog weblog = null;
    private Timestamp starredTime = null;


    public UserWeblogStar() {
    }


    //------------------------------------------------------- Good citizenship

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("{");
        buf.append(getId());
        buf.append(", user=").append(getUser() != null ? getUser().getUserName() : "null");
        buf.append(", weblog=").append(getWeblog() != null ? getWeblog().getHandle() : "null");
        buf.append("}");
        return buf.toString();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof UserWeblogStar)) {
            return false;
        }
        final UserWeblogStar that = (UserWeblogStar) other;
        return new EqualsBuilder()
                .append(getUser(), that.getUser())
                .append(getWeblog(), that.getWeblog())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(getUser())
                .append(getWeblog())
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

    public Weblog getWeblog() {
        return weblog;
    }

    public void setWeblog(Weblog weblog) {
        this.weblog = weblog;
    }

    public Timestamp getStarredTime() {
        return starredTime;
    }

    public void setStarredTime(Timestamp starredTime) {
        this.starredTime = starredTime;
    }

}
