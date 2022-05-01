package com.fincity.nocode.kirun.engine.function.math;

import static com.fincity.nocode.kirun.engine.namespaces.Namespaces.MATH;

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

public class Abs extends AbstractFunction {

	private static final String VALUE = "value";

	private static final Schema SCHEMA = new Schema().setId(VALUE).setTitle(VALUE).setType(new MultipleType()
			.setType(Set.of(SchemaType.DOUBLE, SchemaType.FLOAT, SchemaType.INTEGER, SchemaType.LONG)));

	private static final FunctionSignature SIGNATURE = new FunctionSignature().setName("Abs").setNameSpace(MATH)
			.setParameters(List.of(new Parameter().setSchema(SCHEMA)))
			.setReturns(new Returns().setSchema(List.of(SCHEMA)));

	@Override
	public FunctionSignature getSignature() {
		return SIGNATURE;
	}

	@Override
	protected Result internalExecute(Map<String, List<Argument>> args) {

		JsonPrimitive pValue = (JsonPrimitive) args.get(VALUE).get(0).getValue();
		SchemaType type = PrimitiveUtil.findPrimitiveType(pValue);
		JsonPrimitive rValue = null;

		switch (type) {
		case DOUBLE:
			rValue = new JsonPrimitive(Math.abs(pValue.getAsDouble()));
			break;
		case FLOAT:
			rValue = new JsonPrimitive(Math.abs(pValue.getAsFloat()));
			break;
		case LONG:
			rValue = new JsonPrimitive(Math.abs(pValue.getAsLong()));
			break;
		default:
			rValue = new JsonPrimitive(Math.abs(pValue.getAsInt()));
		}

		return new Result().setValue(rValue);
	}
}
