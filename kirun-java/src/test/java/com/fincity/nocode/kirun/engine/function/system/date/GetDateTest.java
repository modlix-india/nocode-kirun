package com.fincity.nocode.kirun.engine.function.system.date;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.namespaces.Namespaces;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class GetDateTest {

	DateFunctionRepository dfr = new DateFunctionRepository();

	ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
	        new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository());

	@Test
	void test() {

		fep.setArguments(Map.of("isoDate", new JsonPrimitive("2023-12-31T07:35:17.000-12:00")));

		StepVerifier.create(dfr.find(Namespaces.DATE, "GetDate")
		        .flatMap(e -> e.execute(fep)))
		        .expectNextMatches(res -> res.next()
		                .getResult()
		                .get("date")
		                .getAsInt() == 1)
		        .verifyComplete();

		fep.setArguments(Map.of("isoDate", new JsonPrimitive("2023-09-07T17:35:17.123-11:00")));

		StepVerifier.create(dfr.find(Namespaces.DATE, "GetDate")
		        .flatMap(e -> e.execute(fep)))
		        .expectNextMatches(res -> res.next()
		                .getResult()
		                .get("date")
		                .getAsInt() == 8)
		        .verifyComplete();

	}

	@Test
	void test1() {

		fep.setArguments(Map.of("isoDate", new JsonPrimitive("2023-09-07T07:35:17.000Z")));

		StepVerifier.create(dfr.find(Namespaces.DATE, "GetDate")
		        .flatMap(e -> e.execute(fep)))
		        .expectNextMatches(res -> res.next()
		                .getResult()
		                .get("date")
		                .getAsInt() == 7)
		        .verifyComplete();

	}
}
