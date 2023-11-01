package com.fincity.nocode.kirun.engine.util.date;

import java.util.Calendar;
import java.util.TimeZone;

import com.google.gson.JsonArray;

public class DateCompareUtil {

    public static final String YEAR = "year";

    public static final String MONTH = "month";

    public static final String DAY = "day";

    public static final String HOUR = "hour";

    public static final String MINUTE = "minute";

    public static final String SECOND = "second";

    private DateCompareUtil() {

    }

    public static boolean inBetween(String firstDate, String secondDate, String thirdDate, JsonArray fields) {

        return compare(firstDate, thirdDate, "before", fields) && compare(secondDate, thirdDate, "after", fields);
    }

    public static boolean compare(String firstDate, String secondDate, String operationName, JsonArray fields) {

        Calendar firstCal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

        firstCal.setTimeInMillis(GetTimeInMillisUtil.getEpochTime(firstDate));

        Calendar secondCal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

        secondCal.setTimeInMillis(GetTimeInMillisUtil.getEpochTime(secondDate));

        boolean equal = true;

        for (int i = 0; i < fields.size(); i++) {

            equal = equal && compare(firstCal, secondCal, operationName, fields.get(i).getAsString());
        }

        return equal;

    }

    private static boolean compare(Calendar firstDate, Calendar secondDate, String operationName, String fieldName) {

        switch (fieldName) {

            case YEAR:
                return checkFields(firstDate, secondDate, operationName, Calendar.YEAR);

            case MONTH:
                return checkFields(firstDate, secondDate, operationName, Calendar.YEAR, Calendar.MONTH);

            case DAY:
                return checkFields(firstDate, secondDate, operationName, Calendar.YEAR, Calendar.MONTH,
                        Calendar.DAY_OF_MONTH);

            case HOUR:
                return checkFields(firstDate, secondDate, operationName, Calendar.YEAR, Calendar.MONTH,
                        Calendar.DAY_OF_MONTH,
                        Calendar.HOUR_OF_DAY);

            case MINUTE:
                return checkFields(firstDate, secondDate, operationName, Calendar.YEAR, Calendar.MONTH,
                        Calendar.DAY_OF_MONTH,
                        Calendar.HOUR_OF_DAY, Calendar.HOUR_OF_DAY);

            case SECOND:
                return checkFields(firstDate, secondDate, operationName, Calendar.YEAR, Calendar.MONTH,
                        Calendar.DAY_OF_MONTH,
                        Calendar.HOUR_OF_DAY, Calendar.HOUR_OF_DAY, Calendar.SECOND);

            default:
                return false;
        }

    }

    private static boolean checkFields(Calendar firstDate, Calendar secondDate, String operationName, int... fields) {

        int fieldsLength = fields.length;

        if (fieldsLength == 0)
            return false;

        int equalLength = 0;

        for (int i = 0; i < fieldsLength; i++) {

            if (checkEquality(firstDate, secondDate, operationName, fields[i]))
                equalLength++;

            else
                return false;
        }

        return equalLength == fieldsLength;
    }

    private static boolean checkEquality(Calendar firstDate, Calendar secondDate, String operation, int field) {

        if (operation.equals("same"))
            return firstDate.get(field) == secondDate.get(field);

        else if (operation.equals("before"))
            return firstDate.get(field) < secondDate.get(field);

        else if (operation.equals("after"))
            return firstDate.get(field) > secondDate.get(field);

        return false;

    }

}
