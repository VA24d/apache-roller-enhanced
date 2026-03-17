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


class ContentSummarizerStepTest {

    @Test
    void testNameAndDescription() {
        ContentSummarizerStep step = new ContentSummarizerStep();
        assertEquals("ContentSummarizer", step.getName());
        assertNotNull(step.getDescription());
        assertTrue(step.getDescription().contains("summary"));
    }

    @Test
    void testOriginalTextPreservedAfterProcess() {
        // The step should NEVER modify entry.text — only set entry.summary
        ContentSummarizerStep step = new ContentSummarizerStep();
        WeblogEntry entry = new WeblogEntry();
        String originalText = "First sentence here. Second sentence here. Third sentence here. Fourth one too.";
        entry.setText(originalText);

        step.process(entry);

        // Original text must be untouched
        assertEquals(originalText, entry.getText());
        // Summary should be generated (extractive fallback since no API key in tests)
        assertNotNull(entry.getSummary());
        assertTrue(entry.getSummary().contains("First sentence here"));
    }

    @Test
    void testShortTextSkipsSummarization() {
        ContentSummarizerStep step = new ContentSummarizerStep();
        WeblogEntry entry = new WeblogEntry();
        entry.setText("Just one sentence.");
        step.process(entry);

        // Too short for summarization — summary should remain null
        assertNull(entry.getSummary());
        assertEquals("Just one sentence.", entry.getText());
    }

    @Test
    void testTwoSentencesSkipsSummarization() {
        ContentSummarizerStep step = new ContentSummarizerStep();
        WeblogEntry entry = new WeblogEntry();
        entry.setText("First sentence. Second sentence.");
        step.process(entry);

        assertNull(entry.getSummary());
    }

    @Test
    void testNullTextIgnored() {
        ContentSummarizerStep step = new ContentSummarizerStep();
        WeblogEntry entry = new WeblogEntry();
        assertDoesNotThrow(() -> step.process(entry));
        assertNull(entry.getSummary());
    }

    @Test
    void testEmptyTextIgnored() {
        ContentSummarizerStep step = new ContentSummarizerStep();
        WeblogEntry entry = new WeblogEntry();
        entry.setText("   ");
        assertDoesNotThrow(() -> step.process(entry));
        assertNull(entry.getSummary());
    }

    @Test
    void testExtractiveFallbackReturnsFirstThreeSentences() {
        String text = "First sentence. Second sentence. Third sentence. Fourth sentence. Fifth sentence.";
        String summary = ContentSummarizerStep.extractiveFallbackSummary(text);

        assertNotNull(summary);
        assertTrue(summary.contains("First sentence."));
        assertTrue(summary.contains("Second sentence."));
        assertTrue(summary.contains("Third sentence."));
        assertFalse(summary.contains("Fourth sentence."));
    }

    @Test
    void testExtractiveFallbackWithQuestionAndExclamation() {
        String text = "What is Java? It is amazing! Third point here. More content follows.";
        String summary = ContentSummarizerStep.extractiveFallbackSummary(text);

        assertNotNull(summary);
        assertTrue(summary.contains("What is Java?"));
        assertTrue(summary.contains("It is amazing!"));
        assertTrue(summary.contains("Third point here."));
        assertFalse(summary.contains("More content"));
    }

    @Test
    void testExtractiveFallbackNullAndEmpty() {
        assertNull(ContentSummarizerStep.extractiveFallbackSummary(null));
        assertNull(ContentSummarizerStep.extractiveFallbackSummary("   "));
    }

    @Test
    void testStripHtmlRemovesTags() {
        String html = "<p>Hello <b>world</b></p><br/>Test &amp; more";
        String plain = ContentSummarizerStep.stripHtml(html);

        assertFalse(plain.contains("<"));
        assertFalse(plain.contains(">"));
        assertTrue(plain.contains("Hello"));
        assertTrue(plain.contains("world"));
        assertTrue(plain.contains("& more"));
    }

    @Test
    void testSummaryWrappedInParagraphTag() {
        ContentSummarizerStep step = new ContentSummarizerStep();
        WeblogEntry entry = new WeblogEntry();
        entry.setText("First sentence here. Second sentence here. Third sentence here. Fourth one.");
        step.process(entry);

        assertNotNull(entry.getSummary());
        assertTrue(entry.getSummary().startsWith("<p>"));
        assertTrue(entry.getSummary().endsWith("</p>"));
    }
}
