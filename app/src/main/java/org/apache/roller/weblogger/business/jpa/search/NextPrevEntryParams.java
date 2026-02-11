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

package org.apache.roller.weblogger.business.jpa.search;

import java.util.Objects;

import org.apache.roller.weblogger.pojos.WeblogEntry;

/**
 * Parameter object for next/previous entry navigation.
 * Encapsulates parameters for finding adjacent entries in chronological order.
 */
public final class NextPrevEntryParams {

    private final WeblogEntry current;
    private final String categoryName;
    private final String locale;
    private final int maxEntries;
    private final boolean findNext;

    public NextPrevEntryParams(WeblogEntry current, String categoryName, String locale,
                              int maxEntries, boolean findNext) {
        this.current = Objects.requireNonNull(current, "current entry cannot be null");
        this.categoryName = categoryName;  // Can be null
        this.locale = locale;  // Can be null
        this.maxEntries = Math.max(1, maxEntries);
        this.findNext = findNext;
    }

    public WeblogEntry getCurrent() {
        return current;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public String getLocale() {
        return locale;
    }

    public int getMaxEntries() {
        return maxEntries;
    }

    public boolean isFindNext() {
        return findNext;
    }

    public String getDirection() {
        return findNext ? "NEXT" : "PREVIOUS";
    }

    public boolean hasCategory() {
        return categoryName != null && !categoryName.isEmpty();
    }

    public boolean hasLocale() {
        return locale != null && !locale.isEmpty();
    }

    @Override
    public String toString() {
        return "NextPrevEntryParams{" +
                "current=" + current.getId() +
                ", categoryName=" + categoryName +
                ", locale=" + locale +
                ", maxEntries=" + maxEntries +
                ", direction=" + getDirection() +
                '}';
    }
}
