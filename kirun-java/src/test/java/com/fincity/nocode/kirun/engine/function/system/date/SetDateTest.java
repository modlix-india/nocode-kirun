package com.fincity.nocode.kirun.engine.function.system.date;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.namespaces.Namespaces;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class SetDateTest {

    DateFunctionRepository dfr = new DateFunctionRepository();

    ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
            new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository());

    String message = "Invalid ISO 8601 Date format.";

    @Test
    void failTest() {

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("abcd"), "dateValue", new JsonPrimitive(121312)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SetDate")
                .flatMap(e -> e.execute(fep)))
                .expectErrorMessage(message)
                .verify();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("202312=12"), "dateValue", new JsonPrimitive(1245)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SetDate")
                .flatMap(e -> e.execute(fep)))
                .expectError()
                .verify();
    }

    @Test
    void validTest() {

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("-092023-10-04T11:45:38.939-04:00"), "dateValue",
                new JsonPrimitive(12)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SetDate")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(r -> r.next().getResult().get("result").getAsString()
                        .equals("-092023-10-12T11:45:38.939-04:00"))
                .verifyComplete();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("2023-09-03T17:35:17.000Z"), "dateValue",
                new JsonPrimitive(18)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SetDate")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(r -> r.next().getResult().get("result").getAsString()
                        .equals("2023-09-18T17:35:17.000Z"))
                .verifyComplete();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("1970-01-20T15:58:57.561Z"), "dateValue",
                new JsonPrimitive(31)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SetDate")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(r -> r.next().getResult().get("result").getAsString()
                        .equals("1970-01-31T15:58:57.561Z"))
                .verifyComplete();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("2023-10-19T06:44:11.615-05:12"), "dateValue",
                new JsonPrimitive(32)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SetDate")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(r -> r.next().getResult().get("result").getAsString()
                        .equals("2023-11-01T06:44:11.615-05:12"))
                .verifyComplete();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("2023-10-24T14:10:30.700+05:09"), "dateValue",
                new JsonPrimitive(40)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SetDate")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(r -> r.next().getResult().get("result").getAsString()
                        .equals("2023-11-09T14:10:30.700+05:09"))
                .verifyComplete();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("1994-10-24T14:05:30.406-18:00"), "dateValue",
                new JsonPrimitive(76)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SetDate")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(r -> r.next().getResult().get("result").getAsString()
                        .equals("1994-12-15T14:05:30.406-18:00"))
                .verifyComplete();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("1300-10-25T05:42:10.435+14:00"), "dateValue",
                new JsonPrimitive(130)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SetDate")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(r -> r.next().getResult().get("result").getAsString()
                        .equals("1301-02-07T05:42:10.435+14:00"))
                .verifyComplete();
    }
}
