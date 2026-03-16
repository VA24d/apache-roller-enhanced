/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file for additional
 * information regarding copyright ownership.
 */
package org.apache.roller.weblogger.business.pulse;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.config.WebloggerConfig;

/**
 * Method 2: Hybrid — local TF-IDF clustering + LLM labeling.
 *
 * Clusters comments locally using TF-IDF (free), then sends only the
 * cluster keywords + 2 representative comments per cluster to an LLM
 * for polished human-readable theme labels and an overall recap.
 *
 * This minimizes API cost: only one LLM call with a small payload.
 * Falls back to pure TF-IDF labels if the LLM call fails.
 */
public class HybridLlmBreakdownStrategy implements BreakdownStrategy {

    private static final Log log = LogFactory.getLog(HybridLlmBreakdownStrategy.class);

    private final TfIdfBreakdownStrategy tfidfStrategy = new TfIdfBreakdownStrategy();

    @Override
    public String getName() { return "hybrid-llm"; }

    @Override
    public String getDescription() {
        return "Hybrid: local TF-IDF clustering + LLM-enhanced labeling and recap";
    }

    @Override
    public ConversationBreakdown analyze(CommentData data) {
        // Step 1: Run TF-IDF clustering locally (free)
        ConversationBreakdown localResult = tfidfStrategy.analyze(data);

        if (localResult.getThemes() == null || localResult.getThemes().isEmpty()) {
            return localResult;
        }

        // Step 2: Build a compact prompt with cluster summaries
        String prompt = buildPrompt(data, localResult);

        // Step 3: Call LLM for polished labels and recap
        try {
            String llmResponse = callLlm(prompt);
            if (llmResponse != null && !llmResponse.trim().isEmpty()) {
                return parseLlmResponse(llmResponse, localResult);
            }
        } catch (Exception e) {
            log.warn("LLM call failed, falling back to TF-IDF labels: " + e.getMessage());
        }

        // Fallback: return local TF-IDF result as-is
        localResult.setMethodUsed(getName() + " (fallback to tfidf)");
        return localResult;
    }

    private String buildPrompt(CommentData data, ConversationBreakdown localResult) {
        StringBuilder sb = new StringBuilder();
        sb.append("Analyze these discussion themes from blog comments on \"")
          .append(data.getEntryTitle()).append("\".\n\n");

        sb.append("I've clustered ").append(data.getCommentCount())
          .append(" comments into ").append(localResult.getThemes().size())
          .append(" groups. For each group, I provide keywords and sample comments.\n\n");

        for (int i = 0; i < localResult.getThemes().size(); i++) {
            ConversationTheme theme = localResult.getThemes().get(i);
            sb.append("Group ").append(i + 1).append(":\n");
            sb.append("  Keywords: ").append(String.join(", ", theme.getKeywords())).append("\n");
            sb.append("  Comments (").append(theme.getCommentCount()).append(" total):\n");
            for (String rep : theme.getRepresentativeComments()) {
                sb.append("    - \"").append(rep).append("\"\n");
            }
            sb.append("\n");
        }

        sb.append("Please respond with EXACTLY this format (no markdown, no extra text):\n");
        sb.append("THEME1: <clear theme label>\n");
        sb.append("THEME2: <clear theme label>\n");
        sb.append("...(one per group)\n");
        sb.append("RECAP: <2-3 sentence summary of the overall discussion>\n");

        return sb.toString();
    }

    private String callLlm(String prompt) throws Exception {
        String apiKey = WebloggerConfig.getProperty("pulse.llm.apiKey", "");
        String apiUrl = WebloggerConfig.getProperty("pulse.llm.apiUrl",
                "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent");

        if (apiKey.isEmpty()) {
            throw new IllegalStateException("No LLM API key configured (pulse.llm.apiKey)");
        }

        // Build request for Gemini API
        String requestBody = buildGeminiRequest(prompt);
        String fullUrl = apiUrl + "?key=" + apiKey;

        URL url = new URL(fullUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(30000);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(requestBody.getBytes(StandardCharsets.UTF_8));
        }

        int status = conn.getResponseCode();
        if (status != 200) {
            throw new RuntimeException("LLM API returned status " + status);
        }

        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
        }

        return extractTextFromGeminiResponse(response.toString());
    }

    private String buildGeminiRequest(String prompt) {
        // Escape JSON special chars in prompt
        String escaped = prompt.replace("\\", "\\\\")
                               .replace("\"", "\\\"")
                               .replace("\n", "\\n")
                               .replace("\r", "\\r")
                               .replace("\t", "\\t");

        return "{\"contents\":[{\"parts\":[{\"text\":\"" + escaped + "\"}]}],"
                + "\"generationConfig\":{\"temperature\":0.3,\"maxOutputTokens\":500}}";
    }

    private String extractTextFromGeminiResponse(String json) {
        // Simple extraction — find "text":"..." in the response
        int textIdx = json.indexOf("\"text\"");
        if (textIdx < 0) return null;

        int colonIdx = json.indexOf(":", textIdx);
        int startQuote = json.indexOf("\"", colonIdx + 1);
        if (startQuote < 0) return null;

        StringBuilder text = new StringBuilder();
        boolean escaped = false;
        for (int i = startQuote + 1; i < json.length(); i++) {
            char c = json.charAt(i);
            if (escaped) {
                if (c == 'n') text.append('\n');
                else if (c == 't') text.append('\t');
                else text.append(c);
                escaped = false;
            } else if (c == '\\') {
                escaped = true;
            } else if (c == '"') {
                break;
            } else {
                text.append(c);
            }
        }

        return text.toString();
    }

    private ConversationBreakdown parseLlmResponse(String llmResponse,
                                                     ConversationBreakdown localResult) {
        String[] lines = llmResponse.split("\n");
        List<String> themeLabels = new ArrayList<>();
        String recap = null;

        for (String line : lines) {
            line = line.trim();
            if (line.toUpperCase().startsWith("THEME") && line.contains(":")) {
                String label = line.substring(line.indexOf(":") + 1).trim();
                themeLabels.add(label);
            } else if (line.toUpperCase().startsWith("RECAP:")) {
                recap = line.substring(line.indexOf(":") + 1).trim();
            }
        }

        // Apply LLM labels to existing themes
        List<ConversationTheme> themes = localResult.getThemes();
        for (int i = 0; i < themes.size() && i < themeLabels.size(); i++) {
            themes.get(i).setLabel(themeLabels.get(i));
        }

        if (recap == null || recap.isEmpty()) {
            recap = localResult.getRecap();
        }

        return new ConversationBreakdown(themes, recap, getName());
    }
}
