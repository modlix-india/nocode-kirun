package com.fincity.nocode.kirun.engine.function.system.array;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.repository.KIRunFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.KIRunSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.FunctionExecutionParameters;
import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;

class EqualsTest {

	@Test
	void test() {

		Equals equals = new Equals();

		JsonArray srcArray = new JsonArray();
		srcArray.add(30);
		srcArray.add(31);
		srcArray.add(32);
		srcArray.add(33);
		srcArray.add(34);

		JsonArray findArray = new JsonArray();

		findArray.add(30);
		findArray.add(31);
		findArray.add(32);
		findArray.add(33);
		findArray.add(34);

		FunctionExecutionParameters fep = new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository());
		fep.setArguments(Map.of(Equals.PARAMETER_ARRAY_SOURCE.getParameterName(), srcArray,
		        Equals.PARAMETER_ARRAY_FIND.getParameterName(), findArray));

		FunctionOutput fo = equals.execute(fep);

		assertEquals(new JsonPrimitive(true), fo.allResults()
		        .get(0)
		        .getResult()
		        .get(Equals.EVENT_RESULT_NAME));

		findArray.set(1, new JsonPrimitive(41));

		fo = equals.execute(fep);

		assertEquals(new JsonPrimitive(false), fo.allResults()
		        .get(0)
		        .getResult()
		        .get(Equals.EVENT_RESULT_NAME));

		fep = new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository());
		fep.setArguments(Map.of(Equals.PARAMETER_ARRAY_SOURCE.getParameterName(), srcArray,
		        Equals.PARAMETER_ARRAY_FIND.getParameterName(), findArray,
		        Equals.PARAMETER_INT_SOURCE_FROM.getParameterName(), new JsonPrimitive(2),
		        Equals.PARAMETER_INT_FIND_FROM.getParameterName(), new JsonPrimitive(2)));
		
		fo = equals.execute(fep);
		
		assertEquals(new JsonPrimitive(true), fo.allResults()
		        .get(0)
		        .getResult()
		        .get(Equals.EVENT_RESULT_NAME));

		srcArray = new JsonArray();
		srcArray.add(true);
		srcArray.add(true);
		srcArray.add(false);

		findArray = new JsonArray();
		findArray.add(true);
		findArray.add(true);
		findArray.add(false);

		fep = new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository());
		fep.setArguments(Map.of(Equals.PARAMETER_ARRAY_SOURCE.getParameterName(), srcArray,
		        Equals.PARAMETER_ARRAY_FIND.getParameterName(), findArray));

		fo = equals.execute(fep);

		assertEquals(new JsonPrimitive(true), fo.allResults()
		        .get(0)
		        .getResult()
		        .get(Equals.EVENT_RESULT_NAME));

	}

}
