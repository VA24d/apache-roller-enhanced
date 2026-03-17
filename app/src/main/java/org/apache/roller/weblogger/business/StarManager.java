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

package org.apache.roller.weblogger.business;

import java.util.List;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.UserEntryStar;
import org.apache.roller.weblogger.pojos.UserWeblogStar;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogEntry;


/**
 * Manages star (favourite) operations for weblogs and entries.
 */
public interface StarManager {

    // ---- Weblog stars ----

    /**
     * Save or update a weblog star.
     */
    void saveWeblogStar(UserWeblogStar star) throws WebloggerException;

    /**
     * Remove a weblog star.
     */
    void removeWeblogStar(UserWeblogStar star) throws WebloggerException;

    /**
     * Get a weblog star by its ID.
     */
    UserWeblogStar getWeblogStar(String id) throws WebloggerException;

    /**
     * Get the star for a specific user + weblog combination, or null if not starred.
     */
    UserWeblogStar getWeblogStarByUserAndWeblog(User user, Weblog weblog) throws WebloggerException;

    /**
     * Get a user's starred weblogs, sorted by most recent blog post (descending).
     * @param user  the user
     * @param offset  starting index (0-based)
     * @param length  max number of results (-1 for all)
     * @return list of UserWeblogStar objects
     */
    List<UserWeblogStar> getStarredWeblogs(User user, int offset, int length) throws WebloggerException;


    // ---- Entry stars ----

    /**
     * Save or update an entry star.
     */
    void saveEntryStar(UserEntryStar star) throws WebloggerException;

    /**
     * Remove an entry star.
     */
    void removeEntryStar(UserEntryStar star) throws WebloggerException;

    /**
     * Get an entry star by its ID.
     */
    UserEntryStar getEntryStar(String id) throws WebloggerException;

    /**
     * Get the star for a specific user + entry combination, or null if not starred.
     */
    UserEntryStar getEntryStarByUserAndEntry(User user, WeblogEntry entry) throws WebloggerException;

    /**
     * Get a user's starred entries, paginated, newest star first.
     * @param user   the user
     * @param offset starting index (0-based)
     * @param length max number of results (-1 for all)
     * @return list of UserEntryStar objects
     */
    List<UserEntryStar> getStarredEntries(User user, int offset, int length) throws WebloggerException;


    // ---- Trending (aggregate queries) ----

    /**
     * Get the top N trending weblogs by star count.
     * Uses efficient aggregate query (GROUP BY + COUNT), no iteration.
     * @param limit max number of results
     * @return list of Object[] where [0]=Weblog, [1]=Long (star count)
     */
    List<Object[]> getTrendingWeblogs(int limit) throws WebloggerException;

    /**
     * Get the top N trending entries by star count.
     * Uses efficient aggregate query (GROUP BY + COUNT), no iteration.
     * @param limit max number of results
     * @return list of Object[] where [0]=WeblogEntry, [1]=Long (star count)
     */
    List<Object[]> getTrendingEntries(int limit) throws WebloggerException;


    // ---- Counts ----

    /**
     * Get the total number of stars for a weblog.
     */
    long getWeblogStarCount(Weblog weblog) throws WebloggerException;

    /**
     * Get the total number of stars for an entry.
     */
    long getEntryStarCount(WeblogEntry entry) throws WebloggerException;


    /**
     * Release any resources held by this manager.
     */
    void release();
}
