package com.fincity.nocode.kirun.engine.function.system;

import java.util.List;
import java.util.Map;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import com.fincity.nocode.kirun.engine.function.AbstractFunction;
import com.fincity.nocode.kirun.engine.function.Function;
import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.json.schema.type.SchemaType;
import com.fincity.nocode.kirun.engine.json.schema.type.Type;
import com.fincity.nocode.kirun.engine.model.Event;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;
import com.fincity.nocode.kirun.engine.model.Parameter;
import com.fincity.nocode.kirun.engine.namespaces.Namespaces;
import com.fincity.nocode.kirun.engine.runtime.FunctionExecutionParameters;
import com.fincity.nocode.kirun.engine.util.primitive.PrimitiveUtil;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import reactor.util.function.Tuple2;

public abstract class AbstractUnaryFunction<T> extends AbstractFunction {

	static final String VALUE = "value";

	private final FunctionSignature signature;

	protected AbstractUnaryFunction(String namespace, String functionName, SchemaType... schemaType) {

		if (schemaType == null || schemaType.length == 0) {
			schemaType = new SchemaType[] { SchemaType.DOUBLE };
		}

		this.signature = new FunctionSignature().setNamespace(namespace).setName(functionName)
				.setParameters(Map.of(VALUE,
						new Parameter().setParameterName(VALUE)
								.setSchema(new Schema().setName(VALUE).setType(Type.of(schemaType)))))
				.setEvents(Map.ofEntries(Event
						.outputEventMapEntry(Map.of(VALUE, new Schema().setName(VALUE).setType(Type.of(schemaType))))));

	}

	@Override
	public FunctionSignature getSignature() {
		return this.signature;
	}

	@Override
	public Map<String, Event> getProbableEventSignature(Map<String, List<Schema>> probableParameters) {

		Schema s = probableParameters.get(VALUE).get(0);
		return Map.ofEntries(Event.outputEventMapEntry(Map.of(VALUE, s)));
	}

	public static Map.Entry<String, Function> ofEntryString(final String name, UnaryOperator<String> function) {

		return Map.entry(name, new AbstractUnaryFunction<String>(Namespaces.STRING, name, SchemaType.STRING) {

			@Override
			protected FunctionOutput internalExecute(FunctionExecutionParameters context) {
				String input = context.getArguments().get(VALUE).getAsString();

				return new FunctionOutput(
						List.of(EventResult.outputOf(Map.of(VALUE, new JsonPrimitive(function.apply(input))))));
			}

		}

		);
	}

	public static Map.Entry<String, Function> ofEntryStringBooleanOutput(final String name,
			Predicate<String> function) {
		return Map.entry(name, new AbstractUnaryFunction<String>(Namespaces.STRING, name, SchemaType.STRING) {

			@Override
			protected FunctionOutput internalExecute(FunctionExecutionParameters context) {
				String input = context.getArguments().get(VALUE).getAsString();

				return new FunctionOutput(
						List.of(EventResult.outputOf(Map.of(VALUE, new JsonPrimitive(function.test(input))))));
			}
		});
	}

	public static Map.Entry<String, Function> ofEntryAnyTypeToString(final String name) {
		return Map.entry(name, new AbstractUnaryFunction<String>(Namespaces.STRING, name) {

			@Override
			protected FunctionOutput internalExecute(FunctionExecutionParameters context) {
				String input = context.getArguments().get(VALUE).getAsString();

				return new FunctionOutput(List.of(EventResult.outputOf(Map.of(VALUE, new JsonPrimitive(input)))));
			}
		});
	}

	public static Map.Entry<String, Function> ofEntryNumber(final String name, UnaryOperator<Number> function,
			SchemaType... schemaTypes) {
		return Map.entry(name, new AbstractUnaryFunction<Number>(Namespaces.MATH, name, schemaTypes) {

			@Override
			protected FunctionOutput internalExecute(FunctionExecutionParameters context) {

				JsonElement pValue = context.getArguments().get(VALUE);

				Tuple2<SchemaType, Number> primitiveTypeTuple = PrimitiveUtil
						.findPrimitiveNumberType(pValue.getAsJsonPrimitive());
				JsonPrimitive rValue = null;

				switch (primitiveTypeTuple.getT1()) {
				case DOUBLE:
					rValue = new JsonPrimitive(this.mathFunction(Double.class.cast(primitiveTypeTuple.getT2())));
					break;
				case FLOAT:
					rValue = new JsonPrimitive(this.mathFunction(Float.class.cast(primitiveTypeTuple.getT2())));
					break;
				case LONG:
					rValue = new JsonPrimitive(this.mathFunction(Long.class.cast(primitiveTypeTuple.getT2())));
					break;
				case INTEGER:
					rValue = new JsonPrimitive(this.mathFunction(Integer.class.cast(primitiveTypeTuple.getT2())));
					break;
				default:
					rValue = new JsonPrimitive(this.mathFunction(pValue.getAsInt()));
				}

				return new FunctionOutput(List.of(EventResult.outputOf(Map.of(VALUE, (JsonElement) rValue))));
			}

			public Number mathFunction(Number n) {
				return function.apply(n);
			}

		});
	}

	public static Map.Entry<String, Function> ofEntryDouble(String name, DoubleUnaryOperator function,
			SchemaType... schema) {

		return ofEntryNumber(name, n -> (Number) function.applyAsDouble(n.doubleValue()), schema);

	}

	public static Map.Entry<String, Function> ofEntryAnyType(String name,
			Map<Class<? extends Number>, UnaryOperator<Number>> map, SchemaType... schemaTypes) {

		return ofEntryNumber(name, n -> map.get(n.getClass()).apply(n), schemaTypes);

	}

}