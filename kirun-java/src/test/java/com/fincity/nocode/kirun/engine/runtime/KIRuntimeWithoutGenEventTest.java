package com.fincity.nocode.kirun.engine.runtime;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.exception.KIRuntimeException;
import com.fincity.nocode.kirun.engine.json.schema.type.Type;
import com.fincity.nocode.kirun.engine.json.schema.type.Type.SchemaTypeAdapter;
import com.fincity.nocode.kirun.engine.model.FunctionDefinition;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveKIRuntime;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class KIRuntimeWithoutGenEventTest {

	@Test
	void test3() {

		Gson gson = new GsonBuilder().registerTypeAdapter(Type.class, new SchemaTypeAdapter()).create();

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
						            "parameters": { "additionResult": { "name": "additionResult", "type": "INTEGER" } }
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
				new KIRunReactiveSchemaRepository())
				.setArguments(
						Map.of("a", new JsonPrimitive(7), "b", new JsonPrimitive(11), "c", new JsonPrimitive(13)));

		ByteArrayOutputStream outContent = new ByteArrayOutputStream();
		System.setErr(new PrintStream(outContent));
		StepVerifier.create(func.execute(fep)).expectError(KIRuntimeException.class).verify();
	}

	@Test
	void test2() {

		Gson gson = new GsonBuilder().registerTypeAdapter(Type.class, new SchemaTypeAdapter()).create();

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
				new KIRunReactiveSchemaRepository())
				.setArguments(
						Map.of("a", new JsonPrimitive(7), "b", new JsonPrimitive(11), "c", new JsonPrimitive(13)));

		ByteArrayOutputStream outContent = new ByteArrayOutputStream();
		System.setErr(new PrintStream(outContent));
		func.execute(fep).block();
		assertEquals("\"Something muchh....\"" + System.lineSeparator() + "31" + System.lineSeparator(),
				outContent.toString());
	}

	@Test
	void test1() {

		Gson gson = new GsonBuilder().registerTypeAdapter(Type.class, new SchemaTypeAdapter()).create();

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

		var fep = new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
				new KIRunReactiveSchemaRepository())
				.setArguments(
						Map.of("a", new JsonPrimitive(7), "b", new JsonPrimitive(11), "c", new JsonPrimitive(13)));

		ByteArrayOutputStream outContent = new ByteArrayOutputStream();
		System.setOut(new PrintStream(outContent));
		func.execute(fep).block();
		assertEquals("\"Nothing muchh....\"" + System.lineSeparator() + "31" + System.lineSeparator(),
				outContent.toString());
	}
}
