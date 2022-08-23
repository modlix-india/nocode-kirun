package com.fincity.nocode.kirun.engine.function.system.math;

import static com.fincity.nocode.kirun.engine.namespaces.Namespaces.MATH;

import java.util.List;
import java.util.Map;
import java.util.Random;

import com.fincity.nocode.kirun.engine.function.AbstractFunction;
import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.model.Event;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;
import com.fincity.nocode.kirun.engine.model.Parameter;
import com.fincity.nocode.kirun.engine.runtime.FunctionExecutionParameters;

import com.google.gson.JsonPrimitive;

public class RandomFloat extends AbstractFunction {

	private static final String MIN_VALUE = "minValue";

	private static final String MAX_VALUE = "maxValue";

	private static final String VALUE = "value";

	private Random rand = new Random();

	@Override
	public FunctionSignature getSignature() {
		return new FunctionSignature().setName("RandomFloat").setNamespace(MATH)
				.setParameters(Map.of(MIN_VALUE,
						new Parameter().setParameterName(MIN_VALUE)
								.setSchema(Schema.ofFloat(MIN_VALUE).setDefaultValue(new JsonPrimitive(0.0f))),
						MAX_VALUE,
						new Parameter().setParameterName(MAX_VALUE).setSchema(
								Schema.ofFloat(MAX_VALUE).setDefaultValue(new JsonPrimitive(Float.MAX_VALUE)))))
				.setEvents(Map.ofEntries(Event.outputEventMapEntry(Map.of(VALUE, Schema.ofFloat(VALUE)))));
	}

	@Override
	protected FunctionOutput internalExecute(FunctionExecutionParameters context) {

		float minValue = context.getArguments().get(MIN_VALUE).getAsFloat();

		float maxValue = context.getArguments().get(MAX_VALUE).getAsFloat();

		float randValue = rand.nextFloat(minValue, maxValue == Float.MAX_VALUE ? maxValue : maxValue + 1f);

		return new FunctionOutput(List.of(EventResult.outputOf(Map.of(VALUE, new JsonPrimitive(randValue)))));
	}

}