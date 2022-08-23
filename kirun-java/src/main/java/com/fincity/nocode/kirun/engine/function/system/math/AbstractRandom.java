package com.fincity.nocode.kirun.engine.function.system.math;

import static com.fincity.nocode.kirun.engine.namespaces.Namespaces.MATH;

import java.util.List;
import java.util.Map;
import java.util.Random;

import com.fincity.nocode.kirun.engine.function.AbstractFunction;
import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.json.schema.type.SchemaType;
import com.fincity.nocode.kirun.engine.model.Event;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;
import com.fincity.nocode.kirun.engine.model.Parameter;
import com.fincity.nocode.kirun.engine.runtime.FunctionExecutionParameters;
import com.fincity.nocode.kirun.engine.util.primitive.PrimitiveUtil;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import reactor.util.function.Tuple2;

public class AbstractRandom extends AbstractFunction {

	private static final String MIN_VALUE = "minValue";

	private static final String MAX_VALUE = "maxValue";

	private static final String VALUE = "value";

	private Random rand = new Random();

	private FunctionSignature signature;

	protected AbstractRandom(String functionName, Schema schemaType) {
		this.signature = new FunctionSignature().setName(functionName).setNamespace(MATH)
				.setParameters(Map.of(MIN_VALUE, new Parameter()
						.setParameterName(MIN_VALUE).setSchema(schemaType.setDefaultValue(new JsonPrimitive(0))),
						MAX_VALUE,
						new Parameter().setParameterName(MAX_VALUE)
								.setSchema(schemaType.setDefaultValue(new JsonPrimitive(Integer.MAX_VALUE)))))
				.setEvents(Map.ofEntries(Event.outputEventMapEntry(Map.of(VALUE, schemaType))));
	}

	@Override
	public FunctionSignature getSignature() {
		return this.signature;
	}
	

	@Override
	protected FunctionOutput internalExecute(FunctionExecutionParameters context) {

		int minValue = context.getArguments().get(MIN_VALUE).getAsInt();

		int maxValue = context.getArguments().get(MAX_VALUE).getAsInt();

		int randValue = rand.nextInt(minValue, maxValue == Integer.MAX_VALUE ? maxValue : maxValue + 1);

		return new FunctionOutput(List.of(EventResult.outputOf(Map.of(VALUE, new JsonPrimitive(randValue)))));

	}
	
//	public void nothing() {
//		JsonElement pValue = context.getArguments().get(VALUE);
//
//		Tuple2<SchemaType, Number> primitiveTypeTuple = PrimitiveUtil
//				.findPrimitiveNumberType(pValue.getAsJsonPrimitive());
//		JsonPrimitive rValue = null;
//
//		switch (primitiveTypeTuple.getT1()) {
//		case DOUBLE:
//			rValue = new JsonPrimitive(this.mathFunction(Double.class.cast(primitiveTypeTuple.getT2())));
//			break;
//		case FLOAT:
//			rValue = new JsonPrimitive(this.mathFunction(Float.class.cast(primitiveTypeTuple.getT2())));
//			break;
//		case LONG:
//			rValue = new JsonPrimitive(this.mathFunction(Long.class.cast(primitiveTypeTuple.getT2())));
//			break;
//		case INTEGER:
//			rValue = new JsonPrimitive(this.mathFunction(Integer.class.cast(primitiveTypeTuple.getT2())));
//			break;
//		default:
//			rValue = new JsonPrimitive(this.mathFunction(pValue.getAsInt()));
//		}
//
//		return new FunctionOutput(List.of(EventResult.outputOf(Map.of(VALUE, (JsonElement) rValue))));
//	}

}