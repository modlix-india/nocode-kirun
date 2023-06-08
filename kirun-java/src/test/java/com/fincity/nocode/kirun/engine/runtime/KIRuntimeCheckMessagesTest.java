package com.fincity.nocode.kirun.engine.runtime;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.json.schema.array.ArraySchemaType;
import com.fincity.nocode.kirun.engine.json.schema.array.ArraySchemaType.ArraySchemaTypeAdapter;
import com.fincity.nocode.kirun.engine.json.schema.object.AdditionalType;
import com.fincity.nocode.kirun.engine.json.schema.object.AdditionalType.AdditionalTypeAdapter;
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

class KIRuntimeCheckMessagesTest {

  @Test
  void test() {

    AdditionalTypeAdapter addType = new AdditionalTypeAdapter();
    ArraySchemaTypeAdapter asType = new ArraySchemaTypeAdapter();

    Gson gson = new GsonBuilder().setPrettyPrinting().registerTypeAdapter(Type.class, new SchemaTypeAdapter())
        .registerTypeAdapter(AdditionalType.class, addType)
        .registerTypeAdapter(ArraySchemaType.class, asType)
        .create();

    addType.setGson(gson);
    asType.setGson(gson);

    var func = """
                        {
          "name": "getAppData",
          "namespace": "UIApp",
          "parameters": {
            "a": {
              "parameterName": "a",
              "schema": {
                "name": "INTEGER",
                "type": "INTEGER"
              }
            },
            "b": {
              "parameterName": "b",
              "schema": {
                "name": "INTEGER",
                "type": "INTEGER"
              }
            },
            "c": {
              "parameterName": "c",
              "schema": {
                "name": "INTEGER",
                "type": "INTEGER"
              }
            }
          },
          "events": {
            "output": {
              "name": "output",
              "parameters": {
                "additionResult": {
                  "name": "additionResult",
                  "type": "INTEGER"
                }
              }
            }
          },
          "steps": {
            "add1": {
              "statementName": "add1",
              "namespace": "System.Math",
              "name": "Add",
              "parameterMap": {
                "value": {
                  "one": {
                    "key": "one",
                    "type": "EXPRESSION",
                    "expression": "Arguments.a"
                  },
                  "two": {
                    "key": "two",
                    "type": "EXPRESSION",
                    "expression": "10 + 1"
                  },
                  "three": {
                    "key": "three",
                    "type": "EXPRESSION",
                    "expression": "Arguments.c"
                  }
                }
              }
            },
            "genOutput": {
              "statementName": "genOutput",
              "namespace": "System",
              "name": "GenerateEvent",
              "parameterMap": {
                "eventName": {
                  "one": {
                    "key": "one",
                    "type": "VALUE",
                    "value": "output"
                  }
                },
                "results": {
                  "one": {
                    "key": "one",
                    "type": "VALUE",
                    "value": {
                      "name": "additionResult",
                      "value": {
                        "isExpression": true,
                        "value": "Steps.add.output.value"
                      }
                    }
                  }
                }
              }
            }
          }
        }
                        """;

    var fd = gson.fromJson(func, FunctionDefinition.class);

    var graph = new ReactiveKIRuntime(fd, false).getExecutionPlan(new KIRunReactiveFunctionRepository(),
        new KIRunReactiveSchemaRepository());

    var messages = graph.map(g -> g.getNodeMap()
        .values()
        .stream()
        .flatMap((node) -> {
          return node.getData()
              .getMessages()
              .stream()
              .map((e) -> e.getMessage());
        })
        .toList());

    StepVerifier.create(messages)
        .expectNext(List.of("Unable to find the step with name add"))
        .verifyComplete();

    StepVerifier.create(new ReactiveKIRuntime(fd)
        .execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
            new KIRunReactiveSchemaRepository())
            .setArguments(Map.of("a", new JsonPrimitive(7), "b", new JsonPrimitive(11), "c",
                new JsonPrimitive(13)))))
        .verifyErrorMessage("Please fix the errors in the function definition before execution : \n"
            + "[StatementMessage(messageType=ERROR, message=Unable to find the step with name add)]");
  }

}