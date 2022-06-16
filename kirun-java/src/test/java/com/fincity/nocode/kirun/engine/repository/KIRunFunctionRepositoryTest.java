package com.fincity.nocode.kirun.engine.repository;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.function.math.Abs;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;
import com.fincity.nocode.kirun.engine.namespaces.Namespaces;

class KIRunFunctionRepositoryTest {

	@Test
	void test() {

		FunctionSignature signature = new KIRunFunctionRepository().find(new Abs().getSignature()
		        .getFullName())
		        .getSignature();

		assertEquals(Namespaces.MATH + ".Abs", signature.getFullName());
	}

}
