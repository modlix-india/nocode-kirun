package com.fincity.nocode.kirun.engine.util.date;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class DateTimePatternUtil {

    private DateTimePatternUtil() {

    }

    public static DateTimeFormatter getPattern(String date) {

        return date.contains("Z") && !date.contains(".")
                ? DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'").withZone(ZoneOffset.UTC)
                : DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS[xx][XXX]");
    }
}
