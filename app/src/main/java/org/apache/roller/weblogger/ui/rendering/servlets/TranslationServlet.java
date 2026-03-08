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
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Handles translation requests from the frontend widget.
 */
public class TranslationServlet extends HttpServlet {

    private static final Log log = LogFactory.getLog(TranslationServlet.class);
    private static final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try (PrintWriter out = response.getWriter()) {
            Type mapType = new TypeToken<Map<String, Object>>() {
            }.getType();
            Map<String, Object> payload = gson.fromJson(new InputStreamReader(request.getInputStream(), "UTF-8"),
                    mapType);

            if (payload == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"Invalid JSON payload\"}");
                return;
            }

            List<String> texts = (List<String>) payload.get("text");
            String sourceLang = (String) payload.get("sourceLang");
            String targetLang = (String) payload.get("targetLang");
            String providerName = (String) payload.get("provider");

            if (texts == null || texts.isEmpty() || targetLang == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"Missing required fields: text, targetLang\"}");
                return;
            }

            try {
                TranslationProvider provider = TranslationProviderFactory.getProvider(providerName);
                List<String> translations = provider.translate(texts, sourceLang, targetLang);

                String jsonResponse = gson.toJson(Map.of("translations", translations));
                out.print(jsonResponse);

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
}
