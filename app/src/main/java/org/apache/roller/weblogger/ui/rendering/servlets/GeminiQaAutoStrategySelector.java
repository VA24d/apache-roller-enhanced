package org.apache.roller.weblogger.ui.rendering.servlets;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.config.WebloggerConfig;

/**
 * Uses Gemini to choose between RAG and Long Context for auto mode.
 */
public class GeminiQaAutoStrategySelector implements WeblogQaService.AutoStrategyResolver {

    private static final Log log = LogFactory.getLog(GeminiQaAutoStrategySelector.class);
    private static final Gson gson = new Gson();
    private static final Type MAP_TYPE = new TypeToken<Map<String, Object>>() { }.getType();

    private final String apiKey;
    private final String modelName;

    public GeminiQaAutoStrategySelector() {
        this.apiKey = LocalApiPropertiesSupport.getProperty(
                "qa.gemini.apiKey",
                "qa.google.apiKey",
                "google.apiKey");
        this.modelName = StringUtils.defaultIfBlank(
                WebloggerConfig.getProperty("qa.gemini.model"),
                "gemini-2.5-flash");
    }

    @Override
    public WeblogQaService.StrategyDecision select(String question) {
        if (StringUtils.isBlank(apiKey)) {
            return fallbackDecision(question, "Gemini auto-pick was unavailable because the API key is missing, so a local fallback rule was used.");
        }

        try {
            URL url = new URL("https://generativelanguage.googleapis.com/v1beta/models/"
                    + modelName + ":generateContent");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("x-goog-api-key", apiKey);
            connection.setRequestProperty("x-goog-api-client", "roller-weblog-qa-auto/1.0");
            connection.setDoOutput(true);

            Map<String, Object> payload = Map.of(
                    "system_instruction", Map.of(
                            "parts", List.of(Map.of("text",
                                    "You choose the best weblog QA strategy. "
                                    + "Return valid JSON only with keys strategy and reason. "
                                    + "strategy must be exactly 'rag' or 'long-context'. "
                                    + "Choose 'rag' for focused questions that can be answered from a small set of relevant posts. "
                                    + "Choose 'long-context' for broad summaries, history, change-over-time, cross-post comparisons, or whole-blog questions."))),
                    "contents", List.of(Map.of(
                            "role", "user",
                            "parts", List.of(Map.of("text",
                                    "Question: " + question + "\n"
                                            + "Return JSON like {\"strategy\":\"rag\",\"reason\":\"...\"}")))));

            try (OutputStream outputStream = connection.getOutputStream()) {
                outputStream.write(gson.toJson(payload).getBytes(StandardCharsets.UTF_8));
            }

            int responseCode = connection.getResponseCode();
            if (responseCode >= 400) {
                throw new IllegalStateException("Gemini API error (" + responseCode + "): "
                        + readStream(connection.getErrorStream()));
            }

            try (InputStreamReader reader =
                         new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8)) {
                Map<String, Object> response = gson.fromJson(reader, MAP_TYPE);
                String text = extractText(response);
                if (StringUtils.isBlank(text)) {
                    throw new IllegalStateException("Gemini auto-pick returned an empty response.");
                }

                StrategyResponse parsed = parseDecision(text);
                if (parsed != null && ("rag".equals(parsed.strategy) || "long-context".equals(parsed.strategy))) {
                    String reason = StringUtils.defaultIfBlank(parsed.reason,
                            "Gemini auto-picked " + humanLabel(parsed.strategy) + " for this question.");
                    return new WeblogQaService.StrategyDecision(parsed.strategy, reason);
                }
                throw new IllegalStateException("Gemini auto-pick returned invalid strategy payload: " + text);
            }
        } catch (Exception e) {
            log.warn("Gemini auto-pick failed, falling back to local heuristic", e);
            return fallbackDecision(question, "Gemini auto-pick failed, so a local fallback rule was used.");
        }
    }

    private WeblogQaService.StrategyDecision fallbackDecision(String question, String prefix) {
        boolean useLongContext = shouldUseLongContext(question);
        String strategy = useLongContext ? "long-context" : "rag";
        String reason = prefix + " It chose " + humanLabel(strategy)
                + (useLongContext
                ? " because the question looks broad, historical, comparative, or whole-blog."
                : " because the question looks focused enough to answer from a small set of relevant entries.");
        return new WeblogQaService.StrategyDecision(strategy, reason);
    }

    private boolean shouldUseLongContext(String question) {
        String normalized = StringUtils.defaultString(question).toLowerCase(Locale.ROOT);
        return WeblogQaTextSupport.isTemporalQuestion(question)
                || normalized.contains("over time")
                || normalized.contains("how has")
                || normalized.contains("history")
                || normalized.contains("evolution")
                || normalized.contains("compare")
                || normalized.contains("across posts")
                || normalized.contains("across the blog")
                || normalized.contains("main themes")
                || normalized.contains("themes")
                || normalized.contains("first discussed")
                || normalized.contains("last discussed")
                || normalized.contains("timeline")
                || normalized.contains("summarize this blog")
                || normalized.contains("summarize all")
                || normalized.contains("all entries")
                || normalized.contains("all posts")
                || normalized.contains("entire blog")
                || normalized.contains("whole blog")
                || normalized.contains("everything on this blog")
                || normalized.contains("overall summary");
    }

    @SuppressWarnings("unchecked")
    private String extractText(Map<String, Object> response) {
        if (response == null) {
            return null;
        }
        Object candidatesObject = response.get("candidates");
        if (!(candidatesObject instanceof List)) {
            return null;
        }
        for (Object candidateObject : (List<Object>) candidatesObject) {
            if (!(candidateObject instanceof Map)) {
                continue;
            }
            Object contentObject = ((Map<String, Object>) candidateObject).get("content");
            if (!(contentObject instanceof Map)) {
                continue;
            }
            Object partsObject = ((Map<String, Object>) contentObject).get("parts");
            if (!(partsObject instanceof List)) {
                continue;
            }
            StringBuilder builder = new StringBuilder();
            for (Object partObject : (List<Object>) partsObject) {
                if (partObject instanceof Map) {
                    Object textObject = ((Map<String, Object>) partObject).get("text");
                    if (textObject instanceof String && StringUtils.isNotBlank((String) textObject)) {
                        if (builder.length() > 0) {
                            builder.append('\n');
                        }
                        builder.append(((String) textObject).trim());
                    }
                }
            }
            if (builder.length() > 0) {
                return builder.toString();
            }
        }
        return null;
    }

    private StrategyResponse parseDecision(String text) {
        String trimmed = text.trim();
        int start = trimmed.indexOf('{');
        int end = trimmed.lastIndexOf('}');
        if (start >= 0 && end > start) {
            trimmed = trimmed.substring(start, end + 1);
        }
        try {
            return gson.fromJson(trimmed, StrategyResponse.class);
        } catch (Exception e) {
            return null;
        }
    }

    private String readStream(InputStream inputStream) throws Exception {
        if (inputStream == null) {
            return "";
        }
        try (InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
            StringBuilder builder = new StringBuilder();
            int value;
            while ((value = reader.read()) != -1) {
                builder.append((char) value);
            }
            return builder.toString();
        }
    }

    private String humanLabel(String strategy) {
        return "long-context".equals(strategy) ? "Long Context" : "RAG";
    }

    private static final class StrategyResponse {
        private String strategy;
        private String reason;
    }
}
