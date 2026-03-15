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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.pojos.WeblogEntry;


/**
 * Orchestrates a chain of {@link EntryProcessingStep} instances that are
 * applied to a {@link WeblogEntry} at save time.
 *
 * Steps can be added or removed without affecting the remaining pipeline.
 *
 * Design Pattern: Chain of Responsibility — the pipeline delegates to each
 * step sequentially and each step is independent.
 */
public class EntryProcessingPipeline {

    private static final Log LOG = LogFactory.getLog(EntryProcessingPipeline.class);

    private final List<EntryProcessingStep> steps = new ArrayList<>();


    /**
     * Add a processing step to the end of the pipeline.
     */
    public void addStep(EntryProcessingStep step) {
        steps.add(step);
        LOG.debug("Added pipeline step: " + step.getName());
    }

    /**
     * Remove a processing step by name.
     *
     * @return true if a step was removed
     */
    public boolean removeStep(String stepName) {
        boolean removed = steps.removeIf(s -> s.getName().equals(stepName));
        if (removed) {
            LOG.debug("Removed pipeline step: " + stepName);
        }
        return removed;
    }

    /**
     * Returns an unmodifiable view of the current steps.
     */
    public List<EntryProcessingStep> getSteps() {
        return Collections.unmodifiableList(steps);
    }

    /**
     * Execute all steps on the given entry, in order.
     */
    public void execute(WeblogEntry entry) {
        if (entry == null) {
            return;
        }
        LOG.debug("Executing pipeline with " + steps.size() + " steps on entry: "
                + entry.getTitle());

        for (EntryProcessingStep step : steps) {
            try {
                LOG.debug("Running step: " + step.getName());
                step.process(entry);
            } catch (Exception e) {
                LOG.error("Error in pipeline step '" + step.getName()
                        + "' for entry '" + entry.getTitle() + "'", e);
            }
        }
    }
}
