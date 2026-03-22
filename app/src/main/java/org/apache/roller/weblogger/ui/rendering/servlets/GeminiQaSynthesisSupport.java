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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.config.WebloggerConfig;

/**
 * Shared Gemini synthesis support for weblog QA strategies.
 */
public final class GeminiQaSynthesisSupport {

    private GeminiQaSynthesisSupport() {
    }

    public static ContentGenerator defaultGenerator() {
        return new HttpGeminiContentGenerator();
    }

    public static WeblogQaAnswer buildGeminiAnswer(String strategy, String question,
            List<LongContextWeblogQaStrategy.RankedPassage> rankedPassages, int entryCount,
            boolean truncatedContext, int maxSources, ContentGenerator contentGenerator,
            String strategyReason) {

        Map<String, LongContextWeblogQaStrategy.RankedPassage> uniqueDocuments = new LinkedHashMap<>();
        for (LongContextWeblogQaStrategy.RankedPassage passage : rankedPassages) {
            uniqueDocuments.putIfAbsent(passage.getDocument().getId(), passage);
            if (uniqueDocuments.size() >= maxSources) {
                break;
            }
        }

        List<WeblogQaSource> sources = new ArrayList<>();
        for (LongContextWeblogQaStrategy.RankedPassage sourcePassage : uniqueDocuments.values()) {
            sources.add(new WeblogQaSource(
                    sourcePassage.getDocument().getTitle(),
                    sourcePassage.getDocument().getUrl(),
                    WeblogQaTextSupport.formatDate(sourcePassage.getDocument().getPublishedAt()),
                    WeblogQaTextSupport.buildSnippet(sourcePassage.getPassage(), 220),
                    sourcePassage.getScore()));
        }

        String answer = contentGenerator.generateAnswer(strategy, question, rankedPassages);
        return new WeblogQaAnswer(strategy, question, answer, sources,
                entryCount, rankedPassages.size(), truncatedContext, strategyReason);
    }

    interface ContentGenerator {
        String generateAnswer(String strategy, String question,
                List<LongContextWeblogQaStrategy.RankedPassage> rankedPassages);
    }

    static final class HttpGeminiContentGenerator implements ContentGenerator {

        private static final Log log = LogFactory.getLog(HttpGeminiContentGenerator.class);
        private static final Gson gson = new Gson();
        private static final Type MAP_TYPE = new TypeToken<Map<String, Object>>() { }.getType();
        private final String apiKey;
        private final String modelName;

        HttpGeminiContentGenerator() {
            this.apiKey = LocalApiPropertiesSupport.getProperty(
                    "qa.gemini.apiKey",
                    "qa.google.apiKey",
                    "google.apiKey");
            this.modelName = StringUtils.defaultIfBlank(
                    WebloggerConfig.getProperty("qa.gemini.model"),
                    "gemini-2.5-flash");
        }

        @Override
        public String generateAnswer(String strategy, String question,
                List<LongContextWeblogQaStrategy.RankedPassage> rankedPassages) {
            if (StringUtils.isBlank(apiKey)) {
                throw new IllegalStateException(
                        "Gemini API key is missing. Configure qa.gemini.apiKey or qa.google.apiKey in translation-api.properties.");
            }

            try {
                URL url = new URL("https://generativelanguage.googleapis.com/v1beta/models/"
                        + modelName + ":generateContent");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("x-goog-api-key", apiKey);
                connection.setRequestProperty("x-goog-api-client", "roller-weblog-qa/1.0");
                connection.setDoOutput(true);

                Map<String, Object> payload = Map.of(
                        "system_instruction", Map.of(
                                "parts", List.of(Map.of("text",
                                        "You answer questions about a Roller weblog. "
                                        + "Use only the provided weblog passages. "
                                        + "The strategy name describes how the context was prepared: "
                                        + "\"rag\" means the system first retrieved the most relevant entries, "
                                        + "\"long-context\" means the system scanned a much wider slice of the archive. "
                                        + "Do not mention implementation details unless they help compare answers. "
                                        + "If the context is insufficient, say so plainly. "
                                        + "Keep the answer concise and grounded."))),
                        "contents", List.of(Map.of(
                                "role", "user",
                                "parts", List.of(Map.of("text", buildPrompt(strategy, question, rankedPassages))))));

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
                    String extracted = extractText(response);
                    if (StringUtils.isNotBlank(extracted)) {
                        return extracted.trim();
                    }
                }
            } catch (Exception e) {
                log.error("Gemini QA generation failed", e);
                throw new IllegalStateException("Gemini QA generation failed: " + e.getMessage(), e);
            }

            throw new IllegalStateException("Gemini QA generation returned an empty response.");
        }

        private String buildPrompt(String strategy, String question,
                List<LongContextWeblogQaStrategy.RankedPassage> rankedPassages) {
            StringBuilder builder = new StringBuilder();
            builder.append("Strategy: ").append(strategy).append("\n");
            builder.append("Question: ").append(question).append("\n\n");
            builder.append("Context passages:\n");

            int index = 1;
            for (LongContextWeblogQaStrategy.RankedPassage passage : rankedPassages) {
                builder.append(index++)
                        .append(". Title: ")
                        .append(StringUtils.defaultIfBlank(passage.getDocument().getTitle(), "Untitled"))
                        .append("\n   Published: ")
                        .append(WeblogQaTextSupport.formatDate(passage.getDocument().getPublishedAt()))
                        .append("\n   URL: ")
                        .append(StringUtils.defaultIfBlank(passage.getDocument().getUrl(), "N/A"))
                        .append("\n   Passage: ")
                        .append(passage.getPassage())
                        .append("\n");
            }

            builder.append("\nAnswer using only this context.");
            return builder.toString();
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
    }
}
