package org.apache.roller.weblogger.ui.rendering.servlets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TranslationCacheServiceTest {

    private TranslationCacheService cacheService;
    private CountingTranslationProvider provider;

    @BeforeEach
    void setUp() {
        cacheService = new TranslationCacheService();
        provider = new CountingTranslationProvider();
    }

    @Test
    void reusesCachedSectionsWhenOnlyWhitespaceChanges() throws Exception {
        List<TranslationSectionResponse> initialResponse = cacheService.translateSections(
                "mymemory", provider, "en", "mr",
                List.of(section("heading", "Hello world")));

        List<TranslationSectionResponse> repeatResponse = cacheService.translateSections(
                "mymemory", provider, "en", "mr",
                List.of(section("heading", "   Hello   world   ")));

        assertEquals(1, provider.invocationCount);
        assertEquals(1, provider.translatedChunkCount);
        assertFalse(initialResponse.get(0).isCached());
        assertTrue(repeatResponse.get(0).isCached());
        assertEquals(initialResponse.get(0).getContentHash(), repeatResponse.get(0).getContentHash());
    }

    @Test
    void retranslatesOnlyChangedSections() throws Exception {
        List<TranslationSectionRequest> initialSections = List.of(
                section("heading", "Hello world"),
                section("body", "Paragraph one", "Paragraph two"));

        List<TranslationSectionResponse> initialResponse = cacheService.translateSections(
                "sarvam", provider, "en", "hi", initialSections);

        List<TranslationSectionRequest> updatedSections = List.of(
                section("heading", "Hello world"),
                section("body", "Paragraph one updated", "Paragraph two"));

        List<TranslationSectionResponse> updatedResponse = cacheService.translateSections(
                "sarvam", provider, "en", "hi", updatedSections);

        assertEquals(2, provider.invocationCount);
        assertEquals(5, provider.translatedChunkCount);
        assertFalse(initialResponse.get(0).isCached());
        assertFalse(initialResponse.get(1).isCached());
        assertTrue(updatedResponse.get(0).isCached());
        assertFalse(updatedResponse.get(1).isCached());
        assertEquals("hi:HELLO WORLD", updatedResponse.get(0).getTranslations().get(0));
        assertEquals("hi:PARAGRAPH ONE UPDATED", updatedResponse.get(1).getTranslations().get(0));
    }

    private TranslationSectionRequest section(String sectionId, String... texts) {
        TranslationSectionRequest request = new TranslationSectionRequest();
        request.setSectionId(sectionId);
        request.setTexts(List.of(texts));
        return request;
    }

    private static final class CountingTranslationProvider implements TranslationProvider {
        private int invocationCount;
        private int translatedChunkCount;

        @Override
        public List<String> translate(List<String> texts, String sourceLang, String targetLang) {
            invocationCount++;
            translatedChunkCount += texts.size();
            return texts.stream()
                    .map(text -> targetLang + ":" + text.toUpperCase(Locale.ENGLISH).trim())
                    .collect(Collectors.toList());
        }
    }
}
