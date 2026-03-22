package org.apache.roller.weblogger.ui.rendering.servlets;

import java.util.Date;

/**
 * Plain-text representation of a weblog entry for QA.
 */
public class WeblogQaEntryDocument {

    private final String id;
    private final String title;
    private final String summary;
    private final String content;
    private final String url;
    private final Date publishedAt;

    public WeblogQaEntryDocument(String id, String title, String summary,
            String content, String url, Date publishedAt) {
        this.id = id;
        this.title = title;
        this.summary = summary;
        this.content = content;
        this.url = url;
        this.publishedAt = publishedAt == null ? null : new Date(publishedAt.getTime());
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getSummary() {
        return summary;
    }

    public String getContent() {
        return content;
    }

    public String getUrl() {
        return url;
    }

    public Date getPublishedAt() {
        return publishedAt == null ? null : new Date(publishedAt.getTime());
    }

    public String getCombinedText() {
        StringBuilder builder = new StringBuilder();
        append(builder, title);
        append(builder, summary);
        append(builder, content);
        return builder.toString().trim();
    }

    private void append(StringBuilder builder, String value) {
        if (value == null || value.trim().isEmpty()) {
            return;
        }

        if (builder.length() > 0) {
            builder.append(' ');
        }
        builder.append(value.trim());
    }
}
