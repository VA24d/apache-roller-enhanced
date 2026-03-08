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

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.roller.util.RollerConstants;
import org.apache.roller.weblogger.business.plugins.entry.WeblogEntryPlugin;
import org.apache.roller.weblogger.util.HTMLSanitizer;
import org.apache.roller.weblogger.util.I18nMessages;
import org.apache.roller.weblogger.util.Utilities;

/**
 * Handles all presentation/view concerns for a {@link WeblogEntry}.
 *
 * <p>Extracted from WeblogEntry to uphold the Single Responsibility Principle.
 * A domain entity should know <em>what</em> it is (data + business rules),
 * not <em>how</em> to render itself as HTML, RSS, or formatted text.</p>
 *
 * <p>Usage:
 * <pre>
 *     WeblogEntryPresenter presenter = new WeblogEntryPresenter(entry);
 *     String html = presenter.getDisplayContent();
 *     String rss  = presenter.getRss09xDescription(255);
 *     String time = presenter.formatPubTime("yyyy-MM-dd");
 * </pre>
 * </p>
 */
public class WeblogEntryPresenter {

    private static final Log LOG = LogFactory.getFactory().getInstance(WeblogEntryPresenter.class);

    private final WeblogEntry entry;

    /**
     * Create a presenter for the given entry.
     * @param entry the WeblogEntry to present; must not be null
     */
    public WeblogEntryPresenter(WeblogEntry entry) {
        if (entry == null) {
            throw new IllegalArgumentException("WeblogEntry must not be null");
        }
        this.entry = entry;
    }

    /**
     * @return the underlying entry
     */
    public WeblogEntry getEntry() {
        return entry;
    }

    // -----------------------------------------------------------------------
    // Formatting
    // -----------------------------------------------------------------------

    /**
     * Format the publish time using the specified pattern and the weblog's locale.
     * @see java.text.SimpleDateFormat
     */
    public String formatPubTime(String pattern) {
        try {
            SimpleDateFormat format = new SimpleDateFormat(pattern,
                    entry.getWebsite().getLocaleInstance());
            return format.format(entry.getPubTime());
        } catch (RuntimeException e) {
            LOG.error("Unexpected exception", e);
        }
        return "ERROR: formatting date";
    }

    /**
     * Format the update time using the specified pattern.
     * @see java.text.SimpleDateFormat
     */
    public String formatUpdateTime(String pattern) {
        try {
            SimpleDateFormat format = new SimpleDateFormat(pattern);
            return format.format(entry.getUpdateTime());
        } catch (RuntimeException e) {
            LOG.error("Unexpected exception", e);
        }
        return "ERROR: formatting date";
    }

    // -----------------------------------------------------------------------
    // Display title
    // -----------------------------------------------------------------------

    /**
     * Return the title of this entry, or the first 255 characters of the
     * entry's text if the title is blank.
     */
    public String getDisplayTitle() {
        if (entry.getTitle() == null || entry.getTitle().isBlank()) {
            return StringUtils.left(Utilities.removeHTML(entry.getText()), RollerConstants.TEXTWIDTH_255);
        }
        return Utilities.removeHTML(entry.getTitle());
    }

    // -----------------------------------------------------------------------
    // RSS descriptions
    // -----------------------------------------------------------------------

    /**
     * Return RSS 0.9x style description (escaped HTML version of entry text).
     */
    public String getRss09xDescription() {
        return getRss09xDescription(-1);
    }

    /**
     * Return RSS 0.9x style description, truncated to maxLength if specified.
     */
    public String getRss09xDescription(int maxLength) {
        String ret = StringEscapeUtils.escapeHtml3(entry.getText());
        if (maxLength != -1 && ret.length() > maxLength) {
            ret = ret.substring(0, maxLength - 3) + "...";
        }
        return ret;
    }

    // -----------------------------------------------------------------------
    // Transformed / rendered content
    // -----------------------------------------------------------------------

    /**
     * Get entry text, transformed by plugins enabled for this entry.
     */
    public String getTransformedText() {
        return render(entry.getText());
    }

    /**
     * Get entry summary, transformed by plugins enabled for this entry.
     */
    public String getTransformedSummary() {
        return render(entry.getSummary());
    }

    /**
     * Get the right transformed display content depending on the situation.
     *
     * <p>If readMoreLink is specified, prefer summary over content and append a
     * "Read More" link. Otherwise, prefer content over summary.</p>
     */
    public String displayContent(String readMoreLink) {

        String displayContent;

        if (readMoreLink == null || readMoreLink.isBlank() || "nil".equals(readMoreLink)) {
            // Permalink view: prefer text over summary
            if (StringUtils.isNotEmpty(entry.getText())) {
                displayContent = getTransformedText();
            } else {
                displayContent = getTransformedSummary();
            }
        } else {
            // List view: prefer summary over text, include "read more" if needed
            if (StringUtils.isNotEmpty(entry.getSummary())) {
                displayContent = getTransformedSummary();
                if (StringUtils.isNotEmpty(entry.getText())) {
                    List<String> args = List.of(readMoreLink);
                    String readMore = I18nMessages.getMessages(
                            entry.getWebsite().getLocaleInstance())
                            .getString("macro.weblog.readMoreLink", args);
                    displayContent += readMore;
                }
            } else {
                displayContent = getTransformedText();
            }
        }

        return HTMLSanitizer.conditionallySanitize(displayContent);
    }

    /**
     * Get the default transformed display content (permalink view).
     */
    public String getDisplayContent() {
        return displayContent(null);
    }

    // -----------------------------------------------------------------------
    // Internal rendering
    // -----------------------------------------------------------------------

    /**
     * Transform string based on plugins enabled for this weblog entry.
     */
    private String render(String str) {
        String ret = str;
        LOG.debug("Applying page plugins to string");

        Map<String, WeblogEntryPlugin> inPlugins = entry.getWebsite().getInitializedPlugins();
        if (str != null && inPlugins != null) {
            PluginConfiguration pluginConfig = entry.getPluginConfiguration();

            if (!pluginConfig.isEmpty()) {
                for (Map.Entry<String, WeblogEntryPlugin> pluginEntry : inPlugins.entrySet()) {
                    if (pluginConfig.containsPlugin(pluginEntry.getKey())) {
                        WeblogEntryPlugin pagePlugin = pluginEntry.getValue();
                        try {
                            ret = pagePlugin.render(entry, ret);
                        } catch (Exception e) {
                            LOG.error("ERROR from plugin: " + pagePlugin.getName(), e);
                        }
                    }
                }
            }
        }
        return HTMLSanitizer.conditionallySanitize(ret);
    }
}
