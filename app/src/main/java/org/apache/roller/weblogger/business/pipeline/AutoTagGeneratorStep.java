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

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.pojos.WeblogEntry;


/**
 * Pipeline step that generates tags from the blog entry content using
 * keyword frequency analysis and appends them to the entry body.
 *
 * Uses a simple TF-based approach: strips HTML, tokenizes, removes
 * stop words, counts frequencies, and picks the top N most frequent
 * meaningful words as tags.
 */
public class AutoTagGeneratorStep implements EntryProcessingStep {

    private static final Log LOG = LogFactory.getLog(AutoTagGeneratorStep.class);

    private static final String NAME = "AutoTagGenerator";
    private static final String DESCRIPTION =
            "Generates tags from entry content using keyword frequency analysis "
                    + "and appends them to the body.";

    private static final int DEFAULT_MAX_TAGS = 5;
    private static final int MIN_WORD_LENGTH = 4;

    // Common English stop words to ignore
    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
            "the", "and", "for", "are", "but", "not", "you", "all",
            "can", "had", "her", "was", "one", "our", "out", "has",
            "have", "been", "some", "them", "than", "its", "over",
            "such", "that", "this", "with", "will", "each", "make",
            "like", "from", "just", "also", "into", "more", "other",
            "what", "when", "which", "their", "there", "these", "those",
            "then", "they", "your", "about", "would", "could", "should",
            "being", "after", "before", "where", "while", "between",
            "through", "very", "most", "much", "many", "well", "does",
            "here", "only", "even", "back", "know", "still", "really",
            "thing", "things", "http", "https", "www", "html", "nbsp"
    ));

    private final int maxTags;


    public AutoTagGeneratorStep() {
        this(DEFAULT_MAX_TAGS);
    }

    public AutoTagGeneratorStep(int maxTags) {
        this.maxTags = maxTags > 0 ? maxTags : DEFAULT_MAX_TAGS;
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
        String text = entry.getText();
        if (text == null || text.isBlank()) {
            return;
        }

        List<String> tags = extractTags(text, maxTags);
        if (tags.isEmpty()) {
            return;
        }

        // Append tags to the body as a formatted section
        String tagLine = tags.stream()
                .map(t -> "#" + t)
                .collect(Collectors.joining(" "));

        String tagSection = "\n<p class=\"auto-tags\"><em>Tags: " + tagLine + "</em></p>";
        entry.setText(text + tagSection);

        LOG.debug("Auto-generated tags for entry '" + entry.getTitle()
                + "': " + tags);
    }


    /**
     * Extract the top N tags from the given text using word frequency.
     */
    static List<String> extractTags(String text, int maxTags) {
        // Strip HTML
        String stripped = text.replaceAll("<[^>]+>", " ");
        // Tokenize: split on non-alphanumeric
        String[] tokens = stripped.toLowerCase().split("[^a-z0-9]+");

        // Count word frequencies (excluding stop words and short words)
        Map<String, Integer> freq = new HashMap<>();
        for (String token : tokens) {
            if (token.length() >= MIN_WORD_LENGTH && !STOP_WORDS.contains(token)) {
                freq.merge(token, 1, Integer::sum);
            }
        }

        // Sort by frequency descending, take top N
        return freq.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(maxTags)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
}
