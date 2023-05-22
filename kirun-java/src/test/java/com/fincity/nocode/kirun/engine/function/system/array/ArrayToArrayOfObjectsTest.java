package com.fincity.nocode.kirun.engine.function.system.array;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.repository.KIRunFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.KIRunSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.FunctionExecutionParameters;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

class ArrayToArrayOfObjectsTest {

    @Test
    void arrayToArrayOfObjectTest() {

        JsonArray arr = new JsonArray();
        arr.add(true);
        arr.add(1);
        arr.add(2);

        ArrayToArrayOfObjects func = new ArrayToArrayOfObjects();

        FunctionExecutionParameters fep = new FunctionExecutionParameters(new KIRunFunctionRepository(),
                new KIRunSchemaRepository());

        var result = func.execute(fep.setArguments(Map.of("source", arr))).allResults().get(0).getResult()
                .get("output");

        JsonArray resArr = new JsonArray();
        for (int i = 0; i < arr.size(); i++) {
            JsonObject job = new JsonObject();
            job.add("value", arr.get(i));
            resArr.add(job);
        }

        assertEquals(resArr, result);
    }

    @Test
    void arrayToArrayOfObjectWithKeyTest() {

        JsonArray arr = new JsonArray();
        arr.add(true);
        arr.add(1);
        arr.add(2);

        JsonArray keyArr = new JsonArray();
        keyArr.add("This is a key");

        ArrayToArrayOfObjects func = new ArrayToArrayOfObjects();

        FunctionExecutionParameters fep = new FunctionExecutionParameters(new KIRunFunctionRepository(),
                new KIRunSchemaRepository());

        var result = func.execute(fep.setArguments(Map.of("source", arr, "keyName", keyArr.get(0)))).allResults().get(0)
                .getResult()
                .get("output");

        JsonArray resArr = new JsonArray();
        for (int i = 0; i < arr.size(); i++) {
            JsonObject job = new JsonObject();
            job.add(keyArr.get(0).getAsString(), arr.get(i));
            resArr.add(job);
        }

        assertEquals(resArr, result);
    }

    @Test
    void arrayToArrayWithObjectsWithKeyArrayTest() {

        JsonArray arr = new JsonArray();
        arr.add(true);
        arr.add(1);
        arr.add(2);

        for (int i = 0; i < arr.size(); i++) {
            JsonElement elem = arr.remove(0);
            JsonObject job = new JsonObject();
            job.add("source", elem);
            arr.add(job);
        }

        JsonArray keyArr = new JsonArray();
        keyArr.add("This is a key");

        ArrayToArrayOfObjects func = new ArrayToArrayOfObjects();

        FunctionExecutionParameters fep = new FunctionExecutionParameters(new KIRunFunctionRepository(),
                new KIRunSchemaRepository());

        var result = func.execute(fep.setArguments(Map.of("source", arr, "keyName", keyArr.get(0)))).allResults().get(0)
                .getResult()
                .get("output");

        JsonArray resArr = new JsonArray();
        for (int i = 0; i < arr.size(); i++) {
            JsonObject job = new JsonObject();
            job.add(keyArr.get(0).getAsString(), arr.get(i));
            resArr.add(job);
        }
        assertEquals(resArr, result);
    }

    @Test
    void arrayToArrayWithObjectsWithoutKeyTest() {

        JsonArray arr = new JsonArray();
        arr.add(true);
        arr.add(1);
        arr.add(2);

        for (int i = 0; i < arr.size(); i++) {
            JsonElement elem = arr.remove(0);
            JsonObject job = new JsonObject();
            job.add("source", elem);
            arr.add(job);
        }

        ArrayToArrayOfObjects func = new ArrayToArrayOfObjects();

        FunctionExecutionParameters fep = new FunctionExecutionParameters(new KIRunFunctionRepository(),
                new KIRunSchemaRepository());

        var result = func.execute(fep.setArguments(Map.of("source", arr))).allResults().get(0)
                .getResult()
                .get("output");

        JsonArray resArr = new JsonArray();
        for (int i = 0; i < arr.size(); i++) {
            JsonObject job = new JsonObject();
            job.add("value", arr.get(i));
            resArr.add(job);
        }
        assertEquals(resArr, result);
    }

    @Test
    void arrayOfArraysToArrayOfObjectTest() {

        JsonArray arr = new JsonArray();
        JsonArray inArr = new JsonArray();
        inArr.add(true);
        inArr.add(false);
        JsonArray inArr1 = new JsonArray();
        inArr1.add(1);
        inArr1.add("surendhar");
        JsonArray inArr2 = new JsonArray();
        inArr2.add("satya");
        arr.add(inArr);
        arr.add(inArr1);
        arr.add(inArr2);

        ArrayToArrayOfObjects func = new ArrayToArrayOfObjects();

        FunctionExecutionParameters fep = new FunctionExecutionParameters(new KIRunFunctionRepository(),
                new KIRunSchemaRepository());

        JsonArray resArr = new JsonArray();
        for (int i = 0; i < arr.size(); i++) {
            JsonObject job = new JsonObject();
            JsonArray innerArr = arr.get(i).getAsJsonArray();
            for (int j = 0; j < innerArr.size(); j++) {
                job.add("value" + (j + 1), innerArr.get(j));
            }
            resArr.add(job);
        }

        var result = func.execute(fep.setArguments(Map.of("source", arr))).allResults().get(0).getResult()
                .get("output");

        assertEquals(resArr, result);
    }

    @Test
    void arrayOfArraysToArrayOfObjectWithKeyNameArrayTest() {

        JsonArray arr = new JsonArray();
        JsonArray inArr = new JsonArray();
        inArr.add(true);
        inArr.add(false);
        JsonArray inArr1 = new JsonArray();
        inArr1.add(1);
        inArr1.add("surendhar");
        JsonArray inArr2 = new JsonArray();
        inArr2.add("satya");
        arr.add(inArr);
        arr.add(inArr1);
        arr.add(inArr2);

        ArrayToArrayOfObjects func = new ArrayToArrayOfObjects();

        FunctionExecutionParameters fep = new FunctionExecutionParameters(new KIRunFunctionRepository(),
                new KIRunSchemaRepository());

        JsonArray resArr = new JsonArray();

        JsonArray keys = new JsonArray();
        keys.add("valueA");
        keys.add("valueB");
        keys.add("valueC");

        for (int i = 0; i < arr.size(); i++) {
            JsonArray innerArr = arr.get(i).getAsJsonArray();
            JsonObject job = new JsonObject();
            for (int j = 0; j < innerArr.size() && j < keys.size(); j++) {
                job.add(keys.get(j).getAsString(), innerArr.get(j));
            }
            resArr.add(job);
        }

        JsonArray finArr = new JsonArray();

        for (int i = 0; i < arr.size(); i++) {
            JsonObject job = new JsonObject();

            for (int j = 0; j < keys.size() && j < arr.get(i).getAsJsonArray().size(); j++) {
                job.add(keys.get(j).getAsString(), arr.get(i).getAsJsonArray().get(j));
            }
            finArr.add(job);

        }

        var result = func.execute(fep.setArguments(Map.of("source", arr, ArrayToArrayOfObjects.KEY_NAME, keys)))
                .allResults().get(0)
                .getResult()
                .get("output");
        

        assertEquals(finArr, result);
    }

}
