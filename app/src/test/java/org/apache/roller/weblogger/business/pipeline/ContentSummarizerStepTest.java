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
    }

    @Test
    void testShortTextUnchanged() {
        ContentSummarizerStep step = new ContentSummarizerStep(10);
        WeblogEntry entry = new WeblogEntry();
        entry.setText("Short text here.");
        step.process(entry);
        assertEquals("Short text here.", entry.getText());
    }

    @Test
    void testLongTextTruncated() {
        ContentSummarizerStep step = new ContentSummarizerStep(5);
        WeblogEntry entry = new WeblogEntry();
        entry.setText("one two three four five six seven eight nine ten");
        step.process(entry);
        assertTrue(entry.getText().endsWith("[...]"));
        // Should contain the first 5 words
        assertTrue(entry.getText().startsWith("one two three four five"));
    }

    @Test
    void testTruncateStaticMethod() {
        String result = ContentSummarizerStep.truncateToWordLimit(
                "word1 word2 word3 word4 word5 word6", 3);
        assertTrue(result.endsWith("[...]"));
        assertTrue(result.contains("word1"));
        assertTrue(result.contains("word3"));
        assertFalse(result.contains("word6"));
    }

    @Test
    void testNullTextIgnored() {
        ContentSummarizerStep step = new ContentSummarizerStep(10);
        WeblogEntry entry = new WeblogEntry();
        assertDoesNotThrow(() -> step.process(entry));
    }

    @Test
    void testEmptyTextIgnored() {
        String result = ContentSummarizerStep.truncateToWordLimit("", 10);
        assertEquals("", result);
    }

    @Test
    void testExactWordLimitNotTruncated() {
        ContentSummarizerStep step = new ContentSummarizerStep(5);
        WeblogEntry entry = new WeblogEntry();
        entry.setText("one two three four five");
        step.process(entry);
        assertEquals("one two three four five", entry.getText());
    }

    @Test
    void testHtmlTagsPreservedDuringTruncation() {
        ContentSummarizerStep step = new ContentSummarizerStep(3);
        WeblogEntry entry = new WeblogEntry();
        entry.setText("<p>Hello <b>world</b> this</p> is extra text here");
        step.process(entry);
        // Should truncate after ~3 visible words, and end with [...]
        assertTrue(entry.getText().endsWith("[...]"));
    }
}
