package com.fincity.nocode.kirun.engine.function.system.object;

import java.util.List;
import java.util.Map;

import com.fincity.nocode.kirun.engine.function.reactive.AbstractReactiveFunction;
import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;
import com.fincity.nocode.kirun.engine.model.Parameter;
import com.fincity.nocode.kirun.engine.namespaces.Namespaces;
import com.fincity.nocode.kirun.engine.runtime.expression.tokenextractor.ObjectValueSetterExtractor;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.fincity.nocode.kirun.engine.model.Event;

import com.google.gson.JsonPrimitive;

import reactor.core.publisher.Mono;

public class ObjectPutValue extends AbstractReactiveFunction {

    private static final String DATA = "Data.";

    private static final String SOURCE = "source";

    private static final String VALUE = "value";

    private static final String KEY = "key";

    private static final String OVERWRITE = "overwrite";

    private static final String DELETE_KEY_ON_NULL = "deleteKeyOnNull";

    private Parameter parameterSource = Parameter.of(SOURCE, Schema.ofObject(SOURCE));

    private Parameter parameterKey = Parameter.of(KEY, Schema.ofString(KEY));

    private Parameter parameterValue = Parameter.of(VALUE, Schema.ofAny(VALUE));

    private Parameter parameterOverwrite = Parameter.of(OVERWRITE,
            Schema.ofBoolean(OVERWRITE).setDefaultValue(new JsonPrimitive(false)));

    private Parameter parameterDeleteKeyOnNull = Parameter.of(DELETE_KEY_ON_NULL,
            Schema.ofBoolean(DELETE_KEY_ON_NULL).setDefaultValue(new JsonPrimitive(false)));

    @Override
    public FunctionSignature getSignature() {
        return new FunctionSignature()
                .setNamespace(Namespaces.SYSTEM_OBJECT)
                .setName("ObjectPutValue")
                .setParameters(

                        Map.of(

                                SOURCE, parameterSource,
                                KEY, parameterKey,
                                VALUE, parameterValue,
                                OVERWRITE, parameterOverwrite,
                                DELETE_KEY_ON_NULL, parameterDeleteKeyOnNull

                        ))

                .setEvents(Map.ofEntries(Event.outputEventMapEntry(Map.of(VALUE, Schema.ofObject(VALUE))))); 
        
    }

    @Override
    protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {

        var source = context.getArguments().get(SOURCE);
        var key = context.getArguments().get(KEY);
        var value = context.getArguments().get(VALUE);
        var overwrite = context.getArguments().get(OVERWRITE);
        var deleteKeyOnNull = context.getArguments().get(DELETE_KEY_ON_NULL);

        if (source == null || source.isJsonNull() || (source.isJsonPrimitive() && !((JsonPrimitive) source).isString()))
            return Mono.just(new FunctionOutput(List.of())); // change the output

        ObjectValueSetterExtractor ove = new ObjectValueSetterExtractor(source, DATA);
        ove.setValue(key.getAsString(), value, overwrite.getAsBoolean(), deleteKeyOnNull.getAsBoolean());

        return Mono.just(new FunctionOutput(List.of(EventResult.outputOf(Map.of(VALUE, ove.getStore())))));
    }

}
