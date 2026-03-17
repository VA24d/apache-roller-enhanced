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

        pipeline.addStep(new ProfanityFilterStep());
        pipeline.addStep(new SentimentAnalysisStep());

        WeblogEntry entry = new WeblogEntry();
        entry.setTitle("A damn fine blog post");
        entry.setText("This damn blog post has some really interesting content.");

        pipeline.execute(entry);

        // Profanity should be filtered
        assertFalse(entry.getTitle().contains("damn"));
        // Sentiment stored in searchDescription
        assertTrue(entry.getSearchDescription().contains("Sentiment:"));
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
    void testFullPipelineWithAllFiveSteps() {
        EntryProcessingPipeline pipeline = new EntryProcessingPipeline();
        pipeline.addStep(new ProfanityFilterStep());
        pipeline.addStep(new ContentSummarizerStep());
        pipeline.addStep(new SentimentAnalysisStep());
        pipeline.addStep(new ReadingTimeEstimatorStep());
        pipeline.addStep(new AutoTagGeneratorStep(3));

        WeblogEntry entry = new WeblogEntry();
        entry.setTitle("A damn fine programming tutorial");
        entry.setText("This is a programming tutorial about Java. "
                + "Java programming is used everywhere in the world. "
                + "Java developers love programming in Java. "
                + "This damn tutorial covers all the crap you need to know. "
                + "It is an amazing and wonderful guide for beginners.");

        pipeline.execute(entry);

        // Step 1: Profanity filtered in title and text
        assertFalse(entry.getTitle().contains("damn"));

        // Step 2: Summary generated (extractive fallback — no API in tests)
        assertNotNull(entry.getSummary());
        assertTrue(entry.getSummary().contains("<p>"));

        // Step 3: Sentiment stored in searchDescription
        assertTrue(entry.getSearchDescription().contains("Sentiment:"));

        // Step 4: Reading time stored in searchDescription
        assertTrue(entry.getSearchDescription().contains("min read"));

        // Step 5: Tags generated (appended to body)
        assertTrue(entry.getText().contains("auto-tags"));

        // Original content still present (not truncated!)
        assertTrue(entry.getText().contains("programming tutorial"));
    }

    @Test
    void testStepsCanBeIndependentlyAdded() {
        // Verify we can run any subset of steps
        EntryProcessingPipeline pipeline = new EntryProcessingPipeline();
        pipeline.addStep(new ReadingTimeEstimatorStep());
        pipeline.addStep(new AutoTagGeneratorStep(3));

        WeblogEntry entry = new WeblogEntry();
        entry.setText("Java programming is great for building enterprise applications. "
                + "Java has many frameworks and libraries available.");

        pipeline.execute(entry);

        assertTrue(entry.getSearchDescription().contains("min read"));
        assertTrue(entry.getText().contains("auto-tags"));
        // No sentiment since that step wasn't added
        String desc = entry.getSearchDescription();
        assertFalse(desc != null && desc.contains("Sentiment:"));
    }

    @Test
    void testPipelinePreservesOriginalTextContent() {
        // The summarizer should NOT truncate/destroy the original text
        EntryProcessingPipeline pipeline = new EntryProcessingPipeline();
        pipeline.addStep(new ContentSummarizerStep());

        WeblogEntry entry = new WeblogEntry();
        String longText = "First sentence of the article. Second sentence continues. "
                + "Third sentence with details. Fourth sentence concludes the thought. "
                + "Fifth sentence adds more context.";
        entry.setText(longText);

        pipeline.execute(entry);

        // Original text must still be intact
        assertEquals(longText, entry.getText());
        // Summary should be in the summary field, not replacing text
        assertNotNull(entry.getSummary());
    }
}
