package com.fincity.nocode.kirun.engine.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.function.math.MathFunctionRepository;
import com.fincity.nocode.kirun.engine.function.system.GenerateEvent;
import com.fincity.nocode.kirun.engine.json.JsonExpression;
import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionDefinition;
import com.fincity.nocode.kirun.engine.model.Parameter;
import com.fincity.nocode.kirun.engine.model.ParameterReference;
import com.fincity.nocode.kirun.engine.model.Statement;
import com.fincity.nocode.kirun.engine.namespaces.Namespaces;
import com.fincity.nocode.kirun.engine.repository.KIRunFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.KIRunSchemaRepository;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class Binarytest {

	@Test
	void HypotTest() {
		var hypt = new MathFunctionRepository().find(Namespaces.MATH, "Hypotenuse").getSignature();
		var genEvent = new GenerateEvent().getSignature();

		var resultObj = new JsonObject();
		resultObj.add("name", new JsonPrimitive("result"));
		resultObj.add("value", new JsonExpression("Steps.first.output.value"));

		List<EventResult> out = new KIRuntime(
				((FunctionDefinition) new FunctionDefinition().setNamespace("Test").setName("SingleCall")
						.setParameters(
								Map.of("value1",
										new Parameter().setParameterName("value1").setSchema(Schema.ofNumber("value1")),
										"value2",
										new Parameter().setParameterName("value2")
												.setSchema(Schema.ofNumber("value2")))))
						.setSteps(Map.ofEntries(
								Statement.ofEntry(
										new Statement("first").setNamespace(hypt.getNamespace()).setName(hypt.getName())
												.setParameterMap(Map.of("value1",
														List.of(ParameterReference.of("Arguments.value1")), "value2",
														List.of(ParameterReference.of("Arguments.value2"))))),
								Statement.ofEntry(new Statement("second").setNamespace(genEvent.getNamespace())
										.setName(genEvent.getName())
										.setParameterMap(Map.of("eventName",
												List.of(ParameterReference.of(new JsonPrimitive("output"))), "results",
												List.of(ParameterReference.of(resultObj))))))),
				new KIRunFunctionRepository(), new KIRunSchemaRepository())
				.execute(new FunctionExecutionParameters()
						.setArguments(Map.of("value1", new JsonPrimitive(12), "value2", new JsonPrimitive(4))))
				.allResults();

		assertEquals(List.of(new EventResult().setName("output")
				.setResult(Map.of("result", new JsonPrimitive(12.649110640673518d)))), out);
	}

	@Test
	void ArcTangentTest() {

		var arcTan = new MathFunctionRepository().find(Namespaces.MATH, "ArcTangent").getSignature();

		var genEvent = new GenerateEvent().getSignature();
		var nanVal = Double.POSITIVE_INFINITY - Double.POSITIVE_INFINITY;

		var resultObj = new JsonObject();
		resultObj.add("name", new JsonPrimitive("result"));
		resultObj.add("value", new JsonExpression("Steps.first.output.value"));

		List<EventResult> out = new KIRuntime(
				((FunctionDefinition) new FunctionDefinition().setNamespace("Test").setName("SingleCall")
						.setParameters(
								Map.of("value1",
										new Parameter().setParameterName("value1").setSchema(Schema.ofNumber("value1")),
										"value2",
										new Parameter().setParameterName("value2")
												.setSchema(Schema.ofNumber("value2")))))
						.setSteps(
								Map.ofEntries(
										Statement.ofEntry(new Statement("first").setNamespace(arcTan.getNamespace())
												.setName(arcTan.getName())
												.setParameterMap(Map.of("value1",
														List.of(ParameterReference.of("Arguments.value1")), "value2",
														List.of(ParameterReference.of("Arguments.value2"))))),
										Statement.ofEntry(new Statement("second").setNamespace(genEvent.getNamespace())
												.setName(genEvent.getName())
												.setParameterMap(Map.of("eventName",
														List.of(ParameterReference.of(new JsonPrimitive("output"))),
														"results", List.of(ParameterReference.of(resultObj))))))),
				new KIRunFunctionRepository(), new KIRunSchemaRepository())
				.execute(new FunctionExecutionParameters()
						.setArguments(Map.of("value1", new JsonPrimitive(12), "value2", new JsonPrimitive(4))))
				.allResults();

		assertEquals(List.of(new EventResult().setName("output")
				.setResult(Map.of("result", new JsonPrimitive(1.2490457723982544d)))), out);

	}

}
