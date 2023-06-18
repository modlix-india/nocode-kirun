package com.fincity.nocode.kirun.engine.function.system.array;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.exception.KIRuntimeException;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

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

		ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
				new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
				.setArguments(Map.of(BinarySearch.PARAMETER_ARRAY_SOURCE_PRIMITIVE.getParameterName(), src,
						BinarySearch.PARAMETER_INT_SOURCE_FROM.getParameterName(), new JsonPrimitive(1),
						BinarySearch.PARAMETER_FIND_PRIMITIVE.getParameterName(), search,
						BinarySearch.PARAMETER_INT_LENGTH.getParameterName(), new JsonPrimitive(4)));
		BinarySearch bs = new BinarySearch();

		StepVerifier.create(bs.execute(fep))
				.expectNextMatches(result -> result.next().getResult().get(BinarySearch.EVENT_INDEX_NAME)
						.equals(new JsonPrimitive(4))).verifyComplete();
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

		ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
				new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
				.setArguments(Map.of(BinarySearch.PARAMETER_ARRAY_SOURCE_PRIMITIVE.getParameterName(), src,
						BinarySearch.PARAMETER_INT_SOURCE_FROM.getParameterName(), new JsonPrimitive(1),
						BinarySearch.PARAMETER_FIND_PRIMITIVE.getParameterName(), JsonNull.INSTANCE,
						BinarySearch.PARAMETER_INT_LENGTH.getParameterName(), new JsonPrimitive(4)));
		BinarySearch bs = new BinarySearch();

		StepVerifier.create(bs.execute(fep))
				.verifyError(KIRuntimeException.class);
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

		ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
				new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
				.setArguments(Map.of(BinarySearch.PARAMETER_ARRAY_SOURCE_PRIMITIVE.getParameterName(), arr,
						BinarySearch.PARAMETER_INT_SOURCE_FROM.getParameterName(), new JsonPrimitive(1),
						BinarySearch.PARAMETER_FIND_PRIMITIVE.getParameterName(), res,
						BinarySearch.PARAMETER_INT_LENGTH.getParameterName(), new JsonPrimitive(8)));
		BinarySearch bs = new BinarySearch();

		StepVerifier.create(bs.execute(fep))
				.expectNextMatches(result -> result.next().getResult().get(BinarySearch.EVENT_INDEX_NAME)
						.equals(new JsonPrimitive(2))).verifyComplete();
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

		ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
				new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
				.setArguments(Map.of(BinarySearch.PARAMETER_ARRAY_SOURCE_PRIMITIVE.getParameterName(), arr,
						BinarySearch.PARAMETER_INT_SOURCE_FROM.getParameterName(), new JsonPrimitive(-1),
						BinarySearch.PARAMETER_FIND_PRIMITIVE.getParameterName(), res,
						BinarySearch.PARAMETER_INT_LENGTH.getParameterName(), new JsonPrimitive(arr.size())));
		BinarySearch bs = new BinarySearch();

		StepVerifier.create(bs.execute(fep))
				.verifyError(KIRuntimeException.class);
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

		ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
				new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
				.setArguments(Map.of(BinarySearch.PARAMETER_ARRAY_SOURCE_PRIMITIVE.getParameterName(), arr,
						BinarySearch.PARAMETER_INT_SOURCE_FROM.getParameterName(), new JsonPrimitive(4),
						BinarySearch.PARAMETER_FIND_PRIMITIVE.getParameterName(), new JsonArray(),
						BinarySearch.PARAMETER_INT_LENGTH.getParameterName(), new JsonPrimitive(arr.size())));
		BinarySearch bs = new BinarySearch();

		StepVerifier.create(bs.execute(fep))
				.verifyError(KIRuntimeException.class);
	}
}
