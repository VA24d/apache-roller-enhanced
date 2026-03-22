package org.apache.roller.weblogger.ui.rendering.servlets;

/**
 * Source citation for a chatbot answer.
 */
public class WeblogQaSource {

    private final String title;
    private final String url;
    private final String publishedAt;
    private final String excerpt;
    private final double score;

    public WeblogQaSource(String title, String url, String publishedAt, String excerpt, double score) {
        this.title = title;
        this.url = url;
        this.publishedAt = publishedAt;
        this.excerpt = excerpt;
        this.score = score;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }

    public String getPublishedAt() {
        return publishedAt;
    }

    public String getExcerpt() {
        return excerpt;
    }

    public double getScore() {
        return score;
    }
}
