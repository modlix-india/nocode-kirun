package com.fincity.nocode.kirun.engine.function.system.date;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class EpochToDateTest {

	EpochToDate etd = new EpochToDate();
	ReactiveFunctionExecutionParameters rfep = new ReactiveFunctionExecutionParameters(
	        new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository());

	@Test
	void integerTest() {

		rfep.setArguments(Map.of("epoch", new JsonPrimitive(1696431)));

		StepVerifier.create(etd.execute(rfep))
		        .expectNextMatches(r ->
				{
			        return r.next()
			                .getResult()
			                .get("date")
			                .getAsString()
			                .equals("1970-01-20T15:13:51.000Z");
		        })
		        .verifyComplete();
	}

	@Test
	void longTest() {

		rfep.setArguments(Map.of("epoch", new JsonPrimitive(16964941310L)));

		StepVerifier.create(etd.execute(rfep))
		        .expectNextMatches(r ->
				{
			        return r.next()
			                .getResult()
			                .get("date")
			                .getAsString()
			                .equals("2507-08-07T11:41:50.000Z");
		        })
		        .verifyComplete();
	}

	@Test
	void stringTest() {

		rfep.setArguments(Map.of("epoch", new JsonPrimitive("1696494131000")));

		StepVerifier.create(etd.execute(rfep))
		        .expectNextMatches(r ->
				{
			        return r.next()
			                .getResult()
			                .get("date")
			                .getAsString()
			                .equals("2023-10-05T08:22:11.000Z");
		        })
		        .verifyComplete();
	}

	@Test
	void stringTest2() {

		rfep.setArguments(Map.of("epoch", new JsonPrimitive("16964941310")));

		StepVerifier.create(etd.execute(rfep))
		        .expectNextMatches(r ->
				{
			        return r.next()
			                .getResult()
			                .get("date")
			                .getAsString()
			                .equals("2507-08-07T11:41:50.000Z");
		        })
		        .verifyComplete();

	}

	@Test
	void stringTest3() {
		rfep.setArguments(Map.of("epoch", new JsonPrimitive("1696494131")));

		StepVerifier.create(etd.execute(rfep))
		        .expectNextMatches(r ->
				{
			        return r.next()
			                .getResult()
			                .get("date")
			                .getAsString()
			                .equals("2023-10-05T08:22:11.000Z");
		        })
		        .verifyComplete();
	}

	@Test
	void stringTest4() {
		rfep.setArguments(Map.of("epoch", new JsonPrimitive("169640")));

		StepVerifier.create(etd.execute(rfep))
		        .expectNextMatches(r ->
				{
			        return r.next()
			                .getResult()
			                .get("date")
			                .getAsString()
			                .equals("1970-01-02T23:07:20.000Z");
		        })
		        .verifyComplete();
	}

	@Test
	void LongTest2() {
		rfep.setArguments(Map.of("epoch", new JsonPrimitive(1694072117L)));

		StepVerifier.create(etd.execute(rfep))
		        .expectNextMatches(r ->
				{
			        return r.next()
			                .getResult()
			                .get("date")
			                .getAsString()
			                .equals("2023-09-07T07:35:17.000Z");
		        })
		        .verifyComplete();
	}
	
	@Test
	void LongTest3() {
		
		rfep.setArguments(Map.of("epoch", new JsonPrimitive(1694072117000L)));

		StepVerifier.create(etd.execute(rfep))
		        .expectNextMatches(r ->
				{
			        return r.next()
			                .getResult()
			                .get("date")
			                .getAsString()
			                .equals("2023-09-07T07:35:17.000Z");
		        })
		        .verifyComplete();
	}

	@Test
	void booleanTest() {
		rfep.setArguments(Map.of("epoch", new JsonPrimitive(true)));

		StepVerifier.create(etd.execute(rfep))
		        .expectError()
		        .verify();
	}

	@Test
	void dateTest() {
		rfep.setArguments(Map.of("epoch", new JsonPrimitive(1653171L)));

		StepVerifier.create(etd.execute(rfep))
		        .expectNextMatches(r -> r.next()
		                .getResult()
		                .get("date")
		                .getAsString()
		                .equals("1970-01-20T03:12:51.000Z"))
		        .verifyComplete();
	}

	@Test
	void dateTest2() {
		rfep.setArguments(Map.of("epoch", new JsonPrimitive(1696431L)));

		StepVerifier.create(etd.execute(rfep))
		        .expectNextMatches(r -> r.next()
		                .getResult()
		                .get("date")
		                .getAsString()
		                .equals("1970-01-20T15:13:51.000Z"))
		        .verifyComplete();
	}

}
