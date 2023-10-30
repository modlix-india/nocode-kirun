package com.fincity.nocode.kirun.engine.function.system.date;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonArray;

import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class GetTimeAsArrayTest {

	GetTimeAsArray gettimearray = new GetTimeAsArray();
	ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
			new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository());

	@Test
	public void test() {

		fep.setArguments(Map.of("date", new JsonPrimitive("2023-09-07T17:35:17.222Z")));

		StepVerifier.create(gettimearray.execute(fep)).expectNextMatches(r -> {
			JsonArray timeArray = r.allResults().get(0).getResult().get("output").getAsJsonArray();
			assertEquals(17, timeArray.get(0).getAsInt());
			assertEquals(35, timeArray.get(1).getAsInt());
			assertEquals(17, timeArray.get(2).getAsInt());

			return true;

		}).expectComplete().verify();
	}

	@Test
	public void test1() {

		fep.setArguments(Map.of("date", new JsonPrimitive("2023-09-07T18:45:01.000+11:00")));

		StepVerifier.create(gettimearray.execute(fep)).expectNextMatches(r -> {
			JsonArray timeArray = r.allResults().get(0).getResult().get("output").getAsJsonArray();
			assertEquals(18, timeArray.get(0).getAsInt());
			assertEquals(45, timeArray.get(1).getAsInt());
			assertEquals(01, timeArray.get(2).getAsInt());

			return true;

		}).expectComplete().verify();
	}

	@Test
	public void test2() {

		fep.setArguments(Map.of("date", new JsonPrimitive("2023-09-07T17:35:17Z")));

		StepVerifier.create(gettimearray.execute(fep)).expectNextMatches(r -> {
			JsonArray timeArray = r.allResults().get(0).getResult().get("output").getAsJsonArray();
			assertEquals(17, timeArray.get(0).getAsInt());
			assertEquals(35, timeArray.get(1).getAsInt());
			assertEquals(17, timeArray.get(2).getAsInt());

			return true;

		}).expectComplete().verify();
	}

}
