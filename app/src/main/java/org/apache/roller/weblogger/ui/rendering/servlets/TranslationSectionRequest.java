package org.apache.roller.weblogger.ui.rendering.servlets;

import java.util.Collections;
import java.util.List;

/**
 * Payload for a translatable primary-content section.
 */
public class TranslationSectionRequest {

    private String sectionId;
    private List<String> texts;

    public String getSectionId() {
        return sectionId;
    }

    public void setSectionId(String sectionId) {
        this.sectionId = sectionId;
    }

    public List<String> getTexts() {
        return texts == null ? Collections.emptyList() : texts;
    }

    public void setTexts(List<String> texts) {
        this.texts = texts;
    }
}
