package com.fincity.nocode.kirun.engine.runtime.reactive;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.exception.KIRuntimeException;
import com.fincity.nocode.kirun.engine.json.schema.type.Type;
import com.fincity.nocode.kirun.engine.json.schema.type.Type.SchemaTypeAdapter;
import com.fincity.nocode.kirun.engine.model.FunctionDefinition;
import com.fincity.nocode.kirun.engine.repository.KIRunSchemaRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class ReactiveKIRuntimeWithoutGenEventTest {

	@Test
	void test3() {

		Gson gson = new GsonBuilder().registerTypeAdapter(Type.class, new SchemaTypeAdapter())
		        .create();

		var func = new ReactiveKIRuntime(gson.fromJson("""
		              {
		            "name": "getAppData",
		            "namespace": "UIApp",
		            "parameters": {
		                "a": { "parameterName": "a", "schema": { "name": "INTEGER", "type": "INTEGER" } },
		                "b": { "parameterName": "b", "schema": { "name": "INTEGER", "type": "INTEGER" } },
		                "c": { "parameterName": "c", "schema": { "name": "INTEGER", "type": "INTEGER" } }
		            },
		            "events": {
		        		"output": {
		        		            "name": "output",
		        		            "parameters": { "additionResult": { "name": "additionResult", type: "INTEGER" } }
		        		        }
		            },
		            "steps": {
		                "add": {
		                    "statementName": "add",
		                    "namespace": "System.Math",
		                    "name": "Add",
		                    "parameterMap": {
		                        "value": {
		                            "one": { "key": "one", "type": "EXPRESSION", "expression": "Arguments.a" },
		                            "two": { "key": "two", "type": "EXPRESSION", "expression": "10 + 1" },
		                            "three": { "key": "three", "type": "EXPRESSION", "expression": "Arguments.c" }
		                        }
		                    }
		                },
		                "print": {
		                    "statementName": "print",
		                    "namespace": "System",
		                    "name": "Print",
		                    "parameterMap": {
		                        "values": {
		                            "one": {
		                                "key": "one",
		                                "type": "EXPRESSION",
		                                "expression": "Steps.add.output.value",
		                                "order": 2
		                            },
		                            "abc": {
		                                "key": "abc",
		                                "type": "VALUE",
		                                "value": "Something muchh....",
		                                "order": 1
		                            }
		                        },
		                        "stream": { "one": {"key": "one", "type": "VALUE", "value": "STDERR" }}
		                    }
		                }
		            }
		        }""", FunctionDefinition.class), false);

		var fep = new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
		        new KIRunSchemaRepository()).setArguments(
		                Map.of("a", new JsonPrimitive(7), "b", new JsonPrimitive(11), "c", new JsonPrimitive(13)));

		StepVerifier.create(func.execute(fep))
		        .expectError(KIRuntimeException.class);
	}

	@Test
	void test2() {

		Gson gson = new GsonBuilder().registerTypeAdapter(Type.class, new SchemaTypeAdapter())
		        .create();

		var func = new ReactiveKIRuntime(gson.fromJson("""
		              {
		            "name": "getAppData",
		            "namespace": "UIApp",
		            "parameters": {
		                "a": { "parameterName": "a", "schema": { "name": "INTEGER", "type": "INTEGER" } },
		                "b": { "parameterName": "b", "schema": { "name": "INTEGER", "type": "INTEGER" } },
		                "c": { "parameterName": "c", "schema": { "name": "INTEGER", "type": "INTEGER" } }
		            },
		            "events": {
		        		"output": {
		        		            "name": "output",
		        		            "parameters": {}
		        		        }
		            },
		            "steps": {
		                "add": {
		                    "statementName": "add",
		                    "namespace": "System.Math",
		                    "name": "Add",
		                    "parameterMap": {
		                        "value": {
		                            "one": { "key": "one", "type": "EXPRESSION", "expression": "Arguments.a" },
		                            "two": { "key": "two", "type": "EXPRESSION", "expression": "10 + 1" },
		                            "three": { "key": "three", "type": "EXPRESSION", "expression": "Arguments.c" }
		                        }
		                    }
		                },
		                "print": {
		                    "statementName": "print",
		                    "namespace": "System",
		                    "name": "Print",
		                    "parameterMap": {
		                        "values": {
		                            "one": {
		                                "key": "one",
		                                "type": "EXPRESSION",
		                                "expression": "Steps.add.output.value",
		                                "order": 2
		                            },
		                            "abc": {
		                                "key": "abc",
		                                "type": "VALUE",
		                                "value": "Something muchh....",
		                                "order": 1
		                            }
		                        },
		                        "stream": { "one": {"key": "one", "type": "VALUE", "value": "STDERR" }}
		                    }
		                }
		            }
		        }""", FunctionDefinition.class), false);

		var fep = new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
		        new KIRunSchemaRepository()).setArguments(
		                Map.of("a", new JsonPrimitive(7), "b", new JsonPrimitive(11), "c", new JsonPrimitive(13)));

		StepVerifier.create(func.execute(fep))
		        .expectError(KIRuntimeException.class);
	}

	@Test
	void test1() {

		Gson gson = new GsonBuilder().registerTypeAdapter(Type.class, new SchemaTypeAdapter())
		        .create();

		var func = new ReactiveKIRuntime(gson.fromJson("""
		              {
		            "name": "getAppData",
		            "namespace": "UIApp",
		            "parameters": {
		                "a": { "parameterName": "a", "schema": { "name": "INTEGER", "type": "INTEGER" } },
		                "b": { "parameterName": "b", "schema": { "name": "INTEGER", "type": "INTEGER" } },
		                "c": { "parameterName": "c", "schema": { "name": "INTEGER", "type": "INTEGER" } }
		            },
		            "steps": {
		                "add": {
		                    "statementName": "add",
		                    "namespace": "System.Math",
		                    "name": "Add",
		                    "parameterMap": {
		                        "value": {
		                            "one": { "key": "one", "type": "EXPRESSION", "expression": "Arguments.a" },
		                            "two": { "key": "two", "type": "EXPRESSION", "expression": "10 + 1" },
		                            "three": { "key": "three", "type": "EXPRESSION", "expression": "Arguments.c" }
		                        }
		                    }
		                },
		                "print": {
		                    "statementName": "print",
		                    "namespace": "System",
		                    "name": "Print",
		                    "parameterMap": {
		                        "values": {
		                            "one": {
		                                "key": "one",
		                                "type": "EXPRESSION",
		                                "expression": "Steps.add.output.value",
		                                "order": 2
		                            },
		                            "abc": {
		                                "key": "abc",
		                                "type": "VALUE",
		                                "value": "Nothing muchh....",
		                                "order": 1
		                            }
		                        }
		                    }
		                }
		            }
		        }""", FunctionDefinition.class), false);

		var fep = new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(), new KIRunSchemaRepository())
		        .setArguments(
		                Map.of("a", new JsonPrimitive(7), "b", new JsonPrimitive(11), "c", new JsonPrimitive(13)));

		StepVerifier.create(func.execute(fep))
		        .expectError(KIRuntimeException.class);
	}
}
