package com.fincity.nocode.kirun.engine.function.math;

import static com.fincity.nocode.kirun.engine.function.math.Abs.VALUE;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.runtime.FunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class AbsTest {
	
	@Test
	void testExecute() {

		var abs = new Abs();

		StepVerifier.create(abs.execute(new FunctionExecutionParameters().setArguments(Map.of(VALUE, (new JsonPrimitive(-10))))))
		        .expectNext(EventResult.outputOf(Map.of(VALUE, new JsonPrimitive(10))))
		        .expectComplete()
		        .verify();

		StepVerifier.create(abs.execute(new FunctionExecutionParameters().setArguments(Map.of(VALUE, (new JsonPrimitive(-10.099))))))
		        .expectNext(EventResult.outputOf(Map.of(VALUE, new JsonPrimitive(10.099))))
		        .expectComplete()
		        .verify();

		StepVerifier.create(abs.execute(new FunctionExecutionParameters().setArguments(Map.of(VALUE, (new JsonPrimitive(10l))))))
		        .expectNext(EventResult.outputOf(Map.of(VALUE, new JsonPrimitive(10l))))
		        .expectComplete()
		        .verify();

		StepVerifier.create(abs.execute(new FunctionExecutionParameters().setArguments(Map.of(VALUE, (new JsonPrimitive(-101.9999d))))))
		        .expectNext(EventResult.outputOf(Map.of(VALUE, new JsonPrimitive(101.9999d))))
		        .expectComplete()
		        .verify();
	}

}
