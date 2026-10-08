package com.fincity.nocode.kirun.engine.function.system.date;

import java.util.Map;
import java.util.TimeZone;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.namespaces.Namespaces;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class DateFunctionRepositoryTest {

    @BeforeAll
    public static void setup() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    void testDateFunctionRepository(String functionName, int value) {
        DateFunctionRepository repository = new DateFunctionRepository();

        ReactiveFunctionExecutionParameters parameters = new ReactiveFunctionExecutionParameters(
                new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
                .setArguments(
                        Map.of(AbstractDateFunction.PARAMETER_TIMESTAMP_NAME, new JsonPrimitive("2024-01-01")));

        StepVerifier.create(
                repository.find(Namespaces.DATE, functionName)
                        .flatMap(func -> func.execute(parameters))
                        .map(functionOutput -> functionOutput.allResults().get(0).getResult()
                                .get(AbstractDateFunction.EVENT_RESULT_NAME).getAsInt()))
                .expectNext(value)
                .verifyComplete();
    }

    @Test
    void testGetDay() {
        testDateFunctionRepository("GetDay", 1);
    }

    @Test
    void testGetDaysInMonth() {
        testDateFunctionRepository("GetDaysInMonth", 31);
    }

    @Test
    void testGetDaysInYear() {
        testDateFunctionRepository("GetDaysInYear", 366);
    }

    @Test
    void testSetDay() {
        DateFunctionRepository repository = new DateFunctionRepository();

        ReactiveFunctionExecutionParameters parameters = new ReactiveFunctionExecutionParameters(
                new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
                .setArguments(
                        Map.of(AbstractDateFunction.PARAMETER_TIMESTAMP_NAME, new JsonPrimitive("2024-01-01"),
                                AbstractDateFunction.PARAMETER_NUMBER_NAME, new JsonPrimitive(2)));

        StepVerifier.create(
                repository.find(Namespaces.DATE, "SetDay")
                        .flatMap(func -> func.execute(parameters))
                        .map(functionOutput -> functionOutput.allResults().get(0).getResult()
                                .get(AbstractDateFunction.EVENT_RESULT_NAME).getAsString()))
                .expectNext("2024-01-02T00:00:00.000Z")
                .verifyComplete();
    }

    // Comparisons, mirrored in kirun-js (DateFunctionRepositoryTest.ts) and kirun-py
    // (test_date_ops.py): IsSame compares the INSTANT, so the same moment written in two
    // offsets is the same, and 1 ms apart is not. kirun-js used to compare luxon objects with
    // ===, which was always false; Java's isEqual was right, these pin it.

    private Boolean compare(String name, String t1, String t2) {
        ReactiveFunctionExecutionParameters parameters = new ReactiveFunctionExecutionParameters(
                new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
                .setArguments(Map.of(
                        AbstractDateFunction.PARAMETER_TIMESTAMP_NAME_ONE, new JsonPrimitive(t1),
                        AbstractDateFunction.PARAMETER_TIMESTAMP_NAME_TWO, new JsonPrimitive(t2)));

        return new DateFunctionRepository().find(Namespaces.DATE, name)
                .flatMap(func -> func.execute(parameters))
                .map(out -> out.allResults().get(0).getResult()
                        .get(AbstractDateFunction.EVENT_RESULT_NAME).getAsBoolean())
                .block();
    }

    @Test
    void testIsSameForIdenticalTimestamps() {
        org.junit.jupiter.api.Assertions.assertTrue(
                compare("IsSame", "2024-01-01T10:00:00.000Z", "2024-01-01T10:00:00.000Z"));
    }

    @Test
    void testIsSameForSameInstantInDifferentOffsets() {
        org.junit.jupiter.api.Assertions.assertTrue(
                compare("IsSame", "2024-01-01T10:00:00.000Z", "2024-01-01T15:30:00.000+05:30"));
    }

    @Test
    void testIsSameIsFalseForDifferentInstants() {
        org.junit.jupiter.api.Assertions.assertFalse(
                compare("IsSame", "2024-01-01T10:00:00.000Z", "2024-01-01T10:00:00.001Z"));
        org.junit.jupiter.api.Assertions.assertFalse(compare("IsSame", "2024-01-01", "2024-01-02"));
    }

    @Test
    void testOrderingComparisonsAgreeWithIsSame() {
        String a = "2024-01-01T10:00:00.000Z";
        String sameAsA = "2024-01-01T15:30:00.000+05:30";
        String b = "2024-01-01T11:00:00.000Z";

        org.junit.jupiter.api.Assertions.assertTrue(compare("IsBefore", a, b));
        org.junit.jupiter.api.Assertions.assertFalse(compare("IsBefore", a, sameAsA));
        org.junit.jupiter.api.Assertions.assertTrue(compare("IsAfter", b, a));
        org.junit.jupiter.api.Assertions.assertFalse(compare("IsAfter", a, sameAsA));
        org.junit.jupiter.api.Assertions.assertTrue(compare("IsSameOrBefore", a, sameAsA));
        org.junit.jupiter.api.Assertions.assertFalse(compare("IsSameOrBefore", b, a));
        org.junit.jupiter.api.Assertions.assertTrue(compare("IsSameOrAfter", a, sameAsA));
        org.junit.jupiter.api.Assertions.assertFalse(compare("IsSameOrAfter", a, b));
    }
}
