package com.fincity.nocode.kirun.engine.function.system.date;

import java.util.Map;
import java.util.TimeZone;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;

import reactor.test.StepVerifier;

class GetCurrentTimestampTest {

    @BeforeAll
    public static void setup() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    @Test
    void testGetCurrentTimestamp() {
        GetCurrentTimestamp function = new GetCurrentTimestamp();

        ReactiveFunctionExecutionParameters parameters = new ReactiveFunctionExecutionParameters(
                new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
                .setArguments(Map.of());

        StepVerifier
                .create(function.execute(parameters)
                        .map(e -> e.allResults().get(0).getResult().get(GetCurrentTimestamp.EVENT_TIMESTAMP_NAME)))
                .expectNextMatches(jsonElement -> {
                    System.out.println(jsonElement);
                    return jsonElement.isJsonPrimitive() && jsonElement.getAsJsonPrimitive().isString();
                })
                .verifyComplete();
    }
}
