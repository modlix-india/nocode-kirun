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

class FromNowTest {

    @BeforeAll
    public static void setup() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    @Test
    void testFromNowFutureDate() {

        FromNow fromNow = new FromNow();

        ReactiveFunctionExecutionParameters parameters = new ReactiveFunctionExecutionParameters(
                new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
                .setArguments(Map.of(FromNow.PARAMETER_BASE_NAME, new JsonPrimitive("2024-11-13T10:00:00Z"),
                        FromNow.PARAMETER_TIMESTAMP_NAME, new JsonPrimitive("2025-11-13T09:00:00Z")));

        StepVerifier.create(fromNow.execute(parameters)
                .map(functionOutput -> functionOutput.allResults().get(0).getResult()
                        .get(FromNow.EVENT_RESULT_NAME).getAsString()))
                .expectNext("1 year ago")
                .verifyComplete();
    }

}