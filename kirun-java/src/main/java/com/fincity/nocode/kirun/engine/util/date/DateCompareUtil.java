package com.fincity.nocode.kirun.engine.util.date;

import java.util.Calendar;
import java.util.TimeZone;

import com.google.gson.JsonArray;

public class DateCompareUtil {

    private DateCompareUtil() {

    }

    public static final String YEAR = "years";

    public static final String MONTH = "months";

    public static final String DAY = "days";

    public static final String HOUR = "hours";

    public static final String MINUTE = "minutes";

    public static final String SECOND = "seconds";

    public static final String MILLIS = "millis";

    public static final String BEFORE = "before";

    public static final String AFTER = "after";

    public static boolean inBetween(String firstDate, String secondDate, String thirdDate, JsonArray fields) {

        return compare(firstDate, thirdDate, BEFORE, fields) && compare(secondDate, thirdDate, AFTER, fields);
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
                        Calendar.HOUR_OF_DAY, Calendar.MINUTE);

            case SECOND:
                return checkFields(firstDate, secondDate, operationName, Calendar.YEAR, Calendar.MONTH,
                        Calendar.DAY_OF_MONTH,
                        Calendar.HOUR_OF_DAY, Calendar.MINUTE, Calendar.SECOND);

            case MILLIS:
                return checkFields(firstDate, secondDate, operationName, Calendar.YEAR, Calendar.MONTH,
                        Calendar.DAY_OF_MONTH,
                        Calendar.HOUR_OF_DAY, Calendar.MINUTE, Calendar.SECOND, Calendar.MILLISECOND);

            default:
                return false;
        }

    }

    private static boolean checkFields(Calendar firstDate, Calendar secondDate, String operationName, int... fields) {

        int fieldsLength = fields.length;

        if (fieldsLength == 0)
            return false;

        int equalLength = 0;
        int firstDateTotal = 0;
        int secondDateTotal = 0;

        for (int i = 0; i < fieldsLength; i++) {

            firstDateTotal += firstDate.get(fields[i]) * (i + 1);

            secondDateTotal += secondDate.get(fields[i]) * (i + 1);

            if ((i < fieldsLength - 1) && checkEquality(firstDateTotal, secondDateTotal, operationName)
                    || (i == fieldsLength - 1)
                            && checkEqualityForLastField(firstDate, secondDate, operationName, fields[i]))

                equalLength++;

            else
                break;

        }

        return equalLength == fieldsLength;
    }

    private static boolean checkEquality(int firstDateTotal, int secondDateTotal, String operation) {

        if (operation.equals("same"))
            return firstDateTotal == secondDateTotal;

        else if (operation.equals(BEFORE))
            return firstDateTotal <= secondDateTotal;

        else if (operation.equals(AFTER))
            return firstDateTotal >= secondDateTotal;

        return false;

    }

    private static boolean checkEqualityForLastField(Calendar firstDate, Calendar secondDate, String operation,
            int field) {

        if (operation.equals("same"))
            return firstDate.get(field) == secondDate.get(field);

        else if (operation.equals(BEFORE))
            return firstDate.get(field) < secondDate.get(field);

        else if (operation.equals(AFTER))
            return firstDate.get(field) > secondDate.get(field);

        return false;

    }

}
