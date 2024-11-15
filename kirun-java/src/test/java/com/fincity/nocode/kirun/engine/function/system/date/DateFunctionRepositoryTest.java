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
}
