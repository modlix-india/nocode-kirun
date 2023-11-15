package com.fincity.nocode.kirun.engine.function.system.date;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class IsValidISODateTest {

    IsValidISODate validDate = new IsValidISODate();

    ReactiveFunctionExecutionParameters rfep = new ReactiveFunctionExecutionParameters(
            new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository());

    @Test
    void test1() {

        rfep.setArguments(Map.of("isoDate", new JsonPrimitive("aws")));

        StepVerifier.create(validDate.execute(rfep))
                .expectNextMatches(r -> !r.next().getResult().get("output").getAsBoolean())
                .verifyComplete();

        rfep
                .setArguments(Map.of("isoDate", new JsonPrimitive("2023-10-04T11:45:38.939ss")));

        StepVerifier.create(validDate.execute(rfep))
                .expectNextMatches(r -> !r.next().getResult().get("output").getAsBoolean())
                .verifyComplete();

        rfep
                .setArguments(Map.of("isoDate", new JsonPrimitive("2023-10-04T11:45:38.939Z")));

        StepVerifier.create(validDate.execute(rfep))
                .expectNextMatches(r -> r.next().getResult().get("output").getAsBoolean())
                .verifyComplete();

        rfep
                .setArguments(Map.of("isoDate", new JsonPrimitive("2023-10-10T10:02:54.959Z")));

        StepVerifier.create(validDate.execute(rfep))
                .expectNextMatches(r -> r.next().getResult().get("output").getAsBoolean())
                .verifyComplete();

        rfep
                .setArguments(Map.of("isoDate", new JsonPrimitive("2023-10-10T10:02:54.959-12:12")));

        StepVerifier.create(validDate.execute(rfep))
                .expectNextMatches(r -> r.next().getResult().get("output").getAsBoolean())
                .verifyComplete();

        rfep.setArguments(Map.of("isoDate", new JsonPrimitive("2023-10-10T10:02:54.99-34:12")));

        StepVerifier.create(validDate.execute(rfep))
                .expectNextMatches(r -> !r.next().getResult().get("output").getAsBoolean())
                .verifyComplete();

        rfep.setArguments(Map.of("isoDate", new JsonPrimitive("2023-10-10T10:02:54.000Z")));

        StepVerifier.create(validDate.execute(rfep))
                .expectNextMatches(r -> r.next().getResult().get("output").getAsBoolean())
                .verifyComplete();

        rfep.setArguments(Map.of("isoDate", new JsonPrimitive("2023-10-4T14:10:30.700+56:70")));

        StepVerifier.create(validDate.execute(rfep))
                .expectNextMatches(r -> !r.next().getResult().get("output").getAsBoolean())
                .verifyComplete();

        rfep
                .setArguments(Map.of("isoDate", new JsonPrimitive("2023-12-87T10:02:54.959")));

        StepVerifier.create(validDate.execute(rfep))
                .expectNextMatches(r -> !r.next().getResult().get("output").getAsBoolean())
                .verifyComplete();

        rfep
                .setArguments(Map.of("isoDate", new JsonPrimitive("2020-02-29T14:10:30.700+12:21")));

        StepVerifier.create(validDate.execute(rfep))
                .expectNextMatches(r -> r.next().getResult().get("output").getAsBoolean())
                .verifyComplete();

        rfep
                .setArguments(Map.of("isoDate", new JsonPrimitive("2022-02-29T14:10:30.700+12:21")));

        StepVerifier.create(validDate.execute(rfep))
                .expectNextMatches(r -> !r.next().getResult().get("output").getAsBoolean())
                .verifyComplete();

        rfep
                .setArguments(Map.of("isoDate", new JsonPrimitive("2020-02-28T14:10:30.700+12:21")));

        StepVerifier.create(validDate.execute(rfep))
                .expectNextMatches(r -> r.next().getResult().get("output").getAsBoolean())
                .verifyComplete();

        rfep
                .setArguments(Map.of("isoDate", new JsonPrimitive("2023-10-00T11:45:38.939Z")));

        StepVerifier.create(validDate.execute(rfep))
                .expectNextMatches(r -> !r.next().getResult().get("output").getAsBoolean())
                .verifyComplete();
    }
}