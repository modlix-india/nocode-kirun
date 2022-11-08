package com.fincity.nocode.kirun.engine.function.system.array;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.exception.KIRuntimeException;
import com.fincity.nocode.kirun.engine.json.schema.validator.exception.SchemaValidationException;
import com.fincity.nocode.kirun.engine.repository.KIRunFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.KIRunSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.FunctionExecutionParameters;
import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;

class IndexOfArrayTest {

	@Test
	void test() {

		IndexOfArray ind = new IndexOfArray();

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

		JsonArray res = new JsonArray();
		res.add('b');
		res.add('c');
		res.add('d');

		FunctionExecutionParameters fep = new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository())
				.setArguments(Map.of(IndexOfArray.PARAMETER_ARRAY_SOURCE.getParameterName(), arr,
						IndexOfArray.PARAMETER_ARRAY_SECOND_SOURCE.getParameterName(), res,
						IndexOfArray.PARAMETER_INT_FIND_FROM.getParameterName(), new JsonPrimitive(1)));

		assertEquals(new JsonPrimitive(1),
				ind.execute(fep).allResults().get(0).getResult().get(IndexOfArray.EVENT_RESULT_INTEGER.getName()));

	}

	@Test
	void test2() {

		IndexOfArray ind = new IndexOfArray();

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

		JsonArray res = new JsonArray();
		res.add('b');
		res.add('c');
		res.add('e');
		res.add('d');

		FunctionExecutionParameters fep = new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository())
				.setArguments(Map.of(IndexOfArray.PARAMETER_ARRAY_SOURCE.getParameterName(), arr,
						IndexOfArray.PARAMETER_ARRAY_SECOND_SOURCE.getParameterName(), res,
						IndexOfArray.PARAMETER_INT_FIND_FROM.getParameterName(), new JsonPrimitive(4)));

		assertEquals(new JsonPrimitive(5),
				ind.execute(fep).allResults().get(0).getResult().get(IndexOfArray.EVENT_RESULT_INTEGER.getName()));

	}

	@Test
	void test3() {

		var array1 = new JsonArray();

		array1.add("test");
		array1.add("Driven");
		array1.add("developement");
		array1.add("I");
		array1.add("am");
		array1.add("using");
		array1.add("eclipse");
		array1.add("I");
		array1.add("to");
		array1.add("test");
		array1.add("the");
		array1.add("changes");
		array1.add("with");
		array1.add("test");
		array1.add("Driven");
		array1.add("developement");

		var array2 = new JsonArray();

		array2.add("test");
		array2.add("Driven");
		array2.add("developement");
		array2.add("I");
		array2.add("am");
		array2.add("using");
		array2.add("eclipse");
		array2.add("I");
		array2.add("to");
		array2.add("test");
		array2.add("the");
		array2.add("changes");
		array2.add("with");

		var array3 = new JsonArray();

		array3.add("test");
		array3.add("Driven");
		array3.add("developement");
		array3.add("I");
		array3.add("am");
		array3.add("using");
		array3.add("eclipse");
		array3.add("I");
		array3.add("to");
		array3.add("test");
		array3.add("the");
		array3.add("changes");
		array3.add("with");
		array3.add("test");
		array3.add("Driven");
		array3.add("developement");

		var array4 = new JsonArray();

		array4.add("test");
		array4.add("Driven");
		array4.add("developement");
		array4.add("I");
		array4.add("am");
		array4.add("using");
		array4.add("eclipse");
		array4.add("I");
		array4.add("to");

		var arr = new JsonArray();
		arr.add(array2);
		arr.add(array4);
		arr.add(array1);
		arr.add(array1);
		arr.add(array3);
		arr.add(array2);
		arr.add(array4);
		arr.add(array1);
		arr.add(array1);
		arr.add(array4);

		var res = new JsonArray();
		res.add(array1);
		res.add(array1);
		res.add(array4);

		FunctionExecutionParameters fep = new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository())
				.setArguments(Map.of(IndexOfArray.PARAMETER_ARRAY_SOURCE.getParameterName(), arr,
						IndexOfArray.PARAMETER_ARRAY_SECOND_SOURCE.getParameterName(), res,
						IndexOfArray.PARAMETER_INT_FIND_FROM.getParameterName(), new JsonPrimitive(1)));

		IndexOfArray ind = new IndexOfArray();

		assertEquals(new JsonPrimitive(7), ind.execute(fep).allResults().get(0).getResult().get("output"));
	}

	@Test
	void test4() {
		IndexOfArray ind = new IndexOfArray();

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

		JsonArray res = new JsonArray();
		res.add('b');
		res.add('e');
		res.add('d');

		FunctionExecutionParameters fep = new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository())
				.setArguments(Map.of(IndexOfArray.PARAMETER_ARRAY_SOURCE.getParameterName(), arr,
						IndexOfArray.PARAMETER_ARRAY_SECOND_SOURCE.getParameterName(), res,
						IndexOfArray.PARAMETER_INT_FIND_FROM.getParameterName(), new JsonPrimitive(4)));

		assertEquals(new JsonPrimitive(-1),
				ind.execute(fep).allResults().get(0).getResult().get(IndexOfArray.EVENT_RESULT_INTEGER.getName()));

	}

	@Test
	void test5() {
		IndexOfArray ind = new IndexOfArray();

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

		FunctionExecutionParameters fep = new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository())
				.setArguments(Map.of(IndexOfArray.PARAMETER_ARRAY_SOURCE.getParameterName(), arr,
						IndexOfArray.PARAMETER_ARRAY_SECOND_SOURCE.getParameterName(), JsonNull.INSTANCE,
						IndexOfArray.PARAMETER_INT_FIND_FROM.getParameterName(), new JsonPrimitive(4)));

		assertThrows(KIRuntimeException.class, () -> ind.execute(fep).allResults().get(0).getResult()
				.get(IndexOfArray.EVENT_RESULT_INTEGER.getName()));

	}

	@Test
	void test6() {
		IndexOfArray ind = new IndexOfArray();

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

		JsonArray as = new JsonArray();
		as.add('c');
		as.add('e');

		FunctionExecutionParameters fep = new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository())
				.setArguments(Map.of(IndexOfArray.PARAMETER_ARRAY_SOURCE.getParameterName(), arr,
						IndexOfArray.PARAMETER_ARRAY_SECOND_SOURCE.getParameterName(), as));

		assertEquals(new JsonPrimitive(6),
				ind.execute(fep).allResults().get(0).getResult().get(IndexOfArray.EVENT_RESULT_INTEGER.getName()));

	}

	@Test
	void test7() {
		IndexOfArray ind = new IndexOfArray();

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

		JsonArray as = new JsonArray();
		as.add('c');
		as.add('e');

		FunctionExecutionParameters fep = new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository())
				.setArguments(Map.of(IndexOfArray.PARAMETER_ARRAY_SOURCE.getParameterName(), arr,
						IndexOfArray.PARAMETER_ARRAY_SECOND_SOURCE.getParameterName(), as,
						IndexOfArray.PARAMETER_INT_FIND_FROM.getParameterName(), new JsonPrimitive(-2)));

		assertThrows(KIRuntimeException.class, () -> ind.execute(fep));
	}

	@Test
	void test8() {
		IndexOfArray ind = new IndexOfArray();

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

		JsonArray as = new JsonArray();
		as.add('c');
		as.add('e');

		FunctionExecutionParameters fep = new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository())
				.setArguments(Map.of(IndexOfArray.PARAMETER_ARRAY_SOURCE.getParameterName(), as,
						IndexOfArray.PARAMETER_ARRAY_SECOND_SOURCE.getParameterName(), arr,
						IndexOfArray.PARAMETER_INT_FIND_FROM.getParameterName(), new JsonPrimitive(0)));

		assertThrows(KIRuntimeException.class, () -> ind.execute(fep));
	}
}
