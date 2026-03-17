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
 * Pipeline step that estimates reading time based on word count and prepends
 * an "X min read" indicator to the entry text.
 *
 * <p>Uses an average reading speed of 238 words per minute (configurable).
 * Runs after sentiment analysis but before auto-tag generation so the tag
 * section is not counted towards reading time.
 */
public class ReadingTimeEstimatorStep implements EntryProcessingStep {

    private static final Log LOG = LogFactory.getLog(ReadingTimeEstimatorStep.class);

    private static final String NAME = "ReadingTimeEstimator";
    private static final String DESCRIPTION =
            "Estimates reading time and prepends an indicator to entry text.";

    private static final int DEFAULT_WPM = 238;

    private final int wordsPerMinute;


    public ReadingTimeEstimatorStep() {
        this(DEFAULT_WPM);
    }

    public ReadingTimeEstimatorStep(int wordsPerMinute) {
        this.wordsPerMinute = wordsPerMinute > 0 ? wordsPerMinute : DEFAULT_WPM;
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

        String plainText = ContentSummarizerStep.stripHtml(text);
        int minutes = calculateReadingTime(plainText);

        // Store reading time in searchDescription for display via template
        String existing = entry.getSearchDescription();
        String readingTimePart = minutes + " min read";
        if (existing != null && !existing.isBlank()) {
            entry.setSearchDescription(existing + " | " + readingTimePart);
        } else {
            entry.setSearchDescription(readingTimePart);
        }

        LOG.debug("Reading time for entry '" + entry.getTitle()
                + "': " + minutes + " min");
    }


    /**
     * Calculate reading time in minutes from plain text.
     * Minimum 1 minute.
     */
    static int calculateReadingTime(String plainText) {
        if (plainText == null || plainText.isBlank()) {
            return 1;
        }
        String[] words = plainText.split("\\s+");
        int wordCount = words.length;
        return Math.max(1, (int) Math.round((double) wordCount / DEFAULT_WPM));
    }

    /**
     * Overload that accepts a custom WPM.
     */
    int calculateReadingTimeWithWpm(String plainText) {
        if (plainText == null || plainText.isBlank()) {
            return 1;
        }
        String[] words = plainText.split("\\s+");
        int wordCount = words.length;
        return Math.max(1, (int) Math.round((double) wordCount / wordsPerMinute));
    }
}
