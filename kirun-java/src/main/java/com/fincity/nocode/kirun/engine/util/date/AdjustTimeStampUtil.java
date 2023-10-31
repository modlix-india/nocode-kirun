package com.fincity.nocode.kirun.engine.util.date;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;
import java.util.TimeZone;

public class AdjustTimeStampUtil {

    private AdjustTimeStampUtil() {

    }

    public static ZonedDateTime getStartWithGivenField(Instant instant, String fieldName) {

        ZonedDateTime utcTime = ZonedDateTime.ofInstant(instant,
                TimeZone.getTimeZone("UTC").toZoneId());

        switch (fieldName) {

            case "year":

                return utcTime.withDayOfYear(1).with(ChronoField.HOUR_OF_DAY, 0L).truncatedTo(ChronoUnit.HOURS);

            case "month":

                return utcTime.withDayOfMonth(1).with(ChronoField.HOUR_OF_DAY, 0L).truncatedTo(ChronoUnit.HOURS);

            case "quarter":

                int quarterMonth = utcTime.getMonth().firstMonthOfQuarter().getValue();

                return utcTime.withDayOfMonth(quarterMonth).with(ChronoField.HOUR_OF_DAY, 0L)
                        .truncatedTo(ChronoUnit.HOURS);

            case "week":

                return utcTime.with(WeekFields.ISO.getFirstDayOfWeek()).truncatedTo(ChronoUnit.DAYS);

            case "day":

                return utcTime.truncatedTo(ChronoUnit.DAYS);

            case "date":

                return utcTime.truncatedTo(ChronoUnit.DAYS);

            case "hour":

                return utcTime.truncatedTo(ChronoUnit.HOURS);

            case "minute":

                return utcTime.truncatedTo(ChronoUnit.MINUTES);

            case "second":

                return utcTime.truncatedTo(ChronoUnit.SECONDS);

            default:
                return utcTime;
        }
    }
}
