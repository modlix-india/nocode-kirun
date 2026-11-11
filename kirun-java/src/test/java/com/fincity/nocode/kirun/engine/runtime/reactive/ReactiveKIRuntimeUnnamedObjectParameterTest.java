package com.fincity.nocode.kirun.engine.runtime.reactive;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.function.reactive.ReactiveFunction;
import com.fincity.nocode.kirun.engine.json.schema.array.ArraySchemaType;
import com.fincity.nocode.kirun.engine.json.schema.array.ArraySchemaType.ArraySchemaTypeAdapter;
import com.fincity.nocode.kirun.engine.json.schema.object.AdditionalType;
import com.fincity.nocode.kirun.engine.json.schema.object.AdditionalType.AdditionalTypeAdapter;
import com.fincity.nocode.kirun.engine.json.schema.type.Type;
import com.fincity.nocode.kirun.engine.json.schema.type.Type.SchemaTypeAdapter;
import com.fincity.nocode.kirun.engine.model.FunctionDefinition;
import com.fincity.nocode.kirun.engine.reactive.ReactiveHybridRepository;
import com.fincity.nocode.kirun.engine.reactive.ReactiveRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonPrimitive;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

// KIRUN-754: a literal object given as a VALUE to an app function's OBJECT parameter whose schema
// has no name used to fail the whole function with an NPE on Schema.getName().
class ReactiveKIRuntimeUnnamedObjectParameterTest {

	@Test
	void literalObjectForUnnamedObjectParameterRuns() {

		AdditionalTypeAdapter addType = new AdditionalTypeAdapter();
		ArraySchemaTypeAdapter asType = new ArraySchemaTypeAdapter();
		Gson gson = new GsonBuilder().registerTypeAdapter(Type.class, new SchemaTypeAdapter())
				.registerTypeAdapter(AdditionalType.class, addType)
				.registerTypeAdapter(ArraySchemaType.class, asType)
				.create();
		addType.setGson(gson);
		asType.setGson(gson);

		var inner = gson.fromJson("""
				{
				  "name": "g", "namespace": "T",
				  "parameters": { "q": { "parameterName": "q", "schema": { "type": ["OBJECT"], "defaultValue": {} } } },
				  "steps": {
				    "ge": { "statementName": "ge", "namespace": "System", "name": "GenerateEvent",
				      "parameterMap": { "results": { "one": { "key": "one", "type": "VALUE",
				        "value": { "name": "r", "value": { "isExpression": true, "value": "Arguments.q.fields" } } } } } }
				  }
				}""", FunctionDefinition.class);

		var outer = gson.fromJson("""
				{
				  "name": "o", "namespace": "T",
				  "steps": {
				    "wn": { "statementName": "wn", "namespace": "T", "name": "g",
				      "parameterMap": { "q": { "one": { "key": "one", "type": "VALUE", "value": { "fields": "name" } } } } },
				    "ge": { "statementName": "ge", "namespace": "System", "name": "GenerateEvent",
				      "parameterMap": { "results": { "one": { "key": "one", "type": "VALUE",
				        "value": { "name": "s", "value": { "isExpression": true, "value": "Steps.wn.output.r" } } } } } }
				  }
				}""", FunctionDefinition.class);

		var innerRuntime = new ReactiveKIRuntime(inner);

		class InternalRepository implements ReactiveRepository<ReactiveFunction> {

			@Override
			public Mono<ReactiveFunction> find(String namespace, String name) {
				if ("T".equals(namespace) && "g".equals(name))
					return Mono.just(innerRuntime);
				return Mono.empty();
			}

			@Override
			public Flux<String> filter(String name) {
				return Flux.empty();
			}
		}

		var repo = new ReactiveHybridRepository<>(new KIRunReactiveFunctionRepository(), new InternalRepository());

		var output = new ReactiveKIRuntime(outer)
				.execute(new ReactiveFunctionExecutionParameters(repo, new KIRunReactiveSchemaRepository())
						.setArguments(Map.of()))
				.map(fo -> fo.allResults()
						.get(0)
						.getResult());

		StepVerifier.create(output)
				.expectNext(Map.of("s", new JsonPrimitive("name")))
				.verifyComplete();
	}
}
