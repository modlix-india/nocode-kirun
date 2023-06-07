package com.fincity.nocode.kirun.engine.function.system.string;

import java.util.List;
import java.util.Map;

import com.fincity.nocode.kirun.engine.function.reactive.AbstractReactiveFunction;
import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.model.Event;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;
import com.fincity.nocode.kirun.engine.model.Parameter;
import com.fincity.nocode.kirun.engine.namespaces.Namespaces;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

import reactor.core.publisher.Mono;

public class RegionMatches extends AbstractReactiveFunction {

	protected static final String PARAMETER_STRING_NAME = "string";

	protected static final String PARAMETER_BOOLEAN_NAME = "boolean";

	protected static final String PARAMETER_FIRST_OFFSET_NAME = "firstOffset";

	protected static final String PARAMETER_OTHER_STRING_NAME = "otherString";

	protected static final String PARAMETER_SECOND_OFFSET_NAME = "secondOffset";

	protected static final String PARAMETER_INTEGER_NAME = "length";

	protected static final String EVENT_RESULT_NAME = "result";

	protected static final Parameter PARAMETER_STRING = new Parameter().setParameterName(PARAMETER_STRING_NAME)
	        .setSchema(Schema.ofString(PARAMETER_STRING_NAME));

	protected static final Parameter PARAMETER_OTHER_STRING = new Parameter()
	        .setParameterName(PARAMETER_OTHER_STRING_NAME)
	        .setSchema(Schema.ofString(PARAMETER_OTHER_STRING_NAME));

	protected static final Parameter PARAMETER_FIRST_OFFSET = new Parameter()
	        .setParameterName(PARAMETER_FIRST_OFFSET_NAME)
	        .setSchema(Schema.ofInteger(PARAMETER_FIRST_OFFSET_NAME));

	protected static final Parameter PARAMETER_SECOND_OFFSET = new Parameter()
	        .setParameterName(PARAMETER_SECOND_OFFSET_NAME)
	        .setSchema(Schema.ofInteger(PARAMETER_SECOND_OFFSET_NAME));

	protected static final Parameter PARAMETER_INTEGER = new Parameter().setParameterName(PARAMETER_INTEGER_NAME)
	        .setSchema(Schema.ofInteger(PARAMETER_INTEGER_NAME));

	protected static final Parameter PARAMETER_BOOLEAN = new Parameter().setParameterName(PARAMETER_BOOLEAN_NAME)
	        .setSchema(Schema.ofBoolean(PARAMETER_BOOLEAN_NAME));

	protected static final Event EVENT_BOOLEAN = new Event().setName(Event.OUTPUT)
	        .setParameters(Map.of(EVENT_RESULT_NAME, Schema.ofBoolean(EVENT_RESULT_NAME)));

	private final FunctionSignature signature = new FunctionSignature().setName("RegionMatches")
	        .setNamespace(Namespaces.STRING)
	        .setParameters(Map.of(PARAMETER_STRING.getParameterName(), PARAMETER_STRING,
	                PARAMETER_BOOLEAN.getParameterName(), PARAMETER_BOOLEAN, PARAMETER_FIRST_OFFSET.getParameterName(),
	                PARAMETER_FIRST_OFFSET, PARAMETER_OTHER_STRING.getParameterName(), PARAMETER_OTHER_STRING,
	                PARAMETER_SECOND_OFFSET.getParameterName(), PARAMETER_SECOND_OFFSET,
	                PARAMETER_INTEGER.getParameterName(), PARAMETER_INTEGER))
	        .setEvents(Map.of(EVENT_BOOLEAN.getName(), EVENT_BOOLEAN));

	@Override
	public FunctionSignature getSignature() {
		return signature;
	}

	@Override
	protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {

		String inputString = context.getArguments()
		        .get(PARAMETER_STRING_NAME)
		        .getAsString();
		Boolean ignoreCase = context.getArguments()
		        .get(PARAMETER_BOOLEAN_NAME)
		        .getAsBoolean();
		Integer toffSet = context.getArguments()
		        .get(PARAMETER_FIRST_OFFSET_NAME)
		        .getAsInt();
		String otherString = context.getArguments()
		        .get(PARAMETER_OTHER_STRING_NAME)
		        .getAsString();
		Integer oOffSet = context.getArguments()
		        .get(PARAMETER_SECOND_OFFSET_NAME)
		        .getAsInt();
		Integer length = context.getArguments()
		        .get(PARAMETER_INTEGER_NAME)
		        .getAsInt();

		Boolean matches = inputString.regionMatches(ignoreCase, toffSet, otherString, oOffSet, length);

		return Mono.just(new FunctionOutput(
		        List.of(EventResult.outputOf(Map.of(EVENT_RESULT_NAME, new JsonPrimitive(matches))))));
	}
}
