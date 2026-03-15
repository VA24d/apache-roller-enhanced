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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.pojos.WeblogEntry;


/**
 * Pipeline step that truncates blog entry text to a configurable maximum
 * word count. If the content exceeds the limit, the text is trimmed and
 * an ellipsis marker is appended.
 *
 * This acts as a text summarization step — reducing blog length to a
 * fixed max word count as required by the Transforming Feeds feature.
 */
public class ContentSummarizerStep implements EntryProcessingStep {

    private static final Log LOG = LogFactory.getLog(ContentSummarizerStep.class);

    private static final String NAME = "ContentSummarizer";
    private static final String DESCRIPTION =
            "Truncates blog entry text to a maximum word count for summarization.";

    private static final int DEFAULT_MAX_WORDS = 500;

    private final int maxWords;


    public ContentSummarizerStep() {
        this(DEFAULT_MAX_WORDS);
    }

    public ContentSummarizerStep(int maxWords) {
        this.maxWords = maxWords > 0 ? maxWords : DEFAULT_MAX_WORDS;
    }


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
        if (entry.getText() != null) {
            String original = entry.getText();
            String truncated = truncateToWordLimit(original, maxWords);
            if (!original.equals(truncated)) {
                entry.setText(truncated);
                LOG.debug("Content summarized for entry: " + entry.getTitle()
                        + " (max " + maxWords + " words)");
            }
        }
    }


    /**
     * Truncates the given text to the specified word limit.
     * Preserves HTML tags by counting only visible text words.
     * Appends " [...]" if truncation occurs.
     */
    static String truncateToWordLimit(String text, int limit) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        // Strip HTML tags for word counting, but work on original text
        String stripped = text.replaceAll("<[^>]+>", " ").trim();
        String[] words = stripped.split("\\s+");

        if (words.length <= limit) {
            return text;
        }

        // Count words in the original text (skipping HTML tags)
        int wordCount = 0;
        boolean insideTag = false;
        int cutoffIndex = text.length();

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '<') {
                insideTag = true;
                continue;
            }
            if (c == '>') {
                insideTag = false;
                continue;
            }
            if (insideTag) {
                continue;
            }

            // Detect word boundaries
            if (Character.isWhitespace(c)) {
                continue;
            }
            // At start of a word — scan to end of word
            int wordStart = i;
            while (i < text.length() && text.charAt(i) != '<'
                    && !Character.isWhitespace(text.charAt(i))) {
                i++;
            }
            wordCount++;
            if (wordCount >= limit) {
                cutoffIndex = i;
                break;
            }
        }

        return text.substring(0, cutoffIndex) + " [...]";
    }
}
