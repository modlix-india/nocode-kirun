package com.fincity.nocode.kirun.engine.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.HybridRepository;
import com.fincity.nocode.kirun.engine.Repository;
import com.fincity.nocode.kirun.engine.function.Function;
import com.fincity.nocode.kirun.engine.function.system.Print;
import com.fincity.nocode.kirun.engine.json.schema.type.Type;
import com.fincity.nocode.kirun.engine.json.schema.type.Type.SchemaTypeAdapter;
import com.fincity.nocode.kirun.engine.model.FunctionDefinition;
import com.fincity.nocode.kirun.engine.repository.KIRunFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.KIRunSchemaRepository;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

class KIRuntimeundefinedValueTest {

    @Test
    void test() {

        Gson gson = new GsonBuilder().registerTypeAdapter(Type.class, new SchemaTypeAdapter())
                .create();

        var first = new KIRuntime(gson.fromJson(
                """

                                        {
                        "name": "Make an error",
                        "namespace": "UIApp",
                        "steps": {
                        "print": {
                        "statementName": "print",
                        "namespace": "function",
                        "name": "test",
                        "parameterMap": {
                        "values": {
                          "one": {
                            "key": "one",
                            "type": "VALUE",
                            "value": null
                          },
                          "two":{
                              "key":"two",
                              "type":"VALUE"
                          },
                            "three":{
                              "key":"three",
                              "type":"VALUE",
                              "value":undefined
                          }
                        }
                        }
                        }
                        }
                        }""", FunctionDefinition.class));

        Print printMethod = new Print();

        class InternalRepository implements Repository<Function> {

            @Override
            public Function find(String namespace, String name) {

                if ("function".equals(namespace))
                    return printMethod;
                return null;
            }

            @Override
            public List<String> filter(String name) {
                return List.of();
            }
        }

        var repo = new HybridRepository<>(new KIRunFunctionRepository(), new InternalRepository());

        var results = first
                .execute(new FunctionExecutionParameters(repo, new KIRunSchemaRepository()).setArguments(Map.of()));

        var emptyArray = new ArrayList<>();
        assertEquals(emptyArray, results.allResults());
    }

}
