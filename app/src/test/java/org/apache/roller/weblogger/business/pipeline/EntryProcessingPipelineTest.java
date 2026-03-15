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
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


class EntryProcessingPipelineTest {

    @Test
    void testAddAndRemoveSteps() {
        EntryProcessingPipeline pipeline = new EntryProcessingPipeline();
        assertEquals(0, pipeline.getSteps().size());

        pipeline.addStep(new ProfanityFilterStep());
        assertEquals(1, pipeline.getSteps().size());

        pipeline.addStep(new ContentSummarizerStep());
        assertEquals(2, pipeline.getSteps().size());

        assertTrue(pipeline.removeStep("ProfanityFilter"));
        assertEquals(1, pipeline.getSteps().size());

        assertFalse(pipeline.removeStep("NonExistentStep"));
        assertEquals(1, pipeline.getSteps().size());
    }

    @Test
    void testPipelineExecutesStepsInOrder() {
        EntryProcessingPipeline pipeline = new EntryProcessingPipeline();

        // Add profanity filter first, then summarizer
        pipeline.addStep(new ProfanityFilterStep());
        pipeline.addStep(new ContentSummarizerStep(10));

        WeblogEntry entry = new WeblogEntry();
        entry.setTitle("A damn fine blog post");
        entry.setText("This damn blog post has some really interesting "
                + "content that goes on for a while. "
                + "One two three four five six seven eight nine ten "
                + "eleven twelve thirteen fourteen fifteen.");

        pipeline.execute(entry);

        // Profanity should be filtered
        assertFalse(entry.getTitle().contains("damn"));
        // Content should be truncated (max 10 words)
        assertTrue(entry.getText().endsWith("[...]"));
    }

    @Test
    void testPipelineHandlesNullEntry() {
        EntryProcessingPipeline pipeline = new EntryProcessingPipeline();
        pipeline.addStep(new ProfanityFilterStep());
        assertDoesNotThrow(() -> pipeline.execute(null));
    }

    @Test
    void testEmptyPipeline() {
        EntryProcessingPipeline pipeline = new EntryProcessingPipeline();
        WeblogEntry entry = new WeblogEntry();
        entry.setText("Some text");
        pipeline.execute(entry);
        assertEquals("Some text", entry.getText());
    }

    @Test
    void testStepsListIsUnmodifiable() {
        EntryProcessingPipeline pipeline = new EntryProcessingPipeline();
        pipeline.addStep(new ProfanityFilterStep());
        assertThrows(UnsupportedOperationException.class,
                () -> pipeline.getSteps().add(new ContentSummarizerStep()));
    }

    @Test
    void testFullPipelineWithAllThreeSteps() {
        EntryProcessingPipeline pipeline = new EntryProcessingPipeline();
        pipeline.addStep(new ProfanityFilterStep());
        pipeline.addStep(new ContentSummarizerStep(500));
        pipeline.addStep(new AutoTagGeneratorStep(3));

        WeblogEntry entry = new WeblogEntry();
        entry.setTitle("A damn fine programming tutorial");
        entry.setText("This is a programming tutorial about Java. "
                + "Java programming is used everywhere. "
                + "Java developers love programming in Java. "
                + "This damn tutorial covers all the crap you need to know.");

        pipeline.execute(entry);

        // Profanity filtered
        assertFalse(entry.getTitle().contains("damn"));
        assertFalse(entry.getText().contains("damn"));
        assertFalse(entry.getText().contains("crap"));

        // Tags generated (appended to body)
        assertTrue(entry.getText().contains("auto-tags"));
    }
}
