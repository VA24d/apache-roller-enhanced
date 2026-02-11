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

package org.apache.roller.util;

import java.util.Calendar;
import java.util.Date;


/**
 * Utility methods for computing date/time boundaries (start-of-day,
 * end-of-day, start/end of hour, minute, month, and noon).
 *
 * <p>Extracted from the former god-class {@link DateUtil} as part of
 * Issue&nbsp;#10 refactoring (Insufficient Modularization / WMC = 69).</p>
 */
public final class DateBoundaryUtil {

    /** Hour constant used by {@link #getNoonOfDay}. */
    private static final int NOON_HOUR = 12;

    private DateBoundaryUtil() {
        // utility class — prevent instantiation
    }

    // ---------------------------------------------------------------
    //  Start / End of Day
    // ---------------------------------------------------------------

    /**
     * Returns a Date set to the first possible millisecond of the day, just
     * after midnight. If a null day is passed in, a new Date is created.
     * midnight (00m 00h 00s)
     */
    public static Date getStartOfDay(Date day) {
        return getStartOfDay(day, Calendar.getInstance());
    }

    /**
     * Returns a Date set to the first possible millisecond of the day, just
     * after midnight. If a null day is passed in, a new Date is created.
     * midnight (00m 00h 00s)
     */
    public static Date getStartOfDay(Date day, Calendar cal) {
        if (day == null) {
            day = new Date();
        }
        cal.setTime(day);
        cal.set(Calendar.HOUR_OF_DAY, cal.getMinimum(Calendar.HOUR_OF_DAY));
        cal.set(Calendar.MINUTE,      cal.getMinimum(Calendar.MINUTE));
        cal.set(Calendar.SECOND,      cal.getMinimum(Calendar.SECOND));
        cal.set(Calendar.MILLISECOND, cal.getMinimum(Calendar.MILLISECOND));
        return cal.getTime();
    }

    /**
     * Returns a Date set to the last possible millisecond of the day, just
     * before midnight. If a null day is passed in, a new Date is created.
     */
    public static Date getEndOfDay(Date day) {
        return getEndOfDay(day, Calendar.getInstance());
    }

    public static Date getEndOfDay(Date day, Calendar cal) {
        if (day == null) {
            day = new Date();
        }
        cal.setTime(day);
        cal.set(Calendar.HOUR_OF_DAY, cal.getMaximum(Calendar.HOUR_OF_DAY));
        cal.set(Calendar.MINUTE,      cal.getMaximum(Calendar.MINUTE));
        cal.set(Calendar.SECOND,      cal.getMaximum(Calendar.SECOND));
        cal.set(Calendar.MILLISECOND, cal.getMaximum(Calendar.MILLISECOND));
        return cal.getTime();
    }

    // ---------------------------------------------------------------
    //  Start / End of Hour
    // ---------------------------------------------------------------

    /**
     * Returns a Date set to the first possible millisecond of the hour.
     * If a null day is passed in, a new Date is created.
     */
    public static Date getStartOfHour(Date day) {
        return getStartOfHour(day, Calendar.getInstance());
    }

    /**
     * Returns a Date set to the first possible millisecond of the hour.
     * If a null day is passed in, a new Date is created.
     */
    public static Date getStartOfHour(Date day, Calendar cal) {
        if (day == null) {
            day = new Date();
        }
        cal.setTime(day);
        cal.set(Calendar.MINUTE,      cal.getMinimum(Calendar.MINUTE));
        cal.set(Calendar.SECOND,      cal.getMinimum(Calendar.SECOND));
        cal.set(Calendar.MILLISECOND, cal.getMinimum(Calendar.MILLISECOND));
        return cal.getTime();
    }

    /**
     * Returns a Date set to the last possible millisecond of the hour.
     * If a null day is passed in, a new Date is created.
     */
    public static Date getEndOfHour(Date day) {
        return getEndOfHour(day, Calendar.getInstance());
    }

    public static Date getEndOfHour(Date day, Calendar cal) {
        if (day == null || cal == null) {
            return day;
        }

        cal.setTime(day);
        cal.set(Calendar.MINUTE,      cal.getMaximum(Calendar.MINUTE));
        cal.set(Calendar.SECOND,      cal.getMaximum(Calendar.SECOND));
        cal.set(Calendar.MILLISECOND, cal.getMaximum(Calendar.MILLISECOND));
        return cal.getTime();
    }

    // ---------------------------------------------------------------
    //  Start / End of Minute
    // ---------------------------------------------------------------

