package com.fincity.nocode.kirun.engine.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.function.system.MathFunctionRepository;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;
import com.fincity.nocode.kirun.engine.namespaces.Namespaces;

class KIRunFunctionRepositoryTest {

	@Test
	void test() {

		FunctionSignature abs = new MathFunctionRepository().find(Namespaces.MATH, "Absolute").getSignature();

		FunctionSignature signature = new KIRunFunctionRepository().find(abs.getNamespace(), abs.getName())
				.getSignature();

		assertEquals(Namespaces.MATH, signature.getNamespace());

		assertEquals(abs.getName(), signature.getName());
	}

}
