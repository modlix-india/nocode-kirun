package com.fincity.nocode.kirun.engine.function.math;

import static com.fincity.nocode.kirun.engine.function.namespaces.Namespaces.MATH;
import static com.fincity.nocode.kirun.engine.json.schema.type.SchemaType.DOUBLE;
import static com.fincity.nocode.kirun.engine.json.schema.type.SchemaType.FLOAT;
import static com.fincity.nocode.kirun.engine.json.schema.type.SchemaType.INTEGER;
import static com.fincity.nocode.kirun.engine.json.schema.type.SchemaType.LONG;
import static com.fincity.nocode.kirun.engine.json.schema.type.SchemaType.STRING;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fincity.nocode.kirun.engine.function.AbstractFunction;
import com.fincity.nocode.kirun.engine.function.util.PrimitiveUtil;
import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.json.schema.type.MultipleType;
import com.fincity.nocode.kirun.engine.json.schema.type.SchemaType;
import com.fincity.nocode.kirun.engine.model.Argument;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;
import com.fincity.nocode.kirun.engine.model.Parameter;
import com.fincity.nocode.kirun.engine.model.Result;
import com.fincity.nocode.kirun.engine.model.Returns;
import com.google.gson.JsonPrimitive;

public class Add extends AbstractFunction {

	private static final String VALUE = "value";

	private static final Schema SCHEMA = new Schema().setId(VALUE).setTitle(VALUE)
			.setType(new MultipleType().setType(Set.of(DOUBLE, FLOAT, INTEGER, LONG, STRING)));

	private static final FunctionSignature SIGNATURE = new FunctionSignature().setName("Add").setNameSpace(MATH)
			.setParameters(List.of(new Parameter().setSchema(SCHEMA).setVariableArgument(true)))
			.setReturns(new Returns().setSchema(List.of(SCHEMA)));

	@Override
	public FunctionSignature getSignature() {
		return SIGNATURE;
	}

	@Override
	protected Result internalExecute(Map<String, List<Argument>> args) {

		Double d = 0d;
		String s = "";
		SchemaType type = null;
		SchemaType newType = null;
		boolean started = false;
		for (Argument arg : args.get(VALUE)) {
			JsonPrimitive pValue = (JsonPrimitive) arg.getValue();
			newType = PrimitiveUtil.findPrimitiveType(pValue);
			if (type == null || type.ordinal() < newType.ordinal()) {
				type = newType;
				if (type == STRING)
					s = started ? d.toString() : "";
				started = true;
			}
			if (type == STRING)
				s += pValue.getAsString();
			else
				d += pValue.getAsDouble();
		}

		JsonPrimitive rValue = null;

		switch (type) {
		case STRING:
			rValue = new JsonPrimitive(s);
		case DOUBLE:
			rValue = new JsonPrimitive(Math.abs(d));
		case FLOAT:
			rValue = new JsonPrimitive(Math.abs(d.floatValue()));
		case LONG:
			rValue = new JsonPrimitive(Math.abs(d.longValue()));
		default:
			rValue = new JsonPrimitive(Math.abs(d.intValue()));
		}

		return new Result().setValue(rValue);
	}
}
