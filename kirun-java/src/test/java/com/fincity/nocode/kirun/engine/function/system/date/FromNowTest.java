package com.fincity.nocode.kirun.engine.function.system.date;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class FromNowTest {

	FromNow fn = new FromNow();

	ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
	        new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository());

	@Test
	void fromNowTest1() {

		JsonArray arr = new JsonArray();

		arr.add("2023-12-17T13:30:04.970Z");

		arr.add("2023-12-17T14:30:04.970Z");

		Map<String, JsonElement> arguments = new HashMap<>();
		arguments.put("isodates", arr);
		arguments.put("key", new JsonPrimitive("N"));

		fep.setArguments(arguments);

		StepVerifier.create(fn.execute(fep))
		        .expectNextMatches(res -> res.next()
		                .getResult()
		                .get("result")
		                .getAsString()
		                .equals("few hours"))
		        .verifyComplete();

	}

	@Test
	void fromNowTest2() {

		JsonArray arr = new JsonArray();

		arr.add("2023-12-17T13:30:04.970Z");

		arr.add("2023-12-17T14:30:04.970Z");

		Map<String, JsonElement> arguments = new HashMap<>();
		arguments.put("isodates", arr);
		arguments.put("key", new JsonPrimitive("A"));

		fep.setArguments(arguments);

		StepVerifier.create(fn.execute(fep))
		        .expectNextMatches(res -> res.next()
		                .getResult()
		                .get("result")
		                .getAsString()
		                .equals("few hours ago"))
		        .verifyComplete();

	}

	@Test
	void fromNowTest3() {

		JsonArray arr = new JsonArray();

		arr.add("2023-12-17T13:30:04.970Z");

		arr.add("2023-12-17T12:30:04.970Z");

		Map<String, JsonElement> arguments = new HashMap<>();
		arguments.put("isodates", arr);
		arguments.put("key", new JsonPrimitive("I"));

		fep.setArguments(arguments);

		StepVerifier.create(fn.execute(fep))
		        .expectNextMatches(res -> res.next()
		                .getResult()
		                .get("result")
		                .getAsString()
		                .equals("In few hours"))
		        .verifyComplete();

	}

	@Test
	void fromNowTest4() {

		JsonArray arr = new JsonArray();

		arr.add("2023-12-17T13:30:04.970Z");

		arr.add("2023-12-17T14:30:04.970Z");

		Map<String, JsonElement> arguments = new HashMap<>();
		arguments.put("isodates", arr);
		arguments.put("key", new JsonPrimitive("EN"));

		fep.setArguments(arguments);

		StepVerifier.create(fn.execute(fep))
		        .expectNextMatches(res -> res.next()
		                .getResult()
		                .get("result")
		                .getAsString()
		                .equals("1 hour"))
		        .verifyComplete();

	}

	@Test
	void fromNowTest5() {

		JsonArray arr = new JsonArray();

		arr.add("2023-12-17T13:30:04.970Z");

		arr.add("2023-12-17T14:30:04.970Z");

		Map<String, JsonElement> arguments = new HashMap<>();
		arguments.put("isodates", arr);
		arguments.put("key", new JsonPrimitive("EA"));

		fep.setArguments(arguments);

		StepVerifier.create(fn.execute(fep))
		        .expectNextMatches(res -> res.next()
		                .getResult()
		                .get("result")
		                .getAsString()
		                .equals("1 hour ago"))
		        .verifyComplete();

	}

	@Test
	void fromNowTest6() {

		JsonArray arr = new JsonArray();

		arr.add("2023-12-17T13:30:04.970Z");

		arr.add("2023-12-17T12:30:04.970Z");

		Map<String, JsonElement> arguments = new HashMap<>();
		arguments.put("isodates", arr);
		arguments.put("key", new JsonPrimitive("EI"));

		fep.setArguments(arguments);

		StepVerifier.create(fn.execute(fep))
		        .expectNextMatches(res -> res.next()
		                .getResult()
		                .get("result")
		                .getAsString()
		                .equals("In 1 hour"))
		        .verifyComplete();

	}

	@Test
	void fromNowTestEN2() {

		JsonArray arr = new JsonArray();

		arr.add("2023-12-17T13:30:04.970Z");

		arr.add("2023-12-19T15:30:04.970Z");

		Map<String, JsonElement> arguments = new HashMap<>();
		arguments.put("isodates", arr);
		arguments.put("key", new JsonPrimitive("EN2"));

		fep.setArguments(arguments);

		StepVerifier.create(fn.execute(fep))
		        .expectNextMatches(res -> res.next()
		                .getResult()
		                .get("result")
		                .getAsString()
		                .equals("2 days 2 hours"))
		        .verifyComplete();

	}

	@Test
	void fromNowTestEA2() {

		JsonArray arr = new JsonArray();

		arr.add("2023-12-17T13:30:04.970Z");

		arr.add("2023-12-14T15:30:04.970Z");

		Map<String, JsonElement> arguments = new HashMap<>();
		arguments.put("isodates", arr);
		arguments.put("key", new JsonPrimitive("EA2"));

		fep.setArguments(arguments);

		StepVerifier.create(fn.execute(fep))
		        .expectNextMatches(res -> res.next()
		                .getResult()
		                .get("result")
		                .getAsString()
		                .equals("after 3 days 2 hours"))
		        .verifyComplete();

	}

	@Test
	void fromNowTestEI2() {

		JsonArray arr = new JsonArray();

		arr.add("2023-12-17T13:30:04.970Z");

		arr.add("2023-12-14T15:30:04.970Z");

		Map<String, JsonElement> arguments = new HashMap<>();
		arguments.put("isodates", arr);
		arguments.put("key", new JsonPrimitive("EI2"));

		fep.setArguments(arguments);

		StepVerifier.create(fn.execute(fep))
		        .expectNextMatches(res -> res.next()
		                .getResult()
		                .get("result")
		                .getAsString()
		                .equals("In 3 days 2 hours"))
		        .verifyComplete();

	}

	@Test
	void fromNowTestEN3() {

		JsonArray arr = new JsonArray();

		arr.add("2023-12-17T13:30:04.970Z");

		arr.add("2023-12-14T15:30:04.970Z");

		Map<String, JsonElement> arguments = new HashMap<>();
		arguments.put("isodates", arr);
		arguments.put("key", new JsonPrimitive("EI2"));

		fep.setArguments(arguments);

		StepVerifier.create(fn.execute(fep))
		        .expectNextMatches(res -> res.next()
		                .getResult()
		                .get("result")
		                .getAsString()
		                .equals("In 3 days 2 hours"))
		        .verifyComplete();

	}

	@Test
	void fromNowTestEN4() {

		JsonArray arr = new JsonArray();

		arr.add("2023-12-17T13:30:04.970Z");

		arr.add("2023-12-14T15:29:05.970Z");

		Map<String, JsonElement> arguments = new HashMap<>();
		arguments.put("isodates", arr);
		arguments.put("key", new JsonPrimitive("EN4"));

		fep.setArguments(arguments);

		StepVerifier.create(fn.execute(fep))
		        .expectNextMatches(res -> res.next()
		                .getResult()
		                .get("result")
		                .getAsString()
		                .equals("3 days 1 hour 59 minutes 1 second"))
		        .verifyComplete();

	}

	@Test
	void fromNowTestEI5() {

		JsonArray arr = new JsonArray();

		arr.add("2023-12-17T13:30:04.970Z");

		arr.add("2023-09-14T15:29:05.970Z");

		Map<String, JsonElement> arguments = new HashMap<>();
		arguments.put("isodates", arr);
		arguments.put("key", new JsonPrimitive("EI5"));

		fep.setArguments(arguments);

		StepVerifier.create(fn.execute(fep))
		        .expectNextMatches(res -> res.next()
		                .getResult()
		                .get("result")
		                .getAsString()
		                .equals("In 3 months 3 days 1 hour 59 minutes 1 second"))
		        .verifyComplete();

	}

	@Test
	void fromNowTestENA() {

		JsonArray arr = new JsonArray();

		arr.add("2023-12-17T13:30:04.970Z");

		arr.add("2000-09-14T15:30:04.970Z");

		Map<String, JsonElement> arguments = new HashMap<>();
		arguments.put("isodates", arr);
		arguments.put("key", new JsonPrimitive("ENA"));

		fep.setArguments(arguments);

		StepVerifier.create(fn.execute(fep))
		        .expectNextMatches(res -> res.next()
		                .getResult()
		                .get("result")
		                .getAsString()
		                .equals("23 years 3 months 3 days 2 hours 0 minutes 0 seconds"))
		        .verifyComplete();

	}

	@Test
	void fromNowTestEY() {

		JsonArray arr = new JsonArray();

		arr.add("2023-12-17T13:30:04.970Z");

		arr.add("2000-09-14T15:30:04.970Z");

		Map<String, JsonElement> arguments = new HashMap<>();
		arguments.put("isodates", arr);
		arguments.put("key", new JsonPrimitive("EY"));

		fep.setArguments(arguments);

		StepVerifier.create(fn.execute(fep))
		        .expectNextMatches(res -> res.next()
		                .getResult()
		                .get("result")
		                .getAsString()
		                .equals("23 years"))
		        .verifyComplete();

	}

	@Test
	void fromNowTestEM() {

		JsonArray arr = new JsonArray();

		arr.add("2023-12-17T13:30:04.970Z");

		arr.add("2023-09-14T15:30:04.970Z");

		Map<String, JsonElement> arguments = new HashMap<>();
		arguments.put("isodates", arr);
		arguments.put("key", new JsonPrimitive("EM"));

		fep.setArguments(arguments);

		StepVerifier.create(fn.execute(fep))
		        .expectNextMatches(res -> res.next()
		                .getResult()
		                .get("result")
		                .getAsString()
		                .equals("3 months"))
		        .verifyComplete();

	}

	@Test
	void fromNowTestEW() {

		JsonArray arr = new JsonArray();

		arr.add("2023-12-17T13:30:04.970Z");

		arr.add("2023-09-14T15:30:04.970Z");

		Map<String, JsonElement> arguments = new HashMap<>();
		arguments.put("isodates", arr);
		arguments.put("key", new JsonPrimitive("EW"));

		fep.setArguments(arguments);

		StepVerifier.create(fn.execute(fep))
		        .expectNextMatches(res -> res.next()
		                .getResult()
		                .get("result")
		                .getAsString()
		                .equals("13 weeks"))
		        .verifyComplete();

	}

	@Test
	void fromNowTest() {

		JsonArray arr = new JsonArray();

		arr.add("2023-09-14T15:30:04.970+05:30");

		arr.add("2023-11-14T15:30:04.970+03:00");

		Map<String, JsonElement> arguments = new HashMap<>();
		arguments.put("isodates", arr);
		arguments.put("key", new JsonPrimitive("ENA"));

		fep.setArguments(arguments);

		StepVerifier.create(fn.execute(fep))
		        .expectNextMatches(res -> res.next()
		                .getResult()
		                .get("result")
		                .getAsString()
		                .equals("2 months 0 days 3 hours 30 minutes 0 seconds"))
		        .verifyComplete();

	}

}