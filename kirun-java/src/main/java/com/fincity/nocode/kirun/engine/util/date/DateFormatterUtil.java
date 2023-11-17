package com.fincity.nocode.kirun.engine.util.date;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.fincity.nocode.kirun.engine.util.stream.TriFunction;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

public class DateFormatterUtil {

    private DateFormatterUtil() {

    }

    @Data
    @NoArgsConstructor
    public static class DateObject {
        private Long epoch;
        private Integer year;
        private Integer monthIndex;
        private Integer quarterIndex;
        private Integer date;
        private String dateOfTheYear;
        private String weekOfTheYear;
        private Integer era;
        private Integer hours;
        private Boolean k;
        private Boolean h;
        private Integer amOrPm;
        private Integer offset;
        private String resultKey;
        private Integer minutes;
        private Integer seconds;
        private Integer millis;

        public DateObject(DateObject dateObject) {

            this.epoch = dateObject.epoch == null ? null : dateObject.epoch;
            this.year = dateObject.year == null ? null : dateObject.year;
            this.monthIndex = dateObject.monthIndex == null ? null : dateObject.monthIndex;
            this.quarterIndex = dateObject.quarterIndex == null ? null : dateObject.quarterIndex;
            this.date = dateObject.date == null ? null : dateObject.date;
            this.dateOfTheYear = dateObject.dateOfTheYear == null ? null : dateObject.dateOfTheYear;
            this.weekOfTheYear = dateObject.weekOfTheYear == null ? null : dateObject.weekOfTheYear;
            this.era = dateObject.era == null ? null : dateObject.era;
            this.hours = dateObject.hours == null ? null : dateObject.hours;
            this.k = dateObject.k == null ? null : dateObject.k;
            this.h = dateObject.h == null ? null : dateObject.h;
            this.amOrPm = dateObject.amOrPm == null ? null : dateObject.amOrPm;
            this.offset = dateObject.offset == null ? null : dateObject.offset;
            this.resultKey = dateObject.resultKey == null ? null : dateObject.resultKey;
            this.minutes = dateObject.minutes == null ? null : dateObject.minutes;
            this.seconds = dateObject.seconds == null ? null : dateObject.seconds;
            this.millis = dateObject.millis == null ? null : dateObject.millis;

        }
    }

    @Data
    @AllArgsConstructor
    public static class FormattedDateObject {
        private DateObject date;
        private String pattern;
        private String dateString;
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

    @Data
    @NoArgsConstructor
    @Accessors(chain = true)
    public static class Rules {

        String key;
        List<String> values;
        Boolean caseInsensitive;
        Boolean notInteger;
        String resultKey;
        Integer length;
        Boolean hasTh;
        List<Integer> range;
        Integer subtract;
        Function<Integer, Integer> logic;

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

    private static Map<String, Rules[]> valueTokenRules = Map.ofEntries(
            Map.entry("M", createRules('M')),
            Map.entry("Q", createRules('Q')),
            Map.entry("D", createRules('D')),
            Map.entry("d", createRules('d')),
            Map.entry("W", createRules('W')),
            Map.entry("w", createRules('w')),
            Map.entry("Y", createRules('Y')),
            Map.entry("y", createRules('y')),
            Map.entry("N", createRules('N')),
            Map.entry("A", createRules('A')),
            Map.entry("a", createRules('a')),
            Map.entry("h", createRules('h')),
            Map.entry("H", createRules('H')),
            Map.entry("k", createRules('k')),
            Map.entry("m", createRules('m')),
            Map.entry("s", createRules('s')),
            Map.entry("S", createRules('S'))

    );

