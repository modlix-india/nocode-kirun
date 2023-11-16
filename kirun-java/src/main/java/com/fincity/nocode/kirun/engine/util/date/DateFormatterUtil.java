package com.fincity.nocode.kirun.engine.util.date;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.fincity.nocode.kirun.engine.util.stream.TriFunction;

public class DateFormatterUtil {

    private DateFormatterUtil() {

    }

    public static class PatternObject {

        private String str;
        private String pattern;

        public PatternObject(String str, String pattern) {
            this.str = str;
            this.pattern = pattern;
        }

        public String getStr() {
            return this.str;
        }

        public String getPattern() {
            return this.pattern;
        }
    }

    private static final String[] DAYS_OF_WEEK = {
            "Sunday",
            "Monday",
            "Tuesday",
            "Wednesday",
            "Thursday",
            "Friday",
            "Saturday",
    };

    private static final String[] MONTHS_OF_YEAR = {
            "January",
            "February",
            "March",
            "April",
            "May",
            "June",
            "July",
            "August",
            "September",
            "October",
            "November",
            "December",
    };

    private static final String[] DAYS_OF_MONTH_SUFFIX = { "th", "st", "nd", "rd" };

    private static final String[] yearOffset = { "000", "00", "0", "" };

    private static Map<String, TriFunction<String, String, Calendar, PatternObject>> tokenFunction = Map
            .ofEntries(
                    Map.entry("M", DateFormatterUtil::justMonths),
                    Map.entry("Q", DateFormatterUtil::getQuarter),
                    Map.entry("D", DateFormatterUtil::getDayOfYearMonth),
                    Map.entry("d", DateFormatterUtil::getDayOfWeek),
                    Map.entry("W", DateFormatterUtil::weekOfTheYear),
                    Map.entry("w", DateFormatterUtil::weekOfTheYear),
                    Map.entry("Y", DateFormatterUtil::justYear),
                    Map.entry("y", DateFormatterUtil::justYear),
                    Map.entry("N", DateFormatterUtil::getEra),
                    Map.entry("A", (str, pattern, date) -> {

                        str += date.get(Calendar.HOUR) < 12 ? "AM" : "PM";
                        return new PatternObject(str, pattern.substring(1));
                    }),
                    Map.entry("a", (str, pattern, date) -> {

                        str += date.get(Calendar.HOUR) < 12 ? "am" : "pm";
                        return new PatternObject(str, pattern.substring(1));
                    }),
                    Map.entry("h", DateFormatterUtil::simpleHours),
                    Map.entry("H", DateFormatterUtil::justHours),
                    Map.entry("k", DateFormatterUtil::expandedHours),
                    Map.entry("m", DateFormatterUtil::justMinutes),
                    Map.entry("s", DateFormatterUtil::justSeconds),
                    Map.entry("S", DateFormatterUtil::specificSeconds),
                    Map.entry("Z", DateFormatterUtil::justOffset),
                    Map.entry("x", (str, pattern, date) -> {

                        str = str + (date.getTimeInMillis() / 1000);
                        return new PatternObject(str, pattern.substring(1));

                    }),
                    Map.entry("X", (str, pattern, date) -> {

                        str = str + date.getTimeInMillis();
                        return new PatternObject(str, pattern.substring(1));

                    })

            );

    private static final Map<String, String> value_token_rules = Map.ofEntries();

    public static String formattedStringFromDate(Calendar cal, String pattern) {

        List<String> patterns = patternSplitting(pattern);

        String formattedDate = IntStream.range(0, patterns.size())
                .mapToObj(ind -> {
                    if (ind % 2 == 1)
                        return patterns.get(ind);

                    String str = "";
                    String presentPattern = patterns.get(ind);

                    while (presentPattern.length() != 0) {

                        if (tokenFunction.get(presentPattern.substring(0, 1)) != null) {

                            PatternObject nextValues = tokenFunction.get(presentPattern.substring(0, 1)).apply(
                                    str,
                                    presentPattern, cal);

                            str = nextValues.str;
                            presentPattern = nextValues.pattern;

                        } else {
                            str += presentPattern.charAt(0);
                            presentPattern = presentPattern.substring(1);
                        }
                    }

                    return str;

                }).collect(Collectors.joining(""));

        return formattedDate.replace("\'\'", "'");
    }

