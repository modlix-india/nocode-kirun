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

class ToDateStringTest {

    @BeforeAll
    public static void setup() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    @Test
    void testToDateString() {

        ReactiveFunctionExecutionParameters parameters = new ReactiveFunctionExecutionParameters(
                new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
                .setArguments(Map.of(AbstractDateFunction.PARAMETER_TIMESTAMP_NAME,
                        new JsonPrimitive("2024-11-10T10:10:10.100-05:00"),
                        ToDateString.PARAMETER_FORMAT_NAME, new JsonPrimitive("DDD")));

        StepVerifier.create(new ToDateString().execute(parameters).map(e -> e.allResults().get(0).getResult()
                .get(AbstractDateFunction.EVENT_RESULT_NAME)
                .getAsString()))
                .expectNext("November 10, 2024")
                .verifyComplete();
    }
}
