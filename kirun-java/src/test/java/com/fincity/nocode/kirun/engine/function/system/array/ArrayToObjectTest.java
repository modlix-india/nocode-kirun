package com.fincity.nocode.kirun.engine.function.system.array;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class ArrayToObjectTest {

    ArrayToObject ato = new ArrayToObject();
    Gson gson = new Gson();

    @Test
    void simpleArrayOfObjectstest() {

        JsonArray src = gson.fromJson(
                """
                        [{"name":"A","num":1},{"name":"B","num":2},null,{"name":"C","num":3},{"name":"D","num":4},{"name":"E","num":4},null]
                        """,
                JsonArray.class);

        ReactiveFunctionExecutionParameters rfep = new ReactiveFunctionExecutionParameters(
                new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
                .setArguments(Map.of(

                        ArrayToObject.PARAMETER_ARRAY_SOURCE.getParameterName(), src,
                        ArrayToObject.KEY_PATH, new JsonPrimitive("name"),
                        ArrayToObject.VALUE_PATH, new JsonPrimitive("num")

                ))
                .setContext(Map.of())
                .setEvents(Map.of());

        JsonObject res = gson.fromJson("""
                {"A":1,"B":2,"C":3,"D":4,"E":4}
                """, JsonObject.class);

        StepVerifier.create(ato.execute(rfep))
                .expectNextMatches(r -> {
                    return r.next().getResult().get("result").equals(res);
                }).verifyComplete();

        rfep.setArguments(
                Map.of(

                        ArrayToObject.PARAMETER_ARRAY_SOURCE.getParameterName(), src,
                        ArrayToObject.KEY_PATH, new JsonPrimitive("num"),
                        ArrayToObject.VALUE_PATH, new JsonPrimitive("name")

                ));

        JsonObject res2 = gson.fromJson("""
                {"1":"A","2":"B","3":"C","4":"E"}
                """, JsonObject.class);

        StepVerifier.create(ato.execute(rfep))
                .expectNextMatches(r -> {
                    return r.next().getResult().get("result").equals(res2);
                }).verifyComplete();

        rfep.setArguments(

                Map.of(

                        ArrayToObject.PARAMETER_ARRAY_SOURCE.getParameterName(), src,
                        ArrayToObject.KEY_PATH, new JsonPrimitive("num"),
                        ArrayToObject.VALUE_PATH, new JsonPrimitive("name"),
                        ArrayToObject.IGNORE_DUPLICATE_KEYS, new JsonPrimitive(true)

                ));

        JsonObject res3 = gson.fromJson("""
                {"1":"A","2":"B","3":"C","4":"D"}
                """, JsonObject.class);

        StepVerifier.create(ato.execute(rfep))
                .expectNextMatches(r -> {
                    return r.next().getResult().get("result").equals(res3);
                }).verifyComplete();

    }

    @Test
    void invalidKeyorValuePathTest() {

        JsonArray src = gson.fromJson(
                """
                        [{"name":"A","num":1},{"name":"B","num":2},null,{"name":"C","num":3},{"name":"D","num":4},{"name":"E","num":4},null]
                        """,
                JsonArray.class);

        ReactiveFunctionExecutionParameters rfep = new ReactiveFunctionExecutionParameters(
                new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
                .setArguments(Map.of(

                        ArrayToObject.PARAMETER_ARRAY_SOURCE.getParameterName(), src,
                        ArrayToObject.KEY_PATH, new JsonPrimitive("na"),
                        ArrayToObject.VALUE_PATH, new JsonPrimitive("num")

                ))
                .setContext(Map.of())
                .setEvents(Map.of());

        JsonObject jo = new JsonObject();

        StepVerifier.create(ato.execute(rfep)).expectNextMatches(r -> r.next().getResult().get("result").equals(jo))
                .verifyComplete();

        rfep.setArguments(
                Map.of(

                        ArrayToObject.PARAMETER_ARRAY_SOURCE.getParameterName(), src,
                        ArrayToObject.KEY_PATH, new JsonPrimitive("name"),
                        ArrayToObject.VALUE_PATH, new JsonPrimitive("num1")

                ));

        JsonObject job = new JsonObject();

        job.add("A", JsonNull.INSTANCE);
        job.add("B", JsonNull.INSTANCE);
        job.add("C", JsonNull.INSTANCE);
        job.add("D", JsonNull.INSTANCE);
        job.add("E", JsonNull.INSTANCE);

        StepVerifier.create(ato.execute(rfep)).expectNextMatches(r -> r.next().getResult().get("result").equals(job))
                .verifyComplete();

        rfep.setArguments(
                Map.of(

                        ArrayToObject.PARAMETER_ARRAY_SOURCE.getParameterName(), src,
                        ArrayToObject.KEY_PATH, new JsonPrimitive("name"),
                        ArrayToObject.VALUE_PATH, new JsonPrimitive("num1"),
                        ArrayToObject.IGNORE_NULL_VALUES, new JsonPrimitive(true)

                ));

        StepVerifier.create(ato.execute(rfep)).expectNextMatches(r -> {

            return r.next().getResult().get("result").equals(jo);
        })
                .verifyComplete();

    }

    @Test
    void arrayDeepTest() {

        JsonArray src = gson.fromJson(
                """
                        [{"name":"A","num":1,"info":{"age":10}},{"name":"B","num":2,"info":{"age":20}},{"name":"C","num":3,"info":{"age":30}},{"name":"D","num":4,"info":{"age":40}},{"name":"E","num":4,"info":{"age":50}}]
                          """,
                JsonArray.class);

        ReactiveFunctionExecutionParameters rfep = new ReactiveFunctionExecutionParameters(
                new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
                .setArguments(Map.of(

                        ArrayToObject.PARAMETER_ARRAY_SOURCE.getParameterName(), src,
                        ArrayToObject.KEY_PATH, new JsonPrimitive("info.age"),
                        ArrayToObject.VALUE_PATH, new JsonPrimitive("name")

                ))
                .setContext(Map.of())
                .setEvents(Map.of());

        JsonObject job = gson.fromJson("""
                {"10":"A","20":"B","30":"C","40":"D","50":"E"}
                """, JsonObject.class);

        StepVerifier.create(ato.execute(rfep)).expectNextMatches(
                r -> {
                    return r.next().getResult().get("result").equals(job);
                }).verifyComplete();

        rfep.setArguments(
                Map.of(

                        ArrayToObject.PARAMETER_ARRAY_SOURCE.getParameterName(), src,
                        ArrayToObject.KEY_PATH, new JsonPrimitive("info.age"),
                        ArrayToObject.VALUE_PATH, new JsonPrimitive("num")

                ));

        JsonObject job2 = gson.fromJson("""
                {"10":1,"20":2,"30":3,"40":4,"50":4}
                """, JsonObject.class);

        StepVerifier.create(ato.execute(rfep)).expectNextMatches(
                r -> {
                    return r.next().getResult().get("result").equals(job2);
                }).verifyComplete();

    }

}
