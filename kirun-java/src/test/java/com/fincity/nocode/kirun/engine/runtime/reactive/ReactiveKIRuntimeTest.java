package com.fincity.nocode.kirun.engine.runtime.reactive;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.function.system.GenerateEvent;
import com.fincity.nocode.kirun.engine.function.system.If;
import com.fincity.nocode.kirun.engine.function.system.context.Create;
import com.fincity.nocode.kirun.engine.function.system.context.Set;
import com.fincity.nocode.kirun.engine.function.system.loop.RangeLoop;
import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.model.Event;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionDefinition;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.model.Parameter;
import com.fincity.nocode.kirun.engine.model.ParameterReference;
import com.fincity.nocode.kirun.engine.model.Statement;
import com.fincity.nocode.kirun.engine.namespaces.Namespaces;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class ReactiveKIRuntimeTest {

	@Test
	void test() {

		// Testing the logic for Fibonacci series.

		long start = System.currentTimeMillis();
		Integer num = 10;
		JsonArray array = new JsonArray(num);
		int a = 0, b = 1;
		array.add(a);
		array.add(b);

		for (int i = 2; i < num; i++) {

			array.add(array.get(i - 2)
					.getAsInt()
					+ array.get(i - 1)
							.getAsInt());
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
		var createArray = new Statement("createArray").setNamespace(create.getNamespace())
				.setName(create.getName())
				.setParameterMap(Map.of("name", Map.ofEntries(ParameterReference.of(new JsonPrimitive("a"))), "schema",
						Map.ofEntries(ParameterReference.of(arrayOfIntegerSchema))));

		var rangeLoop = new RangeLoop().getSignature();
		var loop = new Statement("loop").setNamespace(rangeLoop.getNamespace())
				.setName(rangeLoop.getName())
				.setParameterMap(Map.of("from", Map.ofEntries(ParameterReference.of(new JsonPrimitive(0))), "to",
						Map.ofEntries(ParameterReference.of("Arguments.Count"))))
				.setDependentStatements(Map.of("Steps.createArray.output", true));

		var resultObj = new JsonObject();
		resultObj.add("name", new JsonPrimitive("result"));
		var expression = new JsonObject();
		expression.addProperty("isExpression", true);
		expression.addProperty("value", "Context.a");
		resultObj.add("value", expression);

		var generate = new GenerateEvent().getSignature();
		var outputGenerate = new Statement("outputStep").setNamespace(generate.getNamespace())
				.setName(generate.getName())
				.setParameterMap(Map.of("eventName", Map.ofEntries(ParameterReference.of(new JsonPrimitive("output"))),
						"results", Map.ofEntries(ParameterReference.of(resultObj))))
				.setDependentStatements(Map.of("Steps.loop.output", true));

		var ifFunction = new If().getSignature();
		var ifStep = new Statement("if").setNamespace(ifFunction.getNamespace())
				.setName(ifFunction.getName())
				.setParameterMap(Map.of("condition", Map.ofEntries(
						ParameterReference
								.of("(Steps.loop.iteration.index = 0) or (Steps.loop.iteration.index = 1)"))));

		var set = new Set().getSignature();
		var set1 = new Statement("setOnTrue").setNamespace(set.getNamespace())
				.setName(set.getName())
				.setParameterMap(Map.of("name",
						Map.ofEntries(
								ParameterReference.of(new JsonPrimitive("Context.a[Steps.loop.iteration.index]"))),
						"value", Map.ofEntries(ParameterReference.of("Steps.loop.iteration.index"))))
				.setDependentStatements(Map.of("Steps.if.true", true));

		var set2 = new Statement("setOnFalse").setNamespace(set.getNamespace())
				.setName(set.getName())
				.setParameterMap(Map.of("name",
						Map.ofEntries(
								ParameterReference.of(new JsonPrimitive("Context.a[Steps.loop.iteration.index]"))),
						"value",
						Map.ofEntries(ParameterReference.of(
								"Context.a[Steps.loop.iteration.index - 1] + Context.a[Steps.loop.iteration.index - 2]"))))
				.setDependentStatements(Map.of("Steps.if.false", true));

		long start1 = System.currentTimeMillis();
		ReactiveKIRuntime runtime = new ReactiveKIRuntime(((FunctionDefinition) new FunctionDefinition()
				.setSteps(Map.ofEntries(Statement.ofEntry(createArray), Statement.ofEntry(loop),
						Statement.ofEntry(outputGenerate), Statement.ofEntry(ifStep), Statement.ofEntry(set1),
						Statement.ofEntry(set2)))
				.setNamespace("Test")
				.setName("Fibonacci")
				.setEvents(Map.ofEntries(Event
						.outputEventMapEntry(Map.of("result", Schema.ofArray("result", Schema.ofInteger("result"))))))
				.setParameters(Map.of("Count", new Parameter().setParameterName("Count")
						.setSchema(Schema.ofInteger("count"))))),
				true);
		var out = runtime
				.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
						new KIRunReactiveSchemaRepository()).setArguments(Map.of("Count", new JsonPrimitive(num))))
				.map(FunctionOutput::allResults);

		out.subscribe(e -> {
			System.out.println("KIRun Logic : " + (System.currentTimeMillis() - start1));
		});

		StepVerifier.create(out)
				.expectNext(List.of(new EventResult().setName("output")
						.setResult(Map.of("result", array))))
				.verifyComplete();
	}

	@Test
	void testSingleFunctionCall() {

		var genEvent = new GenerateEvent().getSignature();

		var resultObj = new JsonObject();
		resultObj.add("name", new JsonPrimitive("result"));
		var expression = new JsonObject();
		expression.addProperty("isExpression", true);
		expression.addProperty("value", "Steps.first.output.value");
		resultObj.add("value", expression);

		var runtime = new ReactiveKIRuntime(((FunctionDefinition) new FunctionDefinition().setNamespace("Test")
				.setName("SingleCall")
				.setParameters(Map.of("Value", new Parameter().setParameterName("Value")
						.setSchema(Schema.ofInteger("Value")))))
				.setSteps(Map.ofEntries(Statement.ofEntry(new Statement("first").setNamespace(Namespaces.MATH)
						.setName("Absolute")
						.setParameterMap(Map.of("value", Map.ofEntries(ParameterReference.of("Arguments.Value"))))),
						Statement.ofEntry(new Statement("second").setNamespace(genEvent.getNamespace())
								.setName(genEvent.getName())
								.setParameterMap(Map.of("eventName",
										Map.ofEntries(ParameterReference.of(new JsonPrimitive("output"))), "results",
										Map.ofEntries(ParameterReference.of(resultObj))))))),
				true);

		var out = runtime
				.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
						new KIRunReactiveSchemaRepository()).setArguments(Map.of("Value", new JsonPrimitive(-10))))
				.map(e -> e.allResults());

		StepVerifier.create(out)
				.expectNext(List.of(new EventResult().setName("output")
						.setResult(Map.of("result", new JsonPrimitive(10)))))
				.verifyComplete();

	}

}
