package org.apache.roller.weblogger.ui.rendering.servlets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class TranslationLanguageSupportTest {

    @Test
    void normalizesSupportedLanguageCodes() {
        assertEquals("en", TranslationLanguageSupport.normalizeLanguageCode("en-US"));
        assertEquals("mr", TranslationLanguageSupport.normalizeLanguageCode("mr_IN"));
        assertEquals("auto", TranslationLanguageSupport.normalizeLanguageCode("auto"));
        assertNull(TranslationLanguageSupport.normalizeLanguageCode("fr"));
    }

    @Test
    void mapsSarvamLanguageCodesIncludingMarathi() {
        assertEquals("en-IN", TranslationLanguageSupport.mapSarvamLanguageCode("en"));
        assertEquals("mr-IN", TranslationLanguageSupport.mapSarvamLanguageCode("mr"));
        assertEquals("hi-IN", TranslationLanguageSupport.mapSarvamLanguageCode("hi"));
        assertTrue(TranslationLanguageSupport.getSupportedLanguages().contains("mr"));
    }
}
