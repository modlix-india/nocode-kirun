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

class MinTest {

	@Test
	void test() {
		var arr = new JsonArray();
		arr.add(12);

		Min min = new Min();

		FunctionExecutionParameters fep = new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository()).setArguments(Map.of("source", arr));

		assertEquals(new JsonPrimitive(12), min.execute(fep).allResults().get(0).getResult().get("output"));

		var arr1 = new JsonArray();
		FunctionExecutionParameters fep1 = new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository()).setArguments(Map.of("source", arr1));

		assertThrows(KIRuntimeException.class, () -> min.execute(fep1).allResults().get(0).getResult().get("output"));

	}

	@Test
	void test2() {
		var arr = new JsonArray();
		arr.add(12);
		arr.add(15);
		arr.add(98);
		arr.add(1);

		Min min = new Min();

		FunctionExecutionParameters fep = new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository()).setArguments(Map.of("source", arr));

		assertEquals(new JsonPrimitive(1), min.execute(fep).allResults().get(0).getResult().get("output"));

		var arr1 = new JsonArray();

		arr1.add('c');
		arr1.add('r');
		arr1.add('d');
		arr1.add('s');
		FunctionExecutionParameters fep1 = new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository()).setArguments(Map.of("source", arr1));

		assertEquals(new JsonPrimitive('c'), min.execute(fep1).allResults().get(0).getResult().get("output"));

	}

	@Test
	void test3() {
		var arr = new JsonArray();
		Min min = new Min();

		FunctionExecutionParameters fep = new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository()).setArguments(Map.of("source", arr));

		assertThrows(KIRuntimeException.class, () -> min.execute(fep).allResults().get(0).getResult().get("output"));
	}

	@Test
	void test4() {
		var arr = new JsonArray();
		arr.add("nocode");
		arr.add("NoCode");
		arr.add("platform");

		Min min = new Min();

		FunctionExecutionParameters fep = new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository()).setArguments(Map.of("source", arr));

		assertEquals(new JsonPrimitive("NoCode"), min.execute(fep).allResults().get(0).getResult().get("output"));
	}

	@Test
	void test5() {
		var arr = new JsonArray();
		arr.add(456);
		arr.add("nocode");
		arr.add("NoCode");
		arr.add("platform");
		arr.add(123);

		Min min = new Min();

		FunctionExecutionParameters fep = new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository()).setArguments(Map.of("source", arr));

		assertEquals(new JsonPrimitive(123), min.execute(fep).allResults().get(0).getResult().get("output"));
	}

	@Test
	void test6() {
		Min min = new Min();

		FunctionExecutionParameters fep = new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository())
				.setArguments(Map.of("source", JsonNull.INSTANCE));

		assertThrows(KIRuntimeException.class,
				() -> min.execute(fep).allResults().get(0).getResult().get("output"));

	}
}
