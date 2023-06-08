package com.fincity.nocode.kirun.engine.function.math;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.exception.KIRuntimeException;
import com.fincity.nocode.kirun.engine.function.system.math.MathFunctionRepository;
import com.fincity.nocode.kirun.engine.namespaces.Namespaces;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class Log10Test {

	@Test
	void test() {
		var logFunction = new MathFunctionRepository().find(Namespaces.MATH, "Log10");

		StepVerifier
		        .create(logFunction
		                .flatMap(log -> log
		                        .execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
		                                new KIRunReactiveSchemaRepository())
		                                .setArguments(Map.of("value", new JsonPrimitive(144)))))
		                .map(fo -> fo.next()
		                        .getResult()
		                        .get("value")))
		        .expectNext(new JsonPrimitive(2.1583624920952498))
		        .verifyComplete();

		StepVerifier
		        .create(logFunction
		                .flatMap(log -> log
		                        .execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
		                                new KIRunReactiveSchemaRepository())
		                                .setArguments(Map.of("value", new JsonPrimitive(2)))))
		                .map(fo -> fo.next()
		                        .getResult()
		                        .get("value")))
		        .expectNext(new JsonPrimitive(0.3010299956639812))
		        .verifyComplete();

		StepVerifier
		        .create(logFunction
		                .flatMap(log -> log
		                        .execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
		                                new KIRunReactiveSchemaRepository())
		                                .setArguments(Map.of("value", new JsonPrimitive(1)))))
		                .map(fo -> fo.next()
		                        .getResult()
		                        .get("value")))
		        .expectNext(new JsonPrimitive(0))
		        .verifyComplete();
	}

	@Test
	void test3() {
		var logFunction = new MathFunctionRepository().find(Namespaces.MATH, "Log10");

		var num = Double.POSITIVE_INFINITY - Double.POSITIVE_INFINITY;

		StepVerifier
		        .create(logFunction
		                .flatMap(log -> log
		                        .execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
		                                new KIRunReactiveSchemaRepository())
		                                .setArguments(Map.of("value", new JsonPrimitive(-123)))))
		                .map(fo -> fo.next()
		                        .getResult()
		                        .get("value")))
		        .expectNext(new JsonPrimitive(num))
		        .verifyComplete();

		StepVerifier
		        .create(logFunction
		                .flatMap(log -> log
		                        .execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
		                                new KIRunReactiveSchemaRepository())
		                                .setArguments(Map.of("value", new JsonPrimitive(num)))))
		                .map(fo -> fo.next()
		                        .getResult()
		                        .get("value")))
		        .expectNext(new JsonPrimitive(num))
		        .verifyComplete();
		StepVerifier
		        .create(logFunction
		                .flatMap(log -> log
		                        .execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
		                                new KIRunReactiveSchemaRepository())
		                                .setArguments(Map.of("value", new JsonPrimitive(Double.POSITIVE_INFINITY)))))
		                .map(fo -> fo.next()
		                        .getResult()
		                        .get("value")))
		        .expectNext(new JsonPrimitive(Double.POSITIVE_INFINITY))
		        .verifyComplete();

		StepVerifier
		        .create(logFunction
		                .flatMap(log -> log
		                        .execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
		                                new KIRunReactiveSchemaRepository())
		                                .setArguments(Map.of("value", new JsonPrimitive(-0.0)))))
		                .map(fo -> fo.next()
		                        .getResult()
		                        .get("value")))
		        .expectNext(new JsonPrimitive(Double.NEGATIVE_INFINITY))
		        .verifyComplete();

		StepVerifier
		        .create(logFunction.flatMap(log -> log
		                .execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
		                        new KIRunReactiveSchemaRepository())
		                        .setArguments(Map.of("value", new JsonPrimitive(""))))))

		        .expectError(KIRuntimeException.class)
		        .verify();

		StepVerifier
		        .create(logFunction
		                .flatMap(log -> log
		                        .execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
		                                new KIRunReactiveSchemaRepository())
		                                .setArguments(Map.of("value", new JsonPrimitive(num)))))
		                .map(fo -> fo.next()
		                        .getResult()
		                        .get("value")))
		        .expectNext(new JsonPrimitive(num))
		        .verifyComplete();

	}

}
