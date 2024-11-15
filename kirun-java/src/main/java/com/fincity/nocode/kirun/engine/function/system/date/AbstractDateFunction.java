package com.fincity.nocode.kirun.engine.function.system.date;

import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.model.Event;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;
import com.fincity.nocode.kirun.engine.model.Parameter;
import com.fincity.nocode.kirun.engine.namespaces.Namespaces;
import com.fincity.nocode.kirun.engine.function.reactive.AbstractReactiveFunction;
import com.fincity.nocode.kirun.engine.function.reactive.ReactiveFunction;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.function.ToIntFunction;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.function.BiFunction;
import com.fincity.nocode.kirun.engine.util.stream.TriFunction;
import java.util.ArrayList;

import com.google.gson.JsonElement;

import reactor.core.publisher.Mono;

public abstract class AbstractDateFunction extends AbstractReactiveFunction {

    private final FunctionSignature signature;
    public static final String PARAMETER_TIMESTAMP_NAME = "isoTimeStamp";
    public static final String PARAMETER_TIMESTAMP_NAME_ONE = "isoTimeStamp1";
    public static final String PARAMETER_TIMESTAMP_NAME_TWO = "isoTimeStamp2";
    public static final String PARAMETER_UNIT_NAME = "unit";
    public static final String PARAMETER_NUMBER_NAME = "number";

    private static final String TIMESTAMP_SCHEMA_NAME = Namespaces.DATE + ".Timestamp";

    public static final Parameter PARAMETER_TIMESTAMP = Parameter.of(
            PARAMETER_TIMESTAMP_NAME,
            Schema.ofRef(TIMESTAMP_SCHEMA_NAME));

    public static final Parameter PARAMETER_TIMESTAMP_ONE = Parameter.of(PARAMETER_TIMESTAMP_NAME_ONE,
            Schema.ofRef(TIMESTAMP_SCHEMA_NAME));

    public static final Parameter PARAMETER_TIMESTAMP_TWO = Parameter.of(PARAMETER_TIMESTAMP_NAME_TWO,
            Schema.ofRef(TIMESTAMP_SCHEMA_NAME));
    public static final Parameter PARAMETER_VARIABLE_UNIT = Parameter.of(
            PARAMETER_UNIT_NAME,
            Schema.ofRef(Namespaces.DATE + ".Timeunit"))
            .setVariableArgument(true);

    public static final Parameter PARAMETER_UNIT = Parameter.of(PARAMETER_UNIT_NAME,
            Schema.ofRef(Namespaces.DATE + ".Timeunit"));

    public static final Parameter PARAMETER_NUMBER = Parameter.of(
            PARAMETER_NUMBER_NAME,
            Schema.ofInteger(PARAMETER_NUMBER_NAME));

    public static final String EVENT_RESULT_NAME = "result";
    public static final String EVENT_TIMESTAMP_NAME = "isoTimeStamp";

    public static final Event EVENT_INT = new Event()
            .setName(Event.OUTPUT)
            .setParameters(Map.of(EVENT_RESULT_NAME, Schema.ofInteger(EVENT_RESULT_NAME)));

    public static final Event EVENT_STRING = new Event()
            .setName(Event.OUTPUT)
            .setParameters(Map.of(EVENT_RESULT_NAME, Schema.ofString(EVENT_RESULT_NAME)));

    public static final Event EVENT_LONG = new Event()
            .setName(Event.OUTPUT)
            .setParameters(Map.of(EVENT_RESULT_NAME, Schema.ofLong(EVENT_RESULT_NAME)));

    protected static final Event EVENT_BOOLEAN = new Event()
            .setName(Event.OUTPUT)
            .setParameters(Map.of(EVENT_RESULT_NAME, Schema.ofBoolean(EVENT_RESULT_NAME)));

    public static final Event EVENT_TIMESTAMP = new Event()
            .setName(Event.OUTPUT)
            .setParameters(Map.of(EVENT_TIMESTAMP_NAME, Schema.ofRef(TIMESTAMP_SCHEMA_NAME)));

    @Override
    public FunctionSignature getSignature() {
        return this.signature;
    }

    protected AbstractDateFunction(String functionName, Event event, Parameter... parameter) {
        this.signature = new FunctionSignature()
                .setName(functionName)
                .setNamespace(Namespaces.DATE)
                .setEvents(Map.of(event.getName(), event));

        if (parameter == null || parameter.length == 0)
            return;

        Map<String, Parameter> paramMap = new HashMap<>();
        for (Parameter e : parameter) {
            paramMap.put(e.getParameterName(), e);
        }
        this.signature.setParameters(paramMap);
    }

