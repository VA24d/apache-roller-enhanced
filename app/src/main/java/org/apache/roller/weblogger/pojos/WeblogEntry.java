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

package org.apache.roller.weblogger.pojos;

import java.io.Serializable;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.util.DateFormatUtil;
import org.apache.roller.util.RollerConstants;
import org.apache.roller.util.UUIDGenerator;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.business.WeblogEntryManager;
import org.apache.roller.weblogger.business.URLStrategy;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.config.WebloggerRuntimeConfig;
import org.apache.roller.weblogger.util.Utilities;

/**
 * Represents a Weblog Entry.
 *
 * <p>This is a domain entity focusing on data and business rules.
 * Presentation/view concerns have been extracted to {@link WeblogEntryPresenter}.
 * Plugin configuration is encapsulated in {@link PluginConfiguration}.
 * Tag management is encapsulated in {@link TagManifest}.</p>
 */
public class WeblogEntry implements Serializable {
    private static final Log mLogger = LogFactory.getFactory().getInstance(WeblogEntry.class);
    
    public static final long serialVersionUID = 2341505386843044125L;

    /** Default title separator for anchor generation. Injectable for testability. */
    public static final char DEFAULT_TITLE_SEPARATOR = '-';
    public static final char UNDERSCORE_TITLE_SEPARATOR = '_';

    public enum PubStatus {DRAFT, PUBLISHED, PENDING, SCHEDULED}

    // ---- Core properties (persisted) ----
    private String    id            = UUIDGenerator.generateUUID();
    private String    title         = null;
    private String    link          = null;
    private String    summary       = null;
    private String    text          = null;
    private String    contentType   = null;
    private String    contentSrc    = null;
    private String    anchor        = null;
    private Timestamp pubTime       = null;
    private Timestamp updateTime    = null;
    private String    plugins       = null;
    private Boolean   allowComments = Boolean.TRUE;
    private Integer   commentDays   = 7;
    private Boolean   rightToLeft   = Boolean.FALSE;
    private Boolean   pinnedToMain  = Boolean.FALSE;
    private PubStatus status        = PubStatus.DRAFT;
    private String    locale        = null;
    private String    creatorUserName = null;      
    private String    searchDescription = null;

    // Set to true when switching between pending/draft/scheduled and published.
    // Either the aggregate table needs the entry's tags added (for published)
    // or subtracted (anything else).
    private Boolean   refreshAggregates = Boolean.FALSE;

    // ---- Associated objects ----
    private Weblog        website  = null;
    private WeblogCategory category = null;

    // Resolved creator (injected, avoids Service Locator calls)
    private transient User creator = null;
    
    // Collection of name/value entry attributes
    private Set<WeblogEntryAttribute> attSet = new TreeSet<>();
    
    // Tag management via TagManifest (delegates business logic, raw sets kept for JPA)
    private transient TagManifest tagManifest = new TagManifest();
    private Set<WeblogEntryTag> tagSet = new HashSet<>();
    private Set<WeblogEntryTag> removedTags = new HashSet<>();
    private Set<WeblogEntryTag> addedTags = new HashSet<>();

    // Injectable title separator for anchor generation (removes static config coupling)
    private transient char titleSeparator = DEFAULT_TITLE_SEPARATOR;

    /**
     * Builder for WeblogEntry to avoid long constructor parameter lists.
     */
    public static class Builder {
        private final WeblogEntry entry;

        public Builder() {
            this.entry = new WeblogEntry();
        }

        public Builder id(String id) {
            entry.setId(id);
            return this;
        }

        public Builder category(WeblogCategory category) {
            entry.setCategory(category);
            return this;
        }

        public Builder website(Weblog website) {
            entry.setWebsite(website);
            return this;
        }

        public Builder creator(User creator) {
            entry.setCreator(creator);
            return this;
        }

        public Builder title(String title) {
            entry.setTitle(title);
            return this;
        }

        public Builder link(String link) {
            entry.setLink(link);
            return this;
        }

        public Builder text(String text) {
            entry.setText(text);
            return this;
        }

        public Builder anchor(String anchor) {
            entry.setAnchor(anchor);
            return this;
        }

        public Builder pubTime(Timestamp pubTime) {
            entry.setPubTime(pubTime);
            return this;
        }

        public Builder updateTime(Timestamp updateTime) {
            entry.setUpdateTime(updateTime);
            return this;
        }

