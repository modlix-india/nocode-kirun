package com.fincity.nocode.kirun.engine.function.system.date;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.model.Parameter;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

import reactor.core.publisher.Mono;

public class SetTimeZone extends AbstractDateFunction {

    public static final String PARAMETER_TIMEZONE_NAME = "timezone";

    public SetTimeZone() {
        super(
                "SetTimeZone",
                AbstractDateFunction.EVENT_TIMESTAMP,
                AbstractDateFunction.PARAMETER_TIMESTAMP,
                new Parameter()
                        .setParameterName(SetTimeZone.PARAMETER_TIMEZONE_NAME)
                        .setSchema(Schema.ofString(SetTimeZone.PARAMETER_TIMEZONE_NAME)));
    }

    @Override
    public Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {
        final String timestamp = context.getArguments().get(AbstractDateFunction.PARAMETER_TIMESTAMP_NAME)
                .getAsString();

        final ZonedDateTime dateTime = DateUtil.getDateTime(timestamp);

        final String timeZone = context.getArguments().get(SetTimeZone.PARAMETER_TIMEZONE_NAME).getAsString();

        return Mono
                .just(new FunctionOutput(List.of(EventResult.outputOf(Map.of(AbstractDateFunction.EVENT_TIMESTAMP_NAME,
                        new JsonPrimitive(dateTime.withZoneSameInstant(ZoneId.of(timeZone))
                                .format(DateUtil.ISO_DATE_TIME_FORMATTER)))))));
    }
}
