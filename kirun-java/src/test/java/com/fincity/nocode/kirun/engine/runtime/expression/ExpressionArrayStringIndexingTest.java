package com.fincity.nocode.kirun.engine.runtime.expression;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.runtime.expression.tokenextractor.TokenValueExtractor;
import com.fincity.nocode.kirun.engine.runtime.tokenextractors.ArgumentsTokenValueExtractor;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class ExpressionArrayStringIndexingTest {

    @Test
    public void testExpressionStringIndex() {
        Map<String, JsonElement> arguments = new HashMap<>();
        arguments.put("a", new JsonPrimitive("kirun "));
        arguments.put("b", new JsonPrimitive(2));
        arguments.put("c", new JsonPrimitive("kiran"));
        JsonArray arr = new JsonArray();
        for (int i = 0; i <= 6; i++) {
            arr.add(new JsonPrimitive(i));
        }
        arguments.put("arr", arr);

        ArgumentsTokenValueExtractor atv = new ArgumentsTokenValueExtractor(arguments);
        Map<String, TokenValueExtractor> valuesMap = Map.of(atv.getPrefix(), atv);

        ExpressionEvaluator ev = new ExpressionEvaluator("Arguments.a");
        // assertEquals("kirun ", ev.evaluate(valuesMap).getAsString());

        // ev = new ExpressionEvaluator("Arguments.a[2]");
        // assertEquals("r", ev.evaluate(valuesMap).getAsString());

        // ev = new ExpressionEvaluator("Arguments.a[-2]");
        // assertEquals("n", ev.evaluate(valuesMap).getAsString());

        // ev = new ExpressionEvaluator("Arguments.arr[2..4]");
        // JsonArray expectedArray = new JsonArray();
        // expectedArray.add(new JsonPrimitive(2));
        // expectedArray.add(new JsonPrimitive(3));
        // assertEquals(expectedArray, ev.evaluate(valuesMap).getAsJsonArray());
        // ev = new ExpressionEvaluator("Arguments.a[..]");
        // assertEquals("kirun ", ev.evaluate(valuesMap).getAsString());

        // ev = new ExpressionEvaluator("Arguments.a[2..4]");
        // assertEquals("ru", ev.evaluate(valuesMap).getAsString());

        // ev = new ExpressionEvaluator("Arguments.a[(4-2)..(6-2)]");
        // assertEquals("ru", ev.evaluate(valuesMap).getAsString());

        // ev = new ExpressionEvaluator("Arguments.a[..4]");
        // assertEquals("kiru", ev.evaluate(valuesMap).getAsString());

        ev = new ExpressionEvaluator("Arguments.a[2..]");
        assertEquals("run ", ev.evaluate(valuesMap).getAsString());

        // ev = new ExpressionEvaluator("Arguments.arr[..4]");
        // JsonArray expectedArray2 = new JsonArray();
        // expectedArray2.add(new JsonPrimitive(0));
        // expectedArray2.add(new JsonPrimitive(1));
        // expectedArray2.add(new JsonPrimitive(2));
        // expectedArray2.add(new JsonPrimitive(3));
        // assertEquals(expectedArray2, ev.evaluate(valuesMap).getAsJsonArray());

        // ev = new ExpressionEvaluator("Arguments.arr[(4-2)..7]");
        // JsonArray expectedArray3 = new JsonArray();
        // expectedArray3.add(new JsonPrimitive(2));
        // expectedArray3.add(new JsonPrimitive(3));
        // expectedArray3.add(new JsonPrimitive(4));
        // expectedArray3.add(new JsonPrimitive(5));
        // expectedArray3.add(new JsonPrimitive(6));
        // assertEquals(expectedArray3, ev.evaluate(valuesMap).getAsJsonArray());

        // ev = new ExpressionEvaluator("Arguments.a[..-4]");
        // assertEquals("ki", ev.evaluate(valuesMap).getAsString());

        // ev = new ExpressionEvaluator("Arguments.a[..(-8+4)]");
        // assertEquals("ki", ev.evaluate(valuesMap).getAsString());

        // ev = new ExpressionEvaluator("Arguments.a[-4..-1]");
        // assertEquals("run", ev.evaluate(valuesMap).getAsString());

        // ev = new ExpressionEvaluator("Arguments.a[-4..]");
        // assertEquals("run ", ev.evaluate(valuesMap).getAsString());

        // ev = new ExpressionEvaluator("Arguments.arr[..-1]");
        // JsonArray expectedArray4 = new JsonArray();
        // expectedArray4.add(new JsonPrimitive(0));
        // expectedArray4.add(new JsonPrimitive(1));
        // expectedArray4.add(new JsonPrimitive(2));
        // expectedArray4.add(new JsonPrimitive(3));
        // expectedArray4.add(new JsonPrimitive(4));
        // expectedArray4.add(new JsonPrimitive(5));
        // assertEquals(expectedArray4, ev.evaluate(valuesMap).getAsJsonArray());

        // ev = new ExpressionEvaluator("Arguments.arr[-2..]");
        // JsonArray expectedArray5 = new JsonArray();
        // expectedArray5.add(new JsonPrimitive(5));
        // expectedArray5.add(new JsonPrimitive(6));
        // assertEquals(expectedArray5, ev.evaluate(valuesMap).getAsJsonArray());
    }
}