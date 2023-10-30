package com.fincity.nocode.kirun.engine.function.system.date;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class ToNowTest {

    ToNow tn = new ToNow();

    ReactiveFunctionExecutionParameters rfep = new ReactiveFunctionExecutionParameters(
            new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository());

    @Test
    void test() {

        rfep.setArguments(Map.of("isodate", new JsonPrimitive("2023-10-25T19:30:04.970+01:30")));

        StepVerifier.create(tn.execute(rfep))
                .expectNextMatches(r -> r.next().getResult().get("result").getAsString().equals("In 4 days"))
                .verifyComplete();

    }

}
