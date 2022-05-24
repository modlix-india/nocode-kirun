package com.fincity.nocode.kirun.engine.runtime;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.model.Argument;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionDefinition;
import com.fincity.nocode.kirun.engine.model.Parameter;
import com.fincity.nocode.kirun.engine.model.Statement;
import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class KIRuntimeTest {

	@Test
	void test() {

		// Testing the logic for Fibonacci series.

		Integer num = 7;
		JsonArray array = new JsonArray(7);
		int a = 0, b = 1;
		array.add(a);
		array.add(b);

		for (int i = 2; i < num; i++) {
			int t = a + b;
			a = b;
			b = t;
			array.add(t);
		}

		StepVerifier.create(new KIRuntime(((FunctionDefinition) new FunctionDefinition().setNamespace("Test")
		        .setName("Fibonacci")
		        .setParameters(Map.of("Count", new Parameter().setParameterName("Count")
		                .setSchema(Schema.INTEGER))))
		        .setSteps(Map.ofEntries(
		        		Statement.ofEntry(new Statement("")),
		        		Statement.ofEntry(new Statement("")),
		        		Statement.ofEntry(new Statement(""))
		        		)))
		        .execute(List.of(new Argument().setName("Count")
		                .setValue(new JsonPrimitive(num)))))
		        .expectNext(EventResult.outputResult(Map.ofEntries(Map.entry("Series", array))));
	}

}
