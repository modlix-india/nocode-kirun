package com.fincity.nocode.kirun.engine.runtime;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.json.schema.type.Type;
import com.fincity.nocode.kirun.engine.json.schema.type.Type.SchemaTypeAdapter;
import com.fincity.nocode.kirun.engine.model.FunctionDefinition;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.reactive.ReactiveHybridRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveKIRuntime;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import reactor.test.StepVerifier;

class KIRuntimeWithDefinitionSetContextTest {

	@Test
	void test() {

		Gson gson = new GsonBuilder().registerTypeAdapter(Type.class, new SchemaTypeAdapter())
				.create();

		var first = new ReactiveKIRuntime(gson.fromJson(
				"""
						{
							"name": "first",
							"namespace": "Internal",
							"steps": {
								"create": {
										"statementName": "create",
										"name": "Create",
										"namespace": "System.Context",
										"position": {
										"left": 80,
										"top": 71.5
										},
										"parameterMap": {
										"name": {
											"4okvvaj0VCFFIx4FPC1fRN": {
											"key": "4okvvaj0VCFFIx4FPC1fRN",
											"type": "VALUE",
											"expression": "",
											"order": 1,
											"value": "versionObject"
											}
										},
										"schema": {
											"2HiPAqgAZoXygvm4UytSFo": {
												"key": "2HiPAqgAZoXygvm4UytSFo",
												"type": "VALUE",
												"expression": "",
												"order": 1,
												"value": {
													"type": "OBJECT",
													"properties": {}
												}
											}
										}
									}
								},

								 "set": {
										"statementName": "set",
										"name": "Set",
										"namespace": "System.Context",
										"position": {
										"left": 368,
										"top": 78.5
										},
										"dependentStatements": {
										"Steps.create.output": true
										},
										"parameterMap": {
										"name": {
											"5fJ0Jih11Fm5YG1ZqD0rKo": {
											"key": "5fJ0Jih11Fm5YG1ZqD0rKo",
											"type": "VALUE",
											"expression": "",
											"order": 1,
											"value": "Context.versionObject"
											}
										},
										"value": {
											"27lNiE0fHDpnlTdoMZUbRe": {
											"key": "27lNiE0fHDpnlTdoMZUbRe",
											"type": "VALUE",
											"expression": "",
											"order": 1,
											"value": {
												"notes": "",
												"number": 0,
												"definition": "",
												"item": ""
											}
											}
										}
										}
									}
							}
						}
							""",
				FunctionDefinition.class), true);

		var repo = new ReactiveHybridRepository<>(new KIRunReactiveFunctionRepository());

		var results = first
				.execute(new ReactiveFunctionExecutionParameters(repo, new KIRunReactiveSchemaRepository(), "Testing"))
				.map(FunctionOutput::next).map(e -> e.getResult().isEmpty());

		StepVerifier.create(results).expectNext(true).verifyComplete();
	}

}
