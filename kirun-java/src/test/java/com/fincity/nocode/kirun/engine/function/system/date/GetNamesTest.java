package com.fincity.nocode.kirun.engine.function.system.date;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class GetNamesTest {

    @Test
    void testMonthNames() {

        ReactiveFunctionExecutionParameters context = new ReactiveFunctionExecutionParameters(
                new KIRunReactiveFunctionRepository(),
                new KIRunReactiveSchemaRepository())
                .setArguments(
                        Map.of(GetNames.PARAMETER_UNIT_NAME, new JsonPrimitive("MONTHS"),
                                GetNames.PARAMETER_LOCALE_NAME, new JsonPrimitive("en")));

        GetNames getNames = new GetNames();

        List<String> months = List.of("January", "February", "March", "April", "May", "June", "July", "August",
                "September", "October", "November", "December");

        StepVerifier
                .create(getNames.execute(context).map(functionOutput -> functionOutput.allResults().get(0).getResult()
                        .get(GetNames.EVENT_NAMES_NAME))
                        .map(jsonElement -> jsonElement.getAsJsonArray().asList().stream()
                                .map(JsonElement::getAsString).collect(Collectors.toList())))
                .expectNext(months)
                .verifyComplete();
    }

    @Test
    void testWeekDays() {

        ReactiveFunctionExecutionParameters context = new ReactiveFunctionExecutionParameters(
                new KIRunReactiveFunctionRepository(),
                new KIRunReactiveSchemaRepository())
                .setArguments(
                        Map.of(GetNames.PARAMETER_UNIT_NAME, new JsonPrimitive("WEEKDAYS"),
                                GetNames.PARAMETER_LOCALE_NAME, new JsonPrimitive("en")));

        GetNames getNames = new GetNames();

        JsonArray days = new JsonArray();
        List.of("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
                .stream().map(JsonPrimitive::new).forEach(days::add);

        StepVerifier
                .create(getNames.execute(context).map(functionOutput -> functionOutput.allResults().get(0).getResult()
                        .get(GetNames.EVENT_NAMES_NAME)))
                .expectNext(days)
                .verifyComplete();
    }

    @Test
    void testTimezones() {

        ReactiveFunctionExecutionParameters context = new ReactiveFunctionExecutionParameters(
                new KIRunReactiveFunctionRepository(),
                new KIRunReactiveSchemaRepository())
                .setArguments(
                        Map.of(GetNames.PARAMETER_UNIT_NAME, new JsonPrimitive("TIMEZONES"),
                                GetNames.PARAMETER_LOCALE_NAME, new JsonPrimitive("en")));

        GetNames getNames = new GetNames();

        StepVerifier
                .create(getNames.execute(context).map(functionOutput -> functionOutput.allResults().get(0).getResult()
                        .get(GetNames.EVENT_NAMES_NAME)))
                .expectNextMatches(e -> e.getAsJsonArray().size() > 100
                        && e.getAsJsonArray().asList().stream()
                                .map(JsonElement::getAsString).anyMatch(x -> x.equals("Kolkata Time") || x.equals("India Time")))
                .verifyComplete();
    }
}
