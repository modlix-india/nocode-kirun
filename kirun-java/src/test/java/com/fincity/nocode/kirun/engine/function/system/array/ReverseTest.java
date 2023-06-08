package com.fincity.nocode.kirun.engine.function.system.array;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;

class ReverseTest {

	@Test
	void testInternalExecute() {

		Reverse rev = new Reverse();

		ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
				new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository());

		JsonArray source = new JsonArray();
		source.add(4);
		source.add(5);
		source.add(6);
		source.add(7);

		JsonArray res = new JsonArray();
		res.add(5);
		res.add(4);
		res.add(6);
		res.add(7);

		fep.setArguments(Map.of(Reverse.PARAMETER_ARRAY_SOURCE.getParameterName(), source,
				Reverse.PARAMETER_INT_SOURCE_FROM.getParameterName(), new JsonPrimitive(0),
				Reverse.PARAMETER_INT_LENGTH.getParameterName(), new JsonPrimitive(2))).setContext(Map.of())
				.setSteps(Map.of());

		rev.execute(fep).block();

		assertEquals(res, source);
	}

	@Test
	void test2() {

		Reverse rev = new Reverse();

		JsonArray arr = new JsonArray();
		arr.add('a');
		arr.add('b');
		arr.add('c');//
		arr.add('d');
		arr.add('a');
		arr.add('b');
		arr.add('c');//
		arr.add('d');

		ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
				new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
				.setArguments(Map.of(Reverse.PARAMETER_ARRAY_SOURCE.getParameterName(), arr,
						Reverse.PARAMETER_INT_SOURCE_FROM.getParameterName(), new JsonPrimitive(2),
						Reverse.PARAMETER_INT_LENGTH.getParameterName(), new JsonPrimitive(6)))
				.setContext(Map.of()).setSteps(Map.of());

		rev.execute(fep).block();

		JsonArray res = new JsonArray();
		res.add('a'); // 0
		res.add('b'); // 1
		res.add('d'); // 2
		res.add('c'); // 3
		res.add('b'); // 4
		res.add('a'); // 5
		res.add('d'); // 6
		res.add('c'); // 7

		assertEquals(res, arr);

	}

	@Test
	void test4() {

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
		arr.add(array1);
		arr.add(array3);
		arr.add(array2);
		arr.add(array4);
		arr.add(array1);

		ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
				new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
				.setArguments(Map.of(Reverse.PARAMETER_ARRAY_SOURCE.getParameterName(), arr,
						Reverse.PARAMETER_INT_SOURCE_FROM.getParameterName(), new JsonPrimitive(1),
						Reverse.PARAMETER_INT_LENGTH.getParameterName(), new JsonPrimitive(arr.size() - 1)))
				.setContext(Map.of()).setSteps(Map.of());

		var res = new JsonArray();
		res.add(array1);
		res.add(array1);
		res.add(array4);
		res.add(array2);
		res.add(array3);

		Reverse rev = new Reverse();

		rev.execute(fep).block();

		assertEquals(res, arr);
	}

	@Test
	void test5() {

		Reverse rev = new Reverse();

		JsonArray arr = new JsonArray();
		arr.add('a');
		arr.add('b');
		arr.add('a');
		arr.add('c');
		arr.add('d');
		arr.add('a');
		arr.add('b');
		arr.add('c');
		arr.add('d');

		ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
				new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
				.setArguments(Map.of(Reverse.PARAMETER_ARRAY_SOURCE.getParameterName(), arr,
						Reverse.PARAMETER_INT_SOURCE_FROM.getParameterName(), new JsonPrimitive(2)))
				.setContext(Map.of()).setSteps(Map.of());

		rev.execute(fep).block();

		JsonArray res = new JsonArray();
		res.add('a');
		res.add('b');
		res.add('d');
		res.add('c');
		res.add('b');
		res.add('a');
		res.add('d');
		res.add('c');
		res.add('a');

		assertEquals(res, arr);
	}
}
