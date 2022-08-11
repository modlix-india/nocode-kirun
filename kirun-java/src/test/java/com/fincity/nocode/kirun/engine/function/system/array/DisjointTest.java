package com.fincity.nocode.kirun.engine.function.system.array;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.exception.KIRuntimeException;
import com.fincity.nocode.kirun.engine.json.schema.validator.exception.SchemaValidationException;
import com.fincity.nocode.kirun.engine.runtime.FunctionExecutionParameters;
import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

class DisjointTest {

	@Test
	void test() {

		JsonArray arr1 = new JsonArray();
		arr1.add('a');
		arr1.add('b');
		arr1.add('c');//
		arr1.add('d');
		arr1.add('e');
		arr1.add('f');//

		JsonArray arr2 = new JsonArray();
		arr2.add('a');//
		arr2.add('b');
		arr2.add('p');
		arr2.add('a');//
		arr2.add('f');
		arr2.add('f');
		arr2.add('e');

		Disjoint dis = new Disjoint();

		FunctionExecutionParameters fep = new FunctionExecutionParameters()
				.setArguments(Map.of(Disjoint.PARAMETER_ARRAY_SOURCE.getParameterName(), arr1,
						Disjoint.PARAMETER_INT_SOURCE_FROM.getParameterName(), new JsonPrimitive(2),
						Disjoint.PARAMETER_ARRAY_SECOND_SOURCE.getParameterName(), arr2,
						Disjoint.PARAMETER_INT_SECOND_SOURCE_FROM.getParameterName(), new JsonPrimitive(0),
						Disjoint.PARAMETER_INT_LENGTH.getParameterName(), new JsonPrimitive(4)));

		JsonArray res = new JsonArray();
		res.add('c');
		res.add('d');
		res.add('e');
		res.add('f');
		res.add('a');
		res.add('b');
		res.add('p');

		JsonArray s1 = dis.execute(fep).allResults().get(0).getResult().get("output").getAsJsonArray();

		Set<Object> set1 = new HashSet<>();

		for (int i = 0; i < s1.size(); i++)
			set1.add(s1.get(i));

		Set<Object> set2 = new HashSet<>();

		for (int i = 0; i < res.size(); i++)
			set2.add(res.get(i));

		assertEquals(set2, set1);

	}

	@Test
	void test2() {

		JsonArray arr1 = new JsonArray();
		arr1.add('a');
		arr1.add('b');
		arr1.add('c');
		arr1.add('d');
		arr1.add('e');
		arr1.add('f');

		JsonArray arr2 = new JsonArray();
		arr2.add('a');
		arr2.add('b');
		arr2.add('a');
		arr2.add('b');
		arr2.add('c');
		arr2.add('d');
		arr2.add('e');
		arr2.add('f');

		Disjoint dis = new Disjoint();

		FunctionExecutionParameters fep = new FunctionExecutionParameters()
				.setArguments(Map.of(Disjoint.PARAMETER_ARRAY_SOURCE.getParameterName(), arr1,
						Disjoint.PARAMETER_INT_SOURCE_FROM.getParameterName(), new JsonPrimitive(-12),
						Disjoint.PARAMETER_ARRAY_SECOND_SOURCE.getParameterName(), arr2,
						Disjoint.PARAMETER_INT_SECOND_SOURCE_FROM.getParameterName(), new JsonPrimitive(2),
						Disjoint.PARAMETER_INT_LENGTH.getParameterName(), new JsonPrimitive(3)));

		assertThrows(SchemaValidationException.class, () -> dis.execute(fep));

	}

	@Test
	void test3() {

		JsonArray arr1 = new JsonArray();
		arr1.add('a');
		arr1.add('b');
		arr1.add('c');
		arr1.add('d');
		arr1.add('e');
		arr1.add('f');

		JsonArray arr2 = new JsonArray();
		arr2.add('a');
		arr2.add('b');
		arr2.add('a');
		arr2.add('b');
		arr2.add('c');
		arr2.add('d');
		arr2.add('e');
		arr2.add('f');

		Disjoint dis = new Disjoint();

		FunctionExecutionParameters fep = new FunctionExecutionParameters()
				.setArguments(Map.of(Disjoint.PARAMETER_ARRAY_SOURCE.getParameterName(), arr1,
						Disjoint.PARAMETER_INT_SOURCE_FROM.getParameterName(), new JsonPrimitive(5),
						Disjoint.PARAMETER_ARRAY_SECOND_SOURCE.getParameterName(), arr2,
						Disjoint.PARAMETER_INT_SECOND_SOURCE_FROM.getParameterName(), new JsonPrimitive(2),
						Disjoint.PARAMETER_INT_LENGTH.getParameterName(), new JsonPrimitive(10)));

		assertThrows(KIRuntimeException.class, () -> dis.execute(fep).allResults().get(0).getResult().get("output"));

	}

	@Test
	void test4() {

		JsonArray arr1 = new JsonArray();
		arr1.add('a');
		arr1.add('b');
		arr1.add('c');//
		arr1.add('d');
		arr1.add('e');
		arr1.add('f');//

		JsonArray arr2 = new JsonArray();
		arr2.add('a');//
		arr2.add('b');
		arr2.add('p');
		arr2.add('a');//
		arr2.add('f');
		arr2.add('f');
		arr2.add('e');

		Disjoint dis = new Disjoint();

		FunctionExecutionParameters fep = new FunctionExecutionParameters()
				.setArguments(Map.of(Disjoint.PARAMETER_ARRAY_SOURCE.getParameterName(), arr1,
						Disjoint.PARAMETER_INT_SOURCE_FROM.getParameterName(), new JsonPrimitive(0),
						Disjoint.PARAMETER_ARRAY_SECOND_SOURCE.getParameterName(), arr2,
						Disjoint.PARAMETER_INT_SECOND_SOURCE_FROM.getParameterName(), new JsonPrimitive(5),
						Disjoint.PARAMETER_INT_LENGTH.getParameterName(), new JsonPrimitive(3)));

		assertThrows(KIRuntimeException.class,
				() -> dis.execute(fep).allResults().get(0).getResult().get("output").getAsJsonArray());

	}

