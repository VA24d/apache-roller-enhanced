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

import java.util.List;

import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


class AutoTagGeneratorStepTest {

    private AutoTagGeneratorStep step;

    @BeforeEach
    void setUp() {
        step = new AutoTagGeneratorStep(3);
    }

    @Test
    void testNameAndDescription() {
        assertEquals("AutoTagGenerator", step.getName());
        assertNotNull(step.getDescription());
    }

    @Test
    void testExtractTagsFromPlainText() {
        String text = "Java programming is great. Java developers love programming. "
                + "Java frameworks are used for programming web applications.";
        List<String> tags = AutoTagGeneratorStep.extractTags(text, 3);
        assertFalse(tags.isEmpty());
        // "java" and "programming" should be top tags
        assertTrue(tags.contains("java"));
        assertTrue(tags.contains("programming"));
    }

    @Test
    void testTagsAppendedToBody() {
        WeblogEntry entry = new WeblogEntry();
        entry.setText("Java programming is amazing. Java frameworks help developers. "
                + "Java is widely used for enterprise programming solutions.");
        step.process(entry);
        assertTrue(entry.getText().contains("auto-tags"));
        assertTrue(entry.getText().contains("#java"));
    }

    @Test
    void testStopWordsExcluded() {
        List<String> tags = AutoTagGeneratorStep.extractTags(
                "the the the the the and and and and and", 5);
        assertTrue(tags.isEmpty());
    }

    @Test
    void testShortWordsExcluded() {
        List<String> tags = AutoTagGeneratorStep.extractTags(
                "a b c d e f g h i j k l m", 5);
        assertTrue(tags.isEmpty());
    }

    @Test
    void testHtmlStripped() {
        String html = "<p>Java <b>programming</b> is great.</p>"
                + "<p>Java programming rocks.</p>";
        List<String> tags = AutoTagGeneratorStep.extractTags(html, 3);
        assertFalse(tags.isEmpty());
        assertTrue(tags.contains("java"));
    }

    @Test
    void testNullTextIgnored() {
        WeblogEntry entry = new WeblogEntry();
        assertDoesNotThrow(() -> step.process(entry));
    }

    @Test
    void testBlankTextIgnored() {
        WeblogEntry entry = new WeblogEntry();
        entry.setText("   ");
        step.process(entry);
        assertEquals("   ", entry.getText());
    }

    @Test
    void testMaxTagsRespected() {
        String text = "alpha alpha beta beta gamma gamma delta delta "
                + "epsilon epsilon zeta zeta eta eta theta theta";
        List<String> tags = AutoTagGeneratorStep.extractTags(text, 3);
        assertEquals(3, tags.size());
    }
}
