package com.fincity.nocode.kirun.engine.util.date;

import java.time.format.DateTimeFormatter;

public class DateTimePatternUtil {

    private DateTimePatternUtil() {

    }

    public static DateTimeFormatter getPattern() {

        return DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss[.SSS][xx][XXX]");
    }
}
