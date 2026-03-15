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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


class ProfanityFilterStepTest {

    private ProfanityFilterStep step;

    @BeforeEach
    void setUp() {
        step = new ProfanityFilterStep();
    }

    @Test
    void testNameAndDescription() {
        assertEquals("ProfanityFilter", step.getName());
        assertNotNull(step.getDescription());
    }

    @Test
    void testFiltersProfaneWordsInText() {
        WeblogEntry entry = new WeblogEntry();
        entry.setText("This is a damn good post about some crap topic.");
        step.process(entry);
        assertFalse(entry.getText().contains("damn"));
        assertFalse(entry.getText().contains("crap"));
        assertTrue(entry.getText().contains("****")); // "damn" -> 4 stars
    }

    @Test
    void testFiltersProfaneWordsInTitle() {
        WeblogEntry entry = new WeblogEntry();
        entry.setTitle("What the hell is going on");
        entry.setText("Clean content here.");
        step.process(entry);
        assertFalse(entry.getTitle().contains("hell"));
        assertEquals("Clean content here.", entry.getText());
    }

    @Test
    void testFiltersProfaneWordsInSummary() {
        WeblogEntry entry = new WeblogEntry();
        entry.setSummary("This is stupid advice.");
        entry.setText("Normal text.");
        step.process(entry);
        assertFalse(entry.getSummary().contains("stupid"));
    }

    @Test
    void testCaseInsensitive() {
        WeblogEntry entry = new WeblogEntry();
        entry.setText("DAMN it, this SUCKS!");
        step.process(entry);
        assertFalse(entry.getText().contains("DAMN"));
        assertFalse(entry.getText().contains("SUCKS"));
    }

    @Test
    void testCleanTextUnchanged() {
        WeblogEntry entry = new WeblogEntry();
        String cleanText = "This is a perfectly clean blog post about Java programming.";
        entry.setText(cleanText);
        step.process(entry);
        assertEquals(cleanText, entry.getText());
    }

    @Test
    void testNullFieldsHandled() {
        WeblogEntry entry = new WeblogEntry();
        // all fields null — should not throw
        assertDoesNotThrow(() -> step.process(entry));
    }

    @Test
    void testWholeWordMatching() {
        WeblogEntry entry = new WeblogEntry();
        // "shell" contains "hell" but should NOT be filtered (whole-word match)
        entry.setText("I love working with shell scripts.");
        step.process(entry);
        assertTrue(entry.getText().contains("shell"));
    }
}
