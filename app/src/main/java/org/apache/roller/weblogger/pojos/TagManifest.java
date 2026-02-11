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
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.util.Utilities;

/**
 * Encapsulates the management of tags for a WeblogEntry.
 * Tracks the current tag set as well as added/removed tags for
 * persistence synchronization.
 *
 * <p>This replaces the scattered tag management logic (tagSet, addedTags,
 * removedTags) previously embedded directly in WeblogEntry, applying the
 * "Replace Primitive with Object" refactoring to improve cohesion.</p>
 */
public class TagManifest implements Serializable {

    private static final long serialVersionUID = 1L;

    private Set<WeblogEntryTag> tags = new HashSet<>();
    private Set<WeblogEntryTag> removedTags = new HashSet<>();
    private Set<WeblogEntryTag> addedTags = new HashSet<>();

    public TagManifest() {
    }

    /**
     * Returns an unmodifiable view of the current tag set.
     */
    public Set<WeblogEntryTag> getTags() {
        return Collections.unmodifiableSet(tags);
    }

    /**
     * Replaces the entire tag set. Resets added/removed tracking.
     */
    public void setTags(Set<WeblogEntryTag> tagSet) {
        this.tags = (tagSet != null) ? tagSet : new HashSet<>();
        this.removedTags = new HashSet<>();
        this.addedTags = new HashSet<>();
    }

    /**
     * Returns an unmodifiable view of the tags added since the last persistence sync.
     */
    public Set<WeblogEntryTag> getAddedTags() {
        return Collections.unmodifiableSet(addedTags);
    }

    /**
     * Returns an unmodifiable view of the tags removed since the last persistence sync.
     */
    public Set<WeblogEntryTag> getRemovedTags() {
        return Collections.unmodifiableSet(removedTags);
    }

    /**
     * Add a tag by name. The tag is normalized to lowercase per the given locale.
     * Duplicate tags are silently ignored.
     *
     * @param name the tag name
     * @param creatorUserName the username of the tag creator
     * @param weblog the weblog this tag belongs to
     * @param entry the entry this tag belongs to
     * @param locale the locale for normalization
     * @param updateTime the timestamp to set on the tag
     */
    public void addTag(String name, String creatorUserName, Weblog weblog,
                       WeblogEntry entry, Locale locale, java.sql.Timestamp updateTime)
            throws WebloggerException {

        name = Utilities.normalizeTag(name, locale);
        if (name.isEmpty()) {
            return;
        }

        for (WeblogEntryTag tag : tags) {
            if (tag.getName().equals(name)) {
                return; // already exists
            }
        }

        WeblogEntryTag tag = new WeblogEntryTag();
        tag.setName(name);
        tag.setCreatorUserName(creatorUserName);
        tag.setWeblog(weblog);
        tag.setWeblogEntry(entry);
        tag.setTime(updateTime);
        tags.add(tag);
        addedTags.add(tag);
    }

    /**
     * Parse a space-separated tag string and synchronize the tag set.
     * Tags removed from the string are tracked for persistence deletion.
     * New tags are tracked for persistence insertion.
     */
    public void setTagsFromString(String tagsString, String creatorUserName,
                                   Weblog weblog, WeblogEntry entry, Locale locale,
                                   java.sql.Timestamp updateTime)
            throws WebloggerException {

        if (StringUtils.isEmpty(tagsString)) {
            removedTags.addAll(tags);
            tags.clear();
            return;
        }

        List<String> updatedTags = Utilities.splitStringAsTags(tagsString);
        Set<String> newTagNames = new HashSet<>(updatedTags.size());

        for (String name : updatedTags) {
            newTagNames.add(Utilities.normalizeTag(name, locale));
        }

        // Remove tags no longer present
        for (Iterator<WeblogEntryTag> it = tags.iterator(); it.hasNext(); ) {
            WeblogEntryTag tag = it.next();
            if (!newTagNames.contains(tag.getName())) {
                removedTags.add(tag);
                it.remove();
            } else {
                newTagNames.remove(tag.getName());
            }
        }

        // Add new tags
        for (String newTag : newTagNames) {
            addTag(newTag, creatorUserName, weblog, entry, locale, updateTime);
        }
    }

    /**
     * Returns a space-separated string of all tag names, sorted alphabetically.
     */
    public String toSortedString() {
        StringBuilder sb = new StringBuilder();
        Set<WeblogEntryTag> sorted = new TreeSet<>(new WeblogEntryTagComparator());
        sorted.addAll(tags);
        for (WeblogEntryTag entryTag : sorted) {
            sb.append(entryTag.getName()).append(" ");
        }
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    /**
     * Provides mutable access to the underlying tag set for JPA/ORM.
     * Should NOT be used by application code; use {@link #getTags()} instead.
     */
    Set<WeblogEntryTag> getMutableTags() {
        return tags;
    }
}
