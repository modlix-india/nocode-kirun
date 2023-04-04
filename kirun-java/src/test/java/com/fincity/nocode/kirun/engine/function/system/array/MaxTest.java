package com.fincity.nocode.kirun.engine.function.system.array;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.exception.KIRuntimeException;
import com.fincity.nocode.kirun.engine.repository.KIRunFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.KIRunSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.FunctionExecutionParameters;
import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;

class MaxTest {

	@Test
	void test() {
		var arr = new JsonArray();
		arr.add(12);

		Max max = new Max();

		FunctionExecutionParameters fep = new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository()).setArguments(Map.of("source", arr));

		assertEquals(new JsonPrimitive(12), max.execute(fep).allResults().get(0).getResult().get("output"));

		var arr1 = new JsonArray();
		FunctionExecutionParameters fep1 = new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository()).setArguments(Map.of("source", arr1));

		assertThrows(KIRuntimeException.class, () -> max.execute(fep1).allResults().get(0).getResult().get("output"));

	}

	@Test
	void test2() {
		var arr = new JsonArray();
		arr.add(12);
		arr.add(15);
		arr.add(98);
		arr.add(1);

		Max max = new Max();

		FunctionExecutionParameters fep = new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository()).setArguments(Map.of("source", arr));

		assertEquals(new JsonPrimitive(98), max.execute(fep).allResults().get(0).getResult().get("output"));

		var arr1 = new JsonArray();

		arr1.add('c');
		arr1.add('r');
		arr1.add('d');
		arr1.add('s');
		FunctionExecutionParameters fep1 = new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository()).setArguments(Map.of("source", arr1));

		assertEquals(new JsonPrimitive('s'), max.execute(fep1).allResults().get(0).getResult().get("output"));

	}

	@Test
	void test3() {
		var arr = new JsonArray();
		Max max = new Max();

		FunctionExecutionParameters fep = new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository()).setArguments(Map.of("source", arr));

		assertThrows(KIRuntimeException.class, () -> max.execute(fep).allResults().get(0).getResult().get("output"));
	}

	@Test
	void test4() {
		var arr = new JsonArray();
		arr.add("nocode");
		arr.add("NoCode");
		arr.add("platform");

		Max max = new Max();

		FunctionExecutionParameters fep = new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository()).setArguments(Map.of("source", arr));

		assertEquals(new JsonPrimitive("platform"), max.execute(fep).allResults().get(0).getResult().get("output"));
	}

	@Test
	void test5() {
		var arr = new JsonArray();
		arr.add(456);
		arr.add("nocode");
		arr.add("NoCode");
		arr.add("platform");
		arr.add(123);

		Max max = new Max();

		FunctionExecutionParameters fep = new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository()).setArguments(Map.of("source", arr));

		assertEquals(new JsonPrimitive("platform"), max.execute(fep).allResults().get(0).getResult().get("output"));
	}

	@Test
	void test6() {
		Max max = new Max();

		FunctionExecutionParameters fep = new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository())
				.setArguments(Map.of("source", JsonNull.INSTANCE));

		assertThrows(KIRuntimeException.class,
				() -> max.execute(fep).allResults().get(0).getResult().get("output"));
	}
}
