package com.fincity.nocode.kirun.engine.runtime;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.function.reactive.ReactiveFunction;
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

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class KIRuntimeNoParameterMapTest {

    @Test
    void test() {

        Gson gson = new GsonBuilder().registerTypeAdapter(Type.class, new SchemaTypeAdapter())
                .create();

        var def = gson.fromJson(
                """

                                        {
                        "name": "Make an error",
                        "namespace": "UIApp",
                        "steps": {
                        "print": {
                        "statementName": "print",
                        "namespace": "function",
                        "name": "test"

                        }
                        }
                        }""", FunctionDefinition.class);

        var first = new ReactiveKIRuntime(def);

        Print printMethod = new Print();

        class InternalRepository implements ReactiveRepository<ReactiveFunction> {

            @Override
            public Mono<ReactiveFunction> find(String namespace, String name) {

                if ("function".equals(namespace))
                    return Mono.just(printMethod);

                return Mono.empty();
            }

            @Override
            public Flux<String> filter(String name) {
                return Flux.empty();
            }
        }

        var repo = new ReactiveHybridRepository<>(new KIRunReactiveFunctionRepository(), new InternalRepository());

        var output = first
                .execute(new ReactiveFunctionExecutionParameters(repo, new KIRunReactiveSchemaRepository())
                        .setArguments(Map.of()))
                .map(fo -> fo.allResults());

        StepVerifier
                .create(output)
                .expectNext(List.of())
                .verifyComplete();
    }

}
