package com.fincity.nocode.kirun.engine.runtime.expression;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * Ports the trailing-whitespace literal probe from kirun-js
 * (ExpressionDoubleEqualsChipLabelTest). A bare quoted string literal followed by
 * trailing whitespace must still evaluate to the string, not null/undefined.
 */
class ExpressionTrailingWhitespaceTest {

    private ReactiveFunctionExecutionParameters params() {
        JsonObject obj = new JsonObject();
        obj.add("string", new JsonPrimitive("Hello"));

        Map<String, Map<String, Map<String, JsonElement>>> output = Map.of("step1",
                Map.of("output", Map.of("obj", obj)));

        return new ReactiveFunctionExecutionParameters(
                new KIRunReactiveFunctionRepository(),
                new KIRunReactiveSchemaRepository()).setArguments(Map.of())
                .setContext(Map.of())
                .setSteps(output);
    }

    @Test
    void bareStringLiteralWithTrailingSpace() {
        var p = params();
        assertEquals(new JsonPrimitive("Hello"),
                new ExpressionEvaluator("'Hello'").evaluate(p.getValuesMap()));
        assertEquals(new JsonPrimitive("Hello"),
                new ExpressionEvaluator("'Hello' ").evaluate(p.getValuesMap()));
        assertEquals(new JsonPrimitive(""),
                new ExpressionEvaluator("''").evaluate(p.getValuesMap()));
        assertEquals(new JsonPrimitive(""),
                new ExpressionEvaluator("'' ").evaluate(p.getValuesMap()));
    }

    @Test
    void templateExpandingToQuotedLiteralWithTrailingSpaces() {
        var p = params();
        var e = new ExpressionEvaluator("\"api/x/{{Steps.step1.output.obj.string}}\"  ");
        assertEquals(new JsonPrimitive("api/x/Hello"), e.evaluate(p.getValuesMap()));
    }
}