        public Builder status(PubStatus status) {
            entry.setStatus(status);
            return this;
        }
        
        public Builder locale(String locale) {
            entry.setLocale(locale);
            return this;
        }

        public WeblogEntry build() {
            return entry;
        }
    }
    
    //----------------------------------------------------------- Construction
    
    public WeblogEntry() {
    }
    
    public WeblogEntry(
            WeblogCategory category,
            Weblog website,
            User creator,
            String title,
            String link,
            String text,
            String anchor,
            Timestamp pubTime,
            Timestamp updateTime,
            PubStatus status) {
        this.category = category;
        this.website = website;
        this.creator = creator;
        this.creatorUserName = creator.getUserName();
        this.title = title;
        this.link = link;
        this.text = text;
        this.anchor = anchor;
        this.pubTime = pubTime;
        this.updateTime = updateTime;
        this.status = status;
    }
    
    public WeblogEntry(WeblogEntry otherData) {
        this.setData(otherData);
    }
    
    //---------------------------------------------------------- Initializaion
    
    /**
     * Set bean properties based on other bean.
     */
    public void setData(WeblogEntry other) {
        
        this.setId(other.getId());
        this.setCategory(other.getCategory());
        this.setWebsite(other.getWebsite());
        this.setCreatorUserName(other.getCreatorUserName());
        this.creator = other.creator;
        this.setTitle(other.getTitle());
        this.setLink(other.getLink());
        this.setText(other.getText());
        this.setSummary(other.getSummary());
        this.setSearchDescription(other.getSearchDescription());
        this.setAnchor(other.getAnchor());
        this.setPubTime(other.getPubTime());
        this.setUpdateTime(other.getUpdateTime());
        this.setStatus(other.getStatus());
        this.setPlugins(other.getPlugins());
        this.setAllowComments(other.getAllowComments());
        this.setCommentDays(other.getCommentDays());
        this.setRightToLeft(other.getRightToLeft());
        this.setPinnedToMain(other.getPinnedToMain());
        this.setLocale(other.getLocale());
    }

    /**
     * Set the title separator character for anchor generation.
     * Allows injection for testability (removes static config coupling).
     */
    public void setTitleSeparator(char separator) {
        this.titleSeparator = separator;
    }
    
