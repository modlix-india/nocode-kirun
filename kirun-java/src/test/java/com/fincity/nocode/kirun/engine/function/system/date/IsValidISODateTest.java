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

class IsValidISODateTest {

    @BeforeAll
    public static void setup() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    @Test
    void testIsValidISODate() {

        ReactiveFunctionExecutionParameters parameters = new ReactiveFunctionExecutionParameters(
                new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
                .setArguments(Map.of(AbstractDateFunction.PARAMETER_TIMESTAMP_NAME,
                        new JsonPrimitive("2024-01-01T00:00:00Z")));

        IsValidISODate function = new IsValidISODate();

        StepVerifier
                .create(function.execute(parameters)
                        .map(e -> e.allResults().get(0).getResult().get(AbstractDateFunction.EVENT_RESULT_NAME)
                                .getAsBoolean()))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void testIsValidISODateWithInvalidDate() {

        ReactiveFunctionExecutionParameters parameters = new ReactiveFunctionExecutionParameters(
                new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
                .setArguments(Map.of(AbstractDateFunction.PARAMETER_TIMESTAMP_NAME,
                        new JsonPrimitive("invalid")));

        IsValidISODate function = new IsValidISODate();

        StepVerifier
                .create(function.execute(parameters)
                        .map(e -> e.allResults().get(0).getResult().get(AbstractDateFunction.EVENT_RESULT_NAME)
                                .getAsBoolean()))
                .expectNext(false)
                .verifyComplete();
    }
}
