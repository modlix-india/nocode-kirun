package com.fincity.nocode.kirun.engine.function.system.date;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class FromDateStringTest {

    FromDateString fds = new FromDateString();

    ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
            new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository());

    @Test
    void test() {

//        fep.setArguments(Map.of("date", new JsonPrimitive("2023 4th"), "dateFormat", new JsonPrimitive("YYYY Qth")));
//
//        StepVerifier.create(fds.execute(fep))
//                .expectNextMatches(
//                        r -> r.next().getResult().get("result").getAsString().equals("2023-09-30T18:30:00.000Z"))
//                .verifyComplete();

        fep.setArguments(Map.of("date", new JsonPrimitive("2001.07.04 CE at 17:38:56 +05:30"),
                "dateFormat", new JsonPrimitive("YYYY.MM.DD NN 'at' HH:mm:ss Z")));

        StepVerifier.create(fds.execute(fep))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString().equals("2001-07-04T12:08:56.000Z"))
                .verifyComplete();
    }

}
