package org.apache.roller.weblogger.ui.rendering.servlets;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

/**
 * Shared text processing helpers for weblog QA.
 */
public final class WeblogQaTextSupport {

    private static final Pattern NON_ALPHANUMERIC = Pattern.compile("[^\\p{L}\\p{N}\\s]");
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");
    private static final Set<String> STOP_WORDS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            "a", "an", "and", "are", "as", "at", "be", "been", "blog", "by", "did", "do",
            "does", "for", "from", "has", "have", "how", "in", "into", "is", "it", "its",
            "last", "latest", "me", "of", "on", "or", "post", "posts", "say", "said", "that",
            "the", "their", "them", "this", "to", "topic", "was", "were", "what", "when",
            "where", "which", "who", "with", "x", "you", "your")));
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH);

    private WeblogQaTextSupport() {
    }

    public static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return WHITESPACE.matcher(value.replace('\n', ' ').replace('\r', ' ').trim()).replaceAll(" ");
    }

    public static List<String> tokenizeQuestion(String question) {
        if (StringUtils.isBlank(question)) {
            return Collections.emptyList();
        }

        String normalized = NON_ALPHANUMERIC.matcher(question.toLowerCase(Locale.ROOT)).replaceAll(" ");
        String[] rawTokens = WHITESPACE.matcher(normalized.trim()).replaceAll(" ").split(" ");
        LinkedHashSet<String> tokens = new LinkedHashSet<>();
        for (String token : rawTokens) {
            if (token.length() < 2 || STOP_WORDS.contains(token)) {
                continue;
            }
            tokens.add(token);
        }
        return new ArrayList<>(tokens);
    }

    public static boolean isTemporalQuestion(String question) {
        String normalized = normalize(question).toLowerCase(Locale.ROOT);
        return normalized.startsWith("when ")
                || normalized.contains(" last discussed")
                || normalized.contains(" latest ")
                || normalized.contains(" most recent ")
                || normalized.contains(" recently");
    }

    public static List<String> splitIntoPassages(String text) {
        String normalized = normalize(text);
        if (normalized.isEmpty()) {
            return Collections.emptyList();
        }

        String[] pieces = normalized.split("(?<=[.!?])\\s+");
        List<String> passages = new ArrayList<>();
        for (String piece : pieces) {
            String candidate = normalize(piece);
            if (candidate.length() >= 20) {
                passages.add(candidate);
            }
        }
        if (passages.isEmpty()) {
            passages.add(normalized);
        }
        return passages;
    }

    public static double scoreText(List<String> queryTokens, String question, String candidate, boolean titleBoost) {
        String normalizedCandidate = normalize(candidate).toLowerCase(Locale.ROOT);
        if (normalizedCandidate.isEmpty()) {
            return 0.0d;
        }

        double score = 0.0d;
        String normalizedQuestion = normalize(question).toLowerCase(Locale.ROOT);
        if (!normalizedQuestion.isEmpty() && normalizedCandidate.contains(normalizedQuestion)) {
            score += 8.0d;
        }

        int matchedTokens = 0;
        for (String token : queryTokens) {
            if (normalizedCandidate.contains(token)) {
                matchedTokens++;
                score += titleBoost ? 2.5d : 1.5d;
            }
        }

        if (!queryTokens.isEmpty()) {
            score += (matchedTokens * 4.0d) / queryTokens.size();
        }

        if (titleBoost) {
            score += 1.0d;
        }

        return score;
    }

    public static String buildSnippet(String text, int maxLength) {
        String normalized = normalize(text);
        if (normalized.length() <= maxLength) {
            return normalized;
        }
        return normalized.substring(0, Math.max(0, maxLength - 3)).trim() + "...";
    }

    public static String formatDate(Date date) {
        return date == null ? "an unknown date" : DATE_FORMAT.format(date);
    }

    public static <T extends RankedDocument> List<T> sortByScoreDescThenDate(List<T> items) {
        items.sort(Comparator
                .comparingDouble(RankedDocument::getScore).reversed()
                .thenComparing(RankedDocument::getPublishedAt, Comparator.nullsLast(Comparator.reverseOrder())));
        return items;
    }

    public interface RankedDocument {
        double getScore();
        Date getPublishedAt();
    }
}
