package com.fincity.nocode.kirun.engine.repository;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.function.reactive.ReactiveFunction;
import com.fincity.nocode.kirun.engine.function.system.math.MathFunctionRepository;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;
import com.fincity.nocode.kirun.engine.namespaces.Namespaces;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class KIRunReactiveFunctionRepositoryTest {

	@Test
	void test() {

		Mono<ReactiveFunction> fun = new MathFunctionRepository().find(Namespaces.MATH, "Absolute");

		StepVerifier.create(fun)
				.expectNextMatches(funs -> funs.getSignature().getName()
						.equals("Absolute"))
				.verifyComplete();

		Mono<FunctionSignature> signature = fun.map(ReactiveFunction::getSignature)
				.flatMap(funs -> new KIRunReactiveFunctionRepository().find(funs.getNamespace(), funs.getName()))
				.map(ReactiveFunction::getSignature);

		StepVerifier.create(signature).expectNextMatches(fs -> fs.getNamespace().equals(Namespaces.MATH))
				.verifyComplete();

		Mono<ReactiveFunction> function = new KIRunReactiveFunctionRepository().find(Namespaces.STRING, "ToString");

		StepVerifier.create(function).expectNextMatches(funs -> funs.getSignature().getName()
				.equals("ToString")).verifyComplete();

		function = new KIRunReactiveFunctionRepository().find(Namespaces.STRING, "IndexOfWithStartPoint");

		StepVerifier.create(function).expectNextMatches(funs -> funs.getSignature().getName()
				.equals("IndexOfWithStartPoint")).verifyComplete();

		function = new KIRunReactiveFunctionRepository().find(Namespaces.SYSTEM_ARRAY, "Compare");

		StepVerifier.create(function).expectNextMatches(funs -> funs.getSignature().getName()
				.equals("Compare")).verifyComplete();

		function = new KIRunReactiveFunctionRepository().find(Namespaces.MATH, "RandomInt");

		StepVerifier.create(function).expectNextMatches(funs -> funs.getSignature().getName()
				.equals("RandomInt")).verifyComplete();

		function = new KIRunReactiveFunctionRepository().find(Namespaces.MATH, "Exponential");

		StepVerifier.create(function).expectNextMatches(funs -> funs.getSignature().getName()
				.equals("Exponential")).verifyComplete();

		function = new KIRunReactiveFunctionRepository().find(Namespaces.SYSTEM, "If");

		StepVerifier.create(function).expectNextMatches(funs -> funs.getSignature().getName()
				.equals("If")).verifyComplete();
	}

}
