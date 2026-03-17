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


class SentimentAnalysisStepTest {

    @Test
    void testNameAndDescription() {
        SentimentAnalysisStep step = new SentimentAnalysisStep();
        assertEquals("SentimentAnalysis", step.getName());
        assertNotNull(step.getDescription());
    }

    @Test
    void testPositiveSentiment() {
        String text = "This is an amazing and wonderful product. I love it, truly fantastic and brilliant work!";
        String sentiment = SentimentAnalysisStep.analyzeSentiment(text);
        assertEquals("Positive", sentiment);
    }

    @Test
    void testNegativeSentiment() {
        String text = "This is terrible and awful. I hate how horrible and frustrating this broken thing is.";
        String sentiment = SentimentAnalysisStep.analyzeSentiment(text);
        assertEquals("Negative", sentiment);
    }

    @Test
    void testNeutralSentiment() {
        String text = "The software application processes data and stores results in a database. It uses standard protocols.";
        String sentiment = SentimentAnalysisStep.analyzeSentiment(text);
        assertEquals("Neutral", sentiment);
    }

    @Test
    void testBadgePrependedToText() {
        SentimentAnalysisStep step = new SentimentAnalysisStep();
        WeblogEntry entry = new WeblogEntry();
        String originalText = "This is an amazing wonderful fantastic post!";
        entry.setText(originalText);

        step.process(entry);

        // Badge should be prepended
        assertTrue(entry.getText().contains("sentiment-badge"));
        assertTrue(entry.getText().contains("Sentiment:"));
        // Original text should still be present
        assertTrue(entry.getText().contains(originalText));
    }

    @Test
    void testSearchDescriptionUpdated() {
        SentimentAnalysisStep step = new SentimentAnalysisStep();
        WeblogEntry entry = new WeblogEntry();
        entry.setText("This is an amazing wonderful fantastic post!");

        step.process(entry);

        assertNotNull(entry.getSearchDescription());
        assertTrue(entry.getSearchDescription().startsWith("Sentiment:"));
    }

    @Test
    void testSearchDescriptionPreservesExisting() {
        SentimentAnalysisStep step = new SentimentAnalysisStep();
        WeblogEntry entry = new WeblogEntry();
        entry.setText("This is an amazing wonderful fantastic post!");
        entry.setSearchDescription("My SEO description");

        step.process(entry);

        assertTrue(entry.getSearchDescription().contains("Sentiment:"));
        assertTrue(entry.getSearchDescription().contains("My SEO description"));
    }

    @Test
    void testNullTextIgnored() {
        SentimentAnalysisStep step = new SentimentAnalysisStep();
        WeblogEntry entry = new WeblogEntry();
        assertDoesNotThrow(() -> step.process(entry));
    }

    @Test
    void testEmptyTextIgnored() {
        SentimentAnalysisStep step = new SentimentAnalysisStep();
        WeblogEntry entry = new WeblogEntry();
        entry.setText("   ");
        assertDoesNotThrow(() -> step.process(entry));
    }

    @Test
    void testPositiveBadgeHasGreenStyling() {
        SentimentAnalysisStep step = new SentimentAnalysisStep();
        WeblogEntry entry = new WeblogEntry();
        entry.setText("Amazing wonderful excellent brilliant fantastic great love!");

        step.process(entry);

        assertTrue(entry.getText().contains("#dff0d8"));
        assertTrue(entry.getText().contains("Positive"));
    }

    @Test
    void testNegativeBadgeHasRedStyling() {
        SentimentAnalysisStep step = new SentimentAnalysisStep();
        WeblogEntry entry = new WeblogEntry();
        entry.setText("Terrible awful horrible hate frustrating broken useless disaster!");

        step.process(entry);

        assertTrue(entry.getText().contains("#f2dede"));
        assertTrue(entry.getText().contains("Negative"));
    }

    @Test
    void testMixedSentimentBalancesToNeutral() {
        // Equal positive and negative words should result in Neutral
        String text = "This is great but also terrible. Amazing yet awful.";
        String sentiment = SentimentAnalysisStep.analyzeSentiment(text);
        assertEquals("Neutral", sentiment);
    }
}
