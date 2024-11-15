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

class SetTimeZoneTest {
    @BeforeAll
    public static void setup() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    @Test
    void testSetTimeZone() {

        SetTimeZone function = new SetTimeZone();

        ReactiveFunctionExecutionParameters parameters = new ReactiveFunctionExecutionParameters(
                new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
                .setArguments(Map.of(
                        SetTimeZone.PARAMETER_TIMESTAMP_NAME, new JsonPrimitive("2024-01-01T00:00:00.000Z"),
                        SetTimeZone.PARAMETER_TIMEZONE_NAME, new JsonPrimitive("Asia/Tokyo")));

        StepVerifier
                .create(function.execute(parameters)
                        .map(e -> e.allResults().get(0).getResult().get(AbstractDateFunction.EVENT_TIMESTAMP_NAME)
                                .getAsString()))
                .expectNext("2024-01-01T09:00:00.000+09:00")
                .verifyComplete();
    }
}
