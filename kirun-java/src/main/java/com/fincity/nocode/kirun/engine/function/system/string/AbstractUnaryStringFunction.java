package com.fincity.nocode.kirun.engine.function.system.string;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.function.ToIntFunction;

import com.fincity.nocode.kirun.engine.function.reactive.AbstractReactiveFunction;
import com.fincity.nocode.kirun.engine.function.reactive.ReactiveFunction;
import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.model.Event;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;
import com.fincity.nocode.kirun.engine.model.Parameter;
import com.fincity.nocode.kirun.engine.namespaces.Namespaces;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

import reactor.core.publisher.Mono;

public abstract class AbstractUnaryStringFunction extends AbstractReactiveFunction {

    protected static final String PARAMETER_STRING_NAME = "string";

    protected static final String EVENT_RESULT_NAME = "result";

    protected static final Parameter PARAMETER_STRING = new Parameter().setParameterName(PARAMETER_STRING_NAME)
            .setSchema(Schema.ofString(PARAMETER_STRING_NAME));

    protected static final Event EVENT_STRING = new Event().setName(Event.OUTPUT)
            .setParameters(Map.of(EVENT_RESULT_NAME, Schema.ofString(EVENT_RESULT_NAME)));

    protected static final Event EVENT_BOOLEAN = new Event().setName(Event.OUTPUT)
            .setParameters(Map.of(EVENT_RESULT_NAME, Schema.ofBoolean(EVENT_RESULT_NAME)));

    protected static final Event EVENT_INT = new Event().setName(Event.OUTPUT)
            .setParameters(Map.of(EVENT_RESULT_NAME, Schema.ofInteger(EVENT_RESULT_NAME)));

    protected static final Event EVENT_ARRAY = new Event().setName(Event.OUTPUT)
            .setParameters(Map.of(EVENT_RESULT_NAME, Schema.ofArray(EVENT_RESULT_NAME)));

    private final FunctionSignature signature;

    protected AbstractUnaryStringFunction(String functionName, Event event) {

        this.signature = new FunctionSignature().setName(functionName)
                .setNamespace(Namespaces.STRING)
                .setParameters(
                        Map.of(PARAMETER_STRING.getParameterName(), PARAMETER_STRING))
                .setEvents(Map.of(event.getName(), event));
    }

    @Override
    public FunctionSignature getSignature() {
        return this.signature;
    }

    public static Map.Entry<String, ReactiveFunction> ofEntryString(final String name,
            final UnaryOperator<String> function) {
        return Map.entry(name, new AbstractUnaryStringFunction(name, EVENT_STRING) {
            @Override
            protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {
                return Mono.just(new FunctionOutput(
                        List.of(EventResult.outputOf(Map.of(EVENT_RESULT_NAME,
                                new JsonPrimitive(function.apply(context.getArguments()
                                        .get(PARAMETER_STRING_NAME)
                                        .getAsString())))))));
            }
        });
    }

    public static Map.Entry<String, ReactiveFunction> ofEntryStringAndBooleanOutput(final String name,
            final Predicate<String> function) {
        return Map.entry(name, new AbstractUnaryStringFunction(name, EVENT_BOOLEAN) {
            @Override
            protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {
                return Mono.just(
                        new FunctionOutput(List.of(EventResult.outputOf(Map.of(EVENT_RESULT_NAME, new JsonPrimitive(
                                function.test(context.getArguments().get(PARAMETER_STRING_NAME).getAsString())))))));
            }
        });
    }

    public static Map.Entry<String, ReactiveFunction> ofEntryStringAndIntegerOutput(final String name,
            final ToIntFunction<String> function) {
        return Map.entry(name, new AbstractUnaryStringFunction(name, EVENT_INT) {
            @Override
            protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {
                return Mono.just(
                        new FunctionOutput(List.of(EventResult.outputOf(Map.of(EVENT_RESULT_NAME, new JsonPrimitive(
                                function.applyAsInt(
                                        context.getArguments().get(PARAMETER_STRING_NAME).getAsString())))))));
            }
        });
    }
}
