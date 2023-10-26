package com.fincity.nocode.kirun.engine.function.system.date;

import java.time.ZonedDateTime;

public class GetMinimumTimeStamp extends GetMaximumTimestamp {

    public GetMinimumTimeStamp() {
        super("GetMinimumTimeStamp", "minimum");
    }

    @Override
    public String compare(String minDate, String currentDate) {

        return ZonedDateTime.parse(minDate).toEpochSecond() <= ZonedDateTime.parse(currentDate).toEpochSecond()
                ? minDate
                : currentDate;
    }

}
