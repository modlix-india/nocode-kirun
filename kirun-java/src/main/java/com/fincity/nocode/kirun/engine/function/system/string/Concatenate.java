package com.fincity.nocode.kirun.engine.function.system.string;

import static com.fincity.nocode.kirun.engine.namespaces.Namespaces.STRING;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.fincity.nocode.kirun.engine.function.AbstractFunction;
import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.json.schema.type.SchemaType;
import com.fincity.nocode.kirun.engine.json.schema.type.SingleType;
import com.fincity.nocode.kirun.engine.model.Event;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;
import com.fincity.nocode.kirun.engine.model.Parameter;
import com.fincity.nocode.kirun.engine.runtime.FunctionExecutionParameters;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class Concatenate extends AbstractFunction {

	static final String VALUE = "value";

	private static final Schema SCHEMA = new Schema().setName(VALUE).setType(new SingleType(SchemaType.STRING));

	private static final FunctionSignature SIGNATURE = new FunctionSignature().setName("Concatenate")
			.setNamespace(STRING)
			.setParameters(Map.of(VALUE, new Parameter().setSchema(SCHEMA).setVariableArgument(true)))
			.setEvents(Map.ofEntries(Event.outputEventMapEntry(Map.of(VALUE, Schema.ofString(VALUE)))));

	@Override
	public FunctionSignature getSignature() {
		return SIGNATURE;
	}

	@Override
	protected FunctionOutput internalExecute(FunctionExecutionParameters context) {

		JsonArray arugments = context.getArguments().get(VALUE).getAsJsonArray();

		StringBuilder concatenatedString = new StringBuilder();

		Iterator<JsonElement> iterator = arugments.iterator();

		while (iterator.hasNext()) {
			concatenatedString.append(iterator.next().getAsString());
		}

		return new FunctionOutput(
				List.of(EventResult.outputOf(Map.of(VALUE, new JsonPrimitive(concatenatedString.toString())))));
	}
}
