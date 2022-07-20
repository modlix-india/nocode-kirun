package com.fincity.nocode.kirun.engine.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.HybridRepository;
import com.fincity.nocode.kirun.engine.function.AbstractFunction;
import com.fincity.nocode.kirun.engine.function.system.GenerateEvent;
import com.fincity.nocode.kirun.engine.function.system.If;
import com.fincity.nocode.kirun.engine.function.system.context.Create;
import com.fincity.nocode.kirun.engine.function.system.context.Set;
import com.fincity.nocode.kirun.engine.function.system.loop.RangeLoop;
import com.fincity.nocode.kirun.engine.function.system.math.MathFunctionRepository;
import com.fincity.nocode.kirun.engine.json.JsonExpression;
import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.model.Event;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionDefinition;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;
import com.fincity.nocode.kirun.engine.model.Parameter;
import com.fincity.nocode.kirun.engine.model.ParameterReference;
import com.fincity.nocode.kirun.engine.model.Statement;
import com.fincity.nocode.kirun.engine.namespaces.Namespaces;
import com.fincity.nocode.kirun.engine.repository.KIRunFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.KIRunSchemaRepository;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

class KIRuntimeTest {

	@Test
	void test() {

		// Testing the logic for Fibonacci series.

		long start = System.currentTimeMillis();
		Integer num = 7000;
		JsonArray array = new JsonArray(num);
		int a = 0, b = 1;
		array.add(a);
		array.add(b);

		for (int i = 2; i < num; i++) {

			array.add(array.get(i - 2).getAsInt() + array.get(i - 1).getAsInt());
		}

		System.out.println("Normal Logic : " + (System.currentTimeMillis() - start));

		var create = new Create().getSignature();
		var arrayOfIntegerSchema = new JsonObject();
		arrayOfIntegerSchema.addProperty("name", "ArrayType");
		arrayOfIntegerSchema.addProperty("type", "ARRAY");
		arrayOfIntegerSchema.add("defaultValue", new JsonArray());
		var integerSchema = new JsonObject();
		integerSchema.addProperty("name", "EachElement");
		integerSchema.addProperty("type", "INTEGER");
		arrayOfIntegerSchema.add("items", integerSchema);
		var createArray = new Statement("createArray").setNamespace(create.getNamespace()).setName(create.getName())
				.setParameterMap(Map.of("name", List.of(ParameterReference.of(new JsonPrimitive("a"))), "schema",
						List.of(ParameterReference.of(arrayOfIntegerSchema))));

		var rangeLoop = new RangeLoop().getSignature();
		var loop = new Statement("loop").setNamespace(rangeLoop.getNamespace()).setName(rangeLoop.getName())
				.setParameterMap(Map.of("from", List.of(ParameterReference.of(new JsonPrimitive(0))), "to",
						List.of(ParameterReference.of("Arguments.Count"))))
				.setDependentStatements(List.of("Steps.createArray.output"));

		var resultObj = new JsonObject();
		resultObj.add("name", new JsonPrimitive("result"));
		resultObj.add("value", new JsonExpression("Context.a"));

		var generate = new GenerateEvent().getSignature();
		var outputGenerate = new Statement("outputStep").setNamespace(generate.getNamespace())
				.setName(generate.getName())
				.setParameterMap(Map.of("eventName", List.of(ParameterReference.of(new JsonPrimitive("output"))),
						"results", List.of(ParameterReference.of(resultObj))))
				.setDependentStatements(List.of("Steps.loop.output"));

		var ifFunction = new If().getSignature();
		var ifStep = new Statement("if").setNamespace(ifFunction.getNamespace()).setName(ifFunction.getName())
				.setParameterMap(Map.of("condition", List.of(
						ParameterReference.of("Steps.loop.iteration.index = 0 or Steps.loop.iteration.index = 1"))));

		var set = new Set().getSignature();
		var set1 = new Statement("setOnTrue").setNamespace(set.getNamespace()).setName(set.getName())
				.setParameterMap(Map.of("name",
						List.of(ParameterReference.of(new JsonPrimitive("Context.a[Steps.loop.iteration.index]"))),
						"value", List.of(ParameterReference.of("Steps.loop.iteration.index"))))
				.setDependentStatements(List.of("Steps.if.true"));

		var set2 = new Statement("setOnFalse").setNamespace(set.getNamespace()).setName(set.getName())
				.setParameterMap(Map.of("name",
						List.of(ParameterReference.of(new JsonPrimitive("Context.a[Steps.loop.iteration.index]"))),
						"value",
						List.of(ParameterReference.of(
								"Context.a[Steps.loop.iteration.index - 1] + Context.a[Steps.loop.iteration.index - 2]"))))
				.setDependentStatements(List.of("Steps.if.false"));

		start = System.currentTimeMillis();
		List<EventResult> out = new KIRuntime(
				((FunctionDefinition) new FunctionDefinition()
						.setSteps(Map.ofEntries(Statement.ofEntry(createArray), Statement.ofEntry(loop),
								Statement.ofEntry(outputGenerate), Statement.ofEntry(ifStep), Statement.ofEntry(set1),
								Statement.ofEntry(set2)))
						.setNamespace("Test").setName("Fibonacci")
						.setEvents(Map.ofEntries(Event.outputEventMapEntry(
								Map.of("result", Schema.ofArray("result", Schema.ofInteger("result"))))))
						.setParameters(Map.of("Count",
								new Parameter().setParameterName("Count").setSchema(Schema.ofInteger("count"))))),
				new KIRunFunctionRepository(), new KIRunSchemaRepository())
				.execute(new FunctionExecutionParameters().setArguments(Map.of("Count", new JsonPrimitive(num))))
				.allResults();
		System.out.println("KIRun Logic : " + (System.currentTimeMillis() - start));
		assertEquals(List.of(new EventResult().setName("output").setResult(Map.of("result", array))), out);

//		StepVerifier
//		        .create()
//		        .expectNext(new EventResult().setName("output")
//		                .setResult(Map.of("result", array)))
//		        .verifyComplete();
	}

