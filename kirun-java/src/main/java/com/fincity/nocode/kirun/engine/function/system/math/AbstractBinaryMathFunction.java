package com.fincity.nocode.kirun.engine.function.system.math;

import static com.fincity.nocode.kirun.engine.namespaces.Namespaces.MATH;

import java.util.List;
import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.function.DoubleBinaryOperator;

import com.fincity.nocode.kirun.engine.function.AbstractFunction;
import com.fincity.nocode.kirun.engine.function.Function;
import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.model.Event;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;
import com.fincity.nocode.kirun.engine.model.Parameter;
import com.fincity.nocode.kirun.engine.runtime.FunctionExecutionParameters;
import com.google.gson.JsonPrimitive;

public abstract class AbstractBinaryMathFunction extends AbstractFunction {

	private static final String VALUE1 = "value1";

	private static final String VALUE2 = "value2";

	private static final String VALUE = "value";

	private final FunctionSignature signature;

	protected AbstractBinaryMathFunction(String functionName) {
		this.signature = new FunctionSignature().setName(functionName).setNamespace(MATH)
				.setParameters(
						Map.of(VALUE1, new Parameter().setParameterName(VALUE1).setSchema(Schema.ofNumber(VALUE1)),
								VALUE2, new Parameter().setParameterName(VALUE2).setSchema(Schema.ofNumber(VALUE2))))
				.setEvents(Map.ofEntries(Event.outputEventMapEntry(Map.of(VALUE, Schema.ofDouble(VALUE)))));
	}

	@Override
	public FunctionSignature getSignature() {
		return this.signature;
	}

	@Override
	protected FunctionOutput internalExecute(FunctionExecutionParameters context) {

		JsonPrimitive v1 = (JsonPrimitive) context.getArguments().get(VALUE1);
		JsonPrimitive v2 = (JsonPrimitive) context.getArguments().get(VALUE2);

		return new FunctionOutput(List.of(EventResult
				.outputOf(Map.of(VALUE, new JsonPrimitive(this.mathFunction(v1.getAsNumber(), v2.getAsNumber()))))));

	}

	public abstract Number mathFunction(Number v1, Number v2);

	public static Map.Entry<String, Function> ofEntry(final String name, BinaryOperator<Number> function) {
		return Map.entry(name, new AbstractBinaryMathFunction(name) {

			@Override
			public Number mathFunction(Number v1, Number v2) {
				return function.apply(v1, v2);
			}
		});
	}

	public static Map.Entry<String, Function> ofEntryDouble(String functionName, DoubleBinaryOperator function) {
		return ofEntry(functionName,
				(variable1, variable2) -> function.applyAsDouble(variable1.doubleValue(), variable2.doubleValue()));
	}
}
