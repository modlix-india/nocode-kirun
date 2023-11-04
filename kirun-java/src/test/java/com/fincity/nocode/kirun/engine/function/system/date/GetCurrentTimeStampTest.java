package com.fincity.nocode.kirun.engine.function.system.date;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;

import reactor.test.StepVerifier;

class GetCurrentTimeStampTest {

    GetCurrentTimeStamp gct = new GetCurrentTimeStamp();

    ReactiveFunctionExecutionParameters rfep = new ReactiveFunctionExecutionParameters(
            new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository());

    @Test
    void test() {

        StepVerifier.create(gct.execute(rfep))
                .expectNextMatches(r -> {
                    System.out.println(r.next().getResult().get("timeStamp"));
                    return true;
                })
                .verifyComplete();

    }

}
