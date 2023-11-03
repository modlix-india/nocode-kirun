package com.fincity.nocode.kirun.engine.util.date;

import java.time.ZoneId;
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

    public static DateTimeFormatter getDateTimeFormatter(String date) {

        if (date.contains("Z"))

            return !date.contains(".")
                    ? DateTimeFormatter.ISO_INSTANT
                    : DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(ZoneOffset.UTC);

        return DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").withZone(ZoneId.systemDefault());

    }
}
