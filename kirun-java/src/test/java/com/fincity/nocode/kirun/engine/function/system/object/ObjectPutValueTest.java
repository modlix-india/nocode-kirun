package com.fincity.nocode.kirun.engine.function.system.object;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.Gson;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class ObjectPutValueTest {

	@Test
	void test1() {

		JsonObject source = new JsonObject();
		source.addProperty("a", 1);
		source.addProperty("b", "string");
		ObjectPutValue objectPutValue = new ObjectPutValue();

		ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
		        new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
		        .setArguments(Map.of("source", source,"key", new JsonPrimitive("newkey"),
		                "value", new JsonPrimitive(10), "overwrite", new JsonPrimitive(false),
		                "deleteKeyOnNull", new JsonPrimitive(true)));

		StepVerifier.create(objectPutValue.internalExecute(fep)
		        .map(e -> e.next()
		                .getResult()
		                .get("value")
		                .getAsJsonObject()))
		        .consumeNextWith(value ->
				{System.out.println("Value inside map: " + value);
			        assertEquals(10, value.get("newkey").getAsInt()
			                );
			        
		        })
		        .verifyComplete();

	}

	@Test
	void test2() {

		JsonObject source = new JsonObject();
		source.addProperty("a", 1);
		source.addProperty("b", "string");
		ObjectPutValue objectPutValue = new ObjectPutValue();

		System.out.println(source);
		ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
		        new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
		        .setArguments(Map.of("source", source,"key", new JsonPrimitive("a"),
		               "value", new JsonPrimitive(10),"overwrite", new JsonPrimitive(true),
		               "deleteKeyOnNull", new JsonPrimitive(false)));

		System.out.println(source);
		StepVerifier.create(objectPutValue.internalExecute(fep)
		        .map(e -> e.next()
		                .getResult()
		                .get("value")
		                .getAsJsonObject()))
		        .consumeNextWith(value ->
				{
			        assertEquals(10, value.get("a")
			                .getAsInt());
			        System.out.println("Value inside map: " + value);
		        })
		        .verifyComplete();

	}

	@Test
	void test3() {
		
		JsonObject source = new JsonObject();
		source.addProperty("a", 10);
		source.addProperty("b", 100);
		ObjectPutValue objectPutValue = new ObjectPutValue();

		ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
		        new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
		        .setArguments(Map.of("source", source,"key", new JsonPrimitive("a"),
		               "value", new JsonPrimitive(10),"overwrite", new JsonPrimitive(true),
		               "deleteKeyOnNull", new JsonPrimitive(false)));

		StepVerifier.create(objectPutValue.internalExecute(fep)
		        .map(e -> e.next()
		                .getResult()
		                .get("value")
		                .getAsJsonObject()))
		        .consumeNextWith(value ->
				{
			        assertEquals(10, value.get("a")
			                .getAsInt());
//			        System.out.println("Value inside map: " + value);
		        })
		        .verifyComplete();

	}
	
	@Test
	void test4() {

		Gson gson = new Gson();

        var store = gson.fromJson(
                """
                        {"name":"Kiran","addresses":[{"city":"Bangalore","state":"Karnataka","country":"India"},{"city":"Kakinada","state":"Andhra Pradesh","country":"India"},{"city":"Beaverton","state":"Oregon"}],"phone":{"home":"080-23456789","office":"080-23456789","mobile":"080-23456789","mobile2":"503-23456789"},"plain":[1,2,3,4]}
                        """,
                JsonObject.class);
        
      JsonObject  obj=(store.getAsJsonObject());
        
		ObjectPutValue objectPutValue = new ObjectPutValue();

		ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
		        new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
		        .setArguments(Map.of("source", obj,"key", new JsonPrimitive("name"),
		               "value", new JsonPrimitive("kirun"),"overwrite", new JsonPrimitive(true),
		               "deleteKeyOnNull", new JsonPrimitive(false)));

		StepVerifier.create(objectPutValue.internalExecute(fep)
		        .map(e -> e.next()
		                .getResult()
		                .get("value")
		                .getAsJsonObject()))
		        .consumeNextWith(value ->
				{
			        assertEquals("kirun", value.get("name").getAsString());
			        assertEquals("Karnataka", value.getAsJsonArray("addresses").get(0).getAsJsonObject().get("state").getAsString());
	                assertEquals("080-23456789", value.getAsJsonObject("phone").get("home").getAsString());
	                assertEquals(1, value.getAsJsonArray("plain").get(0).getAsInt());
		        })
		        .verifyComplete();

	}
	
	@Test
	void test5() {

		Gson gson = new Gson();

        var store = gson.fromJson(
                """
                        {"name":"Kiran","addresses":[{"city":"Bangalore","state":"Karnataka","country":"India"},{"city":"Kakinada","state":"Andhra Pradesh","country":"India"},{"city":"Beaverton","state":"Oregon"}],"phone":{"home":"080-23456789","office":"080-23456789","mobile":"080-23456789","mobile2":"503-23456789"},"plain":[1,2,3,4]}
                        """,
                JsonObject.class);
        
      JsonObject  obj=(store.getAsJsonObject());
      JsonObject obj1=new JsonObject();
      obj1.addProperty("city", "Vizag");
      obj1.addProperty("state","Ap");;
      
      
		ObjectPutValue objectPutValue = new ObjectPutValue();

		ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
		        new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
		        .setArguments(Map.of("source", obj,"key", new JsonPrimitive("addresses"),
		               "value", obj1,"overwrite", new JsonPrimitive(true),
		               "deleteKeyOnNull", new JsonPrimitive(false)));

		StepVerifier.create(objectPutValue.internalExecute(fep)
		        .map(e -> e.next()
		                .getResult()
		                .get("value")
		                .getAsJsonObject()))
		        .consumeNextWith(value ->
				{
			       
			        assertEquals("Ap", value.getAsJsonObject("addresses").get("state").getAsString());
			        assertEquals("Vizag", value.getAsJsonObject("addresses").get("city").getAsString());
	                assertEquals("080-23456789", value.getAsJsonObject("phone").get("home").getAsString());
	                assertEquals(1, value.getAsJsonArray("plain").get(0).getAsInt());
		        })
		        .verifyComplete();

	}
	
	@Test
	void test6() {

		JsonObject source = new JsonObject();
		source.addProperty("a", 1);
		source.addProperty("b", "string");
		ObjectPutValue objectPutValue = new ObjectPutValue();

		ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
		        new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
		        .setArguments(Map.of("source", source,"key", new JsonPrimitive("newkey"),
		                "value", JsonNull.INSTANCE, "overwrite", new JsonPrimitive(false),
		                "deleteKeyOnNull", new JsonPrimitive(true)));

		StepVerifier.create(objectPutValue.internalExecute(fep)
		        .map(e -> e.next()
		                .getResult()
		                .get("value")
		                .getAsJsonObject()))
		        .consumeNextWith(value ->
				{System.out.println("Value inside map: " + value);
			        assertEquals(null, value.get("newkey")
			                );
			        
		        })
		        .verifyComplete();

	}
}
