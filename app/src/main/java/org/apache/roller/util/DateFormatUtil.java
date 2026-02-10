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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import org.apache.commons.lang3.StringUtils;


/**
 * Utility methods for creating date formats, formatting dates to strings,
 * and parsing date strings.
 *
 * <p>Extracted from the former god-class {@link DateUtil} as part of
 * Issue&nbsp;#10 refactoring (Insufficient Modularization / WMC = 69).</p>
 */
public final class DateFormatUtil {

    // ---------------------------------------------------------------
    //  Format pattern constants
    // ---------------------------------------------------------------

    private static final String FORMAT_DEFAULT_DATE = "dd.MM.yyyy";
    private static final String FORMAT_DEFAULT_DATE_MINIMAL = "d.M.yy";

    private static final String FORMAT_6CHARS = "yyyyMM";
    private static final String FORMAT_8CHARS = "yyyyMMdd";

    private static final String FORMAT_ISO_8601 = "yyyy-MM-dd'T'HH:mm:ssZ";
    private static final String FORMAT_ISO_8601_DAY = "yyyy-MM-dd";

    private static final String FORMAT_RFC_822 = "EEE, d MMM yyyy HH:mm:ss Z";

    /** Number of trailing characters that form the timezone offset without colon. */
    private static final int ISO_8601_TZ_SUFFIX_LENGTH = 2;

    private DateFormatUtil() {
        // utility class — prevent instantiation
    }

    // ---------------------------------------------------------------
    //  Format factories
    // ---------------------------------------------------------------

    /** Returns a compact 8-char date format (yyyyMMdd). */
    public static SimpleDateFormat get8charDateFormat() {
        return new SimpleDateFormat(FORMAT_8CHARS);
    }

    /** Returns a compact 6-char month format (yyyyMM). */
    public static SimpleDateFormat get6charDateFormat() {
        return new SimpleDateFormat(FORMAT_6CHARS);
    }

    /** Returns an ISO-8601 date-time format. */
    public static SimpleDateFormat getIso8601DateFormat() {
        return new SimpleDateFormat(FORMAT_ISO_8601);
    }

    /** Returns an ISO-8601 day-only format (yyyy-MM-dd). */
    public static SimpleDateFormat getIso8601DayDateFormat() {
        return new SimpleDateFormat(FORMAT_ISO_8601_DAY);
    }

    /** Returns an RFC-822 date format (US locale). */
    public static SimpleDateFormat getRfc822DateFormat() {
        // http://www.w3.org/Protocols/rfc822/Overview.html#z28
        // Using Locale.US to fix ROL-725 and ROL-628
        return new SimpleDateFormat(FORMAT_RFC_822, Locale.US);
    }

    /**
     * Returns a "friendly" date format using full or minimal digits.
     *
     * @param minimalFormat if {@code true}, use single-digit day/month (d.M.yy)
     */
    public static SimpleDateFormat friendlyDateFormat(boolean minimalFormat) {
        if (minimalFormat) {
            return new SimpleDateFormat(FORMAT_DEFAULT_DATE_MINIMAL);
        }
        return new SimpleDateFormat(FORMAT_DEFAULT_DATE);
    }

    /** Convenience — returns full-digit friendly date format. */
    public static SimpleDateFormat fullDateFormat() {
        return friendlyDateFormat(false);
    }

    // ---------------------------------------------------------------
    //  Generic format / parse
    // ---------------------------------------------------------------

    /**
     * Formats a date using the given format, returning an empty string
     * if either argument is {@code null}.
     */
    public static String format(Date aDate, SimpleDateFormat aFormat) {
        if (aDate == null || aFormat == null) { return ""; }
        synchronized (aFormat) {
            return aFormat.format(aDate);
        }
    }

    /**
     * Parses a string using the given format.  Returns {@code null}
     * when the string is empty or the format is {@code null}.
     */
    public static Date parse(String aValue, SimpleDateFormat aFormat) throws ParseException {
        if (StringUtils.isEmpty(aValue) || aFormat == null) {
            return null;
        }
        synchronized (aFormat) {
            return aFormat.parse(aValue);
        }
    }

    // ---------------------------------------------------------------
    //  Convenience formatting methods
    // ---------------------------------------------------------------

    /** Formats a date as a full friendly string (dd.MM.yyyy). */
    public static String fullDate(Date date) {
        return format(date, fullDateFormat());
    }

    /** Formats a date as an 8-char stamp (yyyyMMdd). */
    public static String format8chars(Date date) {
        return format(date, get8charDateFormat());
    }

    /** Formats a date as an 8-char stamp (yyyyMMdd) with the given time zone. */
    public static String format8chars(Date date, TimeZone tz) {
        SimpleDateFormat formatter = get8charDateFormat();
        formatter.setTimeZone(tz);
        return format(date, formatter);
    }

    /** Formats a date as a 6-char month stamp (yyyyMM). */
    public static String format6chars(Date date) {
        return format(date, get6charDateFormat());
    }

    /** Formats a date as a 6-char month stamp (yyyyMM) with the given time zone. */
    public static String format6chars(Date date, TimeZone tz) {
        SimpleDateFormat formatter = get6charDateFormat();
        formatter.setTimeZone(tz);
        return format(date, formatter);
    }

    /** Formats a date as an ISO-8601 day string (yyyy-MM-dd). */
    public static String formatIso8601Day(Date date) {
        return format(date, getIso8601DayDateFormat());
    }

    /** Formats a date as an RFC-822 string. */
    public static String formatRfc822(Date date) {
        return format(date, getRfc822DateFormat());
    }

    /**
     * Formats a date as a valid ISO-8601 date-time string.
     * Inserts a colon into the timezone offset to comply with the standard.
     */
    public static String formatIso8601(Date date) {
        if (date == null) {
            return "";
        }

        // Add a colon before the last ISO_8601_TZ_SUFFIX_LENGTH chars
        // to make it a valid ISO-8601 date.
        String str = format(date, getIso8601DateFormat());
        StringBuilder sb = new StringBuilder();
        sb.append(str.substring(0, str.length() - ISO_8601_TZ_SUFFIX_LENGTH));
        sb.append(":");
        sb.append(str.substring(str.length() - ISO_8601_TZ_SUFFIX_LENGTH));
        return sb.toString();
    }
}
