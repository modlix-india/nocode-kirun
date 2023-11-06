package com.fincity.nocode.kirun.engine.function.system.date;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class GetTimeAsObjectTest {
	
	GetTimeAsObject gto = new GetTimeAsObject();

    ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
            new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository());

   @Test
   void test() {
	   fep.setArguments(Map.of("isoDate", new JsonPrimitive("2023-09-07T07:35:17.000+12:21")));
	   
	   StepVerifier.create(gto.execute(fep))
	   .expectNextMatches(output -> {
	   EventResult outputMap = output.allResults().get(0);
	   Map<String, JsonElement> resultMap = outputMap.getResult();
       JsonObject resultObject = resultMap.get("result").getAsJsonObject();

       JsonObject expectedObject= new JsonObject();
       expectedObject.addProperty("year", 2023);
       expectedObject.addProperty("month", 9);
       expectedObject.addProperty("day", 7);
       expectedObject.addProperty("hours", 7);
       expectedObject.addProperty("minutes", 35);
       expectedObject.addProperty("seconds", 17);
       expectedObject.addProperty("milli", 0);
       expectedObject.addProperty("offset", -741);
       
       assertEquals(expectedObject, resultObject);

       return true;
   })
   .verifyComplete();
	   
	   
   }
   
   @Test
   void test2() {
	   fep.setArguments(Map.of("isoDate", new JsonPrimitive("2000-11-18T07:35:17.000Z")));
	   
	   StepVerifier.create(gto.execute(fep))
	   .expectNextMatches(output -> {
	   EventResult outputMap = output.allResults().get(0);
	   Map<String, JsonElement> resultMap = outputMap.getResult();
       JsonObject resultObject = resultMap.get("result").getAsJsonObject();

       JsonObject expectedObject= new JsonObject();
       expectedObject.addProperty("year", 2000);
       expectedObject.addProperty("month", 11);
       expectedObject.addProperty("day", 18);
       expectedObject.addProperty("hours", 7);
       expectedObject.addProperty("minutes", 35);
       expectedObject.addProperty("seconds", 17);
       expectedObject.addProperty("milli", 0);
       expectedObject.addProperty("offset", 0);
       
       assertEquals(expectedObject, resultObject);

       return true;
   })
   .verifyComplete();
	   
	   
   }

   @Test
   void test3() {
	   fep.setArguments(Map.of("isoDate", new JsonPrimitive("1990-12-12T17:35:17Z")));
	   
	   StepVerifier.create(gto.execute(fep))
	   .expectNextMatches(output -> {
	   EventResult outputMap = output.allResults().get(0);
	   Map<String, JsonElement> resultMap = outputMap.getResult();
       JsonObject resultObject = resultMap.get("result").getAsJsonObject();

       JsonObject expectedObject= new JsonObject();
       expectedObject.addProperty("year", 1990);
       expectedObject.addProperty("month", 12);
       expectedObject.addProperty("day", 12);
       expectedObject.addProperty("hours", 17);
       expectedObject.addProperty("minutes", 35);
       expectedObject.addProperty("seconds", 17);
       expectedObject.addProperty("milli", 0);
       expectedObject.addProperty("offset", 0);
       
       assertEquals(expectedObject, resultObject);


       return true;
   })
   .verifyComplete();
	   
	   
   }
   
   @Test
   void test5() {
	   fep.setArguments(Map.of("isoDate", new JsonPrimitive("2222-09-07T07:35:17.000-17:00")));
	   
	   StepVerifier.create(gto.execute(fep))
	   .expectNextMatches(output -> {
	   EventResult outputMap = output.allResults().get(0);
	   Map<String, JsonElement> resultMap = outputMap.getResult();
       JsonObject resultObject = resultMap.get("result").getAsJsonObject();

       JsonObject expectedObject= new JsonObject();
       expectedObject.addProperty("year", 2222);
       expectedObject.addProperty("month", 9);
       expectedObject.addProperty("day", 7);
       expectedObject.addProperty("hours", 7);
       expectedObject.addProperty("minutes", 35);
       expectedObject.addProperty("seconds", 17);
       expectedObject.addProperty("milli", 0);
       expectedObject.addProperty("offset", 1020);
       
       assertEquals(expectedObject, resultObject);

       return true;
   })
   .verifyComplete();
	   
	   
   }
}
