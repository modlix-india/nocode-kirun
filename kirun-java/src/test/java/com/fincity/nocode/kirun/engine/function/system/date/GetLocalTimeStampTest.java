package com.fincity.nocode.kirun.engine.function.system.date;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class GetLocalTimeStampTest {

    GetLocalTimeStamp glt = new GetLocalTimeStamp();

    ReactiveFunctionExecutionParameters rfep = new ReactiveFunctionExecutionParameters(
            new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository());

    @Test
    void test1() {

        rfep.setArguments(Map.of("isodate", new JsonPrimitive("2023-10-05T17:14:20.788Z")));

        StepVerifier.create(glt.execute(rfep))
                .expectNextMatches(r -> {
                    System.out.println(r.next().getResult().get("reslult"));
                    return true;
                }).verifyComplete();

    }

}
