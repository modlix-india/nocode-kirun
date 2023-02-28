package com.fincity.nocode.kirun.engine.function.system;

import static com.fincity.nocode.kirun.engine.namespaces.Namespaces.SYSTEM;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fincity.nocode.kirun.engine.function.AbstractFunction;
import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;
import com.fincity.nocode.kirun.engine.model.Parameter;
import com.fincity.nocode.kirun.engine.runtime.FunctionExecutionParameters;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonPrimitive;

public class Print extends AbstractFunction {

	public static final Logger logger = LoggerFactory.getLogger(Print.class);

	static final String VALUES = "values";

	static final String STREAM = "stream";

	private static final String STDOUT = "STDOUT";
	private static final String DEBUGLOG = "DEBUGLOG";
	private static final String ERRORLOG = "ERRORLOG";
	private static final String STDERR = "STDERR";
	private static final String STD = "STD";

	private static final FunctionSignature SIGNATURE = new FunctionSignature().setName("Print").setNamespace(SYSTEM)
			.setParameters(Map.ofEntries(Parameter.ofEntry(VALUES, Schema.ofAny(VALUES), true),
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
	protected FunctionOutput internalExecute(FunctionExecutionParameters context) {

		var values = context.getArguments().get(VALUES);

		var stream = context.getArguments().get(STREAM).getAsString();

		Gson gson = new GsonBuilder().setPrettyPrinting().create();

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
