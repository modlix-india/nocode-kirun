package com.fincity.nocode.kirun.engine.function.system.date;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Map;
import java.util.TimeZone;

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

    }

    @Test
    void test3() {

        rfep.setArguments(Map.of("datetwo", new JsonPrimitive("2023-10-25T13:30:04.970+07:00")));

        StepVerifier.create(dfts.execute(rfep))
                .expectError()
                .verify();

    }

    @Test
    void test4() {

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
                "datetwo", new JsonPrimitive("2023-10-26T06:40:33Z")));

        StepVerifier.create(dfts.execute(rfep))
                .expectNextMatches(r -> r.next().getResult().get("difference").getAsLong() == 0L)
                .verifyComplete();

    }

    @Test
    void test7() {

        rfep.setArguments(Map.of("dateone", new JsonPrimitive("2023-10-26T06:40:33.807Z"),
                "datetwo", new JsonPrimitive("2023-10-26T06:40:33.100Z")));

        StepVerifier.create(dfts.execute(rfep))
                .expectNextMatches(r -> {
                    System.out.println(r.next().getResult().get("difference").getAsLong());

                    return true;
                })
                .verifyComplete();

    }

    @Test
    void test8() {

        String a = "2023-10-26T06:40:33.807Z";

        String b = "2023-10-25T19:30:54.970+01:30";

        String c = "2023-10-26T06:40:33Z";

        String d = "2023-10-26T19:30:54.975+01:30";

        DateTimeFormatter parser = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS[xx][XXX]");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'").withZone(ZoneOffset.UTC);

        System.out.println(parser.parse(a));

        System.out.println(parser.parse(b));
        System.out.println(formatter.parse(c));

        System.out.println(parser.parse(d));

        System.out.println(ZonedDateTime.parse(a, parser).toInstant().toEpochMilli());

        System.out.println(ZonedDateTime.parse(b, parser).toInstant().toEpochMilli());

        System.out.println(ZonedDateTime.parse(c, formatter).toInstant().toEpochMilli());

        System.out.println(ZonedDateTime.parse(d, parser).toInstant().toEpochMilli());

    }

}
