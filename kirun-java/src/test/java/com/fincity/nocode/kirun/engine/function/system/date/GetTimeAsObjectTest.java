package com.fincity.nocode.kirun.engine.function.system.date;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

public class GetTimeAsObjectTest {

 
GetTimeAsObject gto = new GetTimeAsObject();

ReactiveFunctionExecutionParameters rfep = new ReactiveFunctionExecutionParameters(
    new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository());

@Test
public void test1() {

    rfep.setArguments(Map.of("isoDate", new JsonPrimitive("2024-09-20T15:13:51.000Z")));

    StepVerifier.create(gto.execute(rfep))
    .expectNextMatches(r ->{
        JsonObject actualObject = r.allResults().get(0).getResult().get("result").getAsJsonObject();
        JsonObject expectedObject = new JsonObject();
        expectedObject.addProperty("year", 2024);
        expectedObject.addProperty("month", 9);
        expectedObject.addProperty("day", 20);
        expectedObject.addProperty("hour", 15);
        expectedObject.addProperty("minute", 13);
        expectedObject.addProperty("second", 51);
        expectedObject.addProperty("millisecond", 0);
        return actualObject.equals(expectedObject);
    })
    .verifyComplete();
}

@Test
public void test2() {

    rfep.setArguments(Map.of("isoDate", new JsonPrimitive("2024-02-30T15:13:51.000Z")));

    StepVerifier.create(gto.execute(rfep))
        .expectErrorMessage("Please provide valid ISO date.")
        .verify();
}

@Test
public void LongTest3() {

    rfep.setArguments(Map.of("isoDate", new JsonPrimitive("2023-10-10T10:02:54.959-12:12")));

    StepVerifier.create(gto.execute(rfep))
    .expectNextMatches(r ->{
        JsonObject actualObject = r.allResults().get(0).getResult().get("result").getAsJsonObject();
        JsonObject expectedObject = new JsonObject();
        expectedObject.addProperty("year", 2023);
        expectedObject.addProperty("month", 10);
        expectedObject.addProperty("day", 10);
        expectedObject.addProperty("hour", 22);
        expectedObject.addProperty("minute", 14);
        expectedObject.addProperty("second", 54);
        expectedObject.addProperty("millisecond", 959);
        return actualObject.equals(expectedObject);
})
.verifyComplete();
}

@Test
public void LongTest4() {

    rfep.setArguments(Map.of("isoDate", new JsonPrimitive("2023-06-10T10:02:54.959+02:11")));

    StepVerifier.create(gto.execute(rfep))
    .expectNextMatches(r ->{
        JsonObject actualObject = r.allResults().get(0).getResult().get("result").getAsJsonObject();
        JsonObject expectedObject = new JsonObject();
        expectedObject.addProperty("year", 2023);
        expectedObject.addProperty("month", 6);
        expectedObject.addProperty("day", 10);
        expectedObject.addProperty("hour", 7);
        expectedObject.addProperty("minute", 51);
        expectedObject.addProperty("second", 54);
        expectedObject.addProperty("millisecond", 959);
        return actualObject.equals(expectedObject);
    })
    .verifyComplete();
}
}