package com.fincity.nocode.kirun.engine.util.date;

import static com.fincity.nocode.kirun.engine.function.system.date.AbstractDateFunction.DAY;
import static com.fincity.nocode.kirun.engine.function.system.date.AbstractDateFunction.HOUR;
import static com.fincity.nocode.kirun.engine.function.system.date.AbstractDateFunction.MINUTE;
import static com.fincity.nocode.kirun.engine.function.system.date.AbstractDateFunction.MONTH;
import static com.fincity.nocode.kirun.engine.function.system.date.AbstractDateFunction.QUARTER;
import static com.fincity.nocode.kirun.engine.function.system.date.AbstractDateFunction.WEEK;
import static com.fincity.nocode.kirun.engine.function.system.date.AbstractDateFunction.YEAR;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;

public class AdjustTimeStampUtil {

    private AdjustTimeStampUtil() {

    }

    public static final ZonedDateTime startOfTimeStamp(String inputDate, String unit) {

        ZonedDateTime zdt = ZonedDateTime.parse(inputDate, DateTimePatternUtil.getPattern());

        switch (unit) {

            case YEAR:

                return zdt.withDayOfYear(1).with(ChronoField.HOUR_OF_DAY, 0L).truncatedTo(ChronoUnit.HOURS);

            case MONTH:
                return zdt.withDayOfMonth(1).with(ChronoField.HOUR_OF_DAY, 0L).truncatedTo(ChronoUnit.HOURS);

            case QUARTER:
                int quarterMonth = zdt.getMonth().firstMonthOfQuarter().getValue();

                return zdt.with(ChronoField.MONTH_OF_YEAR, quarterMonth).with(TemporalAdjusters.firstDayOfMonth())
                        .truncatedTo(ChronoUnit.DAYS);

            case WEEK:

                return zdt.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY)).truncatedTo(ChronoUnit.DAYS);

            case DAY:
                return zdt.truncatedTo(ChronoUnit.DAYS);

            case HOUR:
                return zdt.truncatedTo(ChronoUnit.HOURS);

            case MINUTE:
                return zdt.truncatedTo(ChronoUnit.MINUTES);

            default:
                return zdt.truncatedTo(ChronoUnit.SECONDS);
        }

    }

    public static ZonedDateTime endOfGivenField(String inputDate, String unit) {

        ZonedDateTime zdt = ZonedDateTime.parse(inputDate, DateTimePatternUtil.getPattern());

        switch (unit) {

            case YEAR:

                return zdt.with(TemporalAdjusters.lastDayOfYear()).with(ChronoField.HOUR_OF_DAY, 23L)
                        .with(ChronoField.MINUTE_OF_HOUR, 59L).with(ChronoField.SECOND_OF_MINUTE, 59L)
                        .with(ChronoField.MILLI_OF_SECOND, 999L);

            case MONTH:

                return zdt.with(TemporalAdjusters.lastDayOfMonth()).with(ChronoField.HOUR_OF_DAY, 23L)
                        .with(ChronoField.MINUTE_OF_HOUR, 59L).with(ChronoField.SECOND_OF_MINUTE, 59L)
                        .with(ChronoField.MILLI_OF_SECOND, 999L);

            case QUARTER:

                int quarterMonth = (zdt.getMonthValue() / 3) + 1;

                LocalDate ld = LocalDate.of(zdt.getYear(), quarterMonth * 3, zdt.getDayOfMonth());
                int day = ld.with(TemporalAdjusters.lastDayOfMonth()).get(ChronoField.DAY_OF_MONTH);

                return zdt
                        .with(ChronoField.MONTH_OF_YEAR, ld.getMonthValue())
                        .with(ChronoField.DAY_OF_MONTH, day)
                        .with(ChronoField.HOUR_OF_DAY, 23L)
                        .with(ChronoField.MINUTE_OF_HOUR, 59L).with(ChronoField.SECOND_OF_MINUTE, 59L)
                        .with(ChronoField.MILLI_OF_SECOND, 999L);

            case WEEK:

                return zdt.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY)).with(ChronoField.HOUR_OF_DAY, 23L)
                        .with(ChronoField.MINUTE_OF_HOUR, 59L).with(ChronoField.SECOND_OF_MINUTE, 59L)
                        .with(ChronoField.MILLI_OF_SECOND, 999L);

            case DAY:

                return zdt
                        .with(ChronoField.HOUR_OF_DAY, 23L)
                        .with(ChronoField.MINUTE_OF_HOUR, 59L).with(ChronoField.SECOND_OF_MINUTE, 59L)
                        .with(ChronoField.MILLI_OF_SECOND, 999L);

            case HOUR:

                return zdt
                        .with(ChronoField.MINUTE_OF_HOUR, 59L)
                        .with(ChronoField.SECOND_OF_MINUTE, 59L)
                        .with(ChronoField.MILLI_OF_SECOND, 999L);

            case MINUTE:

                return zdt.with(ChronoField.SECOND_OF_MINUTE, 59L)
                        .with(ChronoField.MILLI_OF_SECOND, 999L);

            default:
                return zdt.with(ChronoField.MILLI_OF_SECOND, 999L);

        }

    }
}