    //------------------------------------------------------- Good citizenship

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("{");
        buf.append(getId());
        buf.append(", ").append(this.getAnchor());
        buf.append(", ").append(this.getTitle());
        buf.append(", ").append(this.getPubTime());
        buf.append("}");
        return buf.toString();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof WeblogEntry)) {
            return false;
        }
        WeblogEntry o = (WeblogEntry) other;
        return Objects.equals(getId(), o.getId());
    }
    
    @Override
    public int hashCode() { 
        return Objects.hashCode(getId());
    }
    
   //------------------------------------------------------ Simple properties
    
    public String getId() {
        return this.id;
    }
    
    public void setId(String id) {
        // Form bean workaround: empty string is never a valid id
        if (id != null && id.isBlank()) {
            return;
        }
        this.id = id;
    }
    
    public WeblogCategory getCategory() {
        return this.category;
    }
    
    public void setCategory(WeblogCategory category) {
        this.category = category;
    }
       
    /**
     * Return collection of WeblogCategory objects of this entry.
     * Added for symmetry with PlanetEntryData object.
     */
    public List<WeblogCategory> getCategories() {
        return List.of(getCategory());
    }
    
    public Weblog getWebsite() {
        return this.website;
    }
    
    public void setWebsite(Weblog website) {
        this.website = website;
    }
    
    /**
     * Get the creator User. Prefers the injected reference;
     * falls back to Service Locator lookup if not set.
     * 
     * <p>Callers should prefer injecting the User via {@link #setCreator(User)}
     * or the constructor to avoid the Service Locator anti-pattern.</p>
     */
    public User getCreator() {
        if (creator != null) {
            return creator;
        }
        // Fallback: resolve via factory (legacy Service Locator)
        try {
            User resolved = WebloggerFactory.getWeblogger().getUserManager()
                    .getUserByUserName(getCreatorUserName());
            this.creator = resolved;
            return resolved;
        } catch (Exception e) {
            mLogger.error("ERROR fetching user object for username: " + getCreatorUserName(), e);
        }
        return null;
    }

    /**
     * Inject the creator User directly (Dependency Injection).
     * Preferred over letting the entity resolve via WebloggerFactory.
     */
    public void setCreator(User creator) {
        this.creator = creator;
        if (creator != null) {
            this.creatorUserName = creator.getUserName();
        }
    }   
    
    public String getCreatorUserName() {
        return creatorUserName;
    }

    public void setCreatorUserName(String creatorUserName) {
        this.creatorUserName = creatorUserName;
    }   
    
    public String getTitle() {
        return this.title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    /**
     * Get summary for weblog entry (maps to RSS description and Atom summary).
     */
    public String getSummary() {
        return summary;
    }
    
    /**
     * Set summary for weblog entry (maps to RSS description and Atom summary).
     */
    public void setSummary(String summary) {
        this.summary = summary;
    }
    
    /**
     * Get search description for weblog entry.
     */
    public String getSearchDescription() {
        return searchDescription;
    }
    
    /**
     * Set search description for weblog entry
     */
    public void setSearchDescription(String searchDescription) {
        this.searchDescription = searchDescription;
    }

    /**
     * Get content text for weblog entry (maps to RSS content:encoded and Atom content).
     */
    public String getText() {
        return this.text;
    }
    
    /**
     * Set content text for weblog entry (maps to RSS content:encoded and Atom content).
     */
    public void setText(String text) {
        this.text = text;
    }
    
    /**
     * Get content type (text, html, xhtml or a MIME content type)
     */
    public String getContentType() {
        return contentType;
    }
    
    /**
     * Set content type (text, html, xhtml or a MIME content type)
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
    
    /**
     * Get URL for out-of-line content.
     */
    public String getContentSrc() {
        return contentSrc;
    }
    
    /**
     * Set URL for out-of-line content.
     */
    public void setContentSrc(String contentSrc) {
        this.contentSrc = contentSrc;
    }
    
    public String getAnchor() {
        return this.anchor;
    }
    
    public void setAnchor(String anchor) {
        this.anchor = anchor;
    }
    
    //-------------------------------------------------------------------------

    /**
     * Returns the entry attributes set.
     * Use {@link #putEntryAttribute(String, String)} and
     * {@link #removeEntryAttribute(String)} to modify.
     */
    public Set<WeblogEntryAttribute> getEntryAttributes() {
        return attSet;
    }

    /**
     * Replace the entire attribute set (used by JPA/ORM framework).
     */
    public void setEntryAttributes(Set<WeblogEntryAttribute> atts) {
        this.attSet = atts;
    }
    
    public String findEntryAttribute(String name) {
        for (WeblogEntryAttribute att : attSet) {
            if (name.equals(att.getName())) {
                return att.getValue();
            }
        }
        return null;
    }
        
    public void putEntryAttribute(String name, String value) throws Exception {
        WeblogEntryAttribute att = null;
        for (WeblogEntryAttribute o : attSet) {
            if (name.equals(o.getName())) {
                att = o; 
                break;
            }
        }
        if (att == null) {
            att = new WeblogEntryAttribute();
            att.setEntry(this);
            att.setName(name);
            att.setValue(value);
            attSet.add(att);
        } else {
            att.setValue(value);
        }
    }

    /**
     * Remove an entry attribute by name.
     * @return true if the attribute was found and removed
     */
    public boolean removeEntryAttribute(String name) {
        return attSet.removeIf(att -> name.equals(att.getName()));
    }
    
    //-------------------------------------------------------------------------
    
    /**
     * <p>Publish time is the time that an entry is to be (or was) made available
     * for viewing by newsfeed readers and visitors to the Roller site.</p>
     *
     * <p>Roller stores time using the timeZone of the server itself. When
     * times are displayed  in a user's weblog they must be translated
     * to the user's timeZone.</p>
     *
     * <p>NOTE: Times are stored using the SQL TIMESTAMP datatype, which on
     * MySQL has only a one-second resolution.</p>
     */
    public Timestamp getPubTime() {
        return this.pubTime;
    }
    
    public void setPubTime(Timestamp pubTime) {
        this.pubTime = pubTime;
    }
    
    /**
     * <p>Update time is the last time that an weblog entry was saved in the
     * Roller weblog editor or via web services API (XML-RPC or Atom).</p>
     *
     * <p>Roller stores time using the timeZone of the server itself. When
     * times are displayed  in a user's weblog they must be translated
     * to the user's timeZone.</p>
     *
     * <p>NOTE: Times are stored using the SQL TIMESTAMP datatype, which on
     * MySQL has only a one-second resolution.</p>
     */
    public Timestamp getUpdateTime() {
        return this.updateTime;
    }
    
    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }
    
    public PubStatus getStatus() {
        return this.status;
    }
    
    public void setStatus(PubStatus status) {
        this.status = status;
    }
    
    /**
     * Some weblog entries are about one specific link.
     * @return Returns the link.
     */
    public String getLink() {
        return link;
    }
    
    /**
     * @param link The link to set.
     */
    public void setLink(String link) {
        this.link = link;
    }
    
    /**
     * Comma-delimited list of this entry's Plugins.
     * Prefer using {@link #getPluginConfiguration()} for structured access.
     */
    public String getPlugins() {
        return plugins;
    }
    
    public void setPlugins(String string) {
        plugins = string;
    }

    /**
     * Returns an immutable {@link PluginConfiguration} for structured access
     * to the plugin list. Replaces raw string parsing scattered through the entity.
     */
    public PluginConfiguration getPluginConfiguration() {
        return new PluginConfiguration(plugins);
    }

    /**
     * True if comments are allowed on this weblog entry.
     */
    public Boolean getAllowComments() {
        return allowComments;
    }
    /**
     * True if comments are allowed on this weblog entry.
     */
    public void setAllowComments(Boolean allowComments) {
        this.allowComments = allowComments;
    }
    
    /**
     * Number of days after pubTime that comments should be allowed, or 0 for no limit.
     */
    public Integer getCommentDays() {
        return commentDays;
    }
    /**
     * Number of days after pubTime that comments should be allowed, or 0 for no limit.
     */
    public void setCommentDays(Integer commentDays) {
        this.commentDays = commentDays;
    }
    
    /**
     * True if this entry should be rendered right to left.
     */
    public Boolean getRightToLeft() {
        return rightToLeft;
    }
    /**
     * True if this entry should be rendered right to left.
     */
    public void setRightToLeft(Boolean rightToLeft) {
        this.rightToLeft = rightToLeft;
    }
    
    /**
     * True if story should be pinned to the top of the Roller site main blog.
     * @return Returns the pinned.
     */
    public Boolean getPinnedToMain() {
        return pinnedToMain;
    }
    /**
     * True if story should be pinned to the top of the Roller site main blog.
     * @param pinnedToMain The pinned to set.
     */
    public void setPinnedToMain(Boolean pinnedToMain) {
        this.pinnedToMain = pinnedToMain;
    }

    /**
     * The locale string that defines the i18n approach for this entry.
     */
    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }
    
    /**
     * Returns the tag set. Note: JPA/ORM requires mutable access to this
     * collection via PROPERTY access. Application code should prefer
     * {@link #addTag(String)} and {@link #setTagsAsString(String)}.
     */
    public Set<WeblogEntryTag> getTags() {
         return tagSet;
    }

    @SuppressWarnings("unused")
    public void setTags(Set<WeblogEntryTag> tagSet) throws WebloggerException {
         this.tagSet = tagSet;
         this.removedTags = new HashSet<>();
         this.addedTags = new HashSet<>();
         // Sync TagManifest with JPA-loaded tags
         this.tagManifest.setTags(tagSet);
    }
     
    /**
     * Roller lowercases all tags based on locale because there's not a 1:1 mapping
     * between uppercase/lowercase characters across all languages.  
     * @param name tag name to add
     * @throws WebloggerException on error
     */
    public void addTag(String name) throws WebloggerException {
        Locale localeObject = getWebsite() != null ? getWebsite().getLocaleInstance() : Locale.getDefault();
        tagManifest.addTag(name, getCreatorUserName(), getWebsite(), this, localeObject, getUpdateTime());
        // Sync raw sets from TagManifest for JPA
        syncTagsFromManifest();
    }

    public Set<WeblogEntryTag> getAddedTags() {
        return tagManifest.getAddedTags();
    }
    
    public Set<WeblogEntryTag> getRemovedTags() {
        return tagManifest.getRemovedTags();
    }

    public String getTagsAsString() {
        return tagManifest.toSortedString();
    }

    public void setTagsAsString(String tags) throws WebloggerException {
        Locale localeObject = getWebsite() != null ? getWebsite().getLocaleInstance() : Locale.getDefault();
        tagManifest.setTagsFromString(tags, getCreatorUserName(), getWebsite(), this, localeObject, getUpdateTime());
        // Sync raw sets from TagManifest for JPA
        syncTagsFromManifest();
    }

    /**
     * Returns the TagManifest for direct access to tag management.
     */
    public TagManifest getTagManifest() {
        return tagManifest;
    }

    /**
     * Sync the raw JPA-visible tag sets from the TagManifest.
     */
    private void syncTagsFromManifest() {
        // TagManifest.getMutableTags() is package-private for this sync
        this.tagSet = tagManifest.getMutableTags();
        this.addedTags = new HashSet<>(tagManifest.getAddedTags());
        this.removedTags = new HashSet<>(tagManifest.getRemovedTags());
    }

    // ------------------------------------------------------------------------
    
    /**
     * True if comments are still allowed on this entry considering the
     * allowComments and commentDays fields as well as the website and 
     * site-wide configs.
     *
     * @deprecated Couples to static runtime config. Use
     * {@link #getCommentsStillAllowed(boolean)} and pass the site-wide setting.
     */
    @Deprecated
    public boolean getCommentsStillAllowed() {
        return getCommentsStillAllowed(
                WebloggerRuntimeConfig.getBooleanProperty("users.comments.enabled"));
    }

    /**
     * True if comments are still allowed on this entry considering the
     * allowComments and commentDays fields as well as the website config
     * and the provided site-wide comments-enabled flag.
     *
     * @param siteCommentsEnabled whether comments are enabled at the site level
     */
    public boolean getCommentsStillAllowed(boolean siteCommentsEnabled) {
        if (!siteCommentsEnabled) {
            return false;
        }
        if (getWebsite() != null && Boolean.FALSE.equals(getWebsite().getAllowComments())) {
            return false;
        }
        if (Boolean.FALSE.equals(getAllowComments())) {
            return false;
        }
        
        Integer days = getCommentDays();
        if (days == null || days == 0) {
            return true;
        }

        // we want to use pubtime for calculating when comments expire, but
        // if pubtime isn't set (like for drafts) then just use updatetime
        Date calculationDate = getPubTime();
        if (calculationDate == null) {
            calculationDate = getUpdateTime();
        }
        
        if (calculationDate == null) {
            return true;
        }
        
        Instant expiration = calculationDate.toInstant().plus(days, ChronoUnit.DAYS);
        return Instant.now().isBefore(expiration);
    }
    
    
    //------------------------------------------------------------------------
    
    /**
     * Format the publish time of this weblog entry using the specified pattern.
     * @deprecated Presentation concern. Use {@link WeblogEntryPresenter#formatPubTime(String)}.
     */
    @Deprecated
    public String formatPubTime(String pattern) {
        return new WeblogEntryPresenter(this).formatPubTime(pattern);
    }
    
    //------------------------------------------------------------------------
    
    /**
     * Format the update time of this weblog entry using the specified pattern.
     * @deprecated Presentation concern. Use {@link WeblogEntryPresenter#formatUpdateTime(String)}.
     */
    @Deprecated
    public String formatUpdateTime(String pattern) {
        return new WeblogEntryPresenter(this).formatUpdateTime(pattern);
    }
    
    //------------------------------------------------------------------------
    
    /**
     * @deprecated Couples to WebloggerFactory (Service Locator). Use
     * {@link #getComments(WeblogEntryManager)} instead.
     */
    @Deprecated
    public List<WeblogEntryComment> getComments() {
        // LEGACY ADAPTER
        return getComments(true, true);
    }
    
    /**
     * @deprecated Couples to WebloggerFactory (Service Locator). Use
     * {@link #getComments(WeblogEntryManager, boolean)} instead.
     */
    @Deprecated
    public List<WeblogEntryComment> getComments(boolean ignoreSpam, boolean approvedOnly) {
        // LEGACY ADAPTER
        WeblogEntryManager wmgr = WebloggerFactory.getWeblogger().getWeblogEntryManager();
        return getComments(wmgr, approvedOnly);
    }

    /**
     * Get comments for this entry using an injected WeblogEntryManager (DI-friendly).
     *
     * @param wmgr the WeblogEntryManager to use for comment retrieval
     * @param approvedOnly if true, return only approved comments
     * @return list of comments, or empty list on error
     */
    public List<WeblogEntryComment> getComments(WeblogEntryManager wmgr, boolean approvedOnly) {
        try {
            CommentSearchCriteria csc = new CommentSearchCriteria();
            csc.setWeblog(getWebsite());
            csc.setEntry(this);
            csc.setStatus(approvedOnly ? WeblogEntryComment.ApprovalStatus.APPROVED : null);
            return wmgr.getComments(csc);
        } catch (WebloggerException alreadyLogged) {
            // Ignored, empty list returned
        }
        
        return Collections.emptyList();
    }
    
    public int getCommentCount() {
        return getComments().size();
    }
    
    //------------------------------------------------------------------------
        
    /**
     * Returns absolute entry permalink.
     * @deprecated Couples to WebloggerFactory (Service Locator). Use
     * {@link #getPermalink(URLStrategy)} instead.
     */
    @Deprecated
    public String getPermalink() {
        // LEGACY ADAPTER
        return getPermalink(WebloggerFactory.getWeblogger().getUrlStrategy());
    }

    /**
     * Returns absolute entry permalink using an injected URLStrategy (DI-friendly).
     *
     * @param urlStrategy the URL strategy to use for link generation
     * @return the absolute permalink URL
     */
    public String getPermalink(URLStrategy urlStrategy) {
        return urlStrategy.getWeblogEntryURL(getWebsite(), null, getAnchor(), true);
    }
    
    /**
     * Returns entry permalink, relative to Roller context.
     * @deprecated Use getPermalink() instead.
     */
    @Deprecated
    public String getPermaLink() {
        return getRelativePermalink();
    }

    /**
     * Returns entry permalink, relative to Roller context.
     * @deprecated Use getPermalink() instead.
     */
    @Deprecated
    public String getRelativePermalink() {
        String lAnchor = URLEncoder.encode(getAnchor(), StandardCharsets.UTF_8);
        return "/" + getWebsite().getHandle() + "/entry/" + lAnchor;
    }
    
    /**
     * Get relative URL to comments page.
     * @deprecated Use commentLink() instead
     */
    @Deprecated
    public String getCommentsLink() {
        return getRelativePermalink() + "#comments";
    }
    
    /**
     * Return the Title of this post, or the first 255 characters of the
     * entry's text.
     * @deprecated Presentation concern. Use {@link WeblogEntryPresenter#getDisplayTitle()}.
     */
    @Deprecated
    public String getDisplayTitle() {
        return new WeblogEntryPresenter(this).getDisplayTitle();
    }
    
    /**
     * Return RSS 09x style description (escaped HTML version of entry text).
     * @deprecated Presentation concern. Use {@link WeblogEntryPresenter#getRss09xDescription()}.
     */
    @Deprecated
    public String getRss09xDescription() {
        return new WeblogEntryPresenter(this).getRss09xDescription();
    }
    
    /**
     * Return RSS 09x style description (escaped HTML version of entry text).
     * @deprecated Presentation concern. Use {@link WeblogEntryPresenter#getRss09xDescription(int)}.
     */
    @Deprecated
    public String getRss09xDescription(int maxLength) {
        return new WeblogEntryPresenter(this).getRss09xDescription(maxLength);
    }
    
    /**
     * Create anchor for weblog entry, based on title or text.
     * @deprecated Couples to WebloggerFactory (Service Locator). Use
     * {@link #createAnchor(WeblogEntryManager)} instead.
     */
    @Deprecated
    protected String createAnchor() throws WebloggerException {
        // LEGACY ADAPTER
        return createAnchor(WebloggerFactory.getWeblogger().getWeblogEntryManager());
    }

    /**
     * Create anchor for weblog entry using an injected WeblogEntryManager (DI-friendly).
     */
    protected String createAnchor(WeblogEntryManager mgr) throws WebloggerException {
        return mgr.createAnchor(this);
    }
    
    /** Create anchor for weblog entry, based on title or text */
    public String createAnchorBase() {
        
        // Use title (minus non-alphanumeric characters)
        String base = null;
        if (StringUtils.isNotEmpty(getTitle())) {
            base = Utilities.replaceNonAlphanumeric(getTitle(), ' ').trim();    
        }
        // If we still have no base, then try text (minus non-alphanumerics)
        if (StringUtils.isEmpty(base) && StringUtils.isNotEmpty(getText())) {
            base = Utilities.replaceNonAlphanumeric(getText(), ' ').trim();  
        }
        
        if (StringUtils.isNotEmpty(base)) {
            
            // Use only the first 5 words
            String[] tokens = base.split("\\s+");
            StringBuilder sb = new StringBuilder();
            int limit = Math.min(tokens.length, 5);

            for (int i = 0; i < limit; i++) {
                if (i > 0) {
                    sb.append(titleSeparator);
                }
                sb.append(tokens[i].toLowerCase(Locale.ROOT));
            }
            base = sb.toString();
        }
        // No title or text, so instead we will use the items date
        // in YYYYMMDD format as the base anchor
        else {
            base = DateFormatUtil.format8chars(getPubTime());
        }
        
        return base;
    }
    
    /**
     * Create a new {@link WeblogEntryPresenter} for this entry.
     * Provides clean access to all presentation/view concerns.
     */
    public WeblogEntryPresenter presenter() {
        return new WeblogEntryPresenter(this);
    }
    
    /**
     * Convenience method to get the plugins list.
     * @deprecated Use {@link #getPluginConfiguration()} for structured access.
     */
    @Deprecated
    public List<String> getPluginsList() {
        return getPluginConfiguration().getPluginNames();
    }

    /** Convenience method for checking status */
    public boolean isDraft() {
        return getStatus().equals(PubStatus.DRAFT);
    }

    /** Convenience method for checking status */
    public boolean isPending() {
        return getStatus().equals(PubStatus.PENDING);
    }

    /** Convenience method for checking status */
    public boolean isPublished() {
        return getStatus().equals(PubStatus.PUBLISHED);
    }

    /**
     * Get entry text, transformed by plugins enabled for entry.
     * @deprecated Presentation concern. Use {@link WeblogEntryPresenter#getTransformedText()}.
     */
    @Deprecated
    public String getTransformedText() {
        return new WeblogEntryPresenter(this).getTransformedText();
    }

    /**
     * Get entry summary, transformed by plugins enabled for entry.
     * @deprecated Presentation concern. Use {@link WeblogEntryPresenter#getTransformedSummary()}.
     */
    @Deprecated
    public String getTransformedSummary() {
        return new WeblogEntryPresenter(this).getTransformedSummary();
    }

    /**
     * Determine if the specified user has permissions to edit this entry.
     * @deprecated Couples to WebloggerFactory. Use {@link #hasWritePermissions(User, UserManager)}.
     */
    @Deprecated
    public boolean hasWritePermissions(User user) throws WebloggerException {
        // LEGACY ADAPTER
        return hasWritePermissions(user, WebloggerFactory.getWeblogger().getUserManager());
    }

    /**
     * Determine if the specified user has permissions to edit this entry.
     * Accepts UserManager as a parameter for testability (Dependency Injection).
     */
    public boolean hasWritePermissions(User user, UserManager umgr) throws WebloggerException {
        
        // global admins can hack whatever they want
        GlobalPermission adminPerm = 
            new GlobalPermission(List.of(GlobalPermission.ADMIN));
        boolean hasAdmin = umgr.checkPermission(adminPerm, user); 
        if (hasAdmin) {
            return true;
        }
        
        WeblogPermission perm;
        try {
            perm = umgr.getWeblogPermission(getWebsite(), user);
        } catch (WebloggerException ex) {
            mLogger.error("ERROR retrieving user's permission", ex);
            return false;
        }

        boolean author = perm.hasAction(WeblogPermission.POST) || perm.hasAction(WeblogPermission.ADMIN);
        boolean limited = !author && perm.hasAction(WeblogPermission.EDIT_DRAFT);
        
        return author || (limited && (status == PubStatus.DRAFT || status == PubStatus.PENDING));
    }
    
    /**
     * Get the right transformed display content depending on the situation.
     * @deprecated Presentation concern. Use {@link WeblogEntryPresenter#displayContent(String)}.
     */
    @Deprecated
    public String displayContent(String readMoreLink) {
        return new WeblogEntryPresenter(this).displayContent(readMoreLink);
    }
    
    
    /**
     * Get the right transformed display content.
     * @deprecated Presentation concern. Use {@link WeblogEntryPresenter#getDisplayContent()}.
     */
    @Deprecated
    public String getDisplayContent() { 
        return new WeblogEntryPresenter(this).getDisplayContent();
    }

    public Boolean getRefreshAggregates() {
        return refreshAggregates;
    }

    public void setRefreshAggregates(Boolean refreshAggregates) {
        this.refreshAggregates = refreshAggregates;
    }

}