    public static Map.Entry<String, ReactiveFunction> ofEntryTimestampAndIntegerOutput(String name,
            ToIntFunction<String> fun) {

        ReactiveFunction dateFunction = new AbstractDateFunction(name, EVENT_INT, PARAMETER_TIMESTAMP) {
            @Override
            protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {
                String timestamp = context.getArguments().get(PARAMETER_TIMESTAMP_NAME).getAsString();
                JsonPrimitive result = new JsonPrimitive(fun.applyAsInt(timestamp));
                return Mono.just(new FunctionOutput(List.of(
                        EventResult.outputOf(Map.of(EVENT_RESULT_NAME, result)))));
            }
        };

        return Map.entry(name, dateFunction);
    }

    public static Map.Entry<String, ReactiveFunction> ofEntryTimestampAndBooleanOutput(String name,
            Predicate<String> fun) {

        ReactiveFunction dateFunction = new AbstractDateFunction(name, EVENT_BOOLEAN, PARAMETER_TIMESTAMP) {
            @Override
            protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {
                String timestamp = context.getArguments().get(PARAMETER_TIMESTAMP_NAME).getAsString();
                JsonPrimitive result = new JsonPrimitive(fun.test(timestamp));
                return Mono.just(new FunctionOutput(List.of(
                        EventResult.outputOf(Map.of(EVENT_RESULT_NAME, result)))));
            }
        };

        return Map.entry(name, dateFunction);
    }

    public static Map.Entry<String, ReactiveFunction> ofEntryTimestampAndStringOutput(String name,
            UnaryOperator<String> fun) {

        ReactiveFunction dateFunction = new AbstractDateFunction(name, EVENT_STRING, PARAMETER_TIMESTAMP) {
            @Override
            protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {
                String timestamp = context.getArguments().get(PARAMETER_TIMESTAMP_NAME).getAsString();
                JsonPrimitive result = new JsonPrimitive(fun.apply(timestamp));
                return Mono.just(new FunctionOutput(List.of(
                        EventResult.outputOf(Map.of(EVENT_RESULT_NAME, result)))));
            }
        };

        return Map.entry(name, dateFunction);
    }

    public static Map.Entry<String, ReactiveFunction> ofEntryTimestampIntegerAndTimestampOutput(String name,
            BiFunction<String, Integer, String> fun) {
        ReactiveFunction dateFunction = new AbstractDateFunction(name, EVENT_TIMESTAMP, PARAMETER_TIMESTAMP,
                PARAMETER_NUMBER) {
            @Override
            protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {
                String timestamp = context.getArguments().get(PARAMETER_TIMESTAMP_NAME).getAsString();
                Integer number = context.getArguments().get(PARAMETER_NUMBER_NAME).getAsInt();
                JsonPrimitive result = new JsonPrimitive(fun.apply(timestamp, number));
                return Mono.just(new FunctionOutput(List.of(
                        EventResult.outputOf(Map.of(EVENT_RESULT_NAME, result)))));
            }
        };
        return Map.entry(name, dateFunction);
    }

    public static <T extends JsonElement> Map.Entry<String, ReactiveFunction> ofEntryTimestampTimestampAndTOutput(
            String name, Event event,
            TriFunction<String, String, List<JsonElement>, T> fun, Parameter... parameter) {

        Parameter[] paramArray = new Parameter[parameter.length + 2];
        paramArray[0] = PARAMETER_TIMESTAMP_ONE;
        paramArray[1] = PARAMETER_TIMESTAMP_TWO;
        System.arraycopy(parameter, 0, paramArray, 2, parameter.length);

        ReactiveFunction dateFunction = new AbstractDateFunction(name, event, paramArray) {
            @Override
            protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {

                List<JsonElement> args = new ArrayList<>();
                for (Parameter p : parameter) {
                    args.add(context.getArguments().get(p.getParameterName()));
                }

                String timestamp1 = context.getArguments().get(PARAMETER_TIMESTAMP_NAME_ONE).getAsString();
                String timestamp2 = context.getArguments().get(PARAMETER_TIMESTAMP_NAME_TWO).getAsString();

                T result = fun.apply(timestamp1, timestamp2, args);

                return Mono.just(new FunctionOutput(List.of(
                        EventResult.outputOf(Map.of(EVENT_RESULT_NAME, result)))));
            }
        };

        return Map.entry(name, dateFunction);
    }
}
