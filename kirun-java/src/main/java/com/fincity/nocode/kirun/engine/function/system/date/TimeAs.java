package com.fincity.nocode.kirun.engine.function.system.date;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.util.List;
import java.util.Map;

import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.model.Event;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.namespaces.Namespaces;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import reactor.core.publisher.Mono;

public class TimeAs extends AbstractDateFunction {

    public static final String EVENT_TIME_OBJECT_NAME = "object";
    public static final String EVENT_TIME_ARRAY_NAME = "array";

    private final boolean isArray;

    public TimeAs(boolean isArray) {
        super(isArray ? "TimeAsArray" : "TimeAsObject",
                new Event()
                        .setName(Event.OUTPUT)
                        .setParameters(Map.of(
                                isArray ? EVENT_TIME_ARRAY_NAME : EVENT_TIME_OBJECT_NAME,
                                isArray
                                        ? Schema.ofArray(
                                                EVENT_TIME_ARRAY_NAME,
                                                Schema.ofInteger("timeParts"))
                                        : Schema.ofRef(Namespaces.DATE + ".TimeObject"))),
                PARAMETER_TIMESTAMP);

        this.isArray = isArray;
    }

    @Override
    protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters parameters) {

        String timestampString = parameters.getArguments().get(PARAMETER_TIMESTAMP_NAME).getAsString();
        ZonedDateTime zdt = DateUtil.getDateTime(timestampString);

        JsonElement output;

        if (this.isArray) {

            JsonArray timeParts = new JsonArray();
            timeParts.add(new JsonPrimitive(zdt.getYear()));
            timeParts.add(new JsonPrimitive(zdt.getMonthValue()));
            timeParts.add(new JsonPrimitive(zdt.getDayOfMonth()));
            timeParts.add(new JsonPrimitive(zdt.getHour()));
            timeParts.add(new JsonPrimitive(zdt.getMinute()));
            timeParts.add(new JsonPrimitive(zdt.getSecond()));
            timeParts.add(new JsonPrimitive(zdt.get(ChronoField.MILLI_OF_SECOND)));
            output = timeParts;
        } else {

            JsonObject map = new JsonObject();
            map.add("year", new JsonPrimitive(zdt.getYear()));
            map.add("month", new JsonPrimitive(zdt.getMonthValue()));
            map.add("day", new JsonPrimitive(zdt.getDayOfMonth()));
            map.add("hour", new JsonPrimitive(zdt.getHour()));
            map.add("minute", new JsonPrimitive(zdt.getMinute()));
            map.add("second", new JsonPrimitive(zdt.getSecond()));
            map.add("millisecond", new JsonPrimitive(zdt.get(ChronoField.MILLI_OF_SECOND)));
            output = map;
        }

        return Mono.just(new FunctionOutput(List.of(
                EventResult.outputOf(
                        Map.of(this.isArray ? EVENT_TIME_ARRAY_NAME : EVENT_TIME_OBJECT_NAME, output)))));
    }
}
