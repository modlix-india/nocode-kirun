package com.fincity.nocode.kirun.engine.function.system.date;

import java.util.Map;
import java.util.TimeZone;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.exception.KIRuntimeException;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

public class LastFirstOfTest {
    @BeforeAll
    public static void setup() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    @Test
    void testLastOf() {

        LastFirstOf lastFirstOf = new LastFirstOf(true);

        JsonArray array = new JsonArray();
        array.add(new JsonPrimitive("2024-01-03T05:50:00.000+05:30"));
        array.add(new JsonPrimitive("2024-01-02T00:00:00.000Z"));
        array.add(new JsonPrimitive("2024-01-03T10:00:00.000Z"));

        ReactiveFunctionExecutionParameters parameters = new ReactiveFunctionExecutionParameters(
                new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
                .setArguments(Map.of(AbstractDateFunction.PARAMETER_TIMESTAMP_NAME, array));

        StepVerifier
                .create(lastFirstOf.execute(parameters)
                        .map(e -> e.allResults().get(0).getResult().get(AbstractDateFunction.EVENT_TIMESTAMP_NAME)
                                .getAsString()))
                .expectNext("2024-01-03T10:00:00.000Z")
                .verifyComplete();
    }

    @Test
    void testFirstOf() {
        LastFirstOf lastFirstOf = new LastFirstOf(false);

        JsonArray array = new JsonArray();
        array.add(new JsonPrimitive("2024-01-03T05:50:00.000+05:30"));
        array.add(new JsonPrimitive("2024-01-02T00:00:00.000Z"));
        array.add(new JsonPrimitive("2024-01-03T10:00:00.000Z"));

        ReactiveFunctionExecutionParameters parameters = new ReactiveFunctionExecutionParameters(
                new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
                .setArguments(Map.of(AbstractDateFunction.PARAMETER_TIMESTAMP_NAME, array));

        StepVerifier
                .create(lastFirstOf.execute(parameters)
                        .map(e -> e.allResults().get(0).getResult().get(AbstractDateFunction.EVENT_TIMESTAMP_NAME)
                                .getAsString()))
                .expectNext("2024-01-02T00:00:00.000Z")
                .verifyComplete();
    }

    @Test
    void testLastOfWithInvalidDate() {
        LastFirstOf lastFirstOf = new LastFirstOf(false);

        JsonArray array = new JsonArray();

        ReactiveFunctionExecutionParameters parameters = new ReactiveFunctionExecutionParameters(
                new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
                .setArguments(Map.of(AbstractDateFunction.PARAMETER_TIMESTAMP_NAME, array));

        StepVerifier
                .create(lastFirstOf.execute(parameters)
                        .map(e -> e.allResults().get(0).getResult().get(AbstractDateFunction.EVENT_TIMESTAMP_NAME)
                                .getAsString()))
                .expectError(KIRuntimeException.class)
                .verify();
    }
}
