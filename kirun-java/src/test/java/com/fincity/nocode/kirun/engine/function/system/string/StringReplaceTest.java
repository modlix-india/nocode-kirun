package com.fincity.nocode.kirun.engine.function.system.string;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.namespaces.Namespaces;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class StringReplaceTest {

    @Test
    void test5ForSpace() {

        StringFunctionRepository stringFunction = new StringFunctionRepository();

        String s1 = " THIS IS A NOcoDE plATFNORM";

        StepVerifier.create(stringFunction.find(Namespaces.STRING, "Replace")
                .flatMap(sf -> sf.execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
                        new KIRunReactiveSchemaRepository()).setArguments(
                                Map.of(
                                        AbstractTertiaryStringFunction.PARAMETER_STRING_NAME, new JsonPrimitive(s1),
                                        AbstractTertiaryStringFunction.PARAMETER_SECOND_STRING_NAME,
                                        new JsonPrimitive(" "),
                                        AbstractTertiaryStringFunction.PARAMETER_THIRD_STRING_NAME,
                                        new JsonPrimitive(""))))
                        .map(r -> r.allResults().get(0).getResult()
                                .get(AbstractTertiaryStringFunction.EVENT_RESULT_NAME))))
                .expectNext(new JsonPrimitive("THISISANOcoDEplATFNORM"))
                .verifyComplete();

    }

}
