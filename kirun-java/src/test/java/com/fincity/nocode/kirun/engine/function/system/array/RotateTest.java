package com.fincity.nocode.kirun.engine.function.system.array;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.json.schema.validator.exception.SchemaValidationException;
import com.fincity.nocode.kirun.engine.runtime.FunctionExecutionParameters;
import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;

class RotateTest {

	@Test
	void test() {
		var array = new JsonArray();
		array.add("I");
		array.add("am");
		array.add("using");
		array.add("eclipse");
		array.add("to");
		array.add("test");
		array.add("the");
		array.add("changes");
		array.add("with");
		array.add("test");
		array.add("Driven");
		array.add("developement");

		FunctionExecutionParameters fep = new FunctionExecutionParameters();

		fep.setArguments(Map.of("source", array, "rotateDistance", new JsonPrimitive(4))).setContext(Map.of())
				.setSteps(Map.of());

		var res = new JsonArray();

		res.add("to");
		res.add("test");
		res.add("the");
		res.add("changes");
		res.add("with");
		res.add("test");
		res.add("Driven");
		res.add("developement");

		res.add("I");
		res.add("am");
		res.add("using");
		res.add("eclipse");

		Rotate rotate = new Rotate();
		rotate.execute(fep);

		assertEquals(res, array);

		FunctionExecutionParameters fep1 = new FunctionExecutionParameters();

		fep1.setArguments(Map.of("source", array, "rotateDistance", new JsonPrimitive(0))).setContext(Map.of())
				.setSteps(Map.of());

		assertThrows(SchemaValidationException.class, () -> rotate.execute(fep1));
	}

	@Test
	void test2() {
		var array = new JsonArray();
		array.add("I");
		array.add("am");
		array.add("using");
		array.add("eclipse");
		array.add("to");
		array.add("test");
		array.add("the");
		array.add("changes");
		array.add("with");
		array.add("test");
		array.add("Driven");
		array.add("developement");

		FunctionExecutionParameters fep = new FunctionExecutionParameters();

		fep.setArguments(Map.of("source", array, "rotateDistance", new JsonPrimitive(4))).setContext(Map.of())
				.setSteps(Map.of());

		var res = new JsonArray();

		res.add("to");
		res.add("test");
		res.add("the");
		res.add("changes");
		res.add("with");
		res.add("test");
		res.add("Driven");
		res.add("developement");

		res.add("I");
		res.add("am");
		res.add("using");
		res.add("eclipse");

		Rotate rotate = new Rotate();
		rotate.execute(fep);

		assertEquals(res, array);

		FunctionExecutionParameters fep1 = new FunctionExecutionParameters();

		fep1.setArguments(Map.of("source", array, "rotateDistance", new JsonPrimitive(-2))).setContext(Map.of())
				.setSteps(Map.of());

		assertThrows(SchemaValidationException.class, () -> rotate.execute(fep1));
	}

}