	@Test
	void test5() {

		JsonArray arr1 = new JsonArray();
		arr1.add('a');
		arr1.add('b');
		arr1.add('c');//
		arr1.add('d');
		arr1.add('e');
		arr1.add('f');//

		JsonArray arr2 = new JsonArray();
		arr2.add('a');//
		arr2.add('b');
		arr2.add('p');
		arr2.add('a');//
		arr2.add('f');
		arr2.add('f');
		arr2.add('e');

		Disjoint dis = new Disjoint();

		FunctionExecutionParameters fep = new FunctionExecutionParameters()
				.setArguments(Map.of(Disjoint.PARAMETER_ARRAY_SOURCE.getParameterName(), arr1,
						Disjoint.PARAMETER_INT_SOURCE_FROM.getParameterName(), new JsonPrimitive(0),
						Disjoint.PARAMETER_ARRAY_SECOND_SOURCE.getParameterName(), JsonNull.INSTANCE,
						Disjoint.PARAMETER_INT_SECOND_SOURCE_FROM.getParameterName(), new JsonPrimitive(5),
						Disjoint.PARAMETER_INT_LENGTH.getParameterName(), new JsonPrimitive(3)));

		assertThrows(SchemaValidationException.class, () -> dis.execute(fep));
	}

	@Test
	void test7() {

		JsonArray arr1 = new JsonArray();
		arr1.add('a');
		arr1.add('b');
		arr1.add('c');//
		arr1.add('d');
		arr1.add('e');
		arr1.add('f');//

		JsonArray arr2 = new JsonArray();
		arr2.add('a');//
		arr2.add('b');
		arr2.add('p');
		arr2.add('a');//
		arr2.add('f');
		arr2.add('f');
		arr2.add('e');

		Disjoint dis = new Disjoint();

		FunctionExecutionParameters fep = new FunctionExecutionParameters()
				.setArguments(Map.of(Disjoint.PARAMETER_ARRAY_SOURCE.getParameterName(), arr1,
						Disjoint.PARAMETER_INT_SOURCE_FROM.getParameterName(), new JsonPrimitive(0),
						Disjoint.PARAMETER_ARRAY_SECOND_SOURCE.getParameterName(), arr2,
						Disjoint.PARAMETER_INT_SECOND_SOURCE_FROM.getParameterName(), new JsonPrimitive(0),
						Disjoint.PARAMETER_INT_LENGTH.getParameterName(), new JsonPrimitive(6)));

		JsonArray res = new JsonArray();
		res.add('c');
		res.add('d');
		res.add('p');
		res.add('e');

		JsonArray s1 = dis.execute(fep).allResults().get(0).getResult().get("output").getAsJsonArray();

		Set<Object> set1 = new HashSet<>();

		for (int i = 0; i < s1.size(); i++)
			set1.add(s1.get(i));

		Set<Object> set2 = new HashSet<>();

		for (int i = 0; i < res.size(); i++)
			set2.add(res.get(i));

		assertEquals(set2, set1);
	}

	@Test
	void test6() {

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
		js4.addProperty("char", 's');

		var js5 = new JsonObject();

		js5.addProperty("boolean", false);
		js5.add("array", array1);
		js5.addProperty("char", 'b');

		var js6 = new JsonObject();

		js6.addProperty("booleasan", false);
		js6.add("arraay", array1);
		js6.addProperty("char", 'o');

		var js7 = new JsonObject();

		js7.addProperty("boolaadean", false);
		js7.add("arrrray", array1);
		js7.addProperty("char", 'o');

		var arr = new JsonArray();

		arr.add(js1);
		arr.add(js2);
		arr.add(js3);
		arr.add(js4);
		arr.add(js1);

		var arr2 = new JsonArray();
		arr2.add(js5);
		arr2.add(js6);
		arr2.add(js7);
		arr2.add(js1);

		var d = new JsonArray();
		d.add(js2);
		d.add(js3);
		d.add(js4);
		d.add(js5);
		d.add(js6);
		d.add(js7);

		Set<Object> set1 = new HashSet<>();

		for (int i = 0; i < d.size(); i++)
			set1.add(d.get(i));

		FunctionExecutionParameters fep =

				new FunctionExecutionParameters()
						.setArguments(Map.of(Disjoint.PARAMETER_ARRAY_SOURCE.getParameterName(), arr,
								Disjoint.PARAMETER_INT_SOURCE_FROM.getParameterName(), new JsonPrimitive(1),
								Disjoint.PARAMETER_ARRAY_SECOND_SOURCE.getParameterName(), arr2,
								Disjoint.PARAMETER_INT_SECOND_SOURCE_FROM.getParameterName(), new JsonPrimitive(0),
								Disjoint.PARAMETER_INT_LENGTH.getParameterName(), new JsonPrimitive(4)));

		Disjoint dis = new Disjoint();

		var res = dis.execute(fep).allResults().get(0).getResult().get("output").getAsJsonArray();

		Set<Object> set2 = new HashSet<>();

		for (int i = 0; i < res.size(); i++)
			set2.add(res.get(i));

		assertEquals(set1, set2);

	}

}
