/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  The ASF licenses this file to You
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

package org.apache.roller.weblogger.ui.rendering.servlets;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.config.WebloggerConfig;

/**
 * Handles translation requests from the frontend widget.
 */
public class TranslationServlet extends HttpServlet {

    private static final Log log = LogFactory.getLog(TranslationServlet.class);
    private static final Gson gson = new Gson();
    private static final TranslationCacheService CACHE_SERVICE = new TranslationCacheService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try (PrintWriter out = response.getWriter()) {
            TranslationRequestPayload payload = gson.fromJson(
                    new InputStreamReader(request.getInputStream(), StandardCharsets.UTF_8),
                    TranslationRequestPayload.class);

            if (payload == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"Invalid JSON payload\"}");
                return;
            }

            String sourceLang = TranslationLanguageSupport.normalizeLanguageCode(payload.sourceLang, "en");
            String targetLang = TranslationLanguageSupport.normalizeLanguageCode(payload.targetLang);
            String providerName = payload.provider == null ? "" : payload.provider.trim();
            String resolvedProviderName = providerName.isEmpty()
                    ? WebloggerConfig.getProperty("translation.default.provider", "mymemory")
                    : providerName.toLowerCase();

            if (targetLang == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"Unsupported or missing targetLang\"}");
                return;
            }

            try {
                TranslationProvider provider = TranslationProviderFactory.getProvider(resolvedProviderName);
                if (payload.sections != null && !payload.sections.isEmpty()) {
                    List<TranslationSectionResponse> sections = CACHE_SERVICE.translateSections(
                            resolvedProviderName, provider, sourceLang, targetLang, payload.sections);
                    long cachedSections = sections.stream().filter(TranslationSectionResponse::isCached).count();
                    out.print(gson.toJson(Map.of(
                            "sections", sections,
                            "meta", Map.of(
                                    "cachedSections", cachedSections,
                                    "translatedSections", sections.size() - cachedSections))));
                } else if (payload.text != null && !payload.text.isEmpty()) {
                    List<TranslationSectionResponse> sections = CACHE_SERVICE.translateSections(
                            resolvedProviderName, provider, sourceLang, targetLang,
                            List.of(createLegacySection(payload.text)));
                    out.print(gson.toJson(Map.of("translations", sections.get(0).getTranslations())));
                } else {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\":\"Missing required fields: text or sections\"}");
                }

            } catch (Exception e) {
                log.error("Translation failed", e);
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
            }
        }
    }

    private String escapeJson(String msg) {
        if (msg == null)
            return "Unknown error";
        return msg.replace("\"", "\\\"").replace("\n", "\\n");
    }

    private TranslationSectionRequest createLegacySection(List<String> texts) {
        TranslationSectionRequest section = new TranslationSectionRequest();
        section.setSectionId("legacy");
        section.setTexts(texts);
        return section;
    }

    private static final class TranslationRequestPayload {
        private List<String> text;
        private List<TranslationSectionRequest> sections;
        private String sourceLang;
        private String targetLang;
        private String provider;
    }
}
