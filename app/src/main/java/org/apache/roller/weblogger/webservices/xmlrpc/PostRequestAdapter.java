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

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

/**
 * Adapter that encapsulates the XML-RPC "struct" (Hashtable) for post requests,
 * providing type-safe access to post fields. This eliminates scattered magic-string
 * lookups and unsafe casts throughout the handler methods.
 */
public class PostRequestAdapter {

    // Field name constants matching the MetaWeblog API spec
    static final String FIELD_TITLE = "title";
    static final String FIELD_DESCRIPTION = "description";
    static final String FIELD_DATE_CREATED = "dateCreated";
    static final String FIELD_PUB_DATE = "pubDate";
    static final String FIELD_CATEGORIES = "categories";
    static final String FIELD_LINK = "link";

    private final Map<String, ?> struct;

    public PostRequestAdapter(Map<String, ?> struct) {
        if (struct == null) {
            throw new IllegalArgumentException("Post struct must not be null");
        }
        this.struct = struct;
    }

    public String getTitle() {
        String title = (String) struct.get(FIELD_TITLE);
        return title != null ? title : "";
    }

    public String getDescription() {
        return (String) struct.get(FIELD_DESCRIPTION);
    }

    /**
     * Returns the publication date, checking "dateCreated" first and falling back to "pubDate".
     * May return null if neither field is present.
     */
    public Date getDateCreated() {
        Date date = (Date) struct.get(FIELD_DATE_CREATED);
        if (date == null) {
            date = (Date) struct.get(FIELD_PUB_DATE);
        }
        return date;
    }

    /**
     * Returns the list of category names from the struct.
     * Returns an empty list if no categories are specified.
     */
    public List<String> getCategories() {
        Object[] cats = (Object[]) struct.get(FIELD_CATEGORIES);
        if (cats == null || cats.length == 0) {
            return Collections.emptyList();
        }
        List<String> result = new ArrayList<>(cats.length);
        for (Object cat : cats) {
            result.add((String) cat);
        }
        return result;
    }

    /**
     * Returns the first category name, or null if none specified.
     */
    public String getFirstCategory() {
        List<String> cats = getCategories();
        return cats.isEmpty() ? null : cats.get(0);
    }
}
