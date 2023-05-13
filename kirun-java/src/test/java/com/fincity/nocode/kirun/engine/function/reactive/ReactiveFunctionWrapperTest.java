package com.fincity.nocode.kirun.engine.function.reactive;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.namespaces.Namespaces;
import com.fincity.nocode.kirun.engine.repository.KIRunSchemaRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class ReactiveFunctionWrapperTest {

	@Test
	void test() {

		var repo = new KIRunReactiveFunctionRepository();

		Mono<JsonElement> output = repo.find(Namespaces.MATH, "Log10")
		        .flatMap(e ->
				{
			        return e.execute(new ReactiveFunctionExecutionParameters(repo, new KIRunSchemaRepository())
			                .setArguments(Map.of("value", new JsonPrimitive(144))));

		        })
		        .map(e -> e.next()
		                .getResult()
		                .get("value"));

		StepVerifier.create(output)
		        .expectNext(new JsonPrimitive(2.1583624920952498))
		        .expectComplete()
		        .verify();

	}

}
