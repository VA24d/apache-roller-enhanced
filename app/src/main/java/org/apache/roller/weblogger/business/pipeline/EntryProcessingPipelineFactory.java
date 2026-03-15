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
 * Each step can be independently enabled/disabled via properties:
 * <ul>
 *   <li>{@code pipeline.step.profanityFilter.enabled} — profanity filter</li>
 *   <li>{@code pipeline.step.contentSummarizer.enabled} — content truncation</li>
 *   <li>{@code pipeline.step.autoTagGenerator.enabled} — auto tag generation</li>
 * </ul>
 *
 * New steps can be added by creating a class implementing
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
     */
    public static EntryProcessingPipeline createPipeline() {
        EntryProcessingPipeline pipeline = new EntryProcessingPipeline();

        // Step 1: Profanity filter
        if (isStepEnabled("pipeline.step.profanityFilter.enabled")) {
            pipeline.addStep(new ProfanityFilterStep());
        }

        // Step 2: Content summarizer
        if (isStepEnabled("pipeline.step.contentSummarizer.enabled")) {
            int maxWords = getIntConfig(
                    "pipeline.step.contentSummarizer.maxWords", 500);
            pipeline.addStep(new ContentSummarizerStep(maxWords));
        }

        // Step 3: Auto tag generator
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
