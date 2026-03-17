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
import org.apache.roller.weblogger.config.WebloggerConfig;


/**
 * Factory that constructs an {@link EntryProcessingPipeline} based on
 * admin configuration in roller.properties.
 *
 * <p>Each step can be independently enabled/disabled via properties:
 * <ol>
 *   <li>{@code pipeline.step.profanityFilter.enabled} — profanity filter</li>
 *   <li>{@code pipeline.step.contentSummarizer.enabled} — AI summary generation</li>
 *   <li>{@code pipeline.step.sentimentAnalysis.enabled} — sentiment detection</li>
 *   <li>{@code pipeline.step.readingTimeEstimator.enabled} — reading time badge</li>
 *   <li>{@code pipeline.step.autoTagGenerator.enabled} — auto tag generation</li>
 * </ol>
 *
 * <p>New steps can be added by creating a class implementing
 * {@link EntryProcessingStep} and adding it to this factory.
 */
public final class EntryProcessingPipelineFactory {

    private static final Log LOG = LogFactory.getLog(EntryProcessingPipelineFactory.class);

    private EntryProcessingPipelineFactory() {
        // utility class
    }


    /**
     * Build a pipeline from the current admin configuration.
     * Steps that are disabled (or not configured) are skipped.
     *
     * <p>Execution order:
     * <ol>
     *   <li>ProfanityFilter — cleans text before downstream steps see it</li>
     *   <li>ContentSummarizer — generates AI summary from clean text</li>
     *   <li>SentimentAnalysis — analyzes clean text for sentiment</li>
     *   <li>ReadingTimeEstimator — calculates reading time from substantive content</li>
     *   <li>AutoTagGenerator — appends tags last so they don't affect other steps</li>
     * </ol>
     */
    public static EntryProcessingPipeline createPipeline() {
        EntryProcessingPipeline pipeline = new EntryProcessingPipeline();

        // Step 1: Profanity filter (must run first — cleans text for downstream)
        if (isStepEnabled("pipeline.step.profanityFilter.enabled")) {
            pipeline.addStep(new ProfanityFilterStep());
        }

        // Step 2: Content summarizer (AI-powered, generates entry.summary)
        if (isStepEnabled("pipeline.step.contentSummarizer.enabled")) {
            pipeline.addStep(new ContentSummarizerStep());
        }

        // Step 3: Sentiment analysis (keyword-based, prepends badge)
        if (isStepEnabled("pipeline.step.sentimentAnalysis.enabled")) {
            pipeline.addStep(new SentimentAnalysisStep());
        }

        // Step 4: Reading time estimator (prepends "X min read" badge)
        if (isStepEnabled("pipeline.step.readingTimeEstimator.enabled")) {
            int wpm = getIntConfig(
                    "pipeline.step.readingTimeEstimator.wordsPerMinute", 238);
            pipeline.addStep(new ReadingTimeEstimatorStep(wpm));
        }

        // Step 5: Auto tag generator (runs last — tags should not affect other steps)
        if (isStepEnabled("pipeline.step.autoTagGenerator.enabled")) {
            int maxTags = getIntConfig(
                    "pipeline.step.autoTagGenerator.maxTags", 5);
            pipeline.addStep(new AutoTagGeneratorStep(maxTags));
        }

        LOG.info("Entry processing pipeline created with "
                + pipeline.getSteps().size() + " steps");
        return pipeline;
    }


    private static boolean isStepEnabled(String propertyName) {
        String value = WebloggerConfig.getProperty(propertyName);
        return "true".equalsIgnoreCase(value);
    }

    private static int getIntConfig(String propertyName, int defaultValue) {
        String value = WebloggerConfig.getProperty(propertyName);
        if (value != null) {
            try {
                return Integer.parseInt(value.trim());
            } catch (NumberFormatException e) {
                LOG.warn("Invalid integer for " + propertyName + ": " + value);
            }
        }
        return defaultValue;
    }
}
