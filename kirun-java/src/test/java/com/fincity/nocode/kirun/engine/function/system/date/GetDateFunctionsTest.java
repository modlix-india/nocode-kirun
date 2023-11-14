package com.fincity.nocode.kirun.engine.function.system.date;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.namespaces.Namespaces;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class GetDateFunctionsTest {

    DateFunctionRepository dfr = new DateFunctionRepository();

    ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
            new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository());

    static final String message = "Invalid ISO 8601 Date format.";

    @Test
    void getDateTest() {

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("2023-09-07T07:35:17.000Z")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetDate")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("result").getAsInt() == 7)
                .verifyComplete();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("2023-12-31T07:35:17.111-12:00")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetDate")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("result").getAsInt() == 31)
                .verifyComplete();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("2023-09-07T17:35:17.123-11:00")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetDate")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("result").getAsInt() == 7)
                .verifyComplete();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("2023-09-32T07:35:17.000Z")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetDate")
                .flatMap(e -> e.execute(fep)))
                .expectErrorMessage(message)
                .verify();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("2023-09-7T07:35:17.000Z")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetDate")
                .flatMap(e -> e.execute(fep)))
                .expectErrorMessage(message)
                .verify();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("1970-01-20T15:34:57.561Z")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetDate")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("result").getAsInt() == 20)
                .verifyComplete();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("2023-10-19T06:44:11.615-12:11")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetDate")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("result").getAsInt() == 19)
                .verifyComplete();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive(false)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetDate")
                .flatMap(e -> e.execute(fep)))
                .expectError()
                .verify();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("abcd")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetDate")
                .flatMap(e -> e.execute(fep)))
                .expectErrorMessage(message)
                .verify();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("-123124-10-19T06:44:11.615Z")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetDate")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("result").getAsInt() == 19)
                .verifyComplete();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("-123129-02-29T06:44:11.615Z")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetDate")
                .flatMap(e -> e.execute(fep)))
                .expectError().verify();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("-11924-10-19T06:44:11.615Z")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetDate")
                .flatMap(e -> e.execute(fep)))
                .expectError().verify();

    }

    @Test
    void getFullYearTest() {

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("2023-09-07T07:35:17.000Z")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetFullYear")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("result").getAsInt() == 2023)
                .verifyComplete();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("1975-11-20T03:12:51.000Z")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetFullYear")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("result").getAsInt() == 1975)
                .verifyComplete();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("2099-12-7T07:35:17.000Z")));

        StepVerifier.create(dfr.find(Namespaces.DATE,
                "GetFullYear")
                .flatMap(e -> e.execute(fep)))
                .expectErrorMessage(message)
                .verify();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("2099-12-17T07:35:17.00Z")));

        StepVerifier.create(dfr.find(Namespaces.DATE,
                "GetFullYear")
                .flatMap(e -> e.execute(fep)))
                .expectErrorMessage(message)
                .verify();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("2999-01-20T15:34:57.561Z")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetFullYear")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("result").getAsInt() == 2999)
                .verifyComplete();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("9999-10-19T06:44:11.615Z")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetFullYear")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("result").getAsInt() == 9999)
                .verifyComplete();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("-123124-10-19T06:44:11.615Z")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetFullYear")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("result").getAsInt() == -123124)
                .verifyComplete();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive(false)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetFullYear")
                .flatMap(e -> e.execute(fep)))
                .expectError()
                .verify();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("abcd")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetFullYear")
                .flatMap(e -> e.execute(fep)))
                .expectErrorMessage(message)
                .verify();

    }

    @Test
    void getMonthTest() {

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("2023-09-07T17:35:17.000Z")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetMonth")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("result").getAsInt() == 8)
                .verifyComplete();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("1970-01-20T15:58:57.561Z")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetMonth")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("result").getAsInt() == 0)
                .verifyComplete();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("2023-10-19T06:44:11.615Z")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetMonth")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("result").getAsInt() == 9)
                .verifyComplete();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("2507-08-08T11:41:50.000+00:00")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetMonth")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("result").getAsInt() == 7)
                .verifyComplete();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("1970-01-20T15:13:51.001+12:01")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetMonth")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("result").getAsInt() == 0)
                .verifyComplete();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("1970-01-20T15:13:51.200-12:01")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetMonth")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get(
                                "result").getAsInt() == 0)
                .verifyComplete();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("1970-11-18T12:13:51.200-11:01")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetMonth")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get(
                                "result").getAsInt() == 10)
                .verifyComplete();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("1970-11-30T12:13:51.200-12:01")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetMonth")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("result").getAsInt() == 10)
                .verifyComplete();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("2016-02-29T12:13:51.200-12:01")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetMonth")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("result").getAsInt() == 1)
                .verifyComplete();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("2023-09-7T07:35:17.000Z")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetMonth")
                .flatMap(e -> e.execute(fep)))
                .expectErrorMessage(message)
                .verify();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("2023-10-19T23:24:7.615Z")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetMonth")
                .flatMap(e -> e.execute(fep)))
                .expectErrorMessage(message)
                .verify();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("abcd")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetMonth")
                .flatMap(e -> e.execute(fep)))
                .expectErrorMessage(message)
                .verify();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive(false)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetMonth")
                .flatMap(e -> e.execute(fep)))
                .expectError()
                .verify();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("-123124-10-19T06:44:11.615Z")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetMonth")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("result").getAsInt() == 9)
                .verifyComplete();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("-1124-10-19T06:44:11.615Z")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetMonth")
                .flatMap(e -> e.execute(fep)))
                .expectError().verify();
    }

    @Test
    void getHoursTest() {

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("2016-02-29T12:13:51.200-12:01")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetHours")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("result").getAsInt() == 12)
                .verifyComplete();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("1970-01-20T23:13:51.001-11:00")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetHours")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("result").getAsInt() == 23)
                .verifyComplete();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("2023-09-7T07:35:17.000Z")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetHours")
                .flatMap(e -> e.execute(fep)))
                .expectErrorMessage(message)
                .verify();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("1970-01-20T15:34:57.561Z")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetHours")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("result").getAsInt() == 15)
                .verifyComplete();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("2023-10-19T06:44:11.615-14:00")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetHours")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("result").getAsInt() == 6)
                .verifyComplete();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive(false)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetHours")
                .flatMap(e -> e.execute(fep)))
                .expectError()
                .verify();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("abcd")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetHours")
                .flatMap(e -> e.execute(fep)))
                .expectErrorMessage(message)
                .verify();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("2023-10-19T25:44:11.615Z")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetHours")
                .flatMap(e -> e.execute(fep)))
                .expectErrorMessage(message)
                .verify();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("2023-10-19T49:44:11.615Z")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetHours")
                .flatMap(e -> e.execute(fep)))
                .expectErrorMessage(message)
                .verify();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("2023-10-19T24:00:00Z")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetHours")
                .flatMap(e -> e.execute(fep)))
                .expectErrorMessage(message)
                .verify();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("2016-02-31T12:13:51.200-12:01")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetHours")
                .flatMap(e -> e.execute(fep)))
                .expectErrorMessage(message)
                .verify();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("-123124-10-19T06:44:11.615Z")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetHours")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("result").getAsInt() == 6)
                .verifyComplete();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("-123129-02-29T06:44:11.615Z")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetHours")
                .flatMap(e -> e.execute(fep)))
                .expectError().verify();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("-11924-10-19T06:44:11.615Z")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetHours")
                .flatMap(e -> e.execute(fep)))
                .expectError().verify();
    }

    @Test
    void getMinutesTest() {

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("2023-09-07T17:35:17.000Z")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetMinutes")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("result").getAsInt() == 35)
                .verifyComplete();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("2023-10-19T06:44:11.615Z")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetMinutes")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("result").getAsInt() == 44)
                .verifyComplete();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("1970-01-20T15:58:57.561-12:31")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetMinutes")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("result").getAsInt() == 58)
                .verifyComplete();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("2507-08-08T11:41:50.000+09:00")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetMinutes")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("result").getAsInt() == 41)
                .verifyComplete();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("1970-01-20T15:13:51.001+12:01")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetMinutes")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("result").getAsInt() == 13)
                .verifyComplete();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("1970-01-20T15:13:51.200-12:01")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetMinutes")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("result").getAsInt() == 13)
                .verifyComplete();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("2023-09-7T07:35:17.000Z")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetMinutes")
                .flatMap(e -> e.execute(fep)))
                .expectErrorMessage(message)
                .verify();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive(false)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetMinutes")
                .flatMap(e -> e.execute(fep)))
                .expectError()
                .verify();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("2023-10-19T23:84:11.615Z")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetMinutes")
                .flatMap(e -> e.execute(fep)))
                .expectErrorMessage(message)
                .verify();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("2016-02-31T12:13:51.200-12:01")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetMinutes")
                .flatMap(e -> e.execute(fep)))
                .expectErrorMessage(message)
                .verify();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("2024-02-29T12:13:51.200-12:01")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetMinutes")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("result").getAsInt() == 13)
                .verifyComplete();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("-123124-10-19T06:44:11.615Z")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetMinutes")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("result").getAsInt() == 44)
                .verifyComplete();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("-123129-02-29T06:44:11.615Z")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetMinutes")
                .flatMap(e -> e.execute(fep)))
                .expectError().verify();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("-11924-10-19T06:44:11.615Z")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetMinutes")
                .flatMap(e -> e.execute(fep)))
                .expectError().verify();
    }

    @Test
    void getSecondsTest() {

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("2023-09-07T17:35:17.000Z")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetSeconds")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("result").getAsInt() == 17)
                .verifyComplete();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("1970-01-20T15:58:57.561Z")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetSeconds")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("result").getAsInt() == 57)
                .verifyComplete();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("2023-10-19T06:44:11.615Z")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetSeconds")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("result").getAsInt() == 11)
                .verifyComplete();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("2507-08-08T11:41:50.000+00:00")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetSeconds")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("result").getAsInt() == 50)
                .verifyComplete();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("1970-01-20T15:13:51.001+12:01")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetSeconds")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("result").getAsInt() == 51)
                .verifyComplete();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("1970-01-20T15:13:51.200-12:01")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetSeconds")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get(
                                "result").getAsInt() == 51)
                .verifyComplete();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("2023-09-7T07:35:17.000Z")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetSeconds")
                .flatMap(e -> e.execute(fep)))
                .expectErrorMessage(message)
                .verify();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("2023-10-19T23:24:7.615Z")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetSeconds")
                .flatMap(e -> e.execute(fep)))
                .expectErrorMessage(message)
                .verify();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("abcd")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetSeconds")
                .flatMap(e -> e.execute(fep)))
                .expectErrorMessage(message)
                .verify();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive(false)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetSeconds")
                .flatMap(e -> e.execute(fep)))
                .expectError()
                .verify();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("2016-02-31T12:13:56.200-12:01")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetSeconds")
                .flatMap(e -> e.execute(fep)))
                .expectErrorMessage(message)
                .verify();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("2024-02-29T12:13:41.200-12:01")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetSeconds")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("result").getAsInt() == 41)
                .verifyComplete();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("2028-02-29T12:13:49.200-12:01")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetSeconds")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("result").getAsInt() == 49)
                .verifyComplete();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("-123124-10-19T06:44:11.615Z")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetSeconds")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("result").getAsInt() == 11)
                .verifyComplete();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("-123129-03-29T06:44:88.615Z")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetSeconds")
                .flatMap(e -> e.execute(fep)))
                .expectError().verify();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("-11924-10-19T06:44:11.615Z")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetSeconds")
                .flatMap(e -> e.execute(fep)))
                .expectError().verify();
    }

    @Test
    void getMilliSecondsTest() {

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("2023-09-07T17:35:17.000Z")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetMilliSeconds")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("result").getAsInt() == 000)
                .verifyComplete();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("2023-10-19T06:44:11.615Z")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetMilliSeconds")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get(
                                "result").getAsInt() == 615)
                .verifyComplete();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("1970-01-20T15:58:57.561Z")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetMilliSeconds")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("result").getAsInt() == 561)
                .verifyComplete();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("1994-10-24T14:05:30.406+00:00")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetMilliSeconds")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("result").getAsInt() == 406)
                .verifyComplete();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("1383-10-04T14:10:50.70000+00:00")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetMilliSeconds")
                .flatMap(e -> e.execute(fep)))
                .expectErrorMessage(message)
                .verify();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("2023-09-7T07:35:17.000Z")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetMilliSeconds")
                .flatMap(e -> e.execute(fep)))
                .expectErrorMessage(message)
                .verify();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("2023-10-19T23:84:11.615Z")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetMilliSeconds")
                .flatMap(e -> e.execute(fep)))
                .expectErrorMessage(message)
                .verify();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("abcd")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetMilliSeconds")
                .flatMap(e -> e.execute(fep)))
                .expectErrorMessage(message)
                .verify();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive(false)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetMilliSeconds")
                .flatMap(e -> e.execute(fep)))
                .expectError()
                .verify();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("202312=12")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetMilliSeconds")
                .flatMap(e -> e.execute(fep)))
                .expectError()
                .verify();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("2016-02-31T12:13:56.200-12:01")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetMilliSeconds")
                .flatMap(e -> e.execute(fep)))
                .expectErrorMessage(message)
                .verify();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("2024-02-29T12:13:41.189-12:01")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetMilliSeconds")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("result").getAsInt() == 189)
                .verifyComplete();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("2028-02-29T12:13:49.200-12:01")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetMilliSeconds")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("result").getAsInt() == 200)
                .verifyComplete();
    }

    @Test
    void getDayTest() {

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("2023-09-07T17:35:17.000Z")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetDay")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("result")
                                .getAsInt() == 4)
                .verifyComplete();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("2023-09-03T17:35:17.000Z")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetDay")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("result")
                                .getAsInt() == 0)
                .verifyComplete();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("1970-01-20T15:58:57.561Z")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetDay")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("result").getAsInt() == 2)
                .verifyComplete();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("2023-10-19T06:44:11.615Z")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetDay")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get(
                                "result").getAsInt() == 4)
                .verifyComplete();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("2023-10-24T14:10:30.700+00:00")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetDay")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get(
                                "result").getAsInt() == 2)
                .verifyComplete();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("1994-10-24T14:05:30.406+11:00")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetDay")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("result").getAsInt() == 1)
                .verifyComplete();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("1300-10-25T05:42:10.435-12:54")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetDay")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get(
                                "result").getAsInt() == 1)
                .verifyComplete();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("2023-09-7T07:35:17.000Z")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetDay")
                .flatMap(e -> e.execute(fep)))
                .expectErrorMessage(message)
                .verify();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("2023-10-19T23:84:11.615Z")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetDay")
                .flatMap(e -> e.execute(fep)))
                .expectErrorMessage(message)
                .verify();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("abcd")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetDay")
                .flatMap(e -> e.execute(fep)))
                .expectErrorMessage(message)
                .verify();

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetDay")
                .flatMap(e -> e.execute(fep)))
                .expectErrorMessage(message)
                .verify();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("202312=12")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetDay")
                .flatMap(e -> e.execute(fep)))
                .expectError()
                .verify();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("2053-10-04T14:10:50.70000+00:00")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetDay")
                .flatMap(e -> e.execute(fep)))
                .expectError()
                .verify();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("2023-10-19T23:84:11.615Z")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetDay")
                .flatMap(e -> e.execute(fep)))
                .expectError()
                .verify();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("2016-02-31T12:13:56.200-12:01")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetDay")
                .flatMap(e -> e.execute(fep)))
                .expectErrorMessage(message)
                .verify();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("2024-02-29T12:13:41.189-12:01")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetDay")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("result").getAsInt() == 4)
                .verifyComplete();

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("2028-02-29T12:13:49.200+02:01")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "GetDay")
                .flatMap(e -> e.execute(fep)))
                .expectNextMatches(
                        res -> res.next().getResult().get("result").getAsInt() == 2)
                .verifyComplete();
    }
}