    /**
     * Returns a Date set to the first possible millisecond of the minute.
     * If a null day is passed in, a new Date is created.
     */
    public static Date getStartOfMinute(Date day) {
        return getStartOfMinute(day, Calendar.getInstance());
    }

    /**
     * Returns a Date set to the first possible millisecond of the minute.
     * If a null day is passed in, a new Date is created.
     */
    public static Date getStartOfMinute(Date day, Calendar cal) {
        if (day == null) {
            day = new Date();
        }
        cal.setTime(day);
        cal.set(Calendar.SECOND,      cal.getMinimum(Calendar.SECOND));
        cal.set(Calendar.MILLISECOND, cal.getMinimum(Calendar.MILLISECOND));
        return cal.getTime();
    }

    /**
     * Returns a Date set to the last possible millisecond of the minute.
     * If a null day is passed in, a new Date is created.
     */
    public static Date getEndOfMinute(Date day) {
        return getEndOfMinute(day, Calendar.getInstance());
    }

    public static Date getEndOfMinute(Date day, Calendar cal) {
        if (day == null || cal == null) {
            return day;
        }

        cal.setTime(day);
        cal.set(Calendar.SECOND,      cal.getMaximum(Calendar.SECOND));
        cal.set(Calendar.MILLISECOND, cal.getMaximum(Calendar.MILLISECOND));
        return cal.getTime();
    }

    // ---------------------------------------------------------------
    //  Start / End of Month
    // ---------------------------------------------------------------

    /**
     * Returns a Date set to the first possible millisecond of the month, just
     * after midnight. If a null day is passed in, a new Date is created.
     */
    public static Date getStartOfMonth(Date day) {
        return getStartOfMonth(day, Calendar.getInstance());
    }

    public static Date getStartOfMonth(Date day, Calendar cal) {
        if (day == null) {
            day = new Date();
        }
        cal.setTime(day);

        // set time to start of day
        cal.set(Calendar.HOUR_OF_DAY, cal.getMinimum(Calendar.HOUR_OF_DAY));
        cal.set(Calendar.MINUTE,      cal.getMinimum(Calendar.MINUTE));
        cal.set(Calendar.SECOND,      cal.getMinimum(Calendar.SECOND));
        cal.set(Calendar.MILLISECOND, cal.getMinimum(Calendar.MILLISECOND));

        // set time to first day of month
        cal.set(Calendar.DAY_OF_MONTH, 1);

        return cal.getTime();
    }

    /**
     * Returns a Date set to the last possible millisecond of the month, just
     * before midnight. If a null day is passed in, a new Date is created.
     */
    public static Date getEndOfMonth(Date day) {
        return getEndOfMonth(day, Calendar.getInstance());
    }

    public static Date getEndOfMonth(Date day, Calendar cal) {
        if (day == null) {
            day = new Date();
        }
        cal.setTime(day);

        // set time to end of day
        cal.set(Calendar.HOUR_OF_DAY, cal.getMaximum(Calendar.HOUR_OF_DAY));
        cal.set(Calendar.MINUTE,      cal.getMaximum(Calendar.MINUTE));
        cal.set(Calendar.SECOND,      cal.getMaximum(Calendar.SECOND));
        cal.set(Calendar.MILLISECOND, cal.getMaximum(Calendar.MILLISECOND));

        // set time to first day of month
        cal.set(Calendar.DAY_OF_MONTH, 1);

        // add one month
        cal.add(Calendar.MONTH, 1);

        // back up one day
        cal.add(Calendar.DAY_OF_MONTH, -1);

        return cal.getTime();
    }

    // ---------------------------------------------------------------
    //  Noon
    // ---------------------------------------------------------------

    /**
     * Returns a Date set just to Noon, to the closest possible millisecond
     * of the day. If a null day is passed in, a new Date is created.
     * noon (00m 12h 00s)
     */
    public static Date getNoonOfDay(Date day, Calendar cal) {
        if (day == null) {
            day = new Date();
        }
        cal.setTime(day);
        cal.set(Calendar.HOUR_OF_DAY, NOON_HOUR);
        cal.set(Calendar.MINUTE,      cal.getMinimum(Calendar.MINUTE));
        cal.set(Calendar.SECOND,      cal.getMinimum(Calendar.SECOND));
        cal.set(Calendar.MILLISECOND, cal.getMinimum(Calendar.MILLISECOND));
        return cal.getTime();
    }
}
