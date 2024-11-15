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

class TimestampToEpochTest {

    @BeforeAll
    public static void setup() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    @Test
    void testTimestampToEpochSeconds() {

        ReactiveFunctionExecutionParameters parameters = new ReactiveFunctionExecutionParameters(
                new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
                .setArguments(
                        Map.of(AbstractDateFunction.PARAMETER_TIMESTAMP_NAME,
                                new JsonPrimitive("2024-01-01T00:00:00.000+05:30")));

        StepVerifier
                .create(new TimestampToEpoch("TimestampToEpochSeconds", true).execute(parameters)
                        .map(e -> e.allResults().get(0).getResult()
                                .get(AbstractDateFunction.EVENT_RESULT_NAME)
                                .getAsLong()))
                .expectNext(1704047400L)
                .verifyComplete();
    }

    @Test
    void testTimestampToEpochMilliseconds() {

        ReactiveFunctionExecutionParameters parameters = new ReactiveFunctionExecutionParameters(
                new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
                .setArguments(
                        Map.of(AbstractDateFunction.PARAMETER_TIMESTAMP_NAME,
                                new JsonPrimitive("2024-01-01T00:00:00.000+05:30")));

        StepVerifier
                .create(new TimestampToEpoch("TimestampToEpochMilliseconds", false).execute(parameters)
                        .map(e -> e.allResults().get(0).getResult()
                                .get(AbstractDateFunction.EVENT_RESULT_NAME)
                                .getAsLong()))
                .expectNext(1704047400000L)
                .verifyComplete();
    }
}
