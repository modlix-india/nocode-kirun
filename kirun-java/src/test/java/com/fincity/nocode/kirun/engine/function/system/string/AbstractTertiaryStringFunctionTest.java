package com.fincity.nocode.kirun.engine.function.system.string;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.namespaces.Namespaces;
import com.fincity.nocode.kirun.engine.repository.KIRunFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.KIRunSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.FunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

class AbstractTertiaryStringFunctionTest {

	@Test
	void test() {

		StringFunctionRepository stringFunction = new StringFunctionRepository();

		assertEquals(new JsonPrimitive(11),
				stringFunction.find(Namespaces.STRING, "IndexOfWithStartPoint")
						.execute(new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository()).setArguments(Map.of(
								AbstractTertiaryStringFunction.PARAMETER_STRING_NAME,
								new JsonPrimitive(" THIS IS A NOcoDE plATFNORM		"),
								AbstractTertiaryStringFunction.PARAMETER_SECOND_STRING_NAME, new JsonPrimitive("NO"),
								AbstractTertiaryStringFunction.PARAMETER_INDEX_NAME, new JsonPrimitive(10))))
						.allResults().get(0).getResult().get(AbstractTertiaryStringFunction.EVENT_RESULT_NAME));

		assertEquals(new JsonPrimitive(14), stringFunction.find(Namespaces.STRING, "IndexOfWithStartPoint")
				.execute(new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository()).setArguments(Map.of(
						AbstractTertiaryStringFunction.PARAMETER_STRING_NAME, new JsonPrimitive(" fincity compatY "),
						AbstractTertiaryStringFunction.PARAMETER_SECOND_STRING_NAME, new JsonPrimitive("t"),
						AbstractTertiaryStringFunction.PARAMETER_INDEX_NAME, new JsonPrimitive(9))))
				.allResults().get(0).getResult().get(AbstractTertiaryStringFunction.EVENT_RESULT_NAME));

	}

	@Test
	void test2() {

		StringFunctionRepository stringFunction = new StringFunctionRepository();

		String s1 = " THIS IS A NOcoDE plATFNORM		";
		String s2 = " fincity compatY ";

		assertEquals(new JsonPrimitive(6), stringFunction.find(Namespaces.STRING, "LastIndexOfWithStartPoint")
				.execute(new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository()).setArguments(Map.of(
						AbstractTertiaryStringFunction.PARAMETER_STRING_NAME, new JsonPrimitive(s1),
						AbstractTertiaryStringFunction.PARAMETER_SECOND_STRING_NAME, new JsonPrimitive("IS"),
						AbstractTertiaryStringFunction.PARAMETER_INDEX_NAME, new JsonPrimitive(s1.length() - 2))))
				.allResults().get(0).getResult().get(AbstractTertiaryStringFunction.EVENT_RESULT_NAME));

		assertEquals(new JsonPrimitive(14), stringFunction.find(Namespaces.STRING, "LastIndexOfWithStartPoint")
				.execute(new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository()).setArguments(Map.of(
						AbstractTertiaryStringFunction.PARAMETER_STRING_NAME, new JsonPrimitive(s2),
						AbstractTertiaryStringFunction.PARAMETER_SECOND_STRING_NAME, new JsonPrimitive("tY"),
						AbstractTertiaryStringFunction.PARAMETER_INDEX_NAME, new JsonPrimitive(s2.length() - 1))))
				.allResults().get(0).getResult().get(AbstractTertiaryStringFunction.EVENT_RESULT_NAME));

	}

	@Test
	void test3() {

		StringFunctionRepository stringFunction = new StringFunctionRepository();

		String s1 = " THIS IS A NOcoDE plATFNORM";
		String s2 = " fincitY compatY ";

		assertEquals(new JsonPrimitive(" THIS IS A  REPLACED coDE plATFNORM"), stringFunction
				.find(Namespaces.STRING, "ReplaceFirst")
				.execute(new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository()).setArguments(Map.of(
						AbstractTertiaryStringFunction.PARAMETER_STRING_NAME, new JsonPrimitive(s1),
						AbstractTertiaryStringFunction.PARAMETER_SECOND_STRING_NAME, new JsonPrimitive("NO"),
						AbstractTertiaryStringFunction.PARAMETER_THIRD_STRING_NAME, new JsonPrimitive(" REPLACED "))))
				.allResults().get(0).getResult().get(AbstractTertiaryStringFunction.EVENT_RESULT_NAME));

		assertEquals(new JsonPrimitive(" fincinew char compatY "), stringFunction
				.find(Namespaces.STRING, "ReplaceFirst")
				.execute(new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository()).setArguments(Map.of(
						AbstractTertiaryStringFunction.PARAMETER_STRING_NAME, new JsonPrimitive(s2),
						AbstractTertiaryStringFunction.PARAMETER_SECOND_STRING_NAME, new JsonPrimitive("tY"),
						AbstractTertiaryStringFunction.PARAMETER_THIRD_STRING_NAME, new JsonPrimitive("new char"))))
				.allResults().get(0).getResult().get(AbstractTertiaryStringFunction.EVENT_RESULT_NAME));

	}

	@Test
	void test4() {

		StringFunctionRepository stringFunction = new StringFunctionRepository();

		String s1 = " THIS IS A NOcoDE plATFNORM";
		String s2 = " fincitY compatY ";

		assertEquals(new JsonPrimitive(" THIS IS temporary NOcoDE pltemporaryTFNORM"), stringFunction
				.find(Namespaces.STRING, "Replace")
				.execute(new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository()).setArguments(Map.of(
						AbstractTertiaryStringFunction.PARAMETER_STRING_NAME, new JsonPrimitive(s1),
						AbstractTertiaryStringFunction.PARAMETER_SECOND_STRING_NAME, new JsonPrimitive("A"),
						AbstractTertiaryStringFunction.PARAMETER_THIRD_STRING_NAME, new JsonPrimitive("temporary"))))
				.allResults().get(0).getResult().get(AbstractTertiaryStringFunction.EVENT_RESULT_NAME));

		assertEquals(new JsonPrimitive(" fincithankYou compathankYou "), stringFunction
				.find(Namespaces.STRING, "Replace")
				.execute(new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository()).setArguments(Map.of(
						AbstractTertiaryStringFunction.PARAMETER_STRING_NAME, new JsonPrimitive(s2),
						AbstractTertiaryStringFunction.PARAMETER_SECOND_STRING_NAME, new JsonPrimitive("tY"),
						AbstractTertiaryStringFunction.PARAMETER_THIRD_STRING_NAME, new JsonPrimitive("thankYou"))))
				.allResults().get(0).getResult().get(AbstractTertiaryStringFunction.EVENT_RESULT_NAME));

	}
}
