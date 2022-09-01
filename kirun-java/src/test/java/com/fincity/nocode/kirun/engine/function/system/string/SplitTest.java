package com.fincity.nocode.kirun.engine.function.system.string;

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
import com.google.gson.JsonPrimitive;

class SplitTest {

	@Test
	void test1() {

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

		Split split = new Split();

		FunctionExecutionParameters fep = new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository())
				.setArguments(Map.of(Split.PARAMETER_STRING_NAME,
						new JsonPrimitive("I am using eclipse to test the changes with test Driven developement"),
						Split.PARAMETER_SPLIT_STRING_NAME, new JsonPrimitive(" ")));

		assertEquals(array, split.execute(fep).allResults().get(0).getResult().get("result"));

	}

	@Test
	void test2() {

		var array = new JsonArray();
		array.add("I am using ");
		array.add("clips");
		array.add(" to t");
		array.add("st th");
		array.add(" chang");
		array.add("s with t");
		array.add("st Driv");
		array.add("n d");
		array.add("v");
		array.add("lop");
		array.add("m");
		array.add("nt");

		Split split = new Split();

		FunctionExecutionParameters fep = new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository())
				.setArguments(Map.of(Split.PARAMETER_STRING_NAME,
						new JsonPrimitive("I am using eclipse to test the changes with test Driven developement"),
						Split.PARAMETER_SPLIT_STRING_NAME, new JsonPrimitive("e")));

		assertEquals(array, split.execute(fep).allResults().get(0).getResult().get("result"));

	}

	@Test
	void test3() {

		Split split = new Split();

		FunctionExecutionParameters fep = new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository())
				.setArguments(Map.of(Split.PARAMETER_STRING_NAME, JsonNull.INSTANCE, Split.PARAMETER_SPLIT_STRING_NAME,
						new JsonPrimitive("e")));

		assertThrows(SchemaValidationException.class, () -> split.execute(fep));
	}

}
