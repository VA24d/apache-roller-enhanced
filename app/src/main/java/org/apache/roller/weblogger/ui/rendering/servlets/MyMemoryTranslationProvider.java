package org.apache.roller.weblogger.ui.rendering.servlets;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MyMemoryTranslationProvider implements TranslationProvider {

    private static final Log log = LogFactory.getLog(MyMemoryTranslationProvider.class);
    private static final String API_URL = "https://api.mymemory.translated.net/get";
    private static final Gson gson = new Gson();

    @Override
    public List<String> translate(List<String> texts, String sourceLang, String targetLang) throws Exception {
        if (sourceLang.equals(targetLang)) {
            return texts;
        }

        // Use parallel stream for faster batch processing
        return texts.parallelStream().map(text -> {
            try {
                if (text == null || text.trim().isEmpty()) {
                    return text;
                }
                return translateSingle(text, sourceLang, targetLang);
            } catch (Exception e) {
                log.error("Failed to translate text with MyMemory: " + text, e);
                return text; // Fallback to original
            }
        }).collect(Collectors.toList());
    }

    private String translateSingle(String text, String sourceLang, String targetLang) throws Exception {
        String query = URLEncoder.encode(text, StandardCharsets.UTF_8.toString());
        String langpair = URLEncoder.encode(sourceLang + "|" + targetLang, StandardCharsets.UTF_8.toString());

        String urlString = API_URL + "?q=" + query + "&langpair=" + langpair;
        URL url = new URL(urlString);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            throw new Exception("MyMemory API returned HTTP " + responseCode);
        }

        try (InputStreamReader isr = new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)) {
            java.lang.reflect.Type mapType = new TypeToken<Map<String, Object>>() {
            }.getType();
            Map<String, Object> respMap = gson.fromJson(isr, mapType);

            if (respMap != null && respMap.containsKey("responseData")) {
                Map<String, Object> responseData = (Map<String, Object>) respMap.get("responseData");
                if (responseData != null && responseData.containsKey("translatedText")) {
                    return (String) responseData.get("translatedText");
                }
            }
        }

        return text;
    }
}
