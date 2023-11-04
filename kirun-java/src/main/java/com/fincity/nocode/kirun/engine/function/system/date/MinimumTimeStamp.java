package com.fincity.nocode.kirun.engine.function.system.date;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class MinimumTimeStamp extends MaximumTimeStamp {

    public MinimumTimeStamp() {
        super("MinimumTimeStamp");
    }

    @Override
    protected boolean compare(ZonedDateTime zdt, String currentDate, DateTimeFormatter dtf) {

        return zdt.compareTo(ZonedDateTime.parse(currentDate, dtf)) > 0;
    }
}
