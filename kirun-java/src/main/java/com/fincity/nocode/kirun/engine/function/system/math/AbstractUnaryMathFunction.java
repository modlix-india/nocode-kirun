package com.fincity.nocode.kirun.engine.function.system.math;

import static com.fincity.nocode.kirun.engine.namespaces.Namespaces.MATH;

import java.util.List;
import java.util.Map;
import java.util.function.DoubleUnaryOperator;
import java.util.function.UnaryOperator;

import com.fincity.nocode.kirun.engine.function.reactive.AbstractReactiveFunction;
import com.fincity.nocode.kirun.engine.function.reactive.ReactiveFunction;
import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.json.schema.type.SchemaType;
import com.fincity.nocode.kirun.engine.model.Event;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;
import com.fincity.nocode.kirun.engine.model.Parameter;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.fincity.nocode.kirun.engine.util.primitive.PrimitiveUtil;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

public abstract class AbstractUnaryMathFunction extends AbstractReactiveFunction {

	static final String VALUE = "value";

	private final FunctionSignature signature;

	protected AbstractUnaryMathFunction(String functionName) {

		this.signature = new FunctionSignature().setNamespace(MATH)
		        .setName(functionName)
		        .setParameters(Map.of(VALUE, new Parameter().setParameterName(VALUE)
		                .setSchema(Schema.ofNumber(VALUE))))
		        .setEvents(Map.ofEntries(Event.outputEventMapEntry(Map.of(VALUE, Schema.ofNumber(VALUE)))));
	}

	@Override
	public FunctionSignature getSignature() {
		return this.signature;
	}

	@Override
	protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {

		JsonElement pValue = context.getArguments()
		        .get(VALUE);

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

		return Mono.just(new FunctionOutput(List.of(EventResult.outputOf(Map.of(VALUE, (JsonElement) rValue)))));
	}

	@Override
	public Map<String, Event> getProbableEventSignature(Map<String, List<Schema>> probableParameters) {

		Schema s = probableParameters.get(VALUE)
		        .get(0);
		return Map.ofEntries(Event.outputEventMapEntry(Map.of(VALUE, s)));
	}

	public abstract Number mathFunction(Number n);

	public static Map.Entry<String, ReactiveFunction> ofEntry(final String name, UnaryOperator<Number> function) {
		return Map.entry(name, new AbstractUnaryMathFunction(name) {

			@Override
			public Number mathFunction(Number n) {
				return function.apply(n);
			}
		});
	}

	public static Map.Entry<String, ReactiveFunction> ofEntryDouble(String name, DoubleUnaryOperator function) {

		return ofEntry(name, n -> (Number) function.applyAsDouble(n.doubleValue()));
	}

	public static Map.Entry<String, ReactiveFunction> ofEntryAnyType(String name,
	        Map<Class<? extends Number>, UnaryOperator<Number>> map) {

		return ofEntry(name, n -> map.get(n.getClass())
		        .apply(n));
	}
}