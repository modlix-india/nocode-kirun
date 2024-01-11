package com.fincity.nocode.kirun.engine.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

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
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveKIRuntime;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

class KIRuntimeVariableArgDefaultNullTest {

    @Test
    void kiRuntimePrintFunctionWithNoArguments() {

        AdditionalTypeAdapter addType = new AdditionalTypeAdapter();
        ArraySchemaTypeAdapter asType = new ArraySchemaTypeAdapter();

        Gson gson = new GsonBuilder().setPrettyPrinting()
                .registerTypeAdapter(Type.class, new SchemaTypeAdapter())
                .registerTypeAdapter(AdditionalType.class, addType)
                .registerTypeAdapter(ArraySchemaType.class, asType)
                .create();

        addType.setGson(gson);
        asType.setGson(gson);

        var fd = new ReactiveKIRuntime(
                gson.fromJson("""
                             {
                                "name": "varArgWithNothing",
                                "namespace": "Test",
                                "steps": {
                                    "testFunction": {
                                        "statementName": "testFunction",
                                        "namespace": "LocalFunction",
                                        "name": "Other",
                                        "parameterMap": {
                                            "storageName": {
                                                "one": {
                                                    "type": "VALUE",
                                                    "value": "Test",
                                                    "key": "one",
                                                    "order": 1
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        """, FunctionDefinition.class), false);

        var OtherFunction = new ReactiveKIRuntime(
                gson.fromJson(
                        """
                                {
                                    "namespace": "LocalFunction",
                                    "name": "Other",
                                    "steps": {
                                      "print": {
                                        "statementName": "print",
                                        "namespace": "System",
                                        "name": "Print",
                                        "parameterMap": {
                                         "stream" :{
                                            "one" : {
                                                "type": "VALUE",
                                                "value": "STDERR",
                                                "key": "one",
                                                "order": 1
                                            }
                                         },
                                          "values": {
                                            "one": {
                                              "type": "EXPRESSION",
                                              "expression": "'Storage : ' + Arguments.storageName + '__'",
                                              "key": "one",
                                              "order": 1
                                            },
                                            "two": {
                                              "type": "EXPRESSION",
                                              "expression": "'Page : ' + Arguments.page + '__'",
                                              "key": "two",
                                              "order": 2
                                            },
                                            "three": {
                                              "type": "EXPRESSION",
                                              "expression": "'Size : ' + Arguments.size + '__'",
                                              "key": "three",
                                              "order": 3
                                            },
                                            "five": {
                                              "type": "EXPRESSION",
                                              "expression": "'Count : ' + Arguments.count + '__'",
                                              "key": "five",
                                              "order": 5
                                            },
                                            "six": {
                                              "type": "EXPRESSION",
                                              "expression": "'Client Code : ' + Arguments.clientCode + '__'",
                                              "key": "six",
                                              "order": 6
                                            },
                                            "eight": {
                                              "type": "EXPRESSION",
                                              "expression": "'Eager : ' + Arguments.eager + '__'",
                                              "key": "eight",
                                              "order": 8
                                            }
                                          }
                                        }
                                      }
                                    },
                                    "parameters": {
                                      "appCode": {
                                        "schema": {
                                          "namespace": "_",
                                          "name": "appCode",
                                          "version": 1,

                                            "type": "STRING",

                                          "defaultValue": ""
                                        },
                                        "parameterName": "appCode",
                                        "variableArgument": false,
                                        "type": "EXPRESSION"
                                      },
                                      "page": {
                                        "schema": {
                                          "namespace": "_",
                                          "name": "page",
                                          "version": 1,

                                            "type": "INTEGER",

                                          "defaultValue": 0
                                        },
                                        "parameterName": "page",
                                        "variableArgument": false,
                                        "type": "EXPRESSION"
                                      },
                                      "storageName": {
                                        "schema": {
                                          "namespace": "_",
                                          "name": "storageName",
                                          "version": 1,

                                            "type": "STRING"

                                        },
                                        "parameterName": "storageName",
                                        "variableArgument": false,
                                        "type": "EXPRESSION"
                                      },
                                      "size": {
                                        "schema": {
                                          "namespace": "_",
                                          "name": "size",
                                          "version": 1,
                                            "type": "INTEGER",
                                          "defaultValue": 20
                                        },
                                        "parameterName": "size",
                                        "variableArgument": false,
                                        "type": "EXPRESSION"
                                      },
                                      "count": {
                                        "schema": {
                                          "namespace": "_",
                                          "name": "count",
                                          "version": 1,
                                            "type": "BOOLEAN",
                                          "defaultValue": true
                                        },
                                        "parameterName": "count",
                                        "variableArgument": false,
                                        "type": "EXPRESSION"
                                      },
                                      "clientCode": {
                                        "schema": {
                                          "namespace": "_",
                                          "name": "clientCode",
                                          "version": 1,

                                            "type": "STRING",

                                          "defaultValue": ""
                                        },
                                        "parameterName": "clientCode",
                                        "variableArgument": false,
                                        "type": "EXPRESSION"
                                      },
                                      "eagerFields": {
                                        "schema": {
                                          "namespace": "_",
                                          "name": "eagerFields",
                                          "version": 1,
                                            "type": "STRING"

                                        },
                                        "parameterName": "eagerFields",
                                        "variableArgument": true,
                                        "type": "EXPRESSION"
                                      },
                                      "filter": {
                                        "schema": {
                                          "namespace": "_",
                                          "name": "filter",
                                          "version": 1,

                                            "type": "OBJECT",

                                          "defaultValue": {}
                                        },
                                        "parameterName": "filter",
                                        "variableArgument": false,
                                        "type": "EXPRESSION"
                                      },
                                      "eager": {
                                        "schema": {
                                          "namespace": "_",
                                          "name": "eager",
                                          "version": 1,

                                            "type": "BOOLEAN",

                                          "defaultValue": false
                                        },
                                        "parameterName": "eager",
                                        "variableArgument": false,
                                        "type": "EXPRESSION"
                                      }
                                    }
                                  }
                                        """, FunctionDefinition.class),
                false);

        // let result = await new KIRuntime(fd, false).execute(
        // new FunctionExecutionParameters(
        // new HybridRepository<Function>(new KIRunFunctionRepository(), {
        // find: async (namespace: string, name: string): Promise<Function | undefined>
        // => {
        // if (namespace === 'LocalFunction' && name === 'Other') return OtherFunction;
        // return undefined;
        // },

        // filter: async (name: string): Promise<string[]> => {
        // return ['LocalFunction.Other'];
        // },
        // }),
        // new KIRunSchemaRepository(),
        // ),
        // );

        // console.log = oldConsole;

        // expect(test.mock.calls[0]).toMatchObject([
        // 'Storage : Test__',
        // 'Page : 0__',
        // 'Size : 20__',
        // 'Count : true__',
        // 'Client Code : __',
        // 'Eager : false__',
        // ]);
        // });

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        var oldOut = System.err;
        System.setErr(new PrintStream(outContent));

        var result = fd.execute(
                new ReactiveFunctionExecutionParameters(
                        new ReactiveHybridRepository<ReactiveFunction>(new KIRunReactiveFunctionRepository(),
                                new ReactiveRepository<ReactiveFunction>() {
                                    public reactor.core.publisher.Mono<ReactiveFunction> find(String namespace,
                                            String name) {
                                        if (namespace.equals("LocalFunction") && name.equals("Other"))
                                            return reactor.core.publisher.Mono.just(OtherFunction);
                                        return reactor.core.publisher.Mono.empty();
                                    }

                                    public reactor.core.publisher.Flux<String> filter(String name) {
                                        return reactor.core.publisher.Flux.just("LocalFunction.Other");
                                    }
                                }),
                        new KIRunReactiveSchemaRepository()))
                .block();

        System.setErr(oldOut);
        String expected = """
                "Storage : Test__"
                "Page : 0__"
                "Size : 20__"
                "Count : true__"
                "Client Code : __"
                "Eager : false__"
                    """;
        assertEquals(expected, outContent.toString());
    }
}
