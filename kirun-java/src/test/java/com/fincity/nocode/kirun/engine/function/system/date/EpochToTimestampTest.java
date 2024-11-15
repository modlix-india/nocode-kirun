package com.fincity.nocode.kirun.engine.function.system.date;

import java.util.Map;
import java.util.TimeZone;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class EpochToTimestampTest {

        @BeforeAll
        public static void setup() {
                TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        }

        @Test
        void testEpochToTimestampSeconds() {
                EpochToTimestamp epochToTimestamp = new EpochToTimestamp("EpochSecondsToTimestamp", true);
                ReactiveFunctionExecutionParameters parameters = new ReactiveFunctionExecutionParameters(
                                new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
                                .setArguments(
                                                Map.of("epochSeconds",
                                                                new JsonPrimitive(1715529300)));

                StepVerifier.create(epochToTimestamp.execute(parameters)
                                .map(functionOutput -> functionOutput.allResults().get(0).getResult()
                                                .get(EpochToTimestamp.EVENT_TIMESTAMP_NAME).getAsString()))
                                .expectNext("2024-05-12T15:55:00.000Z")
                                .verifyComplete();
        }

        @Test
        void testEpochToTimestampMilliseconds() {
                EpochToTimestamp epochToTimestamp = new EpochToTimestamp("EpochMillisecondsToTimestamp", false);
                ReactiveFunctionExecutionParameters parameters = new ReactiveFunctionExecutionParameters(
                                new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
                                .setArguments(
                                                Map.of("epochMilliseconds",
                                                                new JsonPrimitive(1715529300234L)));

                StepVerifier.create(epochToTimestamp.execute(parameters)
                                .map(functionOutput -> functionOutput.allResults().get(0).getResult()
                                                .get(EpochToTimestamp.EVENT_TIMESTAMP_NAME).getAsString()))
                                .expectNext("2024-05-12T15:55:00.234Z")
                                .verifyComplete();
        }
}
