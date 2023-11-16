package com.fincity.nocode.kirun.engine.function.system.date;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class ToDateStringTest {

    ToDateString tds = new ToDateString();

    ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
            new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository());

    @Test
    void test() {

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("2023-07-21T17:04:58.798Z"),
                "dateFormat", new JsonPrimitive("yyyy.MM.DD 'at' HH:mm:ss z")));

        StepVerifier.create(tds.execute(fep))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString().equals("2023.07.21 at 22:34:58 z"))
                .verifyComplete();
    }

}
