package com.fincity.nocode.kirun.engine.function.system.date;

import java.util.Map;
import java.util.TimeZone;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class AddSubtractTimeTest {

    @BeforeAll
    public static void setup() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    @Test
    void testAddTimeWithNoArguments() {
        AddSubtractTime addSubtractTime = new AddSubtractTime(true);
        ReactiveFunctionExecutionParameters parameters = new ReactiveFunctionExecutionParameters(
                new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
                .setArguments(
                        Map.of(AddSubtractTime.PARAMETER_TIMESTAMP_NAME,
                                new JsonPrimitive("2024-11-11T14:55:00+05:30")));

        StepVerifier.create(addSubtractTime.execute(parameters)
                .map(functionOutput -> functionOutput.allResults().get(0).getResult()
                        .get(AddSubtractTime.EVENT_TIMESTAMP_NAME).getAsString()))
                .expectNext("2024-11-11T14:55:00+05:30")
                .verifyComplete();
    }

    @Test
    void testAddTimeSomeUnits() {

        AddSubtractTime addSubtractTime = new AddSubtractTime(true);
        ReactiveFunctionExecutionParameters parameters = new ReactiveFunctionExecutionParameters(
                new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
                .setArguments(
                        Map.of(AddSubtractTime.PARAMETER_TIMESTAMP_NAME,
                                new JsonPrimitive("2024-11-11T14:55:00.000+05:30"),
                                AddSubtractTime.PARAMETER_YEARS_NAME, new JsonPrimitive(1),
                                AddSubtractTime.PARAMETER_MONTHS_NAME, new JsonPrimitive(1),
                                AddSubtractTime.PARAMETER_DAYS_NAME, new JsonPrimitive(1),
                                AddSubtractTime.PARAMETER_HOURS_NAME, new JsonPrimitive(1),
                                AddSubtractTime.PARAMETER_MINUTES_NAME, new JsonPrimitive(1),
                                AddSubtractTime.PARAMETER_SECONDS_NAME, new JsonPrimitive(1),
                                AddSubtractTime.PARAMETER_MILLISECONDS_NAME, new JsonPrimitive(33)));

        StepVerifier.create(addSubtractTime.execute(parameters)
                .map(functionOutput -> functionOutput.allResults().get(0).getResult()
                        .get(AddSubtractTime.EVENT_TIMESTAMP_NAME).getAsString()))
                .expectNext("2025-12-12T15:56:01.033+05:30")
                .verifyComplete();
    }

    @Test
    void testSubtractTimeSomeUnits() {

        AddSubtractTime addSubtractTime = new AddSubtractTime(false);
        ReactiveFunctionExecutionParameters parameters = new ReactiveFunctionExecutionParameters(
                new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
                .setArguments(
                        Map.of(AddSubtractTime.PARAMETER_TIMESTAMP_NAME,
                                new JsonPrimitive("2024-11-11T14:55:00+05:30"),
                                AddSubtractTime.PARAMETER_MINUTES_NAME, new JsonPrimitive(1),
                                AddSubtractTime.PARAMETER_SECONDS_NAME, new JsonPrimitive(1),
                                AddSubtractTime.PARAMETER_MILLISECONDS_NAME, new JsonPrimitive(33)));

        StepVerifier.create(addSubtractTime.execute(parameters)
                .map(functionOutput -> functionOutput.allResults().get(0).getResult()
                        .get(AddSubtractTime.EVENT_TIMESTAMP_NAME).getAsString()))
                .expectNext("2024-11-11T14:53:58.967+05:30")
                .verifyComplete();
    }
}
