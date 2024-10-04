package com.fincity.nocode.kirun.engine.function.system.date;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonArray;


import reactor.test.StepVerifier;

public class MaximumTimestampTest {
 
    MaximumTimestamp mt = new MaximumTimestamp();

    ReactiveFunctionExecutionParameters rfep = new ReactiveFunctionExecutionParameters(
            new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository());


    @Test
    void invalidTest(){
        JsonArray arr = new JsonArray();

        arr.add("2023-10-25T13:30:04.970+07:00");

        arr.add("2023-10-25T12:30:04.970-1:00");

        arr.add("2023-10-25T19:30:04.970Z");

        rfep.setArguments(Map.of("isoDates", arr));

        StepVerifier.create(mt.execute(rfep))
                .expectError().verify();
    }
             
    @Test
    void test1(){

        JsonArray arr = new JsonArray();

        arr.add("2023-10-25T13:30:04.970Z");

        arr.add("2023-10-25T12:30:04.970Z");

        arr.add("2023-10-25T19:30:04.970Z");

        rfep.setArguments(Map.of("isoDates", arr));

        StepVerifier.create(mt.execute(rfep))
                .expectNextMatches(r -> {
                    return r.next().getResult().get("result").getAsString() == "2023-10-25T19:30:04.970Z";
                }).verifyComplete();
    }

    @Test
    void test2(){
        
        JsonArray arr = new JsonArray();

        arr.add("2023-10-25T12:30:04.970Z");

        rfep.setArguments(Map.of("isoDates", arr));

        StepVerifier.create(mt.execute(rfep))
                .expectNextMatches(r -> {
                    return r.next().getResult().get("result").getAsString() == "2023-10-25T12:30:04.970Z";
                }).verifyComplete();
    }

    @Test
    void test3(){

        JsonArray arr = new JsonArray();

        arr.add("2023-10-25T13:30:04.970+07:00");

        arr.add("2023-10-25T12:30:04.970-11:00");

        arr.add("2023-10-25T19:30:04.970Z");

        arr.add("2023-10-25T13:30:04.970+09:00");

        arr.add("2023-10-25T19:30:04.970+01:30");

        rfep.setArguments(Map.of("isoDates", arr));

        StepVerifier.create(mt.execute(rfep))
                .expectNextMatches(r -> {
                    return r.next().getResult().get("result").getAsString() == "2023-10-25T12:30:04.970-11:00";
                }).verifyComplete();
    }

    @Test
    void test4(){
        JsonArray arr = new JsonArray();

        arr.add("2023-10-25T13:30:04.100+01:00");

        arr.add("2023-10-25T13:30:04.101+02:00");

        arr.add("2023-10-25T13:30:04.102+03:00");

        arr.add("2023-10-25T13:30:04.103+04:00");

        arr.add("2023-10-25T13:30:04.104+05:00");

        rfep.setArguments(Map.of("isoDates", arr));

        StepVerifier.create(mt.execute(rfep))
                .expectNextMatches(r -> {
                    return r.next().getResult().get("result").getAsString() == "2023-10-25T13:30:04.100+01:00";
                }).verifyComplete();

    }
}
