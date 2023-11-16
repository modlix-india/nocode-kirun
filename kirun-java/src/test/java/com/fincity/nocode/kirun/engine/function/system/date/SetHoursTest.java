package com.fincity.nocode.kirun.engine.function.system.date;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.namespaces.Namespaces;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class SetHoursTest {

    DateFunctionRepository dfr = new DateFunctionRepository();

    ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
            new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository());

    String message = "Invalid ISO 8601 Date format.";

    @Test
    void test() {

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("abcd"), "hourValue", new JsonPrimitive(121312)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SetHours")
                .flatMap(e -> e.execute(fep)))
                .expectErrorMessage(message)
                .verify();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("202312=12"), "hourValue", new JsonPrimitive(1245)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SetHours")
                .flatMap(e -> e.execute(fep)))
                .expectError()
                .verify();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("2053-10-04T14:10:50.700+00:00"), "hourValue",
                new JsonPrimitive(-12)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SetHours")
                .flatMap(e -> e.execute(fep)))
                .expectErrorMessage("Hours should be in the range of 0 and 23")
                .verify();

        fep.setArguments(
                Map.of("isoDate", new JsonPrimitive("2023-10-19T23:44:11.615Z"), "hourValue",
                        new JsonPrimitive(24)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SetHours")
                .flatMap(e -> e.execute(fep)))
                .expectErrorMessage("Hours should be in the range of 0 and 23")
                .verify();
    }

    @Test
    void validTest() {

        fep.setArguments(
                Map.of("isoDate", new JsonPrimitive("2023-10-19T06:44:11.615-11:11"), "hourValue",
                        new JsonPrimitive(-0)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SetHours")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString().equals("2023-10-19T00:44:11.615-11:11"))
                .verifyComplete();

        fep.setArguments(
                Map.of("isoDate", new JsonPrimitive("+071994-10-24T14:05:30.406+18:00"), "hourValue",
                        new JsonPrimitive(18)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SetHours")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString()
                                .equals("+071994-10-24T18:05:30.406+18:00"))
                .verifyComplete();

        fep.setArguments(
                Map.of("isoDate", new JsonPrimitive("-011994-10-24T14:05:30.406-18:00"), "hourValue",
                        new JsonPrimitive(17)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SetHours")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString()
                                .equals("-011994-10-24T17:05:30.406-18:00"))
                .verifyComplete();

        fep.setArguments(
                Map.of("isoDate", new JsonPrimitive("1994-10-24T14:05:30.406-18:00"), "hourValue",
                        new JsonPrimitive(10)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SetHours")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        r -> r.next().getResult().get("result").getAsString().equals("1994-10-24T10:05:30.406-18:00"))
                .verifyComplete();

    }
}