	@Test
	void testSingleFunctionCall() {

		var Absolute = new MathFunctionRepository().find(Namespaces.MATH, "Absolute");

		var genEvent = new GenerateEvent().getSignature();

		var resultObj = new JsonObject();
		resultObj.add("name", new JsonPrimitive("result"));
		resultObj.add("value", new JsonExpression("Steps.first.output.value"));

		List<EventResult> out = new KIRuntime(
				((FunctionDefinition) new FunctionDefinition().setNamespace("Test").setName("SingleCall")
						.setParameters(
								Map.of("Value",
										new Parameter().setParameterName("Value")
												.setSchema(Schema.ofInteger("Value")))))
						.setSteps(Map.ofEntries(
								Statement.ofEntry(
										new Statement("first").setNamespace(Namespaces.MATH).setName("Absolute")
												.setParameterMap(Map.of("value",
														List.of(ParameterReference.of("Arguments.Value"))))),
								Statement.ofEntry(new Statement("second").setNamespace(genEvent.getNamespace())
										.setName(genEvent.getName())
										.setParameterMap(Map.of("eventName",
												List.of(ParameterReference.of(new JsonPrimitive("output"))), "results",
												List.of(ParameterReference.of(resultObj))))))),
				new KIRunFunctionRepository(), new KIRunSchemaRepository())
				.execute(new FunctionExecutionParameters().setArguments(Map.of("Value", new JsonPrimitive(-10))))
				.allResults();

//		StepVerifier.create(out)
//		        .expectNext(new EventResult().setName("output")
//		                .setResult(Map.of("result", new JsonPrimitive(10))))
//		        .verifyComplete();

		assertEquals(List.of(new EventResult().setName("output").setResult(Map.of("result", new JsonPrimitive(10)))),
				out);
	}

