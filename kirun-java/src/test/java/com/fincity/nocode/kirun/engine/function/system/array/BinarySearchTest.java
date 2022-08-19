package com.fincity.nocode.kirun.engine.function.system.array;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.json.schema.validator.exception.SchemaValidationException;
import com.fincity.nocode.kirun.engine.runtime.FunctionExecutionParameters;
import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;

class BinarySearchTest {

	@Test
	void test() {
		var src = new JsonArray();
		src.add(2);
		src.add(4);
		src.add(10);
		src.add(12);
		src.add(20);
		src.add(1233);

		var search = new JsonPrimitive(20);

		FunctionExecutionParameters fep = new FunctionExecutionParameters()
				.setArguments(Map.of(BinarySearch.PARAMETER_ARRAY_SOURCE_PRIMITIVE.getParameterName(), src,
						BinarySearch.PARAMETER_INT_SOURCE_FROM.getParameterName(), new JsonPrimitive(1),
						BinarySearch.PARAMETER_FIND_PRIMITIVE.getParameterName(), search,
						BinarySearch.PARAMETER_INT_LENGTH.getParameterName(), new JsonPrimitive(4)));
		BinarySearch bs = new BinarySearch();

		assertEquals(bs.execute(fep).allResults().get(0).getResult().get(BinarySearch.EVENT_INDEX_NAME),
				new JsonPrimitive(4));
	}

	@Test
	void test2() {
		var src = new JsonArray();
		src.add(2);
		src.add(4);
		src.add(10);
		src.add(12);
		src.add(20);
		src.add(1233);

		FunctionExecutionParameters fep = new FunctionExecutionParameters()
				.setArguments(Map.of(BinarySearch.PARAMETER_ARRAY_SOURCE_PRIMITIVE.getParameterName(), src,
						BinarySearch.PARAMETER_INT_SOURCE_FROM.getParameterName(), new JsonPrimitive(1),
						BinarySearch.PARAMETER_FIND_PRIMITIVE.getParameterName(), JsonNull.INSTANCE,
						BinarySearch.PARAMETER_INT_LENGTH.getParameterName(), new JsonPrimitive(4)));
		BinarySearch bs = new BinarySearch();

		assertThrows(SchemaValidationException.class, () -> bs.execute(fep));
	}

	@Test
	void test3() {

		JsonArray arr = new JsonArray();
		arr.add('a');
		arr.add('b');
		arr.add('c');
		arr.add('d');
		arr.add('e');
		arr.add('g');
		arr.add('i');
		arr.add('j');
		arr.add('k');
		arr.add('r');
		arr.add('s');
		arr.add('z');

		var res = new JsonPrimitive('c');

		FunctionExecutionParameters fep = new FunctionExecutionParameters()
				.setArguments(Map.of(BinarySearch.PARAMETER_ARRAY_SOURCE_PRIMITIVE.getParameterName(), arr,
						BinarySearch.PARAMETER_INT_SOURCE_FROM.getParameterName(), new JsonPrimitive(1),
						BinarySearch.PARAMETER_FIND_PRIMITIVE.getParameterName(), res,
						BinarySearch.PARAMETER_INT_LENGTH.getParameterName(), new JsonPrimitive(8)));
		BinarySearch bs = new BinarySearch();

		assertEquals(bs.execute(fep).allResults().get(0).getResult().get(BinarySearch.EVENT_INDEX_NAME),
				new JsonPrimitive(2));
	}

	@Test
	void test4() {

		JsonArray arr = new JsonArray();
		arr.add('a');
		arr.add('b');
		arr.add('c');
		arr.add('d');
		arr.add('e');
		arr.add('g');
		arr.add('i');
		arr.add('j');
		arr.add('k');
		arr.add('r');
		arr.add('s');
		arr.add('z');

		var res = new JsonPrimitive('z');

		FunctionExecutionParameters fep = new FunctionExecutionParameters()
				.setArguments(Map.of(BinarySearch.PARAMETER_ARRAY_SOURCE_PRIMITIVE.getParameterName(), arr,
						BinarySearch.PARAMETER_INT_SOURCE_FROM.getParameterName(), new JsonPrimitive(-1),
						BinarySearch.PARAMETER_FIND_PRIMITIVE.getParameterName(), res,
						BinarySearch.PARAMETER_INT_LENGTH.getParameterName(), new JsonPrimitive(arr.size())));
		BinarySearch bs = new BinarySearch();

		assertThrows(SchemaValidationException.class, () -> bs.execute(fep));
	}
	
	@Test
	void test5() {

		JsonArray arr = new JsonArray();
		arr.add('a');
		arr.add('b');
		arr.add('c');
		arr.add('d');
		arr.add('e');
		arr.add('g');
		arr.add('i');
		arr.add('j');
		arr.add('k');
		arr.add('r');
		arr.add('s');
		arr.add('z');

		FunctionExecutionParameters fep = new FunctionExecutionParameters()
				.setArguments(Map.of(BinarySearch.PARAMETER_ARRAY_SOURCE_PRIMITIVE.getParameterName(), arr,
						BinarySearch.PARAMETER_INT_SOURCE_FROM.getParameterName(), new JsonPrimitive(4),
						BinarySearch.PARAMETER_FIND_PRIMITIVE.getParameterName(), new JsonArray(),
						BinarySearch.PARAMETER_INT_LENGTH.getParameterName(), new JsonPrimitive(arr.size())));
		BinarySearch bs = new BinarySearch();

		assertThrows(SchemaValidationException.class, () -> bs.execute(fep));
	}
}
