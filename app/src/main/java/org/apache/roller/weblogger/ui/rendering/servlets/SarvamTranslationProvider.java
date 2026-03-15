package org.apache.roller.weblogger.ui.rendering.servlets;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.config.WebloggerConfig;

import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

public class SarvamTranslationProvider implements TranslationProvider {

    private static final Log log = LogFactory.getLog(SarvamTranslationProvider.class);
    private static final String API_URL = "https://api.sarvam.ai/translate";
    private static final Gson gson = new Gson();
    private String apiKey;

    public SarvamTranslationProvider() {
        this.apiKey = WebloggerConfig.getProperty("translation.sarvam.apiKey");

        // Attempt to load from secure properties file if not in main config
        if (this.apiKey == null || this.apiKey.isEmpty()) {
            try {
                // In Jetty/Tomcat, the working directory or root may contain
                // translation-api.properties
                // We'll try user.dir as fallback
                String userDir = System.getProperty("user.dir");
                java.io.File propFile = new java.io.File(userDir, "translation-api.properties");
                if (!propFile.exists()) {
                    propFile = new java.io.File(userDir, "../translation-api.properties");
                }

                if (propFile.exists()) {
                    Properties props = new Properties();
                    try (InputStream is = new FileInputStream(propFile)) {
                        props.load(is);
                        this.apiKey = props.getProperty("translation.sarvam.apiKey");
                    }
                }
            } catch (Exception e) {
                log.warn("Could not load translation-api.properties", e);
            }
        }

        if (this.apiKey == null || this.apiKey.isEmpty()) {
            log.warn("Sarvam API key is not configured. Translations will fail.");
        }
    }

    @Override
    public List<String> translate(List<String> texts, String sourceLang, String targetLang) throws Exception {
        if (this.apiKey == null || this.apiKey.isEmpty()) {
            throw new Exception("Sarvam API key is missing. Please configure translation.sarvam.apiKey");
        }

        String normalizedSource = TranslationLanguageSupport.normalizeLanguageCode(sourceLang, "en");
        String normalizedTarget = TranslationLanguageSupport.normalizeLanguageCode(targetLang, "en");
        String srcCode = TranslationLanguageSupport.mapSarvamLanguageCode(normalizedSource);
        String tgtCode = TranslationLanguageSupport.mapSarvamLanguageCode(normalizedTarget);

        // Use parallel stream for faster batch processing
        return texts.parallelStream().map(text -> {
            try {
                if (text == null || text.trim().isEmpty()) {
                    return text;
                }
                return translateSingle(text, srcCode, tgtCode);
            } catch (Exception e) {
                log.error("Failed to translate text: " + text, e);
                return text; // Fallback to original text on failure
            }
        }).collect(Collectors.toList());
    }

    private String translateSingle(String text, String sourceLang, String targetLang) throws Exception {
        if (sourceLang.equals(targetLang)) {
            return text;
        }

        URL url = new URL(API_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("api-subscription-key", this.apiKey);
        conn.setDoOutput(true);

        Map<String, Object> reqBody = Map.of(
                "input", text,
                "source_language_code", sourceLang,
                "target_language_code", targetLang,
                "speaker_gender", "Male",
                "mode", "formal",
                "model", "mayura:v1",
                "enable_preprocessing", true);

        String jsonPayload = gson.toJson(reqBody);

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            InputStream errorStream = conn.getErrorStream();
            String errorMsg = "";
            if (errorStream != null) {
                try (InputStreamReader errReader = new InputStreamReader(errorStream, StandardCharsets.UTF_8)) {
                    StringBuilder sb = new StringBuilder();
                    int c;
                    while ((c = errReader.read()) != -1) {
                        sb.append((char) c);
                    }
                    errorMsg = sb.toString();
                }
            }
            throw new Exception("Sarvam API error (" + responseCode + "): " + errorMsg);
        }

        try (InputStreamReader isr = new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)) {
            java.lang.reflect.Type mapType = new TypeToken<Map<String, Object>>() {
            }.getType();
            Map<String, Object> respMap = gson.fromJson(isr, mapType);
            if (respMap != null && respMap.containsKey("translated_text")) {
                return (String) respMap.get("translated_text");
            }
        }

        return text;
    }
}
