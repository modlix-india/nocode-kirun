package com.fincity.nocode.kirun.engine.function.system.date;

import static com.fincity.nocode.kirun.engine.util.date.IsValidISODateUtil.checkValidity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.ToLongFunction;

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
import com.fincity.nocode.kirun.engine.util.stream.TriFunction;
import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;

import reactor.core.publisher.Mono;

public abstract class AbstractDateFunction extends AbstractReactiveFunction {

    private static final String ISO_DATE = "isoDate";

    private static final String OUTPUT = "result";

    public static final String YEAR = "year";

    public static final String MONTH = "month";

    public static final String DAY = "day";

    public static final String HOUR = "hour";

    public static final String MINUTE = "minute";

    public static final String SECOND = "second";

    private static final String TIME_STAMP = ".timeStamp";

    private static final String ERROR_MSG = "Please provide the valid iso date.";

    private final FunctionSignature signature;

    protected AbstractDateFunction(String namespace, String functionName, String output, SchemaType... schemaType) {

        if (schemaType == null || schemaType.length == 0) {
            schemaType = new SchemaType[] { SchemaType.INTEGER };
        }

        signature = new FunctionSignature()
                .setName(functionName)
                .setNamespace(namespace)
                .setParameters(Map.of(ISO_DATE,
                        new Parameter().setParameterName(ISO_DATE).setSchema(Schema.ofRef(Namespaces.DATE
                                + TIME_STAMP))))
                .setEvents(Map.ofEntries(Event.outputEventMapEntry(
                        Map.of(output, new Schema().setName(output).setType(Type.of(schemaType[0]))))));
    }

    protected AbstractDateFunction(String namespace, String functionName, Event event, Parameter... parameters) {

        Map<String, Parameter> paramMap = new HashMap<>();

        for (Parameter param : parameters)
            paramMap.put(param.getParameterName(), param);

        signature = new FunctionSignature().setName(functionName)
                .setNamespace(namespace)
                .setParameters(paramMap)
                .setEvents(Map.of(event.getName(), event));
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
                        .get(ISO_DATE)
                        .getAsString();

                if (!checkValidity(date))
                    throw new KIRuntimeException(ERROR_MSG);

                return Mono.just(new FunctionOutput(
                        List.of(EventResult.outputOf(Map.of(output, new JsonPrimitive(ufunction.apply(date)))))));
            }
        });
    }

    public static Entry<String, ReactiveFunction> ofEntryDateWithLongOutput(final String functionName,
            String firstParam, ToLongFunction<String> function) {

        Parameter[] params = { Parameter.of(firstParam, Schema.ofRef(Namespaces.DATE + TIME_STAMP)) };

        Event event = new Event().setName(OUTPUT).setParameters(Map.of(OUTPUT, Schema.ofInteger(OUTPUT)));

        return Map.entry(functionName,
                new AbstractDateFunction(Namespaces.DATE, functionName, event, params) {

                    @Override
                    protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {

                        String firstDate = context.getArguments().get(firstParam).getAsString();

                        if (!checkValidity(firstDate))
                            throw new KIRuntimeException(ERROR_MSG);

                        return Mono.just(new FunctionOutput(
                                List.of(EventResult.outputOf(Map.of(OUTPUT,
                                        new JsonPrimitive(function.applyAsLong(firstDate)))))));
                    }
                });
    }

    public static Entry<String, ReactiveFunction> ofEntryTwoDateAndBooleanOutput(final String functionName,
            String firstParam, String secondParam, String thirdParam,
            TriFunction<String, String, JsonArray, Boolean> triFunction) {

        Parameter[] params = { Parameter.of(firstParam, Schema.ofRef(Namespaces.DATE + TIME_STAMP)),
                Parameter.of(secondParam, Schema.ofRef(Namespaces.DATE + TIME_STAMP)),
                Parameter.of(thirdParam, new Schema().setEnums(List.of(
                        new JsonPrimitive(YEAR),
                        new JsonPrimitive(MONTH),
                        new JsonPrimitive(DAY),
                        new JsonPrimitive(HOUR),
                        new JsonPrimitive(MINUTE),
                        new JsonPrimitive(SECOND))), true)
        };

        Event event = new Event().setName(OUTPUT).setParameters(Map.of(OUTPUT, Schema.ofBoolean(OUTPUT)));

        return Map.entry(functionName,
                new AbstractDateFunction(Namespaces.DATE, functionName, event, params) {

                    @Override
                    protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {

                        String firstDate = context.getArguments().get(firstParam).getAsString();

                        String secondDate = context.getArguments().get(secondParam).getAsString();

                        if (!checkValidity(firstDate))
                            throw new KIRuntimeException("Please provide the valid ISO date for " + firstParam);

                        if (!checkValidity(secondDate))
                            throw new KIRuntimeException("Please provide the valid ISO date for " + secondParam);

                        JsonArray arr = context.getArguments().get(thirdParam).getAsJsonArray();

                        int size = arr.size();

                        if (size == 0)
                            throw new KIRuntimeException("Please provide a unit for checking");

                        return Mono.just(new FunctionOutput(
                                List.of(EventResult.outputOf(Map.of(OUTPUT,
                                        new JsonPrimitive(triFunction.apply(firstDate, secondDate, arr)))))));
                    }
                });
    }

}
