package com.fincity.nocode.kirun.engine.function.system.date;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

import reactor.core.publisher.Mono;

public class GetCurrentTimestamp extends AbstractDateFunction {

    public GetCurrentTimestamp() {
        super("GetCurrentTimestamp", AbstractDateFunction.EVENT_TIMESTAMP);
    }

    @Override
    protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {
        return Mono.just(new FunctionOutput(
                List.of(EventResult.outputOf(
                        Map.of(AbstractDateFunction.EVENT_TIMESTAMP_NAME,
                                new JsonPrimitive(
                                        ZonedDateTime.now().format(DateUtil.ISO_DATE_TIME_FORMATTER)))))));
    }
}
