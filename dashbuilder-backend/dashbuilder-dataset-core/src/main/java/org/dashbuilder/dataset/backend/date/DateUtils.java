package org.dashbuilder.dataset.backend.date;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.commons.lang.StringUtils;
import org.dashbuilder.dataset.DataColumn;
import org.dashbuilder.dataset.group.DateIntervalType;
import org.dashbuilder.dataset.group.GroupStrategy;

public class DateUtils {

    public static final String PATTERN_YEAR = "yyyy";
    public static final String PATTERN_MONTH = "yyyy-MM";
    public static final String PATTERN_DAY = "yyyy-MM-dd";
    public static final String PATTERN_HOUR = "yyyy-MM-dd HH";
    public static final String PATTERN_MINUTE = "yyyy-MM-dd HH:mm";
    public static final String PATTERN_SECOND = "yyyy-MM-dd HH:mm:ss";

    public static final SimpleDateFormat FORMATTER_YEAR  = new SimpleDateFormat(PATTERN_YEAR);
    public static final SimpleDateFormat FORMATTER_MONTH = new SimpleDateFormat(PATTERN_MONTH);
    public static final SimpleDateFormat FORMATTER_DAY = new SimpleDateFormat(PATTERN_DAY);
    public static final SimpleDateFormat FORMATTER_HOUR = new SimpleDateFormat(PATTERN_HOUR);
    public static final SimpleDateFormat FORMATTER_MINUTE = new SimpleDateFormat(PATTERN_MINUTE);
    public static final SimpleDateFormat FORMATTER_SECOND = new SimpleDateFormat(PATTERN_SECOND);

    private static final String MONTH_START = "-M";
    private static final String MONTH_END = "-";
    private static final String DAY_START = "-D";
    private static final String DAY_END = " ";
    private static final String HOUR_START = " H";
    private static final String HOUR_END = ":";
    private static final String MINUTE_START = ":m";
    private static final String MINUTE_END = ":";
    private static final String SECOND_START = ":s";
    private static final String SECOND_END = ":";

    public static Date parseDate(DataColumn dateColumn, Object date) throws Exception {
        DateIntervalType type = DateIntervalType.getByName(dateColumn.getIntervalType());
        GroupStrategy strategy = dateColumn.getColumnGroup().getStrategy();
        return parseDate(type, strategy, date);
    }

    public static Date parseDate(DateIntervalType type, GroupStrategy strategy, Object date) throws Exception {
        if (date == null) return null;

        // Fixed grouping
        if (GroupStrategy.FIXED.equals(strategy)) {
            Calendar c = GregorianCalendar.getInstance();

            if (DateIntervalType.SECOND.equals(type)) {
                c.set(Calendar.SECOND, (Integer) date);
                return c.getTime();
            }
            if (DateIntervalType.MINUTE.equals(type)) {
                c.set(Calendar.MINUTE, (Integer) date);
                return c.getTime();
            }
            if (DateIntervalType.HOUR.equals(type)) {
                c.set(Calendar.HOUR, (Integer) date);
                return c.getTime();
            }
            if (DateIntervalType.DAY.equals(type)) {
                c.set(Calendar.DAY_OF_MONTH, (Integer) date);
                return c.getTime();
            }
            if (DateIntervalType.MONTH.equals(type)) {
                c.set(Calendar.MONTH, ((Integer) date)-1);
                return c.getTime();
            }
            if (DateIntervalType.QUARTER.equals(type)) {
                c.set(Calendar.MONTH, ((Integer) date)-1);
                return c.getTime();
            }
            throw new IllegalArgumentException("Interval size '" + type + "' not supported for " +
                    "fixed date intervals. The only supported sizes are: " +
                    StringUtils.join(DateIntervalType.FIXED_INTERVALS_SUPPORTED, ","));
        }

        // Dynamic grouping
        if (type.getIndex() <= DateIntervalType.SECOND.getIndex()) {
            return FORMATTER_SECOND.parse(date.toString());
        }
        if (DateIntervalType.MINUTE.equals(type)) {
            return FORMATTER_MINUTE.parse(date.toString());
        }
        if (DateIntervalType.HOUR.equals(type)) {
            return FORMATTER_HOUR.parse(date.toString());
        }
        if (DateIntervalType.DAY.equals(type)) {
            return FORMATTER_DAY.parse(date.toString());
        }
        if (DateIntervalType.MONTH.equals(type) || DateIntervalType.QUARTER.equals(type)) {
            return FORMATTER_MONTH.parse(date.toString());
        }
        return FORMATTER_YEAR.parse(date.toString());
    }

    public static String ensureTwoDigits(String date, DateIntervalType intervalType) {
        String result = date;
        if (DateIntervalType.compare(intervalType, DateIntervalType.MONTH) <= 0) {
            result = ensureTwoDigits(result, MONTH_START, MONTH_END);
        }
        if (DateIntervalType.compare(intervalType, DateIntervalType.DAY) <= 0) {
            result = ensureTwoDigits(result, DAY_START, DAY_END);
        }
        if (DateIntervalType.compare(intervalType, DateIntervalType.HOUR) <= 0) {
            result = ensureTwoDigits(result, HOUR_START, HOUR_END);
        }
        if (DateIntervalType.compare(intervalType, DateIntervalType.MINUTE) <= 0) {
            result = ensureTwoDigits(result, MINUTE_START, MINUTE_END);
        }
        if (DateIntervalType.compare(intervalType, DateIntervalType.SECOND) <= 0) {
            result = ensureTwoDigits(result, SECOND_START, SECOND_END);
        }
        return result;
    }

    public static String ensureTwoDigits(String date, String symbolStart, String symbolEnd) {
        int start = date.indexOf(symbolStart);
        if (start == -1) return date;

        int digitStart = start+symbolStart.length();
        int end = date.indexOf(symbolEnd, digitStart);
        if (end == -1) end = date.length();

        StringBuilder out = new StringBuilder();
        out.append(date.substring(0, start)).append(symbolStart.charAt(0));
        if (end-digitStart == 1) {
            char digit = date.charAt(digitStart);
            out.append('0').append(digit);
            out.append(date.substring(digitStart+1));
        } else {
            out.append(date.substring(digitStart));
        }
        return out.toString();
    }
}
