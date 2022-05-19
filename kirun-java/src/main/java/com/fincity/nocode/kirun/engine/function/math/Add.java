package com.fincity.nocode.kirun.engine.function.math;

import static com.fincity.nocode.kirun.engine.json.schema.type.SchemaType.STRING;
import static com.fincity.nocode.kirun.engine.namespaces.Namespaces.MATH;

import java.util.List;
import java.util.Map;

import com.fincity.nocode.kirun.engine.function.AbstractFunction;
import com.fincity.nocode.kirun.engine.function.util.PrimitiveUtil;
import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.json.schema.type.SchemaType;
import com.fincity.nocode.kirun.engine.model.Argument;
import com.fincity.nocode.kirun.engine.model.Event;
import com.fincity.nocode.kirun.engine.model.EventResult;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;
import com.fincity.nocode.kirun.engine.model.Parameter;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;

public class Add extends AbstractFunction {

	private static final String VALUE = "value";

	private static final FunctionSignature SIGNATURE = new FunctionSignature().setName("Add")
	        .setNamespace(MATH)
	        .setParameters(List.of(new Parameter().setSchema(Schema.NUMBER)
	                .setVariableArgument(true)))
	        .setEvents(Map.ofEntries(Event.outputEventMapEntry(Map.of(VALUE, Schema.NUMBER))));

	@Override
	public FunctionSignature getSignature() {
		return SIGNATURE;
	}

	@Override
	protected EventResult internalExecute(Map<String, List<Argument>> args) {

		Double d = 0d;
		StringBuilder s = new StringBuilder("");
		SchemaType type = null;
		SchemaType newType = null;
		boolean started = false;
		for (Argument arg : args.get(VALUE)) {
			JsonPrimitive pValue = (JsonPrimitive) arg.getValue();
			newType = PrimitiveUtil.findPrimitiveType(pValue);
			if (type == null || type.ordinal() < newType.ordinal()) {
				type = newType;
				if (type == STRING)
					s = new StringBuilder(started ? d.toString() : "");
				started = true;
			}
			if (type == STRING)
				s.append(pValue.getAsString());
			else
				d += pValue.getAsDouble();
		}

		if (type == null)
			return new EventResult().setValue(JsonNull.INSTANCE);

		JsonPrimitive rValue = null;

		switch (type) {
		case STRING:
			rValue = new JsonPrimitive(s.toString());
			break;
		case DOUBLE:
			rValue = new JsonPrimitive(Math.abs(d));
			break;
		case FLOAT:
			rValue = new JsonPrimitive(Math.abs(d.floatValue()));
			break;
		case LONG:
			rValue = new JsonPrimitive(Math.abs(d.longValue()));
			break;
		default:
			rValue = new JsonPrimitive(Math.abs(d.intValue()));
		}

		return new EventResult().setValue(rValue);
	}
}