    private static Map<String, TriFunction<String, String, DateObject, FormattedDateObject>> valueTokenFunctions = Map
            .ofEntries(
                    Map.entry("Z", (dateString, pattern, date) -> {

                        Matcher x;

                        if (pattern.startsWith("ZZ")) {
                            x = Pattern.compile("^([+-])(\\d{2})(\\d{2})").matcher(dateString);
                            pattern = pattern.substring(2);
                        } else {
                            x = Pattern.compile("^([+-])(\\d{2}):(\\d{2})").matcher(dateString);
                            pattern = pattern.substring(1);
                        }

                        if (x == null || !x.find())
                            return new FormattedDateObject(date, pattern.substring(1), dateString);

                        boolean isNegative = x.group(1).equals("-");

                        int hours = Integer.parseInt(x.group(2));
                        int minutes = Integer.parseInt(x.group(3));

                        int offset = hours * 60 + minutes;

                        if (isNegative)

                            offset = offset * -1;

                        date.setOffset(offset);

                        System.out.println(date);

                        return new FormattedDateObject(date, pattern.substring(1),
                                dateString.substring(x.group(0).length()));
                    }),

                    Map.entry("x", (dateString, pattern, date) -> {

                        Matcher x = Pattern.compile("^-?\\d+").matcher(dateString);

                        if (!x.find())
                            return new FormattedDateObject(date, pattern.substring(1), dateString);

                        DateObject d = new DateObject(date);
                        d.setEpoch(Long.parseLong(x.group(0)) * 1000);

                        return new FormattedDateObject(d, pattern.substring(1),
                                dateString.substring(x.group(0).length()));

                    }),

                    Map.entry("X", (dateString, pattern, date) -> {

                        Matcher matcher = Pattern.compile("^-?\\d+").matcher(dateString);

                        if (!matcher.find())
                            return new FormattedDateObject(date, pattern.substring(1), dateString);

                        DateObject d = new DateObject(date);
                        d.setEpoch(Long.valueOf(matcher.group(0)));

                        return new FormattedDateObject(d, pattern.substring(1),
                                dateString.substring(matcher.group(0).length()));
                    }));

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

