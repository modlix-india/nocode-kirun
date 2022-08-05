package com.fincity.nocode.kirun.engine.function.system.array;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.exception.KIRuntimeException;
import com.fincity.nocode.kirun.engine.json.schema.validator.exception.SchemaValidationException;
import com.fincity.nocode.kirun.engine.runtime.FunctionExecutionParameters;
import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

class DeleteFirstTest {

	@Test
	void test() {
		var arr = new JsonArray();
		arr.add('c');
		arr.add('p');
		arr.add('i');
		arr.add('e');

		DeleteFirst ad = new DeleteFirst();

		var arr1 = new JsonArray();

		arr1.add('p');
		arr1.add('i');
		arr1.add('e');

		FunctionExecutionParameters fep = new FunctionExecutionParameters().setArguments(Map.of("source", arr))
				.setContext(Map.of()).setOutput(Map.of());

		ad.execute(fep);

		assertEquals(arr1, arr);
	}

	@Test
	void test2() {
		var arr = new JsonArray();

		DeleteFirst ad = new DeleteFirst();

		FunctionExecutionParameters fep = new FunctionExecutionParameters().setArguments(Map.of("source", arr))
				.setContext(Map.of()).setOutput(Map.of());

		assertThrows(KIRuntimeException.class, () -> ad.execute(fep));
	}

	@Test
	void test5() {

		var array1 = new JsonArray();

		array1.add("test");
		array1.add("Driven");
		array1.add("developement");
		array1.add("I");
		array1.add("am");

		var js1 = new JsonObject();

		js1.addProperty("boolean", false);
		js1.add("array", array1);
		js1.addProperty("char", 'o');

		var js2 = new JsonObject();

		js2.addProperty("boolean", false);
		js2.add("array", array1);
		js2.addProperty("char", "asd");

		var js3 = new JsonObject();
		js3.add("array", array1);

		var js4 = new JsonObject();

		js4.addProperty("boolean", false);
		js4.add("array", array1);
		js4.addProperty("char", 'o');

		var arr = new JsonArray();

		arr.add(js1);
		arr.add(js2);
		arr.add(js3);
		arr.add(js4);
		arr.add(js1);

		var res = new JsonArray();
		res.add(js2);
		res.add(js3);
		res.add(js4);
		res.add(js1);

		FunctionExecutionParameters fep = new FunctionExecutionParameters().setArguments(Map.of("source", arr))
				.setContext(Map.of()).setOutput(Map.of());

		DeleteFirst del = new DeleteFirst();
		del.execute(fep);

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

		var res = new JsonArray();
		res.add(array3);
		res.add(array2);
		res.add(array4);
		res.add(array1);

		FunctionExecutionParameters fep = new FunctionExecutionParameters().setArguments(Map.of("source", arr))
				.setContext(Map.of()).setOutput(Map.of());

		DeleteFirst freq = new DeleteFirst();
		freq.execute(fep);

		assertEquals(res, arr);

	}

	@Test
	void test6() {
//		var arr = new JsonArray();

		DeleteFirst ad = new DeleteFirst();

		FunctionExecutionParameters fep = new FunctionExecutionParameters()
				.setArguments(Map.of("source", JsonNull.INSTANCE)).setContext(Map.of()).setOutput(Map.of());

		assertThrows(SchemaValidationException.class, () -> ad.execute(fep));
	}
}
