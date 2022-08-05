package com.fincity.nocode.kirun.engine.function.system.array;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.exception.KIRuntimeException;
import com.fincity.nocode.kirun.engine.json.schema.validator.exception.SchemaValidationException;
import com.fincity.nocode.kirun.engine.runtime.FunctionExecutionParameters;

import com.google.gson.JsonArray;
import com.google.gson.JsonNull;

class DeleteTest {

	@Test
	void test() {
		var arr = new JsonArray();
		arr.add(12);
		arr.add(14);
		arr.add(15);
		arr.add(9);

		var res = new JsonArray();
		res.add(14);
		res.add(15);

		var ares = new JsonArray();
		ares.add(12);
		ares.add(9);

		Delete del = new Delete();

		FunctionExecutionParameters fep = new FunctionExecutionParameters()
				.setArguments(Map.of("source", arr, "secondSource", res)).setContext(Map.of()).setOutput(Map.of());

		del.execute(fep);

		assertEquals(ares, arr);

	}

	@Test
	void test2() {

		var arr = new JsonArray();
		arr.add("nocode");
		arr.add("platform");
		arr.add(14);

		var res = new JsonArray();
		res.add("platform");

		Delete del = new Delete();

		FunctionExecutionParameters fep = new FunctionExecutionParameters()
				.setArguments(Map.of("source", arr, "secondSource", res)).setOutput(Map.of()).setContext(Map.of());

		var ares = new JsonArray();
		ares.add("nocode");
		ares.add(14);

		del.execute(fep);

		assertEquals(ares, arr);
	}

	@Test
	void test3() {
		var arr = new JsonArray();
		arr.add("nocode");
		arr.add("platform");
		arr.add(14);

		var res = new JsonArray();
		res.add("platform");

		Delete del = new Delete();

		FunctionExecutionParameters fep = new FunctionExecutionParameters()
				.setArguments(Map.of("source", arr, "secondSource", JsonNull.INSTANCE)).setOutput(Map.of())
				.setContext(Map.of());

		assertThrows(SchemaValidationException.class, () -> del.execute(fep));

	}

	@Test
	void test4() {
		var arr = new JsonArray();
		arr.add("nocode");
		arr.add("platform");
		arr.add(14);

		var res = new JsonArray();
		res.add("platform");

		Delete del = new Delete();

		FunctionExecutionParameters fep = new FunctionExecutionParameters()
				.setArguments(Map.of("source", JsonNull.INSTANCE, "secondSource", arr)).setOutput(Map.of())
				.setContext(Map.of());

		assertThrows(SchemaValidationException.class, () -> del.execute(fep));

	}

	@Test
	void test5() {
		var arr = new JsonArray();
		arr.add("nocode");
		arr.add("platform");
		arr.add(14);

		var res = new JsonArray();
		res.add("platform");

		Delete del = new Delete();

		FunctionExecutionParameters fep = new FunctionExecutionParameters()
				.setArguments(Map.of("source", res, "secondSource", arr)).setOutput(Map.of()).setContext(Map.of());

		assertThrows(KIRuntimeException.class, () -> del.execute(fep));

	}
}