    private static List<String> patternSplitting(String str) { // NOSONAR

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

    private static Rules[] createRules(char ch) { // NOSONAR

        String hours = "hours";

        switch (ch) {
            case 'M': {

                String monthIndex = "monthIndex";

                Rules[] rules = { // NOSONAR

                        new Rules().setKey("MMMM")
                                .setValues(Arrays.stream(MONTHS_OF_YEAR).map(String::toUpperCase)
                                        .toList())
                                .setCaseInsensitive(true)
                                .setNotInteger(true)
                                .setResultKey(monthIndex),

                        new Rules().setKey("MMM")
                                .setLength(3)
                                .setValues(Arrays.stream(MONTHS_OF_YEAR).map(e -> e.substring(0, 3).toUpperCase())
                                        .toList())
                                .setCaseInsensitive(true)
                                .setNotInteger(true)
                                .setResultKey(monthIndex),

                        new Rules().setKey("Mth")
                                .setHasTh(true)
                                .setRange(List.of(1, 12))
                                .setSubtract(1)
                                .setResultKey(monthIndex),

                        new Rules().setKey("MTH")
                                .setHasTh(true)
                                .setRange(List.of(1, 12))
                                .setSubtract(1)
                                .setResultKey(monthIndex),

                        new Rules().setKey("MM")
                                .setRange(List.of(1, 12))
                                .setSubtract(1)
                                .setResultKey(monthIndex),

                        new Rules().setKey("M")
                                .setRange(List.of(1, 12))
                                .setSubtract(1)
                                .setResultKey(monthIndex),

                };
                return rules;
            }

            case 'Q': {

                String quarterIndex = "quarterIndex";

                Rules[] rules = { // NOSONAR

                        new Rules().setKey("QQ")
                                .setRange(List.of(1, 4))
                                .setSubtract(1)
                                .setResultKey(quarterIndex)
                                .setLength(2),

                        new Rules().setKey("Qth")
                                .setHasTh(true)
                                .setRange(List.of(1, 4))
                                .setSubtract(1)
                                .setResultKey(quarterIndex),

                        new Rules().setKey("QTH")
                                .setHasTh(true)
                                .setRange(List.of(1, 4))
                                .setSubtract(1)
                                .setResultKey(quarterIndex),

                        new Rules().setKey("Q")
                                .setRange(List.of(1, 4))
                                .setSubtract(1)
                                .setResultKey(quarterIndex),

                };
                return rules;
            }

            case 'D': {

                String dateOfTheYear = "dateOfTheYear";
                String onlyDate = "date";

                Rules[] rules = { // NOSONAR

                        new Rules().setKey("DDDD")
                                .setLength(3)
                                .setRange(List.of(1, 366))
                                .setResultKey(dateOfTheYear),

                        new Rules().setKey("DDDth")
                                .setHasTh(true)
                                .setRange(List.of(1, 366))
                                .setResultKey(dateOfTheYear),

                        new Rules().setKey("DDDTH")
                                .setHasTh(true)
                                .setRange(List.of(1, 366))
                                .setResultKey(dateOfTheYear),

                        new Rules().setKey("DDD")
                                .setRange(List.of(1, 366))
                                .setResultKey(dateOfTheYear),

                        new Rules().setKey("DD")
                                .setRange(List.of(1, 31))
                                .setResultKey(dateOfTheYear)
                                .setLength(2),

                        new Rules().setKey("Dth")
                                .setHasTh(true)
                                .setRange(List.of(1, 35))
                                .setResultKey(onlyDate),

                        new Rules().setKey("DTH")
                                .setHasTh(true)
                                .setRange(List.of(1, 35))
                                .setResultKey(onlyDate),

                        new Rules().setKey("D")
                                .setRange(List.of(1, 31))
                                .setResultKey(onlyDate),

                };

                return rules;
            }

            case 'd': {

                String dayOfTheWeek = "dayOfTheWeek";

                Rules[] rules = { // NOSONAR

                        new Rules().setKey("dddd")
                                .setValues(Arrays.stream(MONTHS_OF_YEAR).map(String::toUpperCase).toList())
                                .setCaseInsensitive(true)
                                .setNotInteger(true)
                                .setResultKey(dayOfTheWeek),

                        new Rules().setKey("ddd")
                                .setLength(3)
                                .setValues(Arrays.stream(MONTHS_OF_YEAR).map(e -> e.substring(0, 3).toUpperCase())
                                        .toList())
                                .setCaseInsensitive(true)
                                .setNotInteger(true)
                                .setResultKey(dayOfTheWeek),

                        new Rules().setKey("dd")
                                .setLength(2)
                                .setValues(Arrays.stream(MONTHS_OF_YEAR).map(e -> e.substring(0, 2).toUpperCase())
                                        .toList())
                                .setCaseInsensitive(true)
                                .setNotInteger(true)
                                .setResultKey(dayOfTheWeek),

                        new Rules().setKey("dth")
                                .setHasTh(true)
                                .setRange(List.of(1, 7))
                                .setSubtract(1)
                                .setResultKey(dayOfTheWeek),

                        new Rules().setKey("dTH")
                                .setHasTh(true)
                                .setRange(List.of(1, 7))
                                .setSubtract(1)
                                .setResultKey(dayOfTheWeek),

                        new Rules().setKey("d")
                                .setRange(List.of(1, 7))
                                .setSubtract(1)
                                .setResultKey(dayOfTheWeek),

                };

                return rules;
            }

            case 'W': {

                String weekOfTheYear = "weekOfTheYear";

                Rules[] rules = { // NOSONAR

                        new Rules().setKey("WW")
                                .setRange(List.of(1, 53))
                                .setLength(2)
                                .setResultKey(weekOfTheYear),

                        new Rules().setKey("Wth")
                                .setRange(List.of(1, 53))
                                .setHasTh(true)
                                .setResultKey(weekOfTheYear),

                        new Rules().setKey("WTH")
                                .setRange(List.of(1, 53))
                                .setHasTh(true)
                                .setResultKey(weekOfTheYear),

                        new Rules().setKey("W")
                                .setRange(List.of(1, 53))
                                .setResultKey(weekOfTheYear),

                };

                return rules;
            }

            case 'w': {

                String weekOfTheYear = "weekOfTheYear";

                Rules[] rules = { // NOSONAR

                        new Rules().setKey("ww")
                                .setRange(List.of(1, 53))
                                .setLength(2)
                                .setResultKey(weekOfTheYear),

                        new Rules().setKey("wth")
                                .setRange(List.of(1, 53))
                                .setHasTh(true)
                                .setResultKey(weekOfTheYear),

                        new Rules().setKey("wTH")
                                .setRange(List.of(1, 53))
                                .setHasTh(true)
                                .setResultKey(weekOfTheYear),

                        new Rules().setKey("w")
                                .setRange(List.of(1, 53))
                                .setResultKey(weekOfTheYear),

                };

                return rules;

            }

            case 'Y': {

                String year = "year";

                Rules[] rules = { // NOSONAR

                        new Rules()
                                .setKey("YYYY")
                                .setLength(4)
                                .setResultKey(year),

                        new Rules()
                                .setKey("YY")
                                .setLength(2)
                                .setResultKey(year)
                                .setLogic(num -> num >= 70 ? 1900 + num : 2000 + num),

                        new Rules()
                                .setKey("Y")
                                .setResultKey(year)

                };

                return rules;
            }

            case 'y': {

                String year = "year";

                Rules[] rules = { // NOSONAR

                        new Rules()
                                .setKey("yyyy")
                                .setLength(4)
                                .setResultKey(year),

                        new Rules()
                                .setKey("yy")
                                .setLength(2)
                                .setResultKey(year)
                                .setLogic(num -> num >= 70 ? 1900 + num : 2000 + num),

                        new Rules()
                                .setKey("y")
                                .setResultKey(year)

                };

                return rules;

            }

            case 'N': {

                String era = "era";

                Rules[] rules = { // NOSONAR

                        new Rules().setKey("NNNN")
                                .setValues(List.of("BEFORE COMMON ERA", "COMMON ERA"))
                                .setCaseInsensitive(true)
                                .setNotInteger(true)
                                .setResultKey(era)
                                .setLogic(num -> num == 0 ? -1 : 1),

                        new Rules().setKey("NNN")
                                .setValues(List.of("BCE", "CE"))
                                .setCaseInsensitive(true)
                                .setNotInteger(true)
                                .setResultKey(era)
                                .setLogic(num -> num == 0 ? -1 : 1),

                        new Rules().setKey("NN")
                                .setValues(List.of("BCE", "CE"))
                                .setCaseInsensitive(true)
                                .setNotInteger(true)
                                .setResultKey(era)
                                .setLogic(num -> num == 0 ? -1 : 1),

                        new Rules().setKey("N")
                                .setValues(List.of("BCE", "CE"))
                                .setCaseInsensitive(true)
                                .setNotInteger(true)
                                .setResultKey(era)
                                .setLogic(num -> num == 0 ? -1 : 1),

                };

                return rules;

            }

            case 'A': {

                Rules[] rules = { // NOSONAR

                        new Rules().setKey("A")
                                .setValues(List.of("AM", "PM"))
                                .setCaseInsensitive(true)
                                .setNotInteger(true)
                                .setResultKey("amOrPm")

                };

                return rules;
            }

            case 'a': {

                Rules[] rules = { // NOSONAR

                        new Rules().setKey("a")
                                .setValues(List.of("am", "pm"))
                                .setCaseInsensitive(true)
                                .setNotInteger(true)
                                .setResultKey("amOrPm")

                };

                return rules;
            }

            case 'h': {

                Rules[] rules = { // NOSONAR

                        new Rules().setKey("hh")
                                .setRange(List.of(1, 12))
                                .setLength(2)
                                .setResultKey(hours),

                        new Rules().setKey("hth")
                                .setHasTh(true)
                                .setRange(List.of(1, 12))
                                .setResultKey(hours),

                        new Rules().setKey("hTH")
                                .setHasTh(true)
                                .setRange(List.of(1, 12))
                                .setResultKey(hours),

                        new Rules().setKey("h")
                                .setRange(List.of(1, 12))
                                .setResultKey(hours)

                };

                return rules;
            }

            case 'H': {

                Rules[] rules = { // NOSONAR

                        new Rules().setKey("HH")
                                .setRange(List.of(0, 24))
                                .setLength(2)
                                .setResultKey(hours),

                        new Rules().setKey("Hth")
                                .setHasTh(true)
                                .setRange(List.of(0, 23))
                                .setResultKey(hours),

                        new Rules().setKey("HTH")
                                .setHasTh(true)
                                .setRange(List.of(0, 23))
                                .setResultKey(hours),

                        new Rules().setKey("H")
                                .setRange(List.of(0, 23))
                                .setResultKey(hours)

                };

                return rules;

            }

            case 'k': {

                Rules[] rules = { // NOSONAR

                        new Rules().setKey("kk")
                                .setRange(List.of(1, 24))
                                .setLength(2)
                                .setResultKey(hours),

                        new Rules().setKey("kth")
                                .setHasTh(true)
                                .setRange(List.of(1, 24))
                                .setResultKey(hours),

                        new Rules().setKey("kTH")
                                .setHasTh(true)
                                .setRange(List.of(1, 24))
                                .setResultKey(hours),

                        new Rules().setKey("k")
                                .setRange(List.of(1, 24))
                                .setResultKey(hours)

                };

                return rules;

            }

            case 'm': {

                String minutes = "minutes";

                Rules[] rules = { // NOSONAR

                        new Rules().setKey("mm")
                                .setRange(List.of(0, 59))
                                .setLength(2)
                                .setResultKey(minutes),

                        new Rules().setKey("mth")
                                .setHasTh(true)
                                .setRange(List.of(0, 59))
                                .setResultKey(minutes),

                        new Rules().setKey("mTH")
                                .setHasTh(true)
                                .setRange(List.of(0, 59))
                                .setResultKey(minutes),

                        new Rules().setKey("m")
                                .setRange(List.of(0, 59))
                                .setResultKey(minutes),

                };

                return rules;

            }

            case 's': {

                String seconds = "seconds";

                Rules[] rules = { // NOSONAR

                        new Rules().setKey("ss")
                                .setRange(List.of(0, 59))
                                .setLength(2)
                                .setResultKey(seconds),

                        new Rules().setKey("sth")
                                .setHasTh(true)
                                .setRange(List.of(0, 59))
                                .setResultKey(seconds),

                        new Rules().setKey("sTH")
                                .setHasTh(true)
                                .setRange(List.of(0, 59))
                                .setResultKey(seconds),

                        new Rules().setKey("s")
                                .setRange(List.of(0, 59))
                                .setResultKey(seconds),

                };

                return rules;

            }

            case 'S': {

                String millis = "millis";

                Rules[] rules = { // NOSONAR

                        new Rules().setKey("SSSSSSSSS")
                                .setRange(List.of(0, 999))
                                .setLength(9)
                                .setResultKey(millis)
                                .setLogic(num -> (int) Math.round(num / Math.pow(10, -4))),

                        new Rules().setKey("SSSSSSSS")
                                .setRange(List.of(0, 999))
                                .setLength(8)
                                .setResultKey(millis)
                                .setLogic(num -> (int) Math.round(num / Math.pow(10, -5))),

                        new Rules().setKey("SSSSSSS")
                                .setRange(List.of(0, 999))
                                .setLength(7)
                                .setResultKey(millis)
                                .setLogic(num -> (int) Math.round(num / Math.pow(10, -4))),

                        new Rules().setKey("SSSSSS")
                                .setRange(List.of(0, 999))
                                .setLength(6)
                                .setResultKey(millis)
                                .setLogic(num -> (int) Math.round(num / Math.pow(10, -3))),

                        new Rules().setKey("SSSSS")
                                .setRange(List.of(0, 999))
                                .setLength(5)
                                .setResultKey(millis)
                                .setLogic(num -> (int) Math.round(num / Math.pow(10, -2))),

                        new Rules().setKey("SSSS")
                                .setRange(List.of(0, 999))
                                .setLength(4)
                                .setResultKey(millis)
                                .setLogic(num -> (int) Math.round(num / Math.pow(10, -1))),

                        new Rules().setKey("SSS")
                                .setRange(List.of(0, 999))
                                .setLength(3)
                                .setResultKey(millis),

                        new Rules().setKey("SS")
                                .setRange(List.of(0, 999))
                                .setLength(2)
                                .setResultKey(millis)
                                .setLogic(num -> num * 10),

                        new Rules().setKey("Sth")
                                .setHasTh(true)
                                .setRange(List.of(0, 999))
                                .setResultKey(millis),

                        new Rules().setKey("STH")
                                .setHasTh(true)
                                .setRange(List.of(0, 999))
                                .setResultKey(millis),

                        new Rules().setKey("S")
                                .setRange(List.of(0, 999))
                                .setLength(1)
                                .setResultKey(millis)
                                .setLogic(num -> num * 100)

                };

                return rules;

            }

            default:
                return new Rules[0];
        }

    }

    public static Date timeStampObjectToDate(DateObject obj) {

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, obj.getYear());
        cal.set(Calendar.MONTH, obj.getMonthIndex());
        cal.set(Calendar.DATE, obj.getDate());
        cal.set(Calendar.HOUR_OF_DAY, obj.getHours() != null ? obj.getHours() : 0);
        cal.set(Calendar.MINUTE, obj.getMinutes() != null ? obj.getMinutes() : 0);
        cal.set(Calendar.SECOND, obj.getSeconds() != null ? obj.getSeconds() : 0);
        cal.set(Calendar.MILLISECOND, obj.getMillis() != null ? obj.getMillis() : 0);

        return cal.getTime();
    }

