package com.fincity.nocode.kirun.engine.function.system.date;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.model.Parameter;
import com.fincity.nocode.kirun.engine.namespaces.Namespaces;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

import reactor.core.publisher.Mono;

public class IsBetween extends AbstractDateFunction {

    public static final String PARAMETER_START_TIMESTAMP_NAME = "startTimestamp";
    public static final String PARAMETER_END_TIMESTAMP_NAME = "endTimestamp";

    public static final String PARAMETER_CHECK_TIMESTAMP_NAME = "checkTimestamp";

    private static final String TIMESTAMP_NAMESPACE = Namespaces.DATE + ".Timestamp";

    public IsBetween() {
        super(
                "IsBetween",
                EVENT_BOOLEAN,
                new Parameter()
                        .setParameterName(IsBetween.PARAMETER_START_TIMESTAMP_NAME)
                        .setSchema(Schema.ofRef(TIMESTAMP_NAMESPACE)),
                new Parameter()
                        .setParameterName(IsBetween.PARAMETER_END_TIMESTAMP_NAME)
                        .setSchema(Schema.ofRef(TIMESTAMP_NAMESPACE)),
                new Parameter()
                        .setParameterName(IsBetween.PARAMETER_CHECK_TIMESTAMP_NAME)
                        .setSchema(Schema.ofRef(TIMESTAMP_NAMESPACE)));
    }

    @Override
    public Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {

        String startTimestampString = context.getArguments().get(PARAMETER_START_TIMESTAMP_NAME).getAsString();
        String endTimestampString = context.getArguments().get(PARAMETER_END_TIMESTAMP_NAME).getAsString();
        String checkTimestampString = context.getArguments().get(PARAMETER_CHECK_TIMESTAMP_NAME).getAsString();

        ZonedDateTime startTimestamp = DateUtil.getDateTime(startTimestampString);
        ZonedDateTime endTimestamp = DateUtil.getDateTime(endTimestampString);
        ZonedDateTime checkTimestamp = DateUtil.getDateTime(checkTimestampString);

        return Mono.just(new FunctionOutput(List.of(EventResult.outputOf(Map.of(EVENT_RESULT_NAME,
                new JsonPrimitive(checkTimestamp.equals(startTimestamp) || checkTimestamp.equals(endTimestamp)
                        || (checkTimestamp.isAfter(startTimestamp) && checkTimestamp.isBefore(endTimestamp))))))));
    }
}
