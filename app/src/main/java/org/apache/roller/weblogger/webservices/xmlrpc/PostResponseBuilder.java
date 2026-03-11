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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.roller.weblogger.business.URLStrategy;
import org.apache.roller.weblogger.business.Weblogger;
import org.apache.roller.weblogger.config.WebloggerRuntimeConfig;
import org.apache.roller.weblogger.pojos.WeblogCategory;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.util.Utilities;

/**
 * Builds XML-RPC response structs from domain objects.
 * Extracted from MetaWeblogAPIHandler to separate protocol serialization
 * from business logic orchestration (SRP).
 */
public class PostResponseBuilder {

    // ----- Response field name constants (MetaWeblog API spec) -----
    static final String RESP_TITLE = "title";
    static final String RESP_LINK = "link";
    static final String RESP_DESCRIPTION = "description";
    static final String RESP_PUB_DATE = "pubDate";
    static final String RESP_DATE_CREATED = "dateCreated";
    static final String RESP_GUID = "guid";
    static final String RESP_PERMA_LINK = "permaLink";
    static final String RESP_POST_ID = "postid";
    static final String RESP_USER_ID = "userid";
    static final String RESP_AUTHOR = "author";
    static final String RESP_CATEGORIES = "categories";
    static final String RESP_HTML_URL = "htmlUrl";
    static final String RESP_RSS_URL = "rssUrl";

    // ----- Feed format constants -----
    static final String FEED_TYPE_ENTRIES = "entries";
    static final String FEED_FORMAT_RSS = "rss";

    private final Weblogger weblogger;

    public PostResponseBuilder(Weblogger weblogger) {
        this.weblogger = weblogger;
    }

    /**
     * Creates the XML-RPC struct for a single blog post.
     */
    public Map<String, Object> createPostStruct(WeblogEntry entry, String userid) {
        String permalink =
                WebloggerRuntimeConfig.getAbsoluteContextURL() + entry.getPermaLink();

        Map<String, Object> struct = new HashMap<>();
        struct.put(RESP_TITLE, entry.getTitle());
        if (entry.getLink() != null) {
            struct.put(RESP_LINK, Utilities.escapeHTML(entry.getLink()));
        }
        struct.put(RESP_DESCRIPTION, entry.getText());
        if (entry.getPubTime() != null) {
            struct.put(RESP_PUB_DATE, entry.getPubTime());
            struct.put(RESP_DATE_CREATED, entry.getPubTime());
        }
        struct.put(RESP_GUID, Utilities.escapeHTML(permalink));
        struct.put(RESP_PERMA_LINK, Utilities.escapeHTML(permalink));
        struct.put(RESP_POST_ID, entry.getId());

        struct.put(RESP_USER_ID, entry.getCreator().getUserName());
        struct.put(RESP_AUTHOR, entry.getCreator().getEmailAddress());

        if (entry.getCategory() != null) {
            List<Object> catArray = new ArrayList<>();
            catArray.add(entry.getCategory().getName());
            struct.put(RESP_CATEGORIES, catArray);
        }

        return struct;
    }

    /**
     * Creates the XML-RPC struct for a single category.
     */
    public Map<String, String> createCategoryStruct(WeblogCategory category, String userid) {
        Map<String, String> struct = new HashMap<>();
        struct.put(RESP_TITLE, category.getName());
        struct.put(RESP_DESCRIPTION, category.getName());

        URLStrategy strategy = weblogger.getUrlStrategy();

        String catUrl = strategy.getWeblogCollectionURL(category.getWeblog(),
                null, category.getName(), null, null, 0, true);
        struct.put(RESP_HTML_URL, catUrl);

        String rssUrl = strategy.getWeblogFeedURL(category.getWeblog(),
                null, FEED_TYPE_ENTRIES, FEED_FORMAT_RSS,
                category.getName(), null, null, false, true);
        struct.put(RESP_RSS_URL, rssUrl);

        return struct;
    }
}
