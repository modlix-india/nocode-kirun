package com.fincity.nocode.kirun.engine.runtime.expression;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

class ExpressionEqualityTest {

    @Test
    void equalityTestsForNullValues() {
        JsonObject obj = new JsonObject();

        obj.add("number", new JsonPrimitive(20));
        obj.add("zero", new JsonPrimitive(0));
        obj.add("booleanTrue", new JsonPrimitive(true));
        obj.add("booleanFalse", new JsonPrimitive(false));
        obj.add("string", new JsonPrimitive("Hello"));
        obj.add("emptyString", new JsonPrimitive(""));
        obj.add("nullValue", JsonNull.INSTANCE);
        obj.add("emptyObject", new JsonObject());
        obj.add("emptyArray", new JsonArray());

        Map<String, Map<String, Map<String, JsonElement>>> output = Map.of("step1",
                Map.of("output", Map.of("obj", obj)));

        ReactiveFunctionExecutionParameters parameters = new ReactiveFunctionExecutionParameters(
                new KIRunReactiveFunctionRepository(),
                new KIRunReactiveSchemaRepository()).setArguments(Map.of())
                .setContext(Map.of())
                .setSteps(output);

        var exp = new ExpressionEvaluator("Steps.step1.output.obj.number = Steps.step1.output.obj.zero");
        var result = exp.evaluate(parameters.getValuesMap());
        assertEquals(new JsonPrimitive(false), result);

        exp = new ExpressionEvaluator("Steps.step1.output.obj.booleanFalse = (not Steps.step1.output.obj.number)");
        result = exp.evaluate(parameters.getValuesMap());
        assertEquals(new JsonPrimitive(true), result);

        exp = new ExpressionEvaluator("Steps.step1.output.obj.booleanFalse = (not Steps.step1.output.obj.emptyString)");
        result = exp.evaluate(parameters.getValuesMap());
        assertEquals(new JsonPrimitive(true), result);

        exp = new ExpressionEvaluator("Steps.step1.output.obj.booleanTrue = (not Steps.step1.output.obj.zero)");
        result = exp.evaluate(parameters.getValuesMap());
        assertEquals(new JsonPrimitive(true), result);

        exp = new ExpressionEvaluator("Steps.step1.output.obj.emptyString = ''");
        result = exp.evaluate(parameters.getValuesMap());
        assertEquals(new JsonPrimitive(true), result);

        exp = new ExpressionEvaluator("Steps.step1.output.obj.emptyString != ''");
        result = exp.evaluate(parameters.getValuesMap());
        assertEquals(new JsonPrimitive(false), result);

        exp = new ExpressionEvaluator("Steps.step1.output.obj.string != ''");
        result = exp.evaluate(parameters.getValuesMap());
        assertEquals(new JsonPrimitive(true), result);

        exp = new ExpressionEvaluator("Steps.step1.output.obj.string = ''");
        result = exp.evaluate(parameters.getValuesMap());
        assertEquals(new JsonPrimitive(false), result);
    }
}
