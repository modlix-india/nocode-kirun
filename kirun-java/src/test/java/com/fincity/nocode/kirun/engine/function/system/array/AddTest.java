package com.fincity.nocode.kirun.engine.function.system.array;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.json.schema.validator.exception.SchemaValidationException;
import com.fincity.nocode.kirun.engine.repository.KIRunFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.KIRunSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.FunctionExecutionParameters;
import com.google.gson.JsonArray;
import com.google.gson.JsonNull;

class AddTest {

	@Test
	void test() {
		var arr = new JsonArray();
		arr.add(12);

		var arr2 = new JsonArray();
		arr2.add(14);

		Add ad = new Add();

		FunctionExecutionParameters fep = new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository())
				.setArguments(Map.of("source", arr, "secondSource", arr2)).setContext(Map.of()).setSteps(Map.of());

		var res = new JsonArray();
		res.add(12);
		res.add(14);

		ad.execute(fep);

		assertEquals(res, arr);

		var arr1 = new JsonArray();
		arr1.add(12);

		var res1 = new JsonArray();
		res1.add(12);
		res1.add(12);
		res1.add(14);

		FunctionExecutionParameters fep1 = new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository())
				.setArguments(Map.of("source", arr1, "secondSource", arr)).setContext(Map.of()).setSteps(Map.of());

		ad.execute(fep1);

		assertEquals(res1, arr1);

	}

	@Test
	void test2() {

		var emp = new JsonArray();

		var arr = new JsonArray();
		arr.add("nocode");
		arr.add("platform");

		Add ad = new Add();

		FunctionExecutionParameters fep = new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository())
				.setArguments(Map.of("source", arr, "secondSource", emp)).setContext(Map.of()).setSteps(Map.of());

		var res = new JsonArray();
		res.add("nocode");
		res.add("platform");

		ad.execute(fep);

		assertEquals(res, arr);

	}

	@Test
	void test3() {
		var emp = new JsonArray();

		var arr = new JsonArray();

		Add ad = new Add();

		FunctionExecutionParameters fep = new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository())
				.setArguments(Map.of("source", arr, "secondSource", emp)).setContext(Map.of()).setSteps(Map.of());

		ad.execute(fep);

		assertEquals(emp, arr);

	}

	@Test
	void test4() {

		var emp = new JsonArray();

		var arr = new JsonArray();
		arr.add("nocode");
		arr.add("platform");

		Add ad = new Add();

		FunctionExecutionParameters fep = new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository())
				.setArguments(Map.of("source", emp, "secondSource", arr)).setContext(Map.of()).setSteps(Map.of());

		var res = new JsonArray();
		res.add("nocode");
		res.add("platform");

		ad.execute(fep);

		assertEquals(res, arr);

	}

	@Test
	void test5() {

		var arr = new JsonArray();
		arr.add("nocode");
		arr.add("platform");

		Add ad = new Add();

		FunctionExecutionParameters fep = new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository())
				.setArguments(Map.of("source", JsonNull.INSTANCE, "secondSource", arr)).setContext(Map.of())
				.setSteps(Map.of());

		var res = new JsonArray();
		res.add("nocode");
		res.add("platform");

		assertThrows(SchemaValidationException.class, () -> ad.execute(fep));

	}

	@Test
	void test6() {

		var arr = new JsonArray();
		arr.add("nocode");
		arr.add("platform");

		Add ad = new Add();

		FunctionExecutionParameters fep = new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository())
				.setArguments(Map.of("source", arr, "secondSource", JsonNull.INSTANCE)).setContext(Map.of())
				.setSteps(Map.of());

		var res = new JsonArray();
		res.add("nocode");
		res.add("platform");

		assertThrows(SchemaValidationException.class, () -> ad.execute(fep));

	}
}
