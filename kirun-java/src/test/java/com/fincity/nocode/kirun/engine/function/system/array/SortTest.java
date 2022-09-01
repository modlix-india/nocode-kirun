package com.fincity.nocode.kirun.engine.function.system.array;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.repository.KIRunFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.KIRunSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.FunctionExecutionParameters;
import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;

class SortTest {

	@Test
	void test() {

		var arr = new JsonArray();
		arr.add(12);
		arr.add(15);
		arr.add(98);
		arr.add(1);

		FunctionExecutionParameters fep = new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository()).setArguments(
				Map.of("source", arr, "findFrom", new JsonPrimitive(0), "length", new JsonPrimitive(arr.size())));

		var res = new JsonArray();
		res.add(1);
		res.add(12);
		res.add(15);
		res.add(98);

		Sort sort = new Sort();

		assertEquals(res, sort.execute(fep).allResults().get(0).getResult().get("output"));
	}

	@Test
	void mytest() {
		var arr = new JsonArray();
		arr.add(12);
		arr.add(15);
		arr.add(98);
		arr.add(1);

		FunctionExecutionParameters fep = new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository()).setArguments(
				Map.of("source", arr, "findFrom", new JsonPrimitive(1), "ascending", new JsonPrimitive(false)));

		var res = new JsonArray();

		res.add(12);
		res.add(98);
		res.add(15);
		res.add(1);

		Sort sort = new Sort();

		assertEquals(res, sort.execute(fep).allResults().get(0).getResult().get("output"));
	}

	@Test
	void test2() {

		var arr = new JsonArray();
		arr.add(12);
		arr.add(15);
		arr.add(98);
		arr.add(1);
		arr.add("sure");
		arr.add('c');

		var res = new JsonArray();
		res.add(12);
		res.add(15);
		res.add("sure");
		res.add('c');
		res.add(98);
		res.add(1);

		FunctionExecutionParameters fep = new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository()).setArguments(
				Map.of("source", arr, "findFrom", new JsonPrimitive(2), "ascending", new JsonPrimitive(false)));

		Sort sort = new Sort();

		assertEquals(res, sort.execute(fep).allResults().get(0).getResult().get("output"));

	}

}
