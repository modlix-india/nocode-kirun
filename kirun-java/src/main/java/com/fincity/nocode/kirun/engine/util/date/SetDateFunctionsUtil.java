package com.fincity.nocode.kirun.engine.util.date;

import java.time.ZonedDateTime;

import com.fincity.nocode.kirun.engine.exception.KIRuntimeException;

public class SetDateFunctionsUtil {

    private static int[] nonLeap = { 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };

    private static int[] leap = { 31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };

    private static String[] yearOffset = { "000", "00", "0", "" };

    private static String[] expandedYearOffset = { "00000", "0000", "000", "00", "0", "" };

    private SetDateFunctionsUtil() {

    }

    private static boolean checkFirstChar(String date) {
        char first = date.charAt(0);
        return first == '+' || first == '-';
    }

    private static boolean checkLeapYear(int year) {

        if (year % 4 != 0) {
            return false;
        } else if (year % 100 != 0) {
            return true;
        }

        return year % 400 == 0;
    }

    public static String setFullYear(String inputDate, int year) {

        if (year >= 275761 || year <= -271821)
            throw new KIRuntimeException("Given year cannot be set to year as it out of bounds");

        ZonedDateTime zdt = ZonedDateTime.parse(inputDate, DateTimePatternUtil.getPattern());

        int yearFromDate = zdt.getYear();
        int monthFromDate = zdt.getMonthValue();
        int date = zdt.getDayOfMonth();

        boolean hasSign = checkFirstChar(inputDate);
        String yearString = String.valueOf(year < 0 ? year * -1 : year);

        if (monthFromDate == 2 && date == 29 && checkLeapYear(yearFromDate))
            return year + inputDate;
        else if (year >= 0 && year <= 9999)
            return yearOffset[yearString.length() - 1] + yearString + inputDate.substring(hasSign ? 7 : 4);

        StringBuilder buf = new StringBuilder(year > 9999 ? "+" : "-");
        buf.append(expandedYearOffset[yearString.length() - 1]);
        buf.append(yearString);
        buf.append(inputDate.substring(hasSign ? 7 : 4));

        return buf.toString();

    }

    public static String setMonth(String inputDate, int addMonth) {

        ZonedDateTime zdt = ZonedDateTime.parse(inputDate, DateTimePatternUtil.getPattern());

        int convertedMonths = absMonthValue(addMonth % 12) + 1;
        int extraYears = absFloor(addMonth / 12);
        int yearFromDate = zdt.getYear();
        int years = yearFromDate + extraYears;
        int date = zdt.getDayOfMonth();
        int updateMonth = convertedMonths;

        switch (convertedMonths) {

            case 2:
                if (date >= 28) {
                    date = checkLeapYear(yearFromDate) ? date - 29 : date - 28;
                    updateMonth = convertedMonths + 1;
                }
                break;

            case 4, 6, 9, 11:

                if (date == 31) {
                    updateMonth = convertedMonths + 1;
                    date = 1;
                }
        }

        // check month with possible dates
//        let updateTimeStamp = parts[0];
//
//        if (updateTimeStamp.charAt(0) === '+' || updateTimeStamp.charAt(0) === '-')
//            updateTimeStamp =
//                inputDate.substring(0, 8) +
//                convertToString(updateMonth) +
//                '-' +
//                convertToString(date) +
//                inputDate.substring(13);
//        else
//            updateTimeStamp =
//                inputDate.substring(0, 5) +
//                convertToString(updateMonth) +
//                '-' +
//                convertToString(date) +
//                inputDate.substring(10);

        return setFullYear(inputDate, years);
    }

