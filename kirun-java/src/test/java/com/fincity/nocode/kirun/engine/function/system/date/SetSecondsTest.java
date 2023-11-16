package com.fincity.nocode.kirun.engine.function.system.date;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.namespaces.Namespaces;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class SetSecondsTest {

    DateFunctionRepository dfr = new DateFunctionRepository();

    ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
            new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository());

    String message = "Invalid ISO 8601 Date format.";

    @Test
    void failTest() {

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("abcd"), "secondValue", new JsonPrimitive(121312)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SetSeconds")
                .flatMap(e -> e.execute(fep)))
                .expectErrorMessage(message)
                .verify();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("202312=12"), "secondValue", new JsonPrimitive(1245)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SetSeconds")
                .flatMap(e -> e.execute(fep)))
                .expectError()
                .verify();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("2053-10-04T14:10:50.700+00:00"), "secondValue",
                new JsonPrimitive(123)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SetSeconds")
                .flatMap(e -> e.execute(fep)))
                .expectErrorMessage("Seconds should be in the range of 0 and 59")
                .verify();

        fep.setArguments(
                Map.of("isoDate", new JsonPrimitive("2023-10-19T23:24:11.615Z"), "secondValue",
                        new JsonPrimitive(-1123)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SetSeconds")
                .flatMap(e -> e.execute(fep)))
                .expectErrorMessage("Seconds should be in the range of 0 and 59")
                .verify();

    }

    @Test
    void validTest() {

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("1300-10-25T05:42:13.435+14:00"), "secondValue",
                new JsonPrimitive(10)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SetSeconds")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString().equals("1300-10-25T05:42:10.435+14:00"))
                .verifyComplete();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("1300-10-25T05:42:10.435+14:00"), "secondValue",
                new JsonPrimitive(59)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SetSeconds")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString().equals("1300-10-25T05:42:59.435+14:00"))
                .verifyComplete();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("1300-10-25T05:42:10.435Z"), "secondValue",
                new JsonPrimitive(34)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SetSeconds")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString().equals("1300-10-25T05:42:34.435Z"))
                .verifyComplete();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("-011300-10-25T05:42:10.435Z"), "secondValue",
                new JsonPrimitive(45)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SetSeconds")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString().equals("-011300-10-25T05:42:45.435Z"))
                .verifyComplete();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("-011300-10-25T05:42:10.435Z"), "secondValue",
                new JsonPrimitive(59)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SetSeconds")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString().equals("-011300-10-25T05:42:59.435Z"))
                .verifyComplete();
    }

}
