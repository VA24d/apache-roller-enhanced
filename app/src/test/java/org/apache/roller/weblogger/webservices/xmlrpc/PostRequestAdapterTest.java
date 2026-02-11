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

import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.Hashtable;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link PostRequestAdapter}.
 * Validates type-safe extraction from the raw XML-RPC Hashtable struct.
 */
class PostRequestAdapterTest {

    @Test
    void getTitle_returnsTitle_whenPresent() {
        Hashtable<String, Object> struct = new Hashtable<>();
        struct.put("title", "My Post Title");

        PostRequestAdapter adapter = new PostRequestAdapter(struct);
        assertEquals("My Post Title", adapter.getTitle());
    }

    @Test
    void getTitle_returnsEmptyString_whenMissing() {
        Hashtable<String, Object> struct = new Hashtable<>();

        PostRequestAdapter adapter = new PostRequestAdapter(struct);
        assertEquals("", adapter.getTitle());
    }

    @Test
    void getDescription_returnsValue_whenPresent() {
        Hashtable<String, Object> struct = new Hashtable<>();
        struct.put("description", "Some content here");

        PostRequestAdapter adapter = new PostRequestAdapter(struct);
        assertEquals("Some content here", adapter.getDescription());
    }

    @Test
    void getDescription_returnsNull_whenMissing() {
        Hashtable<String, Object> struct = new Hashtable<>();

        PostRequestAdapter adapter = new PostRequestAdapter(struct);
        assertNull(adapter.getDescription());
    }

    @Test
    void getDateCreated_prefersDateCreated_overPubDate() {
        Hashtable<String, Object> struct = new Hashtable<>();
        Date dateCreated = new Date(1000000L);
        Date pubDate = new Date(2000000L);
        struct.put("dateCreated", dateCreated);
        struct.put("pubDate", pubDate);

        PostRequestAdapter adapter = new PostRequestAdapter(struct);
        assertEquals(dateCreated, adapter.getDateCreated());
    }

    @Test
    void getDateCreated_fallsToPubDate_whenDateCreatedMissing() {
        Hashtable<String, Object> struct = new Hashtable<>();
        Date pubDate = new Date(2000000L);
        struct.put("pubDate", pubDate);

        PostRequestAdapter adapter = new PostRequestAdapter(struct);
        assertEquals(pubDate, adapter.getDateCreated());
    }

    @Test
    void getDateCreated_returnsNull_whenBothMissing() {
        Hashtable<String, Object> struct = new Hashtable<>();

        PostRequestAdapter adapter = new PostRequestAdapter(struct);
        assertNull(adapter.getDateCreated());
    }

    @Test
    void getCategories_returnsList_whenPresent() {
        Hashtable<String, Object> struct = new Hashtable<>();
        struct.put("categories", new Object[]{"Tech", "Java"});

        PostRequestAdapter adapter = new PostRequestAdapter(struct);
        List<String> cats = adapter.getCategories();

        assertEquals(2, cats.size());
        assertEquals("Tech", cats.get(0));
        assertEquals("Java", cats.get(1));
    }

    @Test
    void getCategories_returnsEmptyList_whenMissing() {
        Hashtable<String, Object> struct = new Hashtable<>();

        PostRequestAdapter adapter = new PostRequestAdapter(struct);
        assertTrue(adapter.getCategories().isEmpty());
    }

    @Test
    void getCategories_returnsEmptyList_whenEmptyArray() {
        Hashtable<String, Object> struct = new Hashtable<>();
        struct.put("categories", new Object[]{});

        PostRequestAdapter adapter = new PostRequestAdapter(struct);
        assertTrue(adapter.getCategories().isEmpty());
    }

    @Test
    void getFirstCategory_returnsFirst_whenMultiplePresent() {
        Hashtable<String, Object> struct = new Hashtable<>();
        struct.put("categories", new Object[]{"Tech", "Java"});

        PostRequestAdapter adapter = new PostRequestAdapter(struct);
        assertEquals("Tech", adapter.getFirstCategory());
    }

    @Test
    void getFirstCategory_returnsNull_whenNoCategoriesPresent() {
        Hashtable<String, Object> struct = new Hashtable<>();

        PostRequestAdapter adapter = new PostRequestAdapter(struct);
        assertNull(adapter.getFirstCategory());
    }

    @Test
    void constructor_throwsOnNullStruct() {
        assertThrows(IllegalArgumentException.class,
                () -> new PostRequestAdapter(null));
    }
}
