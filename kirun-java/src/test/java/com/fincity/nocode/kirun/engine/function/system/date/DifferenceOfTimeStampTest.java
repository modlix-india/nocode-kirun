package com.fincity.nocode.kirun.engine.function.system.date;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class DifferenceOfTimeStampTest {

    DifferenceOfTimeStamp dfts = new DifferenceOfTimeStamp();

    ReactiveFunctionExecutionParameters rfep = new ReactiveFunctionExecutionParameters(
            new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository());

    @Test
    void test() {

        rfep.setArguments(Map.of("isoDate1", new JsonPrimitive("2023-10-25T13:30:04.970+07:00"),
                "isoDate2", new JsonPrimitive("2023-10-25T19:30:04.970+01:30")));

        StepVerifier.create(dfts.execute(rfep))
                .expectNextMatches(r -> r.next().getResult().get("result").getAsLong() == -41400000L)
                .verifyComplete();

        rfep.setArguments(Map.of("isoDate1", new JsonPrimitive("2023-10-25T13:30:04.970+07:00")));

        StepVerifier.create(dfts.execute(rfep))
                .expectError()
                .verify();

        rfep.setArguments(Map.of("isoDate2", new JsonPrimitive("2023-10-25T13:30:04.970+07:00")));

        StepVerifier.create(dfts.execute(rfep))
                .expectError()
                .verify();

        rfep.setArguments(Map.of("isoDate1", new JsonPrimitive("2023-10-25T13:30:04.970+07:00"),
                "isoDate2", new JsonPrimitive("2023-10-25T19:30:94.970+01:30")));

        StepVerifier.create(dfts.execute(rfep))
                .expectErrorMessage("Please provide the valid ISO date for isoDate2")
                .verify();

        rfep.setArguments(Map.of("isoDate1", new JsonPrimitive("2023-10-25T73:30:04.970+07:00"),
                "isoDate2", new JsonPrimitive("2023-10-25T19:30:54.970+01:30")));

        StepVerifier.create(dfts.execute(rfep))
                .expectErrorMessage("Please provide the valid ISO date for isoDate1")
                .verify();
    }

    @Test
    void test2() {
        
        rfep.setArguments(Map.of("isoDate1", new JsonPrimitive("2023-10-26T06:40:33.807Z"),
                "isoDate2", new JsonPrimitive("2023-10-26T06:40:12.334Z")));

        StepVerifier.create(dfts.execute(rfep))
                .expectNextMatches(r -> r.next().getResult().get("result").getAsLong() == 21473L)
                .verifyComplete();

        rfep.setArguments(Map.of("isoDate1", new JsonPrimitive("2023-10-26T06:40:33.807Z"),
                "isoDate2", new JsonPrimitive("2023-10-26T06:40:33.100Z")));

        StepVerifier.create(dfts.execute(rfep))
                .expectNextMatches(r -> r.next().getResult().get("result").getAsLong() == 707)
                .verifyComplete();

        String a = "2023-10-26T06:40:33.807Z";

        String b = "2023-10-25T19:30:54.970+01:30";

        String c = "2023-10-26T06:40:333Z";

        String d = "2023-10-26T19:30:54.975+01:30";

        rfep.setArguments(Map.of("isoDate1", new JsonPrimitive(a),
                "isoDate2", new JsonPrimitive(b)));

        StepVerifier.create(dfts.execute(rfep))
                .expectNextMatches(r -> r.next().getResult().get("result").getAsLong() == 45578837L)
                .verifyComplete();

        rfep.setArguments(Map.of("isoDate1", new JsonPrimitive(c),
                "isoDate2", new JsonPrimitive(d)));

        StepVerifier.create(dfts.execute(rfep))
                .expectError().verify();

    }

}
