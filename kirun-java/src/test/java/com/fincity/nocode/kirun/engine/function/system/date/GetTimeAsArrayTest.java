package com.fincity.nocode.kirun.engine.function.system.date;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class GetTimeAsArrayTest {
	
	GetTimeAsArray gta = new GetTimeAsArray();

    ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
            new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository());

   @Test
   void test() {
	   fep.setArguments(Map.of("isoDate", new JsonPrimitive("2023-09-07T07:35:17.000+12:21")));
	   
	   StepVerifier.create(gta.execute(fep))
	   .expectNextMatches(output -> {
	   EventResult outputMap = output.allResults().get(0);
	   Map<String, JsonElement> resultMap = outputMap.getResult();
       JsonArray resultArray = resultMap.get("result").getAsJsonArray();

       JsonArray expectedArray = new JsonArray();
       expectedArray.add(new JsonPrimitive(2023));
       expectedArray.add(new JsonPrimitive(9));
       expectedArray.add(new JsonPrimitive(7));
       expectedArray.add(new JsonPrimitive(7));
       expectedArray.add(new JsonPrimitive(35));
       expectedArray.add(new JsonPrimitive(17));
       expectedArray.add(new JsonPrimitive(0));
       expectedArray.add(new JsonPrimitive(-741));

       assertEquals(expectedArray, resultArray);

       return true;
   })
   .verifyComplete();
	   
	   
   }
   
   @Test
   void test2() {
	   fep.setArguments(Map.of("isoDate", new JsonPrimitive("1990-09-10T09:35:00.173Z")));
	   
	   StepVerifier.create(gta.execute(fep))
	   .expectNextMatches(output -> {
	   EventResult outputMap = output.allResults().get(0);
	   Map<String, JsonElement> resultMap = outputMap.getResult();
       JsonArray resultArray = resultMap.get("result").getAsJsonArray();

       JsonArray expectedArray = new JsonArray();
       expectedArray.add(new JsonPrimitive(1990));
       expectedArray.add(new JsonPrimitive(9));
       expectedArray.add(new JsonPrimitive(10));
       expectedArray.add(new JsonPrimitive(9));
       expectedArray.add(new JsonPrimitive(35));
       expectedArray.add(new JsonPrimitive(0));
       expectedArray.add(new JsonPrimitive(173));
       expectedArray.add(new JsonPrimitive(0));

       assertEquals(expectedArray, resultArray);

      
       return true;
   })
   .verifyComplete();
	   
	   
   }
   
   @Test
   void test3() {
	   fep.setArguments(Map.of("isoDate", new JsonPrimitive("1200-11-07T07:35:17.000+09:00")));
	   
	   StepVerifier.create(gta.execute(fep))
	   .expectNextMatches(output -> {
	   EventResult outputMap = output.allResults().get(0);
	   Map<String, JsonElement> resultMap = outputMap.getResult();
       JsonArray resultArray = resultMap.get("result").getAsJsonArray();

       JsonArray expectedArray = new JsonArray();
       expectedArray.add(new JsonPrimitive(1200));
       expectedArray.add(new JsonPrimitive(11));
       expectedArray.add(new JsonPrimitive(7));
       expectedArray.add(new JsonPrimitive(7));
       expectedArray.add(new JsonPrimitive(35));
       expectedArray.add(new JsonPrimitive(17));
       expectedArray.add(new JsonPrimitive(0));
       expectedArray.add(new JsonPrimitive(-540));

       assertEquals(expectedArray, resultArray);

       return true;
   })
   .verifyComplete();
	   
	   
   }

   @Test
   void test4() {
	   fep.setArguments(Map.of("isoDate", new JsonPrimitive("1814-11-18T18:14:18.141-18:00")));
	   
	   StepVerifier.create(gta.execute(fep))
	   .expectNextMatches(output -> {
	   EventResult outputMap = output.allResults().get(0);
	   Map<String, JsonElement> resultMap = outputMap.getResult();
       JsonArray resultArray = resultMap.get("result").getAsJsonArray();

       JsonArray expectedArray = new JsonArray();
       expectedArray.add(new JsonPrimitive(1814));
       expectedArray.add(new JsonPrimitive(11));
       expectedArray.add(new JsonPrimitive(18));
       expectedArray.add(new JsonPrimitive(18));
       expectedArray.add(new JsonPrimitive(14));
       expectedArray.add(new JsonPrimitive(18));
       expectedArray.add(new JsonPrimitive(141));
       expectedArray.add(new JsonPrimitive(1080));

       assertEquals(expectedArray, resultArray);

       return true;
   })
   .verifyComplete();
	   
	   
   }

}
