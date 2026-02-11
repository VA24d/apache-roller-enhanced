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
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;


/**
 * Thin backward-compatibility facade that delegates to the focused utility
 * classes {@link DateBoundaryUtil} and {@link DateFormatUtil}.
 *
 * <p><b>Prefer using the specific classes directly in new code.</b></p>
 *
 * <p>Dead-code methods (zero external callers) have been removed as part of
 * Issue&nbsp;#10 refactoring (Insufficient Modularization / WMC&nbsp;=&nbsp;69).</p>
 *
 * @deprecated use {@link DateBoundaryUtil} or {@link DateFormatUtil} instead
 */
@Deprecated
public abstract class DateUtil {

    // ---------------------------------------------------------------
    //  Boundary delegates
    // ---------------------------------------------------------------

    /** @deprecated use {@link DateBoundaryUtil#getStartOfDay(Date)} */
    @Deprecated public static Date getStartOfDay(Date day) {
        return DateBoundaryUtil.getStartOfDay(day);
    }

    /** @deprecated use {@link DateBoundaryUtil#getStartOfDay(Date, Calendar)} */
    @Deprecated public static Date getStartOfDay(Date day, Calendar cal) {
        return DateBoundaryUtil.getStartOfDay(day, cal);
    }

    /** @deprecated use {@link DateBoundaryUtil#getEndOfDay(Date)} */
    @Deprecated public static Date getEndOfDay(Date day) {
        return DateBoundaryUtil.getEndOfDay(day);
    }

    /** @deprecated use {@link DateBoundaryUtil#getEndOfDay(Date, Calendar)} */
    @Deprecated public static Date getEndOfDay(Date day, Calendar cal) {
        return DateBoundaryUtil.getEndOfDay(day, cal);
    }

    /** @deprecated use {@link DateBoundaryUtil#getStartOfHour(Date)} */
    @Deprecated public static Date getStartOfHour(Date day) {
        return DateBoundaryUtil.getStartOfHour(day);
    }

    /** @deprecated use {@link DateBoundaryUtil#getStartOfHour(Date, Calendar)} */
    @Deprecated public static Date getStartOfHour(Date day, Calendar cal) {
        return DateBoundaryUtil.getStartOfHour(day, cal);
    }

    /** @deprecated use {@link DateBoundaryUtil#getEndOfHour(Date)} */
    @Deprecated public static Date getEndOfHour(Date day) {
        return DateBoundaryUtil.getEndOfHour(day);
    }

    /** @deprecated use {@link DateBoundaryUtil#getEndOfHour(Date, Calendar)} */
    @Deprecated public static Date getEndOfHour(Date day, Calendar cal) {
        return DateBoundaryUtil.getEndOfHour(day, cal);
    }

    /** @deprecated use {@link DateBoundaryUtil#getStartOfMinute(Date)} */
    @Deprecated public static Date getStartOfMinute(Date day) {
        return DateBoundaryUtil.getStartOfMinute(day);
    }

    /** @deprecated use {@link DateBoundaryUtil#getStartOfMinute(Date, Calendar)} */
    @Deprecated public static Date getStartOfMinute(Date day, Calendar cal) {
        return DateBoundaryUtil.getStartOfMinute(day, cal);
    }

    /** @deprecated use {@link DateBoundaryUtil#getEndOfMinute(Date)} */
    @Deprecated public static Date getEndOfMinute(Date day) {
        return DateBoundaryUtil.getEndOfMinute(day);
    }

    /** @deprecated use {@link DateBoundaryUtil#getEndOfMinute(Date, Calendar)} */
    @Deprecated public static Date getEndOfMinute(Date day, Calendar cal) {
        return DateBoundaryUtil.getEndOfMinute(day, cal);
    }

    /** @deprecated use {@link DateBoundaryUtil#getStartOfMonth(Date)} */
    @Deprecated public static Date getStartOfMonth(Date day) {
        return DateBoundaryUtil.getStartOfMonth(day);
    }

    /** @deprecated use {@link DateBoundaryUtil#getStartOfMonth(Date, Calendar)} */
    @Deprecated public static Date getStartOfMonth(Date day, Calendar cal) {
        return DateBoundaryUtil.getStartOfMonth(day, cal);
    }

    /** @deprecated use {@link DateBoundaryUtil#getEndOfMonth(Date)} */
    @Deprecated public static Date getEndOfMonth(Date day) {
        return DateBoundaryUtil.getEndOfMonth(day);
    }

