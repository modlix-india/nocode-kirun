package com.fincity.nocode.kirun.engine.function.system.date;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import com.fincity.nocode.kirun.engine.function.reactive.AbstractReactiveFunction;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;
import com.fincity.nocode.kirun.engine.namespaces.Namespaces;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

import reactor.core.publisher.Mono;

public class TimestampToEpoch extends AbstractReactiveFunction {

    private final FunctionSignature signature;
    private final boolean isSeconds;

    public TimestampToEpoch(String name, boolean isSeconds) {
        super();

        this.isSeconds = isSeconds;
        this.signature = new FunctionSignature()
                .setName(name)
                .setNamespace(Namespaces.DATE)
                .setParameters(
                        Map.of(
                                AbstractDateFunction.PARAMETER_TIMESTAMP_NAME,
                                AbstractDateFunction.PARAMETER_TIMESTAMP))
                .setEvents(Map.of(AbstractDateFunction.EVENT_LONG.getName(), AbstractDateFunction.EVENT_LONG));
    }

    @Override
    public FunctionSignature getSignature() {
        return this.signature;
    }

    @Override
    protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters parameters) {

        ZonedDateTime dateTime = DateUtil.getDateTime(
                parameters.getArguments().get(AbstractDateFunction.PARAMETER_TIMESTAMP_NAME).getAsString());

        return Mono.just(new FunctionOutput(List.of(
                EventResult.outputOf(
                        Map.of(AbstractDateFunction.EVENT_RESULT_NAME,
                                new JsonPrimitive(
                                        this.isSeconds ? dateTime.toEpochSecond()
                                                : dateTime.toInstant().toEpochMilli()))))));
    }
}
