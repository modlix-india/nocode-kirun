package com.fincity.nocode.kirun.engine.function.system.date;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.namespaces.Namespaces;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class DstAndLeapYearTest {

    DateFunctionRepository dfr = new DateFunctionRepository();

    ReactiveFunctionExecutionParameters rfep = new ReactiveFunctionExecutionParameters(
            new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository());

    @Test
    void leapYearTest() {

        rfep.setArguments(Map.of("isoDate", new JsonPrimitive("2023-12-10T10:02:54.959Z")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "IsLeapYear").flatMap(e -> e.execute(rfep)))
                .expectNextMatches(r -> !r.next().getResult().get("result").getAsBoolean())
                .verifyComplete();

        rfep.setArguments(Map.of("isoDate", new JsonPrimitive("2020-10-10T10:02:54.959-12:12")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "IsLeapYear").flatMap(e -> e.execute(rfep)))
                .expectNextMatches(r -> r.next().getResult().get("result").getAsBoolean())
                .verifyComplete();

        rfep.setArguments(Map.of("isoDate", new JsonPrimitive("2020-10-10T10:02:54.959-12:12")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "IsLeapYear").flatMap(e -> e.execute(rfep)))
                .expectNextMatches(r -> r.next().getResult().get("result").getAsBoolean())
                .verifyComplete();

        rfep.setArguments(Map.of("isoDate", new JsonPrimitive("2021-02-29T14:10:30.700+12:21")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "IsLeapYear").flatMap(e -> e.execute(rfep)))
                .expectError().verify();

        rfep.setArguments(Map.of("isoDate", new JsonPrimitive("2020-02-29T14:10:30.700+12:21")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "IsLeapYear").flatMap(e -> e.execute(rfep)))
                .expectNextMatches(r -> r.next().getResult().get("result").getAsBoolean())
                .verifyComplete();
    }

}