    private static Date processParsedDate(DateObject date) { // NOSONAR

        if (date.getEpoch() != null)
            return new Date(date.getEpoch());

        Date currentDate = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(currentDate);

        if (date.getYear() == null)
            date.setYear(cal.get(Calendar.YEAR));
        if (date.getMonthIndex() == null) {
            if (date.getQuarterIndex() != null)
                date.setMonthIndex(date.getQuarterIndex() * 3);
            else
                date.setMonthIndex(cal.get(Calendar.MONTH));
        }

        if (date.getDate() == null) {
            if (date.getDateOfTheYear() != null || date.getWeekOfTheYear() != null) {

                int x = date.getDateOfTheYear() != null
                        ? Integer.parseInt(date.getDateOfTheYear())
                        : Integer.parseInt(date.getWeekOfTheYear()) * 7;

                for (int i = 0; i < 12; i++) {
                    int days = new Date(date.year, i + 1, 0).getDate();

                    if (x <= days) {
                        date.setMonthIndex(i);
                        date.setDate(x);
                        break;
                    }
                    x -= days;
                }
            }
            if (date.getQuarterIndex() != null) {
                date.setDate(1);
            } else
                date.setDate(currentDate.getDate());

        }

        if (date.getEra() != null && date.getEra() == -1 && date.getYear() > 0)
            date.setYear(date.getYear() * -1);

        if (date.getHours() == null)
            date.setHours(0);

        if (Boolean.TRUE.equals(date.k) && date.hours == 24)
            date.setHours(0);

        if (Boolean.TRUE.equals(date.h)) {
            if (date.amOrPm == 1)
                date.setHours(date.getHours() + 12);
            if (date.amOrPm == 0 && date.hours == 12)
                date.setHours(0);
        }

        Date dobj = timeStampObjectToDate(date);

        if (date.offset == null)
            return dobj;

        if (date.offset * -1 == dobj.getTimezoneOffset())
            return dobj;

        int offset = date.offset * 60 * 1000 * -1;

        return new Date(dobj.getTime() - dobj.getTimezoneOffset() * 60 * 1000 + offset);

    }

