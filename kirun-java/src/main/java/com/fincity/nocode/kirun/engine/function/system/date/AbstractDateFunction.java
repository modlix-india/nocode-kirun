package com.fincity.nocode.kirun.engine.function.system.date;

import static com.fincity.nocode.kirun.engine.util.date.IsValidISODateUtil.checkValidity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
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
import com.fincity.nocode.kirun.engine.util.stream.QuadFunction;
import com.fincity.nocode.kirun.engine.util.stream.TriFunction;
import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;

import reactor.core.publisher.Mono;

public abstract class AbstractDateFunction extends AbstractReactiveFunction {

    public static final String ISO_DATE = "isoDate";

    public static final String ISO_DATE1 = "isoDate1";

    public static final String ISO_DATE2 = "isoDate2";

    private static final String OUTPUT = "result";

    public static final String YEAR = "years";

    public static final String MONTH = "months";

    public static final String DAY = "days";

    public static final String HOUR = "hours";

    public static final String MINUTE = "minutes";

    public static final String SECOND = "seconds";

    public static final String MILLIS = "millis";

    public static final String QUARTER = "quarters";

    public static final String WEEK = "weeks";

    private static final String TIME_STAMP = ".timeStamp";

    private static final String ERROR_MSG = "Invalid ISO 8601 Date format.";

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

    public static Entry<String, ReactiveFunction> ofEntryDateWithOutputBoolean(final String functionName,
            String firstParam, Predicate<String> function) {

        Parameter[] params = { Parameter.of(firstParam, Schema.ofRef(Namespaces.DATE + TIME_STAMP)) };

        Event event = new Event().setName(OUTPUT)
                .setParameters(Map.of(OUTPUT, Schema.ofBoolean(OUTPUT)));

        return Map.entry(functionName,
                new AbstractDateFunction(Namespaces.DATE, functionName, event, params) {

                    @Override
                    protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {

                        String firstDate = context.getArguments().get(firstParam).getAsString();

                        if (!checkValidity(firstDate))
                            throw new KIRuntimeException(ERROR_MSG);

                        return Mono.just(new FunctionOutput(
                                List.of(EventResult.outputOf(Map.of(OUTPUT,
                                        new JsonPrimitive(function.test(firstDate)))))));
                    }
                });
    }

    public static Entry<String, ReactiveFunction> ofEntryDateWithIntegerOutput(final String functionName,
            String firstParam, ToIntFunction<String> function) {

        Parameter[] params = { Parameter.of(firstParam, Schema.ofRef(Namespaces.DATE + TIME_STAMP)) };

        Event event = new Event().setName(OUTPUT).setParameters(Map.of(OUTPUT, Schema.ofInteger(OUTPUT)));

        return Map.entry(functionName,
                new AbstractDateFunction(Namespaces.DATE, functionName, event, params) {

                    @Override
                    protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {

                        String date = context.getArguments().get(firstParam).getAsString();

                        if (!checkValidity(date))
                            throw new KIRuntimeException(ERROR_MSG);

                        return Mono.just(new FunctionOutput(
                                List.of(EventResult.outputOf(Map.of(OUTPUT,
                                        new JsonPrimitive(function.applyAsInt(date)))))));
                    }
                });
    }

//    public static Entry<String, ReactiveFunction> ofEntryDateWithStringOutput(final String functionName,
//            String paramName, UnaryOperator<String> function) {
//
//        Parameter[] params = { Parameter.of(paramName, Schema.ofRef(Namespaces.DATE + TIME_STAMP)) };
//
//        Event event = new Event().setName(OUTPUT).setParameters(Map.of(OUTPUT, Schema.ofString(OUTPUT)));
//
//        return Map.entry(functionName, new AbstractDateFunction(Namespaces.DATE, functionName, event, params) {
//
//            @Override
//            protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {
//
//                String date = context.getArguments().get(paramName).getAsString();
//
//                if (!checkValidity(date))
//                    throw new KIRuntimeException(ERROR_MSG);
//
//                return Mono.just(new FunctionOutput(
//                        List.of(EventResult.outputOf(Map.of(OUTPUT,
//                                new JsonPrimitive(function.apply(date)))))));
//            }
//        });
//    }

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

