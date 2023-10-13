package com.fincity.nocode.kirun.engine.function.system.date;

import java.util.List;
import java.util.Map;

import com.fincity.nocode.kirun.engine.exception.KIRuntimeException;
import com.fincity.nocode.kirun.engine.function.reactive.AbstractReactiveFunction;
import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.model.Event;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;
import com.fincity.nocode.kirun.engine.model.Parameter;
import com.fincity.nocode.kirun.engine.namespaces.Namespaces;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.fincity.nocode.kirun.engine.util.date.IsValidIsoDateTime;
import com.google.gson.JsonPrimitive;

import reactor.core.publisher.Mono;

public class IsValidISODate extends AbstractReactiveFunction {

    private static final String VALUE = "isodate";

    private static final String OUTPUT = "output";

    @Override
    public FunctionSignature getSignature() {
        return new FunctionSignature().setName("IsValidISODate").setNamespace(Namespaces.DATE)
                .setParameters(
                        Map.of(VALUE,
                                new Parameter().setParameterName(VALUE)
                                        .setSchema(Schema.ofRef(Namespaces.DATE
                                                + ".timeStamp"))))
                .setEvents(Map.ofEntries(Event.outputEventMapEntry(Map.of(OUTPUT, Schema.ofBoolean(OUTPUT)))));
    }

    @Override
    protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {

        var date = context.getArguments().get(VALUE);

        if (!date.isJsonPrimitive())
            throw new KIRuntimeException("Please provide date type as input.");

        return Mono.just(new FunctionOutput(
                List.of(EventResult.of(OUTPUT, Map.of(OUTPUT,
                        new JsonPrimitive(IsValidIsoDateTime.checkValidity(date.getAsString())))))));
    }

}
