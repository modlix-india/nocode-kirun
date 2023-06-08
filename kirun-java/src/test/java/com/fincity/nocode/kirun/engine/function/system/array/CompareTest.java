package com.fincity.nocode.kirun.engine.function.system.array;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;

import reactor.test.StepVerifier;

class CompareTest {

	@Test
	void testInternalExecute() {

		Compare compare = new Compare();

		ReactiveFunctionExecutionParameters fep = new ReactiveFunctionExecutionParameters(
				new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository());

		JsonArray source = new JsonArray();
		source.add(4);
		source.add(5);

		JsonArray find = new JsonArray();
		find.add(4);
		find.add(6);

		fep.setArguments(Map.of(Compare.PARAMETER_ARRAY_SOURCE.getParameterName(), source,
				Compare.PARAMETER_ARRAY_FIND.getParameterName(), find));

		StepVerifier.create(compare.execute(fep))
				.expectNextMatches(result -> result.next().getResult()
						.get(Equals.EVENT_RESULT_NAME)
						.equals(new JsonPrimitive(Integer.compare(5, 6))))
				.verifyComplete();
	}

	@Test
	void testCompare() {

		Compare compare = new Compare();

		JsonElement[] source = new JsonElement[] { new JsonPrimitive(2), new JsonPrimitive(2), new JsonPrimitive(3),
				new JsonPrimitive(4), new JsonPrimitive(5) };

		JsonElement[] find = new JsonElement[] { new JsonPrimitive(2d), new JsonPrimitive(2d), new JsonPrimitive(2d),
				new JsonPrimitive(3d), new JsonPrimitive(4d), new JsonPrimitive(5d) };

		assertEquals(0, compare.compare(source, 0, 2, find, 1, 3));

		find = new JsonElement[] { new JsonPrimitive(2d), new JsonPrimitive(2d), new JsonPrimitive(3d),
				new JsonPrimitive(4d), new JsonPrimitive(5d) };

		assertEquals(0, compare.compare(source, 0, source.length, find, 0, find.length));

		source = new JsonElement[] { new JsonPrimitive(true), new JsonPrimitive(true) };

		find = new JsonElement[] { new JsonPrimitive(true), null };

		assertEquals(1, compare.compare(source, 0, source.length, find, 0, find.length));

		source = new JsonElement[] { new JsonPrimitive(true), JsonNull.INSTANCE };

		assertEquals(0, compare.compare(source, 0, source.length, find, 0, find.length));
	}

	@Test
	void testCompareTo() {

		Compare compare = new Compare();

		assertEquals(2 - 3, compare.compareTo(2, 3));
		assertEquals(2.0d - 3, compare.compareTo(2.0d, 3));
		assertEquals(Float.compare(30f, 3f), compare.compareTo(30, 3.0f));
	}

}
