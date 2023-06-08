package com.fincity.nocode.kirun.engine.function.system.array;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

class ShuffleTest {

	@Test
	void test() {
		var arr = new JsonArray();

		ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
				new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
				.setArguments(Map.of("source", arr))
				.setContext(Map.of()).setSteps(Map.of());

		Shuffle sf = new Shuffle();

		sf.execute(fep).block();

		assertEquals(new JsonArray(), arr);
	}

	@Test
	void test2() {
		var arr = new JsonArray();
		arr.add("surendhar");

		ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
				new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
				.setArguments(Map.of("source", arr))
				.setContext(Map.of()).setSteps(Map.of());

		Shuffle sf = new Shuffle();

		sf.execute(fep).block();

		var res = new JsonArray();
		res.add("surendhar");

		assertEquals(res, arr);
	}

	@Test
	void test3() {
		var arr = new JsonArray();
		arr.add(1);
		arr.add(3);
		arr.add(2);
		arr.add(5);
		arr.add(5);

		var res = new JsonArray();
		res.addAll(arr.deepCopy());

		Set<Integer> set1 = new HashSet<>();
		for (int i = 0; i < arr.size(); i++)
			set1.add(res.get(i).getAsInt());

		ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
				new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
				.setArguments(Map.of("source", arr))
				.setContext(Map.of()).setSteps(Map.of());

		Shuffle sf = new Shuffle();

		sf.execute(fep).block();

		Set<Integer> set2 = new HashSet<>();
		for (int i = 0; i < arr.size(); i++)
			set2.add(arr.get(i).getAsInt());

		assertEquals(set2, set1);
	}

	@Test
	void test7() {
		var array = new JsonArray();

		array.add("test");
		array.add("Driven");
		array.add("developement");
		array.add("I");
		array.add("am");
		array.add("using");
		array.add("eclipse");
		array.add("I");
		array.add("to");
		array.add("test");
		array.add("the");
		array.add("changes");
		array.add("with");
		array.add("test");
		array.add("Driven");
		array.add("developement");

		var res = new JsonArray();
		res.addAll(array.deepCopy());

		Set<String> set1 = new HashSet<>();
		for (int i = 0; i < array.size(); i++)
			set1.add(res.get(i).getAsString());

		ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
				new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
				.setArguments(Map.of("source", array))
				.setContext(Map.of()).setSteps(Map.of());

		Shuffle suf = new Shuffle();
		suf.execute(fep).block();

		Set<String> set2 = new HashSet<>();
		for (int i = 0; i < array.size(); i++)
			set2.add(array.get(i).getAsString());

		assertEquals(set1, set2);

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
		res.addAll(arr.deepCopy());

		Set<JsonObject> set1 = new HashSet<>();
		for (int i = 0; i < res.size(); i++)
			set1.add(res.get(i).getAsJsonObject());

		ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
				new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
				.setArguments(Map.of("source", arr))
				.setContext(Map.of()).setSteps(Map.of());

		Shuffle freq = new Shuffle();

		freq.execute(fep).block();

		Set<JsonObject> set2 = new HashSet<>();
		for (int i = 0; i < arr.size(); i++)
			set2.add(arr.get(i).getAsJsonObject());

		assertEquals(set1, set2);

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

		var js1 = new JsonObject();

		js1.addProperty("boolean", false);
		js1.add("array", array1);
		js1.addProperty("char", 'o');

		var arr = new JsonArray();
		arr.add(array1);
		arr.add(array3);
		arr.add(array2);
		arr.add(array4);
		arr.add(array1);
		arr.add(js1);

		var res = new JsonArray();
		res.addAll(arr.deepCopy());

		Set<Object> set1 = new HashSet<>();
		for (int i = 0; i < res.size(); i++)
			set1.add(res.get(i));

		ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
				new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
				.setArguments(Map.of("source", arr))
				.setContext(Map.of()).setSteps(Map.of());

		Shuffle suf = new Shuffle();

		suf.execute(fep).block();

		Set<Object> set2 = new HashSet<>();
		for (int i = 0; i < arr.size(); i++)
			set2.add(arr.get(i));

		System.out.println(set1);
		System.out.println(set2);
		assertEquals(set1, set2);
	}

}