    private static FormattedDateObject parseWithRules(String dateString, String pattern, DateObject date, // NOSONAR
            Rules... rules) {

        final String refPattern = pattern;

        if (pattern.isEmpty())
            return new FormattedDateObject(date, pattern, dateString);

        Optional<Rules> rule = Arrays.stream(rules).filter(e -> refPattern.startsWith(e.getKey())).findFirst();

        if (rule.isEmpty())
            return new FormattedDateObject(date, pattern, dateString);

        Rules firstRule = rule.get();

        if (firstRule.getKey().charAt(0) == 'h')
            date.setH(true);

        pattern = pattern.substring(firstRule.getKey().length());

        if (firstRule.getNotInteger() != null) {

            List<String> values = firstRule.getValues();

            final String tempDateString = dateString;

            OptionalInt ind = IntStream.range(0, values.size())
                    .filter(e -> {

                        String value = values.get(e);

                        return firstRule.getCaseInsensitive() ? tempDateString.toUpperCase().startsWith(value)
                                : tempDateString.startsWith(value);
                    }).findFirst();

            if (ind.isEmpty())
                return new FormattedDateObject(date, pattern, dateString);

            date.setResultKey(String.valueOf(ind.getAsInt()));

            return new FormattedDateObject(date, pattern, dateString.substring(values.get(ind.getAsInt()).length()));

        }

        String stringValue;

        if (firstRule.getLength() != null) {

            stringValue = dateString.substring(0, firstRule.getLength());

            if (stringValue.length() != firstRule.getLength())

                return new FormattedDateObject(date, pattern, "");

        }

        else if (firstRule.getHasTh() != null) {

            Pattern pat = Pattern.compile("^(\\d+)(th|st|nd|rd)", Pattern.CASE_INSENSITIVE);
            Matcher match = pat.matcher(dateString);

            if (!match.find())
                return new FormattedDateObject(date, pattern, dateString);

            stringValue = match.group(1);
            dateString = dateString.substring(2);
        }

        else {

            Pattern pat = Pattern.compile("^\\d+");
            Matcher match = pat.matcher(dateString);

            if (!match.find())
                return new FormattedDateObject(date, pattern, dateString);

            stringValue = match.group(0);

        }

        dateString = dateString.substring(stringValue.length());

        Integer value = dateString.matches("\\d+") ? Integer.parseInt(stringValue) : -1;

        if (value == -1)
            return new FormattedDateObject(date, pattern, dateString);

        if (firstRule.getSubtract() != null)
            value -= firstRule.getSubtract();

        if (firstRule.getRange() != null &&
                (value < firstRule.getRange().get(0) || value > firstRule.getRange().get(1)))

            return new FormattedDateObject(date, pattern, dateString);

        if (firstRule.getLogic() != null)
            value = firstRule.getLogic().apply(value);

        date.setResultKey(value.toString());

        return new FormattedDateObject(date, pattern, dateString);

    }

