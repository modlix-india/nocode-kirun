package com.fincity.nocode.kirun.engine.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.function.Function;
import com.fincity.nocode.kirun.engine.function.system.math.MathFunctionRepository;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;
import com.fincity.nocode.kirun.engine.namespaces.Namespaces;

class KIRunFunctionRepositoryTest {

	@Test
	void test() {

		FunctionSignature fun = new MathFunctionRepository().find(Namespaces.MATH, "Absolute").getSignature();

		FunctionSignature signature = new KIRunFunctionRepository().find(fun.getNamespace(), fun.getName())
				.getSignature();

		assertEquals(Namespaces.MATH, signature.getNamespace());
		assertEquals(fun.getName(), signature.getName());
		
		Function function = new KIRunFunctionRepository().find(Namespaces.STRING, "ToString");
		assertNotNull(function);
		assertEquals(function.getSignature().getName(), "ToString");
		
		function = new KIRunFunctionRepository().find(Namespaces.STRING, "IndexOfWithStartPoint");
		assertNotNull(function);
		assertEquals(function.getSignature().getName(), "IndexOfWithStartPoint");
		
		
		function = new KIRunFunctionRepository().find(Namespaces.SYSTEM_ARRAY, "Compare");
		assertNotNull(function);
		assertEquals(function.getSignature().getName(), "Compare");

		function = new KIRunFunctionRepository().find(Namespaces.MATH, "RandomInt");
		assertNotNull(function);
		assertEquals(function.getSignature().getName(), "RandomInt");
		
		function = new KIRunFunctionRepository().find(Namespaces.MATH, "Exponential");
		assertNotNull(function);
		assertEquals(function.getSignature().getName(), "Exponential");
		
		function = new KIRunFunctionRepository().find(Namespaces.SYSTEM, "If");
		assertNotNull(function);
		assertEquals(function.getSignature().getName(), "If");
	}

}
