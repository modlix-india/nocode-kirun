package com.fincity.nocode.kirun.engine.function.system.date;

import java.time.DayOfWeek;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;

import com.google.gson.JsonPrimitive;

import reactor.core.publisher.Mono;

public class StartEndOf extends AbstractDateFunction {

    private final boolean isStart;

    public StartEndOf(boolean isStart) {
        super(
                isStart ? "StartOf" : "EndOf",
                EVENT_TIMESTAMP,
                PARAMETER_TIMESTAMP,
                PARAMETER_UNIT);

        this.isStart = isStart;
    }

    @Override
    protected Mono<FunctionOutput> internalExecute(
            ReactiveFunctionExecutionParameters context) {
        String timestamp = context.getArguments().get(PARAMETER_TIMESTAMP_NAME).getAsString();

        ZonedDateTime dateTime = DateUtil.getDateTime(timestamp);

        String unit = context.getArguments().get(PARAMETER_UNIT_NAME).getAsString().toLowerCase();

        ChronoUnit chronoUnit = ChronoUnit.valueOf(unit.toUpperCase());

        ZonedDateTime newDateTime = this.isStart ? this.floor(dateTime, chronoUnit) : this.ceil(dateTime, chronoUnit);

        return Mono.just(new FunctionOutput(List.of(
                EventResult.outputOf(
                        Map.of(EVENT_TIMESTAMP_NAME,
                                new JsonPrimitive(newDateTime.format(DateUtil.ISO_DATE_TIME_FORMATTER)))))));
    }

    public ZonedDateTime floor(ZonedDateTime zdt, ChronoUnit unit) {

        if (unit.isTimeBased() || unit == ChronoUnit.DAYS) {
            return zdt.truncatedTo(unit);
        } else if (unit == ChronoUnit.WEEKS) {
            WeekFields weekFields = WeekFields.of(Locale.getDefault());
            DayOfWeek firstDayOfWeek = weekFields.getFirstDayOfWeek();
            return zdt.with(TemporalAdjusters.previousOrSame(firstDayOfWeek))
                    .truncatedTo(ChronoUnit.DAYS);
        } else if (unit == ChronoUnit.MONTHS) {
            return zdt.with(TemporalAdjusters.firstDayOfMonth())
                    .truncatedTo(ChronoUnit.DAYS);
        } else if (unit == ChronoUnit.YEARS) {
            return zdt.with(TemporalAdjusters.firstDayOfYear())
                    .truncatedTo(ChronoUnit.DAYS);
        } else {
            throw new UnsupportedOperationException("Unit not supported: " + unit);
        }
    }

    public ZonedDateTime ceil(ZonedDateTime zdt, ChronoUnit unit) {

        ZonedDateTime floorValue = null;
        if (unit.isTimeBased() || unit == ChronoUnit.DAYS) {
            floorValue = zdt.truncatedTo(unit);
        } else if (unit == ChronoUnit.WEEKS) {
            WeekFields weekFields = WeekFields.of(Locale.getDefault());
            DayOfWeek firstDayOfWeek = weekFields.getFirstDayOfWeek();
            floorValue = zdt.with(TemporalAdjusters.previousOrSame(firstDayOfWeek))
                    .truncatedTo(ChronoUnit.DAYS);
        } else if (unit == ChronoUnit.MONTHS) {
            floorValue = zdt.with(TemporalAdjusters.firstDayOfMonth())
                    .truncatedTo(ChronoUnit.DAYS);
        } else if (unit == ChronoUnit.YEARS) {
            floorValue = zdt.with(TemporalAdjusters.firstDayOfYear())
                    .truncatedTo(ChronoUnit.DAYS);
        } else {
            throw new UnsupportedOperationException("Unit not supported: " + unit);
        }

        return floorValue.plus(1, unit).minus(1, ChronoUnit.MILLIS);
    }
}