	@Test
	void testCbrtCall() {

		var cbrt = new MathFunctionRepository().find(Namespaces.MATH, "CubeRoot").getSignature();

		var genEvent = new GenerateEvent().getSignature();

		var resultObj = new JsonObject();
		resultObj.add("name", new JsonPrimitive("result"));
		resultObj.add("value", new JsonExpression("Steps.first.output.value"));

		List<EventResult> out = new KIRuntime(
				((FunctionDefinition) new FunctionDefinition().setNamespace("Test").setName("SingleCall")
						.setParameters(
								Map.of("Value",
										new Parameter().setParameterName("Value")
												.setSchema(Schema.ofInteger("Value")))))
						.setSteps(Map.ofEntries(
								Statement.ofEntry(
										new Statement("first").setNamespace(cbrt.getNamespace()).setName(cbrt.getName())
												.setParameterMap(Map.of("value",
														List.of(ParameterReference.of("Arguments.Value"))))),
								Statement.ofEntry(new Statement("second").setNamespace(genEvent.getNamespace())
										.setName(genEvent.getName())
										.setParameterMap(Map.of("eventName",
												List.of(ParameterReference.of(new JsonPrimitive("output"))), "results",
												List.of(ParameterReference.of(resultObj))))))),
				new KIRunFunctionRepository(), new KIRunSchemaRepository())
				.execute(new FunctionExecutionParameters().setArguments(Map.of("Value", new JsonPrimitive(27))))
				.allResults();

		assertEquals(List.of(new EventResult().setName("output").setResult(Map.of("result", new JsonPrimitive(3.0d)))),
				out);
	}

	@Test
	void testCustomFunction() {

		long start = System.currentTimeMillis();
		Integer num = 7000;
		JsonArray array = new JsonArray(num);
		int a = 0, b = 1;
		array.add(a);
		array.add(b);

		for (int i = 2; i < num; i++) {

			array.add(array.get(i - 2).getAsInt() + array.get(i - 1).getAsInt());
		}

		System.out.println("Normal Logic : " + (System.currentTimeMillis() - start));

		var fibFunctionSignature = new FunctionSignature().setName("FibFunction").setNamespace("FibSpace")
				.setParameters(
						Map.of("value", new Parameter().setParameterName("value").setSchema(Schema.ofInteger("value"))))
				.setEvents(Map.ofEntries(Event
						.outputEventMapEntry(Map.of("value", Schema.ofArray("value", Schema.ofInteger("value"))))));
		;

		var fibFunction = new AbstractFunction() {

			@Override
			public FunctionSignature getSignature() {
				return fibFunctionSignature;
			}

			@Override
			protected FunctionOutput internalExecute(FunctionExecutionParameters context) {

				JsonElement e = context.getArguments().get("value");
				int count = e.getAsJsonPrimitive().getAsInt();

				JsonArray a = new JsonArray(count);
				for (int i = 0; i < count; i++)
					a.add(new JsonPrimitive(i < 2 ? i : (a.get(i - 1).getAsInt() + a.get(i - 2).getAsInt())));
				return new FunctionOutput(List.of(EventResult.outputOf(Map.of("value", a))));
			}
		};

		var genEvent = new GenerateEvent().getSignature();

		var resultObj = new JsonObject();
		resultObj.add("name", new JsonPrimitive("result"));
		resultObj.add("value", new JsonExpression("Steps.fib.output.value"));

		var hybrid = new HybridRepository<>(new KIRunFunctionRepository(), (r, k) -> fibFunction);

		start = System.currentTimeMillis();
		List<EventResult> out = new KIRuntime(
				((FunctionDefinition) new FunctionDefinition().setNamespace("Test").setName("CustomFunction")
						.setParameters(
								Map.of("Value",
										new Parameter().setParameterName("Value")
												.setSchema(Schema.ofInteger("Value")))))
						.setSteps(
								Map.ofEntries(
										Statement
												.ofEntry(new Statement("fib")
														.setNamespace(fibFunctionSignature.getNamespace())
														.setName("asdf")
														.setParameterMap(Map.of(
																"value",
																List.of(ParameterReference.of("Arguments.Value"))))),
										Statement.ofEntry(new Statement("fiboutput")
												.setNamespace(genEvent.getNamespace()).setName(genEvent.getName())
												.setParameterMap(Map.of("eventName",
														List.of(ParameterReference.of(new JsonPrimitive("output"))),
														"results", List.of(ParameterReference.of(resultObj))))))),
				hybrid, new KIRunSchemaRepository())
				.execute(new FunctionExecutionParameters().setArguments(Map.of("Value", new JsonPrimitive(num))))
				.allResults();
		System.out.println("KIRun Logic : " + (System.currentTimeMillis() - start));
		assertEquals(List.of(new EventResult().setName("output").setResult(Map.of("result", array))), out);
	}
}
