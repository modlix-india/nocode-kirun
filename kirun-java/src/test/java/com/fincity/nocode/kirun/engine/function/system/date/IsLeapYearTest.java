package com.fincity.nocode.kirun.engine.function.system.date;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.namespaces.Namespaces;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

public class IsLeapYearTest {

    DateFunctionRepository dfr = new DateFunctionRepository();

	ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
	        new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository());

    @Test
    void test(){

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("2029-15-05T06:04:18.073Z")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "IsLeapYear").flatMap(e -> e.execute(fep)))
            .expectError()
            .verify();
        
    }

    @Test
    void test2(){

        fep.setArguments(Map.of("isoDate", new JsonPrimitive(false)));

        StepVerifier.create(dfr.find(Namespaces.DATE, "IsLeapYear").flatMap(e -> e.execute(fep)))
        .expectError()
        .verify();
    }

    @Test
    void test3(){


        fep.setArguments(Map.of("isoDate", new JsonPrimitive("2024-09-13T23:52:34.633-05:30")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "IsLeapYear").flatMap(e -> e.execute(fep)))
        .expectNextMatches(res -> res.next()
                .getResult()
                .get("result").getAsBoolean())
        .verifyComplete();
    }

    @Test
    void test4(){

        fep.setArguments(Map.of("isoDate", new JsonPrimitive("2019-02-28T07:35:17.000-12:00")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "IsLeapYear").flatMap(e -> e.execute(fep)))
        .expectNextMatches(res -> !res.next()
                .getResult()
                .get("result").getAsBoolean())
        .verifyComplete();
    }

    @Test
    void test5(){


        fep.setArguments(Map.of("isoDate", new JsonPrimitive("2020-02-29T07:35:17.000-12:00")));

        StepVerifier.create(dfr.find(Namespaces.DATE, "IsLeapYear").flatMap(e -> e.execute(fep)))
        .expectNextMatches(res -> res.next()
                .getResult()
                .get("result").getAsBoolean())
        .verifyComplete();
    }
    
}