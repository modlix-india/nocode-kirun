package com.fincity.nocode.kirun.engine.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fincity.nocode.kirun.engine.HybridRepository;
import com.fincity.nocode.kirun.engine.Repository;
import com.fincity.nocode.kirun.engine.function.AbstractReactiveFunction;
import com.fincity.nocode.kirun.engine.function.Function;
import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.json.schema.type.Type;
import com.fincity.nocode.kirun.engine.json.schema.type.Type.SchemaTypeAdapter;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionDefinition;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;
import com.fincity.nocode.kirun.engine.model.Parameter;
import com.fincity.nocode.kirun.engine.repository.KIRunFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.KIRunSchemaRepository;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonPrimitive;

class Print extends AbstractReactiveFunction {

	public static final Logger logger = LoggerFactory.getLogger(Print.class);

	static final String VALUES = "values";

	static final String STREAM = "stream";

	private static final String STDOUT = "STDOUT";
	private static final String DEBUGLOG = "DEBUGLOG";
	private static final String ERRORLOG = "ERRORLOG";
	private static final String STDERR = "STDERR";
	private static final String STD = "STD";

	private static final FunctionSignature SIGNATURE = new FunctionSignature().setName("Print").setNamespace("")
			.setParameters(Map.ofEntries(
					
					Parameter.ofEntry(VALUES, Schema.ofAny(VALUES), true),
					
					Parameter.ofEntry(VALUES+"Pick1", Schema.ofString(VALUES+"Pick1").setDefaultValue(new JsonPrimitive(""))),
					
					Parameter.ofEntry(VALUES+"Pick2", Schema.ofBoolean(VALUES+"Pick2").setDefaultValue(new JsonPrimitive(false)), true),
					
					
					Parameter.ofEntry(STREAM,
							Schema.ofString(STREAM)
									.setEnums(List.of(new JsonPrimitive(STDOUT), new JsonPrimitive(DEBUGLOG),
											new JsonPrimitive(ERRORLOG), new JsonPrimitive(STDERR)))
									.setDefaultValue(new JsonPrimitive(STDOUT)))));

	@Override
	public FunctionSignature getSignature() {
		return SIGNATURE;
	}

	@Override
	protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {

		var values = context.getArguments().get(VALUES).getAsJsonArray().deepCopy();

		var stream = context.getArguments().get(STREAM).getAsString();
		
		var pick1 = context.getArguments().get(VALUES+"Pick1").getAsString();
		var pick2 = context.getArguments().get(VALUES+"Pick2").getAsBoolean();

		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		
		values.add(pick1);
		values.add(pick2);

		for (var value : values.getAsJsonArray()) {

			String stringValue = gson.toJson(value);

			if (stream.startsWith(STD))
				(stream.equals(STDOUT) ? System.out : System.err).println(stringValue); // NOSONAR
			// Have to ignore sonar to get the access to stdout and stderr streams.
			else if (stream.equals(DEBUGLOG))
				logger.debug(stringValue);
			else
				logger.error(stringValue);
		}

		return new FunctionOutput(List.of(EventResult.outputOf(Map.of())));
	}
}

class KIRuntimeNoValuesInParameterMapTest {

    @Test
    void test() {

        Gson gson = new GsonBuilder().registerTypeAdapter(Type.class, new SchemaTypeAdapter())
                .create();

        var first = new KIRuntime(gson.fromJson(
                """

                                        {
                        "name": "Make an error",
                        "namespace": "UIApp",
                        "steps": {
                        "print": {
                        "statementName": "print",
                        "namespace": "function",
                        "name": "test",
                        "parameterMap": {
                        }
                        }
                        }
                        }""", FunctionDefinition.class));

        Print printMethod = new Print();

        class InternalRepository implements Repository<Function> {

            @Override
            public Function find(String namespace, String name) {
                if ("function".equals(namespace))
                    return printMethod;
                return null;
            }

            @Override
            public List<String> filter(String name) {
                return List.of();
            }
        }

        var repo = new HybridRepository<>(new KIRunFunctionRepository(), new InternalRepository());

        var results = first
                .execute(new FunctionExecutionParameters(repo, new KIRunSchemaRepository()).setArguments(Map.of()));

        var res = new ArrayList<>();
        assertEquals(res, results.allResults());

    }

}
