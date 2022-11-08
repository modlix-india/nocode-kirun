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
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

class AddFirstTest {

	@Test
	void test() {
		var arr = new JsonArray();
		arr.add('c');
		arr.add('p');
		arr.add('i');
		arr.add('e');

		AddFirst ad = new AddFirst();

		var arr1 = new JsonArray();
		arr1.add('a');
		arr1.add('c');
		arr1.add('p');
		arr1.add('i');
		arr1.add('e');

		FunctionExecutionParameters fep = new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository())
				.setArguments(Map.of("source", arr, "elementObject", new JsonPrimitive('a'))).setContext(Map.of())
				.setSteps(Map.of());

		ad.execute(fep);

		assertEquals(arr1, arr);

	}

	@Test
	void test2() {

		AddFirst add = new AddFirst();

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

//		JsonArray res = new JsonArray();
//		res.add('b');
//		res.add('c');
//		res.add('e');
//		res.add('d');

		FunctionExecutionParameters fep = new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository())
				.setArguments(Map.of("source", arr, "elementObject", new JsonPrimitive("surendhar")))
				.setContext(Map.of()).setSteps(Map.of());

		add.execute(fep);

		JsonArray out = new JsonArray();
		out.add("surendhar");
		out.add('a');
		out.add('b');
		out.add('c');
		out.add('d');
		out.add('a');
		out.add('b');
		out.add('c');
		out.add('e');
		out.add('d');

		assertEquals(out, arr);

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

		var obj = new JsonObject();

		obj.add("fname", new JsonPrimitive("surendhar"));

		obj.add("lname", new JsonPrimitive(" s"));

		obj.add("age", new JsonPrimitive(23));

		obj.add("company", new JsonPrimitive("Fincity corporation "));

		var res = new JsonArray();
		res.add(obj);
		res.add(array2);
		res.add(array4);
		res.add(array1);
		res.add(array1);
		res.add(array3);
		res.add(array2);
		res.add(array4);
		res.add(array1);
		res.add(array1);
		res.add(array4);

		FunctionExecutionParameters fep =

				new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository()).setArguments(Map.of("source", arr, "elementObject", obj))
						.setContext(Map.of()).setSteps(Map.of());

		AddFirst add = new AddFirst();
		add.execute(fep);

		assertEquals(res, arr);
	}

	@Test
	void test5() {
		var arr = new JsonArray();
		arr.add('c');
		arr.add('p');
		arr.add('i');
		arr.add('e');

		AddFirst ad = new AddFirst();

		FunctionExecutionParameters fep = new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository())
				.setArguments(Map.of("source", JsonNull.INSTANCE, "elementObject", arr)).setContext(Map.of())
				.setSteps(Map.of());

		assertThrows(KIRuntimeException.class, () -> ad.execute(fep));

	}

	@Test
	void test4() {
		var arr = new JsonArray();
		arr.add('c');
		arr.add('p');
		arr.add('i');
		arr.add('e');

		AddFirst ad = new AddFirst();

		FunctionExecutionParameters fep = new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository())
				.setArguments(Map.of("source", arr, "elementObject", JsonNull.INSTANCE)).setContext(Map.of())
				.setSteps(Map.of());

		assertThrows(KIRuntimeException.class, () -> ad.execute(fep));

//		assertEquals(arr, ad.execute(fep));
	}
}
