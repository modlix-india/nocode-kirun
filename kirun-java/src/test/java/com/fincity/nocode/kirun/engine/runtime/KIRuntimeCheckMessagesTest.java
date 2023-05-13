package com.fincity.nocode.kirun.engine.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.exception.KIRuntimeException;
import com.fincity.nocode.kirun.engine.json.schema.array.ArraySchemaType;
import com.fincity.nocode.kirun.engine.json.schema.array.ArraySchemaType.ArraySchemaTypeAdapter;
import com.fincity.nocode.kirun.engine.json.schema.object.AdditionalType;
import com.fincity.nocode.kirun.engine.json.schema.object.AdditionalType.AdditionalTypeAdapter;
import com.fincity.nocode.kirun.engine.json.schema.type.Type;
import com.fincity.nocode.kirun.engine.json.schema.type.Type.SchemaTypeAdapter;
import com.fincity.nocode.kirun.engine.model.FunctionDefinition;
import com.fincity.nocode.kirun.engine.repository.KIRunFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.KIRunSchemaRepository;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonPrimitive;

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

        var graph = new KIRuntime(fd, false).getExecutionPlan(new KIRunFunctionRepository(),
                new KIRunSchemaRepository());

        List<String> messages = graph.getNodeMap()
                .values()
                .stream()
                .flatMap((node) -> {
                    return node.getData()
                            .getMessages()
                            .stream()
                            .map((e) -> e.getMessage());
                })
                .toList();

        assertEquals(List.of("Unable to find the step with name add"), messages);

        KIRuntimeException sve = assertThrows(KIRuntimeException.class, () -> new KIRuntime(fd)
                .execute(new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository())
                        .setArguments(Map.of("a", new JsonPrimitive(7), "b", new JsonPrimitive(11), "c",
                                new JsonPrimitive(13)))));

        assertEquals(
                "Error while executing the function UIApp.getAppData with step name 'Unknown Step' with error : Please fix the errors in the function definition before execution : \n"
                        + "[StatementMessage(messageType=ERROR, message=Unable to find the step with name add)]",
                sve.getMessage());

    }

}
