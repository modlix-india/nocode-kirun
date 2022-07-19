package com.fincity.nocode.kirun.engine.function.math;

import static com.fincity.nocode.kirun.engine.namespaces.Namespaces.MATH;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.fincity.nocode.kirun.engine.exception.KIRuntimeException;
import com.fincity.nocode.kirun.engine.function.AbstractFunction;
import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.model.Event;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;
import com.fincity.nocode.kirun.engine.model.Parameter;
import com.fincity.nocode.kirun.engine.runtime.FunctionExecutionParameters;
import com.fincity.nocode.kirun.engine.util.string.StringFormatter;
import com.google.gson.JsonElement;

public class Max extends AbstractFunction {

	private static final String VALUE = "value";

	private static final FunctionSignature MAX_SIGNATURE = new FunctionSignature().setName("Max").setNamespace(MATH)
			.setParameters(Map.of(VALUE,
					new Parameter().setParameterName(VALUE).setSchema(Schema.ofNumber(VALUE))
							.setVariableArgument(true)))
			.setEvents(Map.ofEntries(Event.outputEventMapEntry(Map.of(VALUE, Schema.ofNumber(VALUE)))));

	@Override
	public FunctionSignature getSignature() {
		return MAX_SIGNATURE;
	}

	@Override
	protected FunctionOutput internalExecute(FunctionExecutionParameters context) {

		JsonElement jsonElement = context.getArguments().get(VALUE);

		if (!jsonElement.isJsonArray()) {
			throw new KIRuntimeException(StringFormatter.format("Expected an array but found $", jsonElement));
		}

		if (jsonElement.getAsJsonArray().isEmpty()) {

			return new FunctionOutput(List.of(EventResult.outputOf(Map.of())));
		}

		Iterator<JsonElement> iterator = jsonElement.getAsJsonArray().iterator();

		JsonElement element = iterator.next();
		double first = element.getAsDouble();

		JsonElement otherOne;

		while (iterator.hasNext()) {

			otherOne = iterator.next();
			if (first > otherOne.getAsDouble())
				continue;
			first = otherOne.getAsDouble();
			element = otherOne;

		}

		return new FunctionOutput(List.of(EventResult.outputOf(Map.of(VALUE, element))));
	}

}
