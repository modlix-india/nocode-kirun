package com.fincity.nocode.kirun.engine.runtime.reactive;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.function.reactive.ReactiveFunction;
import com.fincity.nocode.kirun.engine.json.schema.type.Type;
import com.fincity.nocode.kirun.engine.json.schema.type.Type.SchemaTypeAdapter;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionDefinition;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.reactive.ReactiveHybridRepository;
import com.fincity.nocode.kirun.engine.reactive.ReactiveRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class ReactiveKIRuntimeWithDefinitionOneTest {

	@Test
	void test() {

		Gson gson = new GsonBuilder().registerTypeAdapter(Type.class, new SchemaTypeAdapter())
		        .create();

		var first = new ReactiveKIRuntime(gson.fromJson(
		        """
		                {
		                		        "name": "First",
		                		        "namespace": "Internal",
		                		        "events": {
		                		            "output": {
		                		                "name": "output",
		                		                "parameters": { "aresult": { "name": "aresult", "type": "INTEGER" } }
		                		            }
		                		        },
		                		        "steps": {
		                		            "exSecond": {
		                		                "statementName": "exSecond",
		                		                "name": "Second",
		                		                "namespace": "Internal",
		                		                "parameterMap": {
		                		                    "svalue" : { "one" : { "key": "one", "type": "VALUE", "value": 2 } }
		                		                }
		                		            },
		                		            "exThird": {
		                		                "statementName": "exThird",
		                		                "name": "Third",
		                		                "namespace": "Internal",
		                		                "parameterMap": {
		                		                    "tvalue" : { "one" : { "key": "one", "type": "VALUE", "value": 3 } }
		                		                }
		                		            },
		                		            "genOutput": {
		                		                "statementName": "genOutput",
		                		                "namespace": "System",
		                		                "name": "GenerateEvent",
		                		                "dependentStatements": {
		                		                    "Steps.exSecond.output": true,
		                		                    "Steps.exThird.output": true
		                		                },
		                		                "parameterMap": {
		                		                    "eventName": { "one": { "key": "one", "type": "VALUE", "value": "output" } },
		                		                    "results": {
		                		                        "one": {
		                		                            "key": "one",
		                		                            "type": "VALUE",
		                		                            "value": {
		                		                                "name": "aresult",
		                		                                "value": { "isExpression": true, "value": "Steps.exSecond.output.result" }
		                		                            }
		                		                        }
		                		                    }
		                		                }
		                		            }
		                		        }
		                		    }""",
		        FunctionDefinition.class), true);

		var second = new ReactiveKIRuntime(
		        gson.fromJson(
		                """
		                        {
		                        		            "name": "Second",
		                        		            "namespace": "Internal",
		                        		            "parameters": {
		                        		                "svalue": { "parameterName": "svalue", "schema": { "name": "INTEGER", "type": "INTEGER" } } },
		                        		            "events": {
		                        		                "output": {
		                        		                    "name": "output",
		                        		                    "parameters": { "result": { "name": "result", "type": "INTEGER" } }
		                        		                }
		                        		            },
		                        		            "steps": {
		                        		                "genOutput": {
		                        		                    "statementName": "genOutput",
		                        		                    "namespace": "System",
		                        		                    "name": "GenerateEvent",
		                        		                    "parameterMap": {
		                        		                        "eventName": { "one": { "key": "one", "type": "VALUE", "value": "output" } },
		                        		                        "results": {
		                        		                            "one": {
		                        		                                "key": "one",
		                        		                                "type": "VALUE",
		                        		                                "value": {
		                        		                                    "name": "result",
		                        		                                    "value": { "isExpression": true, "value": "Arguments.svalue * 2" }
		                        		                                }
		                        		                            }
		                        		                        }
		                        		                    }
		                        		                }
		                        		            }
		                        		        }""",
		                FunctionDefinition.class),

		        true);

		var third = new ReactiveKIRuntime(gson.fromJson(
		        """
		                {
		                		            "name": "Third",
		                		            "namespace": "Internal",
		                		            "parameters": {
		                		                "tvalue": { "parameterName": "tvalue", "schema": { "name": "INTEGER", "type": "INTEGER" } } },
		                		            "events": {
		                		                "output": {
		                		                    "name": "output",
		                		                    "parameters": { "result": { "name": "result", "type": "INTEGER" } }
		                		                }
		                		            },
		                		            "steps": {
		                		                "genOutput": {
		                		                    "statementName": "genOutput",
		                		                    "namespace": "System",
		                		                    "name": "GenerateEvent",
		                		                    "parameterMap": {
		                		                        "eventName": { "one": { "key": "one", "type": "VALUE", "value": "output" } },
		                		                        "results": {
		                		                            "one": {
		                		                                "key": "one",
		                		                                "type": "VALUE",
		                		                                "value": {
		                		                                    "name": "result",
		                		                                    "value": { "isExpression": true, "value": "Arguments.tvalue * 3" }
		                		                                }
		                		                            }
		                		                        }
		                		                    }
		                		                }
		                		            }
		                		        }""",
		        FunctionDefinition.class), true);

		class InternalRepository implements ReactiveRepository<ReactiveFunction> {

			@Override
			public Mono<ReactiveFunction> find(String namespace, String name) {

				if (!"Internal".equals(namespace))
					return null;

				if ("Third".equals(name))
					return Mono.just(third);
				if ("Second".equals(name))
					return Mono.just(second);

				return null;
			}

			@Override
			public Flux<String> filter(String name) {
				return Flux.empty();
			}
		}

		var repo = new ReactiveHybridRepository<>(new KIRunReactiveFunctionRepository(), new InternalRepository());

		var results = first
		        .execute(new ReactiveFunctionExecutionParameters(repo, new KIRunReactiveSchemaRepository(), "Testing"));

		;

		results.onErrorContinue((e, x) -> {
			System.out.println("Output : " + e);
			System.out.println(first.getDebugString());
		});

		StepVerifier.create(results.map(FunctionOutput::next)
		        .map(EventResult::getResult)
		        .map(e -> e.get("aresult"))
		        .map(JsonElement::getAsInt))
		        .expectNext(4)
		        .verifyComplete();
	}
}
