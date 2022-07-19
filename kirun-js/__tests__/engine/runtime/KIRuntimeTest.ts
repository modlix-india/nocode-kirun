import { KIRuntime } from '../../../src/engine/runtime/KIRuntime';

test("KIRuntime Test", () => {

    let start = new Date().getTime();
	let num:number = 7000;
    let array:number[] = [];
	let a = 0, b = 1;
		array.push(a);
		array.push(b);

		for (let i = 2; i < num; i++) {

			array.push(array[i-2] + array[i-1]);
		}

		console.log("Normal Logic : " + (new Date().getTime() - start));

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
});