package com.fincity.nocode.kirun.engine.function.system.string;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

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

import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;

public abstract class AbstractBinaryStringFunction extends AbstractFunction {

	protected static final String PARAMETER_STRING_NAME = "string";

	protected static final String PARAMETER_SEARCH_STRING_NAME = "searchString";

	protected static final String PARAMETER_INDEX_NAME = "index";

	protected static final String EVENT_RESULT_NAME = "result";

	protected static final Parameter PARAMETER_STRING = new Parameter().setParameterName(PARAMETER_STRING_NAME)
			.setSchema(Schema.ofString(PARAMETER_STRING_NAME));

	protected static final Parameter PARAMETER_INDEX = new Parameter().setParameterName(PARAMETER_INDEX_NAME)
			.setSchema(Schema.ofInteger(PARAMETER_INDEX_NAME));

	protected static final Parameter PARAMETER_SEARCH_STRING = new Parameter()
			.setParameterName(PARAMETER_SEARCH_STRING_NAME).setSchema(Schema.ofString(PARAMETER_STRING_NAME));

	protected static final Event EVENT_STRING = new Event().setName(Event.OUTPUT)
			.setParameters(Map.of(EVENT_RESULT_NAME, Schema.ofString(EVENT_RESULT_NAME)));

	protected static final Event EVENT_BOOLEAN = new Event().setName(Event.OUTPUT)
			.setParameters(Map.of(EVENT_RESULT_NAME, Schema.ofBoolean(EVENT_RESULT_NAME)));

	protected static final Event EVENT_INT = new Event().setName(Event.OUTPUT)
			.setParameters(Map.of(EVENT_RESULT_NAME, Schema.ofInteger(EVENT_RESULT_NAME)));

	protected static final Event EVENT_ARRAY = new Event().setName(Event.OUTPUT)
			.setParameters(Map.of(EVENT_RESULT_NAME, Schema.ofArray(EVENT_RESULT_NAME)));

	private final FunctionSignature signature;

	protected AbstractBinaryStringFunction(String namespace, String functionName, Parameter parameter1,
			Parameter parameter2, Event event) {

		this.signature = new FunctionSignature().setName(functionName).setNamespace(namespace)
				.setParameters(
						Map.of(parameter1.getParameterName(), parameter1, parameter2.getParameterName(), parameter2))
				.setEvents(Map.of(event.getName(), event));
	}

	@Override
	public FunctionSignature getSignature() {
		return this.signature;
	}

	public static Map.Entry<String, Function> ofEntryAsStringBooleanOutput(final String name,
			BiPredicate<String, String> function) {

		return Map.entry(name, new AbstractBinaryStringFunction(Namespaces.STRING, name, PARAMETER_STRING,
				PARAMETER_SEARCH_STRING, EVENT_BOOLEAN) {

			@Override
			protected FunctionOutput internalExecute(FunctionExecutionParameters context) {

				JsonPrimitive s = context.getArguments().get(PARAMETER_STRING_NAME).getAsJsonPrimitive();
				JsonPrimitive ss = context.getArguments().get(PARAMETER_SEARCH_STRING_NAME).getAsJsonPrimitive();

				return new FunctionOutput(List.of(EventResult.outputOf(Map.of(EVENT_RESULT_NAME,
						new JsonPrimitive(function.test(s.getAsString(), ss.getAsString()))))));
			}

		});
	}

	public static Map.Entry<String, Function> ofEntryAsStringAndIntegerStringOutput(final String name,
			BiFunction<String, Integer, String> function) {
		return Map.entry(name, new AbstractBinaryStringFunction(Namespaces.STRING, name, PARAMETER_STRING,
				PARAMETER_INDEX, EVENT_STRING) {

			@Override
			protected FunctionOutput internalExecute(FunctionExecutionParameters context) {
				JsonPrimitive s = context.getArguments().get(PARAMETER_STRING_NAME).getAsJsonPrimitive();
				JsonPrimitive count = context.getArguments().get(PARAMETER_INDEX_NAME).getAsJsonPrimitive();

				return new FunctionOutput(List.of(EventResult.outputOf(Map.of(EVENT_RESULT_NAME,
						new JsonPrimitive(function.apply(s.getAsString(), count.getAsInt()))))));
			}
		});

	}

	public static Map.Entry<String, Function> ofEntryAsStringIntegerOutput(final String name,
			BiFunction<String, String, Integer> function) {
		return Map.entry(name, new AbstractBinaryStringFunction(Namespaces.STRING, name, PARAMETER_STRING,
				PARAMETER_SEARCH_STRING, EVENT_INT) {

			@Override
			protected FunctionOutput internalExecute(FunctionExecutionParameters context) {
				JsonPrimitive s1 = context.getArguments().get(PARAMETER_STRING_NAME).getAsJsonPrimitive();
				JsonPrimitive s2 = context.getArguments().get(PARAMETER_SEARCH_STRING_NAME).getAsJsonPrimitive();

				return new FunctionOutput(List.of(EventResult.outputOf(Map.of(EVENT_RESULT_NAME,
						new JsonPrimitive(function.apply(s1.getAsString(), s2.getAsString()))))));
			}

		});
	}

	public static Map.Entry<String, Function> ofEntryAsStringArrayOutput(final String name,
			BiFunction<String, String, String[]> function) {
		return Map.entry(name, new AbstractBinaryStringFunction(Namespaces.STRING, name, PARAMETER_STRING,
				PARAMETER_SEARCH_STRING, EVENT_ARRAY) {

			@Override
			protected FunctionOutput internalExecute(FunctionExecutionParameters context) {
				JsonPrimitive s1 = context.getArguments().get(PARAMETER_STRING_NAME).getAsJsonPrimitive();
				JsonPrimitive s2 = context.getArguments().get(PARAMETER_SEARCH_STRING_NAME).getAsJsonPrimitive();

				return new FunctionOutput(List.of(EventResult
						.outputOf(Map.of(EVENT_RESULT_NAME, stringFunction(s1.getAsString(), s2.getAsString())))));
			}

			public JsonArray stringFunction(String s1, String s2) {
				JsonArray stringSplitedJsonArray = new JsonArray();
				String[] splitedString = function.apply(s1, s2);
				int start = 0;
				while (start <= splitedString.length - 1) {
					stringSplitedJsonArray.add(splitedString[start]);
					start++;
				}
				return stringSplitedJsonArray;
			}
		});
	}
}
