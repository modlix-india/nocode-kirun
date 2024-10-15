package com.fincity.nocode.kirun.engine.runtime.reactive;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.json.schema.array.ArraySchemaType;
import com.fincity.nocode.kirun.engine.json.schema.array.ArraySchemaType.ArraySchemaTypeAdapter;
import com.fincity.nocode.kirun.engine.json.schema.object.AdditionalType;
import com.fincity.nocode.kirun.engine.json.schema.object.AdditionalType.AdditionalTypeAdapter;
import com.fincity.nocode.kirun.engine.json.schema.type.Type;
import com.fincity.nocode.kirun.engine.json.schema.type.Type.SchemaTypeAdapter;
import com.fincity.nocode.kirun.engine.model.FunctionDefinition;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class ReactiveKIRuntimeDependencyTest {

  private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
  private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
  private final PrintStream originalOut = System.out;
  private final PrintStream originalErr = System.err;

  @BeforeEach
  public void setUpStreams() {
    System.setOut(new PrintStream(outContent));
    System.setErr(new PrintStream(errContent));
  }

  @AfterEach
  public void restoreStreams() {
    System.setOut(originalOut);
    System.setErr(originalErr);
  }

  @Test
  void test() {

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
          "name": "getAppData",
          "namespace": "UIApp",
          "parameters": {
            "a": {
              "parameterName": "a",
              "schema": {
                "name": "INTEGER",
                "type": [
                  "ARRAY"
                ],
                "items": {
                  "type": [
                    "OBJECT",
                    "NULL"
                  ]
                }
              }
            }
          },
          "steps": {
            "forEach": {
              "statementName": "forEach",
              "namespace": "System.Loop",
              "name": "ForEachLoop",
              "parameterMap": {
                "source": {
                  "one": {
                    "key": "one",
                    "type": "EXPRESSION",
                    "expression": "Arguments.a"
                  }
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
                    "expression": "Steps.forEach.iteration.each.name"
                  }
                }
              },
              "executeIftrue": {
                "Steps.forEach.iteration.each.name": true
              }
            }
          }
        }
        		        """;

    var fd = gson.fromJson(func, FunctionDefinition.class);

    JsonArray ja = new JsonArray();

    JsonObject job1 = new JsonObject();
    job1.addProperty("name", "Kiran");
    job1.addProperty("age", 40);
    ja.add(job1);

    ja.add(JsonNull.INSTANCE);

    JsonObject job2 = new JsonObject();
    job2.addProperty("name", "Kumar");
    job2.addProperty("age", 39);
    ja.add(job2);

    var rt = new ReactiveKIRuntime(fd, true);

    Mono<FunctionOutput> mf = rt.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
        new KIRunReactiveSchemaRepository()).setArguments(Map.of("a", ja)));

    StepVerifier.create(mf.map(e -> e.allResults().get(0).getResult().isEmpty() ? outContent.toString() : ""))
        .expectNext("\"Kiran\"" + System.lineSeparator() + "\"Kumar\"" + System.lineSeparator())
        .verifyComplete();

  }

  @Test
  void test1() {

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
          "name": "getAppData",
          "namespace": "UIApp",
          "parameters": {
            "a": {
              "parameterName": "a",
              "schema": {
                "name": "INTEGER",
                "type": [
                  "ARRAY"
                ],
                "items": {
                  "type": [
                    "OBJECT",
                    "NULL"
                  ]
                }
              }
            }
          },
          "steps": {
            "forEach": {
              "statementName": "forEach",
              "namespace": "System.Loop",
              "name": "ForEachLoop",
              "parameterMap": {
                "source": {
                  "one": {
                    "key": "one",
                    "type": "EXPRESSION",
                    "expression": "Arguments.a"
                  }
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
                    "expression": "Steps.forEach.iteration.each.name"
                  }
                }
              },
              "executeIftrue": {
                "(Steps.forEach.iteration.each.age ?? 1) % 2 = 0": true
              }
            }
          }
        }
        		        """;

    var fd = gson.fromJson(func, FunctionDefinition.class);

    JsonArray ja = new JsonArray();

    JsonObject job1 = new JsonObject();
    job1.addProperty("name", "Kiran");
    job1.addProperty("age", 40);
    ja.add(job1);

    ja.add(JsonNull.INSTANCE);

    JsonObject job2 = new JsonObject();
    job2.addProperty("name", "Kumar");
    job2.addProperty("age", 39);
    ja.add(job2);

    var rt = new ReactiveKIRuntime(fd, true);

    Mono<FunctionOutput> mf = rt.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
        new KIRunReactiveSchemaRepository()).setArguments(Map.of("a", ja)));

    StepVerifier.create(mf.map(e -> e.allResults().get(0).getResult().isEmpty() ? outContent.toString() : ""))
        .expectNext("\"Kiran\"" + System.lineSeparator())
        .verifyComplete();
  }

}
