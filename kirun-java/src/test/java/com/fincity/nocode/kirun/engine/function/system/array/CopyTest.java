package com.fincity.nocode.kirun.engine.function.system.array;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.exception.KIRuntimeException;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.repository.KIRunFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.KIRunSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.FunctionExecutionParameters;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

class CopyTest {

	@Test
	void test() {
		Copy copy = new Copy();

		JsonArray source = new JsonArray();

		source.add(1);
		source.add(2);
		source.add(3);
		source.add(4);
		source.add(5);

		final FunctionExecutionParameters fep1 = new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository())
		        .setArguments(Map.of(Copy.PARAMETER_ARRAY_SOURCE.getParameterName(), source,
		                Copy.PARAMETER_INT_SOURCE_FROM.getParameterName(), new JsonPrimitive(2),
		                Copy.PARAMETER_INT_LENGTH.getParameterName(), new JsonPrimitive(4)));

		assertThrows(KIRuntimeException.class, () -> copy.execute(fep1));

		source.add(6);

		JsonArray result = new JsonArray();
		result.add(3);
		result.add(4);
		result.add(5);
		result.add(6);

		FunctionExecutionParameters fep = new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository())
		        .setArguments(Map.of(Copy.PARAMETER_ARRAY_SOURCE.getParameterName(), source,
		                Copy.PARAMETER_INT_SOURCE_FROM.getParameterName(), new JsonPrimitive(2),
		                Copy.PARAMETER_INT_LENGTH.getParameterName(), new JsonPrimitive(4)));

		FunctionOutput fo = copy.execute(fep);

		assertEquals(result, fo.allResults()
		        .get(0)
		        .getResult()
		        .get(Copy.EVENT_RESULT_NAME));

		source = new JsonArray();

		JsonObject obj = new JsonObject();
		obj.addProperty("name", "Kiran");
		source.add(obj);

		obj = new JsonObject();
		obj.addProperty("name", "Kumar");
		source.add(obj);

		fep = new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository()).setArguments(Map.of(Copy.PARAMETER_ARRAY_SOURCE.getParameterName(),
		        source, Copy.PARAMETER_INT_SOURCE_FROM.getParameterName(), new JsonPrimitive(2),
		        Copy.PARAMETER_INT_LENGTH.getParameterName(), new JsonPrimitive(4)));

		result = new JsonArray();

		obj = new JsonObject();
		obj.addProperty("name", "Kiran");
		result.add(obj);

		obj = new JsonObject();
		obj.addProperty("name", "Kumar");
		result.add(obj);

		fep = new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository())
		        .setArguments(Map.of(Copy.PARAMETER_ARRAY_SOURCE.getParameterName(), source));

		fo = copy.execute(fep);

		assertEquals(result, fo.allResults()
		        .get(0)
		        .getResult()
		        .get(Copy.EVENT_RESULT_NAME));

		assertNotSame(source.get(0), result.get(0));

		fep = new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository()).setArguments(Map.of(Copy.PARAMETER_ARRAY_SOURCE.getParameterName(),
		        source, Copy.PARAMETER_BOOLEAN_DEEP_COPY.getParameterName(), new JsonPrimitive(false)));

		fo = copy.execute(fep);

		result = fo.allResults()
		        .get(0)
		        .getResult()
		        .get(Copy.EVENT_RESULT_NAME)
		        .getAsJsonArray();

		assertSame(source.get(0), result.get(0));
	}

}
