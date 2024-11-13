package com.fincity.nocode.kirun.engine.function.system.date;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.List;
import java.util.Map;

import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.model.Parameter;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

import reactor.core.publisher.Mono;

public class FromDateString extends AbstractDateFunction {

    public static final String PARAMETER_FORMAT_NAME = "format";

    public static final String PARAMETER_TIMESTAMP_STRING_NAME = "timestampString";

    public FromDateString() {
        super(
                "FromDateString",
                EVENT_TIMESTAMP,
                Parameter.of(
                        PARAMETER_TIMESTAMP_STRING_NAME,
                        Schema.ofString(PARAMETER_TIMESTAMP_STRING_NAME)),
                Parameter.of(
                        PARAMETER_FORMAT_NAME,
                        Schema.ofString(PARAMETER_FORMAT_NAME)));
    }

    @Override
    protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {

        String timestampString = context.getArguments().get(PARAMETER_TIMESTAMP_STRING_NAME).getAsString();
        String format = context.getArguments().get(PARAMETER_FORMAT_NAME).getAsString();

        ZonedDateTime currentTime = ZonedDateTime.now();

        TemporalAccessor accessor = new DateTimeFormatterBuilder().appendPattern(DateUtil.toDateTimeFormat(format))
                .parseDefaulting(ChronoField.YEAR, currentTime.get(ChronoField.YEAR))
                .parseDefaulting(ChronoField.MONTH_OF_YEAR, currentTime.get(ChronoField.MONTH_OF_YEAR))
                .parseDefaulting(ChronoField.DAY_OF_MONTH, currentTime.get(ChronoField.DAY_OF_MONTH))
                .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
                .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
                .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
                .parseDefaulting(ChronoField.NANO_OF_SECOND, 0)
                .toFormatter()
                .withZone(currentTime.getZone())
                .parse(timestampString);

        ZonedDateTime zonedDateTime = ZonedDateTime.from(accessor);

        return Mono.just(new FunctionOutput(
                List.of(EventResult.outputOf(
                        Map.of(AbstractDateFunction.EVENT_TIMESTAMP_NAME,
                                new JsonPrimitive(
                                        zonedDateTime.format(DateUtil.ISO_DATE_TIME_FORMATTER)))))));
    }
}
