package com.fincity.nocode.kirun.engine.function.system.math;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.json.schema.type.SchemaType;
import com.fincity.nocode.kirun.engine.namespaces.Namespaces;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class AbstractRandomIntTest {

	@Test
	void test() {

		var min = new JsonPrimitive(1009);

		var max = new JsonPrimitive(1000012);

		AbstractRandom absR = new AbstractRandom("RandomInt", SchemaType.INTEGER);

		ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
				new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
				.setArguments(Map.of("minValue", min, "maxValue", max));

		StepVerifier.create(absR.execute(fep)).expectNextMatches(r -> {
			var x = r.next().getResult().get("value");
			assertTrue(x.getAsInt() >= min.getAsInt() && x.getAsInt() <= max.getAsInt());
			return true;
		}).verifyComplete();
	}

	@Test
	void test2() {

		var min = new JsonPrimitive(10.09d);

		RandomRepository rand = new RandomRepository();

		Mono<AbstractRandom> absR = rand.find(Namespaces.MATH, "RandomDouble").map(r -> (AbstractRandom) r);

		ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
				new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
				.setArguments(Map.of("minValue", min));

		StepVerifier.create(absR.flatMap(e -> e.execute(fep))).expectNextMatches(r -> {
			var x = r.allResults().get(0).getResult().get("value");
			assertTrue(x.getAsDouble() >= min.getAsDouble() && x.getAsDouble() <= Double.MAX_VALUE);
			return true;
		}).verifyComplete();
	}

	@Test
	void test3() {

		var min = new JsonPrimitive(-123);

		var max = new JsonPrimitive(-1);

		RandomRepository rand = new RandomRepository();

		Mono<AbstractRandom> absR = rand.find(Namespaces.MATH, "RandomInt").map(r -> (AbstractRandom) r);

		ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
				new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
				.setArguments(Map.of("minValue", min, "maxValue", max));

		StepVerifier.create(absR.flatMap(e -> e.execute(fep))).expectNextMatches(r -> {
			var x = r.allResults().get(0).getResult().get("value");
			assertTrue(x.getAsInt() >= min.getAsInt() && x.getAsInt() <= max.getAsInt());
			return true;
		}).verifyComplete();
	}
}
