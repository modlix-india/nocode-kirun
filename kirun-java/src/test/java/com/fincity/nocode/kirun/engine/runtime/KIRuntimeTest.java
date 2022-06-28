package com.fincity.nocode.kirun.engine.runtime;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.function.math.Abs;
import com.fincity.nocode.kirun.engine.function.system.GenerateEvent;
import com.fincity.nocode.kirun.engine.json.JsonExpression;
import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionDefinition;
import com.fincity.nocode.kirun.engine.model.Parameter;
import com.fincity.nocode.kirun.engine.model.ParameterReference;
import com.fincity.nocode.kirun.engine.model.Statement;
import com.fincity.nocode.kirun.engine.repository.KIRunFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.KIRunSchemaRepository;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import reactor.core.publisher.Flux;
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

//		StepVerifier.create(new KIRuntime(((FunctionDefinition) new FunctionDefinition().setNamespace("Test")
//		        .setName("Fibonacci")
//		        .setParameters(Map.of("Count", new Parameter().setParameterName("Count")
//		                .setSchema(Schema.INTEGER))))
//		        .setSteps(Map.ofEntries(
//		        		Statement.ofEntry(new Statement("")),
//		        		Statement.ofEntry(new Statement("")),
//		        		Statement.ofEntry(new Statement(""))
//		        		)))
//		        .execute(Map.of(), Map.of()))
//		        .expectNext(EventResult.outputOf(Map.ofEntries(Map.entry("Series", array))));
	}

	@Test
	void testSingleFunctionCall() {

		var abs = new Abs().getSignature();

		var genEvent = new GenerateEvent().getSignature();

		var resultObj = new JsonObject();
		resultObj.add("name", new JsonPrimitive("result"));
		resultObj.add("value", new JsonExpression("Steps.first.output.value"));

		Flux<EventResult> out = new KIRuntime(((FunctionDefinition) new FunctionDefinition().setNamespace("Test")
		        .setName("SingleCall")
		        .setParameters(Map.of("Value", new Parameter().setParameterName("Value")
		                .setSchema(Schema.INTEGER))))
		        .setSteps(Map.ofEntries(Statement.ofEntry(new Statement("first").setNamespace(abs.getNamespace())
		                .setName(abs.getName())
		                .setParameterMap(Map.of("value", List.of(ParameterReference.of("Arguments.Value"))))), Statement
		                        .ofEntry(new Statement("second").setNamespace(genEvent.getNamespace())
		                                .setName(genEvent.getName())
		                                .setParameterMap(Map.of("eventName",
		                                        List.of(ParameterReference.of(new JsonPrimitive("output"))), "results",
		                                        List.of(ParameterReference.of(resultObj))))))),
		        new KIRunFunctionRepository(), new KIRunSchemaRepository())
		        .execute(new FunctionExecutionParameters().setArguments(Map.of("Value", new JsonPrimitive(-10))));

		StepVerifier.create(out)
		        .expectNext(new EventResult().setName("output")
		                .setResult(Map.of("result", new JsonPrimitive(10))))
		        .verifyComplete();
	}
}
