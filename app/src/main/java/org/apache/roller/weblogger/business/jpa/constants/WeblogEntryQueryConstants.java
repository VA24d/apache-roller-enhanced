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

package org.apache.roller.weblogger.business.jpa.constants;

/**
 * Constants for WeblogEntry query parameter indices and array positions.
 * This class centralizes magic numbers used in JPQL queries and result processing.
 */
public final class WeblogEntryQueryConstants {

    // Query parameter indices (1-based for JPQL)
    public static final int PARAM_WEBSITE = 1;
    public static final int PARAM_STATUS = 2;
    public static final int PARAM_END_DATE = 2;
    public static final int PARAM_START_DATE = 3;
    public static final int PARAM_LOCALE = 3;
    public static final int PARAM_TIMESTAMP = 1;

    // Result array indices (0-based for Object arrays)
    public static final int RESULT_COUNT = 0;
    public static final int RESULT_HANDLE = 1;
    public static final int RESULT_ANCHOR = 2;
    public static final int RESULT_TITLE = 3;

    // Query limits
    public static final int MAX_RECENT_ENTRIES = 100;
    public static final int DEFAULT_ENTRY_COUNT = 1;

    // String constants
    public static final String QUERY_SEPARATOR = " AND ";
    public static final String SORT_ORDER_DESC = " DESC";
    public static final String SORT_ORDER_ASC = " ASC";

    private WeblogEntryQueryConstants() {
        // Utility class, should not be instantiated
    }
}
