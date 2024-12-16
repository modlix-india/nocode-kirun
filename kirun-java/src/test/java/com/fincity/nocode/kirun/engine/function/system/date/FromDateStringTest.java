package com.fincity.nocode.kirun.engine.function.system.date;

import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonPrimitive;
import java.time.LocalDate;
import java.util.Map;
import java.util.TimeZone;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

class FromDateStringTest {
    @BeforeAll
    public static void setup() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    @Test
    void testFromDateString() {
        FromDateString fromDateString = new FromDateString();
        ReactiveFunctionExecutionParameters parameters = new ReactiveFunctionExecutionParameters(
                new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
                .setArguments(
                        Map.of(FromDateString.PARAMETER_TIMESTAMP_STRING_NAME,
                                new JsonPrimitive("2024-03"),
                                FromDateString.PARAMETER_FORMAT_NAME,
                                new JsonPrimitive("yyyy-dd")));

        StepVerifier.create(fromDateString.execute(parameters)
                .map(functionOutput -> functionOutput.allResults().get(0).getResult()
                        .get(FromDateString.EVENT_TIMESTAMP_NAME).getAsString()))
                .expectNext("2024-" + LocalDate.now().getMonthValue() + "-03T00:00:00.000Z")
                .verifyComplete();
    }
}