    private static PatternObject getDayOfYearMonth(String str, String pattern, Calendar date) {

        if (pattern.startsWith("DDD")) {
            int dayOfTheYear = 0;

            for (int i = 0; i < date.get(Calendar.MONTH); i++) {
                dayOfTheYear += new Date(date.get(Calendar.YEAR), i + 1, 0).getDate();
            }
            dayOfTheYear += date.get(Calendar.DATE);

            if (pattern.startsWith("DDDD")) {
                str += convertMillisToString(dayOfTheYear);
                return new PatternObject(str, pattern.substring(4));

            } else if (pattern.startsWith("DDDth")) {

                str += numberToOrdinal(dayOfTheYear);
                return new PatternObject(str, pattern.substring(5));

            } else if (pattern.startsWith("DDDTH")) {

                str += numberToOrdinal(dayOfTheYear).toUpperCase();
                return new PatternObject(str, pattern.substring(5));
            }
            str += dayOfTheYear;
            return new PatternObject(str, pattern.substring(3));
        }

        int dayOfTheMonth = date.get(Calendar.DATE);

        if (pattern.startsWith("DD")) {
            str += convertToString(dayOfTheMonth);
            return new PatternObject(str, pattern.substring(2));

        } else if (pattern.startsWith("Dth")) {
            str += numberToOrdinal(dayOfTheMonth);
            return new PatternObject(str, pattern.substring(3));

        } else if (pattern.startsWith("DTH")) {

            str += numberToOrdinal(dayOfTheMonth).toUpperCase();
            return new PatternObject(str, pattern.substring(3));
        }

        str += dayOfTheMonth;
        return new PatternObject(str, pattern.substring(1));
    }

    private static PatternObject getDayOfWeek(String str, String pattern, Calendar date) {

        int dayOfTheWeek = date.get(Calendar.DAY_OF_WEEK);

        if (pattern.startsWith("dddd")) {

            return new PatternObject(str + DAYS_OF_WEEK[dayOfTheWeek], pattern.substring(4));

        } else if (pattern.startsWith("ddd")) {

            return new PatternObject(str + DAYS_OF_WEEK[dayOfTheWeek].substring(0, 3), pattern.substring(3));

        } else if (pattern.startsWith("dd")) {

            return new PatternObject(str + DAYS_OF_WEEK[dayOfTheWeek].substring(0, 2), pattern.substring(2));

        } else if (pattern.startsWith("dth")) {

            return new PatternObject(str + numberToOrdinal(dayOfTheWeek + 1), pattern.substring(3));

        } else if (pattern.startsWith("dTH")) {

            return new PatternObject(str + numberToOrdinal(dayOfTheWeek + 1).toUpperCase(), pattern.substring(3));
        }

        return new PatternObject(str + (dayOfTheWeek + 1), pattern.substring(1));

    }

    private static PatternObject getEra(String str, String pattern, Calendar date) {

        int year = date.get(Calendar.YEAR);

        if (pattern.startsWith("NNNN")) {
            str += year < 0 ? "Before Common Era" : "After Common Era";
            return new PatternObject(str, pattern.substring(4));
        }
        if (pattern.startsWith("NNN")) {
            str += year < 0 ? "BCE" : "CE";
            return new PatternObject(str, pattern.substring(3));
        }
        if (pattern.startsWith("NN")) {
            str += year < 0 ? "BC" : "AD";
            return new PatternObject(str, pattern.substring(2));
        }
        str += year < 0 ? "BC" : "AD";

        return new PatternObject(str, pattern.substring(1));
    }

    private static PatternObject getQuarter(String str, String pattern, Calendar date) {

        int quarter = (date.get(Calendar.MONTH) / 3) + 1;
        if (pattern.startsWith("QQ")) {
            str += convertToString(quarter);
            return new PatternObject(str, pattern.substring(2));

        } else if (pattern.startsWith("Qth")) {
            str += numberToOrdinal(quarter);
            return new PatternObject(str, pattern.substring(3));
        } else if (pattern.startsWith("QTH")) {
            str += numberToOrdinal(quarter).toUpperCase();
            return new PatternObject(str, pattern.substring(3));
        }

        str += quarter;
        return new PatternObject(str, pattern.substring(1));
    }

    private static PatternObject justMonths(String str, String pattern, Calendar date) {

        int month = date.get(Calendar.MONTH);

        if (pattern.startsWith("MMMM")) {
            str += MONTHS_OF_YEAR[month];
            return new PatternObject(str, pattern.substring(4));

        } else if (pattern.startsWith("MMM")) {
            str += MONTHS_OF_YEAR[month].substring(0, 3);
            return new PatternObject(str, pattern.substring(3));
        } else if (pattern.startsWith("Mth")) {
            str += numberToOrdinal(month + 1);
        } else if (pattern.startsWith("MTH")) {

            str += numberToOrdinal(month + 1).toUpperCase();
        } else if (pattern.startsWith("MM")) {
            str += convertToString(month + 1);
            return new PatternObject(str, pattern.substring(2));
        }

        str += (month + 1);
        return new PatternObject(str, pattern.substring(1));
    }

