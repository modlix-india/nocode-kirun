package com.fincity.nocode.kirun.engine.function.system.date;

import java.util.Date;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;

import reactor.test.StepVerifier;

class GetCurrentTimeStampTest {

    GetCurrentTimeStamp gct = new GetCurrentTimeStamp();
    ReactiveFunctionExecutionParameters rfep = new ReactiveFunctionExecutionParameters(
            new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository());

    Date d = new Date();

    @Test
    void test() {

        StepVerifier.create(gct.execute(rfep))
                .expectNextMatches(r -> {
                    return r.next().getResult().get("time").getAsLong() != d.getTime();
                })
                .verifyComplete();
    }

    @Test
    void test2() {

        StepVerifier.create(gct.execute(rfep))
                .expectNextMatches(r -> {
                    return r.next().getResult().get("time").getAsLong() != d.getTime();
                })
                .verifyComplete();
    }

}
