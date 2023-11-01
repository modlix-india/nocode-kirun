package com.fincity.nocode.kirun.engine.util.date;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
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

                return utcTime.with(ChronoField.MONTH_OF_YEAR, quarterMonth).with(TemporalAdjusters.firstDayOfMonth())
                        .truncatedTo(ChronoUnit.DAYS);

            case "week":

                return utcTime.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY)).truncatedTo(ChronoUnit.DAYS);

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

    public static ZonedDateTime getEndWithGivenField(Instant instant, String fieldName) {

        ZonedDateTime utcTime = ZonedDateTime.ofInstant(instant,
                TimeZone.getTimeZone("UTC").toZoneId());

        switch (fieldName) {

            case "year":

                return utcTime.with(TemporalAdjusters.lastDayOfYear()).with(ChronoField.HOUR_OF_DAY, 23L)
                        .with(ChronoField.MINUTE_OF_HOUR, 59L).with(ChronoField.SECOND_OF_MINUTE, 59L)
                        .with(ChronoField.MILLI_OF_SECOND, 999L);

            case "month":

                return utcTime.with(TemporalAdjusters.lastDayOfMonth()).with(ChronoField.HOUR_OF_DAY, 23L)
                        .with(ChronoField.MINUTE_OF_HOUR, 59L).with(ChronoField.SECOND_OF_MINUTE, 59L)
                        .with(ChronoField.MILLI_OF_SECOND, 999L);

            case "quarter":

                int quarterMonth = (utcTime.getMonthValue() / 3) + 1;

                LocalDate ld = LocalDate.of(utcTime.getYear(), quarterMonth * 3, utcTime.getDayOfMonth());
                int day = ld.with(TemporalAdjusters.lastDayOfMonth()).get(ChronoField.DAY_OF_MONTH);

                return utcTime
                        .with(ChronoField.MONTH_OF_YEAR, ld.getMonthValue())
                        .with(ChronoField.DAY_OF_MONTH, day)
                        .with(ChronoField.HOUR_OF_DAY, 23L)
                        .with(ChronoField.MINUTE_OF_HOUR, 59L).with(ChronoField.SECOND_OF_MINUTE, 59L)
                        .with(ChronoField.MILLI_OF_SECOND, 999L);

            case "week":

                return utcTime.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY)).with(ChronoField.HOUR_OF_DAY, 23L)
                        .with(ChronoField.MINUTE_OF_HOUR, 59L).with(ChronoField.SECOND_OF_MINUTE, 59L)
                        .with(ChronoField.MILLI_OF_SECOND, 999L);

            case "day":

                return utcTime
                        .with(ChronoField.HOUR_OF_DAY, 23L)
                        .with(ChronoField.MINUTE_OF_HOUR, 59L).with(ChronoField.SECOND_OF_MINUTE, 59L)
                        .with(ChronoField.MILLI_OF_SECOND, 999L);

            case "date":

                return utcTime
                        .with(ChronoField.HOUR_OF_DAY, 23L)
                        .with(ChronoField.MINUTE_OF_HOUR, 59L).with(ChronoField.SECOND_OF_MINUTE, 59L)
                        .with(ChronoField.MILLI_OF_SECOND, 999L);

            case "hour":

                return utcTime
                        .with(ChronoField.MINUTE_OF_HOUR, 59L)
                        .with(ChronoField.SECOND_OF_MINUTE, 59L)
                        .with(ChronoField.MILLI_OF_SECOND, 999L);

            case "minute":

                return utcTime.with(ChronoField.SECOND_OF_MINUTE, 59L)
                        .with(ChronoField.MILLI_OF_SECOND, 999L);

            case "second":

                return utcTime.with(ChronoField.MILLI_OF_SECOND, 999L);

            default:
                return utcTime;
        }

    }
}