    private static PatternObject specificSeconds(String str, String pattern, Calendar date) { // test properly this
                                                                                              // method

        int millis = date.get(Calendar.MILLISECOND);

        if (pattern.startsWith("Sth") || pattern.startsWith("STH")) {
            boolean hasUpperCase = pattern.startsWith("STH");

            str += hasUpperCase
                    ? numberToOrdinal(millis).toUpperCase()
                    : numberToOrdinal(millis);
            return new PatternObject(str, pattern.substring(3));
        }

        int i = 0;
        while (i < pattern.length() && pattern.charAt(i) == 'S')
            i++;

        if (i <= 3) {
            String x = convertMillisToString(millis);
            str += padStart(x.substring(0, i), i, "0");
        } else
            str += padEnd(padStart(convertMillisToString(millis), i, "0"), i - 3, "0");

        return new PatternObject(str, pattern.substring(i));
    }

    private static PatternObject simpleHours(String str, String pattern, Calendar date) {

        int hours = date.get(Calendar.HOUR_OF_DAY);

        if (hours > 12)
            hours -= 12;
        if (hours == 0)
            hours = 12;

        if (pattern.startsWith("hh")) {

            str += convertToString(hours);
            return new PatternObject(str, pattern.substring(2));

        } else if (pattern.startsWith("hth")) {

            str += numberToOrdinal(hours);
            return new PatternObject(str, pattern.substring(3));

        } else if (pattern.startsWith("hTH")) {

            str += numberToOrdinal(hours).toUpperCase();
            return new PatternObject(str, pattern.substring(3));
        }
        str += hours;
        return new PatternObject(str, pattern.substring(1));
    }

    private static PatternObject justHours(String str, String pattern, Calendar date) {

        int hours = date.get(Calendar.HOUR_OF_DAY);

        if (pattern.startsWith("HH")) {
            str += convertToString(hours);
            return new PatternObject(str, pattern.substring(2));

        } else if (pattern.startsWith("Hth")) {

            str += numberToOrdinal(hours);
            return new PatternObject(str, pattern.substring(3));
        } else if (pattern.startsWith("HTH")) {

            str += numberToOrdinal(hours).toUpperCase();
            return new PatternObject(str, pattern.substring(3));
        }
        str += hours;
        return new PatternObject(str, pattern.substring(1));
    }

    private static PatternObject expandedHours(String str, String pattern, Calendar date) {

        int hours = date.get(Calendar.HOUR_OF_DAY);
        if (hours == 0)
            hours = 24;

        if (pattern.startsWith("kk")) {
            str += convertToString(hours);
            return new PatternObject(str, pattern.substring(2));
        } else if (pattern.startsWith("kth")) {
            str += numberToOrdinal(hours);
            return new PatternObject(str, pattern.substring(3));
        } else if (pattern.startsWith("kTH")) {
            str += numberToOrdinal(hours).toUpperCase();
            return new PatternObject(str, pattern.substring(3));
        }
        str += hours;
        return new PatternObject(str, pattern.substring(1));
    }

    private static PatternObject justMinutes(String str, String pattern, Calendar date) {

        int minutes = date.get(Calendar.MINUTE);

        if (pattern.startsWith("mm")) {

            str += convertToString(minutes);
            return new PatternObject(str, pattern.substring(2));
        }

        else if (pattern.startsWith("mth") || pattern.startsWith("mTH")) {

            boolean hasUpperCase = pattern.startsWith("mTH");

            str += hasUpperCase
                    ? numberToOrdinal(minutes).toUpperCase()
                    : numberToOrdinal(minutes);
            return new PatternObject(str, pattern.substring(3));
        }

        str += convertToString(minutes);

        return new PatternObject(str, pattern.substring(1));
    }

    private static PatternObject justSeconds(String str, String pattern, Calendar date) {

        if (pattern.startsWith("ss")) {

            str = str + convertToString(date.get(Calendar.SECOND));
            return new PatternObject(str, pattern.substring(2));

        } else if (pattern.startsWith("sth")) {

            str += numberToOrdinal(date.get(Calendar.SECOND));
            return new PatternObject(str, pattern.substring(3));
        }
        str += date.get(Calendar.SECOND);
        return new PatternObject(str, pattern.substring(1));

    }

    private static PatternObject justOffset(String str, String pattern, Calendar date) {

        int offset = (date.get(Calendar.ZONE_OFFSET) + date.get(Calendar.DST_OFFSET)) / (60 * 1000 * -1);

        boolean isNegative = offset < 0;
        offset = Math.abs(offset);

        int hours = offset / 60;
        int minutes = offset % 60;

        if (pattern.startsWith("ZZ")) {

            str += (isNegative ? "-" : "+") +
                    convertToString(hours) +
                    convertToString(minutes);

            return new PatternObject(str, pattern.substring(2));
        }

        str += (isNegative ? "-" : "+") +
                convertToString(hours) +
                ":" +
                convertToString(minutes);

        return new PatternObject(str, pattern.substring(1));
    }