    /** @deprecated use {@link DateBoundaryUtil#getEndOfMonth(Date, Calendar)} */
    @Deprecated public static Date getEndOfMonth(Date day, Calendar cal) {
        return DateBoundaryUtil.getEndOfMonth(day, cal);
    }

    /** @deprecated use {@link DateBoundaryUtil#getNoonOfDay(Date, Calendar)} */
    @Deprecated public static Date getNoonOfDay(Date day, Calendar cal) {
        return DateBoundaryUtil.getNoonOfDay(day, cal);
    }

    // ---------------------------------------------------------------
    //  Format / parse delegates
    // ---------------------------------------------------------------

    /** @deprecated use {@link DateFormatUtil#format(Date, SimpleDateFormat)} */
    @Deprecated public static String format(Date aDate, SimpleDateFormat aFormat) {
        return DateFormatUtil.format(aDate, aFormat);
    }

    /** @deprecated use {@link DateFormatUtil#parse(String, SimpleDateFormat)} */
    @Deprecated public static Date parse(String aValue, SimpleDateFormat aFormat)
            throws ParseException {
        return DateFormatUtil.parse(aValue, aFormat);
    }

    /** @deprecated use {@link DateFormatUtil#get8charDateFormat()} */
    @Deprecated public static SimpleDateFormat get8charDateFormat() {
        return DateFormatUtil.get8charDateFormat();
    }

    /** @deprecated use {@link DateFormatUtil#get6charDateFormat()} */
    @Deprecated public static SimpleDateFormat get6charDateFormat() {
        return DateFormatUtil.get6charDateFormat();
    }

    /** @deprecated use {@link DateFormatUtil#getIso8601DateFormat()} */
    @Deprecated public static SimpleDateFormat getIso8601DateFormat() {
        return DateFormatUtil.getIso8601DateFormat();
    }

    /** @deprecated use {@link DateFormatUtil#getIso8601DayDateFormat()} */
    @Deprecated public static SimpleDateFormat getIso8601DayDateFormat() {
        return DateFormatUtil.getIso8601DayDateFormat();
    }

    /** @deprecated use {@link DateFormatUtil#getRfc822DateFormat()} */
    @Deprecated public static SimpleDateFormat getRfc822DateFormat() {
        return DateFormatUtil.getRfc822DateFormat();
    }

    /** @deprecated use {@link DateFormatUtil#fullDateFormat()} */
    @Deprecated public static SimpleDateFormat fullDateFormat() {
        return DateFormatUtil.fullDateFormat();
    }

    /** @deprecated use {@link DateFormatUtil#friendlyDateFormat(boolean)} */
    @Deprecated public static SimpleDateFormat friendlyDateFormat(boolean minimalFormat) {
        return DateFormatUtil.friendlyDateFormat(minimalFormat);
    }

    /** @deprecated use {@link DateFormatUtil#fullDate(Date)} */
    @Deprecated public static String fullDate(Date date) {
        return DateFormatUtil.fullDate(date);
    }

    /** @deprecated use {@link DateFormatUtil#format8chars(Date)} */
    @Deprecated public static String format8chars(Date date) {
        return DateFormatUtil.format8chars(date);
    }

    /** @deprecated use {@link DateFormatUtil#format8chars(Date, TimeZone)} */
    @Deprecated public static String format8chars(Date date, TimeZone tz) {
        return DateFormatUtil.format8chars(date, tz);
    }

    /** @deprecated use {@link DateFormatUtil#format6chars(Date)} */
    @Deprecated public static String format6chars(Date date) {
        return DateFormatUtil.format6chars(date);
    }

    /** @deprecated use {@link DateFormatUtil#format6chars(Date, TimeZone)} */
    @Deprecated public static String format6chars(Date date, TimeZone tz) {
        return DateFormatUtil.format6chars(date, tz);
    }

    /** @deprecated use {@link DateFormatUtil#formatIso8601Day(Date)} */
    @Deprecated public static String formatIso8601Day(Date date) {
        return DateFormatUtil.formatIso8601Day(date);
    }

    /** @deprecated use {@link DateFormatUtil#formatRfc822(Date)} */
    @Deprecated public static String formatRfc822(Date date) {
        return DateFormatUtil.formatRfc822(date);
    }

    /** @deprecated use {@link DateFormatUtil#formatIso8601(Date)} */
    @Deprecated public static String formatIso8601(Date date) {
        return DateFormatUtil.formatIso8601(date);
    }
}
