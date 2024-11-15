package com.fincity.nocode.kirun.engine.function.system.date;

import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

public class TimeAsTest {
    @BeforeAll
    public static void setup() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    @Test
    void testTimeAsArray() {

        ReactiveFunctionExecutionParameters parameters = new ReactiveFunctionExecutionParameters(
                new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
                .setArguments(
                        Map.of(TimeAs.PARAMETER_TIMESTAMP_NAME, new JsonPrimitive("2024-11-10T10:10:10.100-05:00")));

        TimeAs timeAs = new TimeAs(true);

        StepVerifier
                .create(timeAs.execute(parameters)
                        .map(e -> e.allResults().get(0).getResult().get(TimeAs.EVENT_TIME_ARRAY_NAME)
                                .getAsJsonArray().asList().stream().map(JsonElement::getAsNumber)
                                .collect(Collectors.toList())))
                .expectNext(List.of(2024, 11, 10, 10, 10, 10, 100))
                .verifyComplete();
    }

    @Test
    void testTimeAsObject() {

        ReactiveFunctionExecutionParameters parameters = new ReactiveFunctionExecutionParameters(
                new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
                .setArguments(
                        Map.of(TimeAs.PARAMETER_TIMESTAMP_NAME, new JsonPrimitive("2024-11-10T10:10:10.100-05:00")));

        TimeAs timeAs = new TimeAs(false);

        JsonObject jsonObject = new JsonObject();
        jsonObject.add("year", new JsonPrimitive(2024));
        jsonObject.add("month", new JsonPrimitive(11));
        jsonObject.add("day", new JsonPrimitive(10));
        jsonObject.add("hour", new JsonPrimitive(10));
        jsonObject.add("minute", new JsonPrimitive(10));
        jsonObject.add("second", new JsonPrimitive(10));
        jsonObject.add("millisecond", new JsonPrimitive(100));

        StepVerifier
                .create(timeAs.execute(parameters)
                        .map(e -> e.allResults().get(0).getResult().get(TimeAs.EVENT_TIME_OBJECT_NAME)
                                .getAsJsonObject()))
                .expectNext(jsonObject)
                .verifyComplete();
    }
}
