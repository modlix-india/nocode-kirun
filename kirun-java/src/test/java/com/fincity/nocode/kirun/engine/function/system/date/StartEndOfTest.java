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

class StartEndOfTest {
    @BeforeAll
    public static void setup() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    @Test
    void testStartOfYear() {

        ReactiveFunctionExecutionParameters parameters = new ReactiveFunctionExecutionParameters(
                new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
                .setArguments(Map.of(StartEndOf.PARAMETER_TIMESTAMP_NAME, new JsonPrimitive("2023-12-31T22:00:00.000Z"),
                        StartEndOf.PARAMETER_UNIT_NAME, new JsonPrimitive("YEARS")));

        StartEndOf startEndOf = new StartEndOf(true);

        StepVerifier
                .create(startEndOf.execute(parameters)
                        .map(e -> e.allResults().get(0).getResult().get(AbstractDateFunction.EVENT_TIMESTAMP_NAME)
                                .getAsString()))
                .expectNext("2023-01-01T00:00:00.000Z")
                .verifyComplete();

    }

    @Test
    void testEndOfHour() {
        ReactiveFunctionExecutionParameters parameters = new ReactiveFunctionExecutionParameters(
                new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
                .setArguments(
                        Map.of(StartEndOf.PARAMETER_TIMESTAMP_NAME, new JsonPrimitive("2023-12-31T22:23:00.000-05:00"),
                                StartEndOf.PARAMETER_UNIT_NAME, new JsonPrimitive("HOURS")));

        StartEndOf startEndOf = new StartEndOf(false);

        StepVerifier
                .create(startEndOf.execute(parameters)
                        .map(e -> e.allResults().get(0).getResult().get(AbstractDateFunction.EVENT_TIMESTAMP_NAME)
                                .getAsString()))
                .expectNext("2023-12-31T22:59:59.999-05:00")
                .verifyComplete();
    }

    @Test
    void testStartOfDay() {
        ReactiveFunctionExecutionParameters parameters = new ReactiveFunctionExecutionParameters(
                new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
                .setArguments(
                        Map.of(StartEndOf.PARAMETER_TIMESTAMP_NAME, new JsonPrimitive("2023-12-31T22:23:00.000-03:00"),
                                StartEndOf.PARAMETER_UNIT_NAME, new JsonPrimitive("DAYS")));

        StartEndOf startEndOf = new StartEndOf(true);

        StepVerifier
                .create(startEndOf.execute(parameters)
                        .map(e -> e.allResults().get(0).getResult().get(AbstractDateFunction.EVENT_TIMESTAMP_NAME)
                                .getAsString()))
                .expectNext("2023-12-31T00:00:00.000-03:00")
                .verifyComplete();
    }

    @Test
    void testEndOfDay() {
        ReactiveFunctionExecutionParameters parameters = new ReactiveFunctionExecutionParameters(
                new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
                .setArguments(
                        Map.of(StartEndOf.PARAMETER_TIMESTAMP_NAME, new JsonPrimitive("2023-12-31T22:23:00.000-03:00"),
                                StartEndOf.PARAMETER_UNIT_NAME, new JsonPrimitive("DAYS")));

        StartEndOf startEndOf = new StartEndOf(false);

        StepVerifier
                .create(startEndOf.execute(parameters)
                        .map(e -> e.allResults().get(0).getResult().get(AbstractDateFunction.EVENT_TIMESTAMP_NAME)
                                .getAsString()))
                .expectNext("2023-12-31T23:59:59.999-03:00")
                .verifyComplete();
    }

    @Test
    void testEndOfYear() {
        ReactiveFunctionExecutionParameters parameters = new ReactiveFunctionExecutionParameters(
                new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
                .setArguments(
                        Map.of(StartEndOf.PARAMETER_TIMESTAMP_NAME, new JsonPrimitive("2023-12-31T22:00:00.000-06:00"),
                                StartEndOf.PARAMETER_UNIT_NAME, new JsonPrimitive("YEARS")));

        StartEndOf startEndOf = new StartEndOf(false);

        StepVerifier
                .create(startEndOf.execute(parameters)
                        .map(e -> e.allResults().get(0).getResult().get(AbstractDateFunction.EVENT_TIMESTAMP_NAME)
                                .getAsString()))
                .expectNext("2023-12-31T23:59:59.999-06:00")
                .verifyComplete();
    }
}
