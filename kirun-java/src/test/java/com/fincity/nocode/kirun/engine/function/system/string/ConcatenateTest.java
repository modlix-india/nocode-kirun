package com.fincity.nocode.kirun.engine.function.system.string;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class ConcatenateTest {

	@Test
	void test() {
		Concatenate catF = new Concatenate();
		var list = new JsonArray();
		list.add("no code ");
		list.add(" Kirun ");
		list.add(" true ");
		list.add("\"'this is between the strings qith special characters'\"");
		list.add(" PLATform ");

		StepVerifier
				.create(catF.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
						new KIRunReactiveSchemaRepository()).setArguments(Map.of("value", list))))
				.expectNextMatches(result -> result.next().getResult().get("value").equals(new JsonPrimitive(
						"no code  Kirun  true \"'this is between the strings qith special characters'\" PLATform ")))
				.verifyComplete();
	}

}
