package com.fincity.nocode.kirun.engine.function.system.date;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.model.Parameter;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

import reactor.core.publisher.Mono;

public class AddSubtractTime extends AbstractDateFunction {

    public static final String PARAMETER_YEARS_NAME = "years";
    public static final String PARAMETER_MONTHS_NAME = "months";
    public static final String PARAMETER_DAYS_NAME = "days";
    public static final String PARAMETER_HOURS_NAME = "hours";
    public static final String PARAMETER_MINUTES_NAME = "minutes";
    public static final String PARAMETER_SECONDS_NAME = "seconds";
    public static final String PARAMETER_MILLISECONDS_NAME = "milliseconds";

    private final boolean isAdd;

    public AddSubtractTime(boolean isAdd) {
        super(isAdd ? "AddTime" : "SubtractTime",
                EVENT_TIMESTAMP,
                PARAMETER_TIMESTAMP,
                Parameter.of(PARAMETER_YEARS_NAME,
                        Schema.ofNumber(AddSubtractTime.PARAMETER_YEARS_NAME).setDefaultValue(new JsonPrimitive(0))),
                Parameter.of(PARAMETER_MONTHS_NAME,
                        Schema.ofNumber(AddSubtractTime.PARAMETER_MONTHS_NAME).setDefaultValue(new JsonPrimitive(0))),
                Parameter.of(PARAMETER_DAYS_NAME,
                        Schema.ofNumber(AddSubtractTime.PARAMETER_DAYS_NAME).setDefaultValue(new JsonPrimitive(0))),
                Parameter.of(PARAMETER_HOURS_NAME,
                        Schema.ofNumber(AddSubtractTime.PARAMETER_HOURS_NAME).setDefaultValue(new JsonPrimitive(0))),
                Parameter.of(PARAMETER_MINUTES_NAME,
                        Schema.ofNumber(AddSubtractTime.PARAMETER_MINUTES_NAME).setDefaultValue(new JsonPrimitive(0))),
                Parameter.of(PARAMETER_SECONDS_NAME,
                        Schema.ofNumber(AddSubtractTime.PARAMETER_SECONDS_NAME).setDefaultValue(new JsonPrimitive(0))),
                Parameter.of(PARAMETER_MILLISECONDS_NAME, Schema.ofNumber(AddSubtractTime.PARAMETER_MILLISECONDS_NAME)
                        .setDefaultValue(new JsonPrimitive(0))));

        this.isAdd = isAdd;
    }

    @Override
    protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters parameters) {

        String timestamp = parameters.getArguments()
                .get(AbstractDateFunction.PARAMETER_TIMESTAMP_NAME)
                .getAsString();

        ZonedDateTime dateTime = DateUtil.getDateTime(timestamp);

        Integer years = parameters.getArguments()
                .get(AddSubtractTime.PARAMETER_YEARS_NAME)
                .getAsInt();

        Integer months = parameters.getArguments()
                .get(AddSubtractTime.PARAMETER_MONTHS_NAME)
                .getAsInt();

        Integer days = parameters.getArguments()
                .get(AddSubtractTime.PARAMETER_DAYS_NAME)
                .getAsInt();

        Integer hours = parameters.getArguments()
                .get(AddSubtractTime.PARAMETER_HOURS_NAME)
                .getAsInt();

        Integer minutes = parameters.getArguments()
                .get(AddSubtractTime.PARAMETER_MINUTES_NAME)
                .getAsInt();

        Integer seconds = parameters.getArguments()
                .get(AddSubtractTime.PARAMETER_SECONDS_NAME)
                .getAsInt();

        Integer milliseconds = parameters.getArguments()
                .get(AddSubtractTime.PARAMETER_MILLISECONDS_NAME)
                .getAsInt();

        ZonedDateTime newDateTime;

        if (this.isAdd) {
            newDateTime = dateTime.plus(years, ChronoUnit.YEARS)
                    .plus(months, ChronoUnit.MONTHS)
                    .plus(days, ChronoUnit.DAYS)
                    .plus(hours, ChronoUnit.HOURS)
                    .plus(minutes, ChronoUnit.MINUTES)
                    .plus(seconds, ChronoUnit.SECONDS)
                    .plus(milliseconds, ChronoUnit.MILLIS);
        } else {
            newDateTime = dateTime.minus(years, ChronoUnit.YEARS)
                    .minus(months, ChronoUnit.MONTHS)
                    .minus(days, ChronoUnit.DAYS)
                    .minus(hours, ChronoUnit.HOURS)
                    .minus(minutes, ChronoUnit.MINUTES)
                    .minus(seconds, ChronoUnit.SECONDS)
                    .minus(milliseconds, ChronoUnit.MILLIS);
        }

        return Mono.just(new FunctionOutput(
                List.of(EventResult.outputOf(
                        Map.of(AbstractDateFunction.EVENT_TIMESTAMP_NAME,
                                new JsonPrimitive(
                                        newDateTime.format(DateUtil.ISO_DATE_TIME_FORMATTER)))))));
    }
}
