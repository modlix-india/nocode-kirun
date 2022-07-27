package com.fincity.nocode.kirun.engine.function.system.string;

import java.util.List;
import java.util.Map;

import com.fincity.nocode.kirun.engine.function.AbstractFunction;
import com.fincity.nocode.kirun.engine.function.Function;
import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.model.Event;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;
import com.fincity.nocode.kirun.engine.model.Parameter;
import com.fincity.nocode.kirun.engine.namespaces.Namespaces;
import com.fincity.nocode.kirun.engine.runtime.FunctionExecutionParameters;
import com.fincity.nocode.kirun.engine.util.stream.TriFunction;
import com.google.gson.JsonPrimitive;

public abstract class AbstractTertiaryStringFunction extends AbstractFunction {

	protected static final String PARAMETER_STRING_NAME = "string";

	protected static final String PARAMETER_SECOND_STRING_NAME = "secondString";

	protected static final String PARAMETER_THIRD_STRING_NAME = "thirdString";

	protected static final String PARAMETER_INDEX_NAME = "index";

	protected static final String PARAMETER_SECOND_INDEX_NAME = "secondIndex";

	protected static final String EVENT_RESULT_NAME = "result";

	protected static final Parameter PARAMETER_STRING = new Parameter().setParameterName(PARAMETER_STRING_NAME)
			.setSchema(Schema.ofString(PARAMETER_STRING_NAME));

	protected static final Parameter PARAMETER_SECOND_STRING = new Parameter()
			.setParameterName(PARAMETER_SECOND_STRING_NAME).setSchema(Schema.ofString(PARAMETER_SECOND_STRING_NAME));

	protected static final Parameter PARAMETER_THIRD_STRING = new Parameter()
			.setParameterName(PARAMETER_THIRD_STRING_NAME).setSchema(Schema.ofString(PARAMETER_THIRD_STRING_NAME));

	protected static final Parameter PARAMETER_INDEX = new Parameter().setParameterName(PARAMETER_INDEX_NAME)
			.setSchema(Schema.ofInteger(PARAMETER_INDEX_NAME));

	protected static final Parameter PARAMETER_SECOND_INDEX = new Parameter()
			.setParameterName(PARAMETER_SECOND_INDEX_NAME).setSchema(Schema.ofInteger(PARAMETER_SECOND_INDEX_NAME));

	protected static final Event EVENT_STRING = new Event().setName(Event.OUTPUT)
			.setParameters(Map.of(EVENT_RESULT_NAME, Schema.ofString(EVENT_RESULT_NAME)));

	protected static final Event EVENT_BOOLEAN = new Event().setName(Event.OUTPUT)
			.setParameters(Map.of(EVENT_RESULT_NAME, Schema.ofBoolean(EVENT_RESULT_NAME)));

	protected static final Event EVENT_INT = new Event().setName(Event.OUTPUT)
			.setParameters(Map.of(EVENT_RESULT_NAME, Schema.ofInteger(EVENT_RESULT_NAME)));

	protected static final Event EVENT_ARRAY = new Event().setName(Event.OUTPUT)
			.setParameters(Map.of(EVENT_RESULT_NAME, Schema.ofArray(EVENT_RESULT_NAME)));

	private final FunctionSignature signature;

	protected AbstractTertiaryStringFunction(String nameSpaceType, String functionName, Parameter parameter1,
			Parameter parameter2, Parameter parameter3, Event outputEvent) {

		this.signature = new FunctionSignature().setName(functionName).setNamespace(nameSpaceType)
				.setParameters(Map.of(parameter1.getParameterName(), parameter1, parameter2.getParameterName(),
						parameter2, parameter3.getParameterName(), parameter3))
				.setEvents(Map.of(outputEvent.getName(), outputEvent));
	}

	@Override
	public FunctionSignature getSignature() {
		return signature;
	}

	public static Map.Entry<String, Function> ofEntryAsString(final String name,
			TriFunction<String, String, String, String> function) {
		return Map.entry(name, new AbstractTertiaryStringFunction(Namespaces.STRING, name, PARAMETER_STRING,
				PARAMETER_SECOND_STRING, PARAMETER_THIRD_STRING, EVENT_STRING) {

			@Override
			protected FunctionOutput internalExecute(FunctionExecutionParameters context) {
				JsonPrimitive s1 = context.getArguments().get(PARAMETER_STRING_NAME).getAsJsonPrimitive();
				JsonPrimitive oldString = context.getArguments().get(PARAMETER_SECOND_STRING_NAME).getAsJsonPrimitive();
				JsonPrimitive newString = context.getArguments()
						.get(AbstractTertiaryStringFunction.PARAMETER_THIRD_STRING_NAME).getAsJsonPrimitive();

				return new FunctionOutput(List.of(EventResult.outputOf(Map.of(EVENT_RESULT_NAME, new JsonPrimitive(
						function.apply(s1.getAsString(), oldString.getAsString(), newString.getAsString()))))));
			}

		});
	}

	public static Map.Entry<String, Function> ofEntryAsStringAndSubStringOutput(final String name,
			TriFunction<String, Integer, Integer, String> function) {
		return Map.entry(name, new AbstractTertiaryStringFunction(Namespaces.STRING, name, PARAMETER_STRING,
				PARAMETER_INDEX, PARAMETER_SECOND_INDEX, EVENT_STRING) {

			@Override
			protected FunctionOutput internalExecute(FunctionExecutionParameters context) {
				JsonPrimitive string = context.getArguments().get(PARAMETER_STRING_NAME).getAsJsonPrimitive();
				JsonPrimitive start = context.getArguments().get(PARAMETER_INDEX_NAME).getAsJsonPrimitive();
				JsonPrimitive length = context.getArguments().get(PARAMETER_SECOND_INDEX_NAME).getAsJsonPrimitive();
				return new FunctionOutput(List.of(EventResult.outputOf(Map.of(EVENT_RESULT_NAME, new JsonPrimitive(
						function.apply(string.getAsString(), start.getAsInt(), length.getAsInt()))))));
			}

		});

	}

	public static Map.Entry<String, Function> ofEntryAsStringIntegerOutput(final String name,
			TriFunction<String, String, Integer, Integer> function) {
		return Map.entry(name, new AbstractTertiaryStringFunction(Namespaces.STRING, name, PARAMETER_STRING,
				PARAMETER_SECOND_STRING, PARAMETER_INDEX, EVENT_INT) {

			@Override
			protected FunctionOutput internalExecute(FunctionExecutionParameters context) {
				JsonPrimitive s1 = context.getArguments().get(PARAMETER_STRING_NAME).getAsJsonPrimitive();
				JsonPrimitive s2 = context.getArguments().get(PARAMETER_SECOND_STRING_NAME).getAsJsonPrimitive();
				JsonPrimitive index = context.getArguments().get(PARAMETER_INDEX_NAME).getAsJsonPrimitive();

				return new FunctionOutput(List.of(EventResult.outputOf(Map.of(EVENT_RESULT_NAME,
						new JsonPrimitive(function.apply(s1.getAsString(), s2.getAsString(), index.getAsInt()))))));
			}

		});
	}

}
