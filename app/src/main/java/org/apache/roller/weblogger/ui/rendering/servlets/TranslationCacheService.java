package org.apache.roller.weblogger.ui.rendering.servlets;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;

/**
 * Shared section-level cache for translated weblog content.
 *
 * Significant content changes are defined as changes to a section's normalized
 * primary text content. Normalization trims leading/trailing whitespace and
 * collapses repeated whitespace so cosmetic spacing changes do not invalidate
 * the cache, while heading/body text edits do.
 */
public class TranslationCacheService {

    private static final Pattern WHITESPACE = Pattern.compile("\\s+");

    private final ConcurrentMap<String, CachedSectionTranslation> sectionCache = new ConcurrentHashMap<>();

    public List<TranslationSectionResponse> translateSections(String providerName,
            TranslationProvider provider, String sourceLang, String targetLang,
            List<TranslationSectionRequest> sectionRequests) throws Exception {

        if (sectionRequests == null || sectionRequests.isEmpty()) {
            return Collections.emptyList();
        }

        List<TranslationSectionResponse> responses =
                new ArrayList<>(Collections.nCopies(sectionRequests.size(), null));
        Map<String, PendingSection> pendingSections = new LinkedHashMap<>();

        for (int index = 0; index < sectionRequests.size(); index++) {
            TranslationSectionRequest request = sectionRequests.get(index);
            List<String> sourceTexts = sanitizeTexts(request.getTexts());
            String contentHash = computeContentHash(sourceTexts);
            String cacheKey = buildCacheKey(providerName, sourceLang, targetLang, contentHash);

            CachedSectionTranslation cachedSection = sectionCache.get(cacheKey);
            if (cachedSection != null) {
                responses.set(index, new TranslationSectionResponse(request.getSectionId(),
                        cachedSection.contentHash, cachedSection.translations, true));
                continue;
            }

            PendingSection pendingSection = pendingSections.get(cacheKey);
            if (pendingSection == null) {
                pendingSection = new PendingSection(cacheKey, contentHash, sourceTexts);
                pendingSections.put(cacheKey, pendingSection);
            }
            pendingSection.addConsumer(index, request.getSectionId());
        }

        if (!pendingSections.isEmpty()) {
            List<String> textsToTranslate = new ArrayList<>();
            for (PendingSection pendingSection : pendingSections.values()) {
                textsToTranslate.addAll(pendingSection.sourceTexts);
            }

            List<String> translatedTexts = provider.translate(textsToTranslate, sourceLang, targetLang);
            if (translatedTexts.size() != textsToTranslate.size()) {
                throw new IllegalStateException("Translated text count did not match request count");
            }

            int offset = 0;
            for (PendingSection pendingSection : pendingSections.values()) {
                int sectionSize = pendingSection.sourceTexts.size();
                List<String> sectionTranslations = new ArrayList<>(
                        translatedTexts.subList(offset, offset + sectionSize));
                offset += sectionSize;

                sectionCache.put(pendingSection.cacheKey,
                        new CachedSectionTranslation(pendingSection.contentHash, sectionTranslations));

                for (PendingConsumer consumer : pendingSection.consumers) {
                    responses.set(consumer.responseIndex,
                            new TranslationSectionResponse(consumer.sectionId,
                                    pendingSection.contentHash, sectionTranslations, false));
                }
            }
        }

        return responses;
    }

    static String computeContentHash(List<String> texts) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            for (String text : sanitizeTexts(texts)) {
                digest.update(normalizeText(text).getBytes(StandardCharsets.UTF_8));
                digest.update((byte) '\n');
            }
            return toHex(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }

    void clear() {
        sectionCache.clear();
    }

    private static List<String> sanitizeTexts(List<String> texts) {
        if (texts == null || texts.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> sanitized = new ArrayList<>(texts.size());
        for (String text : texts) {
            sanitized.add(text == null ? "" : text);
        }
        return sanitized;
    }

    private static String normalizeText(String text) {
        return WHITESPACE.matcher(text.trim()).replaceAll(" ");
    }

    private static String buildCacheKey(String providerName, String sourceLang,
            String targetLang, String contentHash) {
        return String.format(Locale.ENGLISH, "%s|%s|%s|%s",
                providerName, sourceLang, targetLang, contentHash);
    }

    private static String toHex(byte[] bytes) {
        StringBuilder builder = new StringBuilder(bytes.length * 2);
        for (byte value : bytes) {
            builder.append(String.format(Locale.ENGLISH, "%02x", value));
        }
        return builder.toString();
    }

    private static final class CachedSectionTranslation {
        private final String contentHash;
        private final List<String> translations;

        private CachedSectionTranslation(String contentHash, List<String> translations) {
            this.contentHash = contentHash;
            this.translations = Collections.unmodifiableList(new ArrayList<>(translations));
        }
    }

    private static final class PendingSection {
        private final String cacheKey;
        private final String contentHash;
        private final List<String> sourceTexts;
        private final List<PendingConsumer> consumers = new ArrayList<>();

        private PendingSection(String cacheKey, String contentHash, List<String> sourceTexts) {
            this.cacheKey = cacheKey;
            this.contentHash = contentHash;
            this.sourceTexts = sourceTexts;
        }

        private void addConsumer(int responseIndex, String sectionId) {
            consumers.add(new PendingConsumer(responseIndex, sectionId));
        }
    }

    private static final class PendingConsumer {
        private final int responseIndex;
        private final String sectionId;

        private PendingConsumer(int responseIndex, String sectionId) {
            this.responseIndex = responseIndex;
            this.sectionId = sectionId;
        }
    }
}
