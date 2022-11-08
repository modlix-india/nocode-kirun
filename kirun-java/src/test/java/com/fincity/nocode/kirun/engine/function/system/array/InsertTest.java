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

class InsertTest {

	@Test
	void test() {
		JsonArray arr = new JsonArray();
		arr.add(1);
		arr.add(2);
		arr.add(3);
		arr.add(4);
		arr.add(5);
		arr.add(6);
		arr.add(7);
		arr.add(8);

		JsonArray res = new JsonArray();
		res.add(1);
		res.add(2);
		res.add(9);
		res.add(3);
		res.add(4);
		res.add(5);
		res.add(6);
		res.add(7);
		res.add(8);

		Insert ins = new Insert();

		FunctionExecutionParameters fep = new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository())
				.setArguments(Map.of("source", arr, "offset", new JsonPrimitive(2), "element", new JsonPrimitive(9)))
				.setContext(Map.of()).setSteps(Map.of());

		ins.execute(fep);

		assertEquals(res, arr);

	}

	@Test
	void test2() {
		JsonArray arr = new JsonArray();
		arr.add(1);
		arr.add(2);
		arr.add(3);
		arr.add(4);
		arr.add(5);
		arr.add(6);
		arr.add(7);
		arr.add(8);

		JsonArray res = new JsonArray();
		res.add('a');
		res.add(1);
		res.add(2);
		res.add(3);
		res.add(4);
		res.add(5);
		res.add(6);
		res.add(7);
		res.add(8);

		Insert ins = new Insert();

		FunctionExecutionParameters fep = new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository())
				.setArguments(Map.of("source", arr, "offset", new JsonPrimitive(0), "element", new JsonPrimitive('a')))
				.setContext(Map.of()).setSteps(Map.of());

		ins.execute(fep);

		assertEquals(res, arr);

	}

	@Test
	void test3() {
		JsonArray arr = new JsonArray();

		JsonArray res = new JsonArray();
		res.add('a');

		Insert ins = new Insert();

		FunctionExecutionParameters fep = new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository())
				.setArguments(Map.of("source", arr, "offset", new JsonPrimitive(0), "element", new JsonPrimitive('a')))
				.setContext(Map.of()).setSteps(Map.of());

		ins.execute(fep);

		assertEquals(res, arr);

	}

	@Test
	void test4() {

		JsonArray res = new JsonArray();
		res.add('a');

		Insert ins = new Insert();

		FunctionExecutionParameters fep = new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository()).setArguments(
				Map.of("source", JsonNull.INSTANCE, "offset", new JsonPrimitive(0), "element", new JsonPrimitive('a')))
				.setContext(Map.of()).setSteps(Map.of());

		assertThrows(KIRuntimeException.class, () -> ins.execute(fep));

	}

}