    public static Entry<String, ReactiveFunction> ofEntryDateAndIntegerAndIntegerOutput(final String functionName,
            String firstParam, String secondParam,
            BiFunction<String, Integer, Integer> biFunction) {

        Parameter[] params = { Parameter.of(firstParam, Schema.ofRef(Namespaces.DATE + TIME_STAMP)),
                Parameter.of(secondParam, Schema.ofInteger(secondParam)),
        };

        Event event = new Event().setName(OUTPUT)
                .setParameters(Map.of(OUTPUT, Schema.ofRef(Namespaces.DATE + TIME_STAMP)));

        return Map.entry(functionName,
                new AbstractDateFunction(Namespaces.DATE, functionName, event, params) {

                    @Override
                    protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {

                        String firstDate = context.getArguments().get(firstParam).getAsString();

                        if (!checkValidity(firstDate))
                            throw new KIRuntimeException(ERROR_MSG);

                        int amount = context.getArguments().get(secondParam).getAsInt();

                        return Mono.just(new FunctionOutput(
                                List.of(EventResult.outputOf(Map.of(OUTPUT,
                                        new JsonPrimitive(biFunction.apply(firstDate, amount)))))));
                    }
                });
    }

    public static Entry<String, ReactiveFunction> ofEntryTwoDateAndBooleanOutput(final String functionName,
            String firstParam, String secondParam, String thirdParam,
            TriFunction<String, String, JsonArray, Boolean> triFunction) {

        Parameter[] params = { Parameter.of(firstParam, Schema.ofRef(Namespaces.DATE + TIME_STAMP)),
                Parameter.of(secondParam, Schema.ofRef(Namespaces.DATE + TIME_STAMP)),
                Parameter.of(thirdParam, Schema.ofString(thirdParam).setEnums(List.of(
                        new JsonPrimitive(YEAR),
                        new JsonPrimitive(MONTH),
                        new JsonPrimitive(DAY),
                        new JsonPrimitive(HOUR),
                        new JsonPrimitive(MINUTE),
                        new JsonPrimitive(SECOND),
                        new JsonPrimitive(MILLIS))), true)
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

    public static Entry<String, ReactiveFunction> ofEntryThreeDateAndBooleanOutput(final String functionName,
            String firstParam, String secondParam, String thirdParam, String fourthParam,
            QuadFunction<String, String, String, JsonArray, Boolean> quadFunction) {

        Parameter[] params = { Parameter.of(firstParam, Schema.ofRef(Namespaces.DATE + TIME_STAMP)),
                Parameter.of(secondParam, Schema.ofRef(Namespaces.DATE + TIME_STAMP)),
                Parameter.of(thirdParam, Schema.ofRef(Namespaces.DATE + TIME_STAMP)),
                Parameter.of(fourthParam, Schema.ofString(fourthParam).setEnums(List.of(
                        new JsonPrimitive(YEAR),
                        new JsonPrimitive(MONTH),
                        new JsonPrimitive(DAY),
                        new JsonPrimitive(HOUR),
                        new JsonPrimitive(MINUTE),
                        new JsonPrimitive(SECOND),
                        new JsonPrimitive(MILLIS))), true)
        };

        Event event = new Event().setName(OUTPUT).setParameters(Map.of(OUTPUT, Schema.ofBoolean(OUTPUT)));

        return Map.entry(functionName,
                new AbstractDateFunction(Namespaces.DATE, functionName, event, params) {

                    @Override
                    protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {

                        String firstDate = context.getArguments().get(firstParam).getAsString();

                        String secondDate = context.getArguments().get(secondParam).getAsString();

                        String betweenDate = context.getArguments().get(thirdParam).getAsString();

                        if (!checkValidity(firstDate))
                            throw new KIRuntimeException("Please provide the valid ISO date for " + firstParam);

                        if (!checkValidity(secondDate))
                            throw new KIRuntimeException("Please provide the valid ISO date for " + secondParam);

                        if (!checkValidity(betweenDate))
                            throw new KIRuntimeException("Please provide the valid ISO date for " + secondParam);

                        JsonArray arr = context.getArguments().get(fourthParam).getAsJsonArray();

                        int size = arr.size();

                        if (size == 0)
                            throw new KIRuntimeException("Please provide a unit for checking");

                        return Mono.just(new FunctionOutput(
                                List.of(EventResult.outputOf(Map.of(OUTPUT,
                                        new JsonPrimitive(
                                                quadFunction.apply(firstDate, secondDate, betweenDate, arr)))))));
                    }
                });
    }

    public static Entry<String, ReactiveFunction> ofEntryDateAndUnitAndDateOutput(final String functionName,
            String firstParam, String secondParam,
            BinaryOperator<String> bifunction) {

        Parameter[] params = { Parameter.of(firstParam, Schema.ofRef(Namespaces.DATE + TIME_STAMP)),

                Parameter.of(secondParam, Schema.ofString(secondParam).setEnums(List.of(
                        new JsonPrimitive(YEAR),
                        new JsonPrimitive(MONTH),
                        new JsonPrimitive(DAY),
                        new JsonPrimitive(HOUR),
                        new JsonPrimitive(MINUTE),
                        new JsonPrimitive(SECOND),
                        new JsonPrimitive(MILLIS),
                        new JsonPrimitive(WEEK),
                        new JsonPrimitive(QUARTER))))
        };

        Event event = new Event().setName(OUTPUT)
                .setParameters(Map.of(OUTPUT, Schema.ofRef(Namespaces.DATE + TIME_STAMP)));

        return Map.entry(functionName,
                new AbstractDateFunction(Namespaces.DATE, functionName, event, params) {

                    @Override
                    protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {

                        String date = context.getArguments().get(firstParam).getAsString();

                        if (!checkValidity(date))
                            throw new KIRuntimeException(ERROR_MSG);

                        String unit = context.getArguments().get(secondParam).getAsString();

                        return Mono.just(new FunctionOutput(
                                List.of(EventResult.outputOf(Map.of(OUTPUT,
                                        new JsonPrimitive(bifunction.apply(date, unit)))))));
                    }
                });
    }

    public static Entry<String, ReactiveFunction> ofEntryDateAndLongAndUnitAndDateOutput(final String functionName,
            String firstParam, String secondParam,
            String thirdParam,
            TriFunction<String, Long, String, String> triFunction) {

        Parameter[] params = { Parameter.of(firstParam, Schema.ofRef(Namespaces.DATE + TIME_STAMP)),
                Parameter.of(secondParam, Schema.ofLong(secondParam)),
                Parameter.of(thirdParam, Schema.ofString(thirdParam).setEnums(List.of(
                        new JsonPrimitive(YEAR),
                        new JsonPrimitive(MONTH),
                        new JsonPrimitive(DAY),
                        new JsonPrimitive(HOUR),
                        new JsonPrimitive(MINUTE),
                        new JsonPrimitive(SECOND),
                        new JsonPrimitive(MILLIS))))
        };

        Event event = new Event().setName(OUTPUT)
                .setParameters(Map.of(OUTPUT, Schema.ofRef(Namespaces.DATE + TIME_STAMP)));

        return Map.entry(functionName,
                new AbstractDateFunction(Namespaces.DATE, functionName, event, params) {

                    @Override
                    protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {

                        String firstDate = context.getArguments().get(firstParam).getAsString();

                        if (!checkValidity(firstDate))
                            throw new KIRuntimeException(ERROR_MSG);

                        long amount = context.getArguments().get(secondParam).getAsLong();

                        String unit = context.getArguments().get(thirdParam).getAsString();

                        return Mono.just(new FunctionOutput(
                                List.of(EventResult.outputOf(Map.of(OUTPUT,
                                        new JsonPrimitive(triFunction.apply(firstDate, amount, unit)))))));
                    }
                });
    }

}
