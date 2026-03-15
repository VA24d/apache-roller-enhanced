package org.apache.roller.weblogger.ui.rendering.servlets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Response for a translated primary-content section.
 */
public class TranslationSectionResponse {

    private final String sectionId;
    private final String contentHash;
    private final List<String> translations;
    private final boolean cached;

    public TranslationSectionResponse(String sectionId, String contentHash, List<String> translations, boolean cached) {
        this.sectionId = sectionId;
        this.contentHash = contentHash;
        this.translations = Collections.unmodifiableList(new ArrayList<>(translations));
        this.cached = cached;
    }

    public String getSectionId() {
        return sectionId;
    }

    public String getContentHash() {
        return contentHash;
    }

    public List<String> getTranslations() {
        return translations;
    }

    public boolean isCached() {
        return cached;
    }
}
