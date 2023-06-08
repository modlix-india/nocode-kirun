package com.fincity.nocode.kirun.engine.runtime;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.function.system.GenerateEvent;
import com.fincity.nocode.kirun.engine.function.system.math.Hypotenuse;
import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionDefinition;
import com.fincity.nocode.kirun.engine.model.Parameter;
import com.fincity.nocode.kirun.engine.model.ParameterReference;
import com.fincity.nocode.kirun.engine.model.Statement;
import com.fincity.nocode.kirun.engine.namespaces.Namespaces;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveKIRuntime;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

public class Binarytest {

	@Test
	void HypotTest() {

		var hypt = (new Hypotenuse()).getSignature();
		var genEvent = new GenerateEvent().getSignature();

		var resultObj = new JsonObject();
		resultObj.add("name", new JsonPrimitive("result"));
		var expression = new JsonObject();
		expression.addProperty("isExpression", true);
		expression.addProperty("value", "Steps.first.output.value");
		resultObj.add("value", expression);

		Mono<List<EventResult>> out = new ReactiveKIRuntime(
				((FunctionDefinition) new FunctionDefinition().setNamespace("Test")
						.setName("SingleCall")
						.setParameters(Map.of("value1", new Parameter().setParameterName("value1")
								.setSchema(Schema.ofNumber("value1")), "value2",
								new Parameter().setParameterName("value2")
										.setSchema(Schema.ofNumber("value2")))))
						.setSteps(Map.ofEntries(
								Statement.ofEntry(new Statement("first").setNamespace(hypt.getNamespace())
										.setName(hypt.getName())
										.setParameterMap(Map.of("value",
												Map.ofEntries(ParameterReference.of("Arguments.value1"),
														ParameterReference.of("Arguments.value2"))))),
								Statement.ofEntry(new Statement("second").setNamespace(genEvent.getNamespace())
										.setName(genEvent.getName())
										.setParameterMap(
												Map.of("eventName",
														Map.ofEntries(
																ParameterReference.of(new JsonPrimitive("output"))),
														"results", Map.ofEntries(ParameterReference.of(resultObj))))))))
				.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
						new KIRunReactiveSchemaRepository())
						.setArguments(Map.of("value1", new JsonPrimitive(12), "value2", new JsonPrimitive(4))))
				.map(fo -> fo.allResults());

		StepVerifier.create(out)
				.expectNext(List.of(new EventResult().setName("output")
						.setResult(Map.of("result", new JsonPrimitive(12.649110640673518d)))))
				.verifyComplete();
	}

	@Test
	void ArcTangentTest() {

		var genEvent = new GenerateEvent().getSignature();

		var resultObj = new JsonObject();
		resultObj.add("name", new JsonPrimitive("result"));
		var expression = new JsonObject();
		expression.addProperty("isExpression", true);
		expression.addProperty("value", "Steps.first.output.value");
		resultObj.add("value", expression);

		var out = new ReactiveKIRuntime(((FunctionDefinition) new FunctionDefinition()
				.setNamespace("Test")
				.setName("SingleCall")
				.setParameters(Map.of("value1", new Parameter().setParameterName("value1")
						.setSchema(Schema.ofNumber("value1")), "value2",
						new Parameter().setParameterName("value2")
								.setSchema(Schema.ofNumber("value2")))))
				.setSteps(Map.ofEntries(Statement.ofEntry(new Statement("first").setNamespace(Namespaces.MATH)
						.setName("ArcTangent")
						.setParameterMap(Map.of("value", Map.ofEntries(ParameterReference.of("Arguments.value1"))))),
						Statement.ofEntry(new Statement("second").setNamespace(genEvent.getNamespace())
								.setName(genEvent.getName())
								.setParameterMap(
										Map.of("eventName",
												Map.ofEntries(ParameterReference.of(new JsonPrimitive("output"))),
												"results", Map.ofEntries(ParameterReference.of(resultObj))))))))
				.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
						new KIRunReactiveSchemaRepository())
						.setArguments(Map.of("value1", new JsonPrimitive(4), "value2", new JsonPrimitive(4))))
				.map(fo -> fo.allResults());

		StepVerifier.create(out)
				.expectNext(List.of(new EventResult().setName("output")
						.setResult(Map.of("result", new JsonPrimitive(1.3258176636680326d)))))
				.verifyComplete();
	}

}
