package com.fincity.nocode.kirun.engine.repository;

import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;

import reactor.test.StepVerifier;

class RepositoryFilterTest {

	@Test
	void test() {

		var funcRepo = new KIRunReactiveFunctionRepository();
		var schemaRepo = new KIRunReactiveSchemaRepository();

		StepVerifier.create(funcRepo.filter("Rep").collect(Collectors.toSet()))
				.expectNext(Set.of("System.String.PrePad", "System.String.ReplaceAtGivenPosition",
						"System.String.ReplaceFirst",
						"System.String.Repeat", "System.String.Replace"))
				.verifyComplete();

		StepVerifier.create(funcRepo.filter("root").collect(Collectors.toSet()))
				.expectNext(Set.of("System.Math.CubeRoot", "System.Math.SquareRoot"))
				.verifyComplete();

		StepVerifier.create(schemaRepo.filter("rin"))
				.expectNext("System.string")
				.verifyComplete();

		StepVerifier.create(schemaRepo.filter("ny"))
				.expectNext("System.any")
				.verifyComplete();

		StepVerifier.create(schemaRepo.filter("").collect(Collectors.toSet()))
				.expectNext(Set.of("System.integer", "System.Null", "System.float", "System.number",
						"System.ParameterExpression", "System.long", "System.string", "System.boolean", "System.Schema",
						"System.double", "System.any"))
				.verifyComplete();
	}

}
