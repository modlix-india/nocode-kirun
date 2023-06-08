package com.fincity.nocode.kirun.engine.function.system.object;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class ObjectValuesTest {

    @Test
    void checkNullObject() {

        JsonNull job = JsonNull.INSTANCE;
        ObjectValues values = new ObjectValues();

        JsonArray res = new JsonArray();

        ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
                new KIRunReactiveFunctionRepository(),
                new KIRunReactiveSchemaRepository())
                .setArguments(Map.of("source", job)).setContext(Map.of())
                .setSteps(Map.of());

        StepVerifier.create(values.execute(fep).map(e -> e.next().getResult().get("value"))).expectNext(res)
                .verifyComplete();
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

        ObjectValues values = new ObjectValues();

        JsonArray res = new JsonArray();
        res.add(1);
        res.add(arr);
        res.add(2);

        ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
                new KIRunReactiveFunctionRepository(),
                new KIRunReactiveSchemaRepository())
                .setArguments(Map.of("source", job)).setContext(Map.of())
                .setSteps(Map.of());

        StepVerifier.create(values.execute(fep).map(e -> e.next().getResult().get("value"))).expectNext(res)
                .verifyComplete();
    }

    @Test
    void checkPrimitiveNumberTest() {

        JsonPrimitive prim = new JsonPrimitive(1231243);

        ObjectValues values = new ObjectValues();

        JsonArray res = new JsonArray();

        ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
                new KIRunReactiveFunctionRepository(),
                new KIRunReactiveSchemaRepository())
                .setArguments(Map.of("source", prim)).setContext(Map.of())
                .setSteps(Map.of());

        StepVerifier.create(values.execute(fep).map(e -> e.next().getResult().get("value"))).expectNext(res)
                .verifyComplete();
    }

    @Test
    void checkPrimitiveBooleanTest() {

        JsonPrimitive prim = new JsonPrimitive(false);

        ObjectValues values = new ObjectValues();

        JsonArray res = new JsonArray();

        ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
                new KIRunReactiveFunctionRepository(),
                new KIRunReactiveSchemaRepository())
                .setArguments(Map.of("source", prim)).setContext(Map.of())
                .setSteps(Map.of());

        StepVerifier.create(values.execute(fep).map(e -> e.next().getResult().get("value"))).expectNext(res)
                .verifyComplete();
    }

    @Test
    void checkPrimitiveStringTest() {

        JsonPrimitive prim = new JsonPrimitive("ab c d e");

        ObjectValues values = new ObjectValues();

        JsonArray res = new JsonArray();

        String[] splitted = prim.getAsString().split("");
        for (int i = 0; i < splitted.length; i++)
            res.add(splitted[i]);

        ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
                new KIRunReactiveFunctionRepository(),
                new KIRunReactiveSchemaRepository())
                .setArguments(Map.of("source", prim)).setContext(Map.of())
                .setSteps(Map.of());

        StepVerifier.create(values.execute(fep).map(e -> e.next().getResult().get("value"))).expectNext(res)
                .verifyComplete();
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

        ObjectValues values = new ObjectValues();

        JsonArray res = new JsonArray();

        res.add(false);

        res.add(1);

        res.add("this is a string");

        res.add(tempObj);
        ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
                new KIRunReactiveFunctionRepository(),
                new KIRunReactiveSchemaRepository())
                .setArguments(Map.of("source", arr)).setContext(Map.of())
                .setSteps(Map.of());

        StepVerifier.create(values.execute(fep).map(e -> e.next().getResult().get("value"))).expectNext(res)
                .verifyComplete();
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

        ObjectValues values = new ObjectValues();

        JsonArray res = new JsonArray();
        res.add(1);
        res.add(arr);
        res.add(2);
        res.add(tempObj1);

        ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
                new KIRunReactiveFunctionRepository(),
                new KIRunReactiveSchemaRepository())
                .setArguments(Map.of("source", job)).setContext(Map.of())
                .setSteps(Map.of());

        StepVerifier.create(values.execute(fep).map(e -> e.next().getResult().get("value"))).expectNext(res)
                .verifyComplete();
    }
}