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
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.pojos.WeblogEntry;


/**
 * Pipeline step that filters profane words from entry text and title.
 * Profane words are replaced with asterisks (e.g. "badword" -> "***").
 *
 * The word list is static and built into the step. In a production system
 * this could be loaded from a configuration file or database.
 */
public class ProfanityFilterStep implements EntryProcessingStep {

    private static final Log LOG = LogFactory.getLog(ProfanityFilterStep.class);

    private static final String NAME = "ProfanityFilter";
    private static final String DESCRIPTION =
            "Filters profane or inappropriate words from blog entry text and title.";

    // A curated list of words to filter (kept minimal for demonstration)
    private static final List<String> PROFANE_WORDS = Arrays.asList(
            "damn", "damnit", "crap", "hell", "bastard",
            "idiot", "stupid", "moron", "dumb", "jerk",
            "suck", "sucks", "bullshit", "asshole", "shit"
    );

    // Precompiled patterns for efficiency (case-insensitive, whole-word matching)
    private final List<Pattern> patterns;


    public ProfanityFilterStep() {
        patterns = PROFANE_WORDS.stream()
                .map(word -> Pattern.compile(
                        "\\b" + Pattern.quote(word) + "\\b",
                        Pattern.CASE_INSENSITIVE))
                .collect(Collectors.toList());
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
        if (entry.getTitle() != null) {
            entry.setTitle(filterText(entry.getTitle()));
        }
        if (entry.getText() != null) {
            entry.setText(filterText(entry.getText()));
        }
        if (entry.getSummary() != null) {
            entry.setSummary(filterText(entry.getSummary()));
        }
        LOG.debug("Profanity filter applied to entry: " + entry.getTitle());
    }


    /**
     * Replace each profane word with asterisks of the same length.
     */
    private String filterText(String text) {
        String result = text;
        for (Pattern pattern : patterns) {
            result = pattern.matcher(result).replaceAll(match -> {
                int len = match.group().length();
                return "*".repeat(len);
            });
        }
        return result;
    }
}
