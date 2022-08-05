package com.fincity.nocode.kirun.engine.function.system.array;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.json.schema.validator.exception.SchemaValidationException;
import com.fincity.nocode.kirun.engine.runtime.FunctionExecutionParameters;
import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;

class LastIndexOfArrayTest {

	@Test
	void test() {

		JsonArray arr = new JsonArray();
		arr.add('a');
		arr.add('b');
		arr.add('c');
		arr.add('d');
		arr.add('a');
		arr.add('b');
		arr.add('c');
		arr.add('e');
		arr.add('d');
		arr.add('b');
		arr.add('c');
		arr.add('d');

		JsonArray res = new JsonArray();
		res.add('b');
		res.add('c');
		res.add('d');

		LastIndexOfArray lia = new LastIndexOfArray();

		FunctionExecutionParameters fep = new FunctionExecutionParameters()
				.setArguments(Map.of(IndexOfArray.PARAMETER_ARRAY_SOURCE.getParameterName(), arr,
						IndexOfArray.PARAMETER_ARRAY_SECOND_SOURCE.getParameterName(), res,
						IndexOfArray.PARAMETER_INT_SOURCE_FROM.getParameterName(), new JsonPrimitive(1)));

		assertEquals(new JsonPrimitive(9),
				lia.execute(fep).allResults().get(0).getResult().get(IndexOfArray.EVENT_RESULT_INTEGER.getName()));

	}

	@Test
	void test2() {

		JsonArray arr = new JsonArray();
		arr.add('a');
		arr.add('b');
		arr.add('c');
		arr.add('d');
		arr.add('a');
		arr.add('b');
		arr.add('c');
		arr.add('e');
		arr.add('d');
		arr.add('b');
		arr.add('c');
		arr.add('d');

		JsonArray res = new JsonArray();
		res.add('b');
		res.add('d');

		LastIndexOfArray lia = new LastIndexOfArray();

		FunctionExecutionParameters fep = new FunctionExecutionParameters()
				.setArguments(Map.of(IndexOfArray.PARAMETER_ARRAY_SOURCE.getParameterName(), arr,
						IndexOfArray.PARAMETER_ARRAY_SECOND_SOURCE.getParameterName(), res,
						IndexOfArray.PARAMETER_INT_SOURCE_FROM.getParameterName(), new JsonPrimitive(1)));

		assertEquals(new JsonPrimitive(-1),
				lia.execute(fep).allResults().get(0).getResult().get(IndexOfArray.EVENT_RESULT_INTEGER.getName()));

	}

	@Test
	void test3() {
		JsonArray arr = new JsonArray();
		arr.add('a');
		arr.add('b');
		arr.add('c');
		arr.add('d');
		arr.add('a');
		arr.add('b');
		arr.add('c');
		arr.add('e');
		arr.add('d');
		arr.add('b');
		arr.add('c');
		arr.add('d');

		LastIndexOfArray lia = new LastIndexOfArray();

		FunctionExecutionParameters fep = new FunctionExecutionParameters()
				.setArguments(Map.of(IndexOfArray.PARAMETER_ARRAY_SOURCE.getParameterName(), arr,
						IndexOfArray.PARAMETER_ARRAY_SECOND_SOURCE.getParameterName(), JsonNull.INSTANCE,
						IndexOfArray.PARAMETER_INT_SOURCE_FROM.getParameterName(), new JsonPrimitive(1)));

		assertThrows(SchemaValidationException.class, () -> lia.execute(fep).allResults().get(0).getResult()
				.get(IndexOfArray.EVENT_RESULT_INTEGER.getName()));

		FunctionExecutionParameters fep1 = new FunctionExecutionParameters()
				.setArguments(Map.of(IndexOfArray.PARAMETER_ARRAY_SOURCE.getParameterName(), JsonNull.INSTANCE,
						IndexOfArray.PARAMETER_ARRAY_SECOND_SOURCE.getParameterName(), arr,
						IndexOfArray.PARAMETER_INT_SOURCE_FROM.getParameterName(), new JsonPrimitive(1)));

		assertThrows(SchemaValidationException.class, () -> lia.execute(fep1).allResults().get(0).getResult()
				.get(IndexOfArray.EVENT_RESULT_INTEGER.getName()));

	}

	@Test
	void test4() {

		JsonArray arr = new JsonArray();
		arr.add('a');
		arr.add('b');
		arr.add('c');
		arr.add('d');
		arr.add('a');
		arr.add('b');
		arr.add('c');
		arr.add('e');
		arr.add('d');
		arr.add('b');
		arr.add('c');
		arr.add('d');

		JsonArray res = new JsonArray();
		res.add('b');
		res.add('c');
		res.add('d');

		LastIndexOfArray lia = new LastIndexOfArray();

		FunctionExecutionParameters fep = new FunctionExecutionParameters()
				.setArguments(Map.of(IndexOfArray.PARAMETER_ARRAY_SOURCE.getParameterName(), arr,
						IndexOfArray.PARAMETER_ARRAY_SECOND_SOURCE.getParameterName(), res,
						IndexOfArray.PARAMETER_INT_SOURCE_FROM.getParameterName(), new JsonPrimitive(-1)));

		assertEquals(new JsonPrimitive(9),
				lia.execute(fep).allResults().get(0).getResult().get(IndexOfArray.EVENT_RESULT_INTEGER.getName()));

	}

	@Test
	void test5() {

		var emp = new JsonArray();

		JsonArray arr = new JsonArray();
		arr.add('a');
		arr.add('b');
		arr.add('c');
		arr.add('d');
		arr.add('a');
		arr.add('b');
		arr.add('c');
		arr.add('e');
		arr.add('d');
		arr.add('b');
		arr.add('c');
		arr.add('d');

		JsonArray res = new JsonArray();
		res.add('b');
		res.add('c');
		res.add('d');

		LastIndexOfArray lia = new LastIndexOfArray();

		FunctionExecutionParameters fep = new FunctionExecutionParameters()
				.setArguments(Map.of(IndexOfArray.PARAMETER_ARRAY_SOURCE.getParameterName(), emp,
						IndexOfArray.PARAMETER_ARRAY_SECOND_SOURCE.getParameterName(), res,
						IndexOfArray.PARAMETER_INT_SOURCE_FROM.getParameterName(), new JsonPrimitive(2)));

		assertEquals(new JsonPrimitive(-1),
				lia.execute(fep).allResults().get(0).getResult().get(IndexOfArray.EVENT_RESULT_INTEGER.getName()));

	}

	@Test
	void test6() {

		var emp = new JsonArray();

		JsonArray arr = new JsonArray();
		arr.add('a');
		arr.add('b');
		arr.add('c');
		arr.add('d');
		arr.add('a');
		arr.add('b');
		arr.add('c');
		arr.add('e');
		arr.add('d');
		arr.add('b');
		arr.add('c');
		arr.add('d');

		JsonArray res = new JsonArray();
		res.add('b');
		res.add('c');
		res.add('d');

		LastIndexOfArray lia = new LastIndexOfArray();

		FunctionExecutionParameters fep = new FunctionExecutionParameters()
				.setArguments(Map.of(IndexOfArray.PARAMETER_ARRAY_SOURCE.getParameterName(), res,
						IndexOfArray.PARAMETER_ARRAY_SECOND_SOURCE.getParameterName(), emp,
						IndexOfArray.PARAMETER_INT_SOURCE_FROM.getParameterName(), new JsonPrimitive(2)));

		assertEquals(new JsonPrimitive(-1),
				lia.execute(fep).allResults().get(0).getResult().get(IndexOfArray.EVENT_RESULT_INTEGER.getName()));

	}
}
