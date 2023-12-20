package com.fincity.nocode.kirun.engine.function.system.array;

import static com.fincity.nocode.kirun.engine.namespaces.Namespaces.SYSTEM_ARRAY;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fincity.nocode.kirun.engine.function.reactive.AbstractReactiveFunction;
import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.json.schema.type.SchemaType;
import com.fincity.nocode.kirun.engine.json.schema.type.Type;
import com.fincity.nocode.kirun.engine.model.Event;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;
import com.fincity.nocode.kirun.engine.model.Parameter;
import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;

public abstract class AbstractArrayFunction extends AbstractReactiveFunction {

	private static final String ELEMENT = "element";
	protected static final String EVENT_INDEX_NAME = "index";
	protected static final String EVENT_RESULT_NAME = "result";
	protected static final String EACH_SOURCE = "eachSource";

	protected static final Event EVENT_INDEX = new Event().setName(Event.OUTPUT)
			.setParameters(Map.of(EVENT_INDEX_NAME, Schema.ofInteger(EVENT_INDEX_NAME)));

	protected static final Event EVENT_RESULT_INTEGER = new Event().setName(Event.OUTPUT)
			.setParameters(Map.of(EVENT_RESULT_NAME, Schema.ofInteger(EVENT_RESULT_NAME)));

	protected static final Event EVENT_RESULT_BOOLEAN = new Event().setName(Event.OUTPUT)
			.setParameters(Map.of(EVENT_RESULT_NAME, Schema.ofBoolean(EVENT_RESULT_NAME)));
	
	protected static final Event EVENT_RESULT_STRING = new Event().setName(Event.OUTPUT)
			.setParameters(Map.of(EVENT_RESULT_NAME, Schema.ofBoolean(EVENT_RESULT_NAME)));

	protected static final Event EVENT_RESULT_ARRAY = new Event().setName(Event.OUTPUT).setParameters(
			Map.of(EVENT_RESULT_NAME, Schema.ofArray(EVENT_RESULT_NAME, Schema.ofAny(EVENT_RESULT_NAME))));

	protected static final Event EVENT_RESULT_ANY = new Event().setName(Event.OUTPUT)
			.setParameters(Map.of(EVENT_RESULT_NAME, Schema.ofAny(EVENT_RESULT_NAME)));

	protected static final Parameter PARAMETER_ROTATE_LENGTH = Parameter.of("rotateLength",
			Schema.ofInteger("rotateLength").setDefaultValue(new JsonPrimitive(1)).setMinimum(1));

	protected static final Parameter PARAMETER_INT_LENGTH = Parameter.of("length",
			Schema.ofInteger("length").setDefaultValue(new JsonPrimitive(-1)));

	protected static final Parameter PARAMETER_ARRAY_FIND = Parameter.of("find",
			Schema.ofArray("eachFind", Schema.ofAny("eachFind")));

	protected static final Parameter PARAMETER_INT_SOURCE_FROM = Parameter.of("srcFrom",
			Schema.ofInteger("srcFrom").setDefaultValue(new JsonPrimitive(0)).setMinimum(0));

	protected static final Parameter PARAMETER_INT_SECOND_SOURCE_FROM = Parameter.of("secondSrcFrom",
			Schema.ofInteger("secondSrcFrom").setDefaultValue(new JsonPrimitive(0)).setMinimum(0));

	protected static final Parameter PARAMETER_INT_FIND_FROM = Parameter.of("findFrom",
			Schema.ofInteger("findFrom").setDefaultValue(new JsonPrimitive(0)));

	protected static final Parameter PARAMETER_INT_OFFSET = Parameter.of("offset",
			Schema.ofInteger("offset").setDefaultValue(new JsonPrimitive(0)));

	protected static final Parameter PARAMETER_ARRAY_SOURCE = Parameter.of("source",
			Schema.ofArray(EACH_SOURCE, Schema.ofAny(EACH_SOURCE)));

	protected static final Parameter PARAMETER_ARRAY_SECOND_SOURCE = Parameter.of("secondSource",
			Schema.ofArray("eachSecondSource", Schema.ofAny("eachSecondSource")));

	protected static final Parameter PARAMETER_ARRAY_SOURCE_PRIMITIVE = Parameter.of("source",
			Schema.ofArray(EACH_SOURCE, new Schema().setName(EACH_SOURCE).setType(Type.of(SchemaType.STRING,
					SchemaType.NULL, SchemaType.DOUBLE, SchemaType.FLOAT, SchemaType.LONG, SchemaType.INTEGER))));

	protected static final Parameter PARAMETER_BOOLEAN_DEEP_COPY = Parameter.of("deepCopy",
			Schema.ofBoolean("deepCopy").setDefaultValue(new JsonPrimitive(true)));

	protected static final Parameter PARAMETER_BOOLEAN_ASCENDING = Parameter.of("ascending",
			Schema.ofBoolean("ascending").setDefaultValue(new JsonPrimitive(true)));

	public static final Parameter PARAMETER_KEY_PATH = Parameter.of("keyPath",
			Schema.ofString("keyPath").setDefaultValue(new JsonPrimitive("")));
	
	public static final Parameter PARAMETER_DELIMITER = Parameter.of("delimiter",
			Schema.ofString("delimiter").setDefaultValue(new JsonPrimitive("")));

	protected static final Parameter PARAMETER_FIND_PRIMITIVE = Parameter.of("findPrimitive", Schema.of("findPrimitive",
			SchemaType.STRING, SchemaType.DOUBLE, SchemaType.FLOAT, SchemaType.INTEGER, SchemaType.LONG));

	protected static final Parameter PARAMETER_ANY = Parameter.of(ELEMENT, Schema.ofAny(ELEMENT));
	
	protected static final Parameter PARAMETER_ANY_VAR_ARGS = Parameter.of(ELEMENT, Schema.ofAny(ELEMENT))
			.setVariableArgument(true);

	protected static final Parameter PARAMETER_ARRAY_RESULT = Parameter.of(EVENT_RESULT_NAME,
			Schema.ofArray("eachResult", Schema.ofAny("eachResult")));

	private FunctionSignature signature;

	protected AbstractArrayFunction(String functionName, List<Parameter> parameters, Event event) {

		Map<String, Parameter> paramMap = new HashMap<>();
		for (Parameter param : parameters)
			paramMap.put(param.getParameterName(), param);

		this.signature = new FunctionSignature().setNamespace(SYSTEM_ARRAY).setName(functionName)
				.setParameters(paramMap).setEvents(Map.of(event.getName(), event));
	}

	@Override
	public FunctionSignature getSignature() {
		return this.signature;
	}

	public JsonArray duplicateArray(JsonArray array) {
		JsonArray newArray = new JsonArray();
		for (int i = 0; i < array.size(); i++)
			newArray.add(array.get(i));
		return newArray;
	}
}
