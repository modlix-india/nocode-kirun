package com.fincity.nocode.kirun.engine.util.date;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fincity.nocode.kirun.engine.exception.KIRuntimeException;

public class IsValidISODateUtil {

    private IsValidISODateUtil() {

    }

    private static final Map<String, Integer> dayMonthMap = Map.ofEntries(

            Map.entry("01", 31),
            Map.entry("02", 28),
            Map.entry("03", 31),
            Map.entry("04", 30),
            Map.entry("05", 31),
            Map.entry("06", 30),
            Map.entry("07", 31),
            Map.entry("08", 31),
            Map.entry("09", 30),
            Map.entry("10", 31),
            Map.entry("11", 30),
            Map.entry("12", 31)

    );

    public static final Pattern isoPattern = Pattern.compile(
            "^(\\d{4})-(0[1-9]|1[0-2])-(0[1-9]|[1-2]\\d|3[0-1])T"
                    + "([0-1]\\d|2[0-3]):([0-5]\\d):([0-5]\\d)(Z|\\.\\d{3})"
                    + "(Z|([+-]([01]\\d|2[0-3]):([0-5]\\d)))?$"); // NO SONAR // Required for evaluating date time
                                                                  // object

    public static boolean checkValidity(String input) {

        Matcher matcher = getMatcher(input);

        if (!matcher.matches())
            return false;

        Integer date = Integer.valueOf(matcher.group(3));

        String month = matcher.group(2);

        if (date <= dayMonthMap.get(month))
            return true;

        return month.equals("02") && isLeapYear(Integer.valueOf(matcher.group(1))) && date == 29;

    }

    public static Matcher getMatcher(String input) {

        return isoPattern.matcher(input);
    }

    public static boolean isLeapYear(String input) {
        int year = getFullYear(input);
        return (year % 4 == 0 && year % 100 != 0) || year % 400 == 0;
    }

    public static boolean isLeapYear(int year) {
        return (year % 4 == 0 && year % 100 != 0) || year % 400 == 0;
    }

    public static int getFullYear(String input) {
        return Integer.valueOf(input.substring(0, 4));
    }

    public static int getMonth(String input) {
        return Integer.valueOf(input.substring(5, 7)) - 1;
    }

    public static int getDate(String input) {
        return Integer.valueOf(input.substring(8, 10));
    }

    public static int getHours(String input) {
        return Integer.valueOf(input.substring(11, 13));
    }

    public static int getMinutes(String input) {
        return Integer.valueOf(input.substring(14, 16));
    }

    public static int getSeconds(String input) {
        return Integer.valueOf(input.substring(17, 19));
    }

    public static int getMillis(String input) {
        return input.contains(".") ? Integer.valueOf(input.substring(20, 23)) : 0;
    }

    public static int setYear(String input, int value) {

        String updatedDate = input.replaceFirst("\\d{4}", String.valueOf(value));
        System.out.println(updatedDate);

        if (!checkValidity(updatedDate))
            throw new KIRuntimeException("Please provide valid value for year");

        return getFullYear(updatedDate);
    }
}
