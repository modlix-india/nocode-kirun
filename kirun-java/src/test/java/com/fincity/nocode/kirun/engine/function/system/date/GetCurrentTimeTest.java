package com.fincity.nocode.kirun.engine.function.system.date;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;

import reactor.test.StepVerifier;

class GetCurrentTimeTest {

    GetCurrentTime gct = new GetCurrentTime();
    ReactiveFunctionExecutionParameters rfep = new ReactiveFunctionExecutionParameters(
            new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository());

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    @Test
    void test() {

        Date currentDate = new Date();

        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        StepVerifier.create(gct.execute(rfep))
                .expectNextMatches(r -> {
                    return r.next().getResult().get("time").getAsString() != sdf.format(currentDate);
                })
                .verifyComplete();
    }

    @Test
    void test2() {

        Date currentDate = new Date();

        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        StepVerifier.create(gct.execute(rfep))
                .expectNextMatches(r -> {
                    return r.next().getResult().get("time").getAsString() != sdf.format(currentDate);
                })
                .verifyComplete();
    }

}
