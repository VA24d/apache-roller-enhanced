/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  The ASF licenses this file to You
 * under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.  For additional information regarding
 * copyright in this work, please see the NOTICE file in the top level
 * directory of this distribution.
 */

package org.apache.roller.weblogger.business.pipeline;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.apache.roller.weblogger.pojos.WeblogEntry;


/**
 * Pipeline step that generates a concise AI-powered summary of a blog entry
 * and stores it in {@link WeblogEntry#setSummary(String)}.
 *
 * <p>The original entry text is <b>never modified</b>. Roller's built-in
 * {@code displayContent(readMoreLink)} method automatically shows the summary
 * on list pages with a "Read More" link, and the full text on permalink pages.
 *
 * <p>Uses the Gemini API (same config as Community Pulse: {@code pulse.llm.apiKey}
 * and {@code pulse.llm.apiUrl}). Falls back to extractive summarization
 * (first 2-3 sentences) when the API is unavailable or fails.
 */
public class ContentSummarizerStep implements EntryProcessingStep {

    private static final Log LOG = LogFactory.getLog(ContentSummarizerStep.class);

    private static final String NAME = "ContentSummarizer";
    private static final String DESCRIPTION =
            "Generates an AI-powered summary stored in the entry summary field. "
                    + "Falls back to extractive summarization if the API is unavailable.";

    private static final int MAX_TEXT_FOR_API = 3000;


    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    @Override
    public void process(WeblogEntry entry) {
        String text = entry.getText();
        if (text == null || text.isBlank()) {
            return;
        }

        String plainText = stripHtml(text);

        // Short entries (< 2 sentences) don't need a separate summary
        if (countSentences(plainText) <= 2) {
            LOG.debug("Entry too short for summarization: " + entry.getTitle());
            return;
        }

        String summary = null;

        // Try AI summary first
        try {
            summary = callGeminiForSummary(plainText);
        } catch (Exception e) {
            LOG.warn("Gemini summarization failed for entry '"
                    + entry.getTitle() + "', using extractive fallback: " + e.getMessage());
        }

        // Fallback to extractive summary
        if (summary == null || summary.isBlank()) {
            summary = extractiveFallbackSummary(plainText);
        }

        if (summary != null && !summary.isBlank()) {
            entry.setSummary("<p>" + summary + "</p>");
            LOG.debug("Summary generated for entry: " + entry.getTitle());
        }
    }


    /**
     * Calls the Gemini API to generate a concise summary.
     * Reuses the same API key and URL as the Community Pulse feature.
     */
    String callGeminiForSummary(String plainText) throws Exception {
        String apiKey = WebloggerConfig.getProperty("pulse.llm.apiKey", "");
        String apiUrl = WebloggerConfig.getProperty("pulse.llm.apiUrl",
                "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent");

        if (apiKey.isEmpty()) {
            throw new IllegalStateException("No LLM API key configured (pulse.llm.apiKey)");
        }

        // Truncate very long text to keep API cost low
        String inputText = plainText.length() > MAX_TEXT_FOR_API
                ? plainText.substring(0, MAX_TEXT_FOR_API) + "..."
                : plainText;

        String prompt = "Summarize the following blog post in 2-3 concise sentences. "
                + "Return ONLY the summary text, no labels or prefixes:\n\n" + inputText;

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
            throw new RuntimeException("Gemini API returned status " + status);
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
        String escaped = prompt.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");

        return "{\"contents\":[{\"parts\":[{\"text\":\"" + escaped + "\"}]}],"
                + "\"generationConfig\":{\"temperature\":0.3,\"maxOutputTokens\":200}}";
    }

    private String extractTextFromGeminiResponse(String json) {
        int textIdx = json.indexOf("\"text\"");
        if (textIdx < 0) {
            return null;
        }

        int colonIdx = json.indexOf(":", textIdx);
        int startQuote = json.indexOf("\"", colonIdx + 1);
        if (startQuote < 0) {
            return null;
        }

        StringBuilder text = new StringBuilder();
        boolean escaped = false;
        for (int i = startQuote + 1; i < json.length(); i++) {
            char c = json.charAt(i);
            if (escaped) {
                if (c == 'n') {
                    text.append('\n');
                } else if (c == 't') {
                    text.append('\t');
                } else {
                    text.append(c);
                }
                escaped = false;
            } else if (c == '\\') {
                escaped = true;
            } else if (c == '"') {
                break;
            } else {
                text.append(c);
            }
        }

        return text.toString().trim();
    }


    /**
     * Extracts the first 2-3 sentences as a fallback summary.
     */
    static String extractiveFallbackSummary(String plainText) {
        if (plainText == null || plainText.isBlank()) {
            return null;
        }

        // Split on sentence boundaries: period, exclamation, or question mark
        // followed by whitespace or end of string
        String[] sentences = plainText.split("(?<=[.!?])\\s+");

        int count = Math.min(3, sentences.length);
        StringBuilder summary = new StringBuilder();
        for (int i = 0; i < count; i++) {
            if (i > 0) {
                summary.append(" ");
            }
            summary.append(sentences[i].trim());
        }

        return summary.toString();
    }


    /**
     * Strip HTML tags to get plain text for analysis.
     */
    static String stripHtml(String html) {
        return html.replaceAll("<[^>]+>", " ")
                .replaceAll("&nbsp;", " ")
                .replaceAll("&amp;", "&")
                .replaceAll("&lt;", "<")
                .replaceAll("&gt;", ">")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private static int countSentences(String text) {
        if (text == null || text.isBlank()) {
            return 0;
        }
        String[] sentences = text.split("(?<=[.!?])\\s+");
        return sentences.length;
    }
}
