package com.fincity.nocode.kirun.engine.function.string;

import static com.fincity.nocode.kirun.engine.namespaces.Namespaces.STRING;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fincity.nocode.kirun.engine.function.AbstractFunction;
import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.json.schema.type.SchemaType;
import com.fincity.nocode.kirun.engine.json.schema.type.SingleType;
import com.fincity.nocode.kirun.engine.model.Argument;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;
import com.fincity.nocode.kirun.engine.model.Parameter;
import com.fincity.nocode.kirun.engine.model.Result;
import com.fincity.nocode.kirun.engine.model.Returns;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class Concatenate extends AbstractFunction {

	private static final String VALUE = "value";

	private static final Schema SCHEMA = new Schema().setId(VALUE).setTitle(VALUE)
			.setType(new SingleType().setType(SchemaType.STRING));

	private static final FunctionSignature SIGNATURE = new FunctionSignature().setName("Concatenate")
			.setNameSpace(STRING).setParameters(List.of(new Parameter().setSchema(SCHEMA).setVariableArgument(true)))
			.setReturns(new Returns().setSchema(List.of(SCHEMA)));

	@Override
	public FunctionSignature getSignature() {
		return SIGNATURE;
	}

	@Override
	protected Result internalExecute(Map<String, List<Argument>> args) {

		return new Result().setValue(new JsonPrimitive(args.get(VALUE).stream().sorted().map(Argument::getValue)
				.map(JsonElement::getAsString).collect(Collectors.joining(""))));
	}
}
