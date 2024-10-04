package com.fincity.nocode.kirun.engine.function.system.date;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonArray;

import reactor.test.StepVerifier;

public class MinimumTimestampTest {

    MinimumTimestamp mt = new MinimumTimestamp();

    ReactiveFunctionExecutionParameters rfep = new ReactiveFunctionExecutionParameters(
            new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository());

    @Test()
    void test1(){
        JsonArray arr = new JsonArray();

        arr.add("2023-10-25T13:30:04.970Z");

        arr.add("2023-10-25T12:30:04.970Z");

        arr.add("2023-10-25T19:30:04.970Z");

        rfep.setArguments(Map.of("isoDates", arr));

        StepVerifier.create(mt.execute(rfep))
                .expectNextMatches(r -> {
                    return r.next().getResult().get("result").getAsString() == "2023-10-25T12:30:04.970Z";
                }).verifyComplete();

    } 

    @Test()
    void test2(){

        JsonArray arr = new JsonArray();

        arr.add("2023-10-25T12:30:04.970Z");

        rfep.setArguments(Map.of("isoDates", arr));

        StepVerifier.create(mt.execute(rfep))
                .expectNextMatches(r -> {
                    return r.next().getResult().get("result").getAsString() == "2023-10-25T12:30:04.970Z";
                }).verifyComplete();
    }

    @Test()
    void test3(){
        JsonArray arr = new JsonArray();

        arr.add("2023-10-25T13:30:04.970+07:00");

        arr.add("2023-10-25T12:30:04.970-1:00");

        arr.add("2023-10-25T19:30:04.970Z");

        rfep.setArguments(Map.of("isoDates", arr));

        StepVerifier.create(mt.execute(rfep))
                .expectError().verify();
    }


    @Test()
    void test4(){

        JsonArray arr = new JsonArray();

        arr.add("2023-10-25T13:30:04.970+07:00");

        arr.add("2023-10-25T12:30:04.970-11:00");

        arr.add("2023-10-25T19:30:04.970Z");

        arr.add("2023-10-25T13:30:04.970+09:00");

        arr.add("2023-10-25T19:30:04.970+01:30");

        rfep.setArguments(Map.of("isoDates", arr));

        StepVerifier.create(mt.execute(rfep))
                .expectNextMatches(r -> {
                    return r.next().getResult().get("result").getAsString() == "2023-10-25T13:30:04.970+09:00";
                }).verifyComplete();
    }


    @Test()
    void test5(){
        JsonArray arr = new JsonArray();

        arr.add("2023-10-25T02:30:04.970Z");
        arr.add("2023-10-25T03:30:04.970Z");
        arr.add("2023-10-25T04:30:04.970Z");
        arr.add("2023-10-25T05:30:04.970Z");
        arr.add("2023-10-25T06:30:04.970Z");
        arr.add("2023-10-25T07:30:04.970Z");
        arr.add("2023-10-25T08:30:04.970Z");
        arr.add("2023-10-25T09:30:04.970Z");
        arr.add("2023-10-25T10:30:04.970Z");
        arr.add("2023-10-25T11:30:04.970Z");
        arr.add("2023-10-25T12:30:04.970Z");
        arr.add("2023-10-25T13:30:04.970Z");
        arr.add("2023-10-25T14:30:04.970Z");
        arr.add("2023-10-25T15:30:04.970Z");
        arr.add("2023-10-25T16:30:04.970Z");
        arr.add("2023-10-25T17:30:04.970Z");
        arr.add("2023-10-25T18:30:04.970Z");
        arr.add("2023-10-25T19:30:04.970Z");
        arr.add("2023-10-25T01:30:04.970Z");

        rfep.setArguments(Map.of("isoDates", arr));

        StepVerifier.create(mt.execute(rfep))
                .expectNextMatches(r -> {
                    return r.next().getResult().get("result").getAsString() == "2023-10-25T01:30:04.970Z";
                }).verifyComplete();
    }
}
