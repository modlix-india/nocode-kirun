package com.fincity.nocode.kirun.engine.runtime.reactive;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.json.schema.array.ArraySchemaType;
import com.fincity.nocode.kirun.engine.json.schema.array.ArraySchemaType.ArraySchemaTypeAdapter;
import com.fincity.nocode.kirun.engine.json.schema.object.AdditionalType;
import com.fincity.nocode.kirun.engine.json.schema.object.AdditionalType.AdditionalTypeAdapter;
import com.fincity.nocode.kirun.engine.json.schema.type.Type;
import com.fincity.nocode.kirun.engine.json.schema.type.Type.SchemaTypeAdapter;
import com.fincity.nocode.kirun.engine.model.FunctionDefinition;
import com.fincity.nocode.kirun.engine.repository.KIRunSchemaRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class ReactiveKIRuntimeMessagesTest {

	@Test
	void testMessages1() {

		AdditionalTypeAdapter addType = new AdditionalTypeAdapter();
		ArraySchemaTypeAdapter asType = new ArraySchemaTypeAdapter();

		Gson gson = new GsonBuilder().setPrettyPrinting()
		        .registerTypeAdapter(Type.class, new SchemaTypeAdapter())
		        .registerTypeAdapter(AdditionalType.class, addType)
		        .registerTypeAdapter(ArraySchemaType.class, asType)
		        .create();

		addType.setGson(gson);
		asType.setGson(gson);

		var func = """
		        		        {
		          "name": "loginFunction",
		          "steps": {
		            "messageStep": {
		              "statementName": "messageStep",
		              "namespace": "UIEngine",
		              "name": "Message",
		              "parameterMap": {
		                "msg": {
		                  "value1": {
		                    "key": "value1",
		                    "type": "EXPRESSION",
		                    "expression": "Steps.loginStep.error.data"
		                  }
		                }
		              },
		              "position": {
		                "left": 198,
		                "top": 245
		              }
		            },
		            "genOutput": {
		              "statementName": "genOutput",
		              "namespace": "System",
		              "name": "GenerateEvent",
		              "dependentStatements": {
		                "Steps.loginStep.output": true
		              },
		              "position": {
		                "left": 482,
		                "top": 172
		              }
		            },
		            "loginStep1": {
		              "name": "Login",
		              "namespace": "UIEngine",
		              "statementName": "loginStep1",
		              "parameterMap": {
		                "userName": {
		                  "value1": {
		                    "key": "value1",
		                    "type": "EXPRESSION",
		                    "expression": "Page.user.userName"
		                  }
		                },
		                "password": {
		                  "value1": {
		                    "key": "value1",
		                    "type": "EXPRESSION",
		                    "expression": "Page.user.password"
		                  }
		                },
		                "rememberMe": {
		                  "value1": {
		                    "key": "value1",
		                    "type": "EXPRESSION",
		                    "expression": "Page.user.rememberMe"
		                  }
		                }
		              },
		              "position": {
		                "left": 472,
		                "top": 333
		              }
		            }
		          }
		        }
		        		        """;

		var fd = gson.fromJson(func, FunctionDefinition.class);

		var graph = new ReactiveKIRuntime(fd, false).getExecutionPlan(new KIRunReactiveFunctionRepository(),
		        new KIRunSchemaRepository());

		Mono<List<String>> messages = graph.map(g -> g.getNodeMap()
		        .values()
		        .stream()
		        .flatMap((node) ->
				{
			        return node.getData()
			                .getMessages()
			                .stream()
			                .map((e) -> e.getMessage());
		        })
		        .toList());

		StepVerifier.create(messages)
		        .expectNext(List.of("UIEngine.Login is not available", "Unable to find the step with name loginStep",
		                "UIEngine.Message is not available"))
		        .verifyComplete();
	}
}