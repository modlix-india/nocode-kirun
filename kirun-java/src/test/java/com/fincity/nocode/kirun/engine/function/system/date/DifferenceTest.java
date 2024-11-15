package com.fincity.nocode.kirun.engine.function.system.date;

import java.util.Map;
import java.util.TimeZone;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class DifferenceTest {

    @BeforeAll
    public static void setup() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    @Test
    void testDifference() {

        Difference difference = new Difference();
        ReactiveFunctionExecutionParameters parameters = new ReactiveFunctionExecutionParameters(
                new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository()).setArguments(
                        Map.of(
                                Difference.PARAMETER_TIMESTAMP_NAME_ONE, new JsonPrimitive("2025-01-01"),
                                Difference.PARAMETER_TIMESTAMP_NAME_TWO, new JsonPrimitive("2024-04-25")));

        JsonObject differenceObj = new JsonObject();
        differenceObj.addProperty("milliseconds", 2.16864E10d);

        StepVerifier.create(
                difference.execute(parameters).map(functionOutput -> functionOutput.allResults().get(0).getResult()
                        .get(Difference.EVENT_RESULT_NAME).getAsJsonObject()))
                .expectNext(differenceObj).verifyComplete();
    }

    @Test
    void testDifferenceInDays() {
        Difference difference = new Difference();

        JsonArray units = new JsonArray();
        units.add(new JsonPrimitive("DAYS"));

        ReactiveFunctionExecutionParameters parameters = new ReactiveFunctionExecutionParameters(
                new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository()).setArguments(
                        Map.of(
                                Difference.PARAMETER_TIMESTAMP_NAME_ONE, new JsonPrimitive("2025-01-01T10:20:30+05:30"),
                                Difference.PARAMETER_TIMESTAMP_NAME_TWO, new JsonPrimitive("2024-04-25T10:20:30-05:00"),
                                Difference.PARAMETER_UNIT_NAME, units));

        JsonObject differenceObj = new JsonObject();
        differenceObj.addProperty("days", 250.5625);

        StepVerifier.create(
                difference.execute(parameters).map(functionOutput -> functionOutput.allResults().get(0).getResult()
                        .get(Difference.EVENT_RESULT_NAME).getAsJsonObject()))
                .expectNext(differenceObj).verifyComplete();
    }

    @Test
    void testDifferenceMonthsDaysHoursMinutes() {
        Difference difference = new Difference();

        JsonArray units = new JsonArray();
        units.add(new JsonPrimitive("DAYS"));
        units.add(new JsonPrimitive("HOURS"));
        units.add(new JsonPrimitive("MINUTES"));
        units.add(new JsonPrimitive("MONTHS"));

        ReactiveFunctionExecutionParameters parameters = new ReactiveFunctionExecutionParameters(
                new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository()).setArguments(
                        Map.of(
                                Difference.PARAMETER_TIMESTAMP_NAME_ONE, new JsonPrimitive("2025-01-01T10:20:30+05:30"),
                                Difference.PARAMETER_TIMESTAMP_NAME_TWO, new JsonPrimitive("2024-04-25T10:20:30-05:00"),
                                Difference.PARAMETER_UNIT_NAME, units));

        JsonObject differenceObj = new JsonObject();
        differenceObj.addProperty("months", 8);
        differenceObj.addProperty("days", 10);
        differenceObj.addProperty("hours", 13);
        differenceObj.addProperty("minutes", 30);

        StepVerifier.create(
                difference.execute(parameters).map(functionOutput -> functionOutput.allResults().get(0).getResult()
                        .get(Difference.EVENT_RESULT_NAME).getAsJsonObject()))
                .expectNext(differenceObj).verifyComplete();
    }

    @Test
    void testDifferenceMonthsDaysHoursMinutesInNegative() {
        Difference difference = new Difference();

        JsonArray units = new JsonArray();
        units.add(new JsonPrimitive("DAYS"));
        units.add(new JsonPrimitive("HOURS"));
        units.add(new JsonPrimitive("MINUTES"));
        units.add(new JsonPrimitive("MONTHS"));

        ReactiveFunctionExecutionParameters parameters = new ReactiveFunctionExecutionParameters(
                new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository()).setArguments(
                        Map.of(
                                Difference.PARAMETER_TIMESTAMP_NAME_TWO, new JsonPrimitive("2025-01-01T10:20:30+05:30"),
                                Difference.PARAMETER_TIMESTAMP_NAME_ONE, new JsonPrimitive("2024-04-25T10:20:30-05:00"),
                                Difference.PARAMETER_UNIT_NAME, units));

        JsonObject differenceObj = new JsonObject();
        differenceObj.addProperty("months", -8);
        differenceObj.addProperty("days", -10);
        differenceObj.addProperty("hours", -13);
        differenceObj.addProperty("minutes", -30);

        StepVerifier.create(
                difference.execute(parameters).map(functionOutput -> functionOutput.allResults().get(0).getResult()
                        .get(Difference.EVENT_RESULT_NAME).getAsJsonObject()))
                .expectNext(differenceObj).verifyComplete();
    }

    @Test
    void testDifferenceMonthsDaysHoursMinutesInNegativeOther() {
        Difference difference = new Difference();

        JsonArray units = new JsonArray();
        units.add(new JsonPrimitive("DAYS"));
        units.add(new JsonPrimitive("HOURS"));
        units.add(new JsonPrimitive("MINUTES"));
        units.add(new JsonPrimitive("MONTHS"));

        ReactiveFunctionExecutionParameters parameters = new ReactiveFunctionExecutionParameters(
                new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository()).setArguments(
                        Map.of(
                                Difference.PARAMETER_TIMESTAMP_NAME_TWO, new JsonPrimitive("2025-04-25T10:20:30-05:00"),
                                Difference.PARAMETER_TIMESTAMP_NAME_ONE, new JsonPrimitive("2024-01-01T10:20:30+05:30"),
                                Difference.PARAMETER_UNIT_NAME, units));

        JsonObject differenceObj = new JsonObject();
        differenceObj.addProperty("months", -16);
        differenceObj.addProperty("hours", -10);
        differenceObj.addProperty("minutes", -30);

        StepVerifier.create(
                difference.execute(parameters).map(functionOutput -> functionOutput.allResults().get(0).getResult()
                        .get(Difference.EVENT_RESULT_NAME).getAsJsonObject()))
                .expectNext(differenceObj).verifyComplete();
    }

    @Test
    void testDifferenceInFractions() {
        Difference difference = new Difference();

        JsonArray units = new JsonArray();
        units.add(new JsonPrimitive("DAYS"));

        ReactiveFunctionExecutionParameters parameters = new ReactiveFunctionExecutionParameters(
                new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository()).setArguments(
                        Map.of(
                                Difference.PARAMETER_TIMESTAMP_NAME_TWO, new JsonPrimitive("2025-04-25T10:20:30-05:00"),
                                Difference.PARAMETER_TIMESTAMP_NAME_ONE, new JsonPrimitive("2024-01-01T10:20:30+05:30"),
                                Difference.PARAMETER_UNIT_NAME, units));

        JsonObject differenceObj = new JsonObject();
        differenceObj.addProperty("days", -480.4375);

        StepVerifier.create(
                difference.execute(parameters).map(functionOutput -> functionOutput.allResults().get(0).getResult()
                        .get(Difference.EVENT_RESULT_NAME).getAsJsonObject()))
                .expectNext(differenceObj).verifyComplete();
    }

    @Test
    void testDifferenceInMultipleFractions() {
        Difference difference = new Difference();

        JsonArray units = new JsonArray();
        units.add(new JsonPrimitive("DAYS"));
        units.add(new JsonPrimitive("HOURS"));

        ReactiveFunctionExecutionParameters parameters = new ReactiveFunctionExecutionParameters(
                new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository()).setArguments(
                        Map.of(
                                Difference.PARAMETER_TIMESTAMP_NAME_TWO, new JsonPrimitive("2025-04-25T09:17:23-05:00"),
                                Difference.PARAMETER_TIMESTAMP_NAME_ONE, new JsonPrimitive("2024-01-01T10:20:30+05:30"),
                                Difference.PARAMETER_UNIT_NAME, units));

        JsonObject differenceObj = new JsonObject();
        differenceObj.addProperty("days", -480);
        differenceObj.addProperty("hours", -9.448055555555555);

        StepVerifier.create(
                difference.execute(parameters).map(functionOutput -> functionOutput.allResults().get(0).getResult()
                        .get(Difference.EVENT_RESULT_NAME).getAsJsonObject()))
                .expectNext(differenceObj).verifyComplete();
    }
}
