package com.fincity.nocode.kirun.engine.function.system.string;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class MatchesTest {

    Matches reg = new Matches();

    @Test
    void simpleRegex() {

        String pat = "(\\d{2}).(\\d{2}).(\\d{4})";
        String inp = "21.11.1997";

        StepVerifier.create(reg
                .execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
                        new KIRunReactiveSchemaRepository()).setArguments(
                                Map.of(Matches.PARAMETER_REGEX_NAME, new JsonPrimitive(pat), Matches.PARAMETER_STRING_NAME,
                                        new JsonPrimitive(inp))))
                .map(fo -> fo.allResults().get(0).getResult().get(Matches.EVENT_RESULT_NAME).getAsBoolean()))
                .expectNext(true)
                .verifyComplete();
    }
    
    @Test
    void simpleTestRegex() {

        String inp = "\nThe quick brown fox The what";
        String pat = "[A-Z]he";

        StepVerifier.create(reg
                .execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
                        new KIRunReactiveSchemaRepository()).setArguments(
                                Map.of(Matches.PARAMETER_REGEX_NAME, new JsonPrimitive(pat), Matches.PARAMETER_STRING_NAME,
                                        new JsonPrimitive(inp))))
                .map(fo -> fo.allResults().get(0).getResult().get(Matches.EVENT_RESULT_NAME).getAsBoolean()))
                .expectNext(true)
                .verifyComplete();
    }
    
    @Test
    void simpleFailRegex() {

        String pat = "^(\\d{2}).(\\d{2}).(\\d{4})$";
        String inp = "21.11.1997.";

        StepVerifier.create(reg
                .execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
                        new KIRunReactiveSchemaRepository()).setArguments(
                                Map.of(Matches.PARAMETER_REGEX_NAME, new JsonPrimitive(pat), Matches.PARAMETER_STRING_NAME,
                                        new JsonPrimitive(inp))))
                .map(fo -> fo.allResults().get(0).getResult().get(Matches.EVENT_RESULT_NAME).getAsBoolean()))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void nameRegex() {

        String pat = "(\\w+),\\s(Mr|Ms|Mrs|Dr)\\.\\s?(\\w+)";
        String inp = "smith, Mr.John";

        StepVerifier.create(reg
                .execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
                        new KIRunReactiveSchemaRepository()).setArguments(
                                Map.of(Matches.PARAMETER_REGEX_NAME, new JsonPrimitive(pat), Matches.PARAMETER_STRING_NAME,
                                        new JsonPrimitive(inp))))
                .map(fo -> fo.allResults().get(0).getResult().get(Matches.EVENT_RESULT_NAME).getAsBoolean()))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void timeRegex() {

        String pat = "^([01]?[0-9]|2[0-3]):[0-5][0-9](:[0-5][0-9])?([+-][01][0-9]:[0-5][0-9])?$";
        String inp = "12:34:45";

        StepVerifier.create(reg
                .execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
                        new KIRunReactiveSchemaRepository()).setArguments(
                                Map.of(Matches.PARAMETER_REGEX_NAME, new JsonPrimitive(pat), Matches.PARAMETER_STRING_NAME,
                                        new JsonPrimitive(inp))))
                .map(fo -> {
                    System.out.println(fo.allResults().get(0).getName());
                    return fo.allResults().get(0).getResult().get(Matches.EVENT_RESULT_NAME).getAsBoolean();
                }))
                .expectNext(true)
                .verifyComplete();
    }

}