    public static int setDate(String inputDate, int addDays) {

        ZonedDateTime zdt = ZonedDateTime.parse(inputDate, DateTimePatternUtil.getPattern());

        int year = zdt.getYear();
        int month = zdt.getMonthValue();
        int day = zdt.getDayOfMonth();
        boolean flag = false;
        int[] daysInMonth = checkLeapYear(year) ? leap : nonLeap;

        if (addDays <= 0) {
            day = daysInMonth[month - 2 < 0 ? 11 : month - 2];
            month = month - 1 < 1 ? 12 : month - 1;
            year = month - 1 < 1 ? year - 1 : year;
        }

        // for zero value
        if (addDays == 0) {
            return day;
        }

        // for negative within the previous month
        if (addDays < 0)
            flag = true;

        if (flag && daysInMonth[month - 1] >= addDays * -1) {
            return day + addDays;
        }

        // positive within the month
        if (!flag && addDays <= daysInMonth[month - 1]) {
            return addDays;
        }

        if (!flag) {
            while (addDays > daysInMonth[month - 1]) {
                addDays -= daysInMonth[month - 1];
                month++;
                if (month - 1 > 11) {
                    month = 1;
                    year++;
                }
                daysInMonth = checkLeapYear(year) ? leap : nonLeap;
            }
        } else {
            while (addDays * -1 > daysInMonth[month - 1]) {
                addDays += daysInMonth[month - 1];
                month--;
                if (month - 1 < 0) {
                    month = 12;
                    year--;
                }
                daysInMonth = checkLeapYear(year) ? leap : nonLeap;
            }
        }

        if (addDays < 0)
            return daysInMonth[month - 1] + addDays;
        else
            return addDays;
    }

    public static String setHours(String inputDate, int addHours) {

        if (addHours < 0 || addHours > 24)
            throw new KIRuntimeException("Hours should be in the range of 0 and 23");

        String hours = convertToString(addHours);

        if (inputDate.charAt(0) == '+' || inputDate.charAt(0) == '-')
            return inputDate.substring(0, 14) + hours + inputDate.substring(16);

        return inputDate.substring(0, 11) + hours + inputDate.substring(13);
    }

    public static String setMinutes(String inputDate, int addMinutes) {

        if (addMinutes < 0 || addMinutes > 59)
            throw new KIRuntimeException("Minutes should be in the range of 0 and 59");

        String minutes = convertToString(addMinutes);

        if (inputDate.charAt(0) == '+' || inputDate.charAt(0) == '-')
            return inputDate.substring(0, 17) + minutes + inputDate.substring(19);

        return inputDate.substring(0, 14) + minutes + inputDate.substring(16);
    }

    public static String setSeconds(String inputDate, int addSeconds) {

        if (addSeconds < 0 || addSeconds > 59)
            throw new KIRuntimeException("Seconds should be in the range of 0 and 59");

        String seconds = convertToString(addSeconds);

        if (inputDate.charAt(0) == '+' || inputDate.charAt(0) == '-')
            return inputDate.substring(0, 20) + seconds + inputDate.substring(22);

        return inputDate.substring(0, 17) + seconds + inputDate.substring(19);
    }

    public static String setMilliSeconds(String inputDate, int addMillis) {

        if (addMillis < 0 || addMillis > 999)
            throw new KIRuntimeException("Milliseconds should be in the range of 0 and 999");

        String millis = convertMillisToString(addMillis);

        if (inputDate.charAt(0) == '+' || inputDate.charAt(0) == '-')
            return inputDate.substring(0, 23) + millis + inputDate.substring(26);

        return inputDate.substring(0, 20) + addMillis + inputDate.substring(23);
    }

    private static String convertMillisToString(int val) {
        if (val == 0)
            return "000";
        if (val < 10)
            return "00" + val;
        if (val < 100)
            return "0" + val;
        return String.valueOf(val);
    }

    private static String convertToString(int val) {
        return val < 10 ? "0" + val : String.valueOf(val);
    }

    private static int absMonthValue(int val) {
        return val < 0 ? 12 - Math.abs(val) : val;
    }

    private static int absFloor(float val) {
        return (int) Math.floor(val);
    }
}
