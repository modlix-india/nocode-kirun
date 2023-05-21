package com.fincity.nocode.kirun.engine.function.system.object;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.repository.KIRunFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.KIRunSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.FunctionExecutionParameters;
import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

class ObjectEntriesTest {

    @Test
    void checkNullObject() {

        JsonNull job = JsonNull.INSTANCE;
        ObjectEntries oe = new ObjectEntries();

        JsonArray res = new JsonArray();

        FunctionExecutionParameters fep = new FunctionExecutionParameters(new KIRunFunctionRepository(),
                new KIRunSchemaRepository())
                .setArguments(Map.of("source", job)).setContext(Map.of())
                .setSteps(Map.of());

        assertEquals(res, oe.execute(fep).allResults().get(0).getResult().get("value"));
    }

    @Test
    void checkFirstObjectTest() {

        JsonArray arr = new JsonArray();
        arr.add(false);
        arr.add(true);
        JsonObject job = new JsonObject();
        job.addProperty("a", 1);
        job.addProperty("b", 2);
        job.add("arr", arr);

        ObjectEntries oe = new ObjectEntries();

        JsonArray res = new JsonArray();

        JsonArray arr1 = new JsonArray();
        arr1.add("a");
        arr1.add(1);
        res.add(arr1);
        
        JsonArray arr3 = new JsonArray();
        arr3.add("arr");
        arr3.add(arr);
        res.add(arr3);
        
        JsonArray arr2 = new JsonArray();
        arr2.add("b");
        arr2.add(2);
        res.add(arr2);

        FunctionExecutionParameters fep = new FunctionExecutionParameters(new KIRunFunctionRepository(),
                new KIRunSchemaRepository())
                .setArguments(Map.of("source", job)).setContext(Map.of())
                .setSteps(Map.of());

        assertEquals(res, oe.execute(fep).allResults().get(0).getResult().get("value"));
    }

    @Test
    void checkPrimitiveNumberTest() {

        JsonPrimitive prim = new JsonPrimitive(1231243);

        ObjectEntries oe = new ObjectEntries();

        JsonArray res = new JsonArray();

        FunctionExecutionParameters fep = new FunctionExecutionParameters(new KIRunFunctionRepository(),
                new KIRunSchemaRepository())
                .setArguments(Map.of("source", prim)).setContext(Map.of())
                .setSteps(Map.of());

        assertEquals(res, oe.execute(fep).allResults().get(0).getResult().get("value"));
    }

    @Test
    void checkPrimitiveBooleanTest() {

        JsonPrimitive prim = new JsonPrimitive(false);

        ObjectEntries oe = new ObjectEntries();

        JsonArray res = new JsonArray();

        FunctionExecutionParameters fep = new FunctionExecutionParameters(new KIRunFunctionRepository(),
                new KIRunSchemaRepository())
                .setArguments(Map.of("source", prim)).setContext(Map.of())
                .setSteps(Map.of());

        assertEquals(res, oe.execute(fep).allResults().get(0).getResult().get("value"));
    }

    @Test
    void checkPrimitiveStringTest() {

        JsonPrimitive prim = new JsonPrimitive("ab c d e");

        ObjectEntries oe = new ObjectEntries();

        JsonArray res = new JsonArray();

        String[] splitted = prim.getAsString().split("");
        for (int i = 0; i < splitted.length; i++) {
            JsonArray temp = new JsonArray();
            temp.add(String.valueOf(i));
            temp.add(splitted[i]);
            res.add(temp);

        }

        FunctionExecutionParameters fep = new FunctionExecutionParameters(new KIRunFunctionRepository(),
                new KIRunSchemaRepository())
                .setArguments(Map.of("source", prim)).setContext(Map.of())
                .setSteps(Map.of());

        assertEquals(res, oe.execute(fep).allResults().get(0).getResult().get("value"));
    }

    @Test
    void checkArrayTest() {

        JsonArray arr = new JsonArray();
        arr.add(false);
        arr.add(1);
        arr.add("this is a string");
        JsonObject tempObj = new JsonObject();
        tempObj.add("kjk", new JsonPrimitive("this is an object"));
        arr.add(tempObj);

        ObjectEntries oe = new ObjectEntries();

        JsonArray res = new JsonArray();

        JsonArray arr1 = new JsonArray();

        arr1.add("0");
        arr1.add(false);
        res.add(arr1);

        JsonArray arr2 = new JsonArray();

        arr2.add("1");
        arr2.add(1);
        res.add(arr2);
        JsonArray arr3 = new JsonArray();
        arr3.add("2");
        arr3.add("this is a string");

        res.add(arr3);
        JsonArray arr4 = new JsonArray();

        arr4.add("3");
        arr4.add(tempObj);
        res.add(arr4);

        FunctionExecutionParameters fep = new FunctionExecutionParameters(new KIRunFunctionRepository(),
                new KIRunSchemaRepository())
                .setArguments(Map.of("source", arr)).setContext(Map.of())
                .setSteps(Map.of());

        assertEquals(res, oe.execute(fep).allResults().get(0).getResult().get("value"));
    }

    @Test
    void checkNestedObjectTest() {

        JsonArray arr = new JsonArray();
        arr.add(false);
        arr.add(true);
        
        JsonObject tempObj1 = new JsonObject();
        tempObj1.add("prop1", new JsonPrimitive(false));
        tempObj1.add("a", new JsonPrimitive("1"));
        tempObj1.add("b", new JsonPrimitive("2"));
        tempObj1.add("a", new JsonPrimitive("final value of a"));

        JsonObject tempObj2 = new JsonObject();
        tempObj2.add("tempObj1", tempObj1);
        tempObj2.add("ad", new JsonPrimitive("ad"));
        arr.add(tempObj2);

        JsonObject job = new JsonObject();
        job.addProperty("a", 1);
        job.addProperty("b", 2);
        job.add("arr", arr);
        job.add("prop2", tempObj1);

        ObjectEntries oe = new ObjectEntries();

        JsonArray res = new JsonArray();

        JsonArray arr1 = new JsonArray();

        arr1.add("a");
        arr1.add(1);
        res.add(arr1);

        JsonArray arr3 = new JsonArray();
        arr3.add("arr");
        arr3.add(arr);

        res.add(arr3);
        
        JsonArray arr2 = new JsonArray();

        arr2.add("b");
        arr2.add(2);
        res.add(arr2);
        
        JsonArray arr4 = new JsonArray();

        arr4.add("prop2");
        arr4.add(tempObj1);
        res.add(arr4);

        FunctionExecutionParameters fep = new FunctionExecutionParameters(new KIRunFunctionRepository(),
                new KIRunSchemaRepository())
                .setArguments(Map.of("source", job)).setContext(Map.of())
                .setSteps(Map.of());

        assertEquals(res, oe.execute(fep).allResults().get(0).getResult().get("value"));
    }
}