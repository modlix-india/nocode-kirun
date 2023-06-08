package com.fincity.nocode.kirun.engine.function.system.math;

import static com.fincity.nocode.kirun.engine.namespaces.Namespaces.MATH;

import java.util.List;
import java.util.Map;

import com.fincity.nocode.kirun.engine.exception.KIRuntimeException;
import com.fincity.nocode.kirun.engine.function.reactive.AbstractReactiveFunction;
import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.json.schema.type.SchemaType;
import com.fincity.nocode.kirun.engine.json.schema.type.Type;
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

public class AbstractRandom extends AbstractReactiveFunction {

	private static final String MIN_VALUE = "minValue";

	private static final String MAX_VALUE = "maxValue";

	private static final String VALUE = "value";

	private java.util.Random rand = new java.util.Random();

	private FunctionSignature signature;

	protected AbstractRandom(String functionName, SchemaType schemaType) {

		this.signature = new FunctionSignature().setName(functionName)
		        .setNamespace(MATH)
		        .setParameters(Map.of(MIN_VALUE, new Parameter().setParameterName(MIN_VALUE)
		                .setSchema(new Schema().setType(Type.of(schemaType))
		                        .setDefaultValue(getDefaultMinValue(schemaType))),
		                MAX_VALUE, new Parameter().setParameterName(MAX_VALUE)
		                        .setSchema(new Schema().setType(Type.of(schemaType))
		                                .setDefaultValue(getDefaultMaxValue(schemaType)))))
		        .setEvents(Map.ofEntries(
		                Event.outputEventMapEntry(Map.of(VALUE, new Schema().setType(Type.of(schemaType))))));
	}

	@Override
	public FunctionSignature getSignature() {
		return this.signature;
	}

	@Override
	protected Mono<FunctionOutput> internalExecute(ReactiveFunctionExecutionParameters context) {

		JsonElement minvalue = context.getArguments()
		        .get(MIN_VALUE);

		JsonElement maxValue = context.getArguments()
		        .get(MAX_VALUE);

		Tuple2<SchemaType, Number> primitiveTypeTupleOfMin = PrimitiveUtil
		        .findPrimitiveNumberType(minvalue.getAsJsonPrimitive());

		if (minvalue.getAsDouble() > maxValue.getAsDouble())
			throw new KIRuntimeException("Given minimum value is more than the maximum value.");

		JsonPrimitive result = this.randomFunction(minvalue.getAsJsonPrimitive(), maxValue.getAsJsonPrimitive(),
		        primitiveTypeTupleOfMin.getT1());

		return Mono.just(new FunctionOutput(List.of(EventResult.outputOf(Map.of(VALUE, result)))));

	}

	public JsonPrimitive randomFunction(JsonPrimitive min, JsonPrimitive max, SchemaType st) {

		switch (st) {
		case DOUBLE: {
			return new JsonPrimitive(rand.nextDouble(min.getAsDouble(),
			        max.getAsDouble() == Double.MAX_VALUE ? max.getAsDouble() : max.getAsDouble() + 1d));
		}
		case INTEGER: {
			return new JsonPrimitive(rand.nextInt(min.getAsInt(),
			        max.getAsInt() == Integer.MAX_VALUE ? max.getAsInt() : max.getAsInt() + 1));
		}
		case FLOAT: {
			return new JsonPrimitive(rand.nextFloat(min.getAsFloat(),
			        max.getAsFloat() == Float.MAX_VALUE ? max.getAsFloat() : max.getAsFloat() + 1f));
		}
		case LONG: {
			return new JsonPrimitive(rand.nextLong(min.getAsLong(),
			        max.getAsLong() == Long.MAX_VALUE ? max.getAsLong() : max.getAsLong() + 1));
		}
		default:
			throw new IllegalArgumentException("Unexpected value: ");
		}
	}

	public JsonPrimitive getDefaultMinValue(SchemaType schema) {

		if (schema.compareTo(SchemaType.INTEGER) == 0)
			return new JsonPrimitive(0);
		else if (schema.compareTo(SchemaType.LONG) == 0)
			return new JsonPrimitive(0l);
		else if (schema.compareTo(SchemaType.FLOAT) == 0)
			return new JsonPrimitive(0.0f);
		else
			return new JsonPrimitive(0.0d);

	}

	public JsonPrimitive getDefaultMaxValue(SchemaType schema) {

		if (schema.compareTo(SchemaType.INTEGER) == 0)
			return new JsonPrimitive(Integer.MAX_VALUE);
		else if (schema.compareTo(SchemaType.FLOAT) == 0)
			return new JsonPrimitive(Float.MAX_VALUE);
		else if (schema.compareTo(SchemaType.LONG) == 0)
			return new JsonPrimitive(Long.MAX_VALUE);
		else
			return new JsonPrimitive(Double.MAX_VALUE);

	}

}