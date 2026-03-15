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

import org.apache.roller.weblogger.pojos.WeblogEntry;


/**
 * A single processing step in the entry processing pipeline.
 *
 * Each step transforms a WeblogEntry in-place (e.g. filtering text,
 * adding tags, summarizing content). Steps can be added or removed
 * from the pipeline without affecting others.
 *
 * Design Pattern: Chain of Responsibility — each step is an independent
 * handler in the processing chain.
 */
public interface EntryProcessingStep {

    /**
     * Returns the unique name of this processing step.
     */
    String getName();

    /**
     * Returns a human-readable description of what this step does.
     */
    String getDescription();

    /**
     * Process the given entry. Implementations should modify the entry
     * in-place (e.g. setting filtered text, adding tags).
     *
     * @param entry the weblog entry to process
     */
    void process(WeblogEntry entry);
}
