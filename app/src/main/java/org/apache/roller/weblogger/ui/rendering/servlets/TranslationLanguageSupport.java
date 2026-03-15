package org.apache.roller.weblogger.ui.rendering.servlets;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Shared helpers for supported translation languages.
 */
public final class TranslationLanguageSupport {

    private static final Set<String> SUPPORTED_LANGUAGES = Collections.unmodifiableSet(
            new LinkedHashSet<>(Arrays.asList("en", "hi", "bn", "ta", "te", "kn", "mr")));

    private TranslationLanguageSupport() {
    }

    public static Set<String> getSupportedLanguages() {
        return SUPPORTED_LANGUAGES;
    }

    public static String normalizeLanguageCode(String languageCode) {
        return normalizeLanguageCode(languageCode, null);
    }

    public static String normalizeLanguageCode(String languageCode, String defaultValue) {
        if (languageCode == null || languageCode.trim().isEmpty()) {
            return defaultValue;
        }

        String normalized = languageCode.trim()
                .replace('_', '-')
                .toLowerCase(Locale.ENGLISH);

        if ("auto".equals(normalized)) {
            return normalized;
        }

        int separator = normalized.indexOf('-');
        if (separator > 0) {
            normalized = normalized.substring(0, separator);
        }

        return SUPPORTED_LANGUAGES.contains(normalized) ? normalized : defaultValue;
    }

    public static String mapSarvamLanguageCode(String languageCode) {
        String normalized = normalizeLanguageCode(languageCode, "en");
        switch (normalized) {
            case "hi":
                return "hi-IN";
            case "bn":
                return "bn-IN";
            case "ta":
                return "ta-IN";
            case "te":
                return "te-IN";
            case "kn":
                return "kn-IN";
            case "mr":
                return "mr-IN";
            case "en":
            default:
                return "en-IN";
        }
    }
}
