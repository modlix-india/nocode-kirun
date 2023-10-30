package com.fincity.nocode.kirun.engine.function.system.date;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class DifferenceOfTimeStampsTest {

    DifferenceOfTimeStamps dfts = new DifferenceOfTimeStamps();

    ReactiveFunctionExecutionParameters rfep = new ReactiveFunctionExecutionParameters(
            new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository());

    @Test
    void test1() {

        rfep.setArguments(Map.of("dateone", new JsonPrimitive("2023-10-25T13:30:04.970+07:00"),
                "datetwo", new JsonPrimitive("2023-10-25T19:30:04.970+01:30")));

        StepVerifier.create(dfts.execute(rfep))
                .expectNextMatches(r -> r.next().getResult().get("difference").getAsLong() == -41400000L)
                .verifyComplete();

    }

    @Test
    void test2() {

        rfep.setArguments(Map.of("dateone", new JsonPrimitive("2023-10-25T13:30:04.970+07:00")));

        StepVerifier.create(dfts.execute(rfep))
                .expectError()
                .verify();

        rfep.setArguments(Map.of("datetwo", new JsonPrimitive("2023-10-25T13:30:04.970+07:00")));

        StepVerifier.create(dfts.execute(rfep))
                .expectError()
                .verify();

        rfep.setArguments(Map.of("dateone", new JsonPrimitive("2023-10-25T13:30:04.970+07:00"),
                "datetwo", new JsonPrimitive("2023-10-25T19:30:94.970+01:30")));

        StepVerifier.create(dfts.execute(rfep))
                .expectErrorMessage("Please provide the valid ISO date for datetwo")
                .verify();

    }

    @Test
    void test5() {

        rfep.setArguments(Map.of("dateone", new JsonPrimitive("2023-10-25T73:30:04.970+07:00"),
                "datetwo", new JsonPrimitive("2023-10-25T19:30:54.970+01:30")));

        StepVerifier.create(dfts.execute(rfep))
                .expectErrorMessage("Please provide the valid ISO date for dateone")
                .verify();

    }

    @Test
    void test6() {

        rfep.setArguments(Map.of("dateone", new JsonPrimitive("2023-10-26T06:40:33.807Z"),
                "datetwo", new JsonPrimitive("2023-10-26T06:40:12.334Z")));

        StepVerifier.create(dfts.execute(rfep))
                .expectNextMatches(r -> r.next().getResult().get("difference").getAsLong() == 21473L)
                .verifyComplete();

    }

    @Test
    void test7() {

        rfep.setArguments(Map.of("dateone", new JsonPrimitive("2023-10-26T06:40:33.807Z"),
                "datetwo", new JsonPrimitive("2023-10-26T06:40:33.100Z")));

        StepVerifier.create(dfts.execute(rfep))
                .expectNextMatches(r -> r.next().getResult().get("difference").getAsLong() == 707)
                .verifyComplete();

    }

    @Test
    void test8() {

        String a = "2023-10-26T06:40:33.807Z";

        String b = "2023-10-25T19:30:54.970+01:30";

        String c = "2023-10-26T06:40:333Z";

        String d = "2023-10-26T19:30:54.975+01:30";

        rfep.setArguments(Map.of("dateone", new JsonPrimitive(a),
                "datetwo", new JsonPrimitive(b)));

        StepVerifier.create(dfts.execute(rfep))
                .expectNextMatches(r -> r.next().getResult().get("difference").getAsLong() == 45578837L)
                .verifyComplete();

        rfep.setArguments(Map.of("dateone", new JsonPrimitive(c),
                "datetwo", new JsonPrimitive(d)));

        StepVerifier.create(dfts.execute(rfep))
                .expectError().verify();

    }

}
