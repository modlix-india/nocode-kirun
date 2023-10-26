package com.fincity.nocode.kirun.engine.function.system.date;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Predicate;

import com.fincity.nocode.kirun.engine.exception.KIRuntimeException;
import com.fincity.nocode.kirun.engine.function.reactive.AbstractReactiveFunction;
import com.fincity.nocode.kirun.engine.function.reactive.ReactiveFunction;
import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.json.schema.type.SchemaType;
import com.fincity.nocode.kirun.engine.json.schema.type.Type;
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

public abstract class AbstractDateFunction extends AbstractReactiveFunction {

    static final String VALUE = "isodate";

    static final String OUTPUT = "result";

    private final FunctionSignature signature;

    protected AbstractDateFunction(String namespace, String functionName, SchemaType... schemaType) {

        if (schemaType == null || schemaType.length == 0) {
            schemaType = new SchemaType[] { SchemaType.DOUBLE };
        }

        signature = new FunctionSignature()
                .setName(functionName)
                .setNamespace(namespace)
                .setParameters(Map.of(VALUE,
                        new Parameter().setParameterName(VALUE).setSchema(Schema.ofRef(Namespaces.DATE
                                + ".timeStamp"))))
                .setEvents(Map.ofEntries(Event.outputEventMapEntry(
                        Map.of(OUTPUT, new Schema().setName(OUTPUT).setType(Type.of(schemaType))))));
    }

    protected AbstractDateFunction(String namespace, String functionName, String output, SchemaType... schemaType) {

        if (schemaType == null || schemaType.length == 0) {
            schemaType = new SchemaType[] { SchemaType.DOUBLE };
        }

        signature = new FunctionSignature()
                .setName(functionName)
                .setNamespace(namespace)
                .setParameters(Map.of(VALUE,
                        new Parameter().setParameterName(VALUE).setSchema(Schema.ofRef(Namespaces.DATE
                                + ".timeStamp"))))
                .setEvents(Map.ofEntries(Event.outputEventMapEntry(
                        Map.of(output, new Schema().setName(output).setType(Type.of(schemaType))))));
    }

    @Override
    public FunctionSignature getSignature() {
        return signature;
    }

    public static Entry<String, ReactiveFunction> ofEntryDateAndStringWithOutputName(final String name, String output,
            Function<String, Number> ufunction, SchemaType... schemaType) {

        return Map.entry(name, new AbstractDateFunction(Namespaces.DATE, name, output, schemaType) {

            @Override
            protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {

                String date = context.getArguments()
                        .get(VALUE)
                        .getAsString();

                if (!IsValidIsoDateTime.checkValidity(date))
                    throw new KIRuntimeException("Please provide the valid iso date.");

                return Mono.just(new FunctionOutput(
                        List.of(EventResult.outputOf(Map.of(output, new JsonPrimitive(ufunction.apply(date)))))));
            }
        });
    }

    public static Entry<String, ReactiveFunction> ofEntryDateAndBooleanWithOutputName(final String name, String output,
            Predicate<String> ufunction, SchemaType... schemaType) {

        return Map.entry(name, new AbstractDateFunction(Namespaces.DATE, name, output, schemaType) {

            @Override
            protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {

                String date = context.getArguments()
                        .get(VALUE)
                        .getAsString();

                if (!IsValidIsoDateTime.checkValidity(date))
                    throw new KIRuntimeException("Please provide the valid iso date.");

                return Mono.just(new FunctionOutput(
                        List.of(EventResult.outputOf(Map.of(output, new JsonPrimitive(ufunction.test(date)))))));
            }
        });
    }
}
