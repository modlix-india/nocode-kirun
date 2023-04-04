package com.fincity.nocode.kirun.engine.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

class RepositoryFilterTest {

	@Test
	void test() {

		var funcRepo = new KIRunFunctionRepository();
		var schemaRepo = new KIRunSchemaRepository();

		assertEquals(
		        Set.of("System.String.Repeat", "System.String.Replace", "System.String.ReplaceFirst",
		                "System.String.PrePad", "System.String.ReplaceAtGivenPosition"),
		        new HashSet<>(funcRepo.filter("Rep")));

		assertEquals(Set.of("System.Math.CubeRoot", "System.Math.SquareRoot"), new HashSet<>(funcRepo.filter("root")));

		assertEquals(Set.of(), new HashSet<>(schemaRepo.filter("root")));
		assertEquals(Set.of("System.string"), new HashSet<>(schemaRepo.filter("rin")));
		assertEquals(Set.of("System.any"), new HashSet<>(schemaRepo.filter("ny")));
		assertEquals(
		        Set.of("System.any", "System.boolean", "System.double", "System.float", "System.integer", "System.long",
		                "System.number", "System.string", "System.ParameterExpression", "System.Null", "System.Schema"),
		        new HashSet<>(schemaRepo.filter("")));

	}

}
