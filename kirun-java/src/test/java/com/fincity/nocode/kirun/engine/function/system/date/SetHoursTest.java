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

    @Test
    void SetHoursSuccessTest() {

        fep.setArguments(
                Map.of("isodate", new JsonPrimitive("2023-09-07T17:35:17.123Z"), "hourValue",
                        new JsonPrimitive(-109)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SetHours")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("hour").getAsInt() == 11)
                .verifyComplete();

        fep.setArguments(
                Map.of("isodate", new JsonPrimitive("2023-09-03T17:35:17.980Z"), "hourValue",
                        new JsonPrimitive(100)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SetHours")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("hour")
                                .getAsInt() == 4)
                .verifyComplete();

        fep.setArguments(
                Map.of("isodate", new JsonPrimitive("1970-01-20T15:58:57.561Z"), "hourValue",
                        new JsonPrimitive(1000)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SetHours")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("hour").getAsInt() == 16)
                .verifyComplete();

        fep.setArguments(
                Map.of("isodate", new JsonPrimitive("2023-10-19T06:44:11.615Z"), "hourValue",
                        new JsonPrimitive(-0)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SetHours")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get(
                                "hour").getAsInt() == 0)
                .verifyComplete();

        fep.setArguments(Map.of("isodate", new JsonPrimitive("2023-10-24T14:10:30.700+12:00"), "hourValue",
                new JsonPrimitive(100)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SetHours")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get(
                                "hour").getAsInt() == 4)
                .verifyComplete();

        fep.setArguments(Map.of("isodate", new JsonPrimitive("1994-10-24T14:05:30.406-18:00"), "hourValue",
                new JsonPrimitive(-100)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SetHours")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("hour").getAsInt() == 20)
                .verifyComplete();

        fep.setArguments(
                Map.of("isodate", new JsonPrimitive("1300-10-25T05:42:10.435+14:00"), "hourValue",
                        new JsonPrimitive(-10000)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SetHours")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get(
                                "hour").getAsInt() == 8)
                .verifyComplete();

    }

    @Test
    void SetHoursFailTest() {

        fep.setArguments(
                Map.of("isodate", new JsonPrimitive("2023-09-7T07:35:17.000Z"), "hourValue", new JsonPrimitive(12)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SetHours")
                .flatMap(e -> e.execute(fep)))
                .expectErrorMessage("Please provide the valid iso date.")
                .verify();

        fep.setArguments(
                Map.of("isodate", new JsonPrimitive("2023-10-19T23:84:11.615Z"), "hourValue", new JsonPrimitive(12)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SetHours")
                .flatMap(e -> e.execute(fep)))
                .expectErrorMessage("Please provide the valid iso date.")
                .verify();

        fep.setArguments(Map.of("isodate", new JsonPrimitive("abcd"), "hourValue", new JsonPrimitive(12)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SetHours")
                .flatMap(e -> e.execute(fep)))
                .expectErrorMessage("Please provide the valid iso date.")
                .verify();

        fep.setArguments(Map.of("isodate", new JsonPrimitive("202312=12"), "hourValue", new JsonPrimitive(12)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SetHours")
                .flatMap(e -> e.execute(fep)))
                .expectError()
                .verify();

        fep.setArguments(Map.of("isodate", new JsonPrimitive("2053-10-04T14:10:50.70000+00:00"), "hourValue",
                new JsonPrimitive(12)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "SetHours")
                .flatMap(e -> e.execute(fep)))
                .expectError()
                .verify();

        fep.setArguments(
                Map.of("isodate", new JsonPrimitive("2023-10-19T23:84:11.615Z"), "hourValue", new JsonPrimitive(12)));

        StepVerifier.create(dfr.find(Namespaces.DATE,
                "SetHours")
                .flatMap(e -> e.execute(fep)))
                .expectError()
                .verify();

    }

}
