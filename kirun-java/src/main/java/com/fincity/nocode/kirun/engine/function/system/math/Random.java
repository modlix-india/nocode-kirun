package com.fincity.nocode.kirun.engine.function.system.math;

import java.util.List;
import java.util.Map;

import com.fincity.nocode.kirun.engine.function.reactive.AbstractReactiveFunction;
import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.json.schema.type.SchemaType;
import com.fincity.nocode.kirun.engine.json.schema.type.Type;
import com.fincity.nocode.kirun.engine.model.Event;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;
import com.fincity.nocode.kirun.engine.namespaces.Namespaces;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

import reactor.core.publisher.Mono;

public class Random extends AbstractReactiveFunction {

    private static final String VALUE = "value";

    private static final FunctionSignature SIGNATURE = new FunctionSignature()
            .setName("Random")
            .setNamespace(Namespaces.MATH)
            .setEvents(Map.ofEntries(
                    Event.outputEventMapEntry(Map.of(VALUE, new Schema().setType(Type.of(SchemaType.DOUBLE))))));

    @Override
    public FunctionSignature getSignature() {
        return SIGNATURE;
    }

    @Override
    protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {
        return Mono.just(
                new FunctionOutput(List.of(EventResult.outputOf(Map.of(VALUE, new JsonPrimitive(Math.random()))))));
    }
}
