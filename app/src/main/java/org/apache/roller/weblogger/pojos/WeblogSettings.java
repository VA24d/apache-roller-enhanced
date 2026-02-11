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

package org.apache.roller.weblogger.pojos;

import java.io.Serializable;
import java.util.Date;

public class WeblogSettings implements Serializable {

    private static final long serialVersionUID = 5478450117742642911L;

    private Boolean enableBloggerApi = Boolean.TRUE;
    private String editorPage = null;
    private String bannedwordslist = null;
    private Boolean allowComments = Boolean.TRUE;
    private Boolean emailComments = Boolean.FALSE;
    private String emailAddress = null;
    private String locale = null;
    private String timeZone = null;
    private String defaultPlugins = null;
    private Boolean visible = Boolean.TRUE;
    private Boolean active = Boolean.TRUE;
    private Date dateCreated = new Date();
    private Boolean defaultAllowComments = Boolean.TRUE;
    private int defaultCommentDays = 0;
    private Boolean moderateComments = Boolean.FALSE;
    private int entryDisplayCount = 15;
    private Date lastModified = new Date();
    private boolean enableMultiLang = false;
    private boolean showAllLangs = true;
    private String iconPath = null;
    private String about = null;
    private String creator = null;
    private String analyticsCode = null;

    public Boolean getEnableBloggerApi() {
        return enableBloggerApi;
    }

    public void setEnableBloggerApi(Boolean enableBloggerApi) {
        this.enableBloggerApi = enableBloggerApi;
    }

    public String getEditorPage() {
        return editorPage;
    }

    public void setEditorPage(String editorPage) {
        this.editorPage = normalizeNullable(editorPage);
    }

    public String getBannedwordslist() {
        return bannedwordslist;
    }

    public void setBannedwordslist(String bannedwordslist) {
        this.bannedwordslist = normalizeNullable(bannedwordslist);
    }

    public Boolean getAllowComments() {
        return allowComments;
    }

    public void setAllowComments(Boolean allowComments) {
        this.allowComments = allowComments;
    }

    public Boolean getEmailComments() {
        return emailComments;
    }

    public void setEmailComments(Boolean emailComments) {
        this.emailComments = emailComments;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = normalizeNullable(emailAddress);
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = normalizeNullable(locale);
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = normalizeNullable(timeZone);
    }

    public String getDefaultPlugins() {
        return defaultPlugins;
    }

    public void setDefaultPlugins(String defaultPlugins) {
        this.defaultPlugins = normalizeNullable(defaultPlugins);
    }

    public Boolean getVisible() {
        return visible;
    }

    public void setVisible(Boolean visible) {
        this.visible = visible;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Date getDateCreated() {
        if (dateCreated == null) {
            return null;
        }
        return (Date) dateCreated.clone();
    }

    public void setDateCreated(Date dateCreated) {
        if (dateCreated == null) {
            this.dateCreated = null;
        } else {
            this.dateCreated = (Date) dateCreated.clone();
        }
    }

    public Boolean getDefaultAllowComments() {
        return defaultAllowComments;
    }

    public void setDefaultAllowComments(Boolean defaultAllowComments) {
        this.defaultAllowComments = defaultAllowComments;
    }

    public int getDefaultCommentDays() {
        return defaultCommentDays;
    }

    public void setDefaultCommentDays(int defaultCommentDays) {
        if (defaultCommentDays < 0) {
            throw new IllegalArgumentException("defaultCommentDays must be >= 0");
        }
        this.defaultCommentDays = defaultCommentDays;
    }

    public Boolean getModerateComments() {
        return moderateComments;
    }

    public void setModerateComments(Boolean moderateComments) {
        this.moderateComments = moderateComments;
    }

    public int getEntryDisplayCount() {
        return entryDisplayCount;
    }

    public void setEntryDisplayCount(int entryDisplayCount) {
        if (entryDisplayCount < 0) {
            throw new IllegalArgumentException("entryDisplayCount must be >= 0");
        }
        this.entryDisplayCount = entryDisplayCount;
    }

    public Date getLastModified() {
        if (lastModified == null) {
            return null;
        }
        return (Date) lastModified.clone();
    }

    public void setLastModified(Date lastModified) {
        if (lastModified == null) {
            this.lastModified = null;
        } else {
            this.lastModified = (Date) lastModified.clone();
        }
    }

    public boolean isEnableMultiLang() {
        return enableMultiLang;
    }

    public void setEnableMultiLang(boolean enableMultiLang) {
        this.enableMultiLang = enableMultiLang;
    }

    public boolean isShowAllLangs() {
        return showAllLangs;
    }

    public void setShowAllLangs(boolean showAllLangs) {
        this.showAllLangs = showAllLangs;
    }

    public String getIconPath() {
        return iconPath;
    }

    public void setIconPath(String iconPath) {
        this.iconPath = normalizeNullable(iconPath);
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = normalizeNullable(about);
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = normalizeNullable(creator);
    }

    public String getAnalyticsCode() {
        return analyticsCode;
    }

    public void setAnalyticsCode(String analyticsCode) {
        this.analyticsCode = normalizeNullable(analyticsCode);
    }

    private static String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
