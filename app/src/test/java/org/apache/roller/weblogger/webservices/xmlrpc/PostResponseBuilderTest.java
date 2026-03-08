/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  The ASF licenses this file to You
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

package org.apache.roller.weblogger.webservices.xmlrpc;

import org.apache.roller.weblogger.business.URLStrategy;
import org.apache.roller.weblogger.business.Weblogger;
import org.apache.roller.weblogger.config.WebloggerRuntimeConfig;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogCategory;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link PostResponseBuilder}.
 */
class PostResponseBuilderTest {

    @Test
    @SuppressWarnings("unchecked")
    void createPostStruct_returnsCorrectFields() {
        // Set up mocks
        Weblogger weblogger = mock(Weblogger.class);
        PostResponseBuilder builder = new PostResponseBuilder(weblogger);

        WeblogEntry entry = mock(WeblogEntry.class);
        User creator = mock(User.class);
        WeblogCategory category = mock(WeblogCategory.class);

        when(entry.getTitle()).thenReturn("Test Title");
        when(entry.getText()).thenReturn("Test Body");
        when(entry.getLink()).thenReturn("http://example.com/link");
        when(entry.getPermaLink()).thenReturn("/entry/test-title");
        when(entry.getId()).thenReturn("entry-123");
        when(entry.getPubTime()).thenReturn(new Timestamp(1000000L));
        when(entry.getCreator()).thenReturn(creator);
        when(entry.getCategory()).thenReturn(category);
        when(creator.getUserName()).thenReturn("testuser");
        when(creator.getEmailAddress()).thenReturn("test@example.com");
        when(category.getName()).thenReturn("Tech");

        try (MockedStatic<WebloggerRuntimeConfig> configMock =
                     mockStatic(WebloggerRuntimeConfig.class)) {
            configMock.when(WebloggerRuntimeConfig::getAbsoluteContextURL)
                    .thenReturn("http://localhost:8080");

            Map<String, Object> result = builder.createPostStruct(entry, "testuser");

            assertEquals("Test Title", result.get("title"));
            assertEquals("Test Body", result.get("description"));
            assertEquals("entry-123", result.get("postid"));
            assertEquals("testuser", result.get("userid"));
            assertEquals("test@example.com", result.get("author"));
            assertNotNull(result.get("pubDate"));
            assertNotNull(result.get("dateCreated"));
            assertNotNull(result.get("guid"));
            assertNotNull(result.get("permaLink"));

            // Category should be a list with one element
            Object cats = result.get("categories");
            assertNotNull(cats);
            assertTrue(cats instanceof List);
            assertEquals("Tech", ((List<Object>) cats).get(0));
        }
    }

    @Test
    void createPostStruct_omitsLink_whenNull() {
        Weblogger weblogger = mock(Weblogger.class);
        PostResponseBuilder builder = new PostResponseBuilder(weblogger);

        WeblogEntry entry = mock(WeblogEntry.class);
        User creator = mock(User.class);

        when(entry.getTitle()).thenReturn("Title");
        when(entry.getText()).thenReturn("Body");
        when(entry.getLink()).thenReturn(null); // no link
        when(entry.getPermaLink()).thenReturn("/x");
        when(entry.getId()).thenReturn("id-1");
        when(entry.getPubTime()).thenReturn(null); // no pub time
        when(entry.getCreator()).thenReturn(creator);
        when(entry.getCategory()).thenReturn(null); // no category
        when(creator.getUserName()).thenReturn("u");
        when(creator.getEmailAddress()).thenReturn("u@e.com");

        try (MockedStatic<WebloggerRuntimeConfig> configMock =
                     mockStatic(WebloggerRuntimeConfig.class)) {
            configMock.when(WebloggerRuntimeConfig::getAbsoluteContextURL)
                    .thenReturn("http://localhost");

            Map<String, Object> result = builder.createPostStruct(entry, "u");

            assertFalse(result.containsKey("link"));
            assertFalse(result.containsKey("pubDate"));
            assertFalse(result.containsKey("dateCreated"));
            assertFalse(result.containsKey("categories"));
        }
    }

    @Test
    void createCategoryStruct_returnsCorrectFields() {
        Weblogger weblogger = mock(Weblogger.class);
        URLStrategy strategy = mock(URLStrategy.class);
        Weblog weblog = mock(Weblog.class);
        WeblogCategory category = mock(WeblogCategory.class);

        when(weblogger.getUrlStrategy()).thenReturn(strategy);
        when(category.getName()).thenReturn("Java");
        when(category.getWeblog()).thenReturn(weblog);
        when(strategy.getWeblogCollectionURL(eq(weblog), isNull(), eq("Java"),
                isNull(), isNull(), eq(0), eq(true)))
                .thenReturn("http://localhost/blog/category/Java");
        when(strategy.getWeblogFeedURL(eq(weblog), isNull(), eq("entries"), eq("rss"),
                eq("Java"), isNull(), isNull(), eq(false), eq(true)))
                .thenReturn("http://localhost/blog/feed/entries/rss/Java");

        PostResponseBuilder builder = new PostResponseBuilder(weblogger);
        Map<String, String> result = builder.createCategoryStruct(category, "testuser");

        assertEquals("Java", result.get("title"));
        assertEquals("Java", result.get("description"));
        assertEquals("http://localhost/blog/category/Java", result.get("htmlUrl"));
        assertEquals("http://localhost/blog/feed/entries/rss/Java", result.get("rssUrl"));
    }
}
