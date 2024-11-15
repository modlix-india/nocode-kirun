package com.fincity.nocode.kirun.engine.function.system.date;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.model.Parameter;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

import reactor.core.publisher.Mono;

public class ToDateString extends AbstractDateFunction {
    public static final String PARAMETER_FORMAT_NAME = "format";
    public static final String PARAMETER_LOCALE_NAME = "locale";

    public ToDateString() {
        super(
                "ToDateString",
                EVENT_STRING,
                PARAMETER_TIMESTAMP,
                new Parameter()
                        .setParameterName(ToDateString.PARAMETER_FORMAT_NAME)
                        .setSchema(Schema.ofString(ToDateString.PARAMETER_FORMAT_NAME)),
                new Parameter()
                        .setParameterName(ToDateString.PARAMETER_LOCALE_NAME)
                        .setSchema(Schema.ofString(ToDateString.PARAMETER_LOCALE_NAME)
                                .setDefaultValue(new JsonPrimitive("en"))));
    }

    @Override
    protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters parameters) {

        ZonedDateTime dateTime = DateUtil
                .getDateTime(parameters.getArguments().get(PARAMETER_TIMESTAMP_NAME).getAsString());

        String format = parameters.getArguments().get(PARAMETER_FORMAT_NAME).getAsString();

        return Mono.just(new FunctionOutput(List.of(
                EventResult.outputOf(
                        Map.of(AbstractDateFunction.EVENT_RESULT_NAME,
                                new JsonPrimitive(
                                        dateTime.format(
                                                DateTimeFormatter.ofPattern(DateUtil.toDateTimeFormat(format)))))))));
    }
}
