package com.fincity.nocode.kirun.engine.function.system.date;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

public class GetTimeAsArrayTest {
 
    GetTimeAsArray gta = new GetTimeAsArray();

    	ReactiveFunctionExecutionParameters rfep = new ReactiveFunctionExecutionParameters(
	        new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository());

    @Test
    public void test1() {

        rfep.setArguments(Map.of("isoDate", new JsonPrimitive("2024-09-20T15:13:51.000Z")));

        StepVerifier.create(gta.execute(rfep))
        .expectNextMatches(r ->{
            JsonArray actualArray = r.allResults().get(0).getResult().get("result").getAsJsonArray();
            JsonArray expectedArray = new JsonArray();
            expectedArray.add(2024);
            expectedArray.add(9);
            expectedArray.add(20);
            expectedArray.add(15);
            expectedArray.add(13);
            expectedArray.add(51);
            expectedArray.add(0);
            return actualArray.equals(expectedArray);
        })
        .verifyComplete();
    }

    @Test
    public void test2() {

        rfep.setArguments(Map.of("isoDate", new JsonPrimitive("2024-02-30T15:13:51.000Z")));

        StepVerifier.create(gta.execute(rfep))
        .expectErrorMessage("Please provide valid ISO date.")
        .verify();
    }

    @Test
    public void LongTest3() {

        rfep.setArguments(Map.of("isoDate", new JsonPrimitive("2023-10-10T10:02:54.959-12:12")));

        StepVerifier.create(gta.execute(rfep))
        .expectNextMatches(r ->{
            JsonArray actualArray = r.allResults().get(0).getResult().get("result").getAsJsonArray();
            JsonArray expectedArray = new JsonArray();
            expectedArray.add(2023);
            expectedArray.add(10);
            expectedArray.add(10);
            expectedArray.add(22);
            expectedArray.add(14);
            expectedArray.add(54);
            expectedArray.add(959);
            return actualArray.equals(expectedArray);
        })
        .verifyComplete();
    }

    @Test
    public void LongTest4() {

        rfep.setArguments(Map.of("isoDate", new JsonPrimitive("2023-06-10T10:02:54.959+02:11")));

        StepVerifier.create(gta.execute(rfep))
        .expectNextMatches(r ->{
            JsonArray actualArray = r.allResults().get(0).getResult().get("result").getAsJsonArray();
            JsonArray expectedArray = new JsonArray();
            expectedArray.add(2023);
            expectedArray.add(06);
            expectedArray.add(10);
            expectedArray.add(7);
            expectedArray.add(51);
            expectedArray.add(54);
            expectedArray.add(959);
            return actualArray.equals(expectedArray);
        })
        .verifyComplete();
    }
}
