package com.fincity.nocode.kirun.engine.runtime;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.function.reactive.ReactiveFunction;
import com.fincity.nocode.kirun.engine.json.schema.type.Type;
import com.fincity.nocode.kirun.engine.json.schema.type.Type.SchemaTypeAdapter;
import com.fincity.nocode.kirun.engine.model.FunctionDefinition;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.reactive.ReactiveHybridRepository;
import com.fincity.nocode.kirun.engine.reactive.ReactiveRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveKIRuntime;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonPrimitive;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class KIRuntimeWithDefinitionOneTest {

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
								                    "value" : { "one" : { "key": "one", "type": "VALUE", "value": 2 } }
								                }
								            },
								            "exThird": {
								                "statementName": "exThird",
								                "name": "Third",
								                "namespace": "Internal",
								                "parameterMap": {
								                    "value" : { "one" : { "key": "one", "type": "VALUE", "value": 3 } }
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
										                "value": { "parameterName": "value", "schema": { "name": "INTEGER", "type": "INTEGER" } } },
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
										                                    "value": { "isExpression": true, "value": "Arguments.value * 2" }
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
								                "value": { "parameterName": "value", "schema": { "name": "INTEGER", "type": "INTEGER" } } },
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
								                                    "value": { "isExpression": true, "value": "Arguments.value * 3" }
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
					return Mono.empty();

				if ("Third".equals(name))
					return Mono.just(third);
				if ("Second".equals(name))
					return Mono.just(second);

				return Mono.empty();
			}

			@Override
			public Flux<String> filter(String name) {
				return Flux.empty();
			}
		}

		var repo = new ReactiveHybridRepository<>(new KIRunReactiveFunctionRepository(), new InternalRepository());

		var results = first
				.execute(new ReactiveFunctionExecutionParameters(repo, new KIRunReactiveSchemaRepository(), "Testing"))
				.map(FunctionOutput::next).map(e -> e.getResult().get("aresult").getAsInt());

		StepVerifier.create(results).expectNext(4).verifyComplete();
	}

	@Test
	void test2() {
		Gson gson = new GsonBuilder().registerTypeAdapter(Type.class, new SchemaTypeAdapter())
				.create();

		var first = new ReactiveKIRuntime(gson.fromJson("""
						                {
				  "name": "test",
				  "namespace": "testNamespace",
				  "steps": {
				    "if": {
				      "statementName": "if",
				      "name": "If",
				      "namespace": "System",
				      "position": {
				        "left": 148,
				        "top": 135
				      },
				      "parameterMap": {
				        "condition": {
				          "3jzKlCvmE7dmTj1wIpGw8c": {
				            "key": "3jzKlCvmE7dmTj1wIpGw8c",
				            "type": "EXPRESSION",
				            "expression": "Arguments.a % 2 = 1",
				            "order": 1
				          }
				        }
				      }
				    },
				    "generateEvent": {
				      "statementName": "generateEvent",
				      "name": "GenerateEvent",
				      "namespace": "System",
				      "position": {
				        "left": 507,
				        "top": 55.5
				      },
				      "parameterMap": {
				        "eventName": {
				          "5OdGxruBiEyysESbAubdX2": {
				            "key": "5OdGxruBiEyysESbAubdX2",
				            "type": "VALUE",
				            "expression": "",
				            "value": "output"
				          }
				        },
				        "results": {
				          "4o0c0kvVtWiGjgb37hMTBX": {
				            "key": "4o0c0kvVtWiGjgb37hMTBX",
				            "type": "VALUE",
				            "order": 1,
				            "value": {
				              "name": "returnValue",
				              "value": {
				                "isExpression": true,
				                "value": "Arguments.a + 1"
				              }
				            }
				          }
				        }
				      },
				      "dependentStatements": {
				        "Steps.if.true": true
				      }
				    },
				    "generateEvent1": {
				      "statementName": "generateEvent1",
				      "name": "GenerateEvent",
				      "namespace": "System",
				      "position": {
				        "left": 585,
				        "top": 345.5
				      },
				      "parameterMap": {
				        "results": {
				          "1QE9kyJacs1FemevUUjIOz": {
				            "key": "1QE9kyJacs1FemevUUjIOz",
				            "type": "VALUE",
				            "expression": "",
				            "order": 1,
				            "value": {
				              "name": "returnValue",
				              "value": {
				                "isExpression": true,
				                "value": "Arguments.a + 2"
				              }
				            }
				          }
				        },
				        "eventName": {
				          "J4uLUy3FSRuIWDVZzK85n": {
				            "key": "J4uLUy3FSRuIWDVZzK85n",
				            "type": "VALUE",
				            "expression": "",
				            "value": "output"
				          }
				        }
				      },
				      "dependentStatements": {
				        "Steps.if.false": true
				      }
				    }
				  },
				  "parameters": {
				    "a": {
				      "parameterName": "a",
				      "schema": {
				        "version": 1,
				        "type": [
				          "INTEGER"
				        ]
				      }
				    }
				  },
				  "events": {
				    "output": {
				      "name": "output",
				      "parameters": {
				        "returnValue": {
				          "schema": {
				            "type": [
				              "INTEGER"
				            ],
				            "version": 1
				          }
				        }
				      }
				    }
				  }
				}
						                """, FunctionDefinition.class), true);

		var results = first
				.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
						new KIRunReactiveSchemaRepository(),
						"Testing").setArguments(Map.of("a", new JsonPrimitive(10))))
				.map(fo -> fo.next().getResult().get("returnValue").getAsInt());

		StepVerifier.create(results).expectNext(12).verifyComplete();
	}
}