    private static PatternObject justYear(String str, String pattern, Calendar date) {

        String yearString = String.valueOf(date.get(Calendar.YEAR));

        if (pattern.toUpperCase().startsWith("YYYY")) {
            str = str + yearOffset[yearString.length() - 1] + yearString;
            return new PatternObject(str, pattern.substring(4));
        }

        if (pattern.toUpperCase().startsWith("YY")) {
            str += yearString.substring(2);
            return new PatternObject(str, pattern.substring(2));
        }

        str += yearString;
        return new PatternObject(str, pattern.substring(1));
    }

    private static String padStart(String str, int maxlength, String ch) { // check this once

        StringBuilder sb = new StringBuilder();
        int diff = maxlength - str.length();
        int patternLength = ch.length();

        while (diff >= 0 || diff < patternLength) {
            sb.append(ch);
            diff -= patternLength;
        }
        sb.append(str);
        return sb.toString();
    }

    private static String padEnd(String str, int maxlength, String ch) { // check this once

        StringBuilder sb = new StringBuilder();
        sb.append(str);

        if (str.length() == maxlength)
            return sb.toString();

        int diff = maxlength - str.length();

        while (diff >= 0) {
            sb.append(ch);
            diff -= ch.length();
        }

        return sb.toString();
    }

    private static String convertToString(int num) {

        return num < 10 ? "0" + num : String.valueOf(num);

    }

    private static String convertMillisToString(int num) {

        if (num < 10)
            return "00" + num;
        else if (num < 100)
            return "0" + num;
        return String.valueOf(num);
    }

    private static PatternObject weekOfTheYear(String str, String pattern, Calendar date) {

        int weekOfTheYear = 0;
        int dayOfTheYear = 0;
        int year = date.get(Calendar.YEAR);
        int month = date.get(Calendar.MONTH);
        int day = date.get(Calendar.DAY_OF_WEEK);

        for (int i = 0; i < month; i++) {

            dayOfTheYear += new Date(year, i + 1, 0).getDate();

        }
        dayOfTheYear += day;
        weekOfTheYear = (dayOfTheYear / 7) + 1;

        if (pattern.toUpperCase().startsWith("WW")) {
            str += convertToString(weekOfTheYear);
            return new PatternObject(str, pattern.substring(2));

        }

        if (pattern.toUpperCase().startsWith("WTH")) {
            str += pattern.substring(1, 3).startsWith("TH")
                    ? numberToOrdinal(weekOfTheYear).toUpperCase()
                    : numberToOrdinal(weekOfTheYear);

            return new PatternObject(str, pattern.substring(3));
        }

        return new PatternObject(str + weekOfTheYear, pattern.substring(1));
    }

    private static String numberToOrdinal(int num) {

        if ((num % 10 > 3) || (num % 100) - (num % 10) != 10)
            return num + DAYS_OF_MONTH_SUFFIX[0];

        return num + DAYS_OF_MONTH_SUFFIX[num % 10];
    }

    private static List<String> patternSplitting(String str) {

        List<String> arr = new ArrayList<>();

        int from = 0;
        int i = 0;

        for (; i < str.length(); i++) { // NOSONAR

            if (str.charAt(i) != '\'')
                continue;

            if (i < str.length() - 1 && str.charAt(i + 1) == '\'') {
                i++;
                continue;
            }

            if (from == 0 && i == 0) {

                if (str.length() == 1) {
                    arr.add(str);
                    return arr;
                }

                from = 1;
                continue;
            }
            arr.add(str.substring(arr.isEmpty() ? from : from + 1, i));
            from = i;
        }

        if (arr.isEmpty()) {
            arr.add(str);
            return arr;
        }

        if (from != str.length() - 1) {
            arr.add(str.substring(from + 1));
            return arr;
        }

        return arr;
    }

    public static void dateFromFormattedString(String dateString, String formatString) {

        List<String> patterns = patternSplitting(formatString);

//        let date: any = {};

        for (int i = 0; i < patterns.size(); i++) {

            String pattern = patterns.get(i).replace("\'\'", "'");

            if (i % 2 == 1) {
                dateString = dateString.substring(pattern.length());
                continue;
            }

            while (pattern.length() != 0) {

//              const funArray = VALUE_TOKEN_RULES[pattern[0]];
//              if (!funArray) {
//                pattern = pattern.slice(1);
//                dateString = dateString.slice(1);
//                continue;
//              }
//
//              ({ date, pattern, dateString } =
//                typeof funArray === "function"
//                  ? funArray(dateString, pattern, date)
//                  : parseWithRules(
//                      dateString,
//                      pattern,
//                      date,
//                      VALUE_TOKEN_RULES[pattern[0]]
//                    ));
            }
        }
    }
}
