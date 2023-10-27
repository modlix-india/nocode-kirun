package com.fincity.nocode.kirun.engine.function.system.date;

import static com.fincity.nocode.kirun.engine.util.date.GetTimeInMillisUtil.getEpochTime;

public class MinimumTimeStamp extends MaximumTimestamp {

    public MinimumTimeStamp() {
        super("MinimumTimeStamp", "minimum");
    }

    @Override
    public String compare(String minDate, String currentDate) {

        return getEpochTime(minDate) <= getEpochTime(currentDate) ? minDate : currentDate;
    }

}
