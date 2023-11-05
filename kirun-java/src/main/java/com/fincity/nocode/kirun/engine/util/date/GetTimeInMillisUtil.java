package com.fincity.nocode.kirun.engine.util.date;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class GetTimeInMillisUtil {

    private GetTimeInMillisUtil() {

    }

    public static long getEpochTime(String inputDate) {

        DateTimeFormatter dtf = DateTimePatternUtil.getPattern();
        return ZonedDateTime.parse(inputDate, dtf).toInstant().toEpochMilli();

    }
}
