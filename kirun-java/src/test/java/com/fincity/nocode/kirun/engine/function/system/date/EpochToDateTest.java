package com.fincity.nocode.kirun.engine.function.system.date;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class EpochToDateTest {

    EpochToDate etd = new EpochToDate();
    ReactiveFunctionExecutionParameters rfep = new ReactiveFunctionExecutionParameters(
            new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository());

    @Test
    void integerTest() {

        rfep.setArguments(Map.of("epoch", new JsonPrimitive(1696494131)));

        StepVerifier.create(etd.execute(rfep))
                .expectNextMatches(r -> {
                    return r.next().getResult().get("date").getAsString().equals("2023-10-05T13:52:11.000Z");
                })
                .verifyComplete();
    }

    @Test
    void longTest() {

        rfep.setArguments(Map.of("epoch", new JsonPrimitive(16964941310L)));

        StepVerifier.create(etd.execute(rfep))
                .expectNextMatches(r -> {
                    return r.next().getResult().get("date").getAsString().equals("2507-08-07T17:11:50.000Z");
                })
                .verifyComplete();
    }

    @Test
    void stringTest() {

        rfep.setArguments(Map.of("epoch", new JsonPrimitive("1696494131000")));

        StepVerifier.create(etd.execute(rfep))
                .expectNextMatches(r -> {
                    return r.next().getResult().get("date").getAsString().equals("55729-10-04T23:13:20.000Z");
                })
                .verifyComplete();
    }

    @Test
    void stringTest2() {

        rfep.setArguments(Map.of("epoch", new JsonPrimitive("16964941310")));

        StepVerifier.create(etd.execute(rfep))
                .expectNextMatches(r -> {
                    return r.next().getResult().get("date").getAsString().equals("2507-08-07T17:11:50.000Z");
                })
                .verifyComplete();

    }

    @Test
    void stringTest3() {
        rfep.setArguments(Map.of("epoch", new JsonPrimitive("1696494131")));

        StepVerifier.create(etd.execute(rfep))
                .expectNextMatches(r -> {
                    return r.next().getResult().get("date").getAsString().equals("2023-10-05T13:52:11.000Z");
                })
                .verifyComplete();
    }

}
