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
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.pojos.WeblogEntry;


/**
 * Pipeline step that performs keyword-based sentiment analysis on a blog entry
 * and prepends a sentiment badge (Positive / Neutral / Negative) to the entry
 * text. Also stores the sentiment in {@code searchDescription} for SEO.
 *
 * <p>Uses a deterministic keyword-counting approach (no API call) for speed
 * and reliability. Runs after the profanity filter and content summarizer so
 * it analyzes clean, original text.
 */
public class SentimentAnalysisStep implements EntryProcessingStep {

    private static final Log LOG = LogFactory.getLog(SentimentAnalysisStep.class);

    private static final String NAME = "SentimentAnalysis";
    private static final String DESCRIPTION =
            "Analyzes entry sentiment using keyword detection and prepends a badge.";

    // Score thresholds: > POSITIVE_THRESHOLD = Positive, < NEGATIVE_THRESHOLD = Negative
    private static final int POSITIVE_THRESHOLD = 1;
    private static final int NEGATIVE_THRESHOLD = -1;

    static final Set<String> POSITIVE_WORDS = new HashSet<>(Arrays.asList(
            "great", "excellent", "amazing", "love", "wonderful", "fantastic",
            "brilliant", "awesome", "perfect", "happy", "excited", "beautiful",
            "inspiring", "innovative", "outstanding", "remarkable", "superb",
            "delightful", "magnificent", "impressive", "success", "successful",
            "enjoy", "enjoyable", "pleasant", "positive", "praise", "proud",
            "thrilled", "grateful", "best", "good", "better", "improve",
            "achievement", "celebrate", "elegant", "incredible", "joyful",
            "recommend", "valuable", "winning"
    ));

    static final Set<String> NEGATIVE_WORDS = new HashSet<>(Arrays.asList(
            "terrible", "awful", "horrible", "hate", "disappointing", "worst",
            "broken", "frustrating", "useless", "ugly", "boring", "failure",
            "disaster", "painful", "annoying", "dreadful", "pathetic", "poor",
            "miserable", "disgusting", "offensive", "unpleasant", "negative",
            "tragic", "worse", "bad", "wrong", "problem", "difficult",
            "unfortunate", "regret", "sad", "angry", "fear", "threat",
            "weakness", "flawed", "lacking", "inferior", "troubled"
    ));


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

        String plainText = ContentSummarizerStep.stripHtml(text);
        String sentiment = analyzeSentiment(plainText);

        // Prepend sentiment badge to entry text
        String badgeHtml = buildBadgeHtml(sentiment);
        entry.setText(badgeHtml + text);

        // Also store sentiment in searchDescription for SEO/meta tags
        String existing = entry.getSearchDescription();
        String prefix = "Sentiment: " + sentiment;
        if (existing != null && !existing.isBlank()) {
            entry.setSearchDescription(prefix + " | " + existing);
        } else {
            entry.setSearchDescription(prefix);
        }

        LOG.debug("Sentiment analysis for entry '" + entry.getTitle()
                + "': " + sentiment);
    }


    /**
     * Analyze sentiment by counting positive vs negative keyword matches.
     *
     * @return "Positive", "Neutral", or "Negative"
     */
    static String analyzeSentiment(String plainText) {
        String[] words = plainText.toLowerCase().split("[^a-z]+");

        int positiveCount = 0;
        int negativeCount = 0;

        for (String word : words) {
            if (POSITIVE_WORDS.contains(word)) {
                positiveCount++;
            }
            if (NEGATIVE_WORDS.contains(word)) {
                negativeCount++;
            }
        }

        int score = positiveCount - negativeCount;

        if (score > POSITIVE_THRESHOLD) {
            return "Positive";
        } else if (score < NEGATIVE_THRESHOLD) {
            return "Negative";
        } else {
            return "Neutral";
        }
    }

    private static String buildBadgeHtml(String sentiment) {
        String colorStyle;
        switch (sentiment) {
            case "Positive":
                colorStyle = "background-color:#dff0d8;color:#3c763d;";
                break;
            case "Negative":
                colorStyle = "background-color:#f2dede;color:#a94442;";
                break;
            default:
                colorStyle = "background-color:#f5f5f5;color:#777;";
                break;
        }

        return "<p class=\"sentiment-badge\" style=\"display:inline-block;"
                + "padding:2px 10px;border-radius:4px;font-size:0.85em;"
                + colorStyle + "margin-bottom:8px;\">"
                + "Sentiment: " + sentiment + "</p>\n";
    }
}
