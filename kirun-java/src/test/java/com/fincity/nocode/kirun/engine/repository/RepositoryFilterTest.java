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

		StepVerifier.create(schemaRepo.filter("").sort().collect(Collectors.toSet()))
		.expectNext(Set.of("System.any", "System.boolean", "System.double", "System.Date.timeStamp",
				"System.float", "System.integer", "System.long", "System.number", "System.Null",
				"System.ParameterExpression", "System.Schema", "System.string"))
		.verifyComplete();
	}

}