package com.fincity.nocode.kirun.engine.function.system.date;

import static com.fincity.nocode.kirun.engine.util.date.DateCompareUtil.DAY;
import static com.fincity.nocode.kirun.engine.util.date.DateCompareUtil.HOUR;
import static com.fincity.nocode.kirun.engine.util.date.DateCompareUtil.MINUTE;
import static com.fincity.nocode.kirun.engine.util.date.DateCompareUtil.MONTH;
import static com.fincity.nocode.kirun.engine.util.date.DateCompareUtil.SECOND;
import static com.fincity.nocode.kirun.engine.util.date.DateCompareUtil.YEAR;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.fincity.nocode.kirun.engine.exception.KIRuntimeException;
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
import com.fincity.nocode.kirun.engine.util.date.IsValidIsoDateTime;
import com.fincity.nocode.kirun.engine.util.stream.QuadFunction;
import com.fincity.nocode.kirun.engine.util.stream.TriFunction;

import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;

import reactor.core.publisher.Mono;

public abstract class AbstractCompareDateFunction extends AbstractReactiveFunction {

    protected static final String ISO_DATE1 = "dateone";

    protected static final String ISO_DATE2 = "datetwo";

    protected static final String ISO_DATE3 = "datethree";

    protected static final String TIME_UNIT = "unit";

    protected static final String OUTPUT = "result";

    private static final String ERROR_MSG = "Please provide the valid ISO date for ";

    private final FunctionSignature signature;

    protected AbstractCompareDateFunction(String functionName) {

        this.signature = new FunctionSignature()
                .setName(functionName)
                .setNamespace(Namespaces.DATE)
                .setParameters(Map.ofEntries(
                        Parameter.ofEntry(ISO_DATE1, Schema.ofRef(Namespaces.DATE
                                + ".timeStamp")),
                        Parameter.ofEntry(ISO_DATE2, Schema.ofRef(
                                Namespaces.DATE + ".timeStamp")),
                        Parameter.ofEntry(TIME_UNIT,
                                new Schema().setEnums(
                                        List.of(
                                                new JsonPrimitive(YEAR),
                                                new JsonPrimitive(MONTH),
                                                new JsonPrimitive(DAY),
                                                new JsonPrimitive(HOUR),
                                                new JsonPrimitive(MINUTE),
                                                new JsonPrimitive(SECOND))),
                                true)

                ))
                .setEvents(Map.ofEntries(Event.outputEventMapEntry(
                        Map.of(OUTPUT, Schema.ofBoolean(OUTPUT)))));
    }

    protected AbstractCompareDateFunction(String functionName, String paramName) {

        this.signature = new FunctionSignature()
                .setName(functionName)
                .setNamespace(Namespaces.DATE)
                .setParameters(Map.ofEntries(

                        Parameter.ofEntry(ISO_DATE1, Schema.ofRef(Namespaces.DATE
                                + ".timeStamp")),

                        Parameter.ofEntry(ISO_DATE2, Schema.ofRef(
                                Namespaces.DATE + ".timeStamp")),

                        Parameter.ofEntry(paramName, Schema.ofRef(
                                Namespaces.DATE + ".timeStamp")),

                        Parameter.ofEntry(TIME_UNIT,
                                new Schema().setEnums(
                                        List.of(
                                                new JsonPrimitive(YEAR),
                                                new JsonPrimitive(MONTH),
                                                new JsonPrimitive(DAY),
                                                new JsonPrimitive(HOUR),
                                                new JsonPrimitive(MINUTE),
                                                new JsonPrimitive(SECOND))),
                                true)

                ))
                .setEvents(Map.ofEntries(Event.outputEventMapEntry(
                        Map.of(OUTPUT, Schema.ofBoolean(OUTPUT)))));
    }

    @Override
    public FunctionSignature getSignature() {
        return this.signature;
    }

    public static Entry<String, ReactiveFunction> ofEntryTwoDateAndBooleanOutput(final String functionName,
            TriFunction<String, String, JsonArray, Boolean> triFunction) {

        return Map.entry(functionName,
                new AbstractCompareDateFunction(functionName) {

                    @Override
                    protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {

                        String firstDate = context.getArguments().get(ISO_DATE1).getAsString();

                        String secondDate = context.getArguments().get(ISO_DATE2).getAsString();

                        if (!IsValidIsoDateTime.checkValidity(firstDate))
                            throw new KIRuntimeException("Please provide the valid ISO date for " + ISO_DATE1);

                        if (!IsValidIsoDateTime.checkValidity(secondDate))
                            throw new KIRuntimeException("Please provide the valid ISO date for " + ISO_DATE2);

                        JsonArray arr = context.getArguments().get(TIME_UNIT).getAsJsonArray();

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
            String paramName,
            QuadFunction<String, String, String, JsonArray, Boolean> quadFunction) {

        return Map.entry(functionName,
                new AbstractCompareDateFunction(functionName, paramName) {

                    @Override
                    protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {

                        String firstDate = context.getArguments().get(ISO_DATE1).getAsString();

                        String secondDate = context.getArguments().get(ISO_DATE2).getAsString();

                        String thirdDate = context.getArguments().get(paramName).getAsString();

                        if (!IsValidIsoDateTime.checkValidity(firstDate))
                            throw new KIRuntimeException(ERROR_MSG + ISO_DATE1);

                        if (!IsValidIsoDateTime.checkValidity(secondDate))
                            throw new KIRuntimeException(ERROR_MSG + ISO_DATE2);

                        if (!IsValidIsoDateTime.checkValidity(thirdDate))
                            throw new KIRuntimeException(ERROR_MSG + paramName);

                        JsonArray arr = context.getArguments().get(TIME_UNIT).getAsJsonArray();

                        int size = arr.size();

                        if (size == 0)
                            throw new KIRuntimeException("Please provide a unit for checking");

                        return Mono.just(new FunctionOutput(
                                List.of(EventResult.outputOf(Map.of(OUTPUT,
                                        new JsonPrimitive(
                                                quadFunction.apply(firstDate, secondDate, thirdDate, arr)))))));
                    }
                });
    }
}