    public static Date dateFromFormattedString(String dateString, String formatString) {

        List<String> patterns = patternSplitting(formatString);

        DateObject date = new DateObject();

        FormattedDateObject fdo = new FormattedDateObject(date, formatString, dateString);

        for (int i = 0; i < patterns.size(); i++) {

            String pattern = patterns.get(i).replace("\'\'", "'");

            if (i % 2 == 1) {
                dateString = dateString.substring(pattern.length());
                continue;
            }

            while (pattern.length() != 0) {

                String firstchar = pattern.substring(0, 1);

                boolean isDefinedRules = valueTokenRules.containsKey(firstchar);

                boolean isDefinedFunction = valueTokenFunctions.containsKey(firstchar);

                if (isDefinedRules) {

                    FormattedDateObject output = parseWithRules(dateString, pattern, date,
                            valueTokenRules.get(firstchar));

                    fdo.setDate(output.getDate());
                    fdo.setDateString(output.getDateString());
                    fdo.setPattern(output.getPattern());

                    date = output.getDate();
                    dateString = output.getDateString();
                    pattern = output.getPattern();
                }

                else if (isDefinedFunction) {

                    FormattedDateObject output = valueTokenFunctions.get(firstchar).apply(dateString, pattern, date);
                    fdo.setDate(output.getDate());
                    fdo.setDateString(output.getDateString());
                    fdo.setPattern(output.getPattern());

                    date = output.getDate();
                    dateString = output.getDateString();
                    pattern = output.getPattern();
                } else {
                    pattern = pattern.substring(1);
                    dateString = dateString.substring(1);
                }

            }
        }

        return processParsedDate(fdo.getDate());
    }
}
