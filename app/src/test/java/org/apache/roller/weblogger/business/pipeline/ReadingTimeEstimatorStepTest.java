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


class ReadingTimeEstimatorStepTest {

    @Test
    void testNameAndDescription() {
        ReadingTimeEstimatorStep step = new ReadingTimeEstimatorStep();
        assertEquals("ReadingTimeEstimator", step.getName());
        assertNotNull(step.getDescription());
    }

    @Test
    void testShortTextOnMinRead() {
        // Fewer than 238 words should be 1 min
        int minutes = ReadingTimeEstimatorStep.calculateReadingTime("Hello world this is a short text.");
        assertEquals(1, minutes);
    }

    @Test
    void testMediumText() {
        // ~500 words should be ~2 min (500/238 = 2.1)
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 500; i++) {
            sb.append("word ");
        }
        int minutes = ReadingTimeEstimatorStep.calculateReadingTime(sb.toString().trim());
        assertEquals(2, minutes);
    }

    @Test
    void testLongText() {
        // ~2000 words should be ~8 min (2000/238 = 8.4)
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 2000; i++) {
            sb.append("word ");
        }
        int minutes = ReadingTimeEstimatorStep.calculateReadingTime(sb.toString().trim());
        assertEquals(8, minutes);
    }

    @Test
    void testBadgePrependedToText() {
        ReadingTimeEstimatorStep step = new ReadingTimeEstimatorStep();
        WeblogEntry entry = new WeblogEntry();
        String originalText = "Some blog post content here.";
        entry.setText(originalText);

        step.process(entry);

        assertTrue(entry.getText().contains("reading-time"));
        assertTrue(entry.getText().contains("min read"));
        // Original text should still be present
        assertTrue(entry.getText().contains(originalText));
    }

    @Test
    void testNullTextIgnored() {
        ReadingTimeEstimatorStep step = new ReadingTimeEstimatorStep();
        WeblogEntry entry = new WeblogEntry();
        assertDoesNotThrow(() -> step.process(entry));
    }

    @Test
    void testEmptyTextIgnored() {
        ReadingTimeEstimatorStep step = new ReadingTimeEstimatorStep();
        WeblogEntry entry = new WeblogEntry();
        entry.setText("   ");
        assertDoesNotThrow(() -> step.process(entry));
    }

    @Test
    void testNullAndEmptyCalculation() {
        assertEquals(1, ReadingTimeEstimatorStep.calculateReadingTime(null));
        assertEquals(1, ReadingTimeEstimatorStep.calculateReadingTime("   "));
    }

    @Test
    void testCustomWordsPerMinute() {
        ReadingTimeEstimatorStep step = new ReadingTimeEstimatorStep(100);
        // 500 words at 100 WPM = 5 min
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 500; i++) {
            sb.append("word ");
        }
        int minutes = step.calculateReadingTimeWithWpm(sb.toString().trim());
        assertEquals(5, minutes);
    }

    @Test
    void testInvalidWpmFallsBackToDefault() {
        ReadingTimeEstimatorStep step = new ReadingTimeEstimatorStep(-1);
        // Should use default 238 WPM
        assertEquals("ReadingTimeEstimator", step.getName());
    }

    @Test
    void testBadgeContainsCorrectStyling() {
        ReadingTimeEstimatorStep step = new ReadingTimeEstimatorStep();
        WeblogEntry entry = new WeblogEntry();
        entry.setText("Some content for the blog post.");

        step.process(entry);

        assertTrue(entry.getText().contains("#d9edf7")); // blue background
        assertTrue(entry.getText().contains("reading-time"));
    }
